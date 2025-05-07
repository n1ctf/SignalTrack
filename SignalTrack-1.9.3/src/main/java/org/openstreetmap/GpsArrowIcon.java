package org.openstreetmap;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.jxmapviewer.Style;

import geometry.ICoordinate;
import map.GpsArrow;

public class GpsArrowIcon extends MapIconImpl implements GpsArrow {
    public GpsArrowIcon(BufferedImage image, String name, ICoordinate lonLat, Dimension size, Style style) {
        super(image, null, name, lonLat, size, style);
    }

    @Override
    public double getRadius() {
        return getSize().getWidth() / 2;
    }
}
