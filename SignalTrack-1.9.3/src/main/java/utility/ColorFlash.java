package utility;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jxmapviewer.Style;

public class ColorFlash {

    private final ActionListener flashTimerAction = _ -> flashTimerActionEvent();
    private final Timer flashTimer = new Timer(250, flashTimerAction);

    private Color baseColor;
    private Color flashColor;
    private final Style style;

    public ColorFlash(Color base, Color flash, Style style) {
        this.baseColor = base;
        this.flashColor = flash;
        this.style = style;
        flashTimer.setRepeats(true);
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
    }

    public void setFlashColor(Color flashColor) {
        this.flashColor = flashColor;
    }

    private void flashTimerActionEvent() {
        if (style.getBackColor().equals(baseColor)) {
            style.setBackColor(flashColor);
        } else {
            style.setBackColor(baseColor);
        }
    }

    public void start() {
        if (!flashTimer.isRunning()) {
            flashTimer.start();
        }
    }

    public void stop() {
        flashTimer.stop();
    }
}
