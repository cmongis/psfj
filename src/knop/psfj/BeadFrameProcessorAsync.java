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
		
		init();
		
		if(list.size()== 0) return;
		
		currentBeadImage = list.get(0).getSource();
		
		int numCore = Runtime.getRuntime().availableProcessors();
		garbageCollectionFrequence = numCore * 10;
		ExecutorService executor = Executors.newFixedThreadPool(numCore);
		//ExecutorService executor = new ThreadPoolExecutor(numCore, 1000000, 1, TimeUnit.MINUTES, )
		
		
		for(BeadFrame frame : list) {
			frame.addObserver(this);
			executor.execute(frame.getFindPSFJob());
			
		}
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.MINUTES);
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(BeadFrame frame : list) {
			frame.deleteObserver(this);
		}
		
		currentBeadImage.setProgress(100);
		
		
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrameProcessor#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable origin, Object o) {
		try {
			Message message = (Message)o;
			if(message.getName().contains("psf")) {
				BeadFrame frame = (BeadFrame)origin;
				origin.deleteObservers();
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
