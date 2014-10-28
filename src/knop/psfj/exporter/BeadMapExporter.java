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

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

import knop.psfj.BeadFrame;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadMapExporter.
 */
public class BeadMapExporter {
	
	/** The manager. */
	BeadImageManager manager;
	
	/**
	 * Instantiates a new bead map exporter.
	 *
	 * @param manager the manager
	 */
	public BeadMapExporter(BeadImageManager manager) {
		setManager(manager);
	}
	
	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	public BeadImageManager getManager() {
		return manager;
	}
	
	/**
	 * Sets the manager.
	 *
	 * @param manager the new manager
	 */
	public void setManager(BeadImageManager manager) {
		this.manager = manager;
	}
	
	
	/**
	 * Gets the bead map.
	 *
	 * @param i the i
	 * @return the bead map
	 */
	public ImageProcessor getBeadMap(int i) {
		
		BeadImage image = getManager().getBeadImage(i);
		
		BeadFrameList frameList = image.getBeadFrameList();
		
		ColorProcessor ip = (ColorProcessor) image.getMiddleImage().convertToRGB();
		
		for(BeadFrame frame : frameList) {
			Color c;
			if(frame.isValid()) {
				c = Color.green;
			}
			else {
				c = Color.red;
			}
			ip.setColor(c);
			ip.drawString(""+frame.getId(),frame.getFrameX(),frame.getFrameY());
			ip.drawRect(frame.getFrameX(), frame.getFrameY(), frame.getWidth(), frame.getHeight());
			ip.drawOval(frame.getFrameCenterX()-1, frame.getFrameCenterY()-1, 3, 3);	
		}
		return ip;
	}
	
	/**
	 * Export.
	 */
	public void export() {
		
		
		
		
		
		
	}
	
}
