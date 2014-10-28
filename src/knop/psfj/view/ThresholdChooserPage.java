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

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.utils.MathUtils;
import prefuse.util.ui.JRangeSlider;

// TODO: Auto-generated Javadoc
/**
 * The Class ThresholdChooserPage.
 */
public class ThresholdChooserPage extends WizardPage {

	/** The threshold slider. */
	JSlider thresholdSlider;
	
	/** The bead size slider. */
	JSlider beadSizeSlider;
	
	/** The focus slider. */
	JSlider focusSlider;

	/** The focus label. */
	JLabel focusLabel;
	
	/** The threshold label. */
	JLabel thresholdLabel;
	
	/** The bead size label. */
	JLabel beadSizeLabel;

	/** The focus auto button. */
	JButton focusAutoButton;
	
	/** The threshold auto button. */
	JButton thresholdAutoButton;
	
	/** The bead size auto button. */
	JButton beadSizeAutoButton;

	/** The main panel. */
	JPanel mainPanel;
	
	/** The canvas. */
	KnopImageCanvas canvas;
	
	/** The canvas container. */
	JPanel canvasContainer;

	/** The auto button. */
	JButton autoButton;

	/** The displayed image. */
	int displayedImage = 0;

	/** The range slider. */
	JRangeSlider rangeSlider;
	
	/** The range model. */
	BoundedRangeModel rangeModel = new DefaultBoundedRangeModel();
	
	/** The bead image list. */
	JList beadImageList;
	
	/** The list model. */
	BeadImageLabelListModel listModel = new BeadImageLabelListModel();

	/** The left range label. */
	JLabel leftRangeLabel;
	
	/** The right range label. */
	JLabel rightRangeLabel;
	
	/** The pixel value label. */
	JLabel pixelValueLabel;
	
