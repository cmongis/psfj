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
import ij.io.SaveDialog;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import knop.psfj.BeadAverager;
import knop.psfj.BeadAveragerSubPixel;
import knop.psfj.BeadFrame;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.exporter.CsvExporter;
import knop.psfj.exporter.PDFExporter;
import knop.psfj.utils.FileUtils;
import knop.psfj.utils.TextUtils;

import org.swixml.SwingEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadInspectionPage.
 */
public class BeadInspectionPage extends WizardPage {

	/** The label list model. */
	BeadImageLabelListModel labelListModel = new BeadImageLabelListModel();

	/** The list container. */
	JList listContainer;

	/** The main panel. */
	JPanel mainPanel;

	/** The renderer. */
	ListCellRenderer renderer = BeadImageLabel.getRenderer();

	/** The second panel. */
	JPanel secondPanel;

	/** The canvas. */
	KnopImageCanvas canvas;

	/** The selection infos. */
	JLabel selectionInfos;

	/** The selection model. */
	BeadFrameListSelectionModel selectionModel = new BeadFrameListSelectionModel();

	/** The export substacks button. */
	JButton exportSubstacksButton;
	
	/** The average beads button. */
	JButton averageBeadsButton;
	
	/** The show substack button. */
	JButton showSubstackButton;
	
	/** The display reports button. */
	JButton displayReportsButton;
	
	/** The export csv button. */
	JButton exportCsvButton;

	/** The canvas container. */
	JPanel canvasContainer;

	/** The export substacks. */
	JMenuItem exportSubstacks;

	/** The explaination label. */
	JLabel explainationLabel;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		BeadImageManager manager = new BeadImageManager();

	

	
		manager.add("/Users/cyril/test_img/our_gfp/our_gfp_2048x2048_8bit.tif");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//manager.getBeadImage(0).setFocusPlane(29);
		manager.setThresholdValue(200);
		manager.verifyBeadImageParameters();
		
		manager.getBeadImage(0).getBeadFrameList();
		manager.processProfiles();
		
		
		WizardWindow wizardWindow = new WizardWindow(true);
		manager.addObserver(wizardWindow);
		wizardWindow.addPage(new HeatMapPage(manager));
		wizardWindow.setCurrentPage(0);

