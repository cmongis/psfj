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
package knop.psfj.exporter;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

import knop.psfj.BeadFrame;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.PSFj;
import knop.psfj.graphics.AsymmetryHeatMap;
import knop.psfj.graphics.DistanceHeatMap;
import knop.psfj.graphics.FullHeatMap;
import knop.psfj.graphics.PsfJGraph;
import knop.psfj.graphics.ThetaHeatMap;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.FileUtils;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class CsvExporter.
 */
public class CsvExporter {

	/** The manager. */
	BeadImageManager manager;

	/** The monocolor columns. */
	public ArrayList<String> monocolorColumns;
	
	/** The dual color columns. */
	public ArrayList<String> dualColorColumns;

	/** The Z0_ zmean. */
	public String Z0_ZMEAN = "z0 - z mean";

	/**
	 * Instantiates a new csv exporter.
	 *
	 * @param m the m
	 */
	public CsvExporter(BeadImageManager m) {
		manager = m;

		monocolorColumns = new ArrayList<String>(
				Arrays.asList(new String[]{
						PSFj.BEAD_ID,

						PSFj.XC,
						PSFj.YC,
						PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED),
						PSFj.getColumnID(PSFj.FWHM_KEY, PSFj.X_AXIS,
								PSFj.NOT_NORMALIZED, 0),
						PSFj.getColumnID(PSFj.FWHM_KEY, PSFj.Y_AXIS,
								PSFj.NOT_NORMALIZED, 0),
						PSFj.getColumnID(PSFj.FWHM_KEY, PSFj.Z_AXIS,
								PSFj.NOT_NORMALIZED, 0),
						PSFj.Z0_ZMEAN,
						PSFj.ASYMMETRY_KEY,
						PSFj.THETA_KEY,
						PSFj.getColumnID(PSFj.R_COEFF_KEY, PSFj.X_AXIS,
								PSFj.NOT_NORMALIZED, 0),
						PSFj.getColumnID(PSFj.R_COEFF_KEY, PSFj.Z_AXIS,
								PSFj.NOT_NORMALIZED, 0), PSFj.IS_FITTING_VALID}));

		dualColorColumns = new ArrayList<String>(monocolorColumns);

		dualColorColumns.add(PSFj.getColumnID(PSFj.CHR_SHIFT_XY,
				PSFj.NOT_NORMALIZED));
		dualColorColumns.add(PSFj.getColumnID(PSFj.CHR_SHIFT_XYZ,
				PSFj.NOT_NORMALIZED));

