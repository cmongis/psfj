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

import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.heatmap.HeatMapGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class AsymmetryHeatMap.
 */
public class AsymmetryHeatMap extends FullHeatMap {

	/**
	 * Instantiates a new asymmetry heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 * @param field the field
	 */
	public AsymmetryHeatMap(FovDataSet dataSet, BeadImage image, String field) {
		super(dataSet, image, field);
		
		initGenerator(NORMALIZED);
		setGenerator(getGenerator(NORMALIZED), NOT_NORMALIZED);
		
		
	}

	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		HeatMapGenerator generator = new HeatMapGenerator(dataSet,image);
		generator.setCurrentLUT("psfj_asymmetry");
		generator.setCurrentColumn(field);
		generator.setMinAndMax(0, 0.5, 1);
		generator.setPlotMinAndMax(0, 1, 1.1);
		generator.setUnit("");
		generator.setMinAndMaxLabels("", "", "");
		setTitle(dataSet.getColumnName(field));
		setGenerator(generator, normalized);
	}
	
	public String getShortDescription() {
		return "FWHM max / FWHM min";
	}
	
}