		wizardWindow.show();
	}

	/**
	 * Instantiates a new bead inspection page.
	 *
	 * @param w the w
	 * @param manager the manager
	 */
	public BeadInspectionPage(WizardWindow w, BeadImageManager manager) {
		this.beadImageManager = manager;
		init();

		assignIconToButton("exportCsvButton", "/icon-csv.gif");
		assignIconToButton("displayReportsButton", "/icon-pdf.gif");
		assignIconToButton("exportSubstacksButton", "/icon-save.png");
		assignIconToButton("averageBeadsButton", "/icon-combine.gif");

		canvas.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
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

		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

				Rectangle selection = canvas.lastSelection;

				if (selection == null) {
					selectionModel.clearSelection();
					return;
				}

				if (selection.width == 0 || selection.height == 0) {

					selectionModel.select(new Point(selection.x, selection.y),
							e.isShiftDown() == false);
					updateImage();
				} else {
					selectionModel.select(selection, e.isShiftDown() == false);
					updateImage();
				}
				updateSelectionInfos();
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

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

		// System.out.println(message);

		if (message.getName().contains("error")) {
			JOptionPane.showMessageDialog(mainPanel, message.getStringData(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}

		if (message.getOrigin() instanceof BeadImage) {

			updateItems();

			return;
		}
		if (message.getOrigin() instanceof BeadImageManager) {
			updateItems();
			updateImage();

		}
		if (message.getOrigin() instanceof BeadAveragerSubPixel) {
			getBeadImageManager().setProgress("Averaging beads...",
					message.getIntData());
		}
	}

	/**
	 * Update items.
	 */
	public synchronized void updateItems() {

		labelListModel.updateListFromManager();

		listContainer.repaint();

		// mainPanel.repaint();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onResize(java.awt.Component)
	 */
	public void onResize(Component p) {

	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isBackPossible()
	 */
	@Override
	public boolean isBackPossible() {
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isQuitOkay()
	 */
	@Override
	public boolean isQuitOkay() {
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {
		return mainPanel;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	@Override
	public void onDisplay() {
		// TODO Auto-generated method stub
		selectionModel.resetLastSelection();
		updateItems();
		updateImage();
		onSelectionChanged.valueChanged(null);
		canvas.fitImageToCanvas();
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#init()
	 */
	public void init() {
		try {
			engine = new SwingEngine(this);
			engine.render("knop/psfj/view/BeadInspectionPage.xml")
					.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setTitle("Bead Inspection & Exportation");
		setExplaination("Here you can inspect individual beads or select several beads to be averaged as one for correction.");

		labelListModel.setManager(getBeadImageManager());
		labelListModel.setDisplayImage(false);
		labelListModel.setDisplayBeadNumber(true);
		labelListModel.setDisplayInfos(true);
		canvas = new KnopImageCanvas();
		canvas.setEnableOpening(false);
		canvas.setFittingMode(KnopImageCanvas.FIT_TO_SMALLER_DIMENSION);
		listContainer.setModel(labelListModel);
		listContainer.setCellRenderer(renderer);

		canvasContainer.add(canvas, BorderLayout.CENTER);

		listContainer.addListSelectionListener(onSelectionChanged);

		selectionInfos.setFont(new Font(Font.DIALOG, Font.BOLD, selectionInfos
				.getFont().getSize()));
		selectionInfos.setForeground(Color.red);
		selectionInfos.setText("Please select an image stack first.");
		explainationLabel.setText(TextUtils.readTextRessource(this,
				"/beadinspection.html"));
	}

	ListSelectionListener onSelectionChanged = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {

			BeadImageLabel label = (BeadImageLabel) listContainer
					.getSelectedValue();
			if (label == null)
				return;

			BeadImage image = label.beadImage;

			selectionModel.setBeadFrameList(image.getBeadFrameList());
			updateImage();
			canvas.fitImageToCanvas();
			updateSelectionInfos();

		}
	};
	
	/**
	 * Update selection infos.
	 */
	public void updateSelectionInfos() {

		int selectedBeads = selectionModel.getSelected().size();
		int validBeads = selectionModel.getSelected().getValidBeadFrameCount();
		if (selectedBeads > 0) {
			selectionInfos.setForeground(Color.green.darker());
			selectionInfos.setText(String.format(
					"%d beads selected, %d are valid.", selectedBeads, validBeads));
		} else {
			selectionInfos.setForeground(Color.red);

			String selectionString;

			if (IJ.isMacOSX() || IJ.isMacOSX()) {
				selectionString = "Use right-click or (CMD + left-click) to select beads.";
			} else {
				selectionString = "Use right-click to select beads";
			}

			selectionInfos.setText(selectionString);
		}
		showSubstackButton.setEnabled(validBeads == 1);
		averageBeadsButton.setEnabled(validBeads > 1);
		exportSubstacksButton.setEnabled(selectedBeads > 0);
		displayIndividualReports.setEnabled(selectedBeads > 0);
		exportCsvButton.setEnabled(selectionModel.getLastSelection() != null);
	}

	/**
	 * Auto select.
	 */
	public void autoSelect() {
		System.out.println("selection :" + listContainer.getSelectedIndex());
		if (listContainer.getSelectedIndex() == -1 && labelListModel.size() > 0) {

			try {
				listContainer.setSelectedValue(labelListModel.get(0), true);
				updateImage();
				canvas.fitImageToCanvas();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * Update image.
	 */
	public void updateImage() {
		// mainPanel.repaint();
		// canvas.repaint();
		BeadImageLabel imageLabel = (BeadImageLabel) listContainer
				.getSelectedValue();
		if (imageLabel == null)
			return;
		BeadImage image = imageLabel.beadImage;
		if (image == null)
			return;

		canvas.setImage(selectionModel.drawSelection(image.getMiddleImage()));
		// canvas.fitImageToCanvas();
		canvas.repaint();
	}

	/**
	 * Go selection mode.
	 */
	public void goSelectionMode() {
		canvas.setMode(KnopImageCanvas.SELECTION_MODE);
	}

	/**
	 * Go moving mode.
	 */
	public void goMovingMode() {
		canvas.setMode(KnopImageCanvas.MOVE_MODE);
	}

	/** The export selected beads. */
	public Action exportSelectedBeads = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			exportSelectedBeads();
		}
	};

	/**
	 * Average selected beads.
	 */
	public void averageSelectedBeads() {
		final BeadInspectionPage page = this;
		new Thread() {
			public void run() {

				BeadFrameList list = selectionModel.getSelected()
						.getOnlyValidBeads();
				System.out.println(list.size() + " bead selected !");
				BeadAverager averager = new BeadAveragerSubPixel();
				averager.addObserver(page);
				ImagePlus averageBead = averager.averageBead(list);
				int bitDepth = getBeadImageManager().getBeadImage(listContainer.getSelectedIndex()).getStack().getBitDepth();
				
				IJ.run(averageBead, ""+bitDepth+"-bit", "");
				
				/*
				 * BeadFrame frame = new BeadFrame2D(0,null);
				 * frame.setSource(list.get(0).getSource());
				 * frame.setSubstack(averageBead); frame.findPSF();
				 */
				// averageBead.show();

				SaveDialog sd = new SaveDialog("Save averaged bead...",
						getLastFolder(), getBeadImageManager().getBeadImage(listContainer.getSelectedIndex()).getImageNameWithoutExtension()
								+ "_average", ".tif");

				if (sd.getDirectory() == null || sd.getFileName() ==null)
					return;

				String path = sd.getDirectory() + sd.getFileName();
				setLastFolder(sd.getDirectory());

				IJ.saveAsTiff(averageBead, path);

				averager.deleteObserver(page);

				FileUtils.openFolder(sd.getDirectory());

			}
		}.start();

		// System.out.println(frame.getResolution(0));
	}

	/**
	 * Export selected beads.
	 */
	public void exportSelectedBeads() {
		
		
		new Thread() {
			public void run() {
				System.out.println("Something is happeninig !");
				BeadFrameList list = selectionModel.getSelected();

				String exportFolder = getBeadImageManager().getExportDirectory();

				exportFolder = FileUtils.getDirectory("Select export directory",
						exportFolder);

				if (exportFolder == null)
					return;
				int count = list.size();
				int i = 1;
				for (BeadFrame frame : list) {
					getBeadImageManager().setProgress("Exporting beads....", i*100/count);
					String fileName = exportFolder + "/"
							+ frame.getSource().getImageName() + "_bead_" + frame.getId();
					IJ.saveAsTiff(frame.getSubstack(), fileName);
				}

				FileUtils.openFolder(exportFolder);
				getBeadImageManager().setProgress("", 0);
			}
		}.start();
		
	}

	/** The display selected bead. */
	public Action displaySelectedBead = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("displaying selected bead...");
			selectionModel.getSelected().get(0).getSubstack().show();

		}
	};

	/** The export substacks in csv. */
	public Action exportSubstacksInCsv = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			
			String path;
			
			if(getBeadImageManager().isDualColorAnalysis()) {
				path = FileUtils.getDirectory("Select a directory for CSV exportation", getLastFolder());
				if(path != null) setLastFolder(path);
				else return;
			}
			else {
			SaveDialog sd = new SaveDialog("Export selected beads as CSV",
					getLastFolder(), getBeadImageManager().getAnalysisName()
							+ "_data_", ".csv");

			if (sd.getDirectory() == null)
				return;
				setLastFolder(sd.getDirectory());
				path = sd.getDirectory() + sd.getFileName();
			}
			
			
		

			CsvExporter exporter = new CsvExporter(getBeadImageManager());
			
			exporter.exportCsv(path, selectionModel.getLastSelection(), true);
			
	
		}
	};

	/** The display individual reports. */
	public Action displayIndividualReports = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread() {
				public void run() {
					String tmpDir = System.getProperty("java.io.tmpdir");

					String uuid = UUID.randomUUID().toString();

					String path = tmpDir + uuid + ".pdf";

					PDFExporter exporter = new PDFExporter(getBeadImageManager());
					exporter.addObserver(getBeadImageManager());
					exporter.exportBeadImageList(selectionModel.getSelected(), path);
					exporter.deleteObserver(getBeadImageManager());
					FileUtils.openFolder(path);
				}
			}.start();
		}
	};

	/**
	 * The Class BeadFrameListSelectionModel.
	 */
	public class BeadFrameListSelectionModel
			extends
				ArrayList<BeadFrameObserver> {

		/** The selected. */
		BeadFrameList selected = new BeadFrameList();

		
		/** The last selection. */
		ArrayList<Rectangle> lastSelection = new ArrayList<Rectangle>();
		
		/**
		 * Sets the bead frame list.
		 *
		 * @param list the new bead frame list
		 */
		public void setBeadFrameList(BeadFrameList list) {
			clear();
			for (BeadFrame frame : list) {
				add(new BeadFrameObserver(frame));
			}
			if(getLastSelection() != null) select(getLastSelection(),true);
		}

		public void resetLastSelection() {
			selected.clear();
			lastSelection.clear();
			
		}

		/**
		 * Selection.
		 *
		 * @param p the p
		 */
		public void selection(Point p) {
			select(p, true);
		}

		/**
		 * Select.
		 *
		 * @param p the p
		 * @param clearSelection the clear selection
		 */
		public void select(Point p, boolean clearSelection) {
			
			lastSelection.clear();
			
			if (clearSelection)
				clearSelection();
			for (BeadFrameObserver b : this) {
				if (b.isPointIn(p)) {
					select(b);

				} else if (clearSelection) {
					unselect(b);
				}
			}
		}
		
		/**
		 * Gets the pointed bead frame.
		 *
		 * @param p the p
		 * @return the pointed bead frame
		 */
		public BeadFrame getPointedBeadFrame(Point p) {
			for (BeadFrameObserver b : this) {
				
				
				
				if (b.isPointIn(p)) {
					return b.getFrame();
				}
			}
			return null;
		}
		
		/**
		 * Select.
		 *
		 * @param r the r
		 * @param clearSelection the clear selection
		 */
		public void select(Rectangle r, boolean clearSelection) {
			
			lastSelection.clear();
			
			if (clearSelection) clearSelection();
			for (BeadFrameObserver b : this) {
				setSelection(b, b.isIn(r));
			}
			
			lastSelection.add(r);
			
		}

		/**
		 * Sets the selection.
		 *
		 * @param b the b
		 * @param isSelected the is selected
		 */
		public void setSelection(BeadFrameObserver b, boolean isSelected) {
			b.setSelected(isSelected);
			if (isSelected)
				selected.add(b.getFrame());
			else
				selected.remove(b.getFrame());

		}

		/**
		 * Select.
		 *
		 * @param b the b
		 */
		public void select(BeadFrameObserver b) {
			setSelection(b, true);
		}

		/**
		 * Unselect.
		 *
		 * @param b the b
		 */
		public void unselect(BeadFrameObserver b) {
			setSelection(b, false);
		}

		/**
		 * Clear selection.
		 */
		public void clearSelection() {
			for (BeadFrameObserver b : this) {
				unselect(b);
			}
			selected.clear();
			lastSelection.clear();
		}

		/**
		 * Gets the selected.
		 *
		 * @return the selected
		 */
		public BeadFrameList getSelected() {
			return selected;
		}
		
		/**
		 * Gets the last selection.
		 *
		 * @return the last selection
		 */
		public Rectangle getLastSelection() {
			if(lastSelection.size() == 1) return lastSelection.get(0);
			else return null;
		}

		/**
		 * Draw selection.
		 *
		 * @param ip the ip
		 * @return the image processor
		 */
		public ImageProcessor drawSelection(ImageProcessor ip) {
			ip = ip.convertToRGB();
			for (BeadFrameObserver b : this) {
				Color c;

				if (b.isSelected) {
					c = Color.GREEN;
				} else if (b.getFrame().isValid()) {
					c = Color.WHITE;
				} else {
					c = Color.RED;
				}

				b.getFrame().draw(ip, 10, c);

			}
			
			ip.setColor(Color.GREEN);
			if(getLastSelection() != null) {
				Rectangle s = getLastSelection();
				ip.drawRect(s.x, s.y, s.width, s.height);
			}
			
			return ip;
		}
	}

	/**
	 * Inits the context menu.
	 */
	public void initContextMenu() {

	}

	/** The last folder. */
	String lastFolder;

	/**
	 * Gets the last folder.
	 *
	 * @return the last folder
	 */
	public String getLastFolder() {
		if (lastFolder == null) {
			lastFolder = getBeadImageManager().getBeadImage(0).getImageFolder();
		}
		return lastFolder;
	}

	/**
	 * Sets the last folder.
	 *
	 * @param f the new last folder
	 */
	public void setLastFolder(String f) {
		lastFolder = f;
	}
	
	
	

}
