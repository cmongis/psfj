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
import ij.process.ImageProcessor;
import imagescience.image.Image;
import imagescience.transform.Scale;

import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import knop.psfj.utils.ImageProcessorUtils;
import knop.psfj.utils.MathUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;



// TODO: Auto-generated Javadoc
/**
 * The Class BeadAveragerSubPixel.
 */
public class BeadAveragerSubPixel extends BeadAverager {

	/** The extended frame list. */
	ArrayList<ExtendedBeadFrame> extendedFrameList = new ArrayList<ExtendedBeadFrame>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see knop.psfj.BeadAverager#averageBead(java.util.ArrayList)
	 */
	@Override
	public ImagePlus averageBead(ArrayList<BeadFrame> list) {

		ImagePlus result = null;
		int extensionFactor = 10;
		int addedPixel = 3;
		PerfectCropper cropperX = null;
		PerfectCropper cropperY = null;
		PerfectCropper cropperZ = null;
		setTotalBeads(list.size());

		list = filter(list);

		int idealRange = (2 * addedPixel * extensionFactor);

		int fovWidth = list.get(0).getSource().getImageWidth();
		int fovHeight = list.get(0).getSource().getImageHeight();

		for (BeadFrame frame : list) {
			ExtendedBeadFrame extendedFrame = new ExtendedBeadFrame(frame,
					extensionFactor, addedPixel);

			int x = extendedFrame.boundaries.x;
			int y = extendedFrame.boundaries.y;
			int x2 = x + extendedFrame.boundaries.width;
			int y2 = y + extendedFrame.boundaries.height;

			// if the extended frame is out of the field of view, it,s not
			// taken into account
			if (x < 0 || y < 0 || x2 > fovWidth || y2 > fovHeight) {
				incrementFilteredOutBeadCount();
				continue;
			}
			extendedFrameList.add(extendedFrame);

			if (cropperX == null) {
				cropperX = new PerfectCropper(extendedFrame.getExtendedWidth(),
						extendedFrame.getExtendedWidth() - idealRange);
				cropperY = new PerfectCropper(extendedFrame.getExtendedHeight(),
						extendedFrame.getExtendedHeight() - idealRange);
				cropperZ = new PerfectCropper(extendedFrame.getExtendedDepth(), -1);
			}

			cropperX.addCenter(extendedFrame.getCentroidXAsInt());
			cropperY.addCenter(extendedFrame.getCentroidYAsInt());
			cropperZ.addCenter(extendedFrame.getCentroidZAsInt());

		}

		cropperX.calculateCommonRange();
		cropperY.calculateCommonRange();
		cropperZ.calculateCommonRange();

		ArrayList<ImageStack> centeredStacks = new ArrayList<ImageStack>();

		ImageStack averageStack = null;
		updateProgress(1, extendedFrameList.size());
		int p = 1;
		int max = extendedFrameList.size();
		// for each extended frame
		for (ExtendedBeadFrame extFrame : extendedFrameList) {

			// we get the extended frame
			ImageStack croppedStack = extFrame.getExtendedStack();

			// we calculate the boundaries of the ROI using the PerfectCropper
			// object
			// (it centers the center around the centroid).
			int x = cropperX.getBoundaryLeft(extFrame.getCentroidXAsInt());
			int w = cropperX.getBoundaryRight(extFrame.getCentroidXAsInt()) - x;

			int y = cropperY.getBoundaryLeft(extFrame.getCentroidYAsInt());
			int h = cropperY.getBoundaryRight(extFrame.getCentroidYAsInt()) - y;

			// StackProcessor processor = new StackProcessor(croppedStack);
			// the stack is cropped in X and Y
			croppedStack = ImageProcessorUtils.crop(croppedStack, new Rectangle(x,
					y, w, h));

			System.out.println(String.format(
					"Cropped zone : (%d,%d) : %d x %d, max : %d\nfrom %d to %d", x,
					y, w, h, extFrame.getExtendedWidth(),
					cropperZ.getBoundaryLeft(extFrame.getCentroidZAsInt()),
					cropperZ.getBoundaryRight(extFrame.getCentroidZAsInt())));
			System.out.println(String.format(
					"Effective Cropped zone : (%d,%d) : %d x %d,\nfrom %d to %d", x,
					y, croppedStack.getWidth(), croppedStack.getHeight(),
					cropperZ.getBoundaryLeft(extFrame.getCentroidZAsInt()),
					cropperZ.getBoundaryRight(extFrame.getCentroidZAsInt())));

			// then the stack is cropped in Z
			int originalStackSize = croppedStack.getSize();
			for (int i = cropperZ.getBoundaryRight(extFrame.getCentroidZAsInt()); i != originalStackSize; i++) {
				croppedStack.deleteLastSlice();
			}

			for (int i = 0; i != cropperZ.getBoundaryLeft(extFrame
					.getCentroidZAsInt()); i++) {
				croppedStack.deleteSlice(1);
			}
			System.out.println("stack size : " + croppedStack.getSize());
			// then the cropped stack is added to the future average stack
			if (averageStack == null) {
				averageStack = new ImageStack(croppedStack.getWidth(),
						croppedStack.getHeight());
				for (int i = 0; i != croppedStack.getSize(); i++) {
					averageStack.addSlice(croppedStack.getProcessor(i + 1)
							.convertToFloat());
				}

			} else {
				addStack(croppedStack, averageStack);
			}
			updateProgress(p++, max + 10);
			if (p % 8 == 0)
				System.gc();

			// centeredStacks.add(croppedStack);

		}
		updateProgress(max + 2, max + 10);
		System.out.println("averaging endend. dviding by "
				+ extendedFrameList.size());
		// the average stack is divided by the number of used stack
		for (int i = 0; i != averageStack.getSize(); i++) {
			averageStack.getProcessor(i + 1).multiply(
					1.0 / extendedFrameList.size());
		}

		System.out.println("stacling stack x y and z");
		// ImageStack averageStack = averageStacks(centeredStacks);
		ImagePlus averageStackImagePlus = new ImagePlus("Averaged Bead",
				averageStack);
		Scale scaler = new Scale();
		Image wrapper = Image.wrap(averageStackImagePlus);

		updateProgress(max + 5, max + 10);
		wrapper = scaler.run(wrapper, 1.0 / extensionFactor,
				1.0 / extensionFactor, 1.0 / extensionFactor, 1.0, 1.0,
				Scale.LINEAR);
		updateProgress(max + 9, max + 10);
		System.gc();
		System.out.println(" the end");
		System.out.println(wrapper.imageplus());

		updateProgress(0, 7);
		return wrapper.imageplus();
	}

