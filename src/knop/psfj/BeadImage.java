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
package knop.psfj;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import knop.psfj.heatmap.EasyLUT;
import knop.psfj.locator.BeadLocator;
import knop.psfj.locator.BeadLocator3D;
import knop.psfj.resolution.Counter3D;
import knop.psfj.resolution.DataTricks;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.MathUtils;
import knop.psfj.utils.VisibleColor;
import knop.psfj.view.Message;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * The Class BeadImage.
 */
public class BeadImage extends Observable {

    /**
     * The file address.
     */
    String fileAddress;

    /**
     * The stack size.
     */
    int stackSize = -1;

    /**
     * The dot location : set of Rectangle of a view pixels wide determining the
     * position of beads.
     */
    ArrayList<Rectangle> beadLocation;

    /**
     * The dot frames : calculated from the the dotLocation, each Rectangle in
     * this list represents the extracted area of a bead.
     */
    BeadFrameList beadFrames = new BeadFrameList();

    /**
     * Image Processor Reader object : used when working from disk.
     */
    private ImageProcessorReader ipr = new ImageProcessorReader();

    /**
     * Rhe original image.
     */
    private ImageStack stack;

    /**
     * Image corresponding to the bead focal plane.
     */
    private ImageProcessor middleImage;

    /**
     * List of stack images : each one is a bead.
     */
    private HashMap<Integer, ImageStack> stackList; // = new HashMap<Integer,
    // ImageStack>();

    /**
     * The not set.
     */
    public static int NOT_SET = -1;

    /**
     * The not found.
     */
    public static int NOT_FOUND = -9;

    /**
     * The threshold level for the background substraction.
     */
    protected int thresholdValue = NOT_SET;

    /**
     * The size of the area extracted for each bead.
     */
    protected int frameSize = NOT_SET;

    /**
     * When auto calculating the automatic frame size will use
     * beadEnlargementFactor * sigma in pixel.
     */
    protected int beadEnlargementFactor = 10;

    /**
     * The lut min.
     */
    protected double minIntentisyOfWholeStack;

    /**
     * The lut max.
     */
    protected double maxIntensityOfWholeStack = NOT_SET;

    /**
     * The segmentation mask.
     */
    protected ImageProcessor segmentationMask;

    /**
     * The preview of the process.
     */
    protected ImageProcessor preview;

    /**
     * The image width.
     */
    protected int imageWidth;

    /**
     * The image height.
     */
    protected int imageHeight;

    /**
     * The preview width.
     */
    protected int previewWidth = 500;

    /**
     * The preview height.
     */
    protected int previewHeight = 500;

    /**
     * The preview x.
     */
    protected int previewX = 0;

    /**
     * The preview y.
     */
    protected int previewY = 0;

    /**
     * The bead focus plane.
     */
    protected int beadFocusPlane = NOT_SET;

    /**
     * The image name.
     */
    protected String imageName;

    /**
     * The image path.
     */
    protected String imageFolder = null;

    /**
     * The min threshold.
     */
    protected int minThreshold = NOT_SET;

    /**
     * The max threshold.
     */
    protected int maxThreshold = NOT_SET;

    /**
     * The file size.
     */
    protected double fileSize;

    /**
     * The progress.
     */
    protected int progress;

    /**
     * The is valid.
     */
    protected boolean isValid = false;

    /**
     * The is opening.
     */
    protected boolean isOpening = false;

    /**
     * The image disk size.
     */
    protected long imageDiskSize = -1;

    /**
     * The raw preview.
     */
    protected ImageProcessor rawPreview = null;

    /**
     * The status.
     */
    protected String status;

    /**
     * The frame number.
     */
    protected int frameNumber;

    protected BeadLocator locator = new BeadLocator3D();

    /**
     * ignored frame number.
     */
    protected int ignoredFrameNumber = 0;

    /**
     * The lut.
     */
    protected EasyLUT lut = new EasyLUT("blue_orange_white");

    /**
     * clean but let stacks in memory.
     */
    public static int CLEAN_BUT_LET_STACKS_IN_MEMORY = 0;

    /**
     * clean all.
     */
    public static int CLEAN_ALL = 1;

    /**
     * clean for deletion.
     */
    public static int CLEAN_FOR_DELETION = 2;

    /**
     * The clean frames.
     */
    public static int CLEAN_FRAMES = 3;

    /**
     * The counter3d.
     */
    Counter3D counter3d;

    /**
     * The microscope.
     */
    protected Microscope microscope;

    public static final String MSG_PARAMETER_CHANGED = "parameter changed";

    public static final String MSG_THRESHOLD_CHANGED = "threshold changed";
    public static final String MSG_FOCUS_CHANGED = "focus changed";
    public static final String MSG_FRAMESIZE_CHANGED = "bead enlargement changed";

    public static final String MSG_PREVIEW_UPDATED = "preview updated";
    public static final String MSG_IMAGE_NOT_OKAY = "image not okay";
    public static final String MSG_IMAGE_OKAY = "image okay";

    public static final String MSG_NEW_CHANNEL_DETECTED = "other channel detected";

    /**
     * Instantiates a new bead image.
     */
    public BeadImage() {

    }

    /**
     * The Constant TOP_LEFT_CORNER.
     */
    public final static int TOP_LEFT_CORNER = 0;

    /**
     * The Constant TOP_RIGHT_CORNER.
     */
    public final static int TOP_RIGHT_CORNER = 1;

    /**
     * The Constant BOTTOM_LEFT_CORNER.
     */
    public final static int BOTTOM_LEFT_CORNER = 2;

    /**
     * The Constant BOTTOM_RIGHT_CORNER.
     */
    public final static int BOTTOM_RIGHT_CORNER = 3;

    /**
     * The Constant CENTER.
     */
    public final static int CENTER = 4;

    /**
     * Instantiates a new bead image from the file with the specified address.
     *
     * @param s the address of the file
     */
    public BeadImage(String s) {
        super();
        setFileAddress(s);

    }

    /**
     * Instantiates a new bead image with an image stack address.
     *
     * @param s the ImageStack
     */
    public BeadImage(ImageStack s) {
        super();
        setStack(s);

    }

    /**
     * Sets the stack image.
     *
     * @param s the new stack
     */
    public void setStack(ImageStack s) {
        stack = s;

        setImageHeight(s.getHeight());
        setImageWidth(s.getWidth());

    }

    /**
     * Checks if the image is in memory or if we work from the disk.
     *
     * @return true, if is in memory
     */
    public boolean isInMemory() {
        return (stack != null);
    }

    /**
     * Checks if is the object is opening the image.
     *
     * @return true, if is opening
     */
    public boolean isOpening() {
        return isOpening;
    }

    /**
     * The main method.
     *
     * @param argv the arguments
     */
    public static void main(String[] argv) {

        BeadImage startImage = new BeadImage(
                "/Users/cyril/Downloads/ApoTIRF_60x_dual_channel/ch2_ApoTIRF_60x.tif");

        startImage.workFromMemory();

     
        startImage.autoFocus();
        startImage.autoThreshold();
        startImage.setFrameSize(20);
        BeadFrameProcessor processor = new BeadFrameProcessorAsync(
                startImage.getBeadFrameList());
        //startImage.buildStackList();
        processor.process();
        processor.filter();
        System.out.println("signal to noise ration : "  + startImage.getSignalToNoiseRatio());

		
    }

