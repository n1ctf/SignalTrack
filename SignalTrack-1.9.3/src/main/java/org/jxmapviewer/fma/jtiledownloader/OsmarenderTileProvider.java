package org.jxmapviewer.fma.jtiledownloader;

public class OsmarenderTileProvider extends RotatingTileProvider {
	private static final String[] SUBDOMAINS = { "a", "b", "c" };

	public OsmarenderTileProvider() {
		url = "http://{0}.tah.openstreetmap.org/Tiles/tile/";
	}

	@Override
	protected String[] getSubDomains() {
		return SUBDOMAINS;
	}

	@Override
	public String getName() {
		return "Osmarender";
	}

	@Override
	public int getMaxZoom() {
		return 17;
	}
}
