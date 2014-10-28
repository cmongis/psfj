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
package knop.psfj.resolution;




import ij.measure.Calibration;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.prefs.Preferences;

import knop.psfj.PSFj;
import knop.psfj.utils.IniFile;
import knop.psfj.utils.MathUtils;

public class Microscope {

	public static String MICROSCOPE_SECTION = "microscope";
	public static String CALIBRATION_SECTION = "pixel";

	public static String PIXEL_WIDTH_KEY = "width";
	public static String PIXEL_HEIGHT_KEY = "height";
	public static String PIXEL_DEPTH_KEY = "depth";

	public static String PIXEL_SIZE_KEY = "cal_pixel_size";
	public static String MAGNIFICATION_KEY = "magnification";
	public static String STEP_SIZE_KEY = "step_size";

	public static String BEAD_DIAMETER_KEY = "beadsize";
	public static String RI_KEY = "ri";
	public static String NA_KEY = "na";

	public static String WAVELENGTH_KEY = "wavelength";

	public static String UNIT_KEY = "unit";

	public static String IDENTIFIER_KEY = "id";



	public static final String DEFAULT_UNIT = new String(MathUtils.MICROMETERS);
	
	
	/** Stores the type of microscope **/
	public int microscope = 0;

	/** Stores the emission wavelength (nm) **/
	protected double wavelength = 0;

	/** Stores the numerical aperture **/
	protected double NA = 0;


	protected double refraction = 1.0;
	/** Stores the calibration **/
	protected Calibration cal = null;

	/** Stores the calculated resolutions **/
	public double[] resolution = new double[3];

	/**
	 * Stores the header of the report, containing the informations about the
	 * microscope setup
	 **/
	public String reportHeader = "";

	/** Stores the date of creation of the report **/
	public String date = "";

	/** Stores the informations about the sample **/
	public String sampleInfos = "";

	/** Stores the comment field **/
	public String comments = "";

	protected double beadDiameter;

	protected double magnification;
	protected double additionalMagnification;

	protected double spaceBetweenStacks;
	protected double pixelSize;

	private IniFile config;

	public static int X = 0;
	public static int Y = 1;
	public static int Z = 2;

	String identifier = "";

	String unit = DEFAULT_UNIT;
	
