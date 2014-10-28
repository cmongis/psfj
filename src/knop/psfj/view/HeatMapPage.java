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
import ij.io.SaveDialog;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import knop.psfj.BeadFrame;
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.exporter.HTML5AnalyzerExporter;
import knop.psfj.exporter.HTMLDataSetExporter;
import knop.psfj.exporter.HeatMapTable;
import knop.psfj.graphics.AsymmetryHeatMap;
import knop.psfj.graphics.FullHeatMap;
import knop.psfj.graphics.PsfJGraph;
import knop.psfj.graphics.ThetaHeatMap;
import knop.psfj.heatmap.LUTManager;
import knop.psfj.utils.FileUtils;

import org.swixml.XVBox;



// TODO: Auto-generated Javadoc
/**
 * The Class HeatMapPage.
 */
public class HeatMapPage extends WizardPage {

	/** The canvas. */
	KnopImageCanvas canvas;

	/** The main panel. */
	JPanel mainPanel;
	
	/** The main panel2. */
	JPanel mainPanel2;
	
	/** The fov data set. */
	FovDataSet fovDataSet;

	/** The stats label. */
	JLabel statsLabel;

	

	/** The action combo box. */
	JComboBox<ExportAction> actionComboBox;
	
	/** The column combo box. */
	JComboBox<PsfJGraph> columnComboBox;
	

	/** The stats panel. */
	JPanel statsPanel;

	/** The stats box. */
	XVBox statsBox;

	/** The lut manager. */
	LUTManager lutManager = new LUTManager();

	/** The fullscreen button. */
	JButton fullscreenButton;
	
	/** The micrometer button. */
	JToggleButton micrometerButton;
	
	/** The normalized button. */
	JToggleButton normalizedButton;
	
	/** The last folder. */
	String lastFolder = System.getProperties().getProperty("home");

	/** The description text pane. */
	JLabel descriptionTextPane;
	
	/** The description scroll pane. */
	JScrollPane descriptionScrollPane;
	
	/** The canvas panel. */
	JPanel canvasPanel;
	
	/** The control panel. */
	JPanel controlPanel;
	
	/** The inspection panel. */
	JPanel inspectionPanel;
	
	/** The image label. */
	JLabel imageLabel;
	
	/** The full screen frame. */
	JFrame fullScreenFrame;
	
	/** The unit mode. */
	int unitMode = PsfJGraph.NOT_NORMALIZED;
	
	/** The table. */
	HeatMapTable table = new HeatMapTable();
	
	/** The tabbed pane. */
	JTabbedPane tabbedPane;
	