		for (int axe : PSFj.AXES) {
			dualColorColumns.add(PSFj.getColumnID(PSFj.CHR_SHIFT_KEY, axe,
					PSFj.NOT_NORMALIZED, 0));
		}

	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		BeadImageManager manager = new BeadImageManager();
		manager.add("/home/cyril/test_img/6/6_gfp.tif");
		manager.add("/home/cyril/test_img/6/6_mcherry.tif");
		// manager.add("/Users/cyril/test_img/5/dual channel/ch1_gfp-mc-60x_220um_beads01_R3D.tif");
		// manager.add("/Users/cyril/test_img/5/dual channel/ch2_gfp-mc-60x_220um_beads01_R3D.tif");
		manager.setAnalysisType(BeadImageManager.DUAL_CHANNEL);
		manager.autoFocus(0);
		manager.autoThreshold();
		manager.autoBeadEnlargementFactor();
		manager.processProfiles();
		manager.exportCSVFile(true);
	}

	/**
	 * Rename column name.
	 *
	 * @param dataSet the data set
	 */
	public static void renameColumnName(FovDataSet dataSet) {

		dataSet.setColumnName(PSFj.XC, "x0");
		dataSet.setColumnName(PSFj.YC, "y0");
		dataSet.setColumnName(
				PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED), "z0");

	}

	/**
	 * Export csv.
	 *
	 * @param path the path
	 * @param openAfter the open after
	 */
	public void exportCsv(String path, boolean openAfter) {
		exportCsv(path, null, openAfter);
	}

	/**
	 * Export csv.
	 *
	 * @param path the path
	 * @param roi the roi
	 * @param openAfter the open after
	 */
	public void exportCsv(String path, Rectangle roi, boolean openAfter) {
		
		int progress = 10;
		String status = "Generating CSV...";
		
		
		
		manager.setProgress(path, progress+=20);
		
		if (manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL) {

			FovDataSet dataSet = new FovDataSet();

			for (BeadImage image : manager.getBeadImageList()) {
				dataSet.mergeDataSet(image.getBeadFrameList().getFromROI(roi)
						.getDataSet(false, false));
			}
			
			ArrayList<String> columns = new ArrayList<String>(monocolorColumns);
			if(manager.getBeadImageList().size()> 0) {
				columns.add(PSFj.SOURCE_IMAGE);
			}
			
			manager.setProgress(status, progress+=20);
			dataSet.recalculateZProfileNormalisation(manager.getZProfileMean(0));
			manager.setProgress(status, progress+=20);
			addBeadImageInfos(dataSet, 0);
			addHeatMapInfos(dataSet);
			manager.setProgress(status, progress+=20);
			renameColumnName(dataSet);
			TextUtils.writeStringToFile(path,
					dataSet.exportToString(columns), false);

		}

		else {

			String roiExtension = "";
			if(path.endsWith("/") == false) path = path+"/";
			if (roi != null)
				roiExtension = String.format("_roi_%dx%dpx_at_%dx%d", roi.width,
						roi.height, roi.x, roi.y);
			
			
			//Filtering data
			manager.setProgress(status, progress+=10);
			String path1 = path
					+ manager.getBeadImage(0).getImageNameWithoutExtension()
					+ roiExtension + "_data.csv";
			String path2 = path
					+ manager.getBeadImage(1).getImageNameWithoutExtension()
					+ roiExtension + "_data.csv";
			
			
			FovDataSet dataSet1 = manager.getBeadImage(0).getBeadFrameList()
					.getFromROI(roi).getDataSet(false, true);
			
			manager.setProgress(status, progress+=10);
			dataSet1.recalculateZProfileNormalisation(manager.getZProfileMean(0));
			
			manager.setProgress(status, progress+=10);
			FovDataSet dataSet2 = manager.getBeadImage(1).getBeadFrameList()
					.getFromROI(roi).getDataSet(false, true);
			
			manager.setProgress(status, progress+=10);
			dataSet2.recalculateZProfileNormalisation(manager.getZProfileMean(1));
			
			manager.setProgress(status, progress+=10);
			renameColumnName(dataSet1);
			renameColumnName(dataSet2);

			String path3 = path
					+ manager.getBeadImage(0).getImageNameWithoutExtension() + "_"
					+ manager.getBeadImage(1).getImageNameWithoutExtension()
					+ roiExtension + "_comparaison.csv";
			manager.setProgress(status, progress+=10);
			addBeadImageInfos(dataSet1, 0);
			addBeadImageInfos(dataSet2, 1);

			manager.setProgress(status, progress+=10);
			addHeatMapInfos(dataSet1);
			addHeatMapInfos(dataSet2);

			String csv1 = dataSet1.exportToString(monocolorColumns);
			String csv2 = dataSet2.exportToString(monocolorColumns);
			String csv3 = getCompareDataSet(roi);

			manager.setProgress(status, progress+=10);
			TextUtils.writeStringToFile(path1, csv1, false);
			TextUtils.writeStringToFile(path2, csv2, false);
			TextUtils.writeStringToFile(path3, csv3, false);

			if (openAfter) {
				FileUtils.openFolder(path1);
				FileUtils.openFolder(path2);
				FileUtils.openFolder(path3);
			}
		}

		if (openAfter) {
			FileUtils.openFolder(path);
		}
		manager.setProgress("", 0);
	}

	/**
	 * Gets the compare data set.
	 *
	 * @param roi the roi
	 * @return the compare data set
	 */
	public String getCompareDataSet(Rectangle roi) {

		String wavelength1 = manager.getBeadImage(0).getMicroscope()
				.getWaveLengthAsString();
		String wavelength2 = manager.getBeadImage(1).getMicroscope()
				.getWaveLengthAsString();
		String unit = manager.getMicroscope(0).getUnit();
		BeadFrameList list = manager.getBeadImage(0).getBeadFrameList()
				.getFromROI(roi).getWithAlterEgo();
		return getCompareDataSet(list, wavelength1, wavelength2, unit);

	}
	
	/**
	 * Gets the compare data set.
	 *
	 * @param list the list
	 * @param wavelength1 the wavelength1
	 * @param wavelength2 the wavelength2
	 * @param unit the unit
	 * @return the compare data set
	 */
	public String getCompareDataSet(BeadFrameList list, String wavelength1,
			String wavelength2, String unit) {
		FovDataSet resultData = new FovDataSet();

		String BEAD_1 = "Bead Id ch 1";
		String BEAD_2 = "Bead Id ch 2";

		String X0_1 = "x0 ch 1";
		String X0_2 = "x0 ch 2";
		String DX = "delta x0";

		String Y0_1 = "y0 ch 1";
		String Y0_2 = "y0 ch 2";
		String DY = "delta y0";

		String Z0_1 = "z0 ch 1";
		String Z0_2 = "z0 ch 2";
		String DZ = "delta z0";

		resultData.addColumn(BEAD_1, BEAD_2, X0_1, X0_2, DX, Y0_1, Y0_2, DY,
				Z0_1, Z0_2, DZ);
		resultData.setColumnsUnits(unit, X0_1, X0_2, DX, Y0_1, Y0_2, DY, Z0_1,
				Z0_2, DZ);

		for (BeadFrame frame : list) {

			BeadFrame alterEgo = frame.getAlterEgo();

			resultData.addValue(BEAD_1, frame.getId());
			resultData.addValue(BEAD_2, alterEgo.getId());

			resultData.addValue(X0_1, frame.getFovX());
			resultData.addValue(X0_2, alterEgo.getFovX());
			resultData.addValue(DX, frame.getDeltaX());

			resultData.addValue(Y0_1, frame.getFovY());
			resultData.addValue(Y0_2, alterEgo.getFovY());
			resultData.addValue(DY, frame.getDeltaY());

			resultData.addValue(Z0_1, frame.getZProfile());
			resultData.addValue(Z0_2, alterEgo.getZProfile());
			resultData.addValue(DZ, frame.getDeltaZ());
		}

		resultData.setMetaDataValue("Channel 1", manager.getBeadImage(0)
				.getImageName());
		resultData.setMetaDataValue("Wavelength (channel 1)", wavelength1);

		resultData.setMetaDataValue("Channel 2", manager.getBeadImage(1)
				.getImageName());
		resultData.setMetaDataValue("Wavelength (channel 2)", wavelength2);

		resultData.setMetaDataValue("Note",
				"delta values = channel 2 - channel 1");
		resultData.setMetaDataValue("  ",
				"For x and y coordinates : the origin is the center of the image.");

		return resultData.exportToString();

	}

	/**
	 * Adds the bead image infos.
	 *
	 * @param dataSet the data set
	 * @param imageId the image id
	 */
	public void addBeadImageInfos(FovDataSet dataSet, int imageId) {

		BeadImage image = manager.getBeadImage(imageId);
		Microscope mic = manager.getMicroscope(0);

		dataSet.setMetaDataValue("", "");
		dataSet.addMetaDataSpace();
		dataSet.setMetaDataValue(image.getImageName(), "");
		dataSet.setMetaDataValue("Emission wavelength",
				mic.getWaveLengthAsString());
		dataSet.setMetaDataValue("NA", mic.getNAAsString());
		dataSet.setMetaDataValue("Refraction index",
				mic.getRefractionIndexAsString());
		dataSet.setMetaDataValue("Voxel size", mic.getVoxelSizeAsString());
		dataSet.setMetaDataValue("Bead diameter", mic.getBeadSizeAsString());
		dataSet.addMetaDataSpace();
		dataSet.setMetaDataValue("Frame size", image.getFrameSize() + " pixels");
		dataSet.setMetaDataValue("Threshold", image.getThresholdValue());
		dataSet.addMetaDataSpace();
		dataSet.setMetaDataValue("Theoretical FWHM in XY", image.getMicroscope()
				.getTheoreticalResolutionAsString(PSFj.X_AXIS));
		dataSet.setMetaDataValue("Theoratical FWHM in Z", image.getMicroscope()
				.getTheoreticalResolutionAsString(PSFj.Z_AXIS));

	}

	/**
	 * Adds the heat map infos.
	 *
	 * @param dataSet the data set
	 */
	public void addHeatMapInfos(FovDataSet dataSet) {
		dataSet.addMetaDataSpace();
		dataSet.setMetaDataValue("HeatMap Statistics", "");

		for (PsfJGraph graph : manager.getGraphList()) {
			if (FullHeatMap.class.isAssignableFrom(graph.getClass())) {

				FullHeatMap heatmap = (FullHeatMap) graph;

				String suffix = (manager.getAnalysisType() == BeadImageManager.DUAL_CHANNEL && heatmap instanceof DistanceHeatMap == false)
						? " (" + heatmap.getBeadImage().getImageName() + ")"
						: "";
				dataSet.setMetaDataValue(
						heatmap.getHeatmapName(PSFj.NOT_NORMALIZED) + suffix, manager
								.getHeatmapStatisticsAsString(heatmap,
										PSFj.NOT_NORMALIZED));

				if (heatmap instanceof ThetaHeatMap == false
						&& heatmap instanceof AsymmetryHeatMap == false)
					dataSet.setMetaDataValue(heatmap.getHeatmapName(PSFj.NORMALIZED)
							+ suffix, manager.getHeatmapStatisticsAsString(heatmap,
							PSFj.NORMALIZED));

				if (graph instanceof ThetaHeatMap)
					dataSet.addMetaDataSpace();
			}
		}

	}

}
