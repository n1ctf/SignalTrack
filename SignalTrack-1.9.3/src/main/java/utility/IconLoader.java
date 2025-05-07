package utility;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IconLoader extends SwingWorker<BufferedImage, Void> {
	private static final Log LOG = LogFactory.getLog(IconLoader.class.getName());
    private Object object;
    private File file;
    private InputStream inputStream;
    

    public IconLoader(Object object, InputStream inputStream) {
        this.object = object;
        setObjectImage(this.object, getDefaultIcon(new Dimension(16, 16)));
        this.inputStream = inputStream;
        execute();
    }

    public IconLoader(Object object, URL url) {
        try {
            this.file = new File(url.toURI());
            this.object = object;
            setObjectImage(this.object, getDefaultIcon(new Dimension(16, 16)));
            inputStream = url.openStream();
        } catch (final URISyntaxException | NullPointerException | IOException ex) {
            LOG.warn(ex);
        }
        execute();
    }

    public IconLoader(Object object, String str) {
        try {
            this.file = new File(str);
            this.object = object;
            setObjectImage(this.object, getDefaultIcon(new Dimension(16, 16)));
            inputStream = getClass().getResourceAsStream(str);
        } catch (final NullPointerException ex) {
            LOG.warn(ex);
        }
        execute();
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
        try {
            if (inputStream != null) {
                return ImageIO.read(inputStream);
            } else if (file != null) {
                return ImageIO.read(file);
            } else {
                return getDefaultIcon(new Dimension(16, 16));
            }
        } catch (final IOException ex) {
            if (inputStream != null) {
                LOG.info("file not found: " + inputStream.getClass().getCanonicalName());
            } else {
                LOG.info("file not found: " + file.getCanonicalPath());
            }
            return getDefaultIcon(new Dimension(16, 16));
        }
    }

    @Override
    protected void done() {
        try {
            inputStream.close();
            setObjectImage(object, get());
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOG.warn(ex);
            Thread.currentThread().interrupt();
        }
    }

	private void setObjectImage(Object object, BufferedImage image) {
        if (image == null) {
            return;
        }
        if (object instanceof JButton jButton) {
            jButton.setIcon(new ImageIcon(image));
        }
        if (object instanceof JMenuItem jMenuItem) {
            jMenuItem.setIcon(new ImageIcon(image));
        }
        if (object instanceof JFrame jFrame) {
            jFrame.setIconImage(image);
        }
        if (object instanceof JToggleButton jToggleButton) {
            jToggleButton.setIcon(new ImageIcon(image));
        }
    }

    public static BufferedImage getDefaultIcon(Dimension size) {
        Graphics2D g = null;
        try {
            final BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            g = bi.createGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(new BasicStroke(2.0F));
            g.drawRect(0, 0, size.width, size.height);
            g.setColor(new Color(64, 0, 0));
            g.setStroke(new BasicStroke(1.0F));
            g.drawLine(0, 0, size.width, size.height);
            g.drawLine(size.width, 0, 0, size.height);
            return bi;
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

}
