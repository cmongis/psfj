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
package knop.psfj.graphics;

import ij.process.ImageProcessor;

import javax.swing.ImageIcon;

// TODO: Auto-generated Javadoc
/**
 * The Interface PsfJGraph.
 */
public interface PsfJGraph {
	
	/** The normalized. */
	public static int NORMALIZED = 1;
	
	/** The not normalized. */
	public static int NOT_NORMALIZED = 2;
	
	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	public ImageProcessor getGraph();
	
	/**
	 * Gets the graph.
	 *
	 * @param normalized the normalized
	 * @return the graph
	 */
	public ImageProcessor getGraph(int normalized);
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle();
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * Gets the save id.
	 *
	 * @return the save id
	 */
	public String getSaveId();
	
	/**
	 * Gets the short description.
	 *
	 * @return the short description
	 */
	public String getShortDescription();
	
	/**
	 * Gets the image icon.
	 *
	 * @return the image icon
	 */
	public ImageIcon getImageIcon();
	
}
