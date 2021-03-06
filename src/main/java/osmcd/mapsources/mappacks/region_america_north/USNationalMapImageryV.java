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
/**
 * 
 */
package osmcd.mapsources.mappacks.region_america_north;

import java.awt.Color;

import osmcd.mapsources.AbstractMultiLayerMapSource;
import osmcd.program.interfaces.MapSource;
import osmcd.program.model.TileImageType;

/**
 * http://viewer.nationalmap.gov/example/services.html
 */
public class USNationalMapImageryV extends AbstractMultiLayerMapSource {

	public USNationalMapImageryV() {
		super("USGS National Map Satellite+", TileImageType.JPG);
		mapSources = new MapSource[] { new USNationalMapImagery(), new USNationalMapVS() };
		initializeValues();
	}

	@Override
	public Color getBackgroundColor() {
		return Color.WHITE;
	}
}