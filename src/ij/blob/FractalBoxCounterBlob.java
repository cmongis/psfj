package ij.blob;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;
import ij.util.Tools;
/**
 * This ImageJ Class was adapted by Thorsten Wagner for IJBlob Project 
 */

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.StringTokenizer;

/**
Calculate the so-called "capacity" fractal dimension.  The algorithm
is called, in fractal parlance, the "box counting" method.  In the
simplest terms, the routine counts the number of boxes of a given size
needed to cover a one pixel wide, binary (black on white) border.
The procedure is repeated for boxes that are 2 to 64 pixels wide. 
The output consists of two columns labeled "size" and "count". A plot 
is generated with the log of size on the x-axis and the log of count on
the y-axis and the data is fitted with a straight line. The slope (S) 
of the line is the negative of the fractal dimension, i.e. D=-S.

A full description of the technique can be found in T. G. Smith,
Jr., G. D. Lange and W. B. Marks, Fractal Methods and Results in Cellular Morphology,
which appeared in J. Neurosci. Methods, 69:1123-126, 1996.

---
12/Jun/2006 G. Landini added "set is white" option, otherwise the plugin
assumes that the object is always low-dimensional (i.e. the phase with
the smallest number of pixels). Now it works fine for sets with D near to 2.0

*/
public class FractalBoxCounterBlob {
	static String sizes = "2,3,4,6,8,12,16,32,64";
	static boolean blackBackground;
	int[] boxSizes;
	float[] boxCountSums;
	int maxBoxSize;
	int[] counts;
	Rectangle roi;
	int foreground;
	ImagePlus imp;
	
	public FractalBoxCounterBlob() {
		// TODO Auto-generated constructor stub
		boxSizes = s2ints(sizes);
	}
	/**
	 * 
	 * @param blob The for which the fractal dimension have to determined
	 * @return An 2 element array. [0] = Fractal Dimension, [1] = Goodness of Fit
	 */
	public double[] getFractcalDimension(Blob blob) {
		Polygon p = blob.getOuterContour();
		PolygonRoi proi = new PolygonRoi(p, PolygonRoi.FREEROI);
		Rectangle r = proi.getBounds();
		ImagePlus help = NewImage.createByteImage("", r.width, r.height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = help.getProcessor();
		blob.draw(ip, Blob.DRAW_HOLES);
		ip.invert();
		imp = new ImagePlus("",ip);
		
		boxCountSums = new float[boxSizes.length];
		for (int i=0; i<boxSizes.length; i++)
			maxBoxSize = Math.max(maxBoxSize, boxSizes[i]);
		counts = new int[maxBoxSize*maxBoxSize+1];
		imp.deleteRoi();
		double[] FDandGOF = doBoxCounts(ip);
		return FDandGOF;
	}

	/** Breaks the specified string into an array
	 of ints. Returns null if there is an error.*/
	public int[] s2ints(String s) {
		StringTokenizer st = new StringTokenizer(s, ", \t");
		int nInts = st.countTokens();
		int[] ints = new int[nInts];
		for(int i=0; i<nInts; i++) {
			try {ints[i] = Integer.parseInt(st.nextToken());}
			catch (NumberFormatException e) {IJ.log(""+e); return null;}
		}
		return ints;
	}
	
	public void setBoxSizes(int[] sizes){
		boxSizes = sizes;
	}
	
	public void setBoxSizes(String sizes){
		boxSizes = s2ints(sizes);
	}

	boolean FindMargins(ImageProcessor ip) {
		if (IJ.debugMode) IJ.log("FindMargins");
		int[] histogram = new int[256];
		int width = imp.getWidth();
		int height = imp.getHeight();
		int left, right, top, bottom;

		//Find left edge
 		left = -1;
		do {
			left++;
			if (left>=width) {
				IJ.error("No non-backround pixels found.");
				return false;
			}
			ip.setRoi(left, 0, 1, height);
			histogram = ip.getHistogram();
		} while (histogram[foreground]==0);

		//Find top edge
		top = -1;
		do {
			top++;
			ip.setRoi(left, top, width-left, 1);
			histogram = ip.getHistogram();
		} while (histogram[foreground]==0);

		//Find right edge
		right =width+1;
		do {
			right--;
			ip.setRoi(right-1, top, 1, height-top);
			histogram = ip.getHistogram();
		} while (histogram[foreground]==0);

		//Find bottom edge
		bottom =height+1;
		do {
			bottom--;
			ip.setRoi(left, bottom-1, right-left, 1);
			histogram = ip.getHistogram();
		} while (histogram[foreground]==0);

		roi = new Rectangle(left, top, right-left, bottom-top);
		return true;
	}

	int count(int size, ImageProcessor ip) {
		int[] histogram = new int[256];
		int x = roi.x;
		int y = roi.y;
		int w = (size<=roi.width)?size:roi.width;
		int h = (size<=roi.height)?size:roi.height;
		int right = roi.x+roi.width;
		int bottom = roi.y+roi.height;
		int maxCount = size*size;
		
		for (int i=1; i<=maxCount; i++)
			counts[i] = 0;
		boolean done = false;
		do {
			ip.setRoi(x, y, w, h);
			histogram = ip.getHistogram();
			counts[histogram[foreground]]++;
			x+=size;
			if (x+size>=right) {
				w = right-x;
				if (x>=right) {
					w = size;
					x = roi.x;
					y += size;
					if (y+size>=bottom)
						h = bottom-y;
					done = y>=bottom;
				}
			}
		} while (!done);
		int boxSum = 0;
		int nBoxes;
		for (int i=1; i<=maxCount; i++) {
			nBoxes = counts[i];
			if (nBoxes!=0)
				boxSum += nBoxes;
		}
		return boxSum;
	}
	
	double[] getSlopeAndGoodnessOfFit() {
		int n = boxSizes.length;
		float[] sizes = new float[boxSizes.length];
		for (int i=0; i<n; i++)
			sizes[i] = (float)Math.log(boxSizes[i]);
		CurveFitter cf = new CurveFitter(Tools.toDouble(sizes), Tools.toDouble(boxCountSums));
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		
		double[] p = cf.getParams();
		double[] slopeandrsquared = new double[2];
		slopeandrsquared[0] = -p[1];
		slopeandrsquared[1] = cf.getFitGoodness();
		return slopeandrsquared;			
	}

	double[] doBoxCounts(ImageProcessor ip) {
		if (!FindMargins(ip))
			return null;

		for (int i=0; i<boxSizes.length; i++) {
			int boxSum = count(boxSizes[i], ip);

			boxCountSums[i] = (float)Math.log(boxSum);
		}
		double[] D = getSlopeAndGoodnessOfFit();
		imp.deleteRoi();
		return D;
	}
}

