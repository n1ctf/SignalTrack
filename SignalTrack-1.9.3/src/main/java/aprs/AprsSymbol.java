package aprs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import components.TwoDimElement;

public class AprsSymbol {
	private static final String PAGE_0 = "/aprs-symbols-64-0@2x.png";
	private static final String PAGE_1 = "/aprs-symbols-64-1@2x.png";
	
	private static final Logger LOG = Logger.getLogger(AprsSymbol.class.getName());
	
	private final BufferedImage image;
	
	public AprsSymbol(String ssid) {
		final TwoDimElement<Integer, Integer> ssidLookup = getSsidIcon(ssid);
		final int column = ssidLookup.getT1();
		final int row = ssidLookup.getT2();
		final String page = PAGE_0;
		
		image = getIcon(column, row, page);
	}
	
	public AprsSymbol(Character table, Character symbol) {
		int row = 0;
		int column = 0;
		String page = PAGE_0;

		if (table != null && symbol != null) {
			final int ascii = symbol;
			final int sequence = ascii - 33;
			row = sequence / 16;
			column = sequence % 16;
			page = (table.equals((char) 47)) ? PAGE_0 : PAGE_1;
		}

		image = getIcon(column, row, page);
	}
	
	private BufferedImage getIcon(int column, int row, String fileName) {

		BufferedImage icon = null;

		try (InputStream stream = getClass().getResourceAsStream(fileName)) {

			final BufferedImage t = ImageIO.read(stream);

			final int columnWidth = t.getWidth() / 16;
			final int rowHeight = t.getHeight() / 6;
			icon = t.getSubimage(columnWidth * column, rowHeight * row, columnWidth, rowHeight);

		} catch (IOException | IllegalArgumentException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		return icon;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	private TwoDimElement<Integer, Integer> getSsidIcon(String ssid) {
		final int row;
		final int column = switch (ssid) {
			case "00" -> {
				row = 0;
				yield 12;
			}
			case "01" -> {
				row = 4;
				yield 0;
			}
			case "02" -> {
				row = 3;
				yield 4;
			}
			case "03" -> {
				row = 4;
				yield 5;
			}
			case "04" -> {
				row = 2;
				yield 1;
			}
			case "05" -> {
				row = 3;
				yield 8;
			}
			case "06" -> {
				row = 3;
				yield 7;
			}
			case "07" -> {
				row = 0;
				yield 6;
			}
			case "08" -> {
				row = 5;
				yield 2;
			}
			case "09" -> {
				row = 1;
				yield 15;
			}
			case "10" -> {
				row = 1;
				yield 11;
			}
			case "11" -> {
				row = 2;
				yield 14;
			}
			case "12" -> {
				row = 4;
				yield 9;
			}
			case "13" -> {
				row = 3;
				yield 1;
			}
			case "14" -> {
				row = 4;
				yield 10;
			}
			case "15" -> {
				row = 5;
				yield 5;
			}
			default -> {
				row = 0;
				yield 12;
			}
		};
		return new TwoDimElement<>(column, row);
	}

	@Override
	public String toString() {
		return "AprsSymbol [image=" + image + "]";
	}
}
