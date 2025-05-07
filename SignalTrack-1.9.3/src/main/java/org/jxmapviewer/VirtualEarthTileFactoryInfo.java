package org.jxmapviewer;

import org.jxmapviewer.viewer.TileFactoryInfo;

import map.AbstractMap.SignalTrackMapNames;

/**
 * *****************************************************************************
 * http://www.viavirtualearth.com/vve/Articles/RollYourOwnTileServer.ashx
 *
 * @author Fabrizio Giudici
 * @version $Id: MicrosoftVirtualEarthProvider.java 115 2007-11-08 22:04:36Z
 * fabriziogiudici $
 *******************************************************************************
 */
public class VirtualEarthTileFactoryInfo extends TileFactoryInfo {

    /**
     * Use road map
     */
    public static final MVEMode MAP = new MVEMode("map", "map", "r", ".png");

    /**
     * Use satellite map
     */
    public static final MVEMode SATELLITE = new MVEMode("satellite", "satellite", "a", ".jpeg");

    /**
     * Use hybrid map
     */
    public static final MVEMode HYBRID = new MVEMode("hybrid", "hybrid", "h", ".jpeg");


    private static final int TOP_ZOOM_LEVEL = 19;

    private static final int MAX_ZOOM_LEVEL = 17;

    private static final int MIN_ZOOM_LEVEL = 2;

    private static final int TILE_SIZE = 256;

    private final MVEMode mode;

    /**
     * @param mode the mode
     */
    public VirtualEarthTileFactoryInfo(MVEMode mode) {
        super(getFullName(mode), MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, TOP_ZOOM_LEVEL, TILE_SIZE, false, false, "", "", "", "");
        this.mode = mode;
    }

    private static String getFullName(MVEMode mode) {
        String name = null;
        switch (mode.getName()) {
            case "map" -> name = SignalTrackMapNames.VirtualEarthMap.name();
            case "satellite" -> name = SignalTrackMapNames.VirtualEarthSatellite.name();
            case "hybrid" -> name = SignalTrackMapNames.VirtualEarthHybrid.name();
            default -> {
            }
        }
        return name;
    }

    /**
     * @return the name of the selected mode
     */
    public String getModeName() {
        return mode.getName();
    }

    /**
     * @return the label of the selected mode
     */
    public String getModeLabel() {
        return mode.getLabel();
    }

    @Override
    public String getTileUrl(final int x, final int y, final int zoom) {
        final String quad = tileToQuadKey(x, y, TOP_ZOOM_LEVEL - 0 - zoom);
        return "http://" + mode.getType() + quad.charAt(quad.length() - 1) + ".ortho.tiles.virtualearth.net/tiles/"
                + mode.getType() + quad + mode.getExt() + "?g=1";
    }

    private String tileToQuadKey(final int tx, final int ty, final int zl) {
    	final StringBuilder quad = new StringBuilder();

        for (int i = zl; i > 0; i--) {
        	final int mask = 1 << (i - 1);
            int cell = 0;

            if ((tx & mask) != 0) {
                cell++;
            }

            if ((ty & mask) != 0) {
                cell += 2;
            }

            quad.append(cell);
        }

        return quad.toString();
    }

    @Override
    public String getAttribution() {
        return "\u00A9 Microsoft";
    }

    @Override
    public String getLicense() {
        return "https://www.microsoft.com/en-us/maps/licensing";
    }

}
