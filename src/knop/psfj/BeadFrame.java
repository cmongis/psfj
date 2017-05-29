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
import ij.gui.Line;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.plugin.Slicer;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Observable;

import knop.psfj.resolution.Counter3D;
import knop.psfj.resolution.FindMax;
import knop.psfj.resolution.SideViewGenerator;
import knop.psfj.utils.ImageProcessorUtils;
import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import Objects3D.Object3D;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadFrame.
 * 
 * A Bead Frame is a class representing a frame around a bead.
 * 
 */
public class BeadFrame extends Observable {

	/** The bead id. */
	Integer id;

	/** The boundaries. */
	Rectangle2D boundaries;

	/**  The ImagePlus stack containing the bead. */
	ImagePlus ip;

	/**  The BeadImage object from where the BeadFrame has been extracted. */
	BeadImage source;

	/** The stack size. */
	int stackSize;

	/** The calibration object. */
	Calibration calibration;

	/**
	 * Boolean indicating is the BeadFrame is valid. The BeadFrame is usually
	 * marked as invalid when filtered out because of bad fitting
	 */
	boolean isValid = true;

	/** The invalidity reason. */
	String invalidityReason = null;

	/**  The center of the bead in integer. */
	protected int[] center = null;

	/**  The centroid (center of the bead computed by the Counter3D algorithm). */
	protected float[] centroid = null;

	/** Equals sqrt(2*ln(2)), used to calculate FWHM of Gaussians *. */
	public final static double SQRT2LN2 = Math.sqrt(2 * Math.log(2));

	/** Reference to the x dimension *. */
	public static final int X = 0;

	/** Reference to the y dimension *. */
	public static final int Y = 1;

	/** Reference to the z dimension *. */
	public static final int Z = 2;

        public static final int A = 1;
        
        public static final int B = 0;
        
	/**
	 * Stores the x profile, [0=x as a physical distance ,1=raw intensity,
	 * 2=fitted data][pixel nb, from 0 to width-1].
	 */
	double[][] xProfile = null;

	/** Stores the fitting parameters for the x profile *. */
	double[] xParams = null;

	/** Stores the fitting goodness for the x profile *. */
	double xR2 = Double.NaN;

	/** Stores the fitting parameters for the x profile as a string *. */
	String xParamString = "Fitted on y = a + (b-a)*exp(-(x-c)^2/(2*d^2)";

	/**
	 * Stores the y profile, [0=y as a physical distance ,1=raw intensity,
	 * 2=fitted data][pixel nb, from 0 to height-1].
	 */
	double[][] yProfile = null;

	/** Stores the fitting parameters for the y profile *. */
	double[] yParams = null;

	/** Stores the fitting goodness for the y profile *. */
	double yR2 = Double.NaN;

	/** Stores the fitting parameters for the y profile as a string *. */
	String yParamString = "Fitted on y = a + b*exp(-(x-c)^2/(2*d^2)";

	/**
	 * Stores the z profile, [0=z as a physical distance ,1=raw intensity,
	 * 2=fitted data][pixel nb, from 0 to nSlices-1].
	 */
	double[][] zProfile = null;

	/** Stores the fitting parameters for the z profile *. */
	double[] zParams = null;

	/** Stores the fitting goodness for the z profile *. */
	double zR2 = Double.NaN;

	/** The R2. */
	double[] R2 = new double[3];

	/**  Shift in the X axis with the corresponding bead (alter ego). */
	double deltaX = Double.NaN;

	/**  Shift in the Y axis with the corresponding bead (alter ego). */
	double deltaY = Double.NaN;

	/**  Shift in the Z axis with the corresponding bead. */
	double deltaZ = Double.NaN;

	/**  The side view image (computed after PSF Fitting). */
	protected ImagePlus sideViewImage;

	/** The counter. */
	Counter3D counter;

	/** The max intensity. */
	double maxIntensity;

	/** The is ignored. */
	boolean isIgnored = false;

	/** The object3d. */
	protected Object3D object3d;

	/**  The alter ego (corresponding bead in an other channel). */
	BeadFrame alterEgo;

	public static final String MSG_PSF_FOUND = "psf found";
	
	
	/**
	 * Stores the calculated resolutions (FWHM) in all the dimensions of the
	 * image.
	 */
	double[] resol = {0, 0, 0};

	/** The asymetry. */
	double asymetry = Double.NaN;

	/** Stores the fitting parameters for the z profile as a string *. */
	String zParamString = "Fitted on y = a + (b-a)*exp(-(x-c)^2/(2*d^2)";

	/**
	 * The main method.
	 * 
	 * @param args
	 *           the arguments
	 */
	public static void main(String[] args) {

		BeadImageManager manager = new BeadImageManager();

		// manager.add("/home/cyril/test_img/colocalisation/gfp_01_220beads_small.tif");
		// manager.add("/home/cyril/test_img/colocalisation/mc_01_220beads_small.tif");

		manager.add("/home/cyril/test_img/colocalisation/gfp1.tif");
		manager.add("/home/cyril/test_img/colocalisation/mc1.tif");

		manager.setAnalysisType(BeadImageManager.DUAL_CHANNEL);

		manager.autoFrameSize();
		manager.autoFocus(0);
		manager.autoFocus(1);

		manager.processProfiles();

		new ImagePlus("", manager.getBeadImage(0).getMontage().resize(600))
				.show();

	}

