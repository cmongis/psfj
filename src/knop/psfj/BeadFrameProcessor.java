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

import java.util.Observable;
import java.util.Observer;

import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadFrameProcessor.
 */
public class BeadFrameProcessor extends Observable implements Observer {

	/** The list. */
	BeadFrameList list;

	/** The fitting threshold. */
	double fittingThreshold = 0.9;

	/** The total. */
	int total;
	
	/** The frame completed. */
	int frameCompleted;

	/** The start time. */
	long startTime;
	
	/** The last refresh. */
	long lastRefresh;

	/** The refresh delay. */
	int refreshDelay = 500;

	/** The stop signal. */
	boolean stopSignal = false;

	/** The current bead image. */
	BeadImage currentBeadImage;

	/** The garbage collection frequence. */
	protected int garbageCollectionFrequence = 30;

	/**
	 * Instantiates a new bead frame processor.
	 *
	 * @param list the list
	 */
	public BeadFrameProcessor(BeadFrameList list) {
		this.list = list;
		init();
	}

	/**
	 * Gets the bead per second.
	 *
	 * @return the bead per second
	 */
	public int getBeadPerSecond() {
		long now = System.currentTimeMillis();
		return MathUtils.round((1.0 * frameCompleted)
				/ (1.0 * (now - startTime) / 1000));
	}

	/**
	 * Inits the.
	 */
	public void init() {
		total = list.size();
		startTime = System.currentTimeMillis();
		lastRefresh = System.currentTimeMillis();
		frameCompleted = 0;
	}

	/**
	 * Refresh.
	 */
	public synchronized void refresh() {
		long now = System.currentTimeMillis();

		if (frameCompleted % garbageCollectionFrequence == 0) {
			System.gc();
		}

		if (now - lastRefresh > refreshDelay) {

			int beadPerMinute = getBeadPerSecond();
			if (total != 0)
				currentBeadImage.setProgress(frameCompleted * 100 / total,
						"Executing PSF Fitting : " + frameCompleted + "/" + total
								+ " (" + beadPerMinute + " b/s)");
			lastRefresh = now;
		}
	}

	/**
	 * Process.
	 */
	public void process() {

		total = list.size();
		int i = 0;
		if (total == 0)
			return;

		init();

		BeadImage image = list.get(0).getSource();
		currentBeadImage = image;

		for (BeadFrame bead : list) {

			if (stopSignal) {
				setChanged();
				image.setStatus("Error ! Not enough Memory");
				break;
			}

			bead.addObserver(this);

			bead.findPSF();
			frameCompleted++;
			bead.deleteObserver(this);
			refresh();

			// image.setProgress(i++ * 100 / total, "Executing PSF Fitting...");
		}
	}

