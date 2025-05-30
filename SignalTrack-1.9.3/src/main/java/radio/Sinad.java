package radio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

import java.io.IOException;

public class Sinad implements Runnable {

    private final List<SinadListener> _listeners = new ArrayList<>();

    private static final int EXTERNAL_BUFFER_SIZE = 1152;
    private static final int AVERAGING_FACTOR = 200;
    private static final double MIN_SINAD = -30.0;

    private static final Logger log = Logger.getLogger(Sinad.class.getName());
    private TargetDataLine line;
    private AudioInputStream audioInputStream;
    private final AudioFormat audioFormat;
    private final float sampleRate;
    private final boolean isBigEndian;
    private int si = 0;
    private int sndi = 0;
    private int ndi = 0;
    private double s = 0;
    private double snd = 0;
    private double nd = 0;
    private double m_snd;
    private double m_nd;
    private double m_s;
    private double m_sinadFinal = 0;
    private double sinadAvg = 0;
    private int avg = 0;
    private boolean sinadRun = true;

    public Sinad() {
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000.0F, 16, 2, 4, 48000.0F, false);

        sampleRate = audioFormat.getSampleRate();
        isBigEndian = audioFormat.isBigEndian();

        final DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(audioFormat);
                audioInputStream = new AudioInputStream(line);
            } catch (final LineUnavailableException e) {
                log.log(Level.WARNING, e.getMessage());
            }
        }
    }

    public void startSinad() {
        if (line != null) {
            line.start();
        }
    }

    public void stopSinad() {
        if (line != null) {
            line.stop();
            line.drain();
            line.close();
        }
        sinadRun = false;
    }

    public double getSinad() {
        return m_sinadFinal;
    }

    public double getSignalLevel() {
        return m_s;
    }

    public double getSignalNoiseAndDistortionLevel() {
        return m_snd;
    }

    public double getNoiseAndDistortionLevel() {
        return m_nd;
    }

    public double getSINADFloor() {
        return MIN_SINAD;
    }

    @Override
    public void run() {
        int nBytesRead = 0;
        final byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        while ((nBytesRead != -1) && sinadRun) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (final IOException ex) {
                log.log(Level.SEVERE, ex.getMessage());
            }

            // Calculate the length in seconds of the sample
            final float T = abData.length / audioFormat.getFrameRate();

            // Calculate the number of equidistant points in time
            final int n = (int) (T * sampleRate) / 2;

            // Calculate the time interval at each equidistant point
            final float h = (T / n);

            // this array is the value of the signal at time i*h
            final int[] x = new int[n];

            // convert each pair of byte values from the byte array to an Endian value
            for (int i = 0; i < (n * 2); i += 2) {
                int b1 = abData[i];
                int b2 = abData[i + 1];
                if (b1 < 0) {
                    b1 += 0x100;
                }
                if (b2 < 0) {
                    b2 += 0x100;
                }

                final int value = !isBigEndian ? (b1 << 8) + b2 : b1 + (b2 << 8);

                x[i / 2] = value;
            }

            // do the DFT for each value of x sub j and store as f sub j
            final double[] f = new double[n / 2];

            for (int j = 2; j <= 18; j++) {

                double firstSummation = 0;
                double secondSummation = 0;

                for (int k = 0; k < n; k++) {
                    final double twoPInjk = ((2 * Math.PI) / n) * (j * k);
                    firstSummation += x[k] * Math.cos(twoPInjk);
                    secondSummation += x[k] * Math.sin(twoPInjk);
                }

                f[j] = Math.abs(Math.sqrt(Math.pow(firstSummation, 2) + Math.pow(secondSummation, 2)));

                final double amplitude = (2 * f[j]) / n;
                final double frequency = ((j * h) / T) * sampleRate * 2;

                if ((frequency >= 300.0) && (frequency <= 3000.0)) {
                    snd += (Math.pow(amplitude, 3));
                    sndi++;
                }

                if (((frequency >= 300.0) && (frequency <= 999.0)) || ((frequency >= 1001.0) && (frequency <= 3000.0))) {
                    nd += (Math.pow(amplitude, 3));
                    ndi++;
                }

                if ((frequency >= 999.0) && (frequency <= 1001.0)) {
                    s += (Math.pow(amplitude, 3));
                    si++;
                }

            }
            m_snd = snd / sndi;
            m_nd = nd / ndi;
            m_s = s / si;
            snd = 0;
            nd = 0;
            s = 0;
            sndi = 0;
            ndi = 0;
            si = 0;
            final double sinad = Math.max(20.0 * Math.log10(m_snd / m_nd), 0);
            if (avg < AVERAGING_FACTOR) {
                sinadAvg += sinad;
                avg++;
            } else {
                m_sinadFinal = sinadAvg / AVERAGING_FACTOR;
                sinadAvg = 0;
                avg = 0;
                fireSinadChangedEvent();
            }
        }
        m_sinadFinal = 0;
        fireSinadChangedEvent();
    }

    public synchronized void addSinadListener(SinadListener l) {
        _listeners.add(l);
    }

    public synchronized void removeSinadListener(SinadListener l) {
        _listeners.remove(l);
    }

    private synchronized void fireSinadChangedEvent() {
    	final SinadEvent sinadEvent = new SinadEvent(this, m_sinadFinal);
    	final Iterator<SinadListener> listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            (listeners.next()).sinadChanged(sinadEvent);
        }
    }

}
