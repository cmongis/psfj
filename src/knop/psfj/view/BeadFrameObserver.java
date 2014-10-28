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

import java.awt.Point;
import java.awt.Rectangle;

import knop.psfj.BeadFrame;
import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications
 * about BeadFrame information as the BeadFrame is constructed.
 */
public class BeadFrameObserver {
	 
 	/** The frame. */
 	BeadFrame frame;

	 /** The is selected. */
 	boolean isSelected = false;
	 
	 
	 /**
 	 * This method is called when information about an BeadFrame
 	 * which was previously requested using an asynchronous
 	 * interface becomes available.
 	 *
 	 * @param frame the frame
 	 */
 	public BeadFrameObserver(BeadFrame frame) {
		 setFrame(frame);
	 }
	 
	/**
	 * This method is called when information about an BeadFrame
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @return the frame
	 */
	public BeadFrame getFrame() {
		return frame;
	}

	/**
	 * This method is called when information about an BeadFrame
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param frame the frame
	 */
	public void setFrame(BeadFrame frame) {
		this.frame = frame;
	}
	 
	
	/**
	 * This method is called when information about an BeadFrame
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @return true, if checks if is valid
	 */
	public boolean isValid() {
		return frame.isValid();
	}
	 
	/**
	 * This method is called when information about an BeadFrame
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param p the p
	 * @return true, if checks if is point in
	 */
	public boolean isPointIn(Point p) {
		return frame.getBoundaries().contains(p.getX(), p.getY());
	}
	
  /**
   * This method is called when information about an BeadFrame
   * which was previously requested using an asynchronous
   * interface becomes available.
   *
   * @param r the r
   * @return true, if checks if is in
   */
  public boolean isIn(Rectangle r) {
	  Rectangle beadImageBoundaries = frame.getBoundariesAsRectangle();
	  Point p = new Point(MathUtils.round(beadImageBoundaries.getCenterX()),MathUtils.round(beadImageBoundaries.getCenterY()));
	  return r.contains(p);
  }

  /**
   * This method is called when information about an BeadFrame
   * which was previously requested using an asynchronous
   * interface becomes available.
   *
   * @param s the s
   */
  public void setSelected(boolean s) {
	  isSelected = s;
  }
  
}
