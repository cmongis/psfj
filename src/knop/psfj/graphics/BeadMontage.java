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

import knop.psfj.BeadImageManager;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadMontage.
 */
public class BeadMontage implements PsfJGraph {

	
	/** The manager. */
	BeadImageManager manager;
	
	/**
	 * Instantiates a new bead montage.
	 *
	 * @param manager the manager
	 */
	public BeadMontage(BeadImageManager manager) {
		this.manager = manager;
	}
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getGraph()
	 */
	@Override
	public ImageProcessor getGraph() {
		// TODO Auto-generated method stub
		return manager.getBeadImage(0).getMontage();
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
		return "Bead montage";
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return TextUtils.readTextRessource(this,"/knop/psfj/graphics/BeadMontage.html");
	}

	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getSaveId()
	 */
	@Override
	public String getSaveId() {
		// TODO Auto-generated method stub
		return String.format("%s_%s_bead_montage",
				manager.getBeadImage(0).getImageNameWithoutExtension(),
				manager.getBeadImage(1).getImageNameWithoutExtension());
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getShortDescription()
	 */
	public String getShortDescription() {
		return "Bead samples taken from each corner of the FOV";
	}


	/* (non-Javadoc)
	 * @see knop.psfj.graphics.PsfJGraph#getImageIcon()
	 */
	@Override
	public ImageIcon getImageIcon() {
		return null; //new ImageIcon();
		// TODO Auto-generated method stub
		//return null;
	}

}
