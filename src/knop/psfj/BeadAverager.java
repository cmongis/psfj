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

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Observable;

import knop.psfj.utils.ImageProcessorUtils;
import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadAverager.
 */
public class BeadAverager extends Observable{
	
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		
		BeadImage image = new BeadImage("/home/cyril/test_img/6_small.tif");
		
		//BeadImage  image = new BeadImage("/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/different sigma_no asymmetry_integer centers_2048x2048/sigma_y=4.0_asymmetry=1.0_theta=0.000.tif");
		image.workFromMemory();
		image.autoThreshold();
		//image.setFocusPlane(0);
		image.setFrameSize(20);
		BeadFrameProcessorAsync processor = new BeadFrameProcessorAsync(image.getBeadFrameList());
		processor.process();
		processor.filter();
		BeadFrameList frameList = image.getBeadFrameList().getOnlyValidBeads();
		
		BeadAverager averager = new BeadAverager();
		averager.averageBead(frameList).show();
		
		
	}
	
	/** The filtered out beads. */
	int filteredOutBeads = 0;
	
	/** The total beads. */
	int totalBeads = 0;
	
	
	/**
	 * Average bead.
	 *
	 * @param frameList the frame list
	 * @return the image plus
	 */
	public ImagePlus averageBead(ArrayList<BeadFrame> frameList) {
		
		ImagePlus result;
		DescriptiveStatistics centerX = new DescriptiveStatistics();
		DescriptiveStatistics centerY = new DescriptiveStatistics();
		DescriptiveStatistics centerZ = new DescriptiveStatistics();
		
		
		int originalStackSize = frameList.get(0).getSliceNumber();
		
		setTotalBeads(frameList.size());
		
		//frameList = filter(frameList);
		
		ArrayList<ImageStack> centeredStacks = new ArrayList<ImageStack>();
		
		
		
		for(BeadFrame frame : frameList) {
			
			System.out.println(frame.getCentroidXAsInt());
			centerX.addValue(frame.getCentroidXAsInt());
			centerY.addValue(frame.getCentroidYAsInt());
			centerZ.addValue(frame.getCentroidZAsInt());
			
			
			
		}
		
		int chosenCenter = MathUtils.round(centerZ.getPercentile(50));
		int maxUp = chosenCenter-MathUtils.round(chosenCenter-centerZ.getMin());
		int maxDown = originalStackSize - MathUtils.round(centerZ.getMax());
		
		System.out.println(maxUp);
		System.out.println(maxDown);
		
		
		
		for(BeadFrame frame : frameList) {
			ImageStack subStack = new ImageStack(frame.getSubstack().getWidth(),frame.getSubstack().getHeight());
			int center = frame.getCentroidZAsInt();
			int begin = center-maxUp;
			int end = center+maxDown;
			System.out.println(String.format(" Groing from %d to %d with a center at %d",begin,end,center));
			for(int i = begin;i!=end;i++) {
				subStack.addSlice(frame.getSubstack().getImageStack().getProcessor(i+1));
			}
			
			centeredStacks.add(subStack);
			//new ImagePlus("",subStack).show();
			
		}
		
		
		System.out.println(centerX);
		System.out.println(centerY);
		System.out.println(centerZ);
		
		
		return new ImagePlus("",averageStacks(centeredStacks));
		
		
	}

	/**
	 * Average stacks.
	 *
	 * @param centeredStacks the centered stacks
	 * @return the image stack
	 */
	public ImageStack averageStacks(ArrayList<ImageStack> centeredStacks) {
		
		
		int substackWidth = centeredStacks.get(0).getWidth();
		int substackHeight = centeredStacks.get(0).getHeight();
		int substackSize = centeredStacks.get(0).getSize();
		
		ImageStack averagedStack = new ImageStack(substackWidth,substackHeight);
		
		
		
		for(int i = 0; i != substackSize;i++) {
			int substackNumber = centeredStacks.size();
			ImageProcessor[] ips = new ImageProcessor[substackNumber];
			for(int j = 0; j!= substackNumber;j++) {
				ips[j] = centeredStacks.get(j).getProcessor(i+1);
			}
			averagedStack.addSlice(ImageProcessorUtils.project(ips, ZProjector.AVG_METHOD));
			
		}
		
		
		
		return averagedStack;
		
	}
	
	
	/**
	 * Filter.
	 *
	 * @param frameList the frame list
	 * @return the array list
	 */
	public ArrayList<BeadFrame> filter(ArrayList<BeadFrame> frameList) {
		
		ArrayList<BeadFrame> filtered = new ArrayList<BeadFrame>();
		
		DescriptiveStatistics centerX = new DescriptiveStatistics();
		DescriptiveStatistics centerY = new DescriptiveStatistics();
		DescriptiveStatistics centerZ = new DescriptiveStatistics();
		
		
		
		for(BeadFrame frame : frameList) {
			centerX.addValue(frame.getCentroidX());
			centerY.addValue(frame.getCentroidY());
			centerZ.addValue(frame.getCentroidZ());
		}
		
		double thresholdUp = centerZ.getPercentile(50) + centerZ.getStandardDeviation();
		double thresholdDown = thresholdUp - (centerZ.getStandardDeviation()*2);
		for(BeadFrame frame : frameList) {
			if(frame.getCentroidZ() < thresholdUp && frame.getCentroidZ() > thresholdDown) {
				filtered.add(frame);
			}
			else {
				incrementFilteredOutBeadCount();
			}
		}
		return filtered;
	}

	/**
	 * Gets the total beads.
	 *
	 * @return the total beads
	 */
	public int getTotalBeads() {
		return totalBeads;
	}

	/**
	 * Sets the total beads.
	 *
	 * @param totalBeads the new total beads
	 */
	public void setTotalBeads(int totalBeads) {
		this.totalBeads = totalBeads;
	}

	/**
	 * Gets the filtered out beads.
	 *
	 * @return the filtered out beads
	 */
	public int getFilteredOutBeads() {
		return filteredOutBeads;
	}

	/**
	 * Sets the filtered out beads.
	 *
	 * @param filteredOutBeads the new filtered out beads
	 */
	public void setFilteredOutBeads(int filteredOutBeads) {
		this.filteredOutBeads = filteredOutBeads;
	}
	
	/**
	 * Increment filtered out bead count.
	 */
	public void incrementFilteredOutBeadCount() {
		System.out.println(filteredOutBeads + " beads filtered out.");
		this.filteredOutBeads++;
	}
	
	/**
	 * Update progress.
	 *
	 * @param i the i
	 * @param total the total
	 */
	public void updateProgress(int i, int total) {
		
		setChanged();
		notifyObservers(new Message(this,"progress changed","Averaging beads...",i * 100 / total));
		
		
	}
	
	
	
}
