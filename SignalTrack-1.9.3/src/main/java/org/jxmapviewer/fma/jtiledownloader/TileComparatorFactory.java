package org.jxmapviewer.fma.jtiledownloader;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * This class compares two tiles in three possible ways:
 * <ol>
 * <li>Simply by order, z -&gt; x -&gt; y.
 * <li>By quad-tiles, z -&gt; (quad x) -&gt; (quad y) -&gt; x -&gt; y.
 * <li>By quad-tiles recursively (not supported yet &mdash; I have no idea how).
 * </ol>
 * 
 * @author zverik
 */
public class TileComparatorFactory {
	public static final int COMPARE_DONT = 0;
	public static final int COMPARE_SIMPLE = 1;
	public static final int COMPARE_QUAD = 2;
	public static final int COMPARE_RECURSIVE = 3;
	public static final int COMPARE_COUNT = 3;
	
	private TileComparatorFactory() {
		throw new IllegalStateException("Factory class");
	}
	
	@SuppressWarnings("unchecked")
	private static final Comparator<Tile>[] comparators = new Comparator[] { 
			new SimpleComparator(), 
			new SimpleComparator(),
			new QuadComparator(), 
			new RecursiveComparator() 
	};

	public static Comparator<Tile> getComparator(int type) {
		return comparators[type];
	}

	public static void sortTileList(List<Tile> tileList) {
		final int tileSortingPolicy = AppConfiguration.getInstance().getTileSortingPolicy();
		if (tileSortingPolicy > 0) {
			tileList.sort(TileComparatorFactory.getComparator(tileSortingPolicy));
		}
	}

	private static class SimpleComparator implements Comparator<Tile>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Tile t1, Tile t2) {
			int r = t1.getZ().compareTo(t2.getZ());
			if (r == 0) {
				r = t1.getX().compareTo(t2.getX());
				if (r == 0) {
					r = t1.getY().compareTo(t2.getY());
				}
			}
			return r;
		}
	}

	private static class QuadComparator implements Comparator<Tile>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Tile t1, Tile t2) {
			int r = t1.getZ().compareTo(t2.getZ());
			if (r == 0) {
				final Integer tz1 = t1.getZ() >> 3;
				final Integer tz2 = t2.getZ() >> 3;
				r = tz1.compareTo(tz2);
				if (r == 0) {
					final Integer ty1 = t1.getY() >> 3;
					final Integer ty2 = t2.getY() >> 3;
					r = ty1.compareTo(ty2);
					if (r == 0) {
						final Integer tx1 = t1.getX() >> 3;
						final Integer tx2 = t2.getX() >> 3;
						r = tx1.compareTo(tx2);
					}
				}
			}
			return r;
		}
	}

	private static class RecursiveComparator implements Comparator<Tile>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Tile t1, Tile t2) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
}
