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

import ij.io.OpenDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.PSFj;
import knop.psfj.utils.MemoryUtils;
import knop.psfj.utils.TextUtils;
import net.iharder.dnd.FileDrop;

import org.swixml.SwingEngine;
import org.swixml.XVBox;

import com.jgoodies.forms.layout.CellConstraints;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadImageLoaderPage.
 */
public class BeadImageLoaderPage extends WizardPage {

	/** The main panel. */
	JPanel mainPanel;
	
	/** The list container. */
	JList listContainer;
	
	/** The analysis mode panel. */
	JPanel analysisModePanel;
	
	/** The bead image label map. */
	HashMap<BeadImage, Integer> beadImageLabelMap = new HashMap<BeadImage, Integer>();

	/** The label list. */
	ArrayList<BeadImageLabel> labelList = new ArrayList<BeadImageLabel>();

	/** The label list model. */
	BeadImageLabelListModel labelListModel = new BeadImageLabelListModel();

	

	/** The last folder key. */
	static String LAST_FOLDER_KEY = "Screen 1 : last folder";
	
	/** The last folder. */
	File lastFolder = new File(PSFj.loadConfiguration(LAST_FOLDER_KEY,""));
	
	/** The wavelength list container. */
	JPanel wavelengthListContainer;

	/** The mode radio group. */
	ButtonGroup modeRadioGroup;

	/** The single channel radio. */
	JRadioButton singleChannelRadio;
	
	/** The dual channel radio. */
	JRadioButton dualChannelRadio;

	/** The wavelength label. */
	JLabel wavelengthLabel;

	/** The info container. */
	XVBox infoContainer;
	
	/** The info label. */
	JLabel infoLabel;

