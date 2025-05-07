package org.jxmapviewer.fma.jtiledownloader;

public class TileProviderList {

    private final TileProviderIf[] tileProviders;

    /**
     * Sets up the tileProviderList
     */
    public TileProviderList() {
        tileProviders = new TileProviderIf[]{
            new MapnikTileProvider(),
            new VirtualEarthTileProvider(VirtualEarthTileProvider.MAP),
            new VirtualEarthTileProvider(VirtualEarthTileProvider.SATELLITE),
            new VirtualEarthTileProvider(VirtualEarthTileProvider.HYBRID),
            new OsmarenderTileProvider(),
            new MapQuestTileProvider(),
            new GenericTileProvider("Cyclemap (CloudMade)", "http://c.andy.sandbox.cloudmade.com/tiles/cycle/"),
            new GenericTileProvider("Cyclemap (Thunderflames)", "http://thunderflames.org/tiles/cycle/"),
            new GenericTileProvider("OpenStreetBrowser (Europe)", "http://www.openstreetbrowser.org/tiles/base/"),
            new GenericTileProvider("OpenPisteMap", "http://openpistemap.org/tiles/contours/"),
            new GenericTileProvider("Maplint", "http://tah.openstreetmap.org/Tiles/maplint/"),
            new MapSurferTileProvider(),
            new MapSurferProfileTileProvider(),
            new GenericTileProvider("CloudMade Web style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/1/256/"),
            new GenericTileProvider("CloudMade Mobile style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/2/256/"),
            new GenericTileProvider("CloudMade NoNames style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/3/256/"),
            new GenericTileProvider("CloudMade Night style", "http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/999/256/")};
    }

    public TileProviderIf[] getTileProviderList() {
        return tileProviders.clone();
    }
}
