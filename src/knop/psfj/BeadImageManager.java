/*
 * 
 */
package knop.psfj;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import knop.psfj.exporter.CsvExporerParsable;

import knop.psfj.exporter.CsvExporter;
import knop.psfj.exporter.PDFExporter;
import knop.psfj.exporter.PDFSumUpExporter;
import knop.psfj.graphics.AsymmetryHeatMap;
import knop.psfj.graphics.BeadMap;
import knop.psfj.graphics.BeadMontage;
import knop.psfj.graphics.DistanceHeatMap;
import knop.psfj.graphics.FWHMHeatMap;
import knop.psfj.graphics.FullHeatMap;
import knop.psfj.graphics.PsfJGraph;
import knop.psfj.graphics.ShiftHeatMap;
import knop.psfj.graphics.ThetaHeatMap;
import knop.psfj.graphics.ZProfileHeatMap;
import knop.psfj.heatmap.HeatMapGenerator;
import knop.psfj.locator.BeadLocator;
import knop.psfj.locator.BeadLocator2D;
import knop.psfj.locator.BeadLocator3D;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.FileUtils;
import knop.psfj.utils.MathUtils;
import knop.psfj.utils.MemoryUtils;
import knop.psfj.view.Message;
import knop.psfj.view.WizardWindow;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadImageManager.
 */
public class BeadImageManager extends Observable implements Observer {

	/** The bead image list. */
	ArrayList<BeadImage> beadImageList = new ArrayList<BeadImage>();

	/** The wizard window2. */
	WizardWindow wizardWindow2;

	/** The calibration. */
	Calibration calibration;

	/** The status. */
	private String status;

	/** The progress. */
	private int progress;

	/** The runtime. */
	Runtime runtime = Runtime.getRuntime();

	/** The heat map step. */
	int heatMapStep = 20;

	/** The do export. */
	boolean doExport;

	/** The current column. */
	String currentColumn = "fwhmZ";

	/** The data set. */
	FovDataSet dataSet = new FovDataSet();

	/** The export directory. */
	private String exportDirectory;

	/** The automatic. */
	public static int AUTOMATIC = 0;

	/** The load on memory. */
	public static int LOAD_ON_MEMORY = 1;

	/** The load from disk. */
	public static int LOAD_FROM_DISK = 2;

	/** The reading lock. Used to read the imformations of only one image a time */
	private static Boolean readingLock = new Boolean(true);

	/** The loading lock. Used to load only one entire image at a time */
	private static Boolean loadingLock = new Boolean(true);

	// is ready
	boolean isReady = true;

	/** The graph hash. */
	public HashMap<String, PsfJGraph> graphHash;

	/**
	 * Fitting Threshold. All fitting presenting a R² inferior to this value
	 * will be ignored
	 **/
	double fittingThreshold = 0.80;

	/** hash map containing different channels */
	HashMap<Integer, Microscope> channels = new HashMap<Integer, Microscope>();

	/** The single channel analysis type. */
	public static int SINGLE_CHANNEL = 1;

	/** The dual channel analysis type. */
	public static int DUAL_CHANNEL = 2;

	/** The current type of the analysis (single or dual-channel) */
	protected int analysisType = DUAL_CHANNEL;

        
        public static int LOCATOR_2D = 0;
        public static int LOCATOR_3D = 1;
        
        
        protected BeadLocator locator = new BeadLocator2D();
        
        
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		
		final BeadImageManager manager = new BeadImageManager();

		manager.add("/home/cyril/test_img/6/6_gfp.tif");
		manager.add("/home/cyril/test_img/6/6_mcherry.tif");
		
		// waiting for the images to load
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("let's go !");

		// autocalculating parameters
		for (BeadImage image : manager.getBeadImageList()) {
			image.autoThreshold();
			image.autoFrameSize();
		}
		
		// setting the export directory
		manager.setExportDirectory("/home/cyril/test_img/6/results/");
		
		// processing the psf
		manager.processProfiles();
		
		// exporting (the boolean parameter will prevent the document to open once finished)
		manager.exportPDFSumUp(false);
		manager.exportHeatMaps(false);
		manager.exportCSVFile(false);
		manager.exportInPDF(false);
		
