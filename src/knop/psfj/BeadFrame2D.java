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

import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import knop.psfj.resolution.Gaussian2DFit.GaussianFit;


import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;
// TODO: Auto-generated Javadoc
//import knop.utils.stats.DataSet;

/**
 * The Class BeadFrame2D.
 */
public class BeadFrame2D extends BeadFrame {

	/** The theta. */
	double theta = 0;

	/** The fitted params. */
	double[] fittedParams;
	
	/** The abc. */
	double[] abc;
	
	/** The X0. */
	public static int X0 = GaussianFit.XC;
	
	/** The Y0. */
	public static int Y0 = GaussianFit.YC;
	
	/** The A0. */
	public static int A0 = GaussianFit.INT;
	
	/** The bgr. */
	public static int BGR = GaussianFit.BGR;
	
	/** The sx. */
	public static int SX = GaussianFit.S1;
	
	/** The sy. */
	public static int SY = GaussianFit.S2;
	
	/** The theta. */
	public static int THETA = GaussianFit.S3;

	/** The theoretical bead. */
	ImageProcessor theoreticalBead;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		/*
		 * DataSet dataset = new DataSet();
		 * 
		 * 
		 * String fileName =
		 * "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/different_frame_size_"
		 * + new Date().toLocaleString().replace(" ", "_") + ".csv";
		 * 
		 * //String imageName =
		 * "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/different sigma_no asymmetry_integer centers_2048x2048/sigma_y=3.0_asymmetry=1.0_theta=0.000.tif"
		 * ; //String imageName =
		 * "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/sigma3_asymmetry1-5_different thetas/sigma_y=3.0_asymmetry=1.5_theta=0.800.tif"
		 * ; String imageName =
		 * "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/sigma3_theta0-4_different asymmetries/sigma_y=3.0_asymmetry=1.1_theta=0.400.tif"
		 * ;
		 * 
		 * for(int i = 1;i <= Math.pow(2,15);i*=2) { BeadImage image = new
		 * BeadImage(imageName);
		 * 
		 * 
		 * image.workFromMemory(); addNoise(image, i);
		 * dataset.addValue("division", i);
		 * 
		 * analyseBeadImage(image, -1, 20, dataset, ""+i);
		 * 
		 * }
		 * 
		 * TextUtils.writeStringToFile(fileName, dataset.exportToString(), false);
		 * FileUtils.openFolder(fileName);
		 */
		// String folder =
		// "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/sigma3_theta_0_different asymmetries/";
		// String folder =
		// "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/sigma3_asymmetry1-5_different thetas/";
		// String folder =
		// "/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/2dfit_testimages/different sigma_no asymmetry_integer centers_2048x2048/";
		String folder = "/Users/cyril/test_img/to analyze";
		//analyseFolder(folder, ".*tif$");
	}

	/**
	 * Instantiates a new bead frame2 d.
	 *
	 * @param id the id
	 * @param r the r
	 */
	public BeadFrame2D(int id, Rectangle r) {
		super(id, r);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Recenter centroid from z fitting results.
	 */
	private void recenterCentroidFromZFitting() {
		
		
		
		// translating the new center from float to double
		float newCenter = new Float(getFittingParameter(Z, 2)
				/ getCalibration().pixelDepth);
		
		// translating to int that will indicate the focus plane
		int newIntCenter = MathUtils.round(newCenter);

		// if the new center is off limit (which can happen) the function is aborted, the old center will be used
		if(newCenter < 0 || newCenter > ip.getStackSize()-1)  { System.out.println("Z fitting off limits");return; }
		
		// updating the z coordinate of the centroid and center
		centroid[2] = newCenter;
		center[2] = newIntCenter;
	}
	
	/**
	 * Recenter centroid from the 2d fitting. (usually done after the filtering)
	 */
	public void recenterCentroidFrom2DFitting() {
		centroid[0] = new Float(fittedParams[X0]);
		centroid[1] = new Float(fittedParams[Y0]);
	}

	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrame#findPSF()
	 */
	public void findPSF() {
		try {
			if (isValid == false) {
				resol = new double[]{0.0, 0.0, 0.0};
				return;
			}
			
			try {
				
			// this method find the intencity weighted center of the bead (or so called centroid)
			findCenter();
			}
			// second try
			catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				findCenter();
				System.out.println("Second try is a success");
			}
			
			// if no centroid has been found, then it's a dead end.
			if(centroid == null) {
				setValid(false,"Error when calculating center");
			}
			
			// if(boundaries != null) centerBoundariesAroundCentroid();

			// ip = null;

			// the center of the centroid is converted to int and the Z position is used to indicated
			// the focus plane of the bead.
			getSubstack().setSlice(getCentroidZAsInt());

			resol = new double[3];
			R2 = new double[3];

			// making sure no ROI has been set
			ip.restoreRoi();
			
			// fitting in Z
			getZprofileAndFit();
			
			// redefining the focus plane using the results of the Z fiting
			recenterCentroidFromZFitting();
			
			// calculating SideViewImage (for results)
			getSideViewImage();

			// preparing 2D Gaussian fitting
			GaussianFit fit = new GaussianFit(3, 2);
			
			// retrieving the focus plane of the bead based on the previous calculations
			ImageProcessor fittedSlice = ip.getStack().getProcessor(center[2] + 1);

			
			if(fittedSlice.getBitDepth() == 8) fittedSlice = fittedSlice.duplicate().convertToShort(true);
			
			// 2D fitting in action (the second parameter is the max number of iterations
			double[] fitParams = fit.doGaussianFit(fittedSlice, 3500);// fit.params0_;

			
			// if the 2d fitting works
			if (fitParams.length >= 6) {

				
				// the algorithm returns the following parameters
				double sx = fitParams[GaussianFit.S1]; // sigma x
				double sy = fitParams[GaussianFit.S2]; // sigma y
				
				// also : x0,y0 intensity, background, and theta
				
				double st;

				// System.out.printf("sx = %.3f, sy= %.3f, s3 = %.3f, sx/sy = %.3f",sx,sy,s3,sx/sy);
				
				// making sure that sx is the sigma of the lower axis
				if (sx > sy) {
					st = sx;
					sx = sy;
					sy = st;

					
				}

				// retrieving a,b,c parameters of the 2D Gaussian
				abc = fit.getABC();
				
				// updating parameters
				fitParams[SX] = sx;
				fitParams[SY] = sy;

				// calculating the resoluition
				double fwhmX = sx * getCalibration().pixelWidth * 2 * SQRT2LN2;

				double fwhmY = sy * getCalibration().pixelHeight * 2 * SQRT2LN2;

				double intensityMean = ip.getStatistics().mean;
				
				
				// fitting score
				double rms = fit.getRMS();
				
				// updating resolution
				resol[0] = fwhmX;
				resol[1] = fwhmY;

				// making "BeadFrame Compatible" params
				xParams = new double[]{fitParams[GaussianFit.BGR], fitParams[A0],
						resol[0], 0.0};
				yParams = new double[]{fitParams[GaussianFit.BGR], fitParams[A0],
						resol[1], 0.0};

				double sum = 0;
				
				// calculating the fitting score
				for (short p : (short[]) fittedSlice.getPixels()) {
					sum += Math.pow((1.0 * p - intensityMean), 2);
				}
				
				// storing fitting score
				R2[X] = 1.0 - ((fittedSlice.getWidth() * fittedSlice.getHeight() * Math
						.pow(rms, 2)) / sum);

				R2[Y] = R2[X];
				
				// I don't remember why I put this but it must be important
				while (fittedParams == null) {
					fittedParams = fitParams;
				}

				// if the theta has been found (it's always found but we never know...)
				if (fitParams.length >= 7)
					theta += fitParams[GaussianFit.S3];
				
				// updating some strings to be displayed 
				xParamString = String
						.format(
								"    Fitted on i(x,y) = A * exp(-(a*(x-xc)^2 + c*(y-yc)^2 + 2*b*(x-xc)*(y-yc))) + B\n"
										+ " R^2 = %.3f\n"
										+ "\nParameters:\n"
								+ " A = %.3f   (brightness)\n"
								+ " B = %.3f   (background)\n"
								+ " a = %.3f px\n"
								+ " b = %.3f px\n"
								+ " c = %.3f px\n"
								+ " xc = %.3f px\n"
								+ " yc = %.3f px\n"
								/*
								+ " sigma x = %.3f um\n"
								+ " sigma y = %.3f um\n"
								+ " theta = %.3f degrees\n"
								*/
								,
								R2[X],
								fitParams[A0],
								fitParams[BGR],
								abc[0],
								abc[1],
								abc[2],
								fitParams[X0],
								fitParams[Y0]
								
								
								);

				xParamString = xParamString.replace("\n", "\n\t     ");
			}

			// freeing memeory
			ip = null;

			// System.gc();

			setChanged();
			notifyObservers(new Message(this, MSG_PSF_FOUND));

		} catch (Exception e) {

			e.printStackTrace();
		}

		catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.out.println(new Message(this, "error",
					"There is not enough memory to process the beads !"));
		}
	}

	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrame#getTheta()
	 */
	@Override
	public double getTheta() {
		return theta;
	}

	
	/**
	 * Sqr.
	 *
	 * @param d the d
	 * @return the double
	 */
	public double sqr(double d) {
		return Math.pow(d, 2);
	}

	/**
	 * Gets the overlay of the actual data and the theoretical bead.
	 *
	 * @return the overlay with theoretical
	 */
	public ImageProcessor getOverlayWithTheoretical() {
		
		ImageProcessor real = getSubstack().getStack()
				.getProcessor(getCentroidZAsInt() + 1).duplicate();
		ImageProcessor theoretical = generateTheoreticalBead();

		int frameSize = real.getWidth();
		
		int border = 2;
		
		// creating the result image
		ImageProcessor result = new ColorProcessor(frameSize*2+border,frameSize*3+border);
		
		// filling with white
		result.setColor(Color.white);
		result.fill();
		
		real.resetMinAndMax();

		// setting the LUT
		real.setLut(LUT.createLutFromColor(Color.red));
		theoretical.setLut(LUT.createLutFromColor(Color.green));

		// converting into RGB
		real = real.convertToRGB();
		theoretical = theoretical.convertToRGB();

		// copying each image along side
		result.copyBits(real, 0, 0, Blitter.COPY);
		result.copyBits(theoretical, frameSize+border, 0, Blitter.COPY);
		
		// scaling them twice their orignal size
		real = real.resize(frameSize*2,frameSize*2,true);
		theoretical = theoretical.resize(frameSize*2,frameSize*2,true);
	
		// overlaying
		result.copyBits(theoretical, border/2, frameSize+border, Blitter.COPY);
		result.copyBits(real, border/2, frameSize+border, Blitter.ADD);	
		return result;
	}

	/**
	 * Generate a theoretical bead.
	 *
	 * @return the image processor
	 */
	public ImageProcessor generateTheoreticalBead() {

		ImageProcessor ip = new ShortProcessor(boundaries.getBounds().width,
				boundaries.getBounds().height);

		double A, x0, y0, a, b, c, Z, sigma_x, sigma_y, theta, bgr;

		if (fittedParams != null) {

			A = fittedParams[A0];
			x0 = fittedParams[X0];
			y0 = fittedParams[Y0];
			sigma_x = fittedParams[SX];
			sigma_y = fittedParams[SY];
			theta = fittedParams[THETA] + Math.PI / 2;
			bgr = fittedParams[BGR];

			try {
				if (theta < 0) {
					// theta = Math.abs(theta);
					// double t = sigma_x;
					// sigma_x = sigma_y;
					// sigma_y = t;

				}

				a = Math.pow(Math.cos(theta), 2) / 2 / Math.pow(sigma_x, 2)
						+ Math.pow(Math.sin(theta), 2) / 2 / Math.pow(sigma_y, 2);
				b = -Math.sin(2 * theta) / 4 / sqr(sigma_x) + Math.sin(2 * theta)
						/ 4 / sqr(sigma_y);
				c = Math.pow(Math.sin(theta), 2) / 2 / Math.pow(sigma_x, 2)
						+ Math.pow(Math.cos(theta), 2) / 2 / Math.pow(sigma_y, 2);

				// [X, Y] = meshgrid(-5:.1:5, -5:.1:5);

				for (int X = 0; X != ip.getWidth(); X++) {
					for (int Y = 0; Y != ip.getHeight(); Y++) {
						Z = A
								* Math.exp(-(a * sqr(X - x0) + 2 * b * (X - x0)
										* (Y - y0) + c * sqr(Y - y0))) + bgr;
						// System.out.println(Z);
						ip.set(X, Y, MathUtils.round(Z));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ip;

	}
	
	/**
	 * Gets the fitted parameters.
	 *
	 * @return the fitted parameters
	 */
	public double[] getFittedParameters() {
		return fittedParams;
	}
       
        
	/*
	public static void analyseBeadImage(BeadImage image, int threshold,
			int frameSize, DataSet dataset, String id) {

		try {

			image.setFocusPlane(29);

			image.getMicroscope().setCalibration(new Calibration());

			image.getMicroscope().save();

			if (threshold == -1)
				image.autoThreshold();
			else
				image.setThresholdValue(threshold);
			if (frameSize == -1)
				image.autoBeadEnlargement();
			else
				image.setFrameSize(frameSize);

			System.out.println(image.getFrameSize());

			BeadFrameProcessorAsync processor = new BeadFrameProcessorAsync(
					image.getBeadFrameList());

			long start = System.currentTimeMillis();

			processor.process();
			processor.filter();

		
			 * ImageProcessor verif = image.getPlane(0).convertToRGB();
			 * for(BeadFrame frame : image.getBeadFrameList()) {
			 * verif.setColor(Color.RED);
			 * verif.drawDot(frame.getWeightedXInImage(),
			 * frame.getWeightedYInImage()); verif.setColor(Color.BLUE);
			 * verif.drawDot(frame.getFrameCenterX(),frame.getFrameCenterY()); }
			 

			// ImageProcessorUtils.show(verif);

			long duration = System.currentTimeMillis() - start;
			// duration = duration;
			FovDataSet imageStats = image.getBeadFrameList().getDataSet(true);

			dataset.addValue("frame size", image.getFrameSize());

			// dataset.addValue("", value);

			dataset.addValue("sigma_x", imageStats.getColumnStatistics("sigma_x")
					.getMean());

			dataset
					.addValue("sigma_x_stddev",
							imageStats.getColumnStatistics("sigma_x")
									.getStandardDeviation());

			dataset.addValue("sigma_y", imageStats.getColumnStatistics("sigma_y")
					.getMean());

			dataset
					.addValue("sigma_y_stddev",
							imageStats.getColumnStatistics("sigma_y")
									.getStandardDeviation());

			dataset.addValue("asymmetry",
					imageStats.getColumnStatistics("asymmetry").getMean());

			dataset.addValue("asymmetry_stddev",
					imageStats.getColumnStatistics("asymmetry")
							.getStandardDeviation());

			dataset.addValue("theta", imageStats.getColumnStatistics("theta_rad")
					.getMean());
			dataset.addValue("theta_stddev",
					imageStats.getColumnStatistics("theta_rad")
							.getStandardDeviation());

			dataset.addValue("beads found", image.getTotalBeadCount());
			dataset.addValue("beads deleted", image.getDeletedBeadsCount());

			dataset.addValue("process time", duration);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
