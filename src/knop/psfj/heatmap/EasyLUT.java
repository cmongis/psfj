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
package knop.psfj.heatmap;

import ij.IJ;
import ij.plugin.LutLoader;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class EasyLUT.
 * 
 * EasyLUT allows you to easily manage ImageJ luts from your Eclipse Project. The luts must be located in your sr/lut/ folder.
 * When exporting your project into JAR, EasyLUT will automatically load them from the JAR. 
 * 
 * 
 * Usage :
 * 	ImageProcessor ip = IJ.openImage("/home/you/test/image.tif").getProcessor();
 *		ip.setLut(EasyLUT.get("myLut.lut", ip.getMin(), ip.getMax()));
 * 
 * Copyright 2013, Cyril MONGIS
 * 
 * 
 */
public class EasyLUT {
	
	/** The name. */
	String name;
	
	/** The color model. */
	IndexColorModel colorModel;
	
	
	
	/**
	 * The main method. Gives you an overview of the usage
	 *
	 * @param arg the arguments
	 */
	public static void main(String[] arg) {
		ImageProcessor ip = IJ.openImage("/home/you/test/image.tif").getProcessor();
		ip.setLut(EasyLUT.get("myLut.lut", ip.getMin(), ip.getMax()));
	}
	
	
	
	/**
	 * Instantiates a new easy lut.
	 */
	public EasyLUT() {
		
	}
	
	/**
	 * Sets the color model.
	 *
	 * @param cm the new color model
	 */
	public void setColorModel(IndexColorModel cm) {
		colorModel = cm;
	}
	
	/**
	 * Instantiates a new easy lut.
	 *
	 * @param name the LUT (The lut should be located in the src/lut folder of the project
	 */
	public EasyLUT(String name) {
		this.name = name;
	}

	/**
	 * Gets the color model.
	 *
	 * @return the color model
	 */
	public IndexColorModel getColorModel() {
		if(colorModel != null) return colorModel;
		
		if(name == null) return null;

		
		try {
			colorModel = LutLoader.open(getClass().getResourceAsStream("/lut/"+name+".lut"));
			
		}
		catch(Exception e) {
			try {
				colorModel = LutLoader.open("src/lut/"+name+".lut");
				e.printStackTrace();
			}
			catch(IOException e2) {
				System.err.println("\n\nImpossible to load "+name+".lut");
				e2.printStackTrace();
				
				
				
			}
		}
		return colorModel;
	}
	
	/**
	 * Gets the lut.
	 *
	 * @param min the value used lowest colors
	 * @param max the value used for the highest colors
	 * @return the lut in ImageJ format
	 */
	public LUT getLUT(double min, double max) {
		return new LUT(getColorModel(),min,max);
	}
	
	/**
	 * Gets the buffered image.
	 *
	 * @return the buffered image
	 */
	public BufferedImage getBufferedImage() {
		return getImageFromLUT(getLUT(0,255));
	}
	
	
	/**
	 * Gets the.
	 *
	 * @param name the name
	 * @param min the min
	 * @param max the max
	 * @return the lut
	 */
	public static LUT get(String name, double min, double max) {
		return new EasyLUT(name).getLUT(min, max);
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	
	/**
	 * Gets the image from lut.
	 *
	 * @param lut the lut
	 * @return the image from lut
	 */
	public static BufferedImage getImageFromLUT(LUT lut) {
		
		int width = 100;
		int height = 20;
		
		ByteProcessor ip = new ByteProcessor(width,height);
		ip.setLut(lut);
		for(int color =  0; color !=256 ; color++ ) {
			int x = color*width /255;
			int y = 0;
			
			int w = 1;
			int h = height;
			ip.setColor(color);
			ip.drawRect(x, y, w, h);
		}
		return ip.getBufferedImage();
	}
	
	/**
	 * Equals.
	 *
	 * @param k the k
	 * @return true, if successful
	 */
	public boolean equals(EasyLUT k) {
		return this.getName().equals(k.getName());
	}
	
	
	
	
	
}