		// printing the dataset
		System.out.println(manager.getDataSet().exportToString());
		
		
		// System.out.println("too soon ?");
	}

	/**
	 * Instantiates a new bead image manager.
	 */
	public BeadImageManager() {
		super();
	}

	/**
	 * Adds a beadImage object.
	 * 
	 * @param beadImage
	 *            the bead image
	 */
	public synchronized void add(BeadImage beadImage) {

		// add an bead image to the list
		beadImageList.add(beadImage);
                beadImage.setLocator(getLocator());
		if (exportDirectory == null) {
			exportDirectory = beadImage.getImageFolder();
		}
		setChanged();
		notifyObservers(new Message(this, "beadimage list modified"));

	}

	/**
	 * Adds a ImagePlus object as a bead image.
	 * 
	 * @param ipl
	 *            the ImagePlus to add
	 */
	public void add(ImagePlus ipl) {

		// creating a bead image object
		BeadImage beadImage = new BeadImage();

		// setting the image witdh and height of the bead image from the
		// ImagePlus object
		beadImage.imageWidth = ipl.getWidth();
		beadImage.imageHeight = ipl.getHeight();

		// adding the manager as observer of the bead image
		beadImage.addObserver(this);

		// setting the stack
		beadImage.setStack(ipl.getImageStack());

		// calculating the focus
		beadImage.autoFocus();
                
                beadImage.autoThreshold();
                
		// setting the name from the bead iamge
		beadImage.setImageName(ipl.getShortTitle());

		// trying to determine the bead image
		beadImage.setFileAddress(PSFj.getDefaultHomeDirectory()
				+ File.separatorChar + "PSFj" + File.separatorChar
				+ ipl.getTitle());

		// adding the bead image to the list
		add(beadImage);

	}

	/** int used to determine when comes the next warning check. */
	int imagesYetToLoad = 0;

	public void addAsync(File[] files) {
		for (File f : files) {
			addAsync(f.getAbsolutePath());
		}
	}

	/**
	 * Adds the async.
	 * 
	 * @param fileAddress
	 *            the file address
	 */
	public void addAsync(final String fileAddress) {
		new Thread() {
			public void run() {
				add(fileAddress);
			}
		}.start();
	}

	/**
	 * Adds the image stack from a complete address.
	 * 
	 * @param fileAddress
	 *            the image stack file address
	 */
	private String checkImage(String fileAddress) {

		// flag determining if the image is valid or not
		boolean isValid = true;

		// no reason for now
		String reason = null;

		// trying to open the image
		ImageProcessorReader ipr = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));

		try {
			ipr.setId(fileAddress);

		}
		// if the format cannot be read
		catch (FormatException e) {

			e.printStackTrace();
			try {

				//
				ipr.close();
				reason = "Error when adding image.";
			}
			// if the file cannot be accessed
			catch (IOException e1) {

				e1.printStackTrace();
			}
			isValid = false;
			
			// if no reason has been set
			if (reason == null)
				reason = "PSFj doesn't support this type of image.";

		}
		// don't know why there are two catching of IO exception but let's let
		// it like this
		catch (IOException e) {
			e.printStackTrace();
			
			// if no reason has been set
			if (reason == null)
				reason = "Error when reading the image.";
			isValid = false;
		}

		System.out.println(isValid);
		// the image has been opened, getting informations about the image;
		if (isValid == true) {

			int width = ipr.getSizeX();
			int height = ipr.getSizeY();
			int numChannel = ipr.getSizeC();
			int bitsPerPixel = ipr.getBitsPerPixel();

			// checking file size
			long fileSize = new File(fileAddress).length() / 1000 / 1000;

			long imageAmount = 0;

			for (BeadImage image : getBeadImageList()) {
				imageAmount += image.getImageDiskSize();
			}

			if (imageAmount + fileSize > MemoryUtils.getTotalMemory() * 0.55) {
				isValid = false;

				return String.format("You can only load %dMB of stacks.",
						MemoryUtils.getTotalMemory() * 60 / 100);
			}

			// checking the channel number
			if (numChannel > 2) {
				if (reason == null)
					reason = "Only monochannel image can be added.";
				isValid = false;
			}

			// checking the bit size
			if (bitsPerPixel > 16) {
				isValid = false;
				if (reason == null)
					reason = "Only 8-bits and 16-bits tif images are supported.";
			}

			// checking if the image has not been added already
			for (BeadImage image : getBeadImageList()) {
				if (image.getFileAddress().equals(fileAddress)) {
					isValid = false;
					if (reason == null)
						reason = "You cannot add the same image twice.";
				}
			}

			// checking if the size is not wrong
			if (width <= 0) {
				isValid = false;
				if (reason == null)
					reason = "Error when reading the image. The stack may be corrupted";
			}
			
			// getting the first image ever added
			if (getBeadImageCount() > 0)
				System.out.println("first one = "
						+ getBeadImage(0).getImageWidth());

			// checking if the image is the same size as the other
			if (getBeadImageCount() > 0
					&& (getBeadImage(0).getImageWidth() != width || getBeadImage(
							0).getImageHeight() != height)) {

				if (reason == null)
					reason = "Please add an image stacks of the same size.";
				isValid = false;
			}

			try {
				ipr.close();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		return reason;

	}

	// checking for possible warnings (not errors)
	public void checkForWarnings() {
		
		// checking if bead images has different wavelengths;
		
		System.out.println("Checking for warnings : " + imagesYetToLoad);
		
		// if no image in the list, no need to continue
		if (countBeadImage() == 0)
			return;
		
		// if no image are yet to load, no need to continue
		if (imagesYetToLoad > 0)
			return;

		// we assume all the images have different wavelength
		boolean hasDifferentWavelengths = false;
		
		// getting the wavelength of the first image
		double firstWaveLength = getBeadImage(0).getMicroscope()
				.getWaveLength();

		// if the number of image stacks is greated than 2
		if (countBeadImage() >= 2) {
			// for each image of the list
			for (BeadImage image : getBeadImageList()) {
				
				// getting the wavelength
				double wavelength = image.getMicroscope().getWaveLength();
				
				// if this wavelength is different from the first wavelength
				if (wavelength > 0 && wavelength != firstWaveLength) {
					
					// then this image is said to have a different wavelength from the first
					image.setStatus("Wavelength different from the first.");
					
					// the different wavelength flag is set
					hasDifferentWavelengths = true;
					
					// the loop is broken
					break;
				}
			}
			
			// if there is different wavelengths among the bead images and if it's a single color analysis
			// then the user should be warned that one image may override the other image parameters.
			if (hasDifferentWavelengths && !isDualColorAnalysis())
				notifyWarning("You added images from different wavelengths.\nContinuing a Single-Color analysis will override\nthe image parameters.");

		}

	}

	// notify a warning to the view
	private void notifyWarning(String message) {

		setChanged();
		notifyObservers(new Message(this, "warning", message));
	}

	// add an image from the file address
	public void add(String fileAddress) {

		// the bead image
		BeadImage beadImage = null;
		
		// assuming the bead image is valid
		boolean isValid = true;
		
		// possible problem encountered during loading
		String problem;

		// image yet to load is incremented (this method can be called simoustaneouly so precaution must be taken);
		imagesYetToLoad++;
                
		// making sure that only one image is analyzed at once
		synchronized (readingLock) {
			
			// checking the possible probrems (it returns null if no problem)
			problem = checkImage(fileAddress);
			
			// if no problem has been detected
			if (problem == null) {
				//creating a new bead image
				beadImage = new BeadImage();
				
                                
                                
				// setting the image as valid
				beadImage.isOpening = true;
				
				// setting the file address in the bead image
				beadImage.setFileAddress(fileAddress);
				
				// setting the manager as observer
				beadImage.addObserver(this);
				add(beadImage);
				
				// read the bead image properties from the disk (width, height etc.)
				beadImage.readWidthAndHeightFromDisk();
                                
                                beadImage.isOpening = false;

			} else {
				// display the problem
				System.err.println(problem);
				setChanged();
				notifyObservers(new Message(this, "error", problem));
				
				// setting as invalid
				isValid = false;

			}
			imagesYetToLoad--;
			if (!isValid)
				return;

			// if the wavelength of the new image is different, the mode is
			// switchback
			// to multicolor
			if (isValid
					&& getBeadImageCount() == 2
					&& beadImage.getMicroscope().getWaveLength() != getBeadImage(
							0).getMicroscope().getWaveLength()
					&& beadImage.getMicroscope().getWaveLength() > 0) {
				setAnalysisType(DUAL_CHANNEL);
			}

			else {
				setAnalysisType(SINGLE_CHANNEL);
			}

		}
		
		// checking for warnings like different wavelengths or formats
		checkForWarnings();

		if (!isValid)
			return;

		// if the export directory has not been set, the folder that containts the image is set as default
		if (exportDirectory == null)
			exportDirectory = beadImage.getImageFolder();
		
		// loading the image in memory (using a lock to avoid loading multiple images at the same time
		synchronized (loadingLock) {
			
			isReady = false;
			try {
				// loading the image from memory
				beadImage.workFromMemory();
			}
			// shouldn't happen but we never know
			catch (OutOfMemoryError e) {
				e.printStackTrace();
				notifyObservers(new Message(this, "error",
						"Memory overloaded !"));
			}
			
			// any other error
			catch (Exception e) {
				e.printStackTrace();
			}
			isReady = true;
		}
		
		// the image has been loaded, let's notify the observers
		setChanged();
		notifyObservers(new Message(this, "image loaded"));

	}

	/**
	 * Gets the bead image object from it's position in the bead image list
	 * 
	 * @param i
	 *            the id of the image in the list
	 * @return the bead image
	 */
	public BeadImage getBeadImage(int i) {

		if (i == -1)
			i++;

		if (i < beadImageList.size() && i >= 0) {
			return beadImageList.get(i);
		} else {
			System.out.println("Couln't find bead image number " + i + ".");
			return null;
		}
	}

	/**
	 * Returns the number of bead images that the manager contains
	 * 
	 * @return the image stack count
	 */
	public int countBeadImage() {
		return beadImageList.size();
	}

	/**
	 * Removes the bead image which has the id "i"
	 * 
	 * @param i
	 *            the id of the image that must be removed
	 */
	public void remove(int i) {
		if (i < beadImageList.size()) {
			System.out.println("Removing number " + i);
			remove(getBeadImage(i));

		} else {
			System.out.println("Couln't delete this item. It doesn't exist : "
					+ i);
		}
	}

	/**
	 * Removes the specified bead image object.
	 * 
	 * @param beadImage
	 *            the bead image
	 */
	public void remove(BeadImage beadImage) {
		if (beadImageList != null) {
			beadImageList.remove(beadImage);
		}
		
		// remove all observers of this image (for garbage collection)
		beadImage.deleteObservers();

		//System.out.println(beadImageList.size());
		// System.out.println(beadImageList.get(0));

		// reseting the dataset
		dataSet = new FovDataSet();
		
		// reseting the export directory
		exportDirectory = null;

		// cleaning the bead image from the memory
		beadImage.cleanMemory(BeadImage.CLEAN_FOR_DELETION);
		
		// reseting the microscope settings
		if (countBeadImage() == 2
				&& getBeadImage(0).getMicroscope().getWaveLength() != getBeadImage(
						1).getMicroscope().getWaveLength()) {
			setAnalysisType(DUAL_CHANNEL);
		}
		
		// invoking garbage collection
		System.gc();
		
		setChanged();
		notifyObservers(new Message(this, "beadimage list modified"));
	}

	/**
	 * Gets the bead image object list.
	 * 
	 * @return the bead image list
	 */
	public ArrayList<BeadImage> getBeadImageList() {
		return beadImageList;
	}

	/**
	 * Gets the image list in a string form.
	 * 
	 * @return the image string list
	 */
	public ArrayList<String> getImageList() {

		ArrayList<String> imageList = new ArrayList<String>();
		for (BeadImage b : getBeadImageList()) {
			imageList.add(b.getImageName());
			System.out.println(b.getImageName());
		}
		return imageList;
	}

	/**
	 * Gets the progress of the ongoing process.
	 * 
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Gets the status of the ongoing process.
	 * 
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Gets the microscope of the image with the specified id
	 * 
	 * @param id
	 *            the id of the image
	 * @return the microscope configuration
	 */
	public Microscope getMicroscope(int id) {
		if (getBeadImage(id) != null)
			return getBeadImage(id).getMicroscope();
		else
			return null;
	}

	
	/**
	 * Sets the microscope configuration for the specified image
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @param microscope
	 *            the new microscope
	 */
	public void setMicroscope(int beadImageId, Microscope microscope) {
		getBeadImage(beadImageId).setMicroscope(microscope);
	}

	/*
	 * Processes the message coming from different sources
	 */
	@Override
	public void update(Observable o, Object data) {
		
		
		// if the data sent are simple objects
		if (data instanceof Integer) {
			progress = (Integer) data;
		}
		if (data instanceof String) {
			status = (String) data;
		}

		//if the sent data is a Message object
		if (data instanceof Message) {
			Message message = (Message) data;
			
			// other channel detected (when adding an image stack that contains both channels)
			if (message.getName().contains("other channel detected")) {
				
				BeadImage image = (BeadImage) message.getData();
				add(image);
				return;

			}
			
			// Bead Image processor ? progress information
			if (message.getOrigin() instanceof BeadFrameProcessor
					&& message.getName() == "progress") {
				BeadImage image = (BeadImage) message.getData();
				if (image != null)
					image.setProgress(message.getIntData(),
							message.getStringData());
			}
			
			// image is not okay, let's remove it :-)
			if (message.getName().contains("image not okay")) {
				remove((BeadImage) message.getOrigin());
				setChanged();
				notifyObservers(new Message(this, "image not okay"));
				return;
			}

		}
		
		setChanged();
		notifyObservers(data);
	}

	/**
	 * Calculate the focus plane for a given bead image.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 */
	public void autoFocus(int beadImageId) {
		if (beadImageId >= 0 && beadImageId < countBeadImage()) {
			getBeadImage(beadImageId).autoFocus();
		}
	}

	/**
	 * Sets the focus plane for the specified bead image image.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @param focus
	 *            the focus plane
	 */
	public void setFocusPlane(int beadImageId, int focus) {
		if (beadImageId >= 0 && beadImageId < countBeadImage()) {
			getBeadImage(beadImageId).setFocusPlane(focus);
		}
	}

	/**
	 * Gets the focus point.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @return the focus point
	 */
	public int getFocusPlane(int beadImageId) {
		if (beadImageId >= 0 && beadImageId < countBeadImage()) {
			return getBeadImage(beadImageId).getFocusPlane();
		}
		return -2;
	}

	/**
	 * Gets the threshold value set for the bead image
	 * 
	 * @return the threshold value
	 */
	public int getThresholdValue() {
		if (countBeadImage() > 0)
			return getBeadImage(0).getThresholdValue();
		else
			return 0;
	}

	/**
	 * Gets the frame size
	 * 
	 * @return the frame size
	 */
	public int getFrameSize() {
		if (countBeadImage() > 0)
			return getBeadImage(0).getFrameSize();
		else
			return 0;
	}

	
	/**
	 * Sets the threshold value.
	 * 
	 * @param threshold
	 *            the new threshold value
	 */
	public void setThresholdValue(int threshold) {
		for (BeadImage beadImage : beadImageList) {

			System.out.println("** setting threshold to " + threshold);
			beadImage.setThresholdValue(threshold);
		}
	}

	/**
	 * Sets the threshold value.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @param thresholdValue
	 *            the threshold value
	 */
	public void setThresholdValue(int beadImageId, int thresholdValue) {
		if (getAnalysisType() == SINGLE_CHANNEL) {
			setThresholdValue(thresholdValue);
		} else {
			getBeadImage(beadImageId).setThresholdValue(thresholdValue);
		}
	}

	/**
	 * Sets the bead enlargement.
	 * 
	 * @param enlargement
	 *            the new bead enlargement
	 */

	public void setFrameSize(int enlargement) {
		for (BeadImage beadImage : beadImageList) {
			beadImage.setFrameSize(enlargement);
		}
	}

	/**
	 * Sets the frame size.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @param frameSize
	 *            the frame size in pixels
	 */
	public void setFrameSize(int beadImageId, int frameSize) {
		if (getAnalysisType() == SINGLE_CHANNEL) {
			setFrameSize(frameSize);
		} else {
			getBeadImage(beadImageId).setFrameSize(frameSize);
		}
	}

	/**
	 * Gets the bead frame size factor.
	 * 
	 * @return the bead frame size factor
	 */
	public int getBeadEnlargementFactor() {
		if (countBeadImage() > 0)
			return getBeadImage(0).getBeadEnlargementFactor();
		else
			return 0;
	}

	
	
	/**
	 * Sets the bead enlargement factor.
	 * 
	 * @param enlargement
	 *            the new bead enlargement factor
	 */
	public void setBeadEnlargementFactor(int enlargement) {
		for (BeadImage beadImage : beadImageList) {
			beadImage.setBeadEnlargementFactor(enlargement);
		}
	}

	/**
	 * Update the preview image asynchronously.
	 */
	public void updatePreviewAsync() {
		for (BeadImage beadImage : beadImageList) {
			beadImage.getBeadPreviewAsync();
		}
	}

	/**
	 * If the bead image focus plane and bead enlargement are not set, it launch
	 * the automated calculations.
	 */
	public synchronized void verifyBeadImageParameters() {

		// If one of them is not set
		for (BeadImage image : getBeadImageList()) {

			// verifying focus
			if (image.getFocusPlane() == BeadImage.NOT_SET) {
				// autofocus async is launched
				if (image.isProcessing() == false)
					;
				image.autoFocusAsync();
			}

			// values used on monocolor analysis
			int firstThreshold = getBeadImage(0).getThresholdValue();
			int firstFrameSize = getBeadImage(0).getFrameSize();

			// verifying threshold value
			if (image.getThresholdValue() == BeadImage.NOT_SET) {
				if (isDualColorAnalysis()) {
					image.autoThreshold();
				} else {
					if (firstThreshold == BeadImage.NOT_SET) {
						firstThreshold = getBeadImage(0).autoThreshold();
					}

					image.setThresholdValue(firstThreshold);

				}
			}

			// verifying frame size

			if (image.getFrameSize() == BeadImage.NOT_SET) {

				if (firstFrameSize == BeadImage.NOT_SET) {
					firstFrameSize = getBeadImage(0).autoFrameSize();
				}
				if (isDualColorAnalysis()) {
					image.autoFrameSize();
				} else
					image.setFrameSize(firstFrameSize);

			}

		}

		setChanged();
		notifyObservers(new Message(this, "verification over"));

	}

	/**
	 * Auto threshold.
	 * 
	 * @param i
	 *            the i
	 * @return the int
	 */
	public synchronized int autoThreshold(int i) {

		// does automatic threshold for all images

		if (getBeadImageCount() == 0)
			return 0;
		
		// if single color analysis
		if (getAnalysisType() == SINGLE_CHANNEL) {
			
			// auto threshold for each one
			for (BeadImage image : getBeadImageList()) {
				image.autoThreshold();
			}
			
			//but returns the first image threshold value
			return getBeadImage(0).getThresholdValue();

		} else {
			// or return the threshold of the specified image
			return getBeadImage(i).autoThreshold();
		}
	}

	/**
	 * Gets the bead image count. Obviously a duplicated method... but let it here for now
	 * 
	 * @return the bead image count
	 */
	private int getBeadImageCount() {
		return getBeadImageList().size();
	}

	/**
	 * Calculates and returns the automatic threshold for all the images
	 * 
	 * @return the int
	 */
	public synchronized int autoThreshold() {

		return autoThreshold(0);

	}

	/**
	 * Gets the data set of the field of view
	 * 
	 * @return the data set
	 */
	public FovDataSet getDataSet() {
		return dataSet;
	}

	/**
	 * Gets the z profile mean.
	 * 
	 * @param channel
	 *            the channel
	 * @return the z profile mean
	 */
	public double getZProfileMean(int channel) {

		double mean;
		
		
		if (isDualColorAnalysis()) {
			
			// the mean comes is taken from from the specified image
			mean = getBeadImage(channel)
					.getBeadFrameList()
					.getDataSet(true)
					.getColumnStatistics(
							PSFj.getColumnID(PSFj.Z_PROFILE,
									PSFj.NOT_NORMALIZED)).getMean();
		}
		
		
		// if is single color analysis, the mean comes from all images mixed together.
		else {

			mean = getMergedDataSet(true).getColumnStatistics(
					PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED))
					.getMean();
		}
		
		// displaying the z profile mean
		System.out.println(String.format(
				"z profile mean for channel %d = %.3f", channel, mean));
		return mean;
	}

	/**
	 * Gets the total amount of beads kept after filtering
	 * 
	 * @return the total amount of beads kept after filtering
	 */
	public int getKeptBeadsCount() {
		int keptBeadsCount = 0;
		// summing up all images kept bead count
		for (BeadImage image : getBeadImageList()) {
			keptBeadsCount += image.getValidBeadCount();
		}
		return keptBeadsCount;
	}

	/**
	 * Gets the analyzed bead count.
	 * 
	 * @return the analyzed bead count
	 */
	public int getAnalyzedBeadCount() {
		return getDeletedBeads() + getKeptBeadsCount();
	}

	/**
	 * Process profiles for each bead of each image stack.
	 */
	public void processProfiles() {
		
		// process the bead images
		processBeadImages();
		
		// if single analysis the dataset are merged to one to get more information about the field of view
		if (getAnalysisType() == SINGLE_CHANNEL) {
			mergeDataSets();
		}
		
		// in case of dual color analysis the dataset are compared
		else if (getAnalysisType() == DUAL_CHANNEL) {
			compareDataSets();
		}
		
		// the graph list are built
		buildGraphList();

		// if exporting has been set
		if (doExport)
			export();
		try {
			setChanged();
			notifyObservers(new Message(this, "process finished",
					"process finished"));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/** The graph list. */
	ArrayList<PsfJGraph> graphList;

	/**
	 * Builds the graph list.
	 */
	protected void buildGraphList() {
		
		// resetting the graph list and array
		graphList = new ArrayList<PsfJGraph>();
		graphHash = new HashMap<String, PsfJGraph>();
		
		
		String progressText = "Building heatmaps...";
		int progress = 10;
		System.gc();
		setProgress(progressText, progress += 10);
		
		// the graphs are different depending on the analysis type
		if (getAnalysisType() == DUAL_CHANNEL) {

			int channelId = 0;

			for (BeadImage image : getBeadImageList()) {

				// Getting the dataset for only the valid beads
				FovDataSet dataSet = image.getBeadFrameList()
						.getOnlyValidBeads().getDataSet(true, false);

				System.out.println(dataSet);
				setProgress(progressText, 30 + channelId * 20);
				// for each axe x,y,z
				for (int axe : PSFj.AXES) {

					// id used to retrieve the informations
					String columnId = PSFj.getColumnName(PSFj.FWHM_KEY, axe,
							PSFj.NOT_NORMALIZED);

					System.out.println(columnId);

					// id used to name the heatmap
					String heatmapId = PSFj.getHeatmapName(PSFj.FWHM_KEY, axe,
							channelId);

					System.out.println(columnId);
					// We create a FWMH Heatmap
					FWHMHeatMap heatmap = new FWHMHeatMap(dataSet, image,
							columnId);

					// Setting the title
					heatmap.setTitle(dataSet.getColumnName(columnId) + " of "
							+ image.getImageName());

					// Adding to the graph list and map
					graphList.add(heatmap);
					heatmap.getGenerator(PSFj.NOT_NORMALIZED)
							.getAnnotatedHeatMap();
					graphHash.put(heatmapId, heatmap);

				}

				// Generating ZProfile heatmap
				ZProfileHeatMap heatmap = new ZProfileHeatMap(dataSet, image);

				heatmap.setTitle("Planarity of " + image.getImageName());

				graphList.add(heatmap);
				
				graphHash.put(PSFj.getHeatmapName(PSFj.Z_PROFILE, channelId),
						heatmap);
				
				
				setProgress(progressText, 40 + channelId * 20);
				
				// Generating Asymetry heatmap
				FullHeatMap asymetryMap = new AsymmetryHeatMap(dataSet,
						getBeadImage(channelId), PSFj.getColumnID(
								PSFj.ASYMMETRY_KEY, PSFj.NOT_NORMALIZED, 0));
				// Theta map
				FullHeatMap thetaMap = new ThetaHeatMap(dataSet,
						getBeadImage(channelId), PSFj.getColumnID(
								PSFj.THETA_KEY, PSFj.NOT_NORMALIZED, 0));

				graphHash.put(
						PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, channelId),
						asymetryMap);
				graphHash.put(PSFj.getHeatmapName(PSFj.THETA_KEY, channelId),
						thetaMap);

				graphList.add(asymetryMap);
				graphList.add(thetaMap);

				channelId++;
			}

			
			FovDataSet dataSet = getBeadImage(0).getBeadFrameList()
					.getWithAlterEgo().getDataSet(true, true);
			BeadImage image = getBeadImage(0);
			Microscope m = image.getMicroscope();

			// Creating the chromatic shift heatmap ( use only x,y shift for
			// distance calculation )
			DistanceHeatMap distanceHeatMap = new DistanceHeatMap(dataSet,
					getBeadImage(0), PSFj.getColumnID(PSFj.CHR_SHIFT_XY,
							PSFj.NOT_NORMALIZED),
					m.getXYTheoreticalResolution());
			distanceHeatMap.setManager(this);
			// Creating the chromatic shift heatmap that use x,y,z shifts
			DistanceHeatMap distanceHeatMap3d = new DistanceHeatMap(dataSet,
					image, PSFj.getColumnID(PSFj.CHR_SHIFT_XYZ,
							PSFj.NOT_NORMALIZED), m.getZTheoreticalResolution());
			distanceHeatMap3d.setManager(this);
			// adding them to list and hash
			graphList.add(distanceHeatMap);
			graphHash.put(PSFj.getHeatmapName(PSFj.CHR_SHIFT_XY, -1),
					distanceHeatMap);

			graphList.add(distanceHeatMap3d);
			graphHash.put(PSFj.getHeatmapName(PSFj.CHR_SHIFT_XYZ, -1),
					distanceHeatMap3d);
			setProgress(progressText, 80);
			// Taking of shift heatmaps (x, y, z)
			for (int axe : PSFj.AXES) {

				String columnName = PSFj.getColumnID(PSFj.CHR_SHIFT_KEY, axe,
						PSFj.NOT_NORMALIZED, 0);
				String heatmapName = PSFj.getHeatmapName(PSFj.CHR_SHIFT_KEY,
						axe, channelId);

				ShiftHeatMap shiftHeatmap = new ShiftHeatMap(dataSet, image,
						columnName, m.getTheoreticalResolution(axe));

				shiftHeatmap.setManager(this);

				graphList.add(shiftHeatmap);
				graphHash.put(heatmapName, shiftHeatmap);
			}
			BeadMontage montage = new BeadMontage(this);
			graphList.add(montage);
			graphHash.put(PSFj.BEAD_MONTAGE_KEY, montage);

		}

		
		else {

			progress = 20;

			setProgress(progressText, progress += 15);
			for (int axe : PSFj.AXES) {
				mergeDataSets();

				String columnName = PSFj.getColumnID(PSFj.FWHM_KEY, axe,
						PSFj.NOT_NORMALIZED, 0);
				String heatmapName = PSFj
						.getHeatmapName(PSFj.FWHM_KEY, axe, -1);

				FWHMHeatMap heatmap = new FWHMHeatMap(dataSet, getBeadImage(0),
						columnName);
				graphList.add(heatmap);
				graphHash.put(heatmapName, heatmap);
				setProgress(progressText, progress += 15);
			}

			// z profile
			ZProfileHeatMap heatmap = new ZProfileHeatMap(dataSet,
					getBeadImage(0));
			heatmap.setTitle("Planarity");
			graphList.add(heatmap);
			graphHash.put(PSFj.getHeatmapName(PSFj.Z_PROFILE, -1), heatmap);
			setProgress(progressText, progress += 15);

			// Asymmetry
			FullHeatMap asymetryMap = new AsymmetryHeatMap(dataSet,
					getBeadImage(0), PSFj.getColumnID(PSFj.ASYMMETRY_KEY,
							PSFj.NOT_NORMALIZED, 0));
			setProgress(progressText, progress += 15);

			// theta
			FullHeatMap thetaMap = new ThetaHeatMap(dataSet, getBeadImage(0),
					PSFj.getColumnID(PSFj.THETA_KEY, PSFj.NOT_NORMALIZED, 0));

			graphHash.put(PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, -1),
					asymetryMap);
			graphHash.put(PSFj.getHeatmapName(PSFj.THETA_KEY, -1), thetaMap);

			setProgress(progressText, progress += 15);

			graphList.add(asymetryMap);
			graphList.add(thetaMap);
			setProgress(progressText, progress += 15);
		}

		for (BeadImage image : getBeadImageList()) {
			graphList.add(new BeadMap(image));
		}

		System.gc();

		setProgress("", 0);
	}

	/**
	 * Gets the graph.
	 * 
	 * @param id
	 *            the id
	 * @return the graph
	 */
	public PsfJGraph getGraph(String id) {
		return getGraphHash().get(id);
	}

	/**
	 * Gets the graph hash.
	 * 
	 * @return the graph hash
	 */
	public HashMap<String, PsfJGraph> getGraphHash() {
		if (graphHash == null) {
			buildGraphList();
		}

		return graphHash;

	}

	/**
	 * Gets the graph list.
	 * 
	 * @return the graph list
	 */
	public ArrayList<PsfJGraph> getGraphList() {
		if (graphList == null) {
			buildGraphList();
		}

		return graphList;

	}

	/**
	 * Process bead images one by one
	 */
	protected void processBeadImages() {
		
		// reseting the common dataset
		dataSet = new FovDataSet();
		
		graphList = null;
		System.gc();
		
		// setting all the bead image status to pending
		for (BeadImage beadImage : getBeadImageList()) {
			beadImage.setProgress(0, "Pending...");
		}
		
		for (BeadImage beadImage : getBeadImageList()) {
			
			//creating a async processing for better performances
			BeadFrameProcessor processor = new BeadFrameProcessorAsync(
					beadImage.getBeadFrameList());

			System.out.println(beadImage.getBeadFrameList().size());
			// adding the manager as observer
			processor.addObserver(this);
			
			//setting the fitting threshold (even through it's not really used...)
			processor.setFittingThreshold(fittingThreshold);
			
			// processing the beads
			processor.process();
			
			// filtering the beads
			processor.filter();
			
			processor.deleteObserver(this);
			
			//done
			beadImage.setProgress(100, "Done.");

		}
	}

	/**
	 * Compares the data sets in case of dual color analysis
	 */
	public void compareDataSets() {
		
		// merge first data set if really it's not a dual color analysis but this case shouldn't happen
		System.out.println("Data Set List : " + getBeadImageCount());
		if (getBeadImageCount() != 2) {
			mergeDataSets();
			return;
		}
		
		
		//getting two bead images
		BeadImage image1 = getBeadImage(0);
		BeadImage image2 = getBeadImage(1);
		
		// stats
		DescriptiveStatistics deltaDstats = new DescriptiveStatistics();

		BeadFrame closestBead;
		BeadFrame competitor;
		
		
		double distance;
		double competitorDistance;

		dataSet = new FovDataSet();

		int max = image1.getBeadFrameList().getValidBeadFrameCount();
		int count = 0;

		// Pairing beads with their nearest neighbour in the other channel
		for (BeadFrame bead : image1.getBeadFrameList().getOnlyValidBeads()) {

			if (count % 10 == 0) {
				setProgress("Comparing beads...", count * 100 / max);
			}
			count++;
			
			// getting the closest bead in the other channel
			closestBead = bead.getClosestBead(image2.getBeadFrameList()
					.getOnlyValidBeads());
			
			// if the closest doesn't exists (thresholdwise)
			if (closestBead == null) {
				continue;
			}
		
			else {
				// getting the distance
				distance = bead.getDistance(closestBead);
				
				if (closestBead.getAlterEgo() != null) {
					// if a bead has already been assigned the closest bead, it's a competitor.
					// Let's see which one is the closest.
					competitor = closestBead.getAlterEgo();
					
					// distances
					distance = bead.getDistance(closestBead);
					competitorDistance = closestBead.getDistance(competitor);
					
					// if the distance is less than the competitor, then this bead is the best match for the corresponding one
					if (distance < competitorDistance) {
						// System.out.println("* Competitor : OUT !!");
						
						// they are associated
						bead.setAlterEgo(closestBead);
						closestBead.setAlterEgo(bead);
						
						// the competitor is out
						competitor.resetAlterEgo();
						
						// and we explain to him why :-)
						competitor
								.setInvalidityReason("A closer corresponding bead exists : n°"
										+ bead.getId());
						deltaDstats.addValue(distance);
					}
					// if not, the competor wins and the current beads is left out.
					else {
						bead.resetAlterEgo();
						bead.setInvalidityReason("A closer corresponding bead exists : n°"
								+ competitor.getId());
					}

				}
				// if no competitor, then it's perfect
				else {
					bead.setAlterEgo(closestBead);
					closestBead.setAlterEgo(bead);
					deltaDstats.addValue(bead.getDistance(closestBead));
				}
			}
		}
		
		
		// forgot what it is
		double deltaD;

		// deleting beads too far away from their corresponding beads
		double threshold = getDistanceThreshold();// deltaDmedian
													// +
													// deltaDstdDev;
		count = 0;
		
		max = image1.getValidBeadCount();
		
		
		// for each bead that has a alter ego (corresponding bead in the other channel)
		for (BeadFrame bead : image1.getBeadFrameList().getOnlyValidBeads()
				.getWithAlterEgo()) {

			String reason = "";
			// the distance between those beads
			deltaD = bead.getDistance(bead.getAlterEgo());
			
			//if this distance is greater than the threshold, the bead is burned out
			if (deltaD > threshold) {
				reason = String
						.format("Corresponding bead too far away : %.3f µm (limit : %.3f µm)",
								deltaD, threshold);

				bead.getAlterEgo().setInvalidityReason(reason);
				bead.getAlterEgo().resetAlterEgo();
				bead.setInvalidityReason(reason);
				// bead.setValid(false,reason);
				bead.resetAlterEgo();
			}
			
			// refreshing some progress bar
			count++;
			if (count % 10 == 0) {
				setProgress("Comparing beads...", count * 100 / max);
			}

		}
		
		// just adding those information to the main data set (but is not really used)
		dataSet.mergeDataSet(image1.getBeadFrameList().getWithAlterEgo()
				.getDataSet());

	}

	/**
	 * Gets the merged data set of all beads
	 * 
	 * @param excludeInvalidBeads
	 *            the exclude invalid beads if true
	 * @return the merged dataset
	 */
	public FovDataSet getMergedDataSet(boolean excludeInvalidBeads) {
		// other metadata are saved
		FovDataSet dataSet = new FovDataSet();

		FovDataSet validBeadsDataSet = new FovDataSet();

		for (BeadImage image : getBeadImageList()) {
			dataSet.mergeDataSet(
					image.getBeadFrameList().getDataSet(excludeInvalidBeads,
							false), true);
			validBeadsDataSet.mergeDataSet(
					image.getBeadFrameList().getDataSet(true, false), true);
			System.out.printf("Merging Data sets <- %d\n",
					dataSet.getColumnSize());
		}

		return dataSet;
	}

	/**
	 * Merge data sets.
	 */
	public void mergeDataSets() {
		
		setDataSet(getMergedDataSet(true));

	}

	/**
	 * Compile the final data set.
	 */
	public void compileFinalDataSet() {
		for (BeadImage image : getBeadImageList()) {

			BeadFrameList list = image.getBeadFrameList();

			dataSet.setMetaDataValue(image.getImageName() + " analyzed beads",
					list.getTotalOfBeads());
			dataSet.setMetaDataValue(image.getImageName()
					+ " filtered out beads", list.getNonValidBeadFrameCount());
			dataSet.setMetaDataValue(image.getImageName() + " beads left",
					list.getValidBeadFrameCount());

			double stackSize = image.getCalibration().pixelDepth
					* image.getStackSize();
			dataSet.setMetaDataValue("z_profile_interval_min", 0);
			dataSet.setMetaDataValue("z_profile_interval_max", stackSize);
		}

		setProgress("Generating Heatmaps", 0);
		int i = 0;
		int numGraph = getGraphList().size();

		for (PsfJGraph graph : getGraphList()) {
			graph.getGraph();
			i++;
			setProgress("Generating heatmaps...", i * 100 / numGraph);
		}
		setProgress("", 0);
	}

	/**
	 * Gets the corresponding bead.
	 * 
	 * @param dataSet1
	 *            the data set1
	 * @param dataSet2
	 *            the data set2
	 * @param id
	 *            the id
	 * @return the corresponding bead
	 */
	public int getCorrespondingBead(FovDataSet dataSet1, FovDataSet dataSet2,
			int id) {

		double x;
		double y;
		double z;

		x = dataSet1.getDoubleValue("x", id);
		y = dataSet1.getDoubleValue("y", id);
		z = dataSet1.getDoubleValue("z_profile", id);

		Vector3D v = new Vector3D(x, y, z);
		DescriptiveStatistics distances = new DescriptiveStatistics();
		HashMap<Double, Integer> distancesToId = new HashMap<Double, Integer>();

		for (int i = 0; i != dataSet2.getColumnSize(); i++) {
			Vector3D v2 = new Vector3D(dataSet2.getDoubleValue("x", i),
					dataSet2.getDoubleValue("y", i), dataSet2.getDoubleValue(
							"z_profile", i));
			Vector3D d = v.subtract(v2);
			distances.addValue(d.getNorm());
			distancesToId.put(new Double(d.getNorm()), new Integer(i));
		}

		System.out.println("Corresponding bead to " + id + " (dist : "
				+ distances.getMin() + ")");
		return distancesToId.get(distances.getMin());

	}

	/**
	 * Export all generated documents
	 */
	public void export() {
		exportCSVFile();
		exportPDFSumUp();
		
		exportHeatMaps();
		exportInPDF();

	}

	/**
	 * Export xls file.
	 */
	@Deprecated
	public void exportXLSFile() {
		exportXLSFile(getExportDirectory() + getAnalysisName() + "_data.xls",
				false);
	}

	/**
	 * Export xls file.
	 * 
	 * @param path
	 *            the path
	 * @param openAfter
	 *            the open after
	 */
	public void exportXLSFile(String path, boolean openAfter) {
		exportCSVFile(path, "\t", openAfter);
	}

	/**
	 * Export csv files.
	 * 
	 * @param path
	 *            the folder to export
	 * @param openAfter
	 *            the open after
	 */
	public void exportCSVFile(String path, boolean openAfter) {
		exportCSVFile(path, ",", openAfter);
	}

	/**
	 * Export csv file(s) in the specified folder. Opens the folder if openAfter is true.
	 * 
	 * @param path
	 *            the path
	 * @param separator
	 *            the separator
	 * @param openAfter
	 *            the open after
	 */
	public void exportCSVFile(String path, String separator, boolean openAfter) {
		CsvExporter exporter = new CsvExporter(this);
		exporter.exportCsv(path, openAfter);
	}
        
        public void exportCSVFileV2(String path, String separator, boolean openAfter) {
            CsvExporter exporter = new CsvExporerParsable(this);
            exporter.exportCsv(path, openAfter);
        }

	/**
	 * Export csv file(s) in the export folder. Opens the folder if openAfter is true.
	 * 
	 * @param openAfter
	 *            the open after
	 */
	public void exportCSVFile(boolean openAfter) {
		String path;

		if (!isDualColorAnalysis()) {
			path = getExportDirectory() + getAnalysisName() + "_data.csv";
		} else {
			path = getExportDirectory();
		}
		exportCSVFile(path, openAfter);
	}

	/**
	 * Export csv file to the export folder but doesn't open it.
	 */
	public void exportCSVFile() {
		exportCSVFile(false);
	}

	/**
	 * Export in individual bead pdf reports and open them once finished.
	 */
	public void exportInPDF() {
		exportInPDF(true);
	}

	/**
	 * Checks if is dual color analysis.
	 * 
	 * @return true, if is dual color analysis
	 */
	public boolean isDualColorAnalysis() {
		return getAnalysisType() == DUAL_CHANNEL;
	}

	/**
	 * Export the individual bead pdf files and open them if openAfter is true
	 * 
	 * @param openAfter
	 *            the open after
	 */
	public void exportInPDF(boolean openAfter) {
		
		// create export
		PDFExporter pdfExporter = new PDFExporter(this);
		
		// the operation can take time
		pdfExporter.addObserver(this);
		
		// process the writting
		pdfExporter.writeSingleFileReport();

		// pdfExporter.loadImage("knoplablogo.png");
		pdfExporter.deleteObserver(this);
		
		// if it said to open after
		if (openAfter) {

			FileUtils.openFolder(getExportDirectory());

			FileUtils.openFolder(getExportDirectory()
					+ getBeadImage(0).getImageNameWithoutExtension()
					+ "_beads_page_1.pdf");
			if (isDualColorAnalysis())
				FileUtils.openFolder(getExportDirectory()
						+ getBeadImage(1).getImageNameWithoutExtension()
						+ "_beads_page_1.pdf");
		}

	}


	/**
	 * Export PDF Sum up.
	 */

	public void exportPDFSumUp() {
		exportPDFSumUp(true);
	}

	/**
	 * Export pdf sum up.
	 * 
	 * @param openAfter
	 *            the open after
	 */
	public void exportPDFSumUp(boolean openAfter) {
		PDFSumUpExporter exporter = new PDFSumUpExporter(this);
		exporter.export();
		exporter.closeDocument();
		exporter.closeDocument();
		setProgress("Sum-up exported", 0);
		if (openAfter)
			FileUtils.openFolder(exporter.getDefaultDocumentName());
	}

	public void exportHeatMaps() {
		exportHeatMaps(false);
	}

	/**
	 * Export heat maps.
	 */
	public void exportHeatMaps(boolean openAfter) {

		int size = getGraphList().size();
		int i = 1;
		setProgress("Generating heatmaps...", i * 100 / size);
		if (!FileUtils.folderExists(getExportDirectory())) {
			FileUtils.createFolder(getExportDirectory());
		}
		
		for (PsfJGraph graph : getGraphHash().values()) {
			try {
				FullHeatMap heatmap = (FullHeatMap) graph;
				saveHeatMap(heatmap);
			} catch (Exception e) {
				saveHeatMap(graph.getGraph(), graph.getSaveId(), "png");
			}
			i++;
			setProgress("Generating heatmaps...", i * 100 / size);
		}
		if (openAfter)
			FileUtils.openFolder(getExportDirectory());
		setProgress("", 0);
	}

	/**
	 * Save heat map.
	 * 
	 * @param heatmap
	 *            the heatmap
	 */
	public void saveHeatMap(FullHeatMap heatmap) {

		String channel = "";

		if (getAnalysisType() == DUAL_CHANNEL
				&& (heatmap instanceof FWHMHeatMap || heatmap instanceof ZProfileHeatMap)) {
			channel = "_"
					+ heatmap.getBeadImage().getMicroscope()
							.getWaveLengthAsString().replace(" ", "");
		}

		HeatMapGenerator generator = heatmap
				.getGenerator(PsfJGraph.NOT_NORMALIZED);
		saveHeatMap(generator.getColoredHeatMap(), heatmap.getSaveId()
				+ channel + "_colored", "png");
		saveHeatMap(generator.getRawHeatMap(), heatmap.getSaveId() + channel
				+ "_raw", "tif");
		saveHeatMap(generator.getAnnotatedHeatMap(), heatmap.getSaveId()
				+ channel + "_annotated", "png");
	}

	/**
	 * Save heat map.
	 * 
	 * @param heatmap
	 *            the heatmap
	 * @param suffix
	 *            the suffix
	 */
	public void saveHeatMap(ImageProcessor heatmap, String suffix) {
		saveHeatMap(heatmap, suffix, "png");
	}

	/**
	 * Save heat map.
	 * 
	 * @param heatmap
	 *            the heatmap
	 * @param suffix
	 *            the suffix
	 * @param format
	 *            the format
	 */
	public void saveHeatMap(ImageProcessor heatmap, String suffix, String format) {
		String path = getExportDirectory();
		String completePath = path + suffix + "." + format;
		if (format.contains("tif"))
			IJ.saveAsTiff(new ImagePlus("", heatmap), completePath);

		else if (format.contains("png")) {
			FileSaver fileSaver = new FileSaver(new ImagePlus("", heatmap));
			fileSaver.saveAsPng(completePath);
		}

	}

	/**
	 * Checks if is do export.
	 * 
	 * @return true, if is do export
	 */
	public boolean isDoExport() {
		return doExport;
	}

	/**
	 * Sets the do export.
	 * 
	 * @param doExport
	 *            the new do export
	 */
	public void setDoExport(boolean doExport) {
		this.doExport = doExport;
	}

	/**
	 * Reset data set.
	 */
	public void resetDataSet() {
		System.out.println("reseting dataset");
		dataSet = null;

		graphList = null;
		graphHash = null;

		for (BeadImage image : getBeadImageList()) {
			image.cleanMemory(image.CLEAN_ALL);
			//image.getBeadFrameList();
		}
	}

	/**
	 * Sets the indivicual bead data set.
	 * 
	 * @param d
	 *            the new data set
	 */
	public void setDataSet(FovDataSet d) {
		dataSet = d;
	}

	/**
	 * Sets the current column.
	 * 
	 * @param column
	 *            the new current column
	 */
	public void setCurrentColumn(String column) {
		currentColumn = column;
	}

	/**
	 * Gets the heat map step.
	 * 
	 * @return the heat map step
	 */
	public int getHeatMapStep() {
		return heatMapStep;
	}

	/**
	 * Sets the heat map step.
	 * 
	 * @param heatMapStep
	 *            the new heat map step
	 */
	public void setHeatMapStep(int heatMapStep) {
		this.heatMapStep = heatMapStep;
	}

	/**
	 * Calculates automatically the frame size for each bead image
	 */
	public void autoFrameSize() {
		for (BeadImage b : getBeadImageList()) {
			b.autoFrameSize();
		}
	}

	/**
	 * Sets the export directory.
	 * 
	 * @param text
	 *            the new export directory
	 */
	public void setExportDirectory(String text) {
		if (text.endsWith("/") == false) {
			text += "/";
		}
		if (!new File(text).exists())
			FileUtils.createFolder(text);
		exportDirectory = text;
	}

	/**
	 * Gets the export directory.
	 * 
	 * @return the export directory
	 */
	public String getExportDirectory() {
		if (exportDirectory == null) {
			exportDirectory = new File(getBeadImage(0).getFileAddress())
					.getParentFile().getAbsolutePath();
		}
		return exportDirectory;
	}

	/**
	 * Merge data set.
	 * 
	 * @param selectedFile
	 *            the selected file
	 */
	public void mergeDataSet(File selectedFile) {
		getDataSet().mergeDataSet(
				new FovDataSet(selectedFile.getAbsolutePath()));
	}

	/**
	 * Sets the progress for observers
	 * 
	 * @param message
	 *            the message
	 * @param progress
	 *            the progress
	 */
	public void setProgress(String message, Integer progress) {
		setChanged();
		notifyObservers(new Message(this, "progress changed", message, progress));
	}

	/**
	 * Gets the file names of each bead image (useful for exporting)
	 * 
	 * @return the file names
	 */
	public ArrayList<String> getFileNames() {
		ArrayList<String> fileNames = new ArrayList<String>();

		for (BeadImage beadImage : getBeadImageList()) {
			fileNames.add(beadImage.getImageName());
		}

		return fileNames;

	}

	/**
	 * Gets the bead frame list of all bead images.
	 * 
	 * @return the bead frame list
	 */
	public BeadFrameList getBeadFrameList() {
		BeadFrameList list = new BeadFrameList();
		for (BeadImage image : getBeadImageList()) {
			list.mergeList(image.getBeadFrameList());
		}

		return list;
	}

	/**
	 * Gets the fitting threshold.
	 * 
	 * @return the fitting threshold
	 */
	public double getFittingThreshold() {
		return fittingThreshold;
	}

	/**
	 * Sets the fitting threshold.
	 * 
	 * @param fittingThreshold
	 *            the new fitting threshold
	 */
	public void setFittingThreshold(double fittingThreshold) {
		this.fittingThreshold = fittingThreshold;
	}

	/**
	 * Gets the deleted beads.
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @return the deleted beads
	 */
	public int getDeletedBeads(int beadImageId) {
		return getBeadImage(beadImageId).getDeletedBeadsCount();
	}

	/**
	 * Gets the paired beads.
	 * 
	 * @return the paired beads
	 */
	public int getPairedBeads() {
		return getBeadImage(0).getBeadFrameList().getPairedCount();
	}

	/**
	 * Gets the deleted beads.
	 * 
	 * @return the deleted beads
	 */
	public int getDeletedBeads() {

		int deletedBeads = 0;
		for (BeadImage image : getBeadImageList()) {
			deletedBeads += image.getDeletedBeadsCount();
		}
		return deletedBeads;
	}

	/**
	 * Gets the analysis name.
	 * 
	 * @return the analysis name
	 */
	public String getAnalysisName() {
		return FileUtils.getFileNameWithoutExtension(getBeadImage(0)
				.getImageName());
	}

	/**
	 * Gets the analysis type.
	 * 
	 * @return the analysis type
	 */
	public int getAnalysisType() {

		if (getBeadImageCount() <= 1)
			return SINGLE_CHANNEL;

		return analysisType;
	}

	/**
	 * Sets the analysis type.
	 * 
	 * @param analysisType
	 *            the new analysis type
	 */
	public void setAnalysisType(int analysisType) {

		if (analysisType != this.analysisType) {

			this.analysisType = analysisType;

			setChanged();
			notifyObservers(new Message(this, "analysis type changed"));
		}
	}
        
       
        
        public BeadLocator getLocator() {
            return locator;
        }
        
        
        public void setLocator(BeadLocator locator) {
            
            System.out.println("Updating model to "+locator.getClass().getSimpleName());
            this.locator = locator;
            for(BeadImage image : getBeadImageList()) {
                image.setLocator(locator);
            }
            
            
            
        }

	/**
	 * Gets the calibration of a defined bead image from its id
	 * 
	 * @param beadImageId
	 *            the bead image id
	 * @return the calibration
	 */
	public Calibration getCalibration(int beadImageId) {
		return getMicroscope(beadImageId).getCalibration();
	}

	
	/**
	 * Detects the informations in the microscope sources are the same as the microscope informations contained in the added
	 * stacks
	 * @param source
	 * @return true is the configuration will override the configurations of the bead images.
	 */
	public boolean isThereMicroscopeDataOverriding(Microscope source) {

		// list of booleans
		ArrayList<Boolean> allValueAreEqual = new ArrayList<Boolean>();

		// There is overriding when at least one value is different and the
		// target not equal to 0
		for (BeadImage image : getBeadImageList()) {

			Microscope target = image.getMicroscope();

			// there is NO overriding if the values are the same or if the
			// target microscope equals to 0
			if (!isDualColorAnalysis()) {
				allValueAreEqual.add(source.getWaveLength() == target
						.getWaveLength() || target.getWaveLength() <= 0);
			}
			allValueAreEqual.add(source.getBeadDiameter() == target
					.getBeadDiameter() || target.getBeadDiameter() <= 0);
			allValueAreEqual.add(source.getNA() == target.getNA()
					|| target.getNA() <= 0);
			allValueAreEqual.add(source.getRefractionIndex() == target
					.getRefractionIndex() || target.getNA() <= 0);
			allValueAreEqual.add(source.getPixelWidth() == target
					.getPixelWidth() || target.getPixelWidth() <= 0);
			allValueAreEqual.add(source.getPixelDepth() == target
					.getPixelDepth() || target.getPixelDepth() <= 0);
			allValueAreEqual.add(source.getPixelHeight() == target
					.getPixelHeight() || target.getPixelHeight() <= 0);
		}
		int i = 0;
		for (Boolean b : allValueAreEqual) {
			System.out.println(String.format("question %d = ", i++) + b);
		}
		
		return !PSFj.testList(allValueAreEqual);
	}

	/**
	 * Update the microscope informations of the bead images with the given configuration
	 * 
	 * @param microscope
	 *            the microscope configuration that should override the rest
	 */
	public void updateMicroscope(Microscope microscope) {
		boolean includeWaveLength = (getAnalysisType() == SINGLE_CHANNEL);
		for (BeadImage b : getBeadImageList()) {

			b.getMicroscope().mergeFromOtherMicroscope(microscope,
					includeWaveLength);
		}
	}

	/**
	 * Reset the preview.
	 */
	public void resetPreview() {
		getBeadImage(0).resetPreview();

	}

	/**
	 * Gets the heatmap statistics as string.
	 * 
	 * @param heatmapId
	 *            the heatmap id
	 * @param normalized
	 *            the normalized or not
	 * @param unit
	 *            the unit
	 * @return the heatmap statistics as string
	 */
	public String getHeatmapStatisticsAsString(String heatmapId,
			int normalized, String unit) {
		DescriptiveStatistics stats = getHeatmapStatistics(heatmapId,
				normalized);

		if (heatmapId.contains(PSFj.Z_PROFILE)) {
			return MathUtils.PLUS_MINUS
					+ " "
					+ MathUtils
							.formatDouble(stats.getStandardDeviation(), unit);
		}

		return MathUtils.formatStatistics(stats, unit);

	}

	/**
	 * Gets the heatmap statistics as string.
	 * 
	 * @param heatmap
	 *            the heatmap
	 * @param normalized
	 *            the normalized
	 * @return the heatmap statistics as string
	 */
	public String getHeatmapStatisticsAsString(FullHeatMap heatmap,
			int normalized) {

		if (heatmap.getField().contains(PSFj.Z_PROFILE)) {
			return MathUtils.PLUS_MINUS
					+ " "
					+ MathUtils.formatDouble(
							getHeatmapStatistics(heatmap, normalized)
									.getStandardDeviation(), heatmap
									.getGenerator(normalized).getUnit());
		}

		return MathUtils.formatStatistics(
				getHeatmapStatistics(heatmap, normalized), heatmap
						.getGenerator(normalized).getUnit());
	}

	/**
	 * Gets the heatmap statistics.
	 * 
	 * @param graph
	 *            the graph
	 * @param normalized
	 *            the normalized
	 * @return the heatmap statistics
	 */
	public DescriptiveStatistics getHeatmapStatistics(PsfJGraph graph,
			int normalized) {
		DescriptiveStatistics stats = new DescriptiveStatistics();

		try {
			FullHeatMap heatmap = (FullHeatMap) graph;

			HeatMapGenerator generator = heatmap.getGenerator(normalized);

			if (generator.getSpace().size() == 0) {
				System.out
						.println("Not enough beads, getting column statistics");
				stats = generator.getCurrnetColumnStatistics();
			} else {
				ImageProcessor ip = heatmap.getGenerator(normalized)
						.getSpaceImage();

				for (float f : (float[]) ip.getPixels()) {
					if (!Double.isNaN(f))
						stats.addValue(f);
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
		return stats;
	}

	/**
	 * Gets the heatmap statistics.
	 * 
	 * @param heatmapId
	 *            the heatmap id
	 * @param normalized
	 *            the normalized
	 * @return the heatmap statistics
	 */
	public DescriptiveStatistics getHeatmapStatistics(String heatmapId,
			int normalized) {
		if (getGraph(heatmapId) == null) {
			System.err.println("Error when getting heatmap " + heatmapId);
			return new DescriptiveStatistics();
		}

		return getHeatmapStatistics(getGraph(heatmapId), normalized);

	}

	/**
	 * Gets the merged preview in case of dual color analysis (not sure if it's used...)
	 * 
	 * @param displayedImage
	 *            the displayed image
	 * @return the merged preview
	 */
	public ImageProcessor getMergedPreview(int displayedImage) {
		ImageProcessor ip = getBeadImage(0).getMiddleImage()
				.convertToByte(true);
		ImageProcessor ip2 = getBeadImage(1).getMiddleImage().convertToByte(
				true);

		ip.setLut(LUT.createLutFromColor(getBeadImage(0).getBeadsColor()));
		ip2.setLut(LUT.createLutFromColor(getBeadImage(1).getBeadsColor()));

		ip = ip.convertToRGB();
		ip2 = ip2.convertToRGB();

		ip.copyBits(ip2, 0, 0, Blitter.ADD);

		for (BeadFrame frame : getBeadImage(displayedImage).getBeadFrameList()) {
			frame.draw(ip, getBeadImage(displayedImage).getFrameSize(),
					Color.white);
		}

		return ip;
	}

	/**
	 * Gets the merged map.
	 * 
	 * @return the merged map
	 */
	public ImageProcessor getMergedMap() {

		return null;
	}

	/**
	 * Gets the ignored beads count.
	 * 
	 * @return the ignored beads count
	 */
	public int getIgnoredBeadsCount() {
		int count = 0;
		for (BeadImage image : getBeadImageList()) {
			count += image.getIgnoredFrameNumber();
		}
		return count;
	}

	/**
	 * Gets the distance threshold.
	 * 
	 * @return the distance threshold
	 */
	public double getDistanceThreshold() {
		// TODO Auto-generated method stub
		return getMicroscope(0).getXYTheoreticalResolution() * 2;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
        
        public boolean isImageLoading() {
            for(BeadImage image : getBeadImageList()) {
                if(image.isOpening == true) {
                    return true;
                }
            }
            return false;
        }

}