	/**
	 * Instantiates a new bead frame.
	 * 
	 * @param id
	 *           the id
	 * @param r
	 *           the r
	 */
	public BeadFrame(int id, Rectangle r) {
		this(r);
		this.id = id;
	}

	/**
	 * Instantiates a new bead frame.
	 * 
	 * @param rectangle
	 *           the rectangle
	 */
	public BeadFrame(Rectangle rectangle) {
		this.boundaries = rectangle;
	}

	/**
	 * Gets the X position of the BeadFrame (not the bead itself).
	 * 
	 * @return the x
	 */
	public int getFrameX() {
		return MathUtils.round(boundaries.getX());
	}

	/**
	 * Gets the Y position of the BeadFrame (not the bead itself) .
	 * 
	 * @return the y
	 */
	public int getFrameY() {
		return MathUtils.round(boundaries.getY());
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *           the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the boundaries of the BeadFrame in a Rectangle2D form.
	 *
	 * @return the boundaries
	 */
	public Rectangle2D getBoundaries() {
		return boundaries;
	}

	/**
	 * Gets the boundaries as Rectangle.
	 * 
	 * @return the boundaries as rectangle
	 */
	public Rectangle getBoundariesAsRectangle() {
		return (Rectangle) boundaries;
	}

	/**
	 * Sets the boundaries.
	 * 
	 * @param boundaries
	 *           the new boundaries
	 */
	public void setBoundaries(Rectangle boundaries) {
		this.boundaries = boundaries;
	}

	/**
	 * Gets the substack.
	 * 
	 * @return the substack
	 */
	public ImagePlus getSubstack() {

		if (ip == null) {

			int width = MathUtils.round(boundaries.getWidth());
			int height = MathUtils.round(boundaries.getHeight());
			int x = getFrameX();
			int y = getFrameY();

			// boundaries.setRect(x, y, width, height);

			ImageStack stack = new ImageStack(width, height);

			for (int i = 0; i != source.getStackSize(); i++) {

				ImageProcessor sourcePlane = source.getPlane(i);
				ImageProcessor targetPlane = ImageProcessorUtils.copyRoi(
						sourcePlane, boundaries.getBounds());

				stack.addSlice(targetPlane);
			}
			setSubstack(new ImagePlus("", stack));
			// if(getId() == 200) ip.show();;
		}

		return ip;
	}

	/**
	 * Sets the substack.
	 * 
	 * @param substack
	 *           the new substack
	 */
	public void setSubstack(ImagePlus substack) {
		this.ip = substack;
		stackSize = ip.getStackSize();
		ip.setCalibration(getCalibration());
	}

	/**
	 * Gets the distance.
	 *
	 * @param p1 the p1
	 * @param p2 the p2
	 * @return the distance
	 */
	private double getDistance(float[] p1, float[] p2) {
		double x1 = p1[0]; // getCentroidXInImage();
		double x2 = p2[0]; // bead.getCentroidXInImage();
		double y1 = p1[1]; // getCentroidYInImage();
		double y2 = p2[1]; // bead.getCentroidYInImage();

		if (p1.length > 2 && p2.length > 2) {
			double z1 = p1[2];
			double z2 = p2[2];
			return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)
					+ Math.pow(z1 - z2, 2));
		}

