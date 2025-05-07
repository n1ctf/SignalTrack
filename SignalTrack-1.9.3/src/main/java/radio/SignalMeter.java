package radio;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;

public class SignalMeter extends JPanel {

    private static final long serialVersionUID = -5011237089698705502L;

    private final Rectangle[] meter = new Rectangle[10];
    private final Rectangle[] frame = new Rectangle[10];
    private final Integer[] meterLevel = new Integer[10];
    private List<ScanElement> scanList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private Color[] meterColor = new Color[10];
    private boolean enable = true;

    public SignalMeter() {
        super();
        for (int i = 0; i < meter.length; i++) {
            meter[i] = new Rectangle();
            frame[i] = new Rectangle();
            meterLevel[i] = 0;
            meterColor[i] = Color.BLUE;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        zeroize();
        this.enable = enabled;
    }

    public void zeroize() {
        for (int i = 0; i < 10; i++) {
            meterLevel[i] = 0;
        }
        repaint();
    }

    public void setActiveMeters(List<ScanElement> scanList) {
        this.scanList = scanList;
        repaint();
    }

    public void setMeterLevels(Integer[] percent) {
        if (!enable) {
            return;
        }
        for (int i = 0; i < 10; i++) {
            meterLevel[i] = Math.max(Math.min(percent[i], 100), 0);
        }
        repaint();
    }

    public void setMeterLevels(List<Integer> percent) {
        if (!enable) {
            return;
        }
        for (int i = 0; i < 10; i++) {
            meterLevel[i] = Math.max(Math.min(percent.get(i), 100), 0);
        }
        repaint();
    }

    public void setMeterLevels(Integer percent) {
        if (!enable) {
            return;
        }
        Arrays.fill(meterLevel, Math.max(Math.min(percent, 100), 0));
        repaint();
    }

    public void setMeterLevel(int index, int percent) {
        if (!enable) {
            return;
        }
        meterLevel[index] = Math.max(Math.min(percent, 100), 0);
        repaint();
    }

    public void setMeterColors(Color[] color) {
        meterColor = Arrays.copyOf(color, 10);
        repaint();
    }

    public void setMeterColor(Color color) {
        for (int i = 0; i < meter.length; i++) {
            meterColor[i] = color;
        }
        repaint();
    }

    public void setMeterColor(int index, Color color) {
        meterColor[Math.max(Math.min(index, 9), 0)] = color;
        repaint();
    }

    private void paintChannel(int ch, Graphics2D g2) {
        meter[ch].setSize(getWidth() / 15, (meterLevel[ch] * (getHeight() - 30)) / 100);
        meter[ch].setLocation(((getWidth() / 11) * (ch + 1)) - 4, (18 + (getHeight() - 30)) - ((meterLevel[ch] * (getHeight() - 30)) / 100));
        g2.setColor(meterColor[ch]);
        g2.fill(meter[ch]);
        g2.draw(meter[ch]);
        frame[ch].setSize(getWidth() / 15, getHeight() - 30);
        frame[ch].setLocation(((getWidth() / 11) * (ch + 1)) - 4, getHeight() - ((100 * (getHeight() - 18)) / 100));
        g2.setColor(Color.lightGray);
        g2.draw(frame[ch]);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (enable) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (scanList != null && !scanList.isEmpty() && scanList.size() > 1) {
                    for (int i = 0; i < scanList.size(); i++) {
                        final ScanElement e = scanList.get(i);
                        if (!e.isScanSelected()) {
                            continue;
                        }
                        int ch = i;
                        paintChannel(ch, g2);
                    }
                } else {
                    paintChannel(0, g2);
                }
            }
        } finally {
            g2.dispose();
        }
    }

}
