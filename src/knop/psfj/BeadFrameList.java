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
package knop.psfj;

import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import knop.psfj.resolution.Microscope;
import knop.psfj.utils.MathUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadFrameList.
 */
public class BeadFrameList extends ArrayList<BeadFrame> {

	/**
	 * The bead hashmap. This hashmap is used for retrieving beads by there id.
	 * However, it becomes useless when mixing beads from different sources.
	 * */
	HashMap<Integer, BeadFrame> beadHash = new HashMap<Integer, BeadFrame>();

	
	
	String[] FWHM_NORM_NAMES = new String[] {
			"FWHMmin / Theoretical FWHMxy",
			"FWHMmax / Theoretical FWHMxy",
			"FWHMz / Theoretical FWHMz"
};
	
	
	/**
	 * Returns a FovDataSet of the valid beads contained inside the bead frame
	 * list.
	 * 
	 * @return the data set
	 */
	public FovDataSet getDataSet() {
		return getDataSet(true,false);
	}

	/**
	 * Returns a new BeadFrameList object containing only the valid beads.
	 * 
	 * @return the only valid beads
	 */
	public BeadFrameList getOnlyValidBeads() {
		BeadFrameList list = new BeadFrameList();
		for (BeadFrame frame : this) {
			if (frame.isValid())
				list.add(frame);
		}
		return list;
	}

	/**
	 * Returns a new BeadFrameList object containing only the beads paired with
	 * an alter ego.
	 * 
	 * @return the with alter ego
	 */
	public BeadFrameList getWithAlterEgo() {
		BeadFrameList list = new BeadFrameList();
		for (BeadFrame frame : this) {
			if (frame.getAlterEgo() != null)
				list.add(frame);
		}
		return list;
	}

