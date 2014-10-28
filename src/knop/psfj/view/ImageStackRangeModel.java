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
package knop.psfj.view;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.LUT;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import knop.psfj.BeadImage;
import knop.psfj.heatmap.EasyLUT;
import knop.psfj.utils.MathUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import prefuse.util.ui.JRangeSlider;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageStackRangeModel.
 */
public class ImageStackRangeModel extends DefaultBoundedRangeModel implements ChangeListener {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The stack. */
	ImagePlus stack;
	
	/**
	 * Instantiates a new image stack range model.
	 *
	 * @param substack the substack
	 */
	
	int maximumFromStack = NOT_SET;
	
	public static final int NOT_SET = -1;
	
	public ImageStackRangeModel(BeadImage image) {
		
		super();
		setImagePlus(new ImagePlus("",image.getStack()));
		
	
		maximumFromStack = image.getMaxaximumIntensityOfTheWholeStack();
		
		
		updateRange();
		System.out.printf("updating range %d - %d\n",getMinimumFromStack(),getMaximumFromStack());
		setValue(getMinimumFromStack());
		setExtent(getMaximumFromStack()-getMinimumFromStack());
		addChangeListener(this);
		
	}
	
	public ImageStackRangeModel(ImagePlus substack) {
		
		super();
		setImagePlus(substack);
		
	
		
		
		
		updateRange();
		System.out.printf("updating range %d - %d\n",getMinimumFromStack(),getMaximumFromStack());
		setValue(getMinimumFromStack());
		setExtent(getMaximumFromStack()-getMinimumFromStack());
		addChangeListener(this);
	}
	
	
	
	/**
	 * Sets the image plus.
	 *
	 * @param imp the new image plus
	 */
	public void setImagePlus(ImagePlus imp) {
		stack = imp;
		
	}
	
	/**
	 * Gets the range slider.
	 *
	 * @param e the e
	 * @return the range slider
	 */
	public JRangeSlider getRangeSlider(ChangeEvent e) {
		JRangeSlider slider = (JRangeSlider)e.getSource();
		return slider;
	}
	
	/**
	 * Update range.
	 */
	public void updateRange() {
		if(stack.getBitDepth() == 16) {
				setMinimum(0);
				setMaximum(maximumFromStack);
		}
		else {
			setMinimum(0);
			setMaximum(255);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		
		updateImageRange();
		
	}
	
	/**
	 * Gets the minimum from slider.
	 *
	 * @return the minimum from slider
	 */
	public int getMinimumFromSlider() {
		return getValue();
	}
	
	/**
	 * Gets the maximum from slider.
	 *
	 * @return the maximum from slider
	 */
	public int getMaximumFromSlider() {
		return getValue()+getExtent();
	}
	
	/**
	 * Gets the maximum from stack.
	 *
	 * @return the maximum from stack
	 */
	public int getMaximumFromStack() {
		
		
		/*
		if(maximumFromStack == NOT_SET) {
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for(int i = 0;i!=stack.getStackSize();i++) {
				stack.setSlice(i+1);
				stats.addValue(stack.getProcessor().getStatistics().max);
				
			}
			maximumFromStack = MathUtils.round(stats.getMax());
		}*/
		//stack.deleteRoi();
		return MathUtils.round(stack.getDisplayRangeMax());
		//return maximumFromStack;
	}
	
	/**
	 * Gets the minimum from stack.
	 *
	 * @return the minimum from stack
	 */
	public int getMinimumFromStack() {
		return MathUtils.round(stack.getDisplayRangeMin());
	}
	
	/**
	 * Update image range.
	 */
	public void updateImageRange() {
		System.out.printf("setting image range (%d - %d)\n",getMinimumFromSlider(),getMaximumFromSlider());
		LUT lut = EasyLUT.get("psfj_fwhm", getMinimumFromSlider(), getMaximumFromSlider());
		stack.setDisplayRange(getMinimumFromSlider(), getMaximumFromSlider());
		
		for(int i =0 ; i!= stack.getStackSize();i++) {
			ImageProcessor ip = stack.getStack().getProcessor(i+1);
		 ip.setColorModel(lut);
		 ip.setMinAndMax(getMinimumFromSlider(), getMaximumFromSlider());
			
			//ip.blurGaussian(2);
		//ip.setMinAndMax(getMinimumFromSlider(), getMaximumFromSlider());
		//ip.getLut().min = getMinimumFromSlider();
		//ip.getLut().max = getMaximumFromSlider();
		//ip.setColorModel(ip.getLut().getRGBdefault());
			//ip.setLut(lut);
		//ip.setLut(ip.getLut());
		//stack.getStack().getProcessor(i+1).updateComposite(arg0, arg1);
		}
		
		
	}
	
}
