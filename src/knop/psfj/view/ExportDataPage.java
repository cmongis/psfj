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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import knop.psfj.BeadImageManager;

import org.swixml.XVBox;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportDataPage.
 */
public class ExportDataPage extends WizardPage {

	/** The main panel. */
	XVBox mainPanel;
	
	/** The location v box. */
	XVBox locationVBox;
	
	/** The do export group. */
	ButtonGroup doExportGroup;
	
	/** The multiple export group. */
	ButtonGroup multipleExportGroup;

	/** The do export radio. */
	JRadioButton doExportRadio;
	
	/** The do not export radio. */
	JRadioButton doNotExportRadio;

	/** The multiple export radio. */
	JRadioButton multipleExportRadio;
	
	/** The single export radio. */
	JRadioButton singleExportRadio;

	/** The export directory. */
	String exportDirectory;

	/** The location label. */
	JLabel locationLabel;

	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String arg[]) {

		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();

		manager.addObserver(wizardWindow);

		wizardWindow.addPage(new ExportDataPage(manager));

		wizardWindow.setCurrentPage(0);

	}

	/**
	 * Instantiates a new export data page.
	 *
	 * @param manager the manager
	 */
	public ExportDataPage(BeadImageManager manager) {
		super(manager);

		engine = getSwingEngine();
		setTitle("Computation of the Microscope Resolution");
		setExplaination("");
		try {
			engine.render("knop/psfj/view/ExportDataPage.xml");
			doExportRadio.addChangeListener(onExportChanged);
			doNotExportRadio.addChangeListener(onExportChanged);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Update model.
	 */
	public void updateModel() {
		
	/*	getBeadImageManager().setExportMultiplePDF(
				multipleExportRadio.isSelected()); */
		getBeadImageManager().setDoExport(doExportRadio.isSelected());
		getBeadImageManager().setExportDirectory(exportDirectory);
	}

	/**
	 * Update view.
	 */
	public void updateView() {
		
		//multipleExportRadio.setSelected(getBeadImageManager().isDoExport());

		//singleExportRadio.setSelected(!getBeadImageManager().isDoExport());
		
		doExportRadio.setSelected(getBeadImageManager().isDoExport());
		doNotExportRadio.setSelected(!getBeadImageManager().isDoExport());
			//	.isExportMultiplePDF());
		
		
		
		updateLocationLabel();

		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {
		exportDirectory = getBeadImageManager().getExportDirectory();
		updateView();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		if (doExportRadio.isSelected() && exportDirectory == null)
			return false;
		else
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
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	/** The on export changed. */
	public ChangeListener onExportChanged = new ChangeListener() {

		

		@Override
		public void stateChanged(ChangeEvent e) {
			System.out.println(doExportRadio.isSelected());
			updateModel();
			updateLocationLabel();
			
		}
	};

	
	
	/**
	 * Update location label.
	 */
	public void updateLocationLabel() {
		
		
		locationVBox.setVisible(doExportRadio.isSelected());
		
		
		exportDirectory = getBeadImageManager().getExportDirectory();
		
		
		String text = "<html><b style='font-size:1.1em'>Save Location</b> :  ";
		if(exportDirectory == null) 
			text+= "<font color='red'>Please specify a directory</font>";
		else {
			text+=exportDirectory;
		}
		text += "</html>";
		locationLabel.setText(text);
		
		mainPanel.repaint();
		
	}
	
	/** The on change location button pressed. */
	public AbstractAction onChangeLocationButtonPressed = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String path = IJ.getDirectory("");
			exportDirectory = path;
			updateModel();
			updateView();
			
		}
	};

}
