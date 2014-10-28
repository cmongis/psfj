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
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class FWHMHeatMap.
 */
public class FWHMHeatMap extends FullHeatMap {

	/** The norm value. */
	double normValue;
	
	
	
	/**
	 * Instantiates a new FWHM heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 * @param field the field
	 */
	public FWHMHeatMap(FovDataSet dataSet, BeadImage image, String field) {
		super(dataSet, image, field);
		
		try {
		normValue = dataSet.getTheoriticalValue(field);
		
		}
		catch(Exception e) {
			System.err.println("The data set is likeky to be empty.");
		}
		
		initGenerator(NORMALIZED);
		initGenerator(NOT_NORMALIZED);
		
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getGraph()
	 */
	public ImageProcessor getGraph() {
		return getGraph(NOT_NORMALIZED);
	}
	
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		
		HeatMapGenerator generator = new HeatMapGenerator(dataSet,image);
		
		generator.setCurrentLUT("psfj_fwhm");
		generator.setScaleDivision(3);
		if(normalized == NORMALIZED) {
			generator.setCurrentColumn(field+"_norm");
			generator.setMinAndMax(0.5, 1, 2);
			generator.setPlotMinAndMax(0.5, 1, 2);
			generator.setUnit("");
			generator.setMinAndMaxLabels("0.5x expected", "expected", "2x expected");
			setTitle(dataSet.getColumnName(field+"_norm"));
		}
		else {
			setTitle(dataSet.getColumnName(field));
			generator.setCurrentColumn(field);
			generator.setUnit(MathUtils.MICROMETERS);
			generator.setMinAndMax(normValue/2,normValue,normValue*2);
			generator.setMinAndMaxLabels("0.5x expected", "expected", "2x expected");
			generator.setPlotMinAndMax(normValue/2, normValue, normValue*2);
		}
		
		setGenerator(generator, normalized);
	}
	

	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getShortDescription()
	 */
	public String getShortDescription() {
		return "FWHM accross the field of view";
	}
	
	
}