		else {

			return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		}
	}

	/**
	 * Gets the distance with an other bead.
	 *
	 * @param bead           the bead
	 * @return the distance
	 */
	public double getDistance(BeadFrame bead) {

		if (bead == null)
			return Double.NaN;

		double x1 = getFovX(); // getCentroidXInImage();
		double x2 = bead.getFovX();// bead.getCentroidXInImage();
		double y1 = getFovY();// getCentroidYInImage();
		double y2 = bead.getFovY(); // bead.getCentroidYInImage();

		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

	}

	/**
	 * Gets the distance with alter ego.
	 *
	 * @return the distance with alter ego
	 */
	public double getDistanceWithAlterEgo() {
		if (alterEgo != null) {
			return getDistance(alterEgo);
		}

		else {
			return Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Gets the closest bead from a BeadImageList object.
	 * 
	 * @param list
	 *           the list
	 * @return the closest bead
	 */
	public BeadFrame getClosestBead(BeadFrameList list) {
		double minDistance = -1;
		double distance;

		BeadFrame found = null;

		for (BeadFrame bead : list) {
			distance = getDistance(bead);

			if (minDistance == -1 || distance < minDistance) {
				minDistance = distance;
				found = bead;
			}
		}

		if (found == null)
			return null;

		System.out.printf("For bead (%d) : %.3f , %.3f\n", getId(), getFovX(),
				getFovY());
		System.out.printf(
				"The closest bead (%d) has for coordinates : %.3f , %.3f\n",
				found.getId(), found.getFovX(), found.getFovY());

		return found;

	}

	/**
	 * Sets the source.
	 * 
	 * @param beadImage
	 *           the new source
	 */
	public void setSource(BeadImage beadImage) {

		source = beadImage;
		stackSize = source.getStackSize();
		calibration = new Calibration();
		calibration.pixelWidth = source.getCalibration().pixelWidth;
		calibration.pixelHeight = source.getCalibration().pixelHeight;
		calibration.pixelDepth = source.getCalibration().pixelDepth;

	}

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	public BeadImage getSource() {
		return source;
	}

	/**
	 * Gets the calibration.
	 * 
	 * @return the calibration
	 */
	public Calibration getCalibration() {
		// return calibration;
		return getSource().getCalibration();
	}

	/**
	 * Draw.
	 * 
	 * @param ip
	 *           the ip
	 * @param radius
	 *           the radius
	 */
	public void draw(ImageProcessor ip, int radius) {

		Color c = (isValid() ? Color.white : Color.red);
		draw(ip, radius, c);

	}

	/**
	 * Gets the theta.
	 *
	 * @return the theta
	 */
	public double getTheta() {
		return 0.0;
	}

	/**
	 * Gets the theta in degrees.
	 *
	 * @return the theta in degrees
	 */
	public double getThetaInDegrees() {
		return getTheta() * 180 / Math.PI;
	}

	/**
	 * Draw.
	 *
	 * @param ip the ip
	 * @param radius the radius
	 * @param c the c
	 */
	public void draw(ImageProcessor ip, int radius, Color c) {
		int x = getWeightedXInImage(); // (getCenterX());
		int y = getWeightedYInImage(); // (getCenterY());

		ip.setColor(c.darker());

		// ip.drawOval(x-(getWidth()/2), y-(getWidth()/2),
		// getWidth(),getWidth());
		ip.drawRect(getFrameX(), getFrameY(), getWidth(), getHeight());
		ip.drawRect(getFrameX() - 1, getFrameY() - 1, getWidth() + 2,
				getHeight() + 2);
		// ip.setColor(Color.red);
		// ip.drawOval(x-1, y-1, 3, 3);
		ip.setColor(Color.white);
		ip.drawString("" + id, x, y + 16);

	}

	/**
	 * Gets the fov x.
	 * 
	 * @return the fov x
	 */
	public double getFovX() {
		return // (getX()+getWeightedXInImage() -
					// getSource().getImageWidth()/2)*getCalibration().pixelWidth;
		(getCentroidXInImage() * getCalibration().pixelWidth)
				- (getSource().getFieldOfViewWidth() / 2);
	}

	/**
	 * Gets the fov y.
	 * 
	 * @return the fov y
	 */
	public double getFovY() {
		return // (getY()+getWeightedYInImage() -
					// getSource().getImageHeight()/2)*getCalibration().pixelHeight;
		-1.0 * ((getCentroidYInImage() * getCalibration().pixelHeight)
				- (getSource().getFieldOfViewHeight() / 2));// -
	}

	/**
	 * Gets the center x.
	 * 
	 * @return the center x
	 */
	public int getFrameCenterX() {
		return MathUtils.round(boundaries.getCenterX());
	}

	/**
	 * Gets the center y.
	 * 
	 * @return the center y
	 */
	public int getFrameCenterY() {
		return MathUtils.round(boundaries.getCenterY());
	}

	/**
	 * Gets the weighted x in image.
	 * 
	 * @return the weighted x in image
	 */
	public int getWeightedXInImage() {

		if (centroid == null) {
			return getFrameCenterX();
		}

		return getFrameX() + getCentroidXAsInt();
	}

	/**
	 * Gets the weighted y in image.
	 * 
	 * @return the weighted y in image
	 */
	public int getWeightedYInImage() {
		if (centroid == null) {
			return getFrameCenterY();
		}
		return getFrameY() + getCentroidYAsInt();
	}

	/**
	 * Gets the centroid.
	 *
	 * @return the centroid
	 */
	private float[] getCentroid() {
		if (centroid == null) {
			try {
				findCenter();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Couldn't find the center for the bead "
						+ getId());
				centroid = new float[]{-1, -1, -1};
			}

		}
		return centroid;

	}

	/**
	 * Gets the centroid x.
	 * 
	 * @return the centroid x
	 */
	public double getCentroidX() {
		// findCenter();
		return getCentroid()[0];
	}

	/**
	 * Gets the centroid y.
	 * 
	 * @return the centroid y
	 */
	public double getCentroidY() {
		// findCenter();
		return getCentroid()[1];
	}

	/**
	 * Gets the centroid z.
	 * 
	 * @return the centroid z
	 */
	public double getCentroidZ() {
		// findCenter();
		return getCentroid()[2];
	}

	/**
	 * Gets the centroid x in image.
	 * 
	 * @return the centroid x in image
	 */
	public double getCentroidXInImage() {
		return getFrameX() + getCentroidX();
	}

	/**
	 * Gets the centroid y in image.
	 * 
	 * @return the centroid y in image
	 */
	public double getCentroidYInImage() {
		return getFrameY() + getCentroidY();
	}

	/**
	 * Gets the centroid x as int.
	 *
	 * @return the centroid x as int
	 */
	public int getCentroidXAsInt() {
		return center[0];
	}

	/**
	 * Gets the centroid y as int.
	 *
	 * @return the centroid y as int
	 */
	public int getCentroidYAsInt() {
		return center[1];
	}

	/**
	 * Gets the centroid z as int.
	 *
	 * @return the centroid z as int
	 */
	public int getCentroidZAsInt() {
		return center[2];
	}

	/**
	 * ******************************************************************.
	 * 
	 * @param coords
	 *           the coords
	 * @return the intensity
	 */
	/*********************** FITTING AREA ********************************/
	/*********************************************************************/

	public int getIntensity(float[] coords) {
		return getIntensity(MathUtils.round(coords[0]),
				MathUtils.round(coords[1]), MathUtils.round(coords[2]));
	}

	/**
	 * Center boundaries around centroid.
	 */
	public void centerBoundariesAroundCentroid() {

		int oldCenterX = getFrameCenterX();
		int oldCenterY = getFrameCenterY();
		int newCenterX = getWeightedXInImage();
		int newCenterY = getWeightedYInImage();

		if (oldCenterX == newCenterX && oldCenterY == newCenterY)
			return;

		float xShift = newCenterX - oldCenterX;
		float yShift = newCenterY - oldCenterY;

		centroid[0] = centroid[0] + xShift;
		centroid[1] = centroid[1] + yShift;

		center[0] = MathUtils.round(centroid[0]);
		center[1] = MathUtils.round(centroid[1]);

		int width = MathUtils.round(boundaries.getWidth());
		int height = MathUtils.round(boundaries.getHeight());

		boundaries = new Rectangle(newCenterX - (width / 2), newCenterY
				- (height / 2), width, height);
	}

	/**
	 * Gets the intensity.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 * @param z
	 *           the z
	 * @return the intensity
	 */
	public int getIntensity(int x, int y, int z) {
		getSubstack().setSlice(z);
		return getSubstack().getProcessor().getPixel(x, y);
	}

	/**
	 * Convert int array to float.
	 * 
	 * @param intArray
	 *           the int array
	 * @return the float[]
	 */
	private float[] convertIntArrayToFloat(int[] intArray) {
		float[] floatArray = new float[intArray.length];
		for (int i = 0; i != floatArray.length; i++) {
			floatArray[i] = intArray[i];
		}
		return floatArray;
	}

	/**
	 * Find center.
	 */
	public void findCenter() {

		// if the centroid has already been calculated, the routine is aborted
		if (centroid != null)
			return;

		// a counter 3D object is created.
		counter = new Counter3D(getSubstack(), getSource().getThresholdValue(),
				1, 10000);
		counter.setSizeFiltering(true);

		float[][] centers = new float[0][0];

		try {
			counter.getObjects();
			// the centers returned by the 3D segmentation are harvested
			centers = counter.getCentreOfMassList();
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}

		// If no center is found, then the MetroloJ algorithm is used
		if (centers.length == 0) {
			System.out
					.println("Couldn't find the center using 3D Object Counter for bead "
							+ getId());

			findCenterUsingBrightestPixel();
		}

		// if one or several centers are found
		else {
			// the closest centroid to the center is filtered out
			centroid = filterCentroids(centers);

			// in case the image wouldn't be a stack, the returned centroid would
			// be
			// a 2 value arrays so we transformed it into a 3 values array
			if (centroid.length == 2)
				centroid = new float[]{centroid[0], centroid[1], 0};

			// converting the centroid to int array for (1D fitting)
			center = new int[centroid.length];

			for (int i = 0; i != centroid.length; i++) {
				// System.out.printf("Rounding centroid from %.3f to %d\n",centroid[i],MathUtils.round(centroid[i]));
				center[i] = MathUtils.round(centroid[i]);

			}
		}

		// System.out.println(String.format("Difference from the center : %d x %d pixels",getBoundariesAsRectangle().width/2-center[0],getBoundariesAsRectangle().height/2-center[1]));

		// the boundaries of the substack are recalculated but not updated (the
		// substack will still be the same)
		// centerBoundariesAroundCentroid();

		// getCenterAsIntArray();

		if (ip.getStack().getSize() == 1) {
			center[2] = 0;
			centroid[2] = 0;
		}

		counter = null;

		// centerBoundariesAroundCentroid();

	}

	/**
	 * Find center using brightest pixel.
	 */
	public void findCenterUsingBrightestPixel() {
		// finds the brightest pixels of all stacks
		center = new FindMax().getAllCoordinates(getSubstack());

		// in a case of a flat image, the findMax object returns a 2-values array
		// that must be converted to a 3-values array
		if (center.length == 2)
			center = new int[]{center[0], center[1], 0};

		// since we cannot have a centroid in a subpixel resolution, we convert
		// the int array into a float array
		centroid = convertIntArrayToFloat(center);

		maxIntensity = getSubstack().getStack().getProcessor(center[2] + 1)
				.getPixel(center[0], center[1]);
	}

	/**
	 * Filter centroids.
	 * 
	 * @param centroids
	 *           list of centroids found by the 3D Counter Object
	 * @return the float[] a 3 items array reprensenting x,y, and z coordinates of the center
	 */
	private float[] filterCentroids(float[][] centroids) {

		
		int indexMax = 0;
		int intensityMax = 0;
		double closest = Double.POSITIVE_INFINITY;
		int indexClosest = 0;

		
		float[] center = new float[3];
		
		// getting the boundaries of the center
		if (boundaries == null) {
			boundaries = new Rectangle(0, 0, getSubstack().getWidth(),
					getSubstack().getHeight());
		}
		center[0] = getWidth() / 2;
		center[1] = getHeight() / 2;
		center[2] = source.getFocusPlane()
				* (float) ip.getCalibration().pixelDepth;

		if (centroids.length == 1) {
			indexMax = 0;
		} else {
			float[] point;
			int intensity;
			double distanceToCenter;
			for (int i = 0; i != centroids.length; i++) {
				point = centroids[i];
				intensity = getIntensity(point);
				distanceToCenter = getDistance(center, point);
				if (intensity > intensityMax) {
					indexMax = i;
					intensityMax = intensity;
				}

				if (distanceToCenter < closest) {
					closest = distanceToCenter;
					indexClosest = i;
				}

			}

			object3d = counter.getObject(indexMax);
			maxIntensity = object3d.max;

		}

		return centroids[indexClosest];

	}

	/**
	 * Gets the maximum intensity.
	 *
	 * @return the maximum intensity
	 */
	public double getMaximumIntensity() {
		return maxIntensity;
	}

	/**
	 * Find psf.
	 */
	public void findPSF() {
		if (isValid == false) {
			resol = new double[]{0.0, 0.0, 0.0};
			return;
		}
		try {

			findCenter();
			getSubstack().setSlice(getCentroidZAsInt());

			getXprofileAndFit();
			getYprofileAndFit();
			getZprofileAndFit();

			// System.out.println(getId() + " is finished.");
			getSideViewImage();
			ip = null;

			// System.gc();

			setChanged();
			notifyObservers(new Message(this, MSG_PSF_FOUND));

		} catch (OutOfMemoryError e) {
			System.out.println(new Message(this, "error",
					"There is not enough memory to process the beads !"));
		}

	}

	/**
	 * Gets the minimum bead size.
	 * 
	 * @return the minimum bead size
	 */
	public int getMinimumBeadSize() {

		double volume = getSource().getMicroscope().getXYTheoreticalResolution() / 2;

		volume = 4 / 3 * Math.PI * Math.pow(volume, 3);

		return MathUtils.round(volume
				/ Math.pow(getSource().getCalibration().pixelWidth, 3));
	}

	/**
	 * Gets the find psf job.
	 * 
	 * @return the find psf job
	 */
	public Runnable getFindPSFJob() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					findPSF();
				} catch (Exception e) {
					System.err.println("Error when analysing bead " + getId());
					e.printStackTrace();
				}
			}

		};
	}

	/**
	 * Retrieves data and fills xProfile the x profile through the centre of the
	 * bead retrieved from the current ImageProcessor.
	 * 
	 * @return a double[][] [0=x as a physical distance ,1=raw intensity,
	 *         2=fitted data][pixel nb, from 0 to width-1]
	 */
	protected void getXprofileAndFit() {
		xProfile = new double[3][ip.getWidth()];
		xProfile[1] = ip.getProcessor().getLine(0, center[1], ip.getWidth() - 1,
				center[1]);
		fitProfile(xProfile, xParams, X);
	}

	/**
	 * Retrieves data and fills yProfile the y profile through the centre of the
	 * bead retrieved from the current ImageProcessor.
	 * 
	 * @return a double[][] [0=y as a physical distance ,1=raw intensity,
	 *         2=fitted data][pixel nb, from 0 to width-1]
	 */
	protected void getYprofileAndFit() {
		yProfile = new double[3][ip.getHeight()];
		yProfile[1] = ip.getProcessor().getLine(center[0], 0, center[0],
				ip.getHeight() - 1);
		fitProfile(yProfile, yParams, Y);
	}

	/**
	 * Retrieves data and fills zProfile the z profile through the centre of the
	 * bead retrieved from the current ImageProcessor.
	 * 
	 * @return a double[][] [0=z as a physical distance ,1=raw intensity,
	 *         2=fitted data][pixel nb, from 0 to width-1]
	 */

	protected void getZprofileAndFit() {
            
                if(isFlat()) {
                    zProfile = new double[3][ip.getNSlices()];
                    R2[Z] = 1;
                    return;
                };
            
		ip.setCalibration(new Calibration());
		ip.setRoi(new Line(0, center[1], ip.getWidth() - 1, center[1]));

		ImagePlus crossX = new Slicer().reslice(ip);

		ip.killRoi();
		ip.setCalibration(getCalibration());

		zProfile = new double[3][ip.getNSlices()];
		zProfile[1] = crossX.getProcessor().getLine(center[0], 0, center[0],
				crossX.getHeight() - 1);

		fitProfile(zProfile, zParams, Z);

	}

	/**
	 * Fit profile.
	 * 
	 * @param profile
	 *           the profile
	 * @param params
	 *           the params
	 * @param dimension
	 *           the dimension
	 */
	private void fitProfile(double[][] profile, double[] params, int dimension) {
		double max = profile[1][0];
		double pixelSize = 1;
		int resolIndex = 0;
		Calibration cal = getCalibration();

		switch (dimension) {
			case X :
				pixelSize = cal.pixelWidth;
				break;
			case Y :
				pixelSize = cal.pixelHeight;
				resolIndex = 1;
				break;
			case Z :
				pixelSize = cal.pixelDepth;
				resolIndex = 2;
				break;
		}

		params = new double[4];
		params[0] = max;
		params[1] = max;
		params[2] = 0;
		params[3] = 2 * pixelSize;

		for (int i = 0; i < profile[0].length; i++) {
			profile[0][i] = i * pixelSize;
			double currVal = profile[1][i];
			params[0] = Math.min(params[0], currVal);
			if (currVal > max) {
				params[1] = currVal;
				params[2] = profile[0][i];
				max = currVal;
			}
		}

		zParams = params;

		CurveFitter cv = new CurveFitter(profile[0], profile[1]);
		// cv.setOffsetMultiplySlopeParams(0, 1, 2);
		cv.setInitialParameters(params);
		cv.getMinimizer().setMaximumThreads(1);

		// cv.doCustomFit("y = a + b*exp(-(x-c)*(x-c)/(2*d*d))",params,false);
		cv.doFit(CurveFitter.GAUSSIAN);

		params = cv.getParams();

		for (int i = 0; i < profile[0].length; i++)
			profile[2][i] = CurveFitter.f(CurveFitter.GAUSSIAN, params,
					profile[0][i]);

		String paramString = cv.getResultString();

		params[1] += params[0];

		paramString = paramString.substring(paramString.lastIndexOf("ms") + 2);
		R2[dimension] = cv.getFitGoodness();

		switch (dimension) {
			case X :
				xParamString += paramString;
				xR2 = cv.getFitGoodness();
				xParams = params;
				break;
			case Y :
				yParamString += paramString;
				yR2 = cv.getFitGoodness();
				yParams = params;
				break;
			case Z :
				zParamString += paramString;
				zR2 = cv.getFitGoodness();
				zParams = params;
				break;
		}

		resol[resolIndex] = 2 * SQRT2LN2 * params[3];
	}

	/**
	 * Returns a plot object based on the x profile of the current ImagePlus.
	 * 
	 * @return a plot object
	 */
	public Plot getXplot() {

		Plot plot = new Plot("Profile plot along the x axis", "x ("
				+ getCalibration().getUnit() + ")", "Intensity (AU)", xProfile[0],
				xProfile[2]);
		plot.setSize(300, 200);
		plot.setColor(Color.red);
		plot.addPoints(xProfile[0], xProfile[1], Plot.CIRCLE);
		plot.setColor(Color.black);
		plot.addLabel(0.6, 0.13, "Dots: measured\nLine: fitted");
		return plot;
	}

	/**
	 * Returns a plot object based on the y profile of the current ImagePlus.
	 * 
	 * @return a plot object
	 */
	public Plot getYplot() {
		Plot plot = new Plot("Profile plot along the y axis", "y ("
				+ getCalibration().getUnit() + ")", "Intensity (AU)", yProfile[0],
				yProfile[2]);
		plot.setSize(300, 200);
		plot.setColor(Color.red);
		plot.addPoints(yProfile[0], yProfile[1], Plot.CIRCLE);
		plot.setColor(Color.black);
		plot.addLabel(0.6, 0.13, "Dots: measured\nLine: fitted");
		return plot;
	}

	/**
	 * Returns a plot object based on the z profile of the current ImagePlus.
	 * 
	 * @return a plot object
	 */
	public Plot getZplot() {

		Plot plot = new Plot("Profile plot along the z axis", "z ("
				+ getCalibration().getUnit() + ")", "Intensity (AU)", zProfile[0],
				zProfile[2]);
		plot.setSize(300, 200);
		plot.setColor(Color.red);
		plot.addPoints(zProfile[0], zProfile[1], Plot.CIRCLE);
		plot.setColor(Color.black);
		plot.addLabel(0.6, 0.13, "Dots: measured\nLine: fitted");
		return plot;
	}

	/**
	 * Gets the minimum value among X, Y and Z fitting goodness.
	 * 
	 * @return the fitting goodness
	 */
	public double getMinimumFittingGoodness() {

		DescriptiveStatistics stats = new DescriptiveStatistics(R2);
		return stats.getMin();
	}

	/**
	 * Gets the fitting goodness.
	 * 
	 * @param axe
	 *           the axe
	 * @return the fitting goodness
	 */
	public double getFittingGoodness(int axe) {
		return R2[axe];
	}

	/**
	 * Gets the fitting parameter mean.
	 *
	 * @param parameter the parameter
	 * @return the fitting parameter mean
	 */
	public double getFittingParameterMean(int parameter) {

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int a = 0; a != 3; a++) {
			stats.addValue(getFittingParameter(a, parameter));
		}

		return stats.getMean();

	}

	/**
	 * Gets the fitting parameter.
	 *
	 * @param axe the axe
	 * @param parameter the parameter
	 * @return the fitting parameter
	 */
	public double getFittingParameter(int axe, int parameter) {
		try {
			switch (axe) {

				case X :
					return xParams[parameter];
				case Y :
					return yParams[parameter];
				case Z :
					return zParams[parameter];

			}
			return 0.0;
		} catch (NullPointerException e) {
			return 0.0;
		}
	}

	/**
	 * Gets the c parameter.
	 * 
	 * @param axe
	 *           the axe
	 * @return the c parameter
	 */

	public double getCParameter(int axe) {
		try {
			switch (axe) {

				case X :
					return xParams[2];
				case Y :
					return yParams[2];
				case Z :
					return zParams[2];

			}
			return 0.0;
		} catch (NullPointerException e) {
			return 0.0;
		}
	}

	/**
	 * Returns the calculated resolutions in all available dimensions, i.e. FWHM
	 * after fitting the 2 or 3 profiles
	 * 
	 * @return the x, y and z (if applicable) resolutions as a double array of
	 *         size 2 (or 3).
	 */
	public double[] getResolutions() {
		return resol;
	}

	/**
	 * Gets the resolution.
	 * 
	 * @param axe
	 *           the axe
	 * @return the resolution
	 */
	public double getResolution(int axe) {
		return resol[axe];
	}

	/**
	 * Gets the stack size.
	 * 
	 * @return the stack size
	 */
	public double getStackSize() {
		return getCalibration().pixelDepth * stackSize;
	}

	/**
	 * Discard.
	 * 
	 * @param reason
	 *           the reason
	 */
	public void discard(String reason) {
		setValid(false, reason);
		System.out.println("be cause of : " + reason);
	}

	/**
	 * Sets the valid.
	 * 
	 * @param isValid
	 *           the is valid
	 * @param reason
	 *           the reason
	 */
	public void setValid(boolean isValid, String reason) {
		if (isValid == false) {
			System.out.println(getId() + " has been discarded !");

			setInvalidityReason(reason);
		}
		this.isValid = isValid;
	}

	/**
	 * Checks if is valid.
	 * 
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Gets the correction resolution.
	 * 
	 * @param axe
	 *           the axe
	 * @return the correction resolution
	 */
	public double getCorrectionResolution(int axe) {
		return getSource().getMicroscope().getCorrectedResolution(
				getResolution(axe), axe);
	}

	/**
	 * Gets the distance from center.
	 * 
	 * @return the distance from center
	 */
	public double getDistanceFromCenter() {
		double centerX = getSource().getFieldOfViewWidth() / 2;
		double centerY = getSource().getFieldOfViewHeight() / 2;

		double x = getFovX();
		double y = getFovY();

		double d = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2);
		return Math.sqrt(d);
	}

	/**
	 * Gets the x params.
	 * 
	 * @return the x params
	 */
	public String getXParams() {
		return xParamString;
	}

	/**
	 * Gets the y params.
	 * 
	 * @return the y params
	 */
	public String getYParams() {
		return yParamString;
	}

	/**
	 * Gets the z params.
	 * 
	 * @return the z params
	 */
	public String getZParams() {
		return zParamString;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		// TODO Auto-generated method stub
		return MathUtils.round(boundaries.getWidth());
	}

	/**
	 * Gets the real width.
	 * 
	 * @return the real width
	 */
	public double getRealWidth() {
		return getWidth() * getCalibration().pixelWidth;
	}

	/**
	 * Gets the real height.
	 * 
	 * @return the real height
	 */
	public double getRealHeight() {
		return getHeight() * getCalibration().pixelHeight;
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return MathUtils.round(boundaries.getHeight());
	}

	/**
	 * Gets the raw fwhm.
	 * 
	 * @param axe
	 *           the axe
	 * @return the raw fwhm
	 */
	public double getRawFWHM(int axe) {
		return resol[axe];
	}

	/**
	 * Gets the shift.
	 *
	 * @param axe the axe
	 * @return the shift
	 */
	public double getShift(int axe) {
		if (axe == PSFj.X_AXIS)
			return getDeltaX();
		if (axe == PSFj.Y_AXIS)
			return getDeltaY();
		if (axe == PSFj.Z_AXIS)
			return getDeltaZ();
		return Double.NaN;
	}

	/**
	 * Gets the delta x.
	 * 
	 * @return the delta x
	 */
	public double getDeltaX() {
		return deltaX;
	}

	/**
	 * Sets the delta x.
	 * 
	 * @param deltaX
	 *           the new delta x
	 */
	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}

	/**
	 * Gets the delta y.
	 * 
	 * @return the delta y
	 */
	public double getDeltaY() {
		return deltaY;
	}

	/**
	 * Sets the delta y.
	 * 
	 * @param deltaY
	 *           the new delta y
	 */
	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}

	/**
	 * Gets the delta z.
	 * 
	 * @return the delta z
	 */
	public double getDeltaZ() {
		return deltaZ;
	}

	/**
	 * Sets the delta z.
	 * 
	 * @param deltaZ
	 *           the new delta z
	 */
	public void setDeltaZ(double deltaZ) {
		this.deltaZ = deltaZ;
	}

	/**
	 * Gets the alter ego.
	 * 
	 * @return the alter ego
	 */
	public BeadFrame getAlterEgo() {
		return alterEgo;
	}

	/**
	 * Gets the z profile.
	 * 
	 * @return the z profile
	 */
	public double getZProfile() {
		return getCParameter(BeadFrame.Z);
	}

	/**
	 * Reset alter ego.
	 */
	public void resetAlterEgo() {
		alterEgo = null;
	}

	/**
	 * Sets the alter ego.
	 * 
	 * @param alterEgo
	 *           the new alter ego
	 */
	public void setAlterEgo(BeadFrame alterEgo) {

		if (alterEgo == null)
			return;

		// calculating parameters
		deltaX = alterEgo.getCentroidXInImage() - getCentroidXInImage();
		deltaY = alterEgo.getCentroidYInImage() - getCentroidYInImage();

		deltaX = deltaX * getCalibration().pixelWidth;
		deltaY = deltaY * getCalibration().pixelHeight;

		deltaZ = alterEgo.getZProfile() - getZProfile();// getZProfile()-alterEgo.getZProfile();

		this.alterEgo = alterEgo;
	}

	/**
	 * Gets the side view image.
	 * 
	 * @return the side view image
	 */
	public ImagePlus getSideViewImage() {
		if (sideViewImage == null) {
			SideViewGenerator svg = new SideViewGenerator();
			sideViewImage = svg.getPanelView(getSubstack(),
					SideViewGenerator.MAX_METHOD, true, true, 5, false, null, 0);

		}
		return sideViewImage;
	}

	/**
	 * Sets the side view image.
	 * 
	 * @param sideViewImage
	 *           the new side view image
	 */
	public void setSideViewImage(ImagePlus sideViewImage) {
		this.sideViewImage = sideViewImage;
	}

	/**
	 * Gets the distance including z.
	 * 
	 * @param bead
	 *           the bead
	 * @return the distance including z
	 */
	public double getDistanceIncludingZ(BeadFrame bead) {
		// TODO Auto-generated method stub
		/*
		 * double x1 = getFovX(); double x2 = bead.getFovX();
		 * 
		 * double y1 = getFovY(); double y2 = bead.getFovY();
		 * 
		 * double z1 = getCentroidZ(); double z2 = bead.getCentroidZ();
		 */

		double distance = Math.pow(deltaX, 2) + Math.pow(deltaY, 2)
				+ Math.pow(deltaZ, 2);

		return Math.sqrt(distance);

	}

	/**
	 * Gets the invalidity reason.
	 * 
	 * @return the invalidity reason
	 */
	public String getInvalidityReason() {
		return invalidityReason;
	}

	/**
	 * Sets the invalidity reason.
	 * 
	 * @param invalidityReason
	 *           the new invalidity reason
	 */
	public void setInvalidityReason(String invalidityReason) {

		if (this.invalidityReason == null)
			this.invalidityReason = invalidityReason;
	}

	/**
	 * Gets the bead color.
	 *
	 * @return the bead color
	 */
	public Color getBeadColor() {
		return getSource().getBeadsColor();
	}

	/**
	 * Gets the overlay with alter ego.
	 *
	 * @return the overlay with alter ego
	 */
	public ImageProcessor getOverlayWithAlterEgo() {
		if (getAlterEgo() != null) {
			ImageProcessor ip1 = getSource().copyRoi(getBoundariesAsRectangle());
			ImageProcessor ip2 = getAlterEgo().getSource().copyRoi(
					getBoundariesAsRectangle());

			ip1.setLut(LUT.createLutFromColor(getBeadColor()));
			ip2.setLut(LUT.createLutFromColor(getAlterEgo().getBeadColor()));

			ip1 = normalizeImage(ip1).convertToRGB();
			ip2 = getAlterEgo().normalizeImage(ip2).convertToRGB();

			ip1.copyBits(ip2, 0, 0, Blitter.ADD);

			return ip1;
		} else
			return new ColorProcessor(getWidth(), getHeight());
	}

	/**
	 * Normalize image.
	 *
	 * @param ip the ip
	 * @return the image processor
	 */
	public ImageProcessor normalizeImage(ImageProcessor ip) {
		ip = ip.convertToFloat();
		System.out.println(ip.getStatistics().mean);
		ip.multiply(1.0 / (ip.getStatistics().mean + ip.getStatistics().stdDev));
		ip.add(-1);
		ip.setMinAndMax(0, ip.getMax());
		return ip.convertToByte(true);
	}

	/**
	 * Gets the asymetry.
	 *
	 * @return the asymetry
	 */
	public double getAsymetry() {
		return getResolution(0) / getResolution(1);
	}

	/**
	 * Gets the slice number.
	 *
	 * @return the slice number
	 */
	public int getSliceNumber() {
		return stackSize;
	}

        
        public boolean isFlat() {
            return getSliceNumber() == 1;
        }
        
	/**
	 * Checks if is ignored.
	 *
	 * @return true, if is ignored
	 */
	public boolean isIgnored() {
		return isIgnored;
	}

	/**
	 * Sets the ignored.
	 *
	 * @param isIgnored the new ignored
	 */
	public void setIgnored(boolean isIgnored) {
		this.isIgnored = isIgnored;
	}
}
