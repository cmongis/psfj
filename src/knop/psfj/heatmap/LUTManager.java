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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;

// TODO: Auto-generated Javadoc
/**
 * The Class LUTManager.
 */
public class LUTManager extends ArrayList<EasyLUT> implements ListCellRenderer<Object>, ComboBoxModel<EasyLUT> {
	
	
	/** The selected item. */
	String selectedItem;

	
	/**
	 * Gets the LUT list.
	 *
	 * @return the LUT list
	 */
	public ArrayList<String> getLUTList() {
		ArrayList<String> lutList = new ArrayList<String>();
		
		for(EasyLUT lut : this) {
			lutList.add(lut.getName());
		}
		
		return lutList;
		
	}
	
	
	
	
	/**
	 * Adds the.
	 *
	 * @param name the name
	 * @return the easy lut
	 */
	public EasyLUT add(String name) {
		EasyLUT lut = new EasyLUT(name);
		add(lut);
		return lut;
	}

	/**
	 * Gets the.
	 *
	 * @param name the name
	 * @return the easy lut
	 */
	public EasyLUT get(String name) {
		for(EasyLUT lut : this) {
			if(lut.getName() == name) return lut;
		}
		//if not lut from this name exist, let's return a new one.
		return add(name);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
		
		
	
		Canvas canvas = new Canvas();
		BufferedImage image = ((EasyLUT)value).getBufferedImage();
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		picLabel.setBorder(new EmptyBorder(2,2,2,2));
		
		if(isSelected || cellHasFocus) {
			picLabel.setBackground(Color.blue);
		}
		
		return picLabel;
	}




	/* (non-Javadoc)
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}




	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public EasyLUT getElementAt(int index) {
		// TODO Auto-generated method stub
		return get(index);
	}




	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return size();
	}




	/* (non-Javadoc)
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}




	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		// TODO Auto-generated method stub
		return get(selectedItem);
	}




	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object arg0) {
		// TODO Auto-generated method stub
		EasyLUT lut = (EasyLUT) arg0;
		selectedItem = lut.getName();
	}









	





	
}
