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

import java.util.HashMap;

import javax.swing.ImageIcon;

import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.utils.MathUtils;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class FullHeatMap.
 */
public abstract class FullHeatMap implements PsfJGraph {

	//HeatMapGenerator2 generator;
	
	/** The field. */
	String field;
	
	/** The title. */
	String title;
	
	/** The description. */
	String description;
	
	/** The icon. */
	ImageIcon icon;
	
	/** The data set. */
	protected FovDataSet dataSet;
	
	/** The image. */
	protected BeadImage image;
	
	/** The processed heatmap. */
	ImageProcessor[] processedHeatmap = new ImageProcessor[3];
	
	/** The generators. */
	HeatMapGenerator[] generators = new HeatMapGenerator[3];
	
	
	
	
	/**
	 * Instantiates a new full heat map.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 * @param field the field
	 */
	public FullHeatMap(FovDataSet dataSet, BeadImage image,String field) {
		
		this.field = field;
		title = dataSet.getColumnName(field);
		this.dataSet = dataSet;
		
		this.image = image;
		
		ImageProcessor i = new ColorProcessor(16,16);
		i.setColor(image.getBeadsColor());
		i.fill();
		icon = new ImageIcon(i.getBufferedImage());
		
		
		
		
	//	initGenerator(PsfJGraph.NORMALIZED);
	//	initGenerator(PsfJGraph.NOT_NORMALIZED);
		
	}
	
	/**
	 * Inits the generator.
	 *
	 * @param normalized the normalized
	 */
	public void initGenerator(int normalized) {
		HeatMapGenerator generator = new HeatMapGenerator(dataSet,image);
		generator.setCurrentColumn(field);
		generator.setMinAndMax(dataSet.getMin(field),dataSet.getLine(field),dataSet.getMax(field));
		generator.setMinAndMaxLabels(dataSet.getMinLabel(field),dataSet.getLineLabel(field),dataSet.getMaxLabel(field));
		generator.setCurrentColumn(field);
		generator.getSpace();
		setGenerator(generator, normalized);
	}
	
	/**
	 * Sets the generator.
	 *
	 * @param generator the generator
	 * @param normalized the normalized
	 */
	public void setGenerator(HeatMapGenerator generator, int normalized) {
		generators[normalized] = generator;
		System.gc();
	}
	
	/**
	 * Gets the field.
	 *
	 * @return the field
	 */
	public String getField() {
		return field;
	}
	
	/**
	 * Gets the generator.
	 *
	 * @param normalized the normalized
	 * @return the generator
	 */
	public HeatMapGenerator getGenerator(int normalized) {
		return generators[normalized];
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getGraph()
	 */
	@Override
	public ImageProcessor getGraph() {
		// TODO Auto-generated method stub
		return getGraph(NORMALIZED);
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getGraph(int)
	 */
	public ImageProcessor getGraph(int normalized) {
		
		if(processedHeatmap[normalized] == null) {
			processedHeatmap[normalized] = getGenerator(normalized).getAnnotatedHeatMap();
		}
		return processedHeatmap[normalized];
		
	}
	
	/**
	 * Gets the heatmap name.
	 *
	 * @param normalized the normalized
	 * @return the heatmap name
	 */
	public String getHeatmapName(int normalized) {
		return getGenerator(normalized).getColumnName();
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		if(title == null)
		return dataSet.getColumnName(field);
		else return title;
		
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getDescription()
	 */
	@Override
	public String getDescription() {
		if(description == null) {
		
			HeatMapGenerator generator = getGenerator(PsfJGraph.NOT_NORMALIZED);
			HashMap<String,String> values = new HashMap<String, String>();
		
			values.put("%mean", MathUtils.roundToString(generator.getMeanNumberOfBeadsPerSquare(),2));
			values.put("%stepSize", MathUtils.formatDouble(generator.getStepSize(),"µm"));
			values.put("%windowSize", MathUtils.formatDouble(generator.getWindowSize(),"µm"));
			values.put("%width",""+generator.getColumnCount());
			values.put("%height", ""+generator.getRowCount());
			description =  TextUtils.readTextRessource(this, "/knop/psfj/graphics/FullHeatMap.html", values);
		}
		return description;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getSaveId()
	 */
	@Override
	public String getSaveId() {
		// TODO Auto-generated method stub
		return image.getImageNameWithoutExtension() + "_heatmap_"+field.replace("fwhm","fwhm_").replace("X","min").replace("Y", "max");
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getShortDescription()
	 */
	public String getShortDescription() {
		return "A heatmap";
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getImageIcon()
	 */
	@Override
	public ImageIcon getImageIcon() {
		// TODO Auto-generated method stub
		return icon;
	}


	/**
	 * Gets the bead image.
	 *
	 * @return the bead image
	 */
	public BeadImage getBeadImage() {
		// TODO Auto-generated method stub
		return image;
	}
	
}