	/**
	 * Adds the stack.
	 * 
	 * @param src
	 *           the src
	 * @param dest
	 *           the dest
	 */
	public void addStack(ImageStack src, ImageStack dest) {
		int stackWidth = dest.getWidth();
		int stackHeight = dest.getHeight();
		int stackDepth = dest.getSize();

		for (int i = 0; i != stackDepth; i++) {
			// dest.getProcessor(i+1).copyBits(src.getProcessor(i+1), 0, 0,
			// Blitter.ADD);
			short[] srcPixels;
			if (src.getBitDepth() == 16) {
				srcPixels = (short[]) src.getProcessor(i + 1).getPixels();
			}
			else {
				srcPixels = (short[]) src.getProcessor(i+1).duplicate().convertToShort(true).getPixels();
			}
				float[] destPixels = (float[]) dest.getProcessor(i + 1).getPixels();

				for (int j = 0; j != destPixels.length; j++) {
					destPixels[j] += srcPixels[j];
				}

			

		}

	}

	/**
	 * The Class ExtendedBeadFrame.
	 */
	public class ExtendedBeadFrame {

		/** The frame. */
		BeadFrame frame;

		/** The boundaries. */
		Rectangle boundaries;

		/** The added pixel. */
		int addedPixel;

		/** The extension factor. */
		int extensionFactor;

		/**
		 * Instantiates a new extended bead frame.
		 * 
		 * @param frame
		 *           the frame
		 */
		public ExtendedBeadFrame(BeadFrame frame) {
			this(frame, 10, 2);
		}

		/**
		 * Instantiates a new extended bead frame.
		 * 
		 * @param frame
		 *           the frame
		 * @param extensionFactor
		 *           the extension factor
		 * @param addedPixel
		 *           the added pixel
		 */
		public ExtendedBeadFrame(BeadFrame frame, int extensionFactor,
				int addedPixel) {

			this.extensionFactor = extensionFactor;
			this.addedPixel = addedPixel;

			Rectangle r = frame.getBoundariesAsRectangle();
			int x = r.x - addedPixel;
			int y = r.y - addedPixel;
			int w = r.width + (2 * addedPixel);
			int h = r.height + (2 * addedPixel);
			boundaries = new Rectangle(x, y, w, h);

			this.frame = frame;

		}

		/**
		 * Gets the extended depth.
		 * 
		 * @return the extended depth
		 */
		public int getExtendedDepth() {
			return frame.stackSize * extensionFactor;
		}

		/**
		 * Gets the extended width.
		 * 
		 * @return the extended width
		 */
		public int getExtendedWidth() {
			return boundaries.width * extensionFactor;
		}