	/**
	 * Return a FovDataSet of the beads containd inside the list. if
	 * excludeNonValid is true, only valid beads will be included in the data
	 * set. Elsewhere, valid and non-valid beads will be included.
	 * 
	 * @param excludeNonValid
	 *            the exclude non valid
	 * @return the data set
	 */
	public FovDataSet getDataSet(boolean excludeNonValid) {
		return getDataSet(excludeNonValid, true);
	}

	

	
	/**
	 * Gets the data set.
	 *
	 * @param excludeNonValid the exclude non valid
	 * @param includeMultichannelData the include multichannel data
	 * @return the data set
	 */
	public FovDataSet getDataSet(boolean excludeNonValid,
			boolean includeMultichannelData) {



		// instanciating the data set
		FovDataSet dataSet = new FovDataSet();

		// if the list is empty, the function is aborted
		if (size() == 0)
			return dataSet;

		// retrieving the microscope settings of the beads contained inside the
		// list
		Microscope m = get(0).getSource().getMicroscope(); // get the microscope
															// setting of the
															// first bead
		String unit = m.getUnit();
		
		
		if(includeMultichannelData) {
			dataSet.addColumn(PSFj.WAVELENGHT_KEY).setName("Wavelength");
		}
		dataSet.addColumn(PSFj.BEAD_ID).setName("Bead Id");;
		
		String[] axeStrings = new String[] {
				"min", "max", "z"
		};
		
		
		
		
		for(int axe : PSFj.AXES) {
			
			
			String columnNameNotNormalized = String.format("FWHM %s",axeStrings[axe]);
			String columnNameNotCorrected = columnNameNotNormalized + " not corrected";
			String columnNameNormalized = FWHM_NORM_NAMES[axe];
			String rCoeffName = String.format("R^2 FWHM%s", axeStrings[axe]);
			if(axe < 2) rCoeffName = "R^2 FWHM min/max";
			dataSet.addColumn(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NOT_NORMALIZED)).setUnit(unit).setName(columnNameNotNormalized);;
			dataSet.addColumn(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NOT_NORMALIZED)+"_nc").setUnit(unit).setName(columnNameNotCorrected);;;
			dataSet.addColumn(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NORMALIZED)).setName(columnNameNormalized);;
			dataSet.addColumn(PSFj.getColumnName(PSFj.R_COEFF_KEY, axe,PSFj.NOT_NORMALIZED)).setName(rCoeffName);
		}
		
		
		dataSet.addColumn(PSFj.ASYMMETRY_KEY).setName("Asymmetry");
		dataSet.addColumn(PSFj.THETA_KEY).setName("Theta").setUnit(MathUtils.DEGREES);
		
		dataSet.addColumn(PSFj.XC).setUnit(unit).setName("Bead x coordinates");
		dataSet.addColumn(PSFj.YC).setUnit(unit).setName("Bead y coordinates");
		dataSet.addColumn(PSFj.RADIUS).setUnit(unit).setName("Distance from the center");
		dataSet.addColumn(PSFj.Z_PROFILE,PSFj.NOT_NORMALIZED).setUnit(unit).setName("z0");;
		dataSet.addColumn(PSFj.Z_PROFILE,PSFj.NORMALIZED).setName("(z0 - mean(z0)) / FWHMz");;
		dataSet.addColumn(PSFj.Z0_ZMEAN).setUnit(unit);
	
		
		dataSet.addColumn(PSFj.THRESHOLD_KEY).setName("Threshold");
		dataSet.addColumn(PSFj.FRAME_SIZE_KEY).setName("Frame size");
		dataSet.addColumn(PSFj.SOURCE_IMAGE).setName("Image name");
		
		if(get(0) instanceof BeadFrame2D) {
			dataSet.addColumn(PSFj.SIGMA_X_KEY).setName("Sigma X").setUnit(unit);
			dataSet.addColumn(PSFj.SIGMA_Y_KEY).setName("Sigma Y").setUnit(unit);
			dataSet.addColumn(PSFj.THETA_IN_RADIAN_KEY).setName("Theta").setUnit(MathUtils.RADIANT);
		}
                
                dataSet
                        .addColumn(PSFj.CENTROID_BRIGHTNESS_KEY)
                        .setName(PSFj.CENTROID_BRIGHTNESS_NAME);
		
                dataSet.addColumn(PSFj.FITTED_BRIGHTNESS).setName(PSFj.FITTED_BRIGHTNESS);
                dataSet.addColumn(PSFj.FITTED_BACKGROUND).setName(PSFj.FITTED_BACKGROUND);
                
		axeStrings = new String[] { "x","y","z" };
		String[] axeNormsValue = new String[] { "XY","XY","Z" };
		
		if(includeMultichannelData) {
			
			for(int axe : PSFj.AXES) {
				String notNormName = String.format("Chromatic shift in %s", axeStrings[axe]);
				String normName = String.format("Chromatic shift in %s / Theo. FWHM %s",axeStrings[axe],axeNormsValue[axe]);
				
				dataSet.addColumn(PSFj.CHR_SHIFT_KEY, axe, PSFj.NOT_NORMALIZED).setUnit(unit).setName(notNormName);;
				dataSet.addColumn(PSFj.CHR_SHIFT_KEY, axe, PSFj.NORMALIZED).setName(normName);
			}
			
			dataSet.addColumn(PSFj.CHR_SHIFT_XY, PSFj.NOT_NORMALIZED).setUnit(unit).setName("Chromatic shift (XY)");
			dataSet.addColumn(PSFj.CHR_SHIFT_XY, PSFj.NORMALIZED).setName("Chromatic shift (XY) / Theo. FWHMxy");
			
			dataSet.addColumn(PSFj.CHR_SHIFT_XYZ,PSFj.NOT_NORMALIZED).setUnit(unit).setName("Chromatic shift (XYZ) ");
			dataSet.addColumn(PSFj.CHR_SHIFT_XYZ,PSFj.NORMALIZED).setName("Chromatic shift (XYZ) / Theo. FWHMz");
			
			
			
		}
		
		dataSet.addColumn(PSFj.IS_FITTING_VALID).setName("Fit valid");
		
		// for each bead of the list
		for (BeadFrame bead : this) {

			// if excludeNonValid is true and the current bead is not valid,
			// it's not included in the data set
			if (excludeNonValid == true && bead.isValid() == false)
				continue;

			
			
			for(int axe :PSFj.AXES) {
				dataSet.addValue(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NOT_NORMALIZED),bead.getCorrectionResolution(axe));
				dataSet.addValue(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NOT_NORMALIZED)+"_nc",bead.getResolution(axe));
				dataSet.addValue(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NORMALIZED),bead.getCorrectionResolution(axe)/bead.getSource().getMicroscope().getTheoreticalResolution(axe));
				
			}
			
			dataSet.addValue(PSFj.SOURCE_IMAGE,bead.getSource().getImageNameWithoutExtension());
			
			// adding bead position to the dataset
			dataSet.addValue(PSFj.BEAD_ID, bead.getId());

			
			if(includeMultichannelData) {
				dataSet.addValue(PSFj.WAVELENGHT_KEY,bead.getSource().getMicroscope().getWaveLength()*1000);
			}
			
			dataSet.addValue(PSFj.XC, bead.getFovX());
			dataSet.addValue(PSFj.YC, bead.getFovY());
			dataSet.addValue(PSFj.RADIUS, bead.getDistanceFromCenter());
                        
			double z_profile = bead.getCParameter(BeadFrame.Z);

			dataSet.addValue(PSFj.Z_PROFILE,PSFj.NOT_NORMALIZED, z_profile);
                        
                       

			/*
			// corrected and uncorrected resolutions
			dataSet.addValue("fwhmX", correctedResolutionX);
			dataSet.addValue("fwhmY", correctedResolutionY);
			dataSet.addValue("fwhmZ", correctedResolutionZ);

			dataSet.addValue("fwhmX_norm",
					correctedResolutionX / m.getXYTheoreticalResolution());
			dataSet.addValue("fwhmY_norm",
					correctedResolutionY / m.getXYTheoreticalResolution());
			dataSet.addValue("fwhmZ_norm",
					correctedResolutionZ / m.getZTheoreticalResolution());

			dataSet.addValue("fwhmX_nc", resolutionX);
			dataSet.addValue("fwhmY_nc", resolutionY);
			dataSet.addValue("fwhmZ_nc", resolutionZ);
			 */
			
			
			// the minimum fitting among X, Y and Z fitting
			//dataSet.addValue("fitting_min", bead.getMinimumFittingGoodness());

			
			
			dataSet.addValue(PSFj.ASYMMETRY_KEY,bead.getAsymetry());
			dataSet.addValue(PSFj.THETA_KEY, 180.0 * bead.getTheta() / Math.PI);
			dataSet.addValue(PSFj.FRAME_SIZE_KEY, bead.getBoundariesAsRectangle().width);
			
			dataSet.addValue(PSFj.THRESHOLD_KEY, bead.getSource().getThresholdValue());
			
                         // adding brightness
                        dataSet.addValue(PSFj.CENTROID_BRIGHTNESS_KEY,bead.getIntensity(bead.centroid));
                        
                        dataSet.addValue(PSFj.FITTED_BRIGHTNESS,bead.getFittingParameter(BeadFrame.X, BeadFrame.A));
                        dataSet.addValue(PSFj.FITTED_BACKGROUND,bead.getFittingParameter(BeadFrame.X, BeadFrame.B));
                        
			if(bead instanceof BeadFrame2D) {
			BeadFrame2D bead2d = (BeadFrame2D) bead;
				if(bead2d.getFittedParameters() == null) {
					//System.out.println("Fitted parameter is null... why ??");
					//System.out.println("isValid : "+bead2d.isValid);
					//System.out.println("fitting goodness : "+bead2d.getFittingGoodness(0));
					dataSet.addValue(PSFj.SIGMA_X_KEY,Double.NaN);
					dataSet.addValue(PSFj.SIGMA_Y_KEY,Double.NaN);
					dataSet.addValue(PSFj.THETA_IN_RADIAN_KEY,Double.NaN);
				}
				else {
					dataSet.addValue(PSFj.SIGMA_X_KEY,bead2d.getFittedParameters()[BeadFrame2D.SX]);
					
					dataSet.addValue(PSFj.SIGMA_Y_KEY,bead2d.getFittedParameters()[BeadFrame2D.SY]);
					dataSet.addValue(PSFj.THETA_IN_RADIAN_KEY,bead2d.getTheta());
					
				}
			}
			
                       
			
			// if the bead has an alter ego
			if (includeMultichannelData) {
				if(bead.getAlterEgo() == null) {
					dataSet.addValue("Corresponding bead id",-1);
				}
				else {
					dataSet.addValue("Corresponding bead id",bead.getAlterEgo().getId());
				}
				
				
				for(int axe : PSFj.AXES) {
					dataSet.addValue(PSFj.CHR_SHIFT_KEY, axe,PSFj.NOT_NORMALIZED, 1.0 * bead.getShift(axe));
					dataSet.addValue(PSFj.CHR_SHIFT_KEY, axe,PSFj.NORMALIZED, 1.0 * bead.getShift(axe)/m.getTheoreticalResolution(axe));
				}
				
				
				dataSet.addValue(PSFj.CHR_SHIFT_XY, PSFj.NOT_NORMALIZED, bead.getDistance(bead.getAlterEgo()));
				
				dataSet.addValue(PSFj.CHR_SHIFT_XY,PSFj.NORMALIZED,
						bead.getDistance(bead.getAlterEgo())
								/ m.getXYTheoreticalResolution());

				dataSet.addValue(PSFj.CHR_SHIFT_XYZ,PSFj.NOT_NORMALIZED,
						bead.getDistanceIncludingZ(bead.getAlterEgo()));
				
				dataSet.addValue(PSFj.CHR_SHIFT_XYZ,PSFj.NORMALIZED,
						bead.getDistanceIncludingZ(bead.getAlterEgo())
								/ m.getZTheoreticalResolution());

			}

			// adding fitting goodness
			for(int axe : PSFj.AXES) {
				dataSet.addValue(PSFj.R_COEFF_KEY,axe,PSFj.NOT_NORMALIZED,bead.getFittingGoodness(axe));
			}

			dataSet.addValue(PSFj.IS_FITTING_VALID, bead.isValid() ? 1 : 0);
		}
		
		for(int axe : PSFj.AXES) {
			dataSet.setTheoriticalValue(PSFj.getColumnName(PSFj.FWHM_KEY, axe, PSFj.NOT_NORMALIZED), get(0).getSource().getMicroscope().getTheoreticalResolution(axe));
		}

		

		dataSet.recalculateZProfileNormalisation();


		
		return dataSet;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(BeadFrame bead) {

		beadHash.put(bead.getId(), bead);
		return super.add(bead);

	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		beadHash.clear();
	}

	/**
	 * Gets a BeadFrame from its integer id.
	 * 
	 * @param id
	 *            the BeadFrame integer id
	 * @return a BeadFrame
	 */
	public BeadFrame getFromId(Integer id) {
		return beadHash.get(id);
	}

	/**
	 * Gets the number of non-valid beads.
	 *
	 * @return the number of non valid beads
	 */
	public int getNonValidBeadFrameCount() {

		return getTotalOfBeads() - getValidBeadFrameCount();
	}

	/**
	 * Gets the valid bead frames.
	 *
	 * @return the valid bead frames
	 */
	public int getValidBeadFrameCount() {
		int count = 0;
		for (BeadFrame frame : this) {
			if (frame.isValid()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Add the bead frames from an other bead frame list.
	 *
	 * @param beadFrameList            the bead frame list
	 */
	public void mergeList(BeadFrameList beadFrameList) {
		for (BeadFrame frame : beadFrameList) {
			add(frame);
		}
	}

	/**
	 * Gets the valid bead frames.
	 * 
	 * @return a list of the valid bead frames
	 */
	public BeadFrameList getValidBeadFrames() {

		BeadFrameList list = new BeadFrameList();

		for (BeadFrame frame : this) {
			if (frame.isValid()) {
				list.add(frame);
			}
		}

		return list;
	}

	
	
	
	
	/**
	 * Gets the total of beads.
	 * 
	 * @return the total of beads
	 */
	public int getTotalOfBeads() {
		return size();
	}

	/**
	 * Gets the delta x statistics.
	 * 
	 * @return the delta x statistics
	 */
	public DescriptiveStatistics getDeltaXStatistics() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (BeadFrame f : this) {
			if (f.isValid() == false)
				continue;
			stats.addValue(f.getDeltaX());
		}
		return stats;
	}

	/**
	 * Gets the delta y statistics.
	 * 
	 * @return the delta y statistics
	 */
	public DescriptiveStatistics getDeltaYStatistics() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (BeadFrame f : this) {
			if (f.isValid() == false)
				continue;
			stats.addValue(f.getDeltaY());
		}
		return stats;
	}

	/**
	 * Gets the delta z statistics.
	 * 
	 * @return the delta z statistics
	 */
	public DescriptiveStatistics getDeltaZStatistics() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (BeadFrame f : this) {
			if (f.isValid() == false)
				continue;
			stats.addValue(f.getDeltaZ());
		}
		return stats;
	}

	/**
	 * Gets the number of beads paired bead frames (with an alter ego).
	 *
	 * @return the number of beads with an alter ego
	 */
	public int getPairedCount() {
		int count = 0;
		for (BeadFrame f : this) {
			if (f.getAlterEgo() != null) {
				count++;
			}
		}

		return count;
	}

	
	
	/**
	 * Gets the from roi.
	 *
	 * @param roi the roi
	 * @return the from roi
	 */
	public BeadFrameList getFromROI(Rectangle roi) {
		
		if(roi == null) return this;
		BeadFrameList list = new BeadFrameList();
		
		for(BeadFrame frame : this) {
			if(roi.contains(frame.getFrameCenterX(), frame.getFrameCenterY())) list.add(frame);
		}
		
		return list;
		
	}
	
	
	/**
	 * Gets the certain number of beads contained inside a defined zone. The
	 * zone should be in pixel and in the image space.
	 * 
	 * @param zone
	 *            the zone
	 * @param max
	 *            the maximum number of beads to be retrieved
	 * @return the sample from corner
	 */
	public BeadFrameList getSampleFromCorner(Rectangle zone, int max) {

		BeadFrameList list = new BeadFrameList();

		int count = 0;

		for (BeadFrame frame : this) {
			if (zone.contains(new Point(frame.getFrameX(), frame.getFrameY()))
					&& frame.getAlterEgo() != null) {
				list.add(frame);
				count++;

			}
			if (count >= max)
				break;

		}

		return list;

	}

	/**
	 * Gets the bead montage of the bead contained inside the list.
	 *
	 * @return the bead montage
	 */
	public ImageProcessor getBeadMontage() {

		if (size() == 0)
			return new ColorProcessor(10, 10);

		int lines = MathUtils.round(Math.sqrt(size()));
		int columns = lines;

		int fw = get(0).getWidth();
		int fh = get(0).getHeight();

		int w = get(0).getWidth() * lines;
		int h = get(0).getHeight() * lines;

		ColorProcessor montage = new ColorProcessor(w, h);

		int count = 0;

		for (int l = 0; l != lines; l++) {
			for (int c = 0; c != columns; c++) {
				montage.copyBits(get(count).getOverlayWithAlterEgo(), c * fw, l
						* fh, Blitter.COPY);
				montage.setColor(Color.yellow.darker().darker().darker()
						.darker());
				montage.drawRect(c * fw, l * fh, fw, fh);
				count++;
				if (count >= size())
					break;
			}
			if (count >= size())
				break;
		}

		return montage;

	}
	
	/**
	 * Gets the.
	 *
	 * @param start the start
	 * @param count the count
	 * @return the bead frame list
	 */
	public BeadFrameList get(int start, int count) {
		
		BeadFrameList list = new BeadFrameList();
		
		int index = 0;
		for(BeadFrame frame : this) {
			if ((index >= start) && (index - start < count)) {
            list.add(frame);
        }
        index++;
		}
		
		
		return list;
	}
	
	
}
