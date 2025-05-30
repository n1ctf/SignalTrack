package org.openstreetmap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.HashMap;

import geometry.ICoordinate;

public class AttributionSupport {

    private Attributed source;

    private Image attrImage;
    private String attrTermsText;
    private String attrTermsUrl;
    public static final Font ATTR_FONT = new Font("Arial", Font.PLAIN, 10);
    public static final Font ATTR_LINK_FONT;

    protected Rectangle attrTextBounds = null;
    protected Rectangle attrToUBounds = null;
    protected Rectangle attrImageBounds = null;

    static {
        final HashMap<TextAttribute, Integer> aUnderline = new HashMap<>();
        aUnderline.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        ATTR_LINK_FONT = ATTR_FONT.deriveFont(aUnderline);
    }

    public void initialize(Attributed source) {
        this.source = source;
        final boolean requireAttr = source.requiresAttribution();
        if (requireAttr) {
            attrImage = source.getAttributionImage();
            attrTermsText = source.getTermsOfUseText();
            attrTermsUrl = source.getTermsOfUseURL();
            if ((attrTermsUrl != null) && (attrTermsText == null)) {
            //    attrTermsText = tr("Background Terms of Use");
            }
        } else {
            attrImage = null;
            attrTermsUrl = null;
        }
    }

    public void paintAttribution(Graphics g, int width, int height, ICoordinate topLeft, ICoordinate bottomRight, 
            int zoom, ImageObserver observer) {
        if ((source == null) || !source.requiresAttribution()) {
            attrToUBounds = null;
            attrImageBounds = null;
            attrTextBounds = null;
            return;
        }

        // Draw attribution
        final Font font = g.getFont();
        g.setFont(ATTR_LINK_FONT);

        // Draw terms of use text
        int termsTextHeight = 0;
        int termsTextY = height;

        if (attrTermsText != null) {
            final Rectangle2D termsStringBounds = g.getFontMetrics().getStringBounds(attrTermsText, g);
            final int textRealHeight = (int) termsStringBounds.getHeight();
            termsTextHeight = textRealHeight - 5;
            final int termsTextWidth = (int) termsStringBounds.getWidth();
            termsTextY = height - termsTextHeight;
            final int x = 2;
            final int y = height - termsTextHeight;
            attrToUBounds = new Rectangle(x, y-termsTextHeight, termsTextWidth, textRealHeight);
            g.setColor(Color.black);
            g.drawString(attrTermsText, x + 1, y + 1);
            g.setColor(Color.white);
            g.drawString(attrTermsText, x, y);
        } else {
            attrToUBounds = null;
        }

        // Draw attribution logo
        if (attrImage != null) {
            final int x = 2;
            final int imgWidth = attrImage.getWidth(observer);
            final int imgHeight = attrImage.getHeight(observer);
            final int y = termsTextY - imgHeight - termsTextHeight - 5;
            attrImageBounds = new Rectangle(x, y, imgWidth, imgHeight);
            g.drawImage(attrImage, x, y, null);
        } else {
            attrImageBounds = null;
        }

        g.setFont(ATTR_FONT);
        final String attributionText = source.getAttributionText(zoom, topLeft, bottomRight);
        if (attributionText != null) {
            final Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(attributionText, g);
            final int textHeight = (int) stringBounds.getHeight() - 5;
            final int x = width - (int) stringBounds.getWidth();
            final int y = height - textHeight;
            g.setColor(Color.black);
            g.drawString(attributionText, x + 1, y + 1);
            g.setColor(Color.white);
            g.drawString(attributionText, x, y);
            attrTextBounds = new Rectangle(x, y-textHeight, (int) stringBounds.getWidth(), (int) stringBounds.getHeight());
        } else {
            attrTextBounds = null;
        }

        g.setFont(font);
    }

    public boolean handleAttributionCursor(Point p) {
        if ((attrTextBounds != null) && attrTextBounds.contains(p)) {
            return true;
        } else if ((attrImageBounds != null) && attrImageBounds.contains(p)) {
            return true;
        } else if ((attrToUBounds != null) && attrToUBounds.contains(p)) {
            return true;
        }
        return false;
    }

    public boolean handleAttribution(Point p, boolean click) {
        if ((source == null) || !source.requiresAttribution()) {
			return false;
		}

        if ((attrTextBounds != null) && attrTextBounds.contains(p)) {
            final String attributionURL = source.getAttributionLinkURL();
            if (attributionURL != null) {
                if (click) {
                    FeatureAdapter.openLink(attributionURL);
                }
                return true;
            }
        } else if ((attrImageBounds != null) && attrImageBounds.contains(p)) {
            final String attributionImageURL = source.getAttributionImageURL();
            if (attributionImageURL != null) {
                if (click) {
                    FeatureAdapter.openLink(source.getAttributionImageURL());
                }
                return true;
            }
        } else if ((attrToUBounds != null) && attrToUBounds.contains(p)) {
            final String termsOfUseURL = source.getTermsOfUseURL();
            if (termsOfUseURL != null) {
                if (click) {
                    FeatureAdapter.openLink(termsOfUseURL);
                }
                return true;
            }
        }
        return false;
    }

}