    /**
     * Sign of a double.
     *
     * @param d the double
     * @return -1 for negative doubles, 1 for positive ones
     */
    public int signOf(double d) {
        if (d > 0) {
            return 1;
        }
        if (d < 0) {
            return -1;
        }
        return 0;

    }

    /**
     * Wait until the image is opened.
     */
    public void waitForOpening() {
        while (isOpening()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the image name.
     *
     * @param name the new image name
     */
    public void setImageName(String name) {
        Pattern p = Pattern.compile("(.*)[.]\\w$");
        Matcher m = p.matcher(name);

        if (!m.matches()) {
            imageName = name;
        } else {
            imageName = m.group(1);
        }

        imageName = name;
    }

    /**
     * Sets the image path.
     *
     * @param path the new image path
     */
    public void setImageFolder(String path) {

        if (path.endsWith("/") == false) {
            path = path + "/";
        }

        imageFolder = path;
    }

    /**
     * Gets the image name.
     *
     * @return the image name
     */
    public String getImageName() {

        return imageName;
    }

    /**
     * Gets the image name without extension.
     *
     * @return the image name without extension
     */
    public String getImageNameWithoutExtension() {

        return getImageName().replace(".tif", "");
    }

    /**
     * Gets the image path.
     *
     * @return the image path
     */
    public String getImageFolder() {
        return imageFolder;
    }

    /**
     * Gets the image intensity sum.
     *
     * @param ip the ip
     * @return the image intensity sum
     */
    public double getImageIntensitySum(ImageProcessor ip) {

        double sum = 0;
        for (int x = 0; x != ip.getWidth(); x++) {
            for (int y = 0; y != ip.getHeight(); y++) {
                sum += (ip.getPixel(x, y));
            }
        }

        return sum;

    }

    /**
     * Gets the file address.
     *
     * @return the file address
     */
    public String getFileAddress() {
        return fileAddress;
    }

    /**
     * Gets the fiel of view area.
     *
     * @return the fiel of view area
     */
    public double getFielOfViewArea() {
        return getCalibration().pixelWidth * getImageWidth() * getImageHeight()
                * getCalibration().pixelHeight;
    }

    /**
     * Gets the field of view width.
     *
     * @return the field of view width
     */
    public double getFieldOfViewWidth() {
        return getCalibration().pixelWidth * getImageWidth();
    }

    /**
     * Gets the field of view height.
     *
     * @return the field of view height
     */
    public double getFieldOfViewHeight() {
        return getCalibration().pixelHeight * getImageHeight();
    }

    /**
     * Gets the bead density.
     *
     * @return the bead density
     */
    public double getBeadDensity() {

        return getBeadLocation().size() / getFielOfViewArea();

    }

    /**
     * Gets the background intensity.
     *
     * @return the background intensity
     */
    public double getBackgroundIntensity() {
        ImageStatistics stats = getPlane(0).getStatistics();
        return stats.mean + stats.stdDev;
    }

    /**
     * Read width and height from disk.
     */
    public synchronized void readWidthAndHeightFromDisk() {
        ImageProcessorReader ipr = new ImageProcessorReader(
                new ChannelSeparator(LociPrefs.makeImageReader()));

        try {
            ipr.setId(fileAddress);

        } catch (FormatException e) {

            e.printStackTrace();
            try {
                ipr.close();
            } catch (IOException e1) {

                e1.printStackTrace();
            }
            isValid = false;
            return;

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int width = ipr.getSizeX();
        int height = ipr.getSizeY();
        int numChannel = ipr.getSizeC();
        int bitsPerPixel = ipr.getBitsPerPixel();

        if (numChannel > 2) {
            notifyObservers("error", "Only monochannel image can be added.", null, null);
            isValid = false;
            return;
        }

        if (bitsPerPixel > 16) {
            isValid = false;
            setInvalidityReason("Only 8-bits and 16-bits tif images are supported.");
            setProgress(0, MSG_IMAGE_OKAY);

            notifyObservers(new Message(this, "error", "Only 8-bits and 16-bits tif images are supported."));
            try {
                ipr.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return;
        }

        setImageWidth(width);
        setImageHeight(height);

    }

    /**
     * load the stack into memory.
     */
    public synchronized void workFromMemory() {
		// if the fileAddress is not equal to null, this means that
        // the image has not been previously load into memory
        if (fileAddress != null && stack == null) {
            System.out.println("Loading image in memory...");
            setProgress(0, "Loading image in memory...");

            try {
				// BF.openImagePlus(path)

				// stack = BF.openImagePlus(fileAddress)[0].getImageStack();
				// stack = new
                // Opener().openImage(fileAddress).getImageStack();//
                // IJ.openImage(fileAddress).getStack();
                ImageProcessorReader ipr = new ImageProcessorReader(
                        new ChannelSeparator(LociPrefs.makeImageReader()));
                DescriptiveStatistics standardDeviations = new DescriptiveStatistics();

                ipr.setId(fileAddress);

                int width = ipr.getSizeX();
                int height = ipr.getSizeY();
                int num = ipr.getImageCount();
                int numChannel = ipr.getSizeC();
                int bitsPerPixel = ipr.getBitsPerPixel();

                double stdDev;
                double min;
                double max;

                // if a second channel in the image is detected
                boolean isSecondChannel = false;
                BeadImage secondChannel = null;
                ImageStack secondStack = null;

                if (numChannel == 2) {

                    secondChannel = new BeadImage();
                    secondChannel.setFileAddress(fileAddress);
                    secondChannel.setImageName(secondChannel.getImageName()
                            + "_channel_2");
                    secondStack = new ImageStack(width, height);
                    isSecondChannel = true;
                    secondChannel.setStack(secondStack);

                    notifyObservers(MSG_NEW_CHANNEL_DETECTED,
                            "Two channels detected", null, secondChannel);

                }

                stack = new ImageStack(width, height);

                for (int i = 0; i != num; i++) {
                    setProgress(i, num);
                    setStatus("Loading slice " + (i + 1) + "/" + num + "...");

                    ImageProcessor ip = ipr.openProcessors(i)[0];

                    if (isSecondChannel) {
                        i++;
                        ImageProcessor ip2 = ipr.openProcessors(i)[0];
                        secondStack.addSlice(ip2);
                        secondChannel.setStatus("Loading slice " + (i + 1) + "/"
                                + num + "...");
                        secondChannel.setProgress(i, num);
                    }

                    min = ip.getMin();
                    max = ip.getMax();

                    if (min < minIntentisyOfWholeStack) {
                        minIntentisyOfWholeStack = min;
                    }
                    if (max > maxIntensityOfWholeStack) {
                        maxIntensityOfWholeStack = max;
                    }

                    stdDev = ip.getStatistics().stdDev;

                    if (standardDeviations.getMax() < stdDev) {
                        beadFocusPlane = i;
                    }
                    standardDeviations.addValue(stdDev);
                    stack.addSlice(ip);
                    updateView(ip);
                }
                if (isSecondChannel) {
                    autoFocus();
                    secondChannel.autoFocus();
                    secondChannel.setProgress(100);
                }
                try {
                    ipr.close();
                } catch (Exception e) {
                    System.err.println("Error when closing the image reader.");
                }
                setChanged();
                notifyObservers(new Message(this, MSG_IMAGE_OKAY));
                setStatus("Done.");
                setProgress(100);

                setImageHeight(stack.getHeight());
                setImageWidth(stack.getWidth());

                isValid = true;
                new ImagePlus("", stack).resetDisplayRange();
                // openImage();

            } catch (NullPointerException e) {
                notifyError("Image not valid.");
                e.printStackTrace();
                setProgress(0, "image not okay");
                return;
            } catch (FormatException e) {
                setProgress(0, "This image format is not supported.");
                notifyError("This image format is not supported");
                e.printStackTrace();
            } catch (IOException e) {

                notifyError("File not accessible");
                setProgress(0, "Image not reachable.");
                e.printStackTrace();
            }

        }
    }

    /**
     * Sets the file address and load it if the parameter "load" is true.
     *
     * @param fileAddress the file address
     * @param load load the stack if true
     */
    public void setFileAddress(String fileAddress, boolean load) {

        setFileAddress(fileAddress);
        if (load) {
            workFromMemory();
        }

    }

    /**
     * Sets the file address.
     *
     * @param fileAddress the new file address
     */
    public void setFileAddress(String fileAddress) {

        this.fileAddress = fileAddress;

        File file = new File(fileAddress);

        setImageFolder(file.getParent());
        setImageName(file.getName());

        // make sure to store the image size in memory for further use
        getImageDiskSize();

    }

    /**
     * Notify the observer of an error.
     *
     * @param error the error message
     */
    public void notifyError(String error) {
        setChanged();
        notifyObservers(new Message(this, "error", error));
    }

    /**
     * Gets the raw focus point, without launching the auto calculation
     * algorithm if the focus point is not set.
     *
     * @return the raw focus point
     */
    public int getRawFocusPoint() {
        return beadFocusPlane;
    }

    /**
     * Gets the stack size.
     *
     * @return the stack size
     */
    public int getStackSize() {
        if (stack != null) {
            return stack.getSize();
        } else {
            return 0;
        }
    }

    /**
     * Gets the individual bead stack with the specified id.
     *
     * @param id the bead id
     * @return the image stack of the bead
     */
    public ImageStack getStack(int id) {
        if (stackList == null) {
            buildStackList();
        }

        if (id >= stackList.size()) {
            System.err.println("Stack " + id + " doesn't exist.");
            return null;
        }

        return stackList.get(id);

    }

    /**
     * Builds the stack list : retrieve every bead from the original image stack
     * and store them into a ImageStack array.
     *
     * A list of bead stack is created and each bead stack is filled while
     * loading (and browsing) the stack.
     */
    public void buildStackList() {

        if (stackList != null) {
            return;
        }

        stackList = new HashMap<Integer, ImageStack>();

        setStatus("Retreiving stacks...");

        stackList.clear();

        long start = System.currentTimeMillis();

        // creating an hashmap for storing the different planes
        final HashMap<Integer, ImageProcessor> planes = new HashMap<Integer, ImageProcessor>();

        // an thread that load the images from the disk is created
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i != getStackSize(); i++) {
                    planes.put(i, getPlane(i));
                }
            }
        }.start();

        // while the loading thread is on...
        BeadFrameList frames = getBeadFrameList();

        System.out.println(frames.size() + " beads to retrieve !");
        // for each plane of the stack
        for (int i = 0; i != getStackSize(); i++) {

            // we wait for each plane to loaded
            while (planes.containsKey(i) == false) {
                try {
                    Thread.currentThread();
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                };
            }

			// the plane is loaded !! Let's start doing some processing
            // (in the mean time, the next thread starts to be loadedà
            // System.out.println("Start processing " + i + " ...");
            // getting the plane
            ImageProcessor sp = planes.get(i);

			// for each bead frame,
            for (BeadFrame frame : frames) {

                final int stackNumber = i;

                // copy the part of the frame from the plane image
                ImageProcessor imagePart = copyRoi(sp,
                        frame.getBoundariesAsRectangle());

				// if the current plane is the first, a new image stack
                // corresponding to the bead is created
                if (stackNumber == 0) {
                    stackList.put(frame.getId(), new ImageStack(
                            imagePart.getWidth(), imagePart.getHeight()));
                }

                // adding the slice to the bead stack
                stackList.get(frame.getId()).addSlice(imagePart);

            }

            // System.out.println("Stop processing " + i + "");
            setProgress(i, getStackSize());

			// freeing some memory
            planes.put(i, null);
            planes.put(i, sp.createProcessor(1, 1));
            sp = null;

        }

        for (Integer id : stackList.keySet()) {
            frames.getFromId(id).setSubstack(new ImagePlus("", stackList.get(id)));
        }

        planes.clear();
        stackList.clear();
        stackList = null;

        System.gc();
        long end = System.currentTimeMillis();

        setStatus("Stacks retreived.");
        setProgress(100);

        System.out.println("Stacks retreived in  " + (end - start) + " ms.");

    }

    /**
     * Gets the plane.
     *
     * @param id the plane id
     * @return the plane image processor
     */
    public ImageProcessor getPlane(int id) {

		// System.out.println("Querying plan " + id + "...");
		/*
         * if (id == -1) { findBeadFocusPlaneWithHeuristic(); }
         */
        try {
            if (stack != null) {
                //System.out.println("Getting plane " + id + "...");
                ImageProcessor plane = stack.getProcessor(id + 1);
				// plane.setColorModel(lut.getColorModel());
                //plane.setLut(lut.getLUT(lutMin, lutMax * 0.8));
                return plane;
            } else {
                try {

                    ImageProcessor plane = ipr.openProcessors(id)[0];

                    plane.setColorModel(lut.getColorModel());

                    return plane;
                } catch (FormatException e) {

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();

                }
            }
        } catch (Exception e) {
			// setProgress(0);
            // setStatus("Can't open image.");

        }

        System.err.println("Couldn't get stack " + id + " !");
        return null;
    }

    /**
     * Gets the stack.
     *
     * @return the stack
     */
    public ImageStack getStack() {
        return stack;
    }

    /**
     * Gets the image corresponding to the focal plane.
     *
     * @return the focal plane image
     */
    public ImageProcessor getMiddleImage() {
        if (middleImage == null) {

            if (getFocusPlane() == -1) {
                return null;
            }

            middleImage = getPlane(getFocusPlane());

            ImageStatistics stats = middleImage.getStatistics();
            minThreshold = DataTricks.round(stats.min) - 1;
            maxThreshold = DataTricks.round(stats.max) + 1;

        }
        return middleImage;
    }

    /**
     * Show.
     *
     * @param ip the ip
     * @return the image plus
     */
    public ImagePlus show(ImageProcessor ip) {
        ImagePlus imp = new ImagePlus("PatroloJ", ip);
        imp.show();
        return imp;
    }

    public void setLocator(BeadLocator locator) {
        this.locator = locator;
        beadLocation = null;
        beadFrames = null;
        preview = null;
    }

    public BeadLocator getLocator() {
        locator.setBeadImage(this);
        return locator;
    }

    /**
     * Gets the bead location. This threshold the focus plane image and detect
     * all objects using a Connected Component algorithm. It retuns a list of
     * Rectangle object representing each detected object (beads usually).
     *
     * @return the bead location
     */
    public synchronized ArrayList<Rectangle> getBeadLocation() {
        if (beadLocation == null) {
            beadLocation = getLocator().getBeadLocation();
            System.out.println("beadLocation : "+beadLocation);
        }
        return beadLocation;
    }

    /**
     * Auto bid enlargement.
     *
     * @return the int
     */
    public int autoFrameSize() {
        return autoBeadEnlargement(getMicroscope());
    }

    /**
     * Auto bead enlargement.
     *
     * @param m the m
     * @return the int
     */
    protected int autoBeadEnlargement(Microscope m) {

        int autoFrameSize;// = MathUtils.round(
        //	1.0 * beadEnlargementFactor * m.getXYTheoreticalResolution() / 2 / Math.sqrt(2 * Math.log(2)) / getMicroscope().getCalibration().pixelWidth
        //);

        autoFrameSize = MathUtils.round(m.getXYTheoreticalResolution() * beadEnlargementFactor / m.getCalibration().pixelWidth);

        setFrameSize(autoFrameSize);

        setChanged();
        notifyObservers(new Message(this, MSG_FRAMESIZE_CHANGED));
        System.out.println("auto calculated frame size : " + frameSize);
        return frameSize;
    }

    /**
     * Gets the enlarged frame.
     *
     * @param r the r
     * @return the enlarged frame
     */
    public Rectangle getEnlargedFrame(Rectangle r) {
        Rectangle rn = new Rectangle();

        int f = frameSize;

        if (frameSize * frameSize >= imageWidth * imageHeight * 0.8) {
            rn.setLocation(0, 0);
            rn.setSize(imageWidth, imageHeight);
            return rn;
        }

        int x = MathUtils.round(r.getX() + r.getWidth() / 2 - f / 2);

        int y = MathUtils.round(r.getY() + r.getHeight() / 2 - f / 2);

        x -= 1;
        y -= 1;

        int w = f;
        int h = f;
        rn.setLocation(x, y);
        rn.setSize(w, h);

        return rn;
    }

    /**
     * Sets the wave length.
     *
     * @param wavelength the new wave length
     */
    public void setWaveLength(double wavelength) {
        getMicroscope().setWaveLength(wavelength);
    }

    /**
     * Sets the wave length in nano meters.
     *
     * @param wavelength the new wave length in nano meters
     */
    public void setWaveLengthInNanoMeters(String wavelength) {
        try {
            getMicroscope().setWaveLength(Double.parseDouble(wavelength) / 1000);
        } catch (Exception e) {
            System.err.println("Couldn't set wavelength properly");
        }

    }

    /**
     * Gets the wave length in nano meters.
     *
     * @return the wave length in nano meters
     */
    public String getWaveLengthInNanoMeters() {
        if (getMicroscope() != null) {
            return "" + MathUtils.round(getMicroscope().getWaveLength() * 1000);
        } else {
            return "";
        }
    }

    /**
     * Gets the enlarged frame.
     *
     * @param r the r
     * @param f the f
     * @return the enlarged frame
     */
    public static Rectangle getEnlargedFrame(Rectangle r, int f) {
        Rectangle rn = new Rectangle();
        rn.setLocation(round(r.getCenterX() - f / 2), round(r.getCenterY() - f
                / 2));
        rn.setSize(f, f);

        return rn;
    }

    /**
     * Gets the BeadFrame list (areas extracted for the PSF analysis). This
     * function uses the result of the getBeadLocation, and create a frame
     * around each object. When two frames are overlapping too much, only the
     * frame containing the brightest bead is kept.
     *
     * @return the BeadFrame list object (areas extracted for the PSF analysis)
     */
    public synchronized BeadFrameList getBeadFrameList() {
        if (beadFrames == null) {
            beadFrames = getLocator().getBeadFrameList();
        }
        return beadFrames;
    }

    /**
     * Gets the frame number.
     *
     * @return the frame number
     */
    public int getFrameNumber() {

        return frameNumber;
    }

    /**
     * Sets the frame number.
     *
     * @param frameNumber the new frame number
     */
    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    /**
     * Gets the ignored frame number.
     *
     * @return the ignored frame number
     */
    public int getIgnoredFrameNumber() {
        return ignoredFrameNumber;
    }

    /**
     * Gets the bead max intensity.
     *
     * @param r the r
     * @return the bead max intensity
     */
    public double getBeadMaxIntensity(Rectangle r) {

        ImageProcessor middleImage = getMiddleImage();
        middleImage.setRoi((Rectangle) r.clone());
        double max = middleImage.getStatistics().max;
        middleImage.resetRoi();
        return max;
    }

    /**
     * Copy roi.
     *
     * @param r the r
     * @return the image processor
     */
    public ImageProcessor copyRoi(Rectangle r) {
        return copyRoi(getMiddleImage(), r);
    }

    /**
     * Copy roi into a new image.
     *
     * @param ip the source image
     * @param r the rectangle represanting the ROI
     * @return a ShortProcessor containing the ROI.
     */
    public ImageProcessor copyRoi(ImageProcessor ip, Rectangle r) {

		// System.out.println("Copying a roi of "+r.toString());
        ImageProcessor result = ip.createProcessor(r.width, r.height);

        for (int x = r.x; x != r.x + r.width; x++) {
            for (int y = r.y; y != r.y + r.height; y++) {
                result.setColor(ip.getPixel(x, y));
                result.drawPixel(x - r.x, y - r.y);
            }
        }

        return result;
    }

    /**
     * Gets the calibration.
     *
     * @return the calibration
     */
    public Calibration getCalibration() {

        return getMicroscope().getCalibration();
    }

    /**
     * Sets the calibration.
     *
     * @param cal the new calibration
     */
    public void setCalibration(Calibration cal) {
        getMicroscope().setCalibration(cal);
    }

    /**
     * Gets the segmentation mask from a specified image.
     *
     * @param ip the input image
     * @return the segmentation mask
     */
    private ImageProcessor getSegmentationMask(ImageProcessor ip) {
        return getSegmentationMask(ip, getThresholdValue());
    }

    /**
     * Gets the segmentation mask. This mask is used for bead detection.
     *
     * @param ip the ip
     * @param threshold the threshold
     * @return the segmentation mask
     */
    private ImageProcessor getSegmentationMask(ImageProcessor ip, int threshold) {
        System.out.println("Getting segmentation mask with threshold : "+threshold);
        ImageProcessor mask = ip.duplicate();

        mask.threshold(threshold);
        mask = mask.convertToByte(false);

        mask.invert();
        int[] h = mask.getHistogram();
        for (int i = 0; i != h.length; i++) {
            if (h[i] > 0) {
                System.out.println("i : "+i);
            }
        }
        System.out.println("mask : +" + mask);
        return mask;
    }

    /**
     * Gets the segmented image.
     *
     * @return the segmented image
     */
    public ImageProcessor getSegmentedImage() {
        if (segmentationMask == null) {
            System.out.println("Recalculating mask...");
            segmentationMask = getSegmentationMask(getMiddleImage());
        }
        return segmentationMask;
    }

    /**
     * Returns an image showing the focus plane image with rectangles
     * reprensenting the detected bead and their respective area.
     *
     * @return the bead preview
     */
    public synchronized ImageProcessor getBeadPreview() {

        return getBeadPreview(3);

    }

    /**
     * Gets the bead preview.
     *
     * @param trials the trials
     * @return the bead preview
     */
    public ImageProcessor getBeadPreview(int trials) {
        if (trials == 0) {
            return null;
        }
        if (preview == null) {
            try {
                System.out.println("generating new preview");

                preview = getMiddleImage();

                if (preview == null) {
                    return new ShortProcessor(previewWidth, previewHeight);
                }

                preview = preview.convertToRGB();

                for (BeadFrame frame : getBeadFrameList()) {
                    Rectangle2D r = frame.getBoundaries();

                    if (frame.isValid() == true) {
                        preview.setColor(Color.green);
                    } else {
                        preview.setColor(Color.yellow);
                    }

                    if (frame.isValid() == false) {
                        r = getEnlargedFrame((Rectangle) r, MathUtils.round(r.getWidth() / 2));
                    }

                    preview.drawRect(round(r.getX()), round(r.getY()),
                            round(r.getWidth()), round(r.getHeight()));
                    if (frame.getId() == null) {
                        continue;
                    }

                }

                status = "Done.";
                progress = 100;

                notifyObservers(MSG_PREVIEW_UPDATED, "Done.", 100, null);
            } catch (Exception e) {
                trials--;
                getBeadPreview(trials);
            }
        }
        return preview;

    }

    /**
     * Gets the bead preview in a asynchronous way.
     *
     * @return nothing : you should wait for the signal
     */
    public void getBeadPreviewAsync() {
        new Thread() {
            @Override
            public void run() {

                if (thresholdValue == -1) {
                    autoThreshold();
                }
                if (frameSize == -1) {
                    autoBeadEnlargement(new Microscope());
                }

                try {
                    System.out.println("Getting bead previous async");

                    getBeadPreview();
                    System.out.println("Finished...");

                } catch (ConcurrentModificationException e) {
                    System.out.println("oups...");
                }

            }
        }.start();
    }

    /**
     * Gets the auto threshold.
     *
     * @param sigma the sigma
     * @return the auto threshold
     */
    private double getAutoThreshold(double sigma) {
        // retrieve the focal plane
        ImageProcessor backgroundPlane = getPlane(0);

        // if their's a problem with the focal plane, we return -1;
        if (backgroundPlane == null) {
            return -1;
        }

        // gettting focal plane statistics
        setProgress(50, "Calculating threshold...");
        backgroundPlane.resetRoi();
        ImageStatistics stats = backgroundPlane.getStatistics();

        // retrieving the mean of the first image to serve as background mean
        double bgMean = stats.mean;
        double bgStd = stats.stdDev;

        return bgMean + (bgStd * sigma);
    }

    /**
     * Calculate automatically the threshold value for bead detection.
     *
     * @return the calculated threshold
     */
    public int autoThreshold() {

        double threshold = getAutoThreshold(10);

        System.out.println("Auto detected threshold : " + threshold);
        setThresholdValue(MathUtils.round(threshold));

        setProgress(100, "Done.");

        setChanged();
        notifyObservers(new Message(this, MSG_THRESHOLD_CHANGED));

        return getThresholdValue();
    }

    /**
     * Calculate the minimum and maximum values for the threshold setting.
     *
     * @return nothing : they can be accessed via the attributes minThreshold
     * and maxThreshold.
     */
    private void updateMinAndMax() {

        if (getFocusPlane() == -1) {

            minThreshold = 0;
            maxThreshold = 100;
        }

        if (minThreshold == -1 || maxThreshold == -1) {
            ImageStatistics stats = getMiddleImage().getStatistics();
            minThreshold = DataTricks.round(stats.min) - 1;
            maxThreshold = DataTricks.round(stats.max) + 1;
        }
        setChanged();
        notifyObservers(new Message(this, "min and max changed"));
    }

    /**
     * Gets the threshold minimum value (depends on the focal plane).
     *
     * @return the threshold min
     */
    public int getThresholdMin() {

        return minThreshold;
    }

    /**
     * Gets the threshold maximum value of the whole stack
     *
     * @return the threshold max
     */
    public int getThresholdMax() {

        return maxThreshold;
    }

    /**
     * Gets the threshold value used for bead detection.
     *
     * @return the threshold level
     */
    public int getThresholdValue() {

        return thresholdValue;
    }

    /**
     * Sets the threshold value for.
     *
     * @param newThreshold the new threshold value
     */
    public void setThresholdValue(int newThreshold) {

        System.out.println("** setting threshold to " + newThreshold);

        this.thresholdValue = newThreshold;

        // the segmentation mask is reset
        segmentationMask = null;

        // the bead location is reset
        if (beadLocation != null) {
            beadLocation.clear();
        }

        beadLocation = null;

        beadFrames = null;
        preview = null;
        stackList = null;
        System.gc();

    }

    /**
     * Gets the size of the bead area used for analysis.
     *
     * @return the bead area size
     */
    public int getFrameSize() {

        return frameSize;
    }

    /**
     * Gets the bead enlargement factor.
     *
     * @return the bead enlargement factor
     */
    public int getBeadEnlargementFactor() {
        return beadEnlargementFactor;
    }

    /**
     * Sets the bead enlargement factor.
     *
     * @param beadEnlargementFactor the new bead enlargement factor
     */
    protected void setBeadEnlargementFactor(int beadEnlargementFactor) {
        this.beadEnlargementFactor = beadEnlargementFactor;
    }

    /**
     * Sets the bid enlargement.
     *
     * @param beadEnlargement the new bid enlargement
     */
    public void setFrameSize(int beadEnlargement) {

        if (beadEnlargement == 0 || beadEnlargement == this.frameSize) {
            return;
        }

        System.out.println("** setting bead frame to " + beadEnlargement);

        this.frameSize = beadEnlargement;
        if (beadFrames != null) {
            beadFrames.clear();
        }
        beadFrames = null;
        preview = null;
        stackList = null;
        System.gc();

    }

    /**
     * Find exact focus point.
     */
    public void findExactFocusPoint() {

        ImageProcessor ip = getPlane(0);

        int best = 0;
        // a new standard deviation
        double newStdDev = ip.getStatistics().stdDev;

        // the best among the best
        double bestStdDev = newStdDev;

        // for each plane
        for (int i = 1; i != getStackSize(); i++) {
            setProgress(i, getStackSize());

            // get the plane
            ImageProcessor plane = getPlane(i);

			// tell the views that we are looking
            // at this particular plane
            updateView(plane);

            // the standard deviation of this plane is ...
            newStdDev = plane.getStatistics().stdDev;

            // if it's the best, then congratulation
            if (newStdDev > bestStdDev) {
                bestStdDev = newStdDev;

                // id of the best standard deviation
                best = i;
                /*
                 * System.out
                 * .println("A new best standard deviation\nhas been found : " +
                 * newStdDev);
                 */
                // telling the view what we are doing
                setProgress(100 * i / getStackSize(), "Searching focus...");
            }

        }

        // setting the focus plane to the best
        setFocusPlane(best);

        setProgress(0, "Done.");

    }

    /**
     * Sets the focus plane.
     *
     * @param focusPlane the new focus point
     */
    public void setFocusPlane(int focusPlane) {

        if (focusPlane >= 0 && focusPlane < getStackSize()
                && focusPlane != beadFocusPlane) {
            System.out.println("** setting focus to " + focusPlane);
            beadFocusPlane = focusPlane;
            middleImage = null;
            segmentationMask = null;
            // beadLocation = null;
            preview = null;
            if (getLocator().isFocusPlaneDependent()) {
                beadLocation = null;
                beadFrames = null;
            }
            updateMinAndMax();
        }
    }

    /**
     * Auto focus.
     */
    public void autoFocus() {

        if (isProcessing()) {
            return;
        }

        lockForProcessing();

        try {

            // if (isInMemory()) {
            findExactFocusPoint();
			// } else {
            // findBeadFocusPlaneWithHeuristic();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getThresholdMax();
        setProcessingFinished();
        notifyObservers("focus changed", "Done.", 100, null);
    }

    /**
     * Gets the focus plane.
     *
     * @return the focus plane (from 0 to stackSize -1)
     */
    public int getFocusPlane() {

        return beadFocusPlane;
    }

    /**
     * Find bead focus plane using an heuristic.
     */
    public void findBeadFocusPlaneWithHeuristic() {

        setStatus("Searching for focus point");

        int mean = Math.round(new Float(DataTricks.mean(getPlane(0))));
        mean = mean * 2;
        int plane = getStackSize() / 3;
        double delta_mean = 0;
        int direction = 1;
        int step = 5;
        int initStep = step;

        while (true) {

            ImageProcessor plane1 = getPlane(plane);
            ImageProcessor plane2 = getPlane(plane + step);

            double mean1 = DataTricks.mean(plane1, mean);
            double mean2 = DataTricks.mean(plane2, mean);

			// System.out.println("mean1 : " + mean1);
            // System.out.println("mean2 : " + mean2);
            delta_mean = (mean2 - mean1) / 5;

            if (direction != signOf(delta_mean)) {
                direction = direction * (-1);
                step = step - 1;

                setProgress(initStep - step, initStep);

            }
            updateView(plane1);
            plane = plane + step * direction;
            if (plane < 0) {

            }

            if (step == 0) {
                System.out.println("it breaks at " + plane + " !!!!");

                if (mean1 > mean2) {
                    setFocusPlane(plane);
                } else {
                    setFocusPlane(beadFocusPlane);
                }

                break;
            }
            setProgress(100 * (initStep - step) / initStep,
                    "Searching focus (heuristic) ...");
            plane1 = null;
            plane2 = null;

        }

        setStatus("Done.");
        setProgress(0);

        return;
    }

    /**
     * Gets the bead frame.
     *
     * @param i the i
     * @return the bead frame
     */
    public BeadFrame getBeadFrame(int i) {
        return getBeadFrameList().get(i);
    }

    /**
     * Rounds a double to an int.
     *
     * @param a the a
     * @return the int
     */
    public static int round(double a) {
        return Math.round(new Float(a));
    }

    /**
     * Gets the image height.
     *
     * @return the image height
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Sets the image height.
     *
     * @param imageHeight the new image height
     */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * Gets the image width.
     *
     * @return the image width
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Sets the image width.
     *
     * @param imageWidth the new image width
     */
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     * Creates the rectangle.
     *
     * @param w the width
     * @param h the height
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the rectangle
     */
    public Rectangle createRectangle(int w, int h, int x, int y) {
        Rectangle r = new Rectangle(new Point(x, y), new Dimension(w, h));
        return r;

    }

    /**
     * Update view (notify the observers.
     *
     * @param ip the image processing that the view is suggested to display
     * @param name the name of the message sent to the observers
     */
    public void updateView(ImageProcessor ip, String name) {
        rawPreview = ip;
        Message message = new Message(this, name, ip);
        setChanged();
        notifyObservers(message);
    }

    /**
     * Update the view (or suggest the observers to display a ImageProcessor
     * object).
     *
     * @param ip the ip
     */
    public void updateView(ImageProcessor ip) {
        updateView(ip.duplicate(), "beadimage changing raw preview");
    }

	// **********************************
    // Pattern Observer related functions
    // **********************************
    /**
     * Notify model change.
     */
    public void notifyModelChange() {
        setChanged();
        notifyObservers(this);
    }

    /**
     * Notify observers.
     *
     * @param name the name of the message
     * @param strData the string data inside the message (e.g : "Doing
     * something...")
     * @param intData the int data inside the message (e.g : 10 %)
     * @param data the data
     */
    public void notifyObservers(String name, String strData, Integer intData,
            Object data) {

        setChanged();
        notifyObservers(new Message(this, name, strData, intData, data));
    }

    /**
     * Gets the raw preview (doesn't calculate the preview like it usually do).
     *
     * @return the raw preview
     */
    public ImageProcessor getRawPreview() {
        return preview;
    }

    /**
     * Sets the progress of an ongoing process.
     *
     * @param progress the progress
     * @param status the status
     */
    public void setProgress(final int progress, final String status) {
        this.progress = progress;
        this.status = status;

        sendMessage();

    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(int progress) {
        if (progress == this.progress) {
            return;
        }
        this.progress = progress;
        sendMessage();
    }

    /**
     * Sets the progress.
     *
     * @param progress the progress
     * @param total the total
     */
    public void setProgress(int progress, int total) {
        setProgress(100 * progress / total);
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(String status) {

        this.status = status;
        if (this.status.equals(status)) {
            return;
        }
        sendMessage();

    }

    /**
     * Send message saying that the processing status changed.
     */
    public void sendMessage() {
        try {
            notifyObservers("processing status changed", status, progress, null);
        } catch (Exception e) {
            System.err.println("Error while notifying observers.");
            e.printStackTrace();
        }
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Checks if the image is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Gets the size of the image in the disk.
     *
     * @return the image disk size
     */
    public long getImageDiskSize() {

        if (imageDiskSize == -1) {
            if (getFileAddress() != null) {
                File file = new File(getFileAddress());
                imageDiskSize = file.length() / 1000 / 1000;
            }
        }
        return imageDiskSize;

    }

    /**
     * Sets the image disk size.
     *
     * @param imageDiskSize the new image disk size
     */
    public void setImageDiskSize(long imageDiskSize) {
        this.imageDiskSize = imageDiskSize;
    }

    /**
     * Ask for deletion.
     */
    public void askForDeletion() {
        isValid = false;
        setChanged();
        notifyObservers(new Message(this, MSG_IMAGE_NOT_OKAY));
    }

    /**
     * Auto focus async.
     */
    public void autoFocusAsync() {
        new Thread() {
            @Override
            public void run() {
                autoFocus();
            }
        }.start();

    }

    /**
     * Clean memory.
     */
    public void cleanMemory() {
        cleanMemory(CLEAN_ALL);
    }

    /**
     * Clean memory.
     *
     * @param level the level
     */
    public void cleanMemory(int level) {
        preview = null;
        rawPreview = null;
        middleImage = null;

        if (level == CLEAN_FRAMES) {
            if (beadFrames != null) {
                beadFrames = null;
            }
        }

        if (level == CLEAN_ALL || level == CLEAN_FOR_DELETION) {

            beadFrames = null;
            beadLocation = null;
            stackList = null;

            preview = null;
            segmentationMask = null;

        }

        if (level == CLEAN_FOR_DELETION) {
            stack = null;
            try {
                if (ipr != null) {
                    ipr.close();
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
            ipr = null;

            stackList = null;
            beadLocation = null;
            beadFrames = null;

        }

        System.gc();

    }

    /**
     * Update bead counts.
     */
    public void updateBeadCounts() {

        if (beadFrames != null) {
            frameNumber = beadFrames.size();
            for (BeadFrame frame : beadFrames) {
                if (frame.isIgnored()) {
                    //ignoredFrameCount++;
                } else if (frame.isValid()) {

                }
            }
        }

    }

    /**
     * Gets the kepts beads count.
     *
     * @return the kepts beads count
     */
    public int getValidBeadCount() {
        return getBeadFrameList().getValidBeadFrameCount();
    }

    /**
     * Gets the paired bead count.
     *
     * @return the paired bead count
     */
    public int getPairedBeadCount() {
        return getBeadFrameList().getValidBeadFrames().getWithAlterEgo().size();
    }

    /**
     * Gets the total bead count.
     *
     * @return the total bead count
     */
    public int getTotalBeadCount() {
        return getBeadFrameList().size();
    }

    /**
     * The is processing.
     */
    boolean isProcessing;

    /**
     * Checks if is processing.
     *
     * @return true, if is processing
     */
    public boolean isProcessing() {
        // System.out.println("isProcessing : " + isProcessing);
        return isProcessing;
    }

    /**
     * Lock for processing.
     */
    public synchronized void lockForProcessing() {
        // System.out.println("Locking for processing");
        isProcessing = true;
    }

    /**
     * Sets the processing finished.
     */
    public synchronized void setProcessingFinished() {
        // System.out.println("Process finished");
        isProcessing = false;
    }

    /**
     * Gets the microscope.
     *
     * @return the microscope
     */
    public Microscope getMicroscope() {

        if (microscope == null) {
            microscope = Microscope.loadMicroscopeFromImage(fileAddress);
        }

        return microscope;
    }

    /**
     * Sets the microscope.
     *
     * @param microscope the new microscope
     */
    public void setMicroscope(Microscope microscope) {
        this.microscope = microscope;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result = "******\n[Bead Image] " + stack;
        result += "\nfile : " + getImageName();
        result += "\nBead found : " + getBeadLocation().size();
        result += "\nFrame number : " + getBeadFrameList().size();
        result += "\nThreshold : " + getThresholdValue();
        result += "\nFrame size : " + getFrameSize();
        result += "\nFocus : " + getFocusPlane();
        result += "\n************";
        return result;
    }

    /**
     * Gets the counter.
     *
     * @return the counter
     */
    public Counter3D getCounter() {
        return counter3d;
    }

    /**
     * Reset preview.
     */
    public void resetPreview() {
        middleImage = null;
        preview = null;
        System.out.println("reseting bead preview");
        System.gc();
    }

    /**
     * Gets the map.
     *
     * @return the map
     */
    public ImageProcessor getMap() {

        ImageProcessor ip = getMiddleImage().convertToRGB();

        for (BeadFrame frame : getBeadFrameList()) {

            frame.draw(ip, 10);

        }

        return ip;

    }

    /**
     * Gets the beads color.
     *
     * @return the beads color
     */
    public Color getBeadsColor() {
        if (getMicroscope() == null) {
            return Color.black;
        }
        return VisibleColor
                .wavelengthToColor(getMicroscope().getWaveLength() * 1000);
    }

    /**
     * Gets the icon.
     *
     * @param size the size
     * @return the icon
     */
    public ImageIcon getIcon(int size) {
        ImageProcessor i = new ColorProcessor(size, size);
        i.setColor(getBeadsColor());
        i.fill();
        return new ImageIcon(i.getBufferedImage());

    }

    /**
     * Gets the zone.
     *
     * @param x the x
     * @param y the y
     * @param factor the factor
     * @return the zone
     */
    public Rectangle getZone(int x, int y, int factor) {

        int beadNumber = getBeadFrameList().getWithAlterEgo().size();
        int w = MathUtils.round(Math.sqrt(1.0 * getImageWidth()
                * getImageHeight() / beadNumber * 8));

        // int w = getImageWidth() / factor;
        int h = w; // getImageHeight() / factor;

        int x0 = x;
        int y0 = y;

        x = x0 * getImageWidth() / 2;
        y = y0 * getImageHeight() / 2;

        x = x - w / 2 * (x0 + 1);
        y = y - h / 2 * (y0 + 1);

        x = x + getImageWidth() / 2;
        y = y + getImageHeight() / 2;

        System.out.println(new Rectangle(x, y, w, h).toString()
                + String.format(" for %d,%d", x0, y0));

        return new Rectangle(x, y, w, h);

    }

    /**
     * Gets the corner name.
     *
     * @param x the x
     * @param y the y
     * @return the corner name
     */
    private String getCornerName(int x, int y) {

        String[] xString = {"LEFT", "CENTER", "RIGHT"};
        String[] yString = {"TOP", "CENTER", "BOTTOM"};

        return yString[y + 1] + " - " + xString[x + 1];

    }

    /**
     * Gets the montage.
     *
     * @return the montage
     */
    public ImageProcessor getMontage() {

        int beadPerCorner = 4;

        int beadNumber = getBeadFrameList().getWithAlterEgo().size();

        if (beadNumber < 4 * 9 * 2) {
            beadPerCorner = 1;
        }

        if (beadNumber < 18) {
            return getBeadFrameList().getValidBeadFrames().getBeadMontage();
        }

        int enlargementFactor = 3;

        int cornerWidth = MathUtils.round(Math.sqrt(beadPerCorner))
                * getBeadFrame(0).getWidth() * enlargementFactor;
        int cornerHeight = MathUtils.round(Math.sqrt(beadPerCorner))
                * getBeadFrame(0).getHeight() * enlargementFactor;
        int mw = cornerWidth * 3;
        int mh = cornerHeight * 3;

        ColorProcessor finalMontage = new ColorProcessor(mw, mh);

        for (int x = -1; x != 2; x++) {
            for (int y = -1; y != 2; y++) {

                int xf = (x + 1) * cornerWidth;
                int yf = (y + 1) * cornerHeight;
                ImageProcessor beadMontage = getBeadFrameList().getWithAlterEgo()
                        .getSampleFromCorner(getZone(x, y, 4), beadPerCorner)
                        .getBeadMontage();
                beadMontage = beadMontage.resize(beadMontage.getWidth() * enlargementFactor, beadMontage.getHeight() * enlargementFactor, true);
                finalMontage.copyBits(beadMontage, xf, yf, Blitter.COPY);

                finalMontage.setColor(Color.white.darker().darker());
                finalMontage.setFont(new Font(java.awt.Font.SANS_SERIF, java.awt.Font.CENTER_BASELINE, 10));
                finalMontage.drawString(getCornerName(x, y), xf + 5, yf + 16);

                finalMontage.setColor(Color.yellow.darker());
                finalMontage.drawRect((x + 1) * cornerWidth, (y + 1) * cornerHeight,
                        cornerWidth, cornerHeight);

            }
        }

        return finalMontage;

    }

    /**
     * Gets the distance threshold.
     *
     * @return the distance threshold
     */
    public double getDistanceThreshold() {
        return getMicroscope().getXYTheoreticalResolution() * 2;
    }

    /**
     * The bg mean.
     */
    double bgMean = -1;

    /**
     * The bg std dev.
     */
    double bgStdDev = -1;

    /**
     * The bead max intensity.
     */
    double beadMaxIntensity = -1;

    /**
     * The bead max standard deviation.
     */
    double beadMaxStandardDeviation = -1;

    /**
     * The bead mean b paramter.
     */
    double beadMeanBParamter = -1;

    /**
     * The bead mean b parameter std dev.
     */
    double beadMeanBParameterStdDev = -1;

    /**
     * The offset.
     */
    double offset = 0;

    /**
     * The invalidity reason.
     */
    private String invalidityReason;

    /**
     * Gets the signal mean.
     *
     * @return the signal mean
     */
    public double getSignalMean() {
        return beadMeanBParamter;
    }

    /**
     * Gets the signal standard deviation.
     *
     * @return the signal standard deviation
     */
    public double getSignalStandardDeviation() {
        calculateSignalAndNoise();
        return beadMeanBParameterStdDev;
    }

    /**
     * Gets the signal to noise ratio.
     *
     * @return the signal to noise ratio
     */
    public double getSignalToNoiseRatio() {
        calculateSignalAndNoise();

        return (getSignalMean() - getBackgroundMean()) / getBackgroundStandardDeviation();

    }

    /**
     * Gets the background mean.
     *
     * @return the background mean
     */
    public double getBackgroundMean() {
        calculateSignalAndNoise();
        return bgMean;
    }

    /**
     * Gets the background standard deviation.
     *
     * @return the background standard deviation
     */
    public double getBackgroundStandardDeviation() {
        calculateSignalAndNoise();
        return bgStdDev;
    }

    /**
     * Calculate signal and noise.
     */
    public void calculateSignalAndNoise() {
        if (bgMean == -1) {

			// ImageProcessor beadMask = getSegmentedImage().duplicate();
			// bgMask.invert();
            // middleImage.setMask(beadMask);
            DescriptiveStatistics stats = new DescriptiveStatistics();
            DescriptiveStatistics aStats = new DescriptiveStatistics();
            DescriptiveStatistics bStats = new DescriptiveStatistics();
            for (BeadFrame bead : getBeadFrameList().getOnlyValidBeads()) {
                stats.addValue(bead.getMaximumIntensity());
                aStats.addValue(bead.getFittingParameter(0, 0) - offset);
                bStats.addValue(bead.getFittingParameter(0, 1) - offset);

            }
            beadMaxIntensity = stats.getMean();
            beadMaxStandardDeviation = stats.getStandardDeviation();
            bgMean = aStats.getMean();
            bgStdDev = aStats.getStandardDeviation();
            beadMeanBParamter = bStats.getMean();
            beadMeanBParameterStdDev = bStats.getStandardDeviation();

            /*
             * beadMask.invert(); beadMask.dilate(); beadMask.dilate();
             * beadMask.dilate(); beadMask.dilate();
			 
             for (double i = 0; i != 5; i += 0.5) {
             ImageProcessor middleImage = getMiddleImage().duplicate();
             double threshold = getAutoThreshold(i);
             double realMean = middleImage.getStatistics().mean;
             double realStdDev = middleImage.getStatistics().stdDev;
				
             ImageProcessor bgMask = getSegmentationMask(middleImage,
             MathUtils.round(threshold));
             middleImage.setMask(bgMask);
				
				
             dataset.addValue("i", i);
             dataset.addValue("threshold",threshold);
             dataset.addValue("bgMean", value);
             dataset.addValue("bgStandard",gbS)
				
             bgMean = middleImage.getStatistics().mean;
             bgStdDev = middleImage.getStatistics().stdDev;
             System.out
             .println(String
             .format(
             "Signal to noise : \nMean : %.0f,\nBg. Mean : %.0f,\nDev : %.0f",
             beadMeanIntensity, bgMean, bgStdDev));
             new ImagePlus("bgMask", bgMask).show();
             }
             /*
             * middleImage.setMask(beadMask); bgMean =
             * middleImage.getStatistics().mean; bgStdDev =
             * middleImage.getStatistics().stdDev;
			 
             */
            System.out.println(String.format(
                    "** Old Method **\nMean : %.0f,\nBg. Mean : %.0f,\nDev : %.0f\nBMean : %.0f\n", beadMaxIntensity, bgMean, bgStdDev, beadMeanBParamter));
        }
    }

    /**
     * Gets the deleted beads count.
     *
     * @return the deleted beads count
     */
    public int getDeletedBeadsCount() {
        return getTotalBeadCount() - getValidBeadCount();
    }

    /**
     * Gets the invalidy reason.
     *
     * @return the invalidy reason
     */
    public String getInvalidyReason() {
        if (this.invalidityReason != null) {
            return this.invalidityReason;
        }
        return null;
    }

    /**
     * Sets the invalidity reason.
     *
     * @param reason the new invalidity reason
     */
    public void setInvalidityReason(String reason) {
        if (invalidityReason == null) {
            invalidityReason = reason;
        }
    }

    public int getMaxaximumIntensityOfTheWholeStack() {
		// TODO Auto-generated method stub

        if (maxIntensityOfWholeStack == NOT_SET) {

            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = 0; i != stack.getSize(); i++) {

                stats.addValue(stack.getProcessor(i + 1).getStatistics().max);

            }
            maxIntensityOfWholeStack = stats.getMax();

        }

        return MathUtils.round(maxIntensityOfWholeStack);
    }

    public void setIgnoredFrameNumber(int size) {
        ignoredFrameNumber = size;
    }

}
