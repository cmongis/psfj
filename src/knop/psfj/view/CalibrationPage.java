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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.FileUtils;
import knop.psfj.utils.MathUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class CalibrationPage.
 */
public class CalibrationPage extends WizardPage {

	/** The microscope type combo box. */
	JComboBox microscopeTypeComboBox;

	/** The lambda text field. */
	JTextField lambdaTextField;

	/** The na text field. */
	JTextField naTextField;

	/** The bid size text field. */
	JTextField beadDiameterTextField;

	/** The unit combo box. */
	JComboBox unitComboBox;

	/** The pixel width text field. */
	JTextField pixelWidthTextField;

	/** The pixel height text field. */
	JTextField pixelHeightTextField;

	/** The pixel depth text field. */
	JTextField pixelDepthTextField;

	/** The camera pixel size text field. */
	JTextField cameraPixelSizeTextField;

	/** The space between stacks text field. */
	JTextField spaceBetweenStacksTextField;

	/** The microscope id text field. */
	JTextField microscopeIdTextField;

	/** The microscope id label. */
	JLabel microscopeIdLabel;

	/** The help text label. */
	JLabel helpTextLabel;

	/** The help title label. */
	JLabel helpTitleLabel;

	/** The refraction indice text field. */
	JTextField refractionIndiceTextField;

	/** The main panel. */
	JPanel mainPanel;

	/** The microscope. */
	Microscope microscope = new Microscope();

	/** The is valid. */
	boolean isValid = false;

	/** The fwhm xy label. */
	JLabel fwhmXYLabel;

	/** The fwhm z label. */
	JLabel fwhmZLabel;

	/** The resolution label. */
	JLabel resolutionLabel;

	/** The lambda label. */
	JLabel lambdaLabel;

	/** The bid size label. */
	JLabel beadDiameterLabel;

	/** The bead diam over fwhmxy. */
	JLabel beadDiamOverFWHMXY;

	/** The camera pixel size label. */
	JLabel cameraPixelSizeLabel;

	/** The space between stacks label. */
	JLabel spaceBetweenStacksLabel;

	/** The lock. */
	Object lock = new Object();

	/** The is advanced mode. */
	boolean isAdvancedMode = false;

	/** The advanded mode radio button. */
	JRadioButton advandedModeRadioButton;

	/** The easy mode radio button. */
	JRadioButton easyModeRadioButton;

	/** The magnification combo box. */
	JTextField magnificationComboBox;

	/** The image label. */
	JLabel imageLabel;

	/**
	 * The main method.
	 * 
	 * @param args
	 *           the arguments
	 */
	public static void main(String[] args) {

		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();

		// manager.add("/Users/cyril/test_img/6/6.tif_bead_535.tif");
		manager.add("/Users/cyril/test_img/6/6_dual_color/6_gfp copy 3.tif");
		manager.add("/Users/cyril/test_img/6/6_dual_color/6_gfp copy 4.tif");
		//manager.setAnalysisType(BeadImageManager.DUAL_CHANNEL);
		//manager.getBeadImage(0).setWaveLength(0.625);
		//manager.getBeadImage(1).setWaveLength(0.525);
		manager.addObserver(wizardWindow);

		wizardWindow.addPage(new CalibrationPage(manager));
		wizardWindow.addPage(new FocusChooserPage(manager));
		wizardWindow.setCurrentPage(0);

		wizardWindow.show();

	}

