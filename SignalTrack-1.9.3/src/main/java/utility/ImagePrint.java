package utility;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class ImagePrint implements Printable {
	private final Component comp;

	private static final Logger LOG = Logger.getLogger(ImagePrint.class.getName());
	
    public ImagePrint(Component comp) {
    	this.comp = comp;
    	
    	final PrintService service = PrintServiceLookup.lookupDefaultPrintService();
	    final DocPrintJob job = service.createPrintJob();
	    final DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	    final SimpleDoc doc = new SimpleDoc(this, flavor, null);
	    try {
			job.print(doc, null);
		} catch (final PrintException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
	}

	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		final Graphics2D g2d = (Graphics2D) g;
		g.translate((int) (pf.getImageableX()), (int) (pf.getImageableY()));
		if (pageIndex == 0) {
			final double pageWidth = pf.getImageableWidth();
			final double pageHeight = pf.getImageableHeight();
			final double imageWidth = comp.getWidth();
			final double imageHeight = comp.getHeight();
			final double scaleX = pageWidth / imageWidth;
			final double scaleY = pageHeight / imageHeight;
			final double scaleFactor = Math.min(scaleX, scaleY);
			g2d.scale(scaleFactor, scaleFactor);
			comp.paint(g2d);  
			return Printable.PAGE_EXISTS;
		}
		return Printable.NO_SUCH_PAGE;
	}
}
