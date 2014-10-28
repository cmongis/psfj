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

import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import knop.psfj.BeadImage;
import knop.psfj.utils.MathUtils;

import org.swixml.SwingEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadImageLabel.
 */
public class BeadImageLabel  implements  Observer {


	/** The canvas. */
	KnopImageCanvas canvas;
	
	/** The engine. */
	SwingEngine engine;

	/** The panel. */
	JPanel panel;
	
	/** The preview container. */
	JPanel previewContainer;
	
	/** The title label. */
	JLabel titleLabel;
	
	/** The status label. */
	JLabel statusLabel;
	
	/** The other label. */
	JLabel otherLabel;
	
	/** The progressbar. */
	JProgressBar progressbar;
	
	/** The delete button. */
	JButton deleteButton;

	/** The bead image. */
	BeadImage beadImage;

	/** The can be deleted. */
	boolean canBeDeleted;


	/** The display image infos. */
	boolean displayImageInfos = true;
	

	/** The display bead number. */
	boolean displayBeadNumber = false;
	
	/** The display image. */
	boolean displayImage = false;
	
	/** The show wave length input. */
	boolean showWaveLengthInput = false;
	
	
	
	
	/**
	 * Instantiates a new bead image label.
	 */
	public BeadImageLabel() {
		
		engine = new SwingEngine(this);
		try {
			UIManager.setLookAndFeel(UIManager
					.getSystemLookAndFeelClassName());
			engine.render("knop/psfj/view/BeadImage.xml").setVisible(
					true);
			
			
			panel.setPreferredSize(new Dimension(270,90));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Instantiates a new bead image label.
	 *
	 * @param image the image
	 */
	public BeadImageLabel(BeadImage image){
		this();
		setBeadImage(image);
	}
	
	/**
	 * Sets the delete button.
	 *
	 * @param delete the new delete button
	 */
	public void setDeleteButton(boolean delete) {
		canBeDeleted = delete;
		deleteButton.setVisible(delete);
	}

	/**
	 * Can be deleted.
	 *
	 * @return true, if successful
	 */
	public boolean canBeDeleted() {
		return canBeDeleted;
	}

	/**
	 * Update status.
	 */
	public void updateStatus() {
		try {
			
			titleLabel.setText(beadImage.getImageName());
			statusLabel.setText(beadImage.getStatus());
			
			String info = "";
			
			if(displayImageInfos) {
				info += beadImage.getImageDiskSize() + "MB.   ";
			}
			
			if(displayBeadNumber) {
				if(beadImage.getFrameNumber() > 0) {
					info += String.format("%d beads detected, %d ignored.",beadImage.getFrameNumber()+beadImage.getIgnoredFrameNumber(),beadImage.getIgnoredFrameNumber());
				}
				else if(beadImage.getThresholdValue() <= 0) {
					info += "";
				}
				else {
					info += "No bead found.";
				}
			}
			
			
			
			if(info.equals("")) {
				otherLabel.setVisible(false);
			}
			else {
				otherLabel.setVisible(true);
				otherLabel.setText(info);
			}
			int x = beadImage.getProgress();
			progressbar.setValue(MathUtils.round(0.01*Math.pow(x,2))+1);
			//progressbar.setValue(x);
			panel.repaint();
		}
		
		catch(Exception e) {
			System.err.println("Error when refreshing View");
		}
	}
	
	/**
	 * Show wave length input.
	 *
	 * @param show the show
	 */
	public void showWaveLengthInput(boolean show) {
		showWaveLengthInput = show;
	}
	
	
	/**
	 * Sets the selected.
	 *
	 * @param isSelected the new selected
	 */
	public void setSelected(boolean isSelected) {
		if(panel == null) getView();
		
		Color color;
		if(isSelected) {
			color = new Color(200,200,255);
		}
		else {
			color = Color.WHITE;
		}
		
		panel.setBackground(color);
		
		
	}
	
	/** The ask for deletion. */
	Action askForDeletion = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
						updateStatus();
						beadImage.askForDeletion();
			
		}
	};

	

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		Message message = (Message) arg;
		if(message.getName().contains("preview")) {
			updatePreview(message);
		}
		
		updateStatus();
	}

	/**
	 * Update preview.
	 *
	 * @param message the message
	 */
	private void updatePreview(Message message) {
		if(displayImage) {
			if(message.getData() != null)
			updatePreview((ImageProcessor)message.getData());
		}
		
	}

	/**
	 * Update preview.
	 *
	 * @param ip the ip
	 */
	public synchronized void updatePreview(ImageProcessor ip) {
		
		
		if(canvas == null) {
			canvas = new KnopImageCanvas(100,100);
			previewContainer.add(canvas,BorderLayout.CENTER);
		}
		if(ip != null) {
			
			canvas.setResetOnImageChange(true);
			canvas.setImage(beadImage.getRawPreview());
			canvas.fitImageToCanvas();
			canvas.repaint();
		}
		panel.repaint();
	}
	
	/**
	 * Gets the view.
	 *
	 * @return the view
	 */
	public Component getView() {
		return panel;
	}

	/**
	 * Delete observer.
	 */
	public void deleteObserver() {
		if(beadImage != null) {
			System.out.println("Deleting observer.");
			beadImage.deleteObserver(this);
			
		}
	}
	
	/**
	 * Sets the bead image.
	 *
	 * @param beadImage2 the new bead image
	 */
	public void setBeadImage(BeadImage beadImage2) {
		
		if(beadImage2 == null) return;
		if(beadImage == beadImage2) {
			updateStatus();
			return;
		}
		deleteObserver();
		System.out.println("Adding observer to bead image");
		beadImage = beadImage2;
		beadImage2.addObserver(this);
		updateStatus();
	}

	
	/**
	 * Sets the display image.
	 *
	 * @param displayImage the new display image
	 */
	public void setDisplayImage(boolean displayImage) {
		this.displayImage = displayImage;
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
	 * Checks if is display image infos.
	 *
	 * @return true, if is display image infos
	 */
	public boolean isDisplayImageInfos() {
		return displayImageInfos;
	}

	/**
	 * Sets the display image infos.
	 *
	 * @param displayImageInfos the new display image infos
	 */
	public void setDisplayImageInfos(boolean displayImageInfos) {
		this.displayImageInfos = displayImageInfos;
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
	 * Gets the renderer.
	 *
	 * @return the renderer
	 */
	public static ListCellRenderer<BeadImageLabel> getRenderer() {
		return new ListCellRenderer<BeadImageLabel>() {

			@Override
			public Component getListCellRendererComponent(
					JList<? extends BeadImageLabel> list, BeadImageLabel value, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				
					value.setSelected(isSelected);
					return value.getView();
				
				
			}
		};
	}
	
}
