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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import knop.psfj.view.Message;



/**
 * The Class BeadFrameProcessorAsync.
 * This class distribute PSF fitting through different threads, raising the calculation speed on multi-core systems.
 */
public class BeadFrameProcessorAsync extends BeadFrameProcessor{


	/** The thread number. */
	int threadNumber = Runtime.getRuntime().availableProcessors();

	

	
	/**
	 * Instantiates a new bead frame processor async.
	 *
	 * @param list the list
	 */
	public BeadFrameProcessorAsync(BeadFrameList list) {
		super(list);
	}

	
	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrameProcessor#process()
	 */
	public void process() {
		
		//
		init();
		
		if(list.size()== 0) return;
		
		currentBeadImage = list.get(0).getSource();
		
		// setting the number of cores
		int numCore = Runtime.getRuntime().availableProcessors();
		
		// collecting the garbage every...
		garbageCollectionFrequence = numCore * 10;
		
		// thread pool that will be used to hold the PSF calcultions
		ExecutorService executor = Executors.newFixedThreadPool(numCore);
	
		
		// for each frame of the list
		for(BeadFrame frame : list) {
			
			// setting this class as observer
			frame.addObserver(this);
			
			// adding the calcultion job to the pool
			executor.execute(frame.getFindPSFJob());
			
		}
		
		// wait for all calculation to be over
		executor.shutdown();
		
		// we are ready to wait 30 minutes
		try {
			executor.awaitTermination(30, TimeUnit.MINUTES);
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// removing this class as observer of each bead frame
		for(BeadFrame frame : list) {
			frame.deleteObserver(this);
		}
		
		// setting the progress of the bead image as 100%
		currentBeadImage.setProgress(100);
		
		
	}
	
	
	
	
	/* 
	 * Called when a bead finishes the fitting
	 */
	public void update(Observable origin, Object o) {
		try {
			Message message = (Message)o;
			// if the message contains psf, then the bead
			if(message.getName().contains("psf")) {
				BeadFrame frame = (BeadFrame)origin;
				origin.deleteObservers();
				
				// incrementing the number of completed frames
				frameCompleted++;
				refresh();

			}
		}	
			
		catch(Exception e) {
			System.err.println("Couln't interpret message from bead frames");
		}
	}


	/**
	 * Gets the thread number.
	 *
	 * @return the thread number
	 */
	public int getThreadNumber() {
		return threadNumber;
	}


	/**
	 * Sets the thread number.
	 *
	 * @param threadNumber the new thread number
	 */
	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	
}