		/**
		 * Gets the extended height.
		 * 
		 * @return the extended height
		 */
		public int getExtendedHeight() {
			return boundaries.height * extensionFactor;
		}

		/**
		 * Gets the centroid x as int.
		 * 
		 * @return the centroid x as int
		 */
		public int getCentroidXAsInt() {
			return MathUtils.round(getCentroidX());
		}

		/**
		 * Gets the centroid y as int.
		 * 
		 * @return the centroid y as int
		 */
		public int getCentroidYAsInt() {
			return MathUtils.round(getCentroidY());
		}

		/**
		 * Gets the centroid z as int.
		 * 
		 * @return the centroid z as int
		 */
		public int getCentroidZAsInt() {
			return MathUtils.round(getCentroidZ());
		}

		/**
		 * Gets the centroid x.
		 * 
		 * @return the centroid x
		 */
		public double getCentroidX() {
			// x when enlarging the frame
			double newX = frame.getCentroidX() + addedPixel;

			return newX * extensionFactor;
		}

		/**
		 * Gets the centroid y.
		 * 
		 * @return the centroid y
		 */
		public double getCentroidY() {
			// x when enlarging the frame
			double newX = frame.getCentroidY() + addedPixel;
			return newX * extensionFactor;
		}

		/**
		 * Gets the centroid z.
		 * 
		 * @return the centroid z
		 */
		public double getCentroidZ() {
			return extensionFactor * frame.getCentroidZ();
		}

		/**
		 * Gets the extended stack.
		 * 
		 * @return the extended stack
		 */
		public ImageStack getExtendedStack() {

			// getting the original substack
			ImageStack source = frame.getSource().getStack();
			ImageStack result = null;

			// for each plane of the stack
			for (int i = 0; i != source.getSize(); i++) {

				// retrieveing the plane
				ImageProcessor ip = source.getProcessor(i + 1);

				// cropping and extended part of the ROI
				ip.setRoi(boundaries);
				ImageProcessor croppedIp = ip.crop();

				// make it bigger !!!
				croppedIp = croppedIp
						.resize(croppedIp.getWidth() * extensionFactor);

				// enlarging it also in Z
				for (int j = 0; j != extensionFactor; j++) {

					if (result == null) {
						result = new ImageStack(croppedIp.getWidth(),
								croppedIp.getHeight());
					}
					result.addSlice(croppedIp.duplicate());
				}
				croppedIp = null;
			}

			return result;

		}

	}

	/**
	 * The Class PerfectCropper.
	 */
	public class PerfectCropper {

		/** The range size. */
		int rangeSize;

		/** The centers. */
		ArrayList<Integer> centers = new ArrayList<Integer>();

		/** The range min. */
		int rangeMin;

		/** The range max. */
		int rangeMax;

		/** The chosen center. */
		int chosenCenter;

		/** The intended range. */
		int intendedRange = -1;

		/**
		 * Instantiates a new perfect cropper.
		 * 
		 * @param max
		 *           the max
		 * @param intendedRange
		 *           the intended range
		 */
		public PerfectCropper(int max, int intendedRange) {
			rangeSize = max;
			this.intendedRange = intendedRange;
		}

		/**
		 * Instantiates a new perfect cropper.
		 * 
		 * @param max
		 *           the max
		 * @param centers
		 *           the centers
		 * @param intendedRange
		 *           the intended range
		 */
		public PerfectCropper(int max, int[] centers, int intendedRange) {
			this(max, intendedRange);
			this.centers = new ArrayList<Integer>();

			for (int c : centers) {
				this.centers.add(c);
			}

		}

		/**
		 * Adds the center.
		 * 
		 * @param v
		 *           the v
		 */
		public void addCenter(int v) {
			this.centers.add(v);
		}

		/**
		 * Calculate common range.
		 */
		public void calculateCommonRange() {

			DescriptiveStatistics stats = new DescriptiveStatistics();

			for (int v : centers) {
				stats.addValue(v);
			}

			chosenCenter = MathUtils.round(stats.getPercentile(50));

			if (intendedRange <= 0) {
				rangeMin = chosenCenter
						- MathUtils.round(chosenCenter - stats.getMin());
				rangeMax = rangeSize - MathUtils.round(stats.getMax());
			} else {
				rangeMin = intendedRange / 2;
				rangeMax = intendedRange / 2;
			}
		}

		/**
		 * Gets the boundary left.
		 * 
		 * @param center
		 *           the center
		 * @return the boundary left
		 */
		public int getBoundaryLeft(int center) {
			return center - rangeMin;
		}

		/**
		 * Gets the boundary right.
		 * 
		 * @param center
		 *           the center
		 * @return the boundary right
		 */
		public int getBoundaryRight(int center) {
			return center + rangeMax;
		}

	}

}
