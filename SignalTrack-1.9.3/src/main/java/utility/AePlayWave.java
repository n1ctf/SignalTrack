package utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AePlayWave implements Runnable {

    private static final Logger LOG = Logger.getLogger(AePlayWave.class.getName());
    private AudioInputStream audioStream;

    public AePlayWave(File audioFile) throws UnsupportedAudioFileException, IOException {
        this(AudioSystem.getAudioInputStream(audioFile));
    }
    
    public AePlayWave(AudioInputStream audioStream) {
        this.audioStream = audioStream;
    }
    
    public AePlayWave(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
    	audioStream = AudioSystem.getAudioInputStream(inputStream);
    }

    @Override
    public void run() {
        Line line = null;
        try {
        	final AudioFormat format = audioStream.getFormat();
        	final DataLine.Info info = new DataLine.Info(Clip.class, format);
            line = AudioSystem.getLine(info);
            final Clip audioClip = (Clip) line;
            audioClip.open(audioStream);
            audioClip.start();
            Thread.sleep(audioClip.getMicrosecondLength() / 100);
        } catch (LineUnavailableException e) {
            LOG.log(Level.WARNING, "LineUnavailableException", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "InterruptedException", e);
        } catch (IOException e) {
        	LOG.log(Level.WARNING, "IOException", e);
		} finally {
            try {
                if (audioStream != null) {
                    audioStream.close();
                }
                if (line != null) {
                    line.close();
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "IOException", e);
            }
        }
    }
}
