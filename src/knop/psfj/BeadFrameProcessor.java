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
 * This class processes takes a list of beads and processes the PSFs for each bead. When finished, the BeadFrameProcessor filters the beads
 * according to predertemined parameters.
 */
public class BeadFrameProcessor extends Observable implements Observer {

	/** The list that must be processed. */
	BeadFrameList list;

	/** The fitting threshold. If the fitting efficiency is lower than this value, the beads are excluded*/
	double fittingThreshold = 0.9;

	/** The total number of frames. */
	int total;
	
	/** The number frame completed. */
	int frameCompleted;

	/** The start time. */
	long startTime;
	
	/** The last refresh. */
	long lastRefresh;

	/** The period to which the view is refreshed. */
	int refreshDelay = 500;

	/** The stop signal. */
	boolean stopSignal = false;

	/** The bead image from which the list of beads is extracted. */
	BeadImage currentBeadImage;

	/** The garbage collection frequence. The garbage collector will be called every 30 beads by default. */
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
	 * Gets the number of bead processed per second.
	 *
	 * @return the bead per second
	 */
	public int getBeadPerSecond() {
		long now = System.currentTimeMillis();
		return MathUtils.round((1.0 * frameCompleted)
				/ (1.0 * (now - startTime) / 1000));
	}

	/**
	 * Inits the process
	 */
	public void init() {
		total = list.size();
		startTime = System.currentTimeMillis();
		lastRefresh = System.currentTimeMillis();
		frameCompleted = 0;
	}

	/**
	 * Refresh the view b
	 */
	public synchronized void refresh() {
		
		
		long now = System.currentTimeMillis();

		if (frameCompleted % garbageCollectionFrequence == 0) {
			System.gc();
		}
		// if the last refresh was more than refresh delay
		if (now - lastRefresh > refreshDelay) {

			// the number of processed bead per second is processed
			int beadPerSecond = getBeadPerSecond();
			if (total != 0)
				currentBeadImage.setProgress(frameCompleted * 100 / total,
						"Executing PSF Fitting : " + frameCompleted + "/" + total
								+ " (" + beadPerSecond + " b/s)");
			lastRefresh = now;
		}
	}

	/**
	 * Process the beads
	 */
	public void process() {

		// gets the total number of beads
		total = list.size();
		
		int i = 0;
		
		//if the total is 0, then the function is aborted
		if (total == 0)
			return;
		
		//launching the init sequence
		init();
		
		//getting the bead image from where the beads are processed
		BeadImage image = list.get(0).getSource();
		
		// setting it like the current image
		currentBeadImage = image;

		// for each bead frame of the list
		for (BeadFrame bead : list) {

			// if the stop signal is set to 'true', the function is aborted.
			if (stopSignal) {
				setChanged();
				image.setStatus("Error ! Not enough Memory");
				break;
			}

			// the bead is observed
			bead.addObserver(this);

			// searching for the psf
			bead.findPSF();
			
			// incrementing the number of psf calculation completed
			frameCompleted++;
			
			// deleting the observer
			bead.deleteObserver(this);
			
			// refreshing the view is possible
			refresh();

		}
	}

	/**
	 * Filters the beads
	 */
	public void filter() {

		// calling the garbage collector just in case
		System.gc();
		
		// if the list is empty, the function is aborted
		if(list.size() ==0) return;
		
		// getting the bead image from where the beads are extracted
		BeadImage image = list.get(0).getSource();
		
		// setting the status of the bead image
		image.setStatus("Filtering beads");
		
		// checking if the fitting is valid
		boolean isFittingValid;

		double stackSize;
		
		// getting the image stack dimension for filtering
		double stackWidth = list.get(0).getRealWidth();
		double stackHeight = list.get(0).getRealHeight();
		double stackDepth = list.get(0).getStackSize();
		
		// creating a array that will contain the axe sizes in micrometers
		double axeSize[] = new double[3];

		//axe sizes put in an array
		axeSize[0] = stackWidth;
		axeSize[1] = stackHeight;
		axeSize[2] = stackDepth;
		
		// minimum and maximum C parameters accepted for a valid fitting
		double cMin, cMax;

		// getting the stack size;
		stackSize = list.get(0).getStackSize();
		
		// getting the total number of beads
		int total = list.size();
		
		int i = 0;
		
		// for each bead in the list
		for (BeadFrame bead : list) {
			
			// This is a democracy so the fitting is assumed
			// to be innocent (and valid) until the opposit has been proved.
			isFittingValid = true;
			
			// c parameter
			double c;

			/* filtering R parameter */

			for (int axe = 0; axe != 3; axe++) {
				
				// for x and y, the threshold is 0.9
				if (axe <= 1) {
					if (bead.getFittingGoodness(axe) < 0.9) {
						isFittingValid = false;
					}
				}
				// but for z the threshold is 0.8
				else {
					if (bead.getFittingGoodness(axe) < 0.8) {
						isFittingValid = false;
					}
				}
			}

			
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