	/**
	 * Filter.
	 */
	public void filter() {

		System.gc();

		double[] resolutions;

		double resolutionX;
		double resolutionY;
		double resolutionZ;

		double correctedResolutionX;
		double correctedResolutionY;
		double correctedResolutionZ;
		
		
		
		if(list.size() ==0) return;
		BeadImage image = list.get(0).getSource();
		image.setStatus("Filtering beads");

		// Statistics will be created in order to filter out outlayers

		/* Getting statistic of the profile */

		Integer finalPid = 1;

		boolean isFittingValid;

		double stackSize = 0.0;

		// double meanResolution = zResolution.getPercentile(50);

		double stackWidth = list.get(0).getRealWidth();
		double stackHeight = list.get(0).getRealHeight();
		double stackDepth = list.get(0).getStackSize();

		double axeSize[] = new double[3];

		axeSize[0] = stackWidth;
		axeSize[1] = stackHeight;
		axeSize[2] = stackDepth;
		double cMin, cMax, rThreshold;

		stackSize = list.get(0).getStackSize();
		int total = list.size();
		int i = 0;

		for (BeadFrame bead : list) {

			isFittingValid = true;
			String reason;
			double r, c;

			/* filtering R parameter */

			for (int j = 0; j != 3; j++) {
				if (j <= 1) {
					if (bead.getFittingGoodness(j) < 0.9) {
						isFittingValid = false;
					}
				} else {
					if (bead.getFittingGoodness(j) < 0.8) {
						isFittingValid = false;
					}
				}
			}
			/*
			 * r = bead.getMinimumFittingGoodness(); if(r < fittingThreshold) {
			 * isFittingValid = false;
			 * 
			 * }
			 */

			
			/* filtering x0 and y0 parameters for 2D fits */

			if (bead instanceof BeadFrame2D) {
				BeadFrame2D beadframe2d = (BeadFrame2D) bead;

				double xThreshold = image.getMicroscope()
						.getXYTheoreticalResolution()
						/ image.getCalibration().pixelWidth;
				double yThreshold = image.getMicroscope()
						.getXYTheoreticalResolution()
						/ image.getCalibration().pixelHeight;

				double[] fittedParameters = beadframe2d.getFittedParameters();
				if (fittedParameters != null) {
					double x0 = fittedParameters[BeadFrame2D.X0];
					double y0 = fittedParameters[BeadFrame2D.Y0];

					double xd = Math.abs(x0 - bead.getCentroidX());
					double yd = Math.abs(y0 - bead.getCentroidY());

					// System.out.println(String.format("xThreshold : %.3f, x0 : %.3f, xd = %.3f",xThreshold,x0,yd));
					// System.out.println(String.format("yThreshold : %.3f, y0 : %.3f, yd = %.3f",xThreshold,x0,yd));
					
					//after that the bead x and y is re-centered
					beadframe2d.recenterCentroidFrom2DFitting();
					
					
					
					if (xd > xThreshold || yd > yThreshold) {
						isFittingValid = false;
						bead.setInvalidityReason("The fitted bead is likely to be a different bead from the center bead.");
					}

				}

			}
			
			
			
			/* filtering C parameter */

			for (int axe = 0; axe != 3; axe++) {
				// in case of 2D Fitting, there is no c parameters for x and y, so
				// we skip those axis
				if (bead instanceof BeadFrame2D && (axe == 0 || axe == 1))
					continue;
				c = bead.getCParameter(axe);
				cMin = bead.getResolution(axe) / 2;
				cMax = axeSize[axe] - bead.getResolution(axe) / 2;

				/* If there's an outlayer it's skipped */
				if (c < cMin || c > cMax || c < 0) {

					// System.out.println("R is deleted ! : " + r);
					isFittingValid = false;
					break;
				}
			}

			

			/*
			 * 
			 * for (int axe = 0; axe != 3; axe++) {
			 * 
			 * 
			 * if(bead.getSliceNumber() == 1 && axe == 2) continue;
			 * 
			 * if(bead.getResolution(axe) == Double.NaN) { bead.setValid(false,
			 * "The 2D fitting didn't work."); isFittingValid = false; break; }
			 * 
			 * c = bead.getCParameter(axe);
			 * 
			 * if(c == 0) { bead.setValid(false,
			 * "C parameter has not been calulated."); isFittingValid = false;
			 * break; }
			 * 
			 * r = bead.getFittingGoodness(axe); cMin = bead.getResolution(axe)/2;
			 * cMax = axeSize[axe] - bead.getResolution(axe)/2; rThreshold =
			 * fittingThreshold;
			 * 
			 * // If there's an outlayer it's skipped if (r < rThreshold || r <
			 * fittingThreshold || c < cMin || c > cMax || c < 0) {
			 * 
			 * // System.out.println("R is deleted ! : " + r); isFittingValid =
			 * false; }
			 * 
			 * }
			 */
			image.setProgress(i++, total);
			bead.setValid(isFittingValid, "R or C parameter off limits.");

		}

		// image.getMap();

	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		Message message = (Message) arg1;
		if (message.getName().contains("error")) {
			stopSignal = true;
		}
	}

	/**
	 * Sets the fitting threshold.
	 *
	 * @param fittingThreshold the new fitting threshold
	 */
	public void setFittingThreshold(double fittingThreshold) {
		this.fittingThreshold = fittingThreshold;

	}

	/**
	 * Gets the garbage collection frequence.
	 *
	 * @return the garbage collection frequence
	 */
	public int getGarbageCollectionFrequence() {
		return garbageCollectionFrequence;
	}

	/**
	 * Sets the garbage collection frequence.
	 *
	 * @param garbageCollectionFrequence the new garbage collection frequence
	 */
	public void setGarbageCollectionFrequence(int garbageCollectionFrequence) {
		this.garbageCollectionFrequence = garbageCollectionFrequence;
	}
}
