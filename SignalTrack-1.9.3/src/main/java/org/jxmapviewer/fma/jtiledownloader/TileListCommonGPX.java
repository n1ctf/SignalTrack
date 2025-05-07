/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of jTileDownloader.
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.jxmapviewer.fma.jtiledownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class TileListCommonGPX extends TileListCommon {
	private static final Logger log = Logger.getLogger(TileListCommonGPX.class.getName());
	private final List<Tile> tilesToDownload = new ArrayList<>();

	public void updateList(String fileName, int corridorSize) {
		tilesToDownload.clear();
		final File file = new File(fileName);
		if (file.exists() && file.isFile()) {
			try {
				final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
				domFactory.setNamespaceAware(true);
				final DocumentBuilder builder = domFactory.newDocumentBuilder();
				final Document document = builder.parse(file);

				final Node gpxNode = document.getFirstChild();
				if (!"gpx".equalsIgnoreCase(gpxNode.getNodeName())) {
					log.log(Level.FINE, "invalid file!");
				}

				final NodeList nodes = gpxNode.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					if (nodes.item(i).getLocalName() != null && "trk".equalsIgnoreCase(nodes.item(i).getLocalName())) {
						// Download all zoomlevels
						for (int zoomLevel : getDownloadZoomLevels()) {
							// handle all trgSegments
							final NodeList trkSegs = nodes.item(i).getChildNodes();
							for (int indexTrkSeg = 0; indexTrkSeg < trkSegs.getLength(); indexTrkSeg++) {
								if (trkSegs.item(indexTrkSeg).getLocalName() != null
										&& "trkseg".equalsIgnoreCase(trkSegs.item(indexTrkSeg).getLocalName())) {
									// handle all trkpts
									final NodeList trkPts = trkSegs.item(indexTrkSeg).getChildNodes();
									for (int indexTrkPt = 0; indexTrkPt < trkPts.getLength(); indexTrkPt++) {
										if (trkPts.item(indexTrkPt).getLocalName() != null
												&& "trkpt".equalsIgnoreCase(trkPts.item(indexTrkPt).getLocalName())) {
											handleTrkPt(trkPts.item(indexTrkPt), zoomLevel, corridorSize);
										}
									}
								}
							}
						}
					}
				}
			} catch (SAXParseException spe) {
				final Exception e = (spe.getException() != null) ? spe.getException() : spe;
				log.log(Level.SEVERE, "Error parsing {0}, line {1}, message: {2} ", new Object[] {spe.getSystemId(), spe.getLineNumber(), e.getLocalizedMessage()});
			} catch (SAXException sxe) {
				final Exception e = (sxe.getException() != null) ? sxe.getException() : sxe;
				log.log(Level.SEVERE, "Error parsing GPX", e);
			} catch (ParserConfigurationException pce) {
				log.log(Level.SEVERE, "Error in parser configuration", pce);
			} catch (IOException ioe) {
				log.log(Level.SEVERE, "Error parsing GPX", ioe);
			}
		}
	}

	private void handleTrkPt(Node item, int zoomLevel, int corridorSize) {
		final NamedNodeMap attrs = item.getAttributes();
		if (attrs.getNamedItem("lat") != null && attrs.getNamedItem("lon") != null) {
			try {
				final Double lat = Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
				final Double lon = Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
				int minDownloadTileXIndex = 0;
				int maxDownloadTileXIndex = 0;
				int minDownloadTileYIndex = 0;
				int maxDownloadTileYIndex = 0;
				if (corridorSize > 0) {
					final double minLat = lat - 360 * (corridorSize * 1000 / Constants.EARTH_CIRC_POLE);
					final double minLon = lon - 360
							* (corridorSize * 1000 / (Constants.EARTH_CIRC_EQUATOR * Math.cos(lon * Math.PI / 180)));
					final double maxLat = lat + 360 * (corridorSize * 1000 / Constants.EARTH_CIRC_POLE);
					final double maxLon = lon + 360
							* (corridorSize * 1000 / (Constants.EARTH_CIRC_EQUATOR * Math.cos(lon * Math.PI / 180)));
					minDownloadTileXIndex = calculateTileX(minLon, zoomLevel);
					maxDownloadTileXIndex = calculateTileX(maxLon, zoomLevel);
					minDownloadTileYIndex = calculateTileY(minLat, zoomLevel);
					maxDownloadTileYIndex = calculateTileY(maxLat, zoomLevel);
				} else {
					minDownloadTileXIndex = calculateTileX(lon, zoomLevel);
					maxDownloadTileXIndex = minDownloadTileXIndex;
					minDownloadTileYIndex = calculateTileY(lat, zoomLevel);
					maxDownloadTileYIndex = minDownloadTileYIndex;
				}

				for (int tileXIndex = Math.min(minDownloadTileXIndex, maxDownloadTileXIndex); tileXIndex <= Math
						.max(minDownloadTileXIndex, maxDownloadTileXIndex); tileXIndex++) {
					for (int tileYIndex = Math.min(minDownloadTileYIndex, maxDownloadTileYIndex); tileYIndex <= Math
							.max(minDownloadTileYIndex, maxDownloadTileYIndex); tileYIndex++) {
						final Tile tile = new Tile(tileXIndex, tileYIndex, zoomLevel);
						if (!tilesToDownload.contains(tile)) {
							log.log(Level.FINE, "add {0} to download list...", tile);
							tilesToDownload.add(tile);
						}
					}

				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

	/**
	 * @see org.jxmapviewer.fma.jtiledownloader.TileList#getTileListToDownload()
	 */
	@Override
	public List<Tile> getTileListToDownload() {
		return tilesToDownload;
	}
}
