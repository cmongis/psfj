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
package knop.psfj.utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageProcessorUtils.
 */
public class ImageProcessorUtils {

	
	/** The max projection. */
	public static int MAX_PROJECTION = ZProjector.MAX_METHOD;
	
	/** The avg projection. */
	public static int AVG_PROJECTION = ZProjector.AVG_METHOD;
	
	/**
	 * Project.
	 *
	 * @param ips the ips
	 * @param method the method
	 * @return the image processor
	 */
	public static ImageProcessor project(ImageProcessor[] ips, int method) {
		ZProjector projector = new ZProjector();
		ImageProcessor firstIp = ips[0];
		ImageStack stack = new ImageStack(firstIp.getWidth(),firstIp.getHeight());
		
		for(ImageProcessor ip : ips) {
			if(ip.getWidth() != firstIp.getWidth() || ip.getHeight() != firstIp.getHeight()) {
				System.err.println("ImageProcessorUtils.project() : ImageProcessor size different from the first. Skipped");
				continue;
			}
			stack.addSlice(ip);
		}
		
		projector.setImage(new ImagePlus("",stack));
		
		projector.setMethod(method);
		
		projector.doProjection();
		return projector.getProjection().getProcessor();
		
	}
	
	/**
	 * Show.
	 *
	 * @param ip the ip
	 */
	public static void show(ImageProcessor ip) {
		show(ip,"");
	}
	
	/**
	 * Show.
	 *
	 * @param ip the ip
	 * @param title the title
	 */
	public static void show(ImageProcessor ip, String title) {
		new ImagePlus(title,ip).show();
	}
	
	/**
	 * Copy roi.
	 *
	 * @param ip the ip
	 * @param r the r
	 * @return the image processor
	 */
	public static ImageProcessor copyRoi(ImageProcessor ip, Rectangle r) {

		// System.out.println("Copying a roi of "+r.toString());

		ImageProcessor result = ip.createProcessor(r.width, r.height);

		for (int x = r.x; x != r.x + r.width; x++) {
			for (int y = r.y; y != r.y + r.height; y++) {
				result.setColor(ip.getPixel(x, y));
				result.drawPixel(x - r.x, y - r.y);
			}
		}

		return result;
	}
	
	
	/**
	 * Gets the encoded png base64.
	 *
	 * @param ip the ip
	 * @return the encoded png base64
	 */
	public static String getEncodedPNGBase64(ImageProcessor ip) {
      try {

          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          ImageIO.write(ip.getBufferedImage(), "png", stream);

          String encodedImage = DatatypeConverter.printBase64Binary(stream.toByteArray());

          return encodedImage;

      } catch (IOException ex) {
          
      }
      return "";
  }
	
	/**
	 * Crop.
	 *
	 * @param stack the stack
	 * @param roi the roi
	 * @return the image stack
	 */
	public static ImageStack crop(ImageStack stack, Rectangle roi) {
		
		ImageStack result = null;
		
		for(int i = 0;i!= stack.getSize();i++) {
			ImageProcessor ip = stack.getProcessor(i+1);
			ip.setRoi(roi);
			ip = ip.crop();
			if(result == null) {
				result = new ImageStack(ip.getWidth(),ip.getHeight());
			}
			
			result.addSlice(ip);
			
		}
		return result;
	}
	
	/**
	 * Scale.
	 *
	 * @param stack the stack
	 * @param width the width
	 * @return the image stack
	 */
	public static ImageStack scale(ImageStack stack, int width) {
		return scale(stack,width,-1,false);
	}
	
	/**
	 * Scale.
	 *
	 * @param stack the stack
	 * @param width the width
	 * @param interpolate the interpolate
	 * @return the image stack
	 */
	public static ImageStack scale(ImageStack stack, int width, boolean interpolate) {
		return scale(stack,width,-1,interpolate);
	}
	
	/**
	 * Scale.
	 *
	 * @param stack the stack
	 * @param width the width
	 * @param height the height
	 * @param interpolate the interpolate
	 * @return the image stack
	 */
	public static ImageStack scale(ImageStack stack, int width, int height, boolean interpolate) {
		ImageStack result = null;
		
		for(int i = 0;i!= stack.getSize();i++) {
			ImageProcessor ip = stack.getProcessor(i+1);
			if(height > 0)
				ip = ip.resize(width,height,interpolate);
			else 
				ip = ip.resize(width,height * width/ip.getWidth(),interpolate);
			
			if(result == null) {
				result = new ImageStack(ip.getWidth(),ip.getHeight());
			}
			
			result.addSlice(ip);
			
		}
		return result;
	}
	
}