	/**
	 * Instantiates a new calibration page.
	 */
	public CalibrationPage() {
		super();

		setTitle("Image stack calibration");
		setExplaination("Please enter all necessary informations.");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// engine.getTaglib().registerTag(":-", JPlanSelector.class);

			engine.render("knop/psfj/view/CalibrationPage.xml").setVisible(true);

			mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			naTextField.addKeyListener(doubleValidator);
			lambdaTextField.addKeyListener(doubleValidator);
			beadDiameterTextField.addKeyListener(doubleValidator);
			refractionIndiceTextField.addKeyListener(doubleValidator);

			cameraPixelSizeTextField.addKeyListener(doubleValidator);
			// cameraPixelSizeTextField.addKeyListener(onCalibrationChanged);
			cameraPixelSizeTextField
					.addMouseListener(onAutoCalibrationFieldFocused);

			spaceBetweenStacksTextField.addKeyListener(doubleValidator);
			// spaceBetweenStacksTextField.addKeyListener(onCalibrationChanged);
			spaceBetweenStacksTextField
					.addMouseListener(onAutoCalibrationFieldFocused);

			magnificationComboBox.addKeyListener(doubleValidator);
			// magnificationComboBox.addKeyListener(onCalibrationChanged);
			magnificationComboBox.addMouseListener(onAutoCalibrationFieldFocused);

			pixelWidthTextField.addKeyListener(doubleValidator);
			pixelWidthTextField.addKeyListener(onAdvancedCalibrationChanged);
			pixelWidthTextField.addMouseListener(onCalibrationFieldFocused);

			pixelHeightTextField.addKeyListener(doubleValidator);
			pixelHeightTextField.addKeyListener(onAdvancedCalibrationChanged);
			pixelHeightTextField.addMouseListener(onCalibrationFieldFocused);

			pixelDepthTextField.addKeyListener(doubleValidator);
			pixelDepthTextField.addKeyListener(onAdvancedCalibrationChanged);
			pixelDepthTextField.addMouseListener(onCalibrationFieldFocused);

			// microscopeIdTextField.setInputVerifier(textVerifier);
			microscopeIdTextField.addKeyListener(textValidator);

			advandedModeRadioButton.addMouseListener(onCalibrationFieldFocused);
			easyModeRadioButton.addMouseListener(onAutoCalibrationFieldFocused);

			try {
				imageLabel.setIcon(new ImageIcon(FileUtils.loadImageRessource(this,
						"/fwhm.png")));
				imageLabel.setHorizontalAlignment(JLabel.RIGHT);
			} catch (NullPointerException e) {
				System.err.println("Couldn't load image...");
				e.printStackTrace();
			}
			installHelp();
			helpTitleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
			helpTextLabel.setPreferredSize(new Dimension(500, 60));
			helpTextLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
			resizeFrame();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {

		init();

	}

	/**
	 * Instantiates a new calibration page.
	 * 
	 * @param manager
	 *           the manager
	 */
	public CalibrationPage(BeadImageManager manager) {
		this();
		setBeadImageManager(manager);
		// setMicroscope(manager.getMicroscope());
	}

	/**
	 * Checks if is float equal to zero.
	 * 
	 * @param number
	 *           the number
	 * @return true, if is float equal to zero
	 */
	public boolean isFloatEqualToZero(String number) {
		try {
			if (Double.parseDouble(number) <= 0.0)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if is double right.
	 * 
	 * @param text
	 *           the text
	 * @return true, if is double right
	 */
	public boolean isDoubleRight(String text) {
		Matcher m = doublePattern.matcher(text);
		return m.matches() && !isFloatEqualToZero(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#init()
	 */
	public void init() {

		if (microscope == null)
			microscope = new Microscope();

		// if a manager is set
		if (getBeadImageManager() != null) {
			// if the microscope configuration of the first is valid
			for (BeadImage image : getBeadImageManager().getBeadImageList()) {
				// the first valid configuration met is merged
				if (image.getMicroscope().isConfigurationValid()) {
					// then you merge it
					microscope.mergeFromOtherMicroscope(image.getMicroscope(), true);
					break;
				}
				
			}

		}

		isAdvancedMode = microscope.isAdvancedMode();

		updateFields();
		updateResolution();

		updateCalibrationView();
		updateMode();

		checkDoubleFields();

		resizeFrame();
	}

	/**
	 * Update fields.
	 */
	public void updateFields() {
		naTextField.setText(formatDoubleForField(microscope.getNA()));
		lambdaTextField
				.setText(formatDoubleForField(microscope.getWaveLength() * 1000));
		beadDiameterTextField.setText(formatDoubleForField(MathUtils.round(microscope
				.getBeadDiameter() * 1000)));
		refractionIndiceTextField.setText(formatDoubleForField(microscope
				.getRefractionIndex()));

		pixelDepthTextField.setText(new Double(
				microscope.getCalibration().pixelDepth).toString());
		pixelHeightTextField.setText(new Double(
				microscope.getCalibration().pixelHeight).toString());
		pixelWidthTextField.setText(new Double(
				microscope.getCalibration().pixelWidth).toString());

		// microscopeIdTextField.setText(microscope.getIdentifier());

	}

	/**
	 * Resize frame.
	 */
	public void resizeFrame() {
		// mainFrame.setSize(mainFrame.getPreferredSize());
		// mainFrame.setSize(1000, 600);
	}

	/**
	 * Gets the na.
	 * 
	 * @return the na
	 */
	public double getNA() {
		return parseDouble(naTextField.getText());
	}

	/**
	 * Gets the wave length.
	 * 
	 * @return the wave length
	 */
	public double getWaveLength() {

		return parseDouble(lambdaTextField.getText()) / 1000.0;
	}

	/**
	 * Gets the microscope type.
	 * 
	 * @return the microscope type
	 */
	public int getMicroscopeType() {
		return 0;
	}

	/**
	 * Gets the refraction indice.
	 * 
	 * @return the refraction indice
	 */
	public double getRefractionIndice() {

		return parseDouble(refractionIndiceTextField.getText());
	}

	public double getBeadDiameter() {
		System.out.println(beadDiameterTextField.getText() + " becomes "+parseDouble(beadDiameterTextField.getText()));
		return parseDouble(beadDiameterTextField.getText()) / 1000;
	}

	public String getMicroscopeId() {
		return microscopeIdTextField.getText();
	}

	public double getCameraPixelSize() {
		return parseDouble(cameraPixelSizeTextField.getText());
	}

	public double getMagnification() {
		return parseDouble(magnificationComboBox.getText());
	}

	public double getStackGap() {
		return parseDouble(spaceBetweenStacksTextField.getText());
	}

	/**
	 * The Class ComboBoxModel.
	 */
	public class ComboBoxModel extends DefaultComboBoxModel {

		/**
		 * Instantiates a new combo box model.
		 */
		public ComboBoxModel() {
			super(new Object[]{"Confocal", "Widefield"});
		}
	}

	public void updateModel() {

		microscope.setBeadDiameter(getBeadDiameter());
		microscope.setWaveLength(getWaveLength());
		microscope.setIdentifier(getMicroscopeId());
		microscope.setRefraction(getRefractionIndice());
		microscope.setNA(getNA());

		microscope.setCameraPixelSize(getCameraPixelSize());
		microscope.setMagnification(getMagnification());
		microscope.setSpaceBetweenStacks(getStackGap());

		if (isAdvancedMode()) {
			microscope.getCalibration().pixelWidth = parseDouble(pixelWidthTextField
					.getText());
			microscope.getCalibration().pixelHeight = parseDouble(pixelHeightTextField
					.getText());
			microscope.getCalibration().pixelDepth = parseDouble(pixelDepthTextField
					.getText());
		} else {
			microscope.calculateCalibrationFromPixelSize();
			updateCalibrationView();
		}
	}

	public double parseDouble(String doubleString) {
		try {
			return Double.parseDouble(doubleString);

		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Update resolution.
	 */
	public void updateResolution() {

		if (!checkDoubleField(naTextField)
				|| !checkDoubleField(refractionIndiceTextField)
				|| !checkDoubleField(beadDiameterTextField))
			return;

		if (!getBeadImageManager().isDualColorAnalysis()
				&& checkDoubleField(lambdaTextField) == false)
			return;

		String beadOverFWHM;

		if (getBeadImageManager().getAnalysisType() == beadImageManager.SINGLE_CHANNEL) {
			fwhmXYLabel.setText(microscope.formatDouble(microscope
					.getXYTheoreticalResolution()));
			fwhmZLabel.setText(microscope.formatDouble(microscope
					.getZTheoreticalResolution()));
			beadOverFWHM = MathUtils.roundToString(
					(microscope.getBeadDiameter() / microscope
							.getXYTheoreticalResolution()), 3);
		} else {
			Microscope m1 = new Microscope(microscope);
			m1.setWaveLength(getBeadImageManager().getMicroscope(0)
					.getWaveLength());
			Microscope m2 = new Microscope(microscope);
			m2.setWaveLength(getBeadImageManager().getMicroscope(1)
					.getWaveLength());

			fwhmXYLabel.setText(String.format("%s / %s",
					m1.formatDouble(m1.getXYTheoreticalResolution()),
					m2.formatDouble(m2.getXYTheoreticalResolution())));

			fwhmZLabel.setText(String.format("%s / %s",
					m1.formatDouble(m1.getZTheoreticalResolution()),
					m2.formatDouble(m2.getZTheoreticalResolution())));

			beadOverFWHM = String.format("%.3f / %.3f",
					m1.getBeadDiameter() / m1.getTheoreticalResolution(0),
					m2.getBeadDiameter() / m2.getTheoreticalResolution(0));

		}

		beadDiamOverFWHMXY.setText(beadOverFWHM);
	}

	/**
	 * Gets the label.
	 * 
	 * @param id
	 *           the id
	 * @return the label
	 */
	public JLabel getLabel(String id) {
		return (JLabel) engine.find(id);
	}

	/**
	 * Gets the unit.
	 * 
	 * @return the unit
	 */
	public String getUnit() {
		return microscope.getUnit();
	}

	/**
	 * Calibrate.
	 */
	public void calibrate() {

		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Update mode.
	 */
	public void updateMode() {
		System.out.println("Updating mode...");
		HashMap<Boolean, Color> colorMap = new HashMap<Boolean, Color>();
		colorMap.put(new Boolean(true), Color.black);
		colorMap.put(new Boolean(false), Color.gray);

		boolean mode = isAdvancedMode();

		easyModeRadioButton.setSelected(!mode);
		advandedModeRadioButton.setSelected(mode);

		pixelDepthTextField.setEditable(mode);
		pixelHeightTextField.setEditable(mode);
		pixelWidthTextField.setEditable(mode);
		spaceBetweenStacksTextField.setEditable(!mode);
		cameraPixelSizeTextField.setEditable(!mode);
		magnificationComboBox.setEnabled(!mode);

		pixelDepthTextField.setForeground(colorMap.get(mode));
		pixelHeightTextField.setForeground(colorMap.get(mode));
		pixelWidthTextField.setForeground(colorMap.get(mode));

		spaceBetweenStacksTextField.setForeground(colorMap.get(!mode));
		cameraPixelSizeTextField.setForeground(colorMap.get(!mode));
		magnificationComboBox.setForeground(colorMap.get(!mode));

		getLabel("pixelDepthLabel").setForeground(colorMap.get(mode));
		getLabel("pixelHeightLabel").setForeground(colorMap.get(mode));
		getLabel("pixelWidthLabel").setForeground(colorMap.get(mode));

		getLabel("cameraPixelSizeLabel").setForeground(colorMap.get(!mode));
		getLabel("spaceBetweenStacksLabel").setForeground(colorMap.get(!mode));
		getLabel("magnificationLabel").setForeground(colorMap.get(!mode));

	}

	/**
	 * Update calibration view.
	 */
	public void updateCalibrationView() {

		updateMode();
		System.out.println("Updating calibration view...");

		microscopeIdTextField.setText(microscope.getIdentifier());

		// if the user doesn't know the image calibration
		if (!isAdvancedMode()) {

			// if microscope informations are not complete then the calibration
			// cannot be calculated
			if (microscope.getCameraPixelSize() == 0
					|| microscope.getSpaceBetweenStacks() == 0
					|| microscope.getMagnification() == 0) {
				pixelWidthTextField.setText("??");
				pixelHeightTextField.setText("??");
				pixelDepthTextField.setText("??");

			} else {

				// else the parameters has been calculated and should be displayed
				pixelDepthTextField.setText(formatDoubleForField(microscope
						.getCalibration().pixelDepth));
				pixelHeightTextField.setText(formatDoubleForField(microscope
						.getCalibration().pixelHeight));
				pixelWidthTextField.setText(formatDoubleForField(microscope
						.getCalibration().pixelWidth));

				magnificationComboBox.setText(""
						+ MathUtils.round(microscope.getMagnification()));

			}
		}

		// if the user knows the calibration
		else {

			cameraPixelSizeTextField.setText(formatDoubleForField(microscope
					.getCameraPixelSize()));
			spaceBetweenStacksTextField.setText(formatDoubleForField(microscope
					.getSpaceBetweenStacks()));
			magnificationComboBox.setText(formatDoubleForField(microscope
					.getMagnification()));

			pixelDepthTextField.setText(formatDoubleForField(microscope
					.getCalibration().pixelDepth));
			pixelHeightTextField.setText(formatDoubleForField(microscope
					.getCalibration().pixelHeight));
			pixelWidthTextField.setText(formatDoubleForField(microscope
					.getCalibration().pixelWidth));

		}

		// in case of dual channel configuraiton, we show two wavelengthes
		if (getBeadImageManager().getAnalysisType() == BeadImageManager.DUAL_CHANNEL) {
			lambdaTextField.setEnabled(false);
			BeadImageManager manager = getBeadImageManager();
			lambdaTextField.setText(manager.getMicroscope(0)
					.getWaveLengthAsString()
					+ " / "
					+ manager.getMicroscope(1).getWaveLengthAsString());
		} else {
			lambdaTextField.setEnabled(true);
		}
	}

	private String formatDoubleForField(double d) {

		if (d == 0.0)
			return "";

		else if (d > 10 && d - MathUtils.round(d) == 0.0)
			return MathUtils.roundToString(d, 0);

		else
			return new Double(d).toString();
	}

	/**
	 * Checks if is advanced mode.
	 * 
	 * @return true, if is advanced mode
	 */
	public boolean isAdvancedMode() {

		return isAdvancedMode;

	}

	/**
	 * Insert help.
	 * 
	 * @param comp
	 *           the comp
	 * @param title
	 *           the title
	 * @param text
	 *           the text
	 */
	public void insertHelp(JComponent comp, final String title, final String text) {

		comp.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				setHelp("", "");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				setHelp(title, text);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * Insert help.
	 * 
	 * @param title
	 *           the title
	 * @param text
	 *           the text
	 * @return the mouse listener
	 */
	public MouseListener insertHelp(final String title, final String text) {

		return new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				setHelp("", "");
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

				setHelp(title, text);
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {

			}
		};
	}

	/**
	 * Sets the help.
	 * 
	 * @param name
	 *           the name
	 * @param text
	 *           the text
	 */
	public void setHelp(String name, String text) {

		helpTextLabel.setText("<html>" + text + "</html>");
		helpTitleLabel.setText(name);
		resizeFrame();
	}

	/**
	 * Insert help.
	 * 
	 * @param target
	 *           the target
	 * @param title
	 *           the title
	 * @param text
	 *           the text
	 */
	public void insertHelp(String target, String title, String text) {
		String[] targets = target.split(",");
		for (String t : targets) {
			JComponent c = (JComponent) engine.find(t);
			if (c != null)
				insertHelp(c, title, text);
			resizeFrame();
		}
	}

	/**
	 * Sets the valid.
	 * 
	 * @param isValid
	 *           the new valid
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;

		if (isValid == true) {
			microscope.save();
		}

		if (getBeadImageManager() != null) {
			getBeadImageManager().update(getBeadImageManager(), this);
		}
	}

	/**
	 * Install help.
	 */
	public void installHelp() {

		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file

			Document dom;

			try {

				dom = db.parse("src/MicroscopeEditorHelp.xml");

			} catch (FileNotFoundException e) {
				dom = db.parse(this.getClass().getResourceAsStream(
						"/MicroscopeEditorHelp.xml"));
			}

			if (dom == null) {
				IJ.error("Could load help file");
				return;
			}

			NodeList list = dom.getElementsByTagName("instruction");

			for (int i = 0; i != list.getLength(); i++) {

				try {

					String title = ((Element) list.item(i))
							.getElementsByTagName("title").item(0).getFirstChild()
							.getTextContent();

					System.out.println(title);

					String target = ((Element) list.item(i))
							.getElementsByTagName("target").item(0).getFirstChild()
							.getTextContent();
					String description = ((Element) list.item(i))
							.getElementsByTagName("description").item(0)
							.getFirstChild().getTextContent();

					if (description == null)
						description = "";
					if (title == null)
						title = "";
					if (target == null)
						continue;

					description = description.replace("\\n", "<br>");

					insertHelp(target, title, description);

				} catch (NullPointerException e) {
					System.out.println("Couldn't handle help " + list.item(i));
				}

			}

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {

		return isValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#isBackPossible()
	 */
	@Override
	public boolean isBackPossible() {

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#isQuitOkay()
	 */
	@Override
	public boolean isQuitOkay() {

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {

		return mainPanel;
	}

	public boolean askBeforeLeavingForward() {
		System.out.println("Asking...");
		if (getBeadImageManager().isThereMicroscopeDataOverriding(microscope)) {
			int result = JOptionPane
					.showConfirmDialog(
							mainPanel,
							"Clicking on \"OK\" will override the image calibration.\nAre you sure you want to continue ?",
							"Warning", JOptionPane.WARNING_MESSAGE);
			return result == JOptionPane.OK_OPTION;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.view.WizardPage#onLeavingForward()
	 */
	@Override
	public void onLeavingForward() {
		System.out.println("onLeaving");
		getBeadImageManager().updateMicroscope(microscope);
		// getBeadImageManager().autoThreshold();
		// getBeadImageManager().autoBeadEnlargementFactor();
		//getBeadImageManager().resetPreview();
		getBeadImageManager().verifyBeadImageParameters();

	}

	public boolean checkDoubleFields() {

		ArrayList<Boolean> results = new ArrayList<Boolean>();

		results.add(textPattern.matcher(microscopeIdTextField.getText())
				.matches());

		if (isAdvancedMode()) {
			results.add(checkDoubleField(pixelHeightTextField));
			results.add(checkDoubleField(pixelWidthTextField));
			results.add(checkDoubleField(pixelDepthTextField));
		} else {
			results.add(checkDoubleField(spaceBetweenStacksTextField));
			results.add(checkDoubleField(cameraPixelSizeTextField));
			results.add(checkDoubleField(magnificationComboBox));
		}
		results.add(checkDoubleField(beadDiameterTextField));

		if (!getBeadImageManager().isDualColorAnalysis())
			results.add(checkDoubleField(lambdaTextField));
		results.add(checkDoubleField(naTextField));

		results.add(checkDoubleField(refractionIndiceTextField));

		for (boolean result : results) {
			if (!result) {
				setValid(false);
				return false;
			}
		}
		setValid(true);
		return true;
	}

	private boolean checkDoubleField(JTextField textField) {
		String text = textField.getText();// + arg0.getKeyChar();

		if (isDoubleRight(text)) {
			textField.setForeground(Color.black);

			return true;
			// textField.setText(text);

		} else {
			textField.setForeground(new Color(210, 0, 0));

			return false;
		}
	}

	/** The double validator. */
	private KeyListener doubleValidator = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent arg0) {

		}

		@Override
		public void keyReleased(KeyEvent arg0) {

			// JTextField textField = (JTextField) arg0.getSource();

			checkDoubleFields();
			updateModel();
			updateResolution();
		}

		@Override
		public void keyPressed(KeyEvent arg0) {

		}
	};

	/** The text validator. */
	private KeyListener textValidator = new KeyListener() {

		int maxlength = 50;

		@Override
		public void keyTyped(KeyEvent e) {
			JTextField textField = (JTextField) e.getSource();
			if (textField.getText().length() > maxlength)
				e.consume();// textField.setText(textField.getText().substring(0,
								// maxlength-1));
			if (!textPattern.matcher(textField.getText() + e.getKeyChar())
					.matches()) {
				e.consume();
				JOptionPane
						.showMessageDialog(null,
								"The following characters are forbidden for this field : = \\ / #");
				setHelp(
						"Error",
						"<font color='red'>The following characters are forbidden for this field : = \\ / #</font>");

			}

		}

		@Override
		public void keyReleased(KeyEvent e) {
			JTextField textField = (JTextField) e.getSource();

		}

		public void keyPressed(KeyEvent e) {
			JTextField textField = (JTextField) e.getSource();

		}
	};

	/** The double pattern. */
	private Pattern doublePattern = Pattern.compile("^\\d{0,}\\.{0,1}\\d{1,}$");

	/** The text pattern. */
	private Pattern textPattern = Pattern.compile("[^\\#\\=\\n\\\\\\/]*");

	/** The validate window. */
	public Action validateWindow = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// mainFrame.show(false);
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	};

	private KeyListener onAdvancedCalibrationChanged = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {

			microscope.setCameraPixelSize(0);
			microscope.setSpaceBetweenStacks(0);
			microscope.setMagnification(0);

			magnificationComboBox.setText("");
			cameraPixelSizeTextField.setText("");
			spaceBetweenStacksTextField.setText("");

		}

		@Override
		public void keyPressed(KeyEvent e) {

		}
	};

	/** The on mode changed. */
	public Action onModeChanged = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("onModeChanged");
			updateMode();
			updateCalibrationView();
		}
	};

	/** The on auto calibration field focused. */
	public MouseListener onAutoCalibrationFieldFocused = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			isAdvancedMode = false;
			updateCalibrationView();
			// updateMode();
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}
	};

	/** The on calibration field focused. */
	MouseListener onCalibrationFieldFocused = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			isAdvancedMode = true;
			updateCalibrationView();
			// updateMode();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	};

}
