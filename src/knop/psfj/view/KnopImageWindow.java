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

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import knop.psfj.heatmap.EasyLUT;
import knop.psfj.heatmap.LUTManager;
import knop.psfj.utils.MathUtils;
import prefuse.util.ui.JRangeSlider;

// TODO: Auto-generated Javadoc
/**
 * The Class KnopImageWindow.
 */
public class KnopImageWindow extends JFrame {
	
	
	/** The canvas. */
	KnopImageCanvas canvas;
	
	/** The lut combo box. */
	JComboBox lutComboBox = new JComboBox();
	
	/** The pixel label. */
	JLabel pixelLabel = new JLabel();
	
	/** The stack slider. */
	JSlider stackSlider = new JSlider();
	
	/** The img. */
	ImagePlus img;
	
	/** The stack number. */
	int stackNumber;
	
	/** The current slice. */
	int currentSlice = 0;
	
	/** The range model. */
	BoundedRangeModel rangeModel;
	
	/** The main panel. */
	JPanel mainPanel;
	
	/** The range slider. */
	JRangeSlider rangeSlider;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		
		new KnopImageWindow(IJ.openImage("/home/cyril/test_img/6.tif")).show();
		
	}
	
	
	/**
	 * Instantiates a new knop image window.
	 */
	public KnopImageWindow() {
		this((ImageProcessor)null);
	}
	
	/**
	 * Instantiates a new knop image window.
	 *
	 * @param ip the ip
	 */
	public KnopImageWindow(ImagePlus ip) {
		
		this(ip.getStack().getProcessor(1));
		img = ip;
		if(img.getStackSize() > 1) {
			mainPanel.add(stackSlider,BorderLayout.EAST);
		}
		updateStackSlider();
		initRangeModel();
		stackSlider.setOrientation(JSlider.VERTICAL);
		stackSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				currentSlice = stackSlider.getValue();
				
			}
		});
		
		
		
		
	}
	
	
	/**
	 * Instantiates a new knop image window.
	 *
	 * @param ip the ip
	 */
	public KnopImageWindow(ImageProcessor ip) {
		
		/* Main Panel */
		mainPanel = new JPanel(new BorderLayout(10,10));
		((BorderLayout)mainPanel.getLayout()).setHgap(5);
		add(mainPanel);
		
		rangeModel = new ImageStackRangeModel(new ImagePlus("",ip));
		
		rangeSlider = new JRangeSlider(rangeModel,JRangeSlider.HORIZONTAL,JRangeSlider.LEFTRIGHT_TOPBOTTOM);
		rangeModel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				canvas.repaint();
				System.out.println("repainting");
			}
		});
		/* Control Panel */
		JPanel controlPanel = new JPanel(new BorderLayout());
		mainPanel.add(controlPanel,BorderLayout.NORTH);
		mainPanel.add(rangeSlider,BorderLayout.SOUTH);
		controlPanel.setBorder(new EmptyBorder(5,5,5,5));
		((BorderLayout)controlPanel.getLayout()).setHgap(5);
		/* Canvas */
		int canvasWidth = 400;
		double f = 1.0*ip.getHeight()/ip.getWidth();
		int canvasHeight = MathUtils.round(canvasWidth*f);
		
		
		canvas = new KnopImageCanvas(canvasWidth,canvasHeight);
		canvas.setImage(ip);
		mainPanel.add(canvas,BorderLayout.CENTER);
		canvas.setEnableOpening(false);
		canvas.addMouseMotionListener(onMouseMove);
		canvas.setResetOnImageChange(false);
		canvas.fitImageToCanvas();
		canvas.repaint();
		
		/* LUT ComboBox */
		controlPanel.add(lutComboBox,BorderLayout.WEST);
		LUTManager manager = new LUTManager();
		manager.add("thermal");
		manager.add("blue_orange_white");
		manager.add("blue_orange_white_invert");
		
		for(EasyLUT lut : manager) {
			lutComboBox.addItem(lut);
		}
		
		
		
		
		lutComboBox.setRenderer(manager);
		lutComboBox.addItemListener(onLutChanged);
		
		/* Pixel Label */
		
		controlPanel.add(pixelLabel,BorderLayout.EAST);
		
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addComponentListener(onWindowsResize);
		
		pack();
		System.out.println(canvas.getMagnification());
		
	}
	
	
	/** The on windows resize. */
	ComponentAdapter onWindowsResize = new ComponentAdapter() {
		
		public void componentResized(ComponentEvent e) {
			System.out.println(canvas.getPreferredSize());
			canvas.setSize(canvas.getPreferredSize());
			
			
		}
		
	
	};
	
	/** The on mouse move. */
	MouseMotionListener onMouseMove = new MouseMotionListener() {
		
		@Override
		public void mouseMoved(MouseEvent e) {
			pixelLabel.setText(""+canvas.getLastPixelValue());
			
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/** The on lut changed. */
	private ItemListener onLutChanged = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			System.out.println("Changing lut");
			ImageProcessor image = canvas.getImage().getProcessor();
			EasyLUT selectedLut = (EasyLUT) lutComboBox.getSelectedItem();
			image.setLut(selectedLut.getLUT(image.getMin(),image.getMax()));
			canvas.setImage(image);
			canvas.repaint();
		}
	};
	
	
	
	
	/**
	 * Inits the range model.
	 */
	public void initRangeModel() {
		if(img == null) return;
		
	
		
	}
	

	
	

	
	/**
	 * Update stack slider.
	 */
	public void updateStackSlider() {
		stackSlider.setMaximum(img.getStackSize());
		stackSlider.setValue(0);
	}
	
}