	/** The inspection page. */
	BeadInspectionPage inspectionPage;
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]) {

		BeadImageManager manager = new BeadImageManager();
		manager.add("/home/cyril/test_img/6/6_gfp.tif");
		manager.add("/home/cyril/test_img/6/6_mcherry.tif");
	
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//manager.add("/home/cyril/test_img/colocalisation/mc_01_220beads_small.tif");
		//manager.autoThreshold();
		manager.autoThreshold();
		manager.autoBeadEnlargementFactor();
		manager.processProfiles();

		WizardWindow wizardWindow = new WizardWindow();

		manager.addObserver(wizardWindow);

		wizardWindow.addPage(new HeatMapPage(manager));
		wizardWindow.setCurrentPage(0);
		wizardWindow.show();
		System.out.println("!!!!!!!!!!!!!!!!!! FIN !!!!!!!!!!!!!!!!");

	}

	/**
	 * Instantiates a new heat map page.
	 *
	 * @param manager the manager
	 */
	public HeatMapPage(BeadImageManager manager) {
		super(manager);

		engine = getSwingEngine();
		try {
			engine.render("knop/psfj/view/HeatMapPage.xml");

			setTitle("Results & Export");
			setExplaination("");
			

			actionComboBox.setRenderer(new ListCellRenderer<ExportAction>() {

				@Override
				public Component getListCellRendererComponent(
						JList<? extends ExportAction> list, ExportAction value,
						int index, boolean isSelected, boolean cellHasFocus) {
					// TODO Auto-generated method stub
					JLabel l = (JLabel) value.getComponent();
				
					if(isSelected) {
						l.setBackground(list.getSelectionBackground());
						l.setForeground(list.getSelectionForeground());
					}
					else {
						l.setBackground(list.getBackground());
						l.setForeground(list.getForeground());
						
					}
					return l;
				}
			});
			
			
			columnComboBox.addItemListener(onColumnChanged);
			columnComboBox.setUI(new BasicComboBoxUI());
			columnComboBox.setRenderer(new ListCellRenderer<PsfJGraph>() {

				@Override
				public Component getListCellRendererComponent(
						JList<? extends PsfJGraph> list, PsfJGraph value,
						int index, boolean isSelected, boolean cellHasFocus) {
					

					String labelName = String.format("<html><b>%s</b><br><small>%s<mall><br></html>",value.getTitle(),value.getShortDescription());

					JLabel label = new JLabel(labelName);
					label.setOpaque(true);
					label.setBackground(Color.white);
					if(value.getImageIcon() != null) label.setIcon(value.getImageIcon());
					if (isSelected || cellHasFocus) {

						label.setBackground(new Color(200, 200, 255));
					}

					label.setBorder(new EmptyBorder(5, 10, 5, 5));

					return label;
				}
			});
			//statsLabel.addComponentListener(onPanelResize);
		
			tabbedPane.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					
					if(tabbedPane.getSelectedIndex() == 2) {
					
					inspectionPage.updateImage();
					inspectionPage.autoSelect();
					inspectionPage.onDisplay();
					
					
					
					}
					
					if(tabbedPane.getSelectedIndex() == 1 && canvas != null) {
						canvas.setSize(canvas.getPreferredSize());
						canvas.fitImageToCanvas();
					}
					new Thread() {
						public void run() {
							
							tabbedPane.getSelectedComponent().repaint();
						}
					}.start();
					
				}
			});
			
			statsPanel.add(table,BorderLayout.CENTER);
			
			try {
				imageLabel.setIcon(new ImageIcon(FileUtils.loadImageRessource(this, "/3d-fit-illustration.png")));
				
				}
				catch(NullPointerException e) {
					System.err.println("Couldn't load image...");
					e.printStackTrace();
				}
			
			
			assignIconToButton("generatePdfButton", "/icon-pdf.gif");
			
		
			
			ArrayList<String> list = new ArrayList<String>();
			
			
			for(Object key : UIManager.getLookAndFeelDefaults().keySet()) {
				//System.out.println(key);
				list.add(key.toString());
			}
			
			Collections.sort(list);
			for(String k : list)  if(k.contains("icon") || k.contains("Icon")) System.out.println(k);
			
			inspectionPage = new BeadInspectionPage(null, manager);
			inspectionPanel.add(inspectionPage.getComponent(),BorderLayout.CENTER);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public void updateActionComboBox() {
		
		
		actionComboBox.removeAllItems();
		actionComboBox.addItem(emptyAction);
		
		actionComboBox.addItem(onSaveAsCSV);
	
		actionComboBox.addItem(onSaveHeatMapAs);
		
		
		if(getBeadImageManager().getAnalysisType() == BeadImageManager.DUAL_CHANNEL) {
			actionComboBox.addItem(onExportLandMark);
		}
		
		actionComboBox.addItem(onPDFExport);
		actionComboBox.addItem(onSaveDataAs);
		
	

		actionComboBox.addItemListener(actionComboBoxListener);
	}
	

	/** The action combo box listener. */
	ItemListener actionComboBoxListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			

			if (e.getStateChange() == ItemEvent.DESELECTED)
				return;
			final Object o = actionComboBox.getSelectedItem();

			if (o instanceof ExportAction) {
				new Thread(){
					public void run() {
						((ExportAction) o).actionPerformed(null);
					}
				}.start();
			}

			actionComboBox.setSelectedIndex(0);

		}
	};

	/** The on column changed. */
	ItemListener onColumnChanged = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {

			// if(e.getStateChange() == ItemEvent.DESELECTED) return;
			updateView();

		}
	};

	/**
	 * Gets the fov data set.
	 *
	 * @return the fov data set
	 */
	public FovDataSet getFovDataSet() {
		fovDataSet = getBeadImageManager().getDataSet();
		return fovDataSet;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {
		
		updateActionComboBox();

		columnComboBox.removeAllItems();

		for (PsfJGraph graph : getBeadImageManager().getGraphList()) {
			columnComboBox.addItem(graph);
		}

		
		if(columnComboBox.getSelectedIndex() == -1) {
			columnComboBox.setSelectedIndex(0);
		}
		
		/*
		 * for(String item: getBeadImageManager().getHeatMapList()) {
		 * columnComboBox.addItem(item); }
		 * 
		 * for(int i=0;i!=getBeadImageManager().getBeadImageList().size();i++) {
		 * columnComboBox.addItem("beadmap_"+i); }
		 */

		getFovDataSet();
		
		normalizedButton.setSelected(false);
		micrometerButton.setSelected(true);
		//updateView();
		updateStats();
		updateView();
		
		if(getBeadImageManager().getAnalysisType() == BeadImageManager.DUAL_CHANNEL) {
			columnComboBox.setSelectedIndex(8);
		}
		table.setManager(getBeadImageManager());
		table.addHeatmaps();
		inspectionPage.onDisplay();
	}

	/**
	 * Update stats.
	 */
	public void updateStats() {

		HTMLDataSetExporter exporter = new HTMLDataSetExporter(
				getBeadImageManager());

		//statsLabel.setOpaque(true);
		//statsLabel.setBackground(Color.white);
		statsLabel.setText("<html>" + exporter.getStatistics() + "</html>");

	}

	

	/**
	 * Update view.
	 */
	public void updateView() {

		if (columnComboBox == null)
			return;

		
		
		
		PsfJGraph currentGraph = (PsfJGraph) columnComboBox.getSelectedItem();
		if (currentGraph == null)
			return;
		
		setUnitButtonVisible(FullHeatMap.class.isAssignableFrom(currentGraph.getClass()) && ! (currentGraph instanceof AsymmetryHeatMap) && ! (currentGraph instanceof ThetaHeatMap));
		
		
		setDescription(currentGraph.getDescription());
		

		setCurrentProcessor(currentGraph.getGraph(unitMode));
		
	}

	/**
	 * Sets the unit button visible.
	 *
	 * @param visible the new unit button visible
	 */
	public void setUnitButtonVisible(boolean visible) {
		micrometerButton.setEnabled(visible);
		normalizedButton.setEnabled(visible);
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof Message) {
			update((Message) arg1);
		}
		
		inspectionPage.update(arg0,arg1);
		
	}

	/**
	 * Update.
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		if (message.getName().contains("heatmap generation")) {
			
		}

	}

	/**
	 * Sets the current processor.
	 *
	 * @param ip the new current processor
	 */
	public void setCurrentProcessor(ImageProcessor ip) {
		if (ip != null) {
			if (canvas == null) {
				canvas = new KnopImageCanvas();
				canvas.setSize(700, 300);
				canvas.setResetOnImageChange(true);
				canvas.setFittingMode(KnopImageCanvas.FIT_TO_SMALLER_DIMENSION);
				canvas.setEnableOpening(false);
				canvas.setMode(KnopImageCanvas.MOVE_MODE);
				
				canvasPanel.add(canvas, BorderLayout.CENTER);
				canvasPanel.addComponentListener(new ComponentListener() {
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
				});;
				
				canvas.setSize(canvas.getPreferredSize());
				canvas.setImage(ip);
				canvas.fitImageToCanvas();
				canvas.repaint();
				System.out.println("Creating canvas !!!");

			} else {
				//System.out.println("repainting : " + ip);

				canvas.setImage(ip);
				
				canvas.repaint();

			}
		}
	}

	
	
	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		
		return true;
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

	/** The on save heat map as. */
	public ExportAction onSaveHeatMapAs = new ExportAction("Heatmaps","/icon-img.png") {
		public String toString() {
			return "Heatmaps";
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String path = FileUtils.getDirectory("Select the export folder ",
					getBeadImageManager().getExportDirectory());

			System.out.println(path);
			if (path != null) {
				setLastFolder(path);
				getBeadImageManager().setExportDirectory(path);
				
				
				getBeadImageManager().exportHeatMaps(true);
				
				
			}

		}
	};

	/** The on pdf export. */
	public ExportAction onPDFExport = new ExportAction("Bead reports PDF file.","/icon-pdf.gif") {

		

		@Override
		public void actionPerformed(ActionEvent arg0) {
			

		
					String path = FileUtils.getDirectory(
							"Choose the export folder", getBeadImageManager()
									.getExportDirectory());
					if (path == null)
						return;
					
					setLastFolder(path);
					System.out.println(path);
					getBeadImageManager().setExportDirectory(path);
					getBeadImageManager().exportInPDF();
			
		}
	};

	/** The on export land mark. */
	public ExportAction onExportLandMark = new ExportAction("Landmark XLS file","/icon_xls.png") {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getBeadImageManager().getAnalysisType() == beadImageManager.DUAL_CHANNEL) {
				SaveDialog sd = new SaveDialog("Save Landmark file...",
						getLastFolder(), getBeadImageManager()
								.getAnalysisName() + "_landmark", ".xls");

				if (sd.getDirectory() == null)
					return;

				String path = sd.getDirectory() + sd.getFileName();
				setLastFolder(sd.getDirectory());

				FovDataSet dataSet = new FovDataSet();
				dataSet.setSeparator("\t");
				int count = 0;
				
				for (BeadFrame frame : getBeadImageManager().getBeadImage(0)
						.getBeadFrameList().getOnlyValidBeads().getWithAlterEgo()) {
					
					
					dataSet.addValue("index", count++);
					//dataSet.addValue("bead2", frame.getAlterEgo().getId());

					dataSet.addValue("xSource", frame.getWeightedXInImage());
					dataSet.addValue("ySource", frame.getWeightedYInImage());
					dataSet.addValue("xTarget", frame.getAlterEgo()
							.getWeightedXInImage());
					dataSet.addValue("yTarget", frame.getAlterEgo()
							.getWeightedYInImage());
					
				}

				dataSet.exportToCrappyFormat(path);
				FileUtils.openFolder(path);
			}

		}

		

	};

	/** The on save data as. */
	public ExportAction onSaveDataAs = new ExportAction("All above(long)","/icon-save.png") {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String path = FileUtils.getDirectory("Choose the export folder",
					getBeadImageManager().getExportDirectory());

			if (path == null)
				return;

			getBeadImageManager().setExportDirectory(path);

			new Thread() {
				public void run() {
					getBeadImageManager().export();
				}
			}.start();

		}

		public String toString() {
			return "All above (long)";
		}
	};

	/** The empty action. */
	public ExportAction emptyAction = new ExportAction("...",null) {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
		}
		
	};
	
	
	/** The on save as csv. */
	public ExportAction onSaveAsCSV = new ExportAction("Results CSV file.","/icon-csv.gif") {
		
		

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(getBeadImageManager().getAnalysisType() == BeadImageManager.SINGLE_CHANNEL) {
			SaveDialog sd = new SaveDialog("Save CSV file...", getLastFolder(),
					getBeadImageManager().getAnalysisName(), ".csv");

			if (sd.getDirectory() == null)
				return;

			String path = sd.getDirectory() + sd.getFileName();
			setLastFolder(sd.getDirectory());
			getBeadImageManager().exportCSVFile(path,true);
			}
			else {
				String path = FileUtils.getDirectory("Select CSV export directory", getBeadImageManager().getExportDirectory());
				if(path != null) {
					getBeadImageManager().setExportDirectory(path);
					getBeadImageManager().exportCSVFile(true);
					setLastFolder(path);
				}
			}
		}
	};

	/** The on save as excel. */
	public AbstractAction onSaveAsExcel = new AbstractAction() {
		public String toString() {
			return "Excel sheet.";
		}
		
		public void actionPerformed(ActionEvent arg0) {
			SaveDialog sd = new SaveDialog("Save CSV file...", getLastFolder(),
					getBeadImageManager().getAnalysisName(), ".xls");

			if (sd.getDirectory() == null)
				return;

			String path = sd.getDirectory() + sd.getFileName();
			setLastFolder(sd.getDirectory());
			//getBeadImageManager().exportXLSFile(path);
			FileUtils.openFolder(path);
		}
		
	};
	
	
	/** The on save as htm l5. */
	public AbstractAction onSaveAsHTML5 = new AbstractAction() {

		@Override
		public String toString() {
			return "HTML5 Data Analyzer (useful for plotting)";
		}

		public void actionPerformed(ActionEvent arg0) {

			SaveDialog sd = new SaveDialog("Save HTML5 Data Analyzer...",
					getLastFolder(), "html5_analyzer", ".html");

			if (sd.getDirectory() == null)
				return;

			String path = sd.getDirectory() + sd.getFileName();
			setLastFolder(sd.getDirectory());

			HTML5AnalyzerExporter exporter = new HTML5AnalyzerExporter(
					getBeadImageManager());
			exporter.saveHTMLReportAs(path);

		}
	};

	/** The on restart. */
	public AbstractAction onRestart = new AbstractAction() {

		public String toString() {
			return "Restart Analysis";
		}

		public void actionPerformed(ActionEvent arg0) {
			
			notifyObservers(new Message(this, "change page request",
					new Integer(0)));
		}
	};

	/** The on quick pdf sum up. */
	public ExportAction onQuickPDFSumUp = new ExportAction("Export a quick PDF sum-up","/icon-pdf.png") {

		@Override
		public void actionPerformed(ActionEvent arg0) {
				new Thread() {
					public void run() {
						getBeadImageManager().exportPDFSumUp();
					}
				}.start();
		}
	};

	/** The on include data set. */
	public AbstractAction onIncludeDataSet = new AbstractAction() {

		public String toString() {
			return "Append existing analysis (data.txt) to the data set";
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			OpenDialog od = new OpenDialog("Merge previous analysis...",
					getLastFolder(), "data.csv");
			if (od.getDirectory() == null)
				return;
			String path = od.getDirectory() + od.getFileName();
			setLastFolder(od.getDirectory());
			getBeadImageManager().mergeDataSet(new File(path));
			updateView();
			updateStats();

		}

	};

	
	/** The is full screen. */
	boolean isFullScreen = false;
	
	/** The on fullscreen. */
	public AbstractAction onFullscreen = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			/*
			 * 
			 * statsScrollPane.setVisible(!fullscreenButton.isSelected());
			 * //statsLabel.setVisible(); mainPanel.updateUI();
			 */
			
			
			descriptionScrollPane.setVisible(isFullScreen);
			//statsScrollPane.setVisible(isFullScreen);
			
			if(isFullScreen) {
				
				
				fullScreenFrame.remove(mainPanel2);
				
				mainPanel.add(mainPanel2);
				
				fullScreenFrame.dispose();
				fullScreenFrame = null;
				
				fullscreenButton.setText("Display on full screen");
				isFullScreen = false;
			}
			
			else {
				
			fullscreenButton.setText("Quit full-screen mode");
			fullScreenFrame = new JFrame("Test");
			fullScreenFrame.setUndecorated(true);
			fullScreenFrame.setLayout(new BorderLayout(10,10));
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			
			
			
			mainPanel.remove(mainPanel2);
			fullScreenFrame.add(mainPanel2);
			
			
			gs.setFullScreenWindow(fullScreenFrame);
			fullScreenFrame.validate();
			updateView();
			isFullScreen = true;
			}
			
			
			/*
			frame.add(new KnopImageCanvas(getWidth(),getHeight())new Component() {
				BufferedImage img = canvas.getImage().getBufferedImage();

				@Override
				public void paint(Graphics g) {
					super.paint(g);

					g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
				}
			});*/

			
			
			

		}
	};



	
	/**
	 * Sets the description.
	 *
	 * @param text the new description
	 */
	public void setDescription(String text) {
		descriptionTextPane.setText("<html><div style='width:200px'>"+text+"</div></html>");
	}
	
	
	
	/** The on micrometer button pressed. */
	public Action onMicrometerButtonPressed = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			unitMode = PsfJGraph.NOT_NORMALIZED;
			updateView();
			
		}
	};
	
	/** The on normalized button pressed. */
	public Action onNormalizedButtonPressed = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			unitMode = PsfJGraph.NORMALIZED;
			updateView();
			
		}
	};
	

	/**
	 * Gets the last folder.
	 *
	 * @return the last folder
	 */
	public String getLastFolder() {
		// TODO Auto-generated method stub
		if (lastFolder == null)
			lastFolder = getBeadImageManager().getExportDirectory();
		return lastFolder;
	}

	/**
	 * Sets the last folder.
	 *
	 * @param path the new last folder
	 */
	public void setLastFolder(String path) {
		lastFolder = path;
	}

	
	
	/**
	 * The Class ExportAction.
	 */
	private abstract class ExportAction extends AbstractAction{
		
		/** The icon path. */
		String iconPath;
		
		/** The title. */
		String title;
		
		/**
		 * Instantiates a new export action.
		 *
		 * @param title the title
		 * @param iconPath the icon path
		 */
		public ExportAction(String title, String iconPath) {
			this.iconPath = iconPath;
			this.title = title;
		}
		
		/**
		 * Sets the icon path.
		 *
		 * @param path the new icon path
		 */
		public void setIconPath(String path) {
			iconPath = path;
		}
	
		
		/** The label. */
		JLabel label;
		
		/**
		 * Gets the component.
		 *
		 * @return the component
		 */
		public Component getComponent() {
			if(label != null) return label;
			label = new JLabel();
			label.setText(toString());
			label.setBorder(new EmptyBorder(5,5,5,5));
			label.setOpaque(true);
			label.setIconTextGap(10);
			
			if(iconPath!=null)
				try {
				assignIconToLabel(label, iconPath);
				}
			catch(Exception e) {
				System.err.println("Well someone messed up with the image "+iconPath);
			}
			return label;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public abstract void actionPerformed(ActionEvent arg0);
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return title;
		}
	}
	
	
}