	/**
	 * Creates a new microscope object, used to store informations about the
	 * microscope and the sample.
	 *
	 * @param cal the cal
	 * @param microscope            microscope type (0: wide-field, 1: confocal)
	 * @param wavelength            emission wavelength (nm)
	 * @param NA            numerical aperture
	 * @param refraction the refraction
	 * @param sampleInfos            sample informations to be displayed on the report
	 * @param comments            comments to be displayed on the report
	 */
	public Microscope(Calibration cal, int microscope, double wavelength,
			double NA, double refraction,  String sampleInfos,
			String comments) {
		this.cal = cal;
		this.microscope = microscope;
		this.wavelength = wavelength;
		this.NA = NA;
		this.refraction = refraction;
	
		this.sampleInfos = sampleInfos;
		this.comments = comments;

		calculateTheoreticalResolutions();

		

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.SHORT);
		date = df.format(Calendar.getInstance().getTime()).toString();
	}

	
	
	
	
	public Microscope() {

		Preferences prefs = Preferences.userRoot().node("/PSFj25");
		cal = new Calibration();
		cal.pixelWidth = prefs.getDouble(PIXEL_WIDTH_KEY, 0.0);
		cal.pixelHeight = prefs.getDouble(PIXEL_HEIGHT_KEY, 0.0);
		cal.pixelDepth = prefs.getDouble(PIXEL_DEPTH_KEY, 0.0);

		cal.setUnit(prefs.get(UNIT_KEY, DEFAULT_UNIT));
		
	
		
		wavelength = prefs.getDouble(WAVELENGTH_KEY, 0.0);

		NA = prefs.getDouble(NA_KEY, 0);
		refraction = prefs.getDouble(RI_KEY, 0.0);
		;
		beadDiameter = prefs.getDouble(BEAD_DIAMETER_KEY, 0.0);

		magnification = prefs.getDouble(MAGNIFICATION_KEY, 0);

		identifier = "";
		unit = DEFAULT_UNIT;
		calculateTheoreticalResolutions();

	}

	
	public Microscope(Microscope m) {
		this();
		setWaveLength(m.getWaveLength());
		setBeadDiameter(m.getBeadDiameter());
		setNA(m.getNA());
		setRefraction(m.getRefractionIndex());
		setCalibration(m.getCalibration());
		
	}
	
	
	public Microscope(String filename) {
		this(new IniFile(filename.replaceAll("\\.\\w+$", "") + ".ini"));
	}

	public static void main(String[] args) {
		Microscope m = new Microscope("/Users/cyril/test_img/6_gfp.ini");
		m.save();
		System.out.println(m.getUnit());
		System.out.println(m.getMicroscopeHeader());
	}
	
	public Microscope(IniFile ini) {
		super();
		config = ini;
		cal = new Calibration();

		Preferences prefs =PSFj.getPreferences();

		wavelength = config.getDoubleValue(MICROSCOPE_SECTION, WAVELENGTH_KEY,
				prefs.getDouble(WAVELENGTH_KEY, 0.0));
		NA = config.getDoubleValue(MICROSCOPE_SECTION, NA_KEY,
				prefs.getDouble(NA_KEY, 0.0));
		refraction = config.getDoubleValue(MICROSCOPE_SECTION, RI_KEY,
				prefs.getDouble(RI_KEY, 0.0));
		beadDiameter = config.getDoubleValue(MICROSCOPE_SECTION, BEAD_DIAMETER_KEY,
				prefs.getDouble(BEAD_DIAMETER_KEY, 0.0));

		if (config.getDoubleValue(MICROSCOPE_SECTION, PIXEL_DEPTH_KEY, -1) > 0.0) {

			spaceBetweenStacks = config.getDoubleValue(MICROSCOPE_SECTION,
					STEP_SIZE_KEY, prefs.getDouble(STEP_SIZE_KEY, 0.0));
			pixelSize = config.getDoubleValue(MICROSCOPE_SECTION,
					PIXEL_SIZE_KEY, prefs.getDouble(PIXEL_SIZE_KEY, 0.0));
			magnification = config.getDoubleValue(MICROSCOPE_SECTION,
					MAGNIFICATION_KEY, prefs.getDouble(MAGNIFICATION_KEY, 0.0));

			calculateCalibrationFromPixelSize();
		} else {
			cal.pixelWidth = config.getDoubleValue(CALIBRATION_SECTION,
					PIXEL_WIDTH_KEY, prefs.getDouble(PIXEL_WIDTH_KEY, 0.0));
			cal.pixelHeight = config.getDoubleValue(CALIBRATION_SECTION,
					PIXEL_HEIGHT_KEY, prefs.getDouble(PIXEL_HEIGHT_KEY, 0.0));
			cal.pixelDepth = config.getDoubleValue(CALIBRATION_SECTION,
					PIXEL_DEPTH_KEY, prefs.getDouble(PIXEL_DEPTH_KEY, 0.0));

		}

		setIdentifier(config.getStringValue(MICROSCOPE_SECTION, IDENTIFIER_KEY));

		setUnit(config.getStringValue(CALIBRATION_SECTION, UNIT_KEY, DEFAULT_UNIT));
		
		if(!MathUtils.isMetricUnit(cal.getUnit()) && !cal.getUnit().equals(MathUtils.PIXEL) && !cal.getUnit().equals(MathUtils.PIXELS)) {
			setUnit(DEFAULT_UNIT);
		}
		
		
		save();

	}
	
	
	public void setPixelWidth(double width) {
		getCalibration().pixelWidth = width;
	}
	
	public void setPixelHeight(double height) {
		getCalibration().pixelHeight = height;
	}
	
	public void setPixelDepth(double depth) {
		getCalibration().pixelDepth = depth;
	}
	
	
	public boolean isAdvancedMode() {
		return !(pixelSize > 0 && magnification > 0 && spaceBetweenStacks > 0);
	}

	
	public void setUnit(String unit) {
		this.unit = unit;
		getCalibration().setUnit(unit);
	}
	
	public static String getBaseName(String filename) {
		return filename.replaceAll("\\.\\w+$", "");
	}
	
	public static Microscope loadMicroscopeFromImage(String filename) {
		File image = new File(filename);
		String basename = image.getAbsolutePath().replaceAll("\\.\\w+$", "");

		System.out.println(basename);

		Microscope m = new Microscope(basename + ".ini");

		return m;

	}

	
	public void calculateCalibrationFromPixelSize() {
		
		if(pixelSize == 0 || magnification == 0 || spaceBetweenStacks == 0) return;
		
		double pixelWidth = pixelSize
				/ getMagnification();
		
		double pixelDepth = spaceBetweenStacks;
		
				
		getCalibration().pixelWidth = pixelWidth;
		getCalibration().pixelHeight = pixelWidth;
		getCalibration().pixelDepth = pixelDepth;
	}
	
	
	public void save() {

		/*
		Preferences prefs = PSFj.getPreferences();
		prefs.putDouble(PIXEL_WIDTH_KEY, cal.pixelWidth);
		prefs.putDouble(PIXEL_HEIGHT_KEY, cal.pixelHeight);
		prefs.putDouble(PIXEL_DEPTH_KEY, cal.pixelDepth);

		prefs.putDouble(WAVELENGTH_KEY, wavelength);
		prefs.putDouble(NA_KEY, NA);
		prefs.putDouble(RI_KEY, refraction);
		prefs.putDouble(BEAD_SIZE_KEY, beadSize);

		prefs.putDouble(MAGNIFICATION_KEY, magnification);
		prefs.putDouble(STEP_SIZE_KEY, cal.pixelDepth);
		prefs.putDouble(PIXEL_SIZE_KEY, pixelSize);
		 
		
		
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/
		if (config != null) {
			config.addStringValue(MICROSCOPE_SECTION, WAVELENGTH_KEY,
					MathUtils.roundToString(wavelength, 4));
			config.addStringValue(MICROSCOPE_SECTION, NA_KEY,
					MathUtils.roundToString(NA, 4));
			config.addStringValue(MICROSCOPE_SECTION, RI_KEY,
					MathUtils.roundToString(refraction, 4));
			config.addStringValue(MICROSCOPE_SECTION, BEAD_DIAMETER_KEY,
					MathUtils.roundToString(beadDiameter, 4));

			if (pixelSize > 0.0) {
				config.addStringValue(MICROSCOPE_SECTION, PIXEL_SIZE_KEY,
						MathUtils.roundToString(pixelSize, 4));
				config.addStringValue(MICROSCOPE_SECTION, MAGNIFICATION_KEY,
						MathUtils.roundToString(getMagnification(), 3));
				config.addStringValue(MICROSCOPE_SECTION, STEP_SIZE_KEY,
						MathUtils.roundToString(spaceBetweenStacks, 4));
			}
			config.addStringValue(CALIBRATION_SECTION, PIXEL_WIDTH_KEY,
					MathUtils.roundToString(cal.pixelWidth, 4));
			config.addStringValue(CALIBRATION_SECTION, PIXEL_HEIGHT_KEY,
					MathUtils.roundToString(cal.pixelHeight, 4));
			config.addStringValue(CALIBRATION_SECTION, PIXEL_DEPTH_KEY,
					MathUtils.roundToString(cal.pixelDepth, 4));
			config.addStringValue(CALIBRATION_SECTION, UNIT_KEY, getUnit());
			config.addStringValue(MICROSCOPE_SECTION, IDENTIFIER_KEY,
					getIdentifier());
			config.save();
		}

	}

	/**
	 * Rounds any double to the user provided number of digits
	 * 
	 * @param nb2round
	 *            number to round
	 * @param nbOfDigits
	 *            number of digits
	 * @return a rounded double
	 */
	private double round(double nb2round, int nbOfDigits) {
		return Math.round(nb2round * Math.pow(10, nbOfDigits))
				/ Math.pow(10, nbOfDigits);
	}

	public Calibration getCalibration() {
		return cal;
	}

	public String getMicroscopeHeader() {

		reportHeader = "";
		if (getIdentifier().equals("") == false) {
		//	reportHeader += getIdentifier() + "\n";
		}
		reportHeader += "Emission wavelength: " + MathUtils.formatDouble(wavelength,getUnit()) + " "
				+ "\nNA: " + NA + "\nn: "
				+ round(refraction, 3) + "\nVoxel size: "
				+ formatDouble(cal.pixelWidth) + " (x) x"
				+ formatDouble(cal.pixelHeight) + " (y) x"
				+ formatDouble(cal.pixelDepth) + " (z) "
				+ "\nBead diameter : " + formatDouble(beadDiameter) + " "
				+ "\n\n";
		return reportHeader;
	}

	public void calculateTheoreticalResolutions() {
		resolution[0] = 0.84 * 0.61 * wavelength / NA;
		resolution[1] = resolution[0];
		resolution[2] = 1.76 * wavelength * refraction / Math.pow(NA, 2);
	}

	public double getXYTheoreticalResolutionWithBeadSize() {
		calculateTheoreticalResolutions();
		return Math.sqrt(Math.pow(beadDiameter, 2.5) + Math.pow(resolution[0], 2));
	}

	public double getZTheoreticalResolutionWithBeadSize() {
		calculateTheoreticalResolutions();
		return Math.sqrt(Math.pow(beadDiameter, 2.5) + Math.pow(resolution[2], 2));
	}

	public double getZTheoreticalResolution() {
		calculateTheoreticalResolutions();
		return resolution[2];
	}

	public double getXYTheoreticalResolution() {
		calculateTheoreticalResolutions();
		return resolution[0];
	}

	
	

	public double getCorrectedResolution(double fwhm, int axe) {

		double expectedFWHM;

		if (axe == X || axe == Y) {
			expectedFWHM = getXYTheoreticalResolution();
		} else {
			expectedFWHM = getZTheoreticalResolution();
		}

		return fwhm * getAlpha(fwhm, expectedFWHM, beadDiameter, axe);

	}

	/**
	 * Gets the p. The formula is given in the technical support of the
	 * associated publication.
	 * 
	 * @param mu
	 *            the mu
	 * @param sigma
	 *            the sigma
	 * @param beadDiameter
	 *            the bead diamter
	 * @return the p
	 */
	private double getP(double mu, double sigma, double beadDiameter, double P0) {

		// left part of the formula
		double left = P0 / (sigma * beadDiameter * Math.sqrt(2.0 * Math.PI));

		// top part of what's inside the exponencial
		double right_up = Math.pow((Math.log(beadDiameter) - mu), 2);

		// bottom part of what's inside the exponenciel
		double right_down = 2.0 * Math.pow(sigma, 2);

		// let's assemble everything according to the formula
		double result = left * Math.exp(-1.0 * right_up / right_down);

		return result;
	}

	private double getS(double beadDiameter, double c, double s0) {

		return s0 * (beadDiameter / (1.0 + beadDiameter + c));
	}

	/**
	 * Calculate the alpha correction factor for the FWHM according to the ratio
	 * between the expected FWHM and the bead diameter. For more informations,
	 * please check the technical support in the publication.
	 * 
	 * @param fwhm
	 *            the FWHM (or calculated resolution
	 * @param expectedFWHM
	 *            the expected (or theoretical) resolution
	 * @param beadDiameter
	 *            the bead diameter
	 * @param axe
	 *            the axe of the calculate (X, Y or Z)
	 * @return the alpha correction factor
	 */
	private double getAlpha(double fwhm, double expectedFWHM,
			double beadDiameter, int axe) {

		double alpha;

		double mu;
		double sigma;
		double s0;
		double p0;
		double c;
		if (axe == X || axe == Y) {

			s0 = 0.012;
			c = 6.07;
			p0 = -12.28;

			mu = 4.05;
			sigma = 1.71;

		} else {

			s0 = -0.10;
			c = -0.0062;
			p0 = 0.90;

			mu = 0.36;
			sigma = 0.46;
		}

		double p = getP(mu, sigma, beadDiameter, p0);
		double s = getS(beadDiameter, c, s0);

		double top = expectedFWHM;
		double bottom = Math.sqrt(Math.pow(expectedFWHM, 2)
				+ Math.pow(beadDiameter, 2))
				+ s + p;

		// System.out.println(String.format("[Axe %d]\nFWHM : %.3f\nExpected : %.3f\np: %.3f\ns : %.3f\nalpha : %.3f",axe,fwhm,expectedFWHM,p,s,top/bottom));

		return top / bottom;
	}

	public double getBeadDiameter() {
		return beadDiameter;
	}

	public String getUnit() {
		return unit;//getCalibration().getUnit();
	}

	public String toString() {
		String result = String
				.format("[Microscope : CPS : %s, Step : %s, M : %s]\n[Calibration : %sx %s x %s]",
						MathUtils.roundToString(pixelSize, 4),
						MathUtils.roundToString(this.spaceBetweenStacks, 4),
						MathUtils.roundToString(getMagnification(), 2),
						MathUtils.roundToString(cal.pixelWidth, 4),
						MathUtils.roundToString(cal.pixelWidth, 4),
						MathUtils.roundToString(cal.pixelDepth, 4));
		return result;

	}

	public Objective getObjective(String name, double magnification) {
		return new Objective(name, magnification);
	}

	public class Objective implements Comparable<Objective> {
		String name;
		double magnification;

		public Objective(String name, double magnification) {
			this.name = name;
			this.magnification = magnification;
		}

		@Override
		public int compareTo(Objective o) {
			if (getMagnification() == o.getMagnification()) {
				return 0;
			}
			return -1;
		}

		public boolean equals(Object o) {
			if (o instanceof Objective) {
				if (((Objective) o).getMagnification() == getMagnification()) {

					return true;
				} else
					return false;
			} else {
				return false;
			}
		}

		public double getMagnification() {
			return magnification;
		}

		public String toString() {
			return name;
		}

	}

	public String getWaveLengthAsString() {
		return formatDouble(wavelength);
	}

	public String getNAAsString() {
		return "" + MathUtils.round(NA, 5);
	}

	public String getRefractionIndexAsString() {
		return "" + MathUtils.round(refraction, 5);
	}

	public String getBeadSizeAsString() {
		return formatDouble(beadDiameter);
	}

	public String getVoxelSizeAsString() {
		String result = "";
		result += formatDouble(cal.pixelWidth);
		result += " x " + formatDouble(cal.pixelHeight);
		result += " x " + formatDouble(cal.pixelDepth);

		return result;
	}

	public String getConfigurationFileFromImage(String imageAddress) {
		String basename = imageAddress.replaceAll("\\.\\w+$", "");
		String iniFile = basename + ".ini";
		return iniFile;
	}

	public double getWaveLength() {
		return wavelength;
	}

	public void setWaveLength(double w) {
		wavelength = w;
	}

	public void setIdentifier(String text) {

		identifier = text;

		if (config != null) {
			// System.out.println("Setting idenfier to "+text);
			if (text.equals(""))
				return;
			config.addStringValue(MICROSCOPE_SECTION, IDENTIFIER_KEY, text);
		}
	}

	public String getIdentifier() {
		if (config != null)
			return config
					.getStringValue(MICROSCOPE_SECTION, IDENTIFIER_KEY, "");
		return identifier;

	}

	public void setNA(double nA) {
		NA = nA;
	}

	public double getNA() {
		return NA;
	}

	public void setRefraction(double refraction) {
		this.refraction = refraction;
	}

	public void setCalibration(Calibration cal) {
		this.cal = cal;
	}

	public void setSpaceBetweenStacks(double spaceBetweenStacks) {
		this.spaceBetweenStacks = spaceBetweenStacks;
	}

	public double getMagnification() {
		return magnification;
	}

	public void setMagnification(double magnification) {
		this.magnification = magnification;
	}

	public double getAdditionalMagnification() {
		return additionalMagnification;
	}

	public void setAdditionalMagnification(double additionalMagnification) {
		this.additionalMagnification = additionalMagnification;
	}

	public double getRefractionIndex() {
		return refraction;
	}

	public double getCameraPixelSize() {
		return pixelSize;
	}

	public void setCameraPixelSize(double p) {
		pixelSize = p;
	}

	public void setBeadDiameter(double d) {
		beadDiameter = d;
	}

	public double getSpaceBetweenStacks() {
		return spaceBetweenStacks;
	}

	public double getPixelWidth() {
		return getCalibration().pixelWidth;
	}
	
	public double getPixelHeight() {
		return getCalibration().pixelHeight;
	}
	
	public double getPixelDepth() {
		return getCalibration().pixelDepth;
	}
	
	
	public boolean isConfigurationValid() {
		
		
		ArrayList<Boolean> checkPoints = new ArrayList<Boolean>();
		
		checkPoints.add(beadDiameter > 0);
		checkPoints.add(wavelength > 0);
		checkPoints.add(refraction > 0);
		checkPoints.add(NA > 0);
		
		checkPoints.add(getCalibration().pixelWidth > 0);
		checkPoints.add(getCalibration().pixelHeight > 0);
		checkPoints.add(getCalibration().pixelDepth > 0);
		
		for(boolean checkPoint : checkPoints) {
			if(checkPoint == false) return false;
		}
		return true;
	}
	
	public void mergeFromOtherMicroscope(Microscope m, boolean includeWavelength) {

		if (includeWavelength) {
			setWaveLength(m.getWaveLength());
		}

		
		
		setIdentifier(m.getIdentifier());

		setNA(m.getNA());
		setRefraction(m.getRefractionIndex());
		System.out.println("The entered bead diameter is :"+m.getBeadDiameter());
		setBeadDiameter(m.getBeadDiameter());

		setSpaceBetweenStacks(m.getSpaceBetweenStacks());
		setMagnification(m.getMagnification());
		setCameraPixelSize(m.getCameraPixelSize());
		
		Calibration nc = new Calibration();
		Calibration oc = m.getCalibration();
		
		nc.setUnit(oc.getUnit());
		nc.pixelWidth = oc.pixelWidth;
		nc.pixelHeight = oc.pixelHeight;
		nc.pixelDepth = oc.pixelDepth;
		
		setCalibration(nc);
		setUnit(m.getUnit());
		save();
	}

	public String formatDouble(double number) {
		// TODO Auto-generated method stub
		return MathUtils.formatDouble(number, getUnit());
	}

	public double getTheoreticalResolution(int axe) {
		if(axe == PSFj.Z_AXIS) return getZTheoreticalResolution();
		else return getXYTheoreticalResolution();
	}
	
	public String getTheoreticalResolutionAsString(int axe) {
		return formatDouble(getTheoreticalResolution(axe));
	}

}
