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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import knop.psfj.BeadImage;

import org.swixml.XVBox;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class WaveLengthTextField.
 */
public class WaveLengthTextField extends JTextField {
	
	
	
	/** The bead image. */
	BeadImage beadImage;
	
	
	JLabel iconLabel;
	
	
	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String arg[]) {
		JFrame frame = new JFrame("hey");
		frame.setSize(200, 200);
		XVBox box = new XVBox();
		box.add(new WaveLengthTextField());
		
		frame.add(box);
		frame.pack();
		frame.show();
	}
	
	
	/**
	 * Instantiates a new wave length text field.
	 */
	public WaveLengthTextField() {
		super(6);
		
		//setBorder(new TitledBorder("wavelengthbox"));
		//setLayout(new FlowLayout());
		//setLayout();
		
		FormLayout layout = new FormLayout("right:pref, 5dlu, pref, 5dlu, left:pref","p");
		setLayout(layout);
		CellConstraints c = new CellConstraints();
		
		addKeyListener(onTextChanged);
		setVisible(true);
	}
	
	/**
	 * Instantiates a new wave length text field.
	 *
	 * @param b the b
	 */
	public WaveLengthTextField(BeadImage b) {
		this();
		
		
		
		setBeadImage(b);
		//setPreferredSize(new Dimension(30,30));
	}

	/**
	 * Gets the bead image.
	 *
	 * @return the bead image
	 */
	public BeadImage getBeadImage() {
		return beadImage;
	}

	/**
	 * Sets the bead image.
	 *
	 * @param beadImage the new bead image
	 */
	public void setBeadImage(BeadImage beadImage) {
		this.beadImage = beadImage;
		
		setText(beadImage.getWaveLengthInNanoMeters());
		//setText("nm  : "+beadImage.getImageName());
	}
	

	
	/**
	 * Checks if is valid.
	 *
	 * @param text the text
	 * @return true, if is valid
	 */
	public boolean isValid(String text) {
		return true;
	}
	
	/**
	 * Update bead image.
	 */
	public void updateBeadImage() {
		if(beadImage == null) return;
		//System.out.println("Something happend");
		
		if(isValid(getText())) {
			//System.out.println("changing to "+getText());
			beadImage.setWaveLengthInNanoMeters(getText());
			beadImage.getMicroscope().save();
			updateIconLabel();
		}
	}
	
	
	
	
	/** The on text changed. */
	protected KeyListener onTextChanged = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			updateBeadImage();
		}
		
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void updateIconLabel() {
		if(iconLabel == null) return;
		if(beadImage.getMicroscope().getWaveLength() == 0.0) return;
		iconLabel.setIcon(beadImage.getIcon(16));
		
		
	}
	
	public void setIconLabel(JLabel label) {
		iconLabel = label;
	}
	
}
