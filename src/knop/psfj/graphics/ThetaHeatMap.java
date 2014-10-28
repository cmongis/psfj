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
import knop.psfj.heatmap.ThetaHeatMapGenerator;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ThetaHeatMap.
 */
public class ThetaHeatMap extends FullHeatMap {

	
	
	
	
	
	/**
	 * Instantiates a new theta heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 * @param field the field
	 */
	public ThetaHeatMap(FovDataSet dataSet, BeadImage image, String field) {
		super(dataSet, image, field);
		setTitle(dataSet.getColumnName(field));
		
		initGenerator(NOT_NORMALIZED);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		HeatMapGenerator generator = new ThetaHeatMapGenerator(dataSet);
		generator.setBeadImage(image);
		generator.setCurrentColumn(field);
		generator.setMinAndMax(-90, 0, 90);
		generator.setPlotMinAndMax(-90, 0, 90);
		generator.setUnit("deg"
				);
		generator.setMinAndMaxLabels("", "", "");
		
		setGenerator(generator, NORMALIZED);
		setGenerator(generator, NOT_NORMALIZED);
	}
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getTitle()
	 */
	public String getTitle() {
		return "Asymmetry weigthed " + dataSet.getColumnName("theta");
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getDescription()
	 */
	public String getDescription() {
	
		return TextUtils.readTextRessource(this, "/knop/psfj/graphics/ThetaHeatMap.html") + super.getDescription();
	}
}