	/** The range panel. */
	JPanel rangePanel; 
	
	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String[] arg) {

		
		BeadImageManager manager = new BeadImageManager();

		manager.add("/Users/cyril/test_img/6/6_dual_color/6_gfp.tif");
		manager.add("/Users/cyril/test_img/6/6_dual_color/6_mcherry.tif");
		//manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
		//manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
		// thPage.updateList();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WizardWindow wizardWindow = new WizardWindow();
		manager.addObserver(wizardWindow);

		ThresholdChooserPage thPage = new ThresholdChooserPage(manager);
		wizardWindow.addPage(new ThresholdChooserPage(manager));

		wizardWindow.setCurrentPage(0);

		
		wizardWindow.show();

	}

	/**
	 * Instantiates a new threshold chooser page.
	 *
	 * @param manager the manager
	 */
	public ThresholdChooserPage(BeadImageManager manager) {
		super();

		setTitle("Focus, threshold and sub-stack size preview");
		setExplaination("<ul>" +
				"<li>Use the treshold slider to adjust the detection of suitable beads.</li>"
				+ "<li>Use the \"sub-stack size\" slider to adjust the frame around the beads. The frame should<br>be as large as possible " +
				"without including neighboring beads (as much as possible).</li>" +
				"<li>Place mouse over the image and use wheel to zoom, keep button pressed to move the image.</li></ul>");

		setBeadImageManager(manager);
		try {
			getSwingEngine().render(
					"knop/psfj/view/ThresholdChooserPage.xml").setVisible(
					true);

			focusSlider.addChangeListener(onAdjustingFocusSlider);
			thresholdSlider.addChangeListener(onAjustingThresholdSlider);
			beadSizeSlider.addChangeListener(onAdjustingBeadSizeSlider);

			focusSlider.setOrientation(SwingConstants.VERTICAL);
			focusSlider.setInverted(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rangeSlider = new JRangeSlider(rangeModel,JRangeSlider.HORIZONTAL,JRangeSlider.LEFTRIGHT_TOPBOTTOM);
		rangeSlider.setBackground(Color.black);
		
		canvasContainer.setOpaque(true);
		canvasContainer.setBackground(Color.black);
		canvasContainer.setBorder(new EmptyBorder(2,2,2,2));
		
		createCanvas();
		
		pixelValueLabel.setForeground(Color.white);
		
		leftRangeLabel.setForeground(Color.white);
		leftRangeLabel.setHorizontalTextPosition(JLabel.CENTER);
		//rightRangeLabel.setForeground(Color.white);
		rangeSlider.setBorder(new EmptyBorder(5,5,5,5));
		rangePanel.add(rangeSlider,BorderLayout.SOUTH);
		rangePanel.setOpaque(true);
		rangePanel.setBackground(Color.black);
		
		
		listModel.setManager(getBeadImageManager());
		listModel.setDisplayInfos(false);
		listModel.setDisplayBeadNumber(true);
		
		beadImageList.setCellRenderer(beadImageCellRenderer);
		beadImageList.setModel(listModel);
		beadImageList.addListSelectionListener(onListChange);
		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof Message) {
			update((Message) arg);
		}
	}

	/**
	 * Update.
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		long begin = System.currentTimeMillis();
		//System.out.println(message);
		if (message.getOrigin() instanceof knop.psfj.BeadImage) {
			if(message.getName().contains("raw preview") && message.getOrigin() == getBeadImage()) {
				
				
				setCurrentProcessor(getBeadImage().getRawPreview());
				return;
			}

			if (message.getName() == BeadImage.MSG_PREVIEW_UPDATED) {
				updateImage();
			}
			if (message.getName().contains("changed")) {
				updateSliderValues();
				updateSliderLabels();
			}
			
			if(message.getName().contains("min and max")) {
				updateSliders();
			}
		}
		if (message.getOrigin() instanceof BeadImageManager) {
			updateList();
			updateSliderValues();

		}

		updateItems();

		long end = System.currentTimeMillis();

		//System.out.println("updated in " + (end - begin) + "ms");

	}

	/**
	 * Update sliders.
	 */
	public void updateSliders() {
		BeadImage beadImage = getBeadImage();

		if (beadImage != null) {

			/*
			 * if (beadImage.getThresholdLevel() == -1) {
			 * beadImage.autoThreshold(); }
			 * 
			 * if (beadImage.getBidEnlargement() == -1) {
			 * beadImage.autoBidEnlargement(getBeadImageManager()
			 * .getMicroscope()); }
			 */
			
			
			
			focusSlider.setValueIsAdjusting(true);
			beadSizeSlider.setValueIsAdjusting(true);
			thresholdSlider.setValueIsAdjusting(true);
			System.out.println("*** updating sliders "+beadImage.getStackSize());
			focusSlider.setMinimum(1);
			focusSlider.setMaximum(beadImage.getStackSize());

			thresholdSlider.setMinimum(beadImage.getThresholdMin());
			
			int beadImageMax = beadImage.getThresholdMax();
			//if (beadImageMax > thresholdSlider.getMaximum()) {
				thresholdSlider.setMaximum(beadImageMax);
			//}
			
			int fwhmX = MathUtils.round(beadImage.getMicroscope().getTheoreticalResolution(0)/beadImage.getCalibration().pixelWidth);
			
			beadSizeSlider.setMinimum(fwhmX*5);
			beadSizeSlider.setMaximum(fwhmX*20);
			updateSliderLabels();
			updateSliderValues();

			thresholdSlider.setValueIsAdjusting(false);
			focusSlider.setValueIsAdjusting(false);
			beadSizeSlider.setValueIsAdjusting(false);

			rangeModel = new ImageStackRangeModel(beadImage);
			rangeModel.addChangeListener(onRangeChanged);
			
			rangeSlider.setModel(rangeModel);
			
		}
	}

	/**
	 * Update slider values.
	 */
	public void updateSliderValues() {
		BeadImage beadImage = getBeadImage();

		if (beadImage != null) {

			focusSlider.setValue(beadImage.getFocusPlane() + 1);
		}
		BeadImageManager manager = getBeadImageManager();
		if (manager != null && manager.countBeadImage() > 0) {
			if (thresholdSlider.getValue() != beadImage.getThresholdValue() && thresholdSlider.getValue() != -1)
				thresholdSlider.setValue(beadImage.getThresholdValue());
			if (beadSizeSlider.getValue() != beadImage.getFrameSize())
				beadSizeSlider.setValue(beadImage.getFrameSize());

		} else {
			System.err
					.println("Cannot update sliders values : beadImage == null");
		}
		updateSliderLabels();

	}

	/**
	 * Update slider labels.
	 */
	public void updateSliderLabels() {
		if (getBeadImage() == null)
			return;

		thresholdLabel.setText(String.format("% d / %d    ",
				thresholdSlider.getValue(), thresholdSlider.getMaximum()));
		focusLabel.setText(String.format("%d / %d", focusSlider.getValue(),
				getBeadImage().getStackSize()));
		beadSizeLabel.setText(String.format("%d / %d px    ",
				beadSizeSlider.getValue(), beadSizeSlider.getMaximum()));
	}

	/**
	 * Update image.
	 */
	public void updateImage() {
		if (getBeadImage() != null) {
			//System.out.println(getBeadImage().getBeadPreview());
			/*
			if(getBeadImageManager().getAnalysisType() == BeadImageManager.DUAL_CHANNEL) {
				setCurrentProcessor(getBeadImageManager().getMergedPreview(displayedImage));
			}
			else*/
			setCurrentProcessor(getBeadImage().getBeadPreview());
		}

	}

	/**
	 * Update current preview async.
	 */
	public void updateCurrentPreviewAsync() {
		if (getBeadImage() != null) {
			getBeadImage().getBeadPreviewAsync();
		}
	}

	/**
	 * Update all previews async.
	 */
	public void updateAllPreviewsAsync() {
		if (getBeadImageManager() != null) {
			getBeadImageManager().updatePreviewAsync();
		}
	}

	/**
	 * Update list.
	 */
	public synchronized void updateList() {
		listModel.updateListFromManager();
	}

	/**
	 * Update items.
	 */
	public synchronized void updateItems() {
		
		beadImageList.repaint();
		/*for (BeadImageLabel label : (BeadImageLabel[])listModel.toArray()) {
			label.updateStatus();
		}*/
	}

	/**
	 * Creates the canvas.
	 */
	public void createCanvas() {
		canvas = new KnopImageCanvas(600, 350);
		
		canvas.setResetOnImageChange(false);
		canvas.doOpening = false;
		canvas.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
				pixelValueLabel.setText("");
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
			}
		});
		canvas.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
				if(getBeadImage() == null) return;
				Point p = canvas.getMousePositionOnImage();
				
				int x = p.x;
				int y = p.y;
				int z = getBeadImage().getFocusPlane();
				int value = getBeadImage().getPlane(z).getPixel(x, y);
				pixelValueLabel.setText(String.format(" Pixel value = %d",value));
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		canvasContainer.add(canvas, BorderLayout.CENTER);
		canvasContainer.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				canvas.setSize(canvas.getPreferredSize());
				
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		canvas.setSize(canvas.getPreferredSize());
		canvas.repaint();
	}
	
	
	/**
	 * Sets the current processor.
	 *
	 * @param ip the new current processor
	 */
	public synchronized void setCurrentProcessor(ImageProcessor ip) {
		
		
				canvas.setImage(ip);
				canvas.repaint();
	}

	/**
	 * Gets the bead image.
	 *
	 * @return the bead image
	 */
	public BeadImage getBeadImage() {
		BeadImageManager manager = getBeadImageManager();
		if (manager != null) {
			if (manager.countBeadImage() > 0
					&& displayedImage < manager.countBeadImage()) {
				return manager.getBeadImage(displayedImage);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {

		


		
		updateList();
		updateItems();
		beadImageList.setSelectedIndex(0);
		
	
		
		
		getBeadImageManager().verifyBeadImageParameters();
		
		updateSliders();
		updateSliderValues();
		
		for(BeadImage image : getBeadImageManager().getBeadImageList()) {
			image.getBeadPreview();
		}
		updateImage();
		
		canvas.fitImageToCanvas();
		/*
		updateImage();
		updateAllPreviewsAsync();
		updateCurrentPreviewAsync();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				canvas.fitImageToCanvas();
				canvas.repaint();
				//if(beadImageList.getSelectedIndex() == -1) beadImageList.setSelectedIndex(0);
				//updateSliders();
				
			}
		}.start();
		
		*/
	
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
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	/** The on text. */
	ActionListener onText = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("test");
		}
	};

	/** The on ajusting threshold slider. */
	ChangeListener onAjustingThresholdSlider = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent event) {
			// TODO Auto-generated method stub
			BeadImage beadImage = getBeadImage();
			BeadImageManager manager = getBeadImageManager();
			if (manager == null)
				return;

			if (beadImage == null)
				return;

			System.out.println("* Change Threshold !!!");

			if (thresholdSlider.getValueIsAdjusting()) {
				updateSliderLabels();
			} else {
				
				if (thresholdSlider.getValue() == thresholdSlider.getMinimum())
					return;
				if (thresholdSlider.getValue() == manager.getThresholdValue())
					return;
				
				if(thresholdSlider.getValue() == thresholdSlider.getMaximum()) return;
				
				setThresholdValue(thresholdSlider.getValue());
				
				
			}

		}
	};

	/** The on adjusting bead size slider. */
	ChangeListener onAdjustingBeadSizeSlider = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			BeadImage beadImage = getBeadImage();
			System.out.println("* Change Bead Size!!!");
			if (beadImage != null) {
				if (beadSizeSlider.getValueIsAdjusting()) {
					updateSliderLabels();
				} else {
					if (getBeadImage().getFrameSize() != beadSizeSlider
							.getValue()) {
						getBeadImageManager().setFrameSize(displayedImage,
								beadSizeSlider.getValue());
						updateAllPreviewsAsync();
					}

				}
			}
		}
	};

	/** The on adjusting focus slider. */
	ChangeListener onAdjustingFocusSlider = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			BeadImage beadImage = getBeadImage();
			if (beadImage != null) {
				if (focusSlider.getValueIsAdjusting()) {
					setCurrentProcessor(beadImage.getPlane(focusSlider
							.getValue() - 1));
					focusLabel.setText("" + focusSlider.getValue() + " / "
							+ beadImage.getStackSize());
				} else {
					getBeadImageManager().setFocusPlane(displayedImage,
							focusSlider.getValue() - 1);
					updateCurrentPreviewAsync();
				}
			}
		}
	};

	/** The on auto threshold button pressed. */
	public AbstractAction onAutoThresholdButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			BeadImage beadImage = getBeadImage();
			if (beadImage != null) {
				System.out.println("Auto !!!");
				getBeadImageManager().autoThreshold(displayedImage);
				updateAllPreviewsAsync();

			}
		}
	};

	/** The on auto bead size button pressed. */
	public AbstractAction onAutoBeadSizeButtonPressed = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			BeadImage beadImage = getBeadImage();
			if (beadImage != null) {
				getBeadImageManager().autoFrameSize();
				updateAllPreviewsAsync();

			}
		}
	};

	/** The on auto focus button pressed. */
	public AbstractAction onAutoFocusButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			BeadImage beadImage = getBeadImage();
			if (beadImage != null) {
				beadImage.autoFocusAsync();
			}

		}
	};

	/** The on list change. */
	public ListSelectionListener onListChange = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			displayedImage = beadImageList.getSelectedIndex();
			System.out.println("Displayed image changed : " + displayedImage);
		
			updateSliders();
			updateSliderValues();
			updateImage();
		}
	};

	/** The on range changed. */
	protected ChangeListener onRangeChanged = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			
			if(getBeadImage() == null) return;
			System.out.println(e.getSource());
			if(rangeModel.getValueIsAdjusting() == true) {
			
			System.out.println("updating image...");
			setCurrentProcessor(getBeadImage().getStack().getProcessor(getBeadImage().getFocusPlane()));
			}
			else {
				getBeadImage().resetPreview();
				updateImage();
			}
			//canvas.repaint();
			//updateImage();
			leftRangeLabel.setText(String.format("Min/max Range : [ %d - %d ]       ",rangeModel.getValue(),rangeModel.getValue()+rangeModel.getExtent()));
			
		}
	};
	
	
	/** The bead image cell renderer. */
	protected ListCellRenderer<BeadImageLabel> beadImageCellRenderer = new ListCellRenderer<BeadImageLabel>() {

		@Override
		public Component getListCellRendererComponent(
				JList<? extends BeadImageLabel> list, BeadImageLabel value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			
				value.setSelected(isSelected);
				return value.getView();
			
			/*
			
			try {

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
				int beadFound = beadImage.getDotNumber();

				String status = beadImage.getStatus();

				if (!isSelected) {
					background = "white";
				} else {
					background = "#5555FF";
				}

				String text = String.format("<html>"
						+ "<div style='width:100px;padding:5px;background:%s'>"
						+ "<b>%s</b><br><i>Beads found : %d<br>%s" + "</div>"
						+ "</html>", background, name, beadFound, status);

				label.setText(text);

				return label;

			} catch (Exception e) {
				return new JLabel("...");
			}

		}*/
		}
	};

	

	/**
	 * Sets the threshold value.
	 *
	 * @param value the new threshold value
	 */
	protected void setThresholdValue(int value) {
		BeadImageManager manager = getBeadImageManager();
		
		
		
		if(manager != null) {
			
			if(getBeadImage().getThresholdValue() != value) {
				manager.setThresholdValue(displayedImage, value);
				manager.updatePreviewAsync();
			}
			
			/*
			if(manager.getAnalysisType() == manager.SINGLE_CHANNEL && manager.getThresholdValue() != value) {
				manager.setThresholdValue(value);
				manager.updatePreviewAsync();
			}
			else if(getBeadImage().getThresholdValue() != value){
				getBeadImage().setThresholdValue(value);
				manager.updatePreviewAsync();
			}*/
			
		}
		
	}
	
	/**
	 * Sets the bead enlargement.
	 *
	 * @param value the new bead enlargement
	 */
	protected void setBeadEnlargement(int value) {
		BeadImageManager manager = getBeadImageManager();
		if(manager != null) {
			if(manager.getAnalysisType() == manager.SINGLE_CHANNEL && manager.getThresholdValue() != value) {
				manager.setThresholdValue(value);
				manager.updatePreviewAsync();
			}
			else if(getBeadImage().getThresholdValue() != value){
				getBeadImage().setThresholdValue(value);
				manager.updatePreviewAsync();
			}
			
		}
	}

}
