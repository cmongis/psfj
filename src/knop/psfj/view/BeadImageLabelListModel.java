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

import javax.swing.DefaultListModel;

import knop.psfj.BeadImageManager;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadImageLabelListModel.
 */
public class BeadImageLabelListModel extends DefaultListModel<BeadImageLabel> {
	
	/** The manager. */
	BeadImageManager manager;
	
	/**
	 * Sets the manager.
	 *
	 * @param manager the new manager
	 */
	public void setManager(BeadImageManager manager) {
		this.manager = manager;
	}
	
	
	/** The display bead number. */
	boolean displayBeadNumber;
	
	/** The display infos. */
	boolean displayInfos;
	
	/** The display image. */
	boolean displayImage;
	
	/**
	 * Update list from manager.
	 */
	public void updateListFromManager() {
		if (manager == null)
			return;
		int beadImageCount = manager.getBeadImageList().size();
		
		int elementList = getSize();
		
		
		
		
		while (getSize() > beadImageCount) {
			System.out.println("Trying to remove" + (getSize()-1));
			removeElement(getElementAt(getSize()-1));
			
			System.out.println(getSize());
		}


		for (int i = 0; i != beadImageCount; i++) {

			if (i >= elementList) {
				BeadImageLabel label = new BeadImageLabel(manager.getBeadImage(i));
				label.setDisplayBeadNumber(displayBeadNumber);
				label.setDisplayImage(displayImage);
				label.setDisplayImageInfos(displayInfos);
				
				addElement(label);
				System.out.println("Adding new bead image label");
			} else {
				
				get(i).setBeadImage(manager.getBeadImage(i));
			}
		}
	}

	/**
	 * Checks if is display bead number.
	 *
	 * @return true, if is display bead number
	 */
	public boolean isDisplayBeadNumber() {
		return displayBeadNumber;
	}

	/**
	 * Sets the display bead number.
	 *
	 * @param displayBeadNumber the new display bead number
	 */
	public void setDisplayBeadNumber(boolean displayBeadNumber) {
		this.displayBeadNumber = displayBeadNumber;
	}

	/**
	 * Checks if is display infos.
	 *
	 * @return true, if is display infos
	 */
	public boolean isDisplayInfos() {
		return displayInfos;
	}

	/**
	 * Sets the display infos.
	 *
	 * @param displayInfos the new display infos
	 */
	public void setDisplayInfos(boolean displayInfos) {
		this.displayInfos = displayInfos;
	}

	/**
	 * Checks if is display image.
	 *
	 * @return true, if is display image
	 */
	public boolean isDisplayImage() {
		return displayImage;
	}

	/**
	 * Sets the display image.
	 *
	 * @param displayImage the new display image
	 */
	public void setDisplayImage(boolean displayImage) {
		this.displayImage = displayImage;
	}
	
	
}
