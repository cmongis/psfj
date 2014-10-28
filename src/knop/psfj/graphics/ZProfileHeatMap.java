/*
    This file is part of PSFj.

    PSFj is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PSFj is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PSFj.  If not, see <http://www.gnu.org/licenses/>. 
    
	Copyright 2013,2014 Cyril MONGIS, Patrick Theer, Michael Knop
	
 */
package knop.psfj.graphics;

import ij.process.ImageProcessor;
import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.PSFj;
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ZProfileHeatMap.
 */
public class ZProfileHeatMap extends FullHeatMap{

	/**
	 * Instantiates a new z profile heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 */
	public ZProfileHeatMap(FovDataSet dataSet, BeadImage image) {
		super(dataSet, image, PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED, -1));
		
		initGenerator(NORMALIZED);
		initGenerator(NOT_NORMALIZED);
		
	}
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getGraph()
	 */
	public ImageProcessor getGraph() {
		return getGraph(NORMALIZED);
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		
		HeatMapGenerator generator = new HeatMapGenerator(dataSet,image);
		
		generator.setCurrentLUT("psfj_planarity");
		String columnName  = PSFj.getColumnID(PSFj.Z_PROFILE, normalized, 0);
		generator.setCurrentColumn(columnName);
		if(normalized == NORMALIZED) {
			generator.setMinAndMax(-1, 0, 1);
			generator.setUnit("");
			generator.setCurrentColumn(PSFj.getColumnID(PSFj.Z_PROFILE, normalized, 0));
			
		}
		
		else {
			generator.setUnit(MathUtils.MICROMETERS);
			if(dataSet.getColumnSize() > 0 ) {
				double mean = dataSet.getColumnStatistics(columnName).getMean();
				double fwhmZth = image.getMicroscope().getZTheoreticalResolution();
				generator.setMinAndMax(mean-fwhmZth, mean, mean + fwhmZth);
				generator.setMinAndMaxLabels("z mean - FWHMz th.", "z mean", "mean + FWHMz");
			}
		}
		
		setGenerator(generator, normalized);
		
	}
	


	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getShortDescription()
	 */
	public String getShortDescription() {
		return "Position of the beads in z";
	}
	
	public String getSaveId() {
		return super.getSaveId().replace("z_profile", "planarity");
	}
	
}
