package utility;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

public class PrintUtilities implements Printable, Runnable {
    private final Component comp;

    public PrintUtilities(Component comp) {
        this.comp = comp;
    }    

    public PrintUtilities(BufferedImage image) {
    	comp = new ImagePrint(image);
    }
    
    public PrintUtilities(JComponent jcomp) {
    	comp = jcomp;
    }
    
    @Override
	public void run() {
        final PrinterJob printJob = PrinterJob.getPrinterJob();
        final PageFormat pageFormat = printJob.defaultPage();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        printJob.setPrintable(this, pageFormat);
        if (printJob.printDialog()) {
        	try {
				printJob.print();
			} catch (final PrinterException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return (NO_SUCH_PAGE);
        } else {
            final Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            final int width = (int) Math.round(pageFormat.getImageableWidth());
            final int height = (int) Math.round(pageFormat.getImageableHeight());
            final Dimension toFit = new Dimension(width, height);
            final Point2D.Double scaleFactor = getScaleFactorToFit(comp.getSize(), toFit);
            g2d.scale(scaleFactor.x, scaleFactor.y);
            disableDoubleBuffering(comp);
            comp.paint(g2d);
            enableDoubleBuffering(comp);
            g2d.dispose();
            return (PAGE_EXISTS);
        }
    }

    private void disableDoubleBuffering(Component c) {
        final RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
    }

    private void enableDoubleBuffering(Component c) {
        final RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }
    
    private static class ImagePrint extends Component {
        private static final long serialVersionUID = 1L;
        private final transient BufferedImage image;
        private final int w;
        private final int h;

        public ImagePrint(BufferedImage image) {
            this.image = image;
            w = image.getWidth();
            h =  image.getHeight();
            configureComponent();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, w, h, null);
        }
        
        private void configureComponent() {
        	setSize(w, h);
        }
    }

    private double getScaleFactor(int iMasterSize, int iTargetSize) {
        return (double) iTargetSize / (double) iMasterSize;
    }

    private Point2D.Double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScaleWidth = 1D;
        double dScaleHeight = 1D;
        if ((original != null) && (toFit != null)) {
            dScaleWidth = getScaleFactor(original.width, toFit.width);
            dScaleHeight = getScaleFactor(original.height, toFit.height);
        }
        return new Point.Double(dScaleWidth, dScaleHeight);
    }
}
