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
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.utils.MathUtils;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class DistanceHeatMap.
 */
public class DistanceHeatMap extends FullHeatMap {

	/** The norm value. */
	double normValue;

	/** The manager. */
	BeadImageManager manager;

	/**
	 * Instantiates a new distance heat map.
	 * 
	 * @param dataSet
	 *           the data set
	 * @param image
	 *           the image
	 * @param field
	 *           the field
	 */
	public DistanceHeatMap(FovDataSet dataSet, BeadImage image, String field) {
		super(dataSet, image, field);
		normValue = image.getMicroscope().getXYTheoreticalResolution();
	}

	/**
	 * Sets the manager.
	 * 
	 * @param manager
	 *           the new manager
	 */
	public void setManager(BeadImageManager manager) {
		this.manager = manager;
	}

	/**
	 * Instantiates a new distance heat map.
	 * 
	 * @param dataSet
	 *           the data set
	 * @param image
	 *           the image
	 * @param field
	 *           the field
	 * @param normalizationValue
	 *           the normalization value
	 */
	public DistanceHeatMap(FovDataSet dataSet, BeadImage image, String field,
			double normalizationValue) {
		super(dataSet, image, field);
		this.normValue = normalizationValue;

		try {

			ImageProcessor i = new ColorProcessor(20, 20);
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

		} catch (NullPointerException e) {
			System.err.println("Error when generating icon...");
		} catch (IndexOutOfBoundsException e2) {
			icon = new ImageIcon(new ColorProcessor(20, 20).getBufferedImage());
		}

		initGenerator(NORMALIZED);
		initGenerator(NOT_NORMALIZED);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.graphics.FullHeatMap#getGraph()
	 */
	@Override
	public ImageProcessor getGraph() {

		return getGraph(NOT_NORMALIZED);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.graphics.FullHeatMap#initGenerator(int)
	 */
	public void initGenerator(int normalized) {
		HeatMapGenerator generator = new HeatMapGenerator(dataSet, image);

		generator.setCurrentLUT("psfj_planarity");
		if (normalized == NORMALIZED) {
			generator.setCurrentColumn(field + "_norm");
			generator.setMinAndMax(-1, 0, 1);
			generator.setPlotMinAndMax(0, 0, 1);
			generator.setUnit("");
			generator.setMinAndMaxLabels("x Th. Value", "", "x Th. Value");
			setTitle(dataSet.getColumnName(field + "_norm"));
		} else {
			setTitle(dataSet.getColumnName(field));
			generator.setCurrentColumn(field);
			generator.setUnit(MathUtils.MICROMETERS);
			generator.setMinAndMax(-normValue, 0, normValue);
			generator.setMinAndMaxLabels("", "", "FWHM (th)");
			generator.setPlotMinAndMax(0, 0, normValue);
		}
		setGenerator(generator, normalized);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.graphics.FullHeatMap#getDescription()
	 */
	public String getDescription() {
		return TextUtils.readTextRessource(this, "/knop/psfj/graphics/DistanceHeatMap.html")
				+ super.getDescription();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.graphics.FullHeatMap#getSaveId()
	 */
	public String getSaveId() {

		String suffix;
		if (manager == null)
			suffix = image.getImageNameWithoutExtension();
		else
			suffix = manager.getBeadImage(0).getImageNameWithoutExtension() + "_"
					+ manager.getBeadImage(1).getImageNameWithoutExtension();

		if (field.equals("deltaD")) {
			return suffix + "_heatmap_distance_XY";
		} else if (field.equals("delta3D")) {
			return suffix + "_heatmap_distance_XYZ";
		}

		else
			return suffix + "_" + field;

	}

}