	String faq = TextUtils.readTextRessource(this, "/info.html");
	String warning = TextUtils.readTextRessource(this, "/knop/psfj/view/JavaMemoryWarning.html");
	
	
        AdvancedSettingWindow advancedSettings = new AdvancedSettingWindow();
        
        
	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String[] arg) {

		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();

		manager.addObserver(wizardWindow);

		wizardWindow.addPage(new BeadImageLoaderPage(wizardWindow, manager));

		wizardWindow.setCurrentPage(0);

		wizardWindow.show();
		// manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");
		// manager.add("/media/data/Knop/Patrick/100x_512x512c1.tif");

	}

	/**
	 * Instantiates a new bead image loader page.
	 */
	public BeadImageLoaderPage() {

	}

	/**
	 * Instantiates a new bead image loader page.
	 *
	 * @param w the wizard window
	 * @param m the BeadImageManager
	 */
	public BeadImageLoaderPage(WizardWindow w, BeadImageManager m) {

		beadImageManager = m;
		// beadImageManager.addObserver(this);

		init();
	}

	
	/**
	 * Sets the last opened folder
	 *
	 * @param lastFolder the paht of the last folder
	 */
	public void setLastFolder(String lastFolder) {
		this.lastFolder = new File(lastFolder);
		PSFj.saveConfiguration(LAST_FOLDER_KEY,lastFolder);
	}
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#init()
	 */
        @Override
	public void init() {
		try {
			engine = new SwingEngine(this);
			engine.render("knop/psfj/view/BeadImageLoaderPage.xml").setVisible(
					true);
		} catch (Exception e) {
                        e.printStackTrace();
                       
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		setTitle("Open stacks");
		setExplaination(
				"Click on <b>Add Image Stack</b> or <b>drag-and-drop your image stack</b> in this windows.<br>"
				+ "<b>E.g.</b> : .tif, single-color files, 8 or 16-bits grayscale)<br>"
				+"<br>For <b>single-color analysis</b>, add one or multiple image stacks from different fields of view.<br>"
				+ "For <b>dual-color analysis</b>, add two image stacks corresponding to each channel from the same field of view."
				);

		labelListModel.setManager(getBeadImageManager());
		labelListModel.setDisplayImage(false);
		labelListModel.setDisplayBeadNumber(true);
		labelListModel.setDisplayInfos(true);

		listContainer.setModel(labelListModel);
		listContainer.setCellRenderer(renderer);
		new FileDrop(mainPanel, new FileDrop.Listener() {

			public void filesDropped(final java.io.File[] files) {
				System.out.println(files.length + " files dropped !");
				getBeadImageManager().addAsync(files);
				
			}
		});
		assignSystemIconToButton("openButton", "FileView.fileIcon");
		
		// testing if PSFj is running on a 32-bit Java Runtime or on 64-bit JRE
		int byte_number = Integer.parseInt(System.getProperty("sun.arch.data.model") );
		System.out.println(byte_number);
		
		String body = "<html><div style=\"width:400px;margin-left:20px\"> \n %s <br> %s <div></html>";
		warning = warning.replace("%max_memory%",""+MemoryUtils.getMaximumMemory());
		warning = warning.replace("%max_stack_memory%",""+(MemoryUtils.getMaximumMemory()/2));
		if(byte_number == 32)
			infoLabel.setText(String.format(body,warning,faq));
		else
			infoLabel.setText(String.format(body,faq,""));
                
                
                advancedSettings.setManager(getBeadImageManager());
                
	}
        
        
        public void showAdvancedSettings() {
            advancedSettings.show();
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
	 * Handle the messages comming from the different observable
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		// System.out.println(message);

		
		if(message.getName().contains("reset")) {
			getBeadImageManager().resetDataSet();
			
		}
		
		if (message.getOrigin() instanceof BeadImage) {

			updateItems();
			return;
		}
		if (message.getOrigin() instanceof BeadImageManager) {

			updateModeView();
			updateItems();
			System.out.println(message);
			if(message.getName().contains("analysis type") ) updateWaveLengthList();
			updateModeView();
		}
	}

	/**
	 * Update item list.
	 */
	public synchronized void updateItems() {

		labelListModel.updateListFromManager();
		listContainer.repaint();
		// mainPanel.repaint();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub

		if (getBeadImageManager().getBeadImageList().size() == 0)
			return false;
		
		boolean isDualColor = getBeadImageManager().isDualColorAnalysis();
		try { 
		if(isDualColor &&
				(getBeadImageManager().getMicroscope(0).getWaveLength() == 0.0
				|| getBeadImageManager().getMicroscope(1).getWaveLength() == 0.0)) {
			return false;
		}
		}catch(Exception e) {
			return false;
		}

		return true && getBeadImageManager().isReady() && !getBeadImageManager().isImageLoading();
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

	/** The open image. */
	public AbstractAction openImage = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			new Thread() {
				public void run() {
					if (lastFolder == null)
						lastFolder = new File("");
					
					OpenDialog sd = new OpenDialog("Open an image",
							lastFolder.getAbsolutePath(), ".tif");
					if (sd.getFileName() != null) {
						String file = sd.getDirectory() + sd.getFileName();
						setLastFolder(sd.getDirectory());
						
						beadImageManager.add(file);
						
					}
				}

			}.start();
		}

	};

	/**
	 * Update the wavelength list
	 */
	public void updateWaveLengthList() {
		if (isSingleMode())
			return;
		wavelengthListContainer.removeAll();

		int row = 1;
		CellConstraints c = new CellConstraints();

		for (BeadImage image : getBeadImageManager().getBeadImageList()) {
			// wavelengthListContainer.add(new JLabel(image.getImageName()));
			if (row > 3)
				break;
			
			JLabel label = new JLabel(image.getImageName(), image.getIcon(16),
					JLabel.LEADING);
			wavelengthListContainer.add(label, c.xy(1, row));
					
			WaveLengthTextField textField = new WaveLengthTextField(image);
			textField.setIconLabel(label);
			textField.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
					notifyReadyStateChange();
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			wavelengthListContainer.add(textField,
					c.xy(3, row));
			wavelengthListContainer.add(new JLabel("nm"), c.xy(5, row));
			row += 2;

		}
		
	}

	/**
	 * Update mode view (single-color or dual-color).
	 */
	public void updateModeView() {
		infoContainer.setVisible(getBeadImageManager().countBeadImage() <= 1);
		infoLabel.setVisible(getBeadImageManager().countBeadImage() <= 1);
		BeadImageManager m = getBeadImageManager();
		
		boolean isSingleColor = !m.isDualColorAnalysis();
		boolean moreThanTwo = m.countBeadImage() > 2;
		boolean atLeastTwo = m.countBeadImage() >=2;
		
		singleChannelRadio.setSelected(isSingleColor);
		dualChannelRadio.setSelected(!isSingleColor);
		
		wavelengthLabel.setVisible(!isSingleColor);
		wavelengthListContainer.setVisible(!isSingleColor);
		
		
		showAnalysisTypePanel(atLeastTwo);
		
	
		
		dualChannelRadio.setEnabled(!moreThanTwo);
		
	}

	/**
	 * Show analysis type panel.
	 *
	 * @param mode the mode
	 */
	public void showAnalysisTypePanel(boolean mode) {
		analysisModePanel.setVisible(mode);
	}

	/**
	 * Checks if is single mode.
	 *
	 * @return true, if is single mode
	 */
	public boolean isSingleMode() {
		return (getBeadImageManager().getAnalysisType() == BeadImageManager.SINGLE_CHANNEL);
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	@Override
	public void onDisplay() {
		// TODO Auto-generated method stub
		
		getBeadImageManager().resetDataSet();
		updateModeView();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onResize(java.awt.Component)
	 */
	public void onResize(Component e) {
		mainPanel.setSize(e.getSize());
		listContainer.setSize(mainPanel.getWidth(),
				listContainer.getPreferredSize().height);
	}

	/** The list cell rendered usef for... */
	ListCellRenderer<BeadImageLabel> renderer = new ListCellRenderer<BeadImageLabel>() {

		@Override
		public Component getListCellRendererComponent(
				JList<? extends BeadImageLabel> list, BeadImageLabel value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// TODO Auto-generated method stub

			value.setSelected(isSelected);
			return value.getView();
		}
	};

	/** on mode changed (single-color or dual-color). */
	public Action onModeChanged = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String mode = e.getActionCommand();

			if (mode.equals("single channel")) {
				getBeadImageManager().setAnalysisType(
						BeadImageManager.SINGLE_CHANNEL);
			} else {
				getBeadImageManager().setAnalysisType(
						BeadImageManager.DUAL_CHANNEL);
			}
			getBeadImageManager().checkForWarnings();
			updateModeView();

		}
	};

	/** when a  delete bead. */
	public Action onDeleteBead = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Asking for deletion !");
			System.out.println(listContainer.getSelectedIndex());
			getBeadImageManager().remove(listContainer.getSelectedIndex());

		}
	};

}
