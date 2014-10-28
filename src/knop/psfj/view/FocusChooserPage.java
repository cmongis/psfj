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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;

import org.swixml.XVBox;

// TODO: Auto-generated Javadoc
/**
 * The Class FocusChooserPage.
 */
public class FocusChooserPage extends WizardPage {

	/** The main panel. */
	XVBox mainPanel;

	/** The canvas. */
	KnopImageCanvas canvas;

	/** The focus slider. */
	JSlider focusSlider;

	/** The previous image button. */
	JButton previousImageButton;
	
	/** The next image button. */
	JButton nextImageButton;
	
	/** The second panel. */
	JPanel secondPanel;
	
	/** The focus slider label. */
	JLabel focusSliderLabel;

	/** The bead image list. */
	JList beadImageList;
	
	/** The list model. */
	DefaultListModel<BeadImage> listModel = new DefaultListModel<BeadImage>();
	
	/** The displayed bead image. */
	protected int displayedBeadImage = 0;

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String[] arg) {
		WizardWindow wizardWindow = new WizardWindow();
		final BeadImageManager manager = new BeadImageManager();

		manager.addObserver(wizardWindow);

		wizardWindow.addPage(new BeadImageLoaderPage(wizardWindow, manager));
		wizardWindow.addPage(new FocusChooserPage(manager));
		wizardWindow.addPage(new CalibrationPage(manager));
		wizardWindow.addPage(new ThresholdChooserPage(manager));
		wizardWindow.addPage(new ExportDataPage(manager));
		wizardWindow.addPage(new ProcessingPage(manager));
		wizardWindow.addPage(new HeatMapPage(manager));
		wizardWindow.setCurrentPage(0);

		new Thread() {
			public void run() {
				//manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
				//manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
				//manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
				// manager.add("/media/data/Knop/Patrick/100x_tirf_gfp.tif");
				//manager.add("/media/data/Knop/Patrick/water_gfp.tif");
				//manager.add("/media/data/Knop/Patrick/water_gfp_02.tif");
				 //manager.add("/media/data/Knop/Patrick/water_gfp_03.tif");
				//manager.add("/home/cyril/Bureau/100x_planapo_mc.tif");
			}
		}.start();

	}

	/**
	 * Instantiates a new focus chooser page.
	 *
	 * @param m the m
	 */
	public FocusChooserPage(BeadImageManager m) {
		super();
		setBeadImageManager(m);
		try {
			getSwingEngine().render("knop/psfj/view/FocusChooserPage.xml")
					.setVisible(true);

			focusSlider.addChangeListener(onAjustingSlider);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		focusSlider.setOrientation(SwingConstants.VERTICAL);
		focusSlider.setInverted(true);
		beadImageList.setCellRenderer(beadImageCellRenderer);
		beadImageList.setModel(listModel);
		beadImageList.addListSelectionListener(onListChange);
		setTitle("Step 2 : Selection of focal plane");
		setExplaination("The software attempts automatically to determine<br>the focal plane (the plane where most beads are in focus)." +
				"<br>If it fails, use the slider to choose the correct plane.");

	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isBackPossible()
	 */
	@Override
	public boolean isBackPossible() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isQuitOkay()
	 */
	@Override
	public boolean isQuitOkay() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {
		
		updateImage();
		updateSlider();
		updateList();
		beadImageList.setSelectedIndex(0);
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onResize(java.awt.Component)
	 */
	public void onResize(Component e) {
		System.out.println(canvas.getMaximumSize());
		canvas.setSize(canvas.getPreferredSize());

	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {
		updateImage();
		return mainPanel;

	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof Message)
			update((Message) arg);

	}

	/**
	 * Update.
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		if (message.getOrigin() instanceof BeadImage) {
			System.out.println(message);
			if (message.getData() != null) {
				try {
					System.out.println(getCurrentBeadImage() == message.getOrigin());
					if (getCurrentBeadImage() ==  message
							.getOrigin())
						setCurrentProcessor((ImageProcessor) message.getData());
						return;
				} catch (Exception e) {
					System.err.println("Problem when refreshing preview");
				}
			}

			if (message.getName().contains("focus changed")) {
				updateSliderValues();
				updateSliderLabel();
				updateListValues();
				updateImage();
			}

		}

	}

	/**
	 * Update slider values.
	 */
	public void updateSliderValues() {
		BeadImage beadImage = getCurrentBeadImage();
		if (beadImage == null)
			return;

		focusSlider.setValue(beadImage.getFocusPlane() + 1);

	}

	/**
	 * Update slider.
	 */
	public void updateSlider() {
		BeadImage beadImage = getCurrentBeadImage();
		if (beadImage != null) {
			
			focusSlider.setValueIsAdjusting(true);
			focusSlider.setMaximum(beadImage.getStackSize());

			updateSliderLabel();
			updateSliderValues();
		}
	}

	/**
	 * Update slider label.
	 */
	public void updateSliderLabel() {
		BeadImage beadImage = getCurrentBeadImage();
		if (beadImage != null) {
			focusSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
			focusSliderLabel.setText(String.format("%d / %d",
					focusSlider.getValue(), beadImage.getStackSize()));
		}
	}

	/**
	 * Update image.
	 */
	public void updateImage() {

		/*
		 * new Thread() { public void run() {
		 * 
		 * BeadImage beadImage = getCurrentBeadImage(); if
		 * (beadImage.getFocusPlane() == -1) {
		 * System.out.println("Launching autoFocus");
		 * focusSlider.setVisible(false); mainPanel.repaint();
		 * setCurrentProcessor(beadImage.getBeadPreview());
		 * focusSlider.setVisible(true); updateSlider();
		 * 
		 * } else { setCurrentProcessor(getCurrentPreview()); } } }.start();
		 */

		BeadImage beadImage = getCurrentBeadImage();
		if (beadImage != null) {
			setCurrentProcessor(beadImage.getPlane(focusSlider.getValue() - 1));
		}
	}

	/**
	 * Update list.
	 */
	public void updateList() {
		listModel.removeAllElements();
		for (int i = 0; i != getBeadImageManager().countBeadImage(); i++) {
			listModel.addElement(getBeadImageManager().getBeadImage(i));
		}

		beadImageList.setSelectedIndex(displayedBeadImage);

	}

	/**
	 * Update list values.
	 */
	public void updateListValues() {

		beadImageList.repaint();
	}

	/**
	 * Gets the current bead image.
	 *
	 * @return the current bead image
	 */
	public BeadImage getCurrentBeadImage() {
		int beadImageCount = getBeadImageManager().countBeadImage();
		if (beadImageCount > 0 && displayedBeadImage < beadImageCount) {
			return getBeadImageManager().getBeadImage(displayedBeadImage);
		} else {
			return null;
		}
	}

	/**
	 * Gets the current preview.
	 *
	 * @return the current preview
	 */
	public ImageProcessor getCurrentPreview() {
		BeadImage beadImage = getCurrentBeadImage();
		if (beadImage == null)
			return null;
		beadImage.getFocusPlane();
		return beadImage.getBeadPreview();
	}

	/**
	 * Sets the current processor.
	 *
	 * @param ip the new current processor
	 */
	public synchronized void setCurrentProcessor(ImageProcessor ip) {
		if (ip != null) {
			if (canvas == null) {
				canvas = new KnopImageCanvas(600, 400);
				canvas.setImage(ip);
				secondPanel.add(canvas, BorderLayout.CENTER);
			} else {
				canvas.setImage(ip);
				canvas.repaint();
			}
		}
	}

	/** The on ajusting slider. */
	ChangeListener onAjustingSlider = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			// TODO Auto-generated method stub
			BeadImage beadImage = getCurrentBeadImage();
			if (beadImage == null)
				return;
			if (focusSlider.getValueIsAdjusting()) {
				setCurrentProcessor(beadImage
						.getPlane(focusSlider.getValue() - 1));
				updateSliderLabel();
			} else {
				beadImage.setFocusPlane(focusSlider.getValue() - 1);
				updateListValues();
			}

		}
	};

	/** The on auto button pressed. */
	public AbstractAction onAutoButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getCurrentBeadImage().autoFocusAsync();

		}
	};

	/** The on previous image button pressed. */
	public AbstractAction onPreviousImageButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if (displayedBeadImage > 0) {
				displayedBeadImage--;
				updateImage();
				updateSlider();

			}

		}
	};

	/** The on next image button pressed. */
	public AbstractAction onNextImageButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (displayedBeadImage < beadImageManager.countBeadImage() - 1) {
				displayedBeadImage++;
				updateImage();
				updateSlider();
			}
		}
	};

	/** The on list change. */
	private ListSelectionListener onListChange = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			// TODO Auto-generated method stub
			displayedBeadImage = beadImageList.getSelectedIndex();
			updateImage();
			updateSlider();
		}
	};

	/** The bead image cell renderer. */
	protected ListCellRenderer<Object> beadImageCellRenderer = new ListCellRenderer<Object>() {

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Object> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {

			JLabel label = new JLabel();

			String background;
			BeadImage beadImage = null;

			if (value instanceof BeadImage) {
				beadImage = (BeadImage) value;
			}

			if (beadImage == null) {
				return label;
			}

			String name = beadImage.getImageName();
			int focusPoint = beadImage.getRawFocusPoint() + 1;

			if (!isSelected) {
				background = "white";
			} else {
				background = "#5555FF";
			}

			String text = String
					.format("<html>"
							+ "<div style='padding:5px;width:150px;background:%s;margin-bottom:solid 1px #DDDDDD'>"
							+ "<b>%s</b><br><i>Focus plan : %d" + "</div>"
							+ "</html>", background, name, focusPoint);

			label.setText(text);

			return label;

		}

	};

}
