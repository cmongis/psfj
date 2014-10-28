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

import knop.psfj.BeadImage;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadMap.
 */
public class BeadMap implements PsfJGraph {

	
	/** The image. */
	BeadImage image;
	
	
	/**
	 * Instantiates a new bead map.
	 *
	 * @param image the image
	 */
	public BeadMap(BeadImage image) {
		this.image = image;
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getGraph()
	 */
	@Override
	public ImageProcessor getGraph() {
		// TODO Auto-generated method stub
		return image.getMap();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getGraph(int)
	 */
	@Override
	public ImageProcessor getGraph(int normalized) {
		// TODO Auto-generated method stub
		return getGraph();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getTitle()
	 */
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Bead map of "+image.getImageName();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return TextUtils.readTextRessource(this, "/knop/psfj/graphics/BeadMap.html");
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getShortDescription()
	 */
	public String getShortDescription() {
		return "A map showing the detected beads";
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getSaveId()
	 */
	@Override
	public String getSaveId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getImageIcon()
	 */
	@Override
	public ImageIcon getImageIcon() {
		// TODO Auto-generated method stub
		return null;
	}

}
