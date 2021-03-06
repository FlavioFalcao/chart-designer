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
package osmcd.program.atlascreators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import osmcd.exceptions.MapCreationException;
import osmcd.program.ProgramInfo;
import osmcd.program.annotations.AtlasCreatorName;
import osmcd.program.atlascreators.tileprovider.TileProvider;
import osmcd.program.interfaces.MapInterface;
import osmcd.program.interfaces.MapSpace;
import osmcd.utilities.Utilities;

/**
 * Touratech QV
 */
@AtlasCreatorName(value = "Touratech QV", type = "Ttqv")
public class TTQV extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(layerDir);
		} catch (IOException e1) {
			throw new MapCreationException(map, e1);
		}
		createTiles();
		writeCalFile();
	}

	private void writeCalFile() throws MapCreationException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + "_png.cal"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
			double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;
			double scale = ((latitudeMax - latitudeMin) * (longitudeMax - longitudeMin)) / (width * height);

			String nsowLine = "%s = 6 = %2.6f\r\n";
			String cLine = "c%d_%s = 7 =  %2.6f\r\n";

			mapWriter.write("; Calibration File for QV Map\r\n");
			mapWriter.write("; generated by " + ProgramInfo.getCompleteTitle() + "\r\n");
			mapWriter.write("name = 10 = " + mapName + ".png\r\n");
			mapWriter.write("fname = 10 = " + mapName + ".png\r\n");
			mapWriter.write(String.format(Locale.ENGLISH, nsowLine, "nord", latitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, nsowLine, "sued", latitudeMin));
			mapWriter.write(String.format(Locale.ENGLISH, nsowLine, "ost", longitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, nsowLine, "west", longitudeMin));
			NumberFormat nf = new DecimalFormat("0.000000E000", Utilities.DFS_ENG);
			mapWriter.write("scale_area = 6 =  " + nf.format(scale).toLowerCase() + "\r\n");
			mapWriter.write("proj_mode = 10 = proj\r\n");
			mapWriter.write("projparams = 10 = proj=merc\r\n");
			mapWriter.write("datum1 = 10 = WGS 84# 6378137# 298.257223563# 0# 0# 0#\r\n");
			mapWriter.write("c1_x = 7 =  0\r\n");
			mapWriter.write("c1_y = 7 =  0\r\n");
			mapWriter.write("c2_x = 7 =  " + (width - 1) + "\r\n");
			mapWriter.write("c2_y = 7 =  0\r\n");
			mapWriter.write("c3_x = 7 =  " + (width - 1) + "\r\n");
			mapWriter.write("c3_y = 7 =  " + (height - 1) + "\r\n");
			mapWriter.write("c4_x = 7 =  0\r\n");
			mapWriter.write("c4_y = 7 =  " + (height - 1) + "\r\n");
			mapWriter.write("c5_x = 7 =  0\r\n");
			mapWriter.write("c5_y = 7 =  0\r\n");
			mapWriter.write("c6_x = 7 =  0\r\n");
			mapWriter.write("c6_y = 7 =  0\r\n");
			mapWriter.write("c7_x = 7 =  0\r\n");
			mapWriter.write("c7_y = 7 =  0\r\n");
			mapWriter.write("c8_x = 7 =  0\r\n");
			mapWriter.write("c8_y = 7 =  0\r\n");
			mapWriter.write("c9_x = 7 =  0\r\n");
			mapWriter.write("c9_y = 7 =  0\r\n");
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 1, "lat", latitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 1, "lon", longitudeMin));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 2, "lat", latitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 2, "lon", longitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 3, "lat", latitudeMin));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 3, "lon", longitudeMax));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 4, "lat", latitudeMin));
			mapWriter.write(String.format(Locale.ENGLISH, cLine, 4, "lon", longitudeMin));

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			throw new MapCreationException("Error writing cal file: " + e.getMessage(), map, e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
