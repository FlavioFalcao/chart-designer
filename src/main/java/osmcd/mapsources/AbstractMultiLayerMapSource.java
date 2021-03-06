/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.mapsources;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import osmcd.exceptions.TileException;
import osmcd.gui.mapview.PreviewMap;
import osmcd.program.interfaces.InitializableMapSource;
import osmcd.program.interfaces.MapSource;
import osmcd.program.interfaces.MapSpace;
import osmcd.program.model.MapSourceLoaderInfo;
import osmcd.program.model.TileImageType;

public abstract class AbstractMultiLayerMapSource implements InitializableMapSource, Iterable<MapSource> {

	protected Logger log;

	protected String name = "";
	protected TileImageType tileType = TileImageType.PNG;
	protected MapSource[] mapSources;

	private int maxZoom;
	private int minZoom;
	private MapSpace mapSpace;
	protected MapSourceLoaderInfo loaderInfo = null;

	public AbstractMultiLayerMapSource(String name, TileImageType tileImageType) {
		this();
		this.name = name;
		this.tileType = tileImageType;
	}

	protected AbstractMultiLayerMapSource() {
		log = Logger.getLogger(this.getClass());
	}

	protected void initializeValues() {
		MapSource refMapSource = mapSources[0];
		mapSpace = refMapSource.getMapSpace();
		maxZoom = PreviewMap.MAX_ZOOM;
		minZoom = 0;
		for (MapSource ms : mapSources) {
			maxZoom = Math.min(maxZoom, ms.getMaxZoom());
			minZoom = Math.max(minZoom, ms.getMinZoom());
			if (!ms.getMapSpace().equals(mapSpace))
				throw new RuntimeException("Different map spaces used in multi-layer map source");
		}
	}

	@Override
	public void initialize() {
		MapSource refMapSource = mapSources[0];
		mapSpace = refMapSource.getMapSpace();
		maxZoom = PreviewMap.MAX_ZOOM;
		minZoom = 0;
		for (MapSource ms : mapSources) {
			if (ms instanceof InitializableMapSource)
				((InitializableMapSource) ms).initialize();
			maxZoom = Math.min(maxZoom, ms.getMaxZoom());
			minZoom = Math.max(minZoom, ms.getMinZoom());
		}
	}

	public MapSource[] getLayerMapSources() {
		return mapSources;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	public String getStoreName() {
		return null;
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, InterruptedException,
			TileException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		BufferedImage image = getTileImage(zoom, x, y, loadMethod);
		if (image == null)
			return null;
		// TODO: here can write with compress
		ImageIO.write(image, tileType.getFileExt(), buf);
		return buf.toByteArray();
	}

	// public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
	// InterruptedException, TileException {
	// int tileSize = mapSpace.getTileSize();
	// BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_3BYTE_BGR);
	// Graphics2D g2 = image.createGraphics();
	// try {
	// g2.setColor(getBackgroundColor());
	// g2.fillRect(0, 0, tileSize, tileSize);
	// boolean used = false;
	// for (MapSource layerMapSource : mapSources) {
	// BufferedImage layerImage = layerMapSource.getTileImage(zoom, x, y, loadMethod);
	// if (layerImage != null) {
	// log.debug("Multi layer loading: " + layerMapSource + " " + x + " " + y + " " + zoom);
	// g2.drawImage(layerImage, 0, 0, null);
	// used = true;
	// }
	// }
	// if (used)
	// return image;
	// else
	// return null;
	// } finally {
	// g2.dispose();
	// }
	// }

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			InterruptedException, TileException {
		// int tileSize = mapSpace.getTileSize();
		BufferedImage image = null;
		Graphics2D g2 = null;
		try {
			ArrayList<BufferedImage> layerImages = new ArrayList<BufferedImage>(mapSources.length);
			int maxSize = mapSpace.getTileSize();
			for (int i = 0; i < mapSources.length; i++) {
				MapSource layerMapSource = mapSources[i];
				BufferedImage layerImage = layerMapSource.getTileImage(zoom, x, y, loadMethod);
				if (layerImage != null) {
					log.debug("Multi layer loading: " + layerMapSource + " " + x + " " + y + " " + zoom);
					layerImages.add(layerImage);
					int size = layerImage.getWidth();
					if (size > maxSize) {
						maxSize = size;
					}
				}
			}

			// optimize for when only one layer exist
			if (layerImages.size() == 1) {
				return layerImages.get(0);
			} else if (layerImages.size() > 1) {
				image = new BufferedImage(maxSize, maxSize, BufferedImage.TYPE_3BYTE_BGR);
				g2 = image.createGraphics();
				g2.setColor(getBackgroundColor());
				g2.fillRect(0, 0, maxSize, maxSize);

				for (int i = 0; i < layerImages.size(); i++) {
					BufferedImage layerImage = layerImages.get(i);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getLayerAlpha(i)));
					g2.drawImage(layerImage, 0, 0, maxSize, maxSize, null);
				}
				return image;
			} else {
				return null;
			}

		} finally {
			if (g2 != null) {
				g2.dispose();
			}

		}
	}

	protected float getLayerAlpha(int layerIndex) {
		return 1.0f;
	}

	public TileImageType getTileImageType() {
		return tileType;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Iterator<MapSource> iterator() {
		return Arrays.asList(mapSources).iterator();
	}

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		if (this.loaderInfo != null)
			throw new RuntimeException("LoaderInfo already set");
		this.loaderInfo = loaderInfo;
	}

}
