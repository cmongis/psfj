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

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

import javax.swing.ImageIcon;

import knop.psfj.BeadFrame;
import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ShiftHeatMap.
 */
public class ShiftHeatMap extends DistanceHeatMap {
	
	/**
	 * Instantiates a new shift heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 * @param field the field
	 * @param normalizationValue the normalization value
	 */
	public ShiftHeatMap(FovDataSet dataSet, BeadImage image, String field,
			double normalizationValue) {
		super(dataSet, image, field, normalizationValue);
	
		ImageProcessor i = new ColorProcessor(20,20);
		
		
		try {
			BeadFrame f = image.getBeadFrameList().getOnlyValidBeads().get(0);
		
			Color c1 = f.getBeadColor();
			Color c2 = f.getAlterEgo().getBeadColor();
			
			i.setColor(Color.white);
			i.fill();
			
			i.setColor(c2);
			i.setRoi(5, 5, 15, 15);
			i.fill();
			i.drawRect(5, 5, 15, 15);
		
			i.setColor(c1);
			i.setRoi(0, 0, 15, 15);
			i.fill();
			i.drawRect(0, 0, 15, 15);
			
			icon = new ImageIcon(i.getBufferedImage());
			
		}
		catch(Exception e) {
			
		}
		
		initGenerator(NORMALIZED);
		initGenerator(NOT_NORMALIZED);
		
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.DistanceHeatMap#getGraph()
	 */
	public ImageProcessor getGraph() {
		return getGraph(NOT_NORMALIZED);
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.DistanceHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		HeatMapGenerator generator = new HeatMapGenerator(dataSet,image);
		generator.setCurrentLUT("psfj_planarity");
		if(normalized == NORMALIZED) {
			generator.setMinAndMax(-1, 0, 1);
			generator.setUnit("");
			generator.setCurrentColumn(field+"_norm");
			
		}
		else {
			generator.setMinAndMax(-1*normValue, 0, normValue);
			
			generator.setUnit(MathUtils.MICROMETERS);
			generator.setCurrentColumn(field);
			
			
		}
		setGenerator(generator, normalized);
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.FullHeatMap#getShortDescription()
	 */
	public String getShortDescription() {
		return "Distance between corresponding beads.";
	}
	
	
	

}
