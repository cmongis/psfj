/*
 * 
 */
import java.io.File;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.SplashScreen;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.IniFile;
import knop.psfj.view.BeadImageLoaderPage;
import knop.psfj.view.CalibrationPage;
import knop.psfj.view.HeatMapPage;
import knop.psfj.view.ProcessingPage;
import knop.psfj.view.ThresholdChooserPage;
import knop.psfj.view.WizardWindow;
import knop.utils.stats.DataSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class PSFj {
	public static String MICROSCOPE_SECTION = "microscope";
	public static String CALIBRATION_SECTION = "pixel";

	
	public static String getVersion() {
		return "2.0, build 72";
	}
	
	
	
	public static void main(String[] args) {
		for( String arg : args) {
			System.out.println(arg);
		}
		if (args.length == 0) {
			
			PSFj.init(new String[0]);
		} else {

			/*
			 * Parameter Parsing
			 */

			Options options = new Options();
			options.addOption("v","version", false,"displays PSFj version");
			options.addOption("h", "help", false, "displays help");
			options.addOption("w","bead-frame",true,"bead frame of with a size of N times the expected FWHM");
			options.addOption("i", "input", true, "input file");
			options.addOption("o", "output", true,
					"output directory (optional)  : if not specified,");
			options.addOption(
					"c",
					"calibration",
					true,
					"calibration file .ini (optional) : if a file contains the same name as the image with the .ini extension, it will be loaded as the image calibration file");
			options.addOption("g", "generate-calibration", true,
					"generates a typical calibration file");

			options.addOption("m", "heatmap", false, "exports heatmap");
			options.addOption("s", "sum-up", false, "exports PDF Summary");
			options.addOption("d", "csv", false, "exports data as csv");
			options.addOption("r", "bead-report", false,
					"exports bead PSF Reports");
			options.addOption("a", "all", false, "exports all above");
			options.addOption("f", "fill-report", true,
					"compile informations in a file in the export folder");
			
			options.addOption("2c","dual-channel",false,"compare two channels");
			
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = null;
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);

			}
			
			if(cmd.hasOption("v")) {
				System.out.println(knop.psfj.PSFj.getVersion());
				System.exit(0);
			}
			
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("psfj", options);
				System.exit(0);
			}

			if (cmd.hasOption("g")) {
				generateDefaultCalibrationFile(cmd.getOptionValue("g"));
				System.exit(0);
			}
			// options.add

			boolean errorOccured = false;

			String filename = cmd.getOptionValue("i");
			String outputDirectory = cmd.getOptionValue("o");
			String calibrationFile = cmd.getOptionValue("c");
			
			
			
			System.out.println(calibrationFile);
			if (filename == null || new File(filename).exists() == false) {
				System.err.println("Error : Input file missing");
				System.exit(1);
			}
			

			/*
			 * Image Processing
			 */

			BeadImageManager manager = new BeadImageManager();
			BeadImage image;
			
			
			

			
			try {
				
				image = new BeadImage(filename);
				
				if(calibrationFile != null) {
					image.setMicroscope(new Microscope(new IniFile(calibrationFile)));
				}
				
				image.workFromMemory();
				image.autoFocus();
				manager.add(image);
				
				if(cmd.hasOption('w')) {
					
					int enlargement;
					if(cmd.getOptionValue("w") == null) {
						enlargement = 20;
					}
					else {
						enlargement = Integer.parseInt(cmd.getOptionValue("w"));
					}
					
					manager.setBeadEnlargementFactor(enlargement);
				}
				
				manager.verifyBeadImageParameters();

				if (cmd.hasOption("o") && cmd.getOptionValue("o") != null) {
					manager.setExportDirectory(cmd.getOptionValue("o"));
				}
				System.out.println(manager.getFrameSize());
				System.out.println(manager.getThresholdValue());

				manager.processProfiles();
				
			} catch (Exception e) {
				errorOccured = true;
				e.printStackTrace();
			}

			if (cmd.hasOption("f")) {
				String reportAddress = cmd.getOptionValue("f");
				
				if(reportAddress == null) {
					reportAddress = manager.getExportDirectory()
							+ "compilation.csv";
				}
				
				System.out.println("Adding results to "+reportAddress);
				
				DataSet compilation = new DataSet(reportAddress);
				FovDataSet dataset = manager.getDataSet();

				compilation.addValue("name", manager.getAnalysisName());

				if (errorOccured) {
					for (String field : new String[] { "fwhmX_median",
							 "fwhmY_median","fwhmZ_median","fwhmX_theoretical",
							"fwhmY_theoretical", 
							"fwhmZ_theoretical", 
							"analyzed_beads", "kept_beads"}) {
						compilation.addValue(field, "err");
					}
					compilation.addValue("bead_frame",""+manager.getBeadEnlargementFactor());
					
					compilation.addValue("density", manager.getBeadImage(0).getBeadDensity());
				
					compilation.addValue("snr", manager.getBeadImage(0).getSignalToNoiseRatio());
					compilation.save();
					System.exit(1);
				}

				compilation.addValue("fwhmX_median",
						dataset.getMetaDataValue("fwhmX_median"));
				compilation.addValue("fwhmY_median",
						dataset.getMetaDataValue("fwhmY_median"));
				
				compilation.addValue("fwhmZ_median",
						dataset.getMetaDataValue("fwhmZ_median"));
			
				compilation.addValue("fwhmX_theoretical",
						dataset.getTheoriticalValue("fwhmX"));
				compilation.addValue("fwhmY_theoretical",
						dataset.getTheoriticalValue("fwhmY"));
				
				compilation.addValue("fwhmZ_theoretical",
						dataset.getTheoriticalValue("fwhmZ"));
				compilation.addValue("analyzed_beads",
						manager.getAnalyzedBeadCount());
				compilation.addValue("kept_beads", manager.getKeptBeadsCount());
				compilation.addValue("bead_frame",""+manager.getBeadEnlargementFactor());
				compilation.addValue("density", manager.getBeadImage(0).getBeadDensity());
				compilation.addValue("a_Mean", manager.getBeadImage(0).getBackgroundMean());
				compilation.addValue("a_StdDev",manager.getBeadImage(0).getBackgroundStandardDeviation());
				compilation.addValue("b_mean",manager.getBeadImage(0).getSignalMean());
			   compilation.addValue("b_stddev",manager.getBeadImage(0).getSignalStandardDeviation());
				compilation.save();
			}

			if (errorOccured) {
				System.exit(1);
			}

			if (cmd.hasOption("s"))
				manager.exportPDFSumUp(false);
			if (cmd.hasOption("m"))
				manager.exportHeatMaps();

			if (cmd.hasOption("d"))
				manager.exportCSVFile();

			if (cmd.hasOption("r"))
				manager.exportInPDF();

		}
	}

	public static Microscope loadMicroscopeFromImage(String filename) {
		File image = new File(filename);
		String basename = image.getAbsolutePath()
				.replaceAll("\\.\\w+$", "");
		
		Microscope m = new Microscope(basename+".ini");
		
		
		return m;
		
	}
	
	
	public static String getCalibrationFileName(String filename,
			String calibration) {

		/* calibration is indicated and the file exists :-) let's go ! */
		if (calibration != null && new File(calibration).exists())
			return calibration;

		/*
		 * No calibration, let's see if there is a ini file with the same name
		 * as the image
		 */

		File image = new File(filename);

		if (calibration == null) {
			String basename = image.getAbsolutePath()
					.replaceAll("\\.\\w+$", "");
			if (basename == null)
				return null;

			String iniFile = basename + ".ini";
			System.out.println(iniFile);
			if (new File(iniFile).exists()) {
				return iniFile;
			} else {
				return null;
			}
		}
		return null;
	}

	public static void generateDefaultCalibrationFile(String file) {
		
		if(file == null) {
			file = "calibration.ini";
		}
		
		else {
			file = file.replaceAll("\\.(\\w+)$", "") + ".ini";
		}
		
		System.out.println("Writing "+file+"...");
		
		if(new File(file).exists() == true) return;
		
		
		IniFile ini = new IniFile(file);

		ini.addStringValue(MICROSCOPE_SECTION, "wavelength", "0.5");
		ini.addStringValue(MICROSCOPE_SECTION, "na", "1.27");
		ini.addStringValue(MICROSCOPE_SECTION, "ri", "1.33");
		ini.addStringValue(MICROSCOPE_SECTION, "beadsize", "0.175");

		
		ini.addStringValue(MICROSCOPE_SECTION, "cam_pixel_size","6.5");
		ini.addStringValue(MICROSCOPE_SECTION, "magnification", "60");
		ini.addStringValue(MICROSCOPE_SECTION, "step_size", "0.1");
		
		ini.addStringValue(CALIBRATION_SECTION, "width", "-1");
		ini.addStringValue(CALIBRATION_SECTION, "height", "-1");
		ini.addStringValue(CALIBRATION_SECTION, "depth", "-1");
		ini.addStringValue(CALIBRATION_SECTION, "unit", "µm");

		ini.save();

	}

	public static void init(String[] arg) {
		
		
		SplashScreen splh = null;
		try {
		splh = new SplashScreen("/knoplablogo.png");
		splh.show();
		}
		catch(Exception e) {
			try {
				splh = new SplashScreen("src/knoplablogo.png");
				splh.show();
			}
			catch(Exception e2) {
				System.out.println("Couldn't load splashscreen.");
			}
		}
		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();
		manager.addObserver(wizardWindow);
		wizardWindow.addPage(new BeadImageLoaderPage(wizardWindow, manager));
		//wizardWindow.addPage(new FocusChooserPage(manager));
		wizardWindow.addPage(new CalibrationPage(manager));
		wizardWindow.addPage(new ThresholdChooserPage(manager));
		//wizardWindow.addPage(new ExportDataPage(manager));
		wizardWindow.addPage(new ProcessingPage(manager));
		wizardWindow.addPage(new HeatMapPage(manager));
		//wizardWindow.addPage(new BeadInspectionPage(wizardWindow, manager));
		wizardWindow.setCurrentPage(0);
		if(splh != null) {
			splh.hide();
			splh.setVisible(false);
			splh.disable();
		}
		
		wizardWindow.show();
	}
}
