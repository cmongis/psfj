/*
    IJBlob is a ImageJ library for extracting connected components in binary Images
    Copyright (C) 2012  Thorsten Wagner wagner@biomedical-imaging.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package ij.blob;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Does Connected Component Labeling 
 * @author Thorsten Wagner
 */
public class ConnectedComponentLabeler {
	
	private ImagePlus imp;
	private ImageProcessor labledImage;
	private int NOLABEL = 0;
	private int labelCount = 1;
	private int BACKGROUND = 255;
	private int OBJECT = 0;
	private ManyBlobs allBlobs;
	/*
	 * Die Reihenfolge, wie das 3x3 Fester um den Punkt p durchlaufen wird.
	 * Fenster:
	 * 5 * 6 * 7 
	 * 4 * p * 0 
	 * 3 * 2 * 1 
	 */
	int iterationorder[] = { 5, 4, 3, 6, 2, 7, 0, 1 };
	
	/**
	 * @param allBlobs A ManyBlobs Object where the Blobs has to be stored
	 * @param imp The image 
	 */
	public ConnectedComponentLabeler(ManyBlobs allBlobs, ImagePlus imp, int BACKGROUND, int OBJECT) {
		this.allBlobs = allBlobs;
		this.imp = imp;
		this.BACKGROUND = BACKGROUND;
		this.OBJECT = OBJECT;

		addWhiteBorder(imp);

		labledImage = new ColorProcessor(this.imp.getWidth(), this.imp.getHeight());
		
	}
	
	/**
	 * Start the Connected Component Algorithm
	 * @see  ﻿F. Chang, “A linear-time component-labeling algorithm using contour tracing technique,” Computer Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
	 */
	public void doConnectedComponents() {
		
		ImageProcessor ip = imp.getProcessor();

		ByteProcessor proc = (ByteProcessor) ip;
		byte[] pixels = (byte[]) proc.getPixels();
		int w = proc.getWidth();
		Rectangle roi = ip.getRoi();
		double value;
		
		System.out.println("do connected components");
		
		for (int i = roi.y; i < roi.y + roi.height; ++i) {
			int offset = i * w;
			for (int j = roi.x; j < roi.x + roi.width; ++j) {
				value = pixels[offset + j] & 255;
				if (value == OBJECT) {
					if (isNewExternalContour(j, i, proc) && hasNoLabel(j, i)) {
						
						labledImage.set(j, i, labelCount);
						Polygon outerContour = traceContour(j, i, proc,
								labelCount, 1);
						
						allBlobs.add(new Blob(outerContour, labelCount));
						++labelCount;

					} else if (isNewInternalContour(j, i, proc)) {
				
						int label = labledImage.get(j, i);
						if (hasNoLabel(j, i)) {
							label = labledImage.get(j - 1, i);
							labledImage.set(j, i, label);

						}
						Polygon innerContour = traceContour(j, i, proc, label,
								2);

						getBlobByLabel(label).addInnerContour(innerContour);

					} else if (hasNoLabel(j, i)) {
					
						int precedinglabel = labledImage.get(j - 1, i);
						labledImage.set(j, i, precedinglabel);
					}

				}
			}
		}
                System.out.println("Connected component finished.");
		// printImage(labledImage);
	}
	

	private Polygon traceContour(int x, int y, ByteProcessor proc, int label,
			int start) {

		Polygon contour = new Polygon();
		Point startPoint = new Point(x, y);
		contour.addPoint(x, y);

		Point nextPoint = nextPointOnContour(startPoint, proc, start);
		Point T = nextPointOnContour(startPoint, proc, start);
		if (nextPoint.x == -1) {
			// Point is isolated;
			return contour;
		}

		boolean equalsStartpoint = false;
		do {
			contour.addPoint(nextPoint.x, nextPoint.y);
			labledImage.set(nextPoint.x, nextPoint.y, label);
			equalsStartpoint = nextPoint.equals(startPoint);
			nextPoint = nextPointOnContour(nextPoint, proc, -1);
		} while (!equalsStartpoint || !nextPoint.equals(T));

		return contour;
	}

	Point prevContourPoint;

	// start = 1 -> External Contour
	// start = 2 -> Internal Contour
	private Point nextPointOnContour(Point startPoint, ByteProcessor proc,
			int start) {

		/*
		 ************
		 *5 * 6 * 7 * 
		 *4 * p * 0 * 
		 *3 * 2 * 1 * 
		 ************
		 */
		Point[] helpindexToPoint = new Point[8];

		int[] neighbors = new int[8]; // neighbors of p
		int x = startPoint.x;
		int y = startPoint.y;

		int I = 2;
		int k = I - 1;

		int u = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int window_x = (x - k + i);
				int window_y = (y - k + j);
				if (window_x != x || window_y != y) {
					neighbors[iterationorder[u]] = proc.get(window_x, window_y);
					helpindexToPoint[iterationorder[u]] = new Point(window_x,
							window_y);
					u++;
				}
			}
		}
		ArrayList<Point> indexToPoint = new ArrayList<Point>(
				Arrays.asList(helpindexToPoint));

		final int NOSTARTPOINT = -1;
		final int STARTEXTERNALCONTOUR = 1;
		final int STARTINTERNALCONTOUR = 2;

		switch (start) {
		case NOSTARTPOINT:
			int prevContourPointIndex = indexToPoint.indexOf(prevContourPoint);
			start = (prevContourPointIndex + 2) % 8;
			break;
		case STARTEXTERNALCONTOUR:
			start = 7;
			break;
		case STARTINTERNALCONTOUR:
			start = 3;
			break;
		}

		int counter = start;
		int pos = -2;

		while (pos != start) {
			pos = counter % 8;
			if (neighbors[pos] == OBJECT) {
				prevContourPoint = startPoint;
				return indexToPoint.get(pos);
			}
			Point p = indexToPoint.get(pos);
			if (neighbors[pos] == BACKGROUND) {
				try {
					labledImage.set(p.x, p.y, -1);
				} catch (Exception e) {
					IJ.log("x " + p.x + " y " + p.y);
				}
			}

			counter++;
			pos = counter % 8;
		}
		Point isIsolated = new Point(-1, -1);

		return isIsolated;
	}

	private boolean isNewExternalContour(int x, int y, ByteProcessor proc) {
		return isBackground(x, y - 1, proc);
	}
	
	private boolean hasNoLabel(int x, int y) {
		int label = labledImage.get(x, y);
		return label == NOLABEL;
	}

	private boolean isMarked(int x, int y) {
		return labledImage.get(x, y) == -1;
	}

	private boolean isBackground(int x, int y, ByteProcessor proc) {
		return (proc.get(x, y) == BACKGROUND);
	}

	private boolean isNewInternalContour(int x, int y, ByteProcessor proc) {
		return isBackground(x, y + 1, proc) && !isMarked(x, y + 1);
	}
	
	private Blob getBlobByLabel(int label) {
		for (int i = 0; i < allBlobs.size(); i++) {
			if (allBlobs.get(i).getLabel() == label) {
				return allBlobs.get(i);
			}
		}
		return null;
	}
	
	private void addWhiteBorder(ImagePlus img) {
		boolean hasWhiteBorder = true;
		ImageProcessor oldip = img.getProcessor();
		ByteProcessor oldproc = (ByteProcessor) oldip;
		byte[] pixels = (byte[]) oldproc.getPixels();
		int w = oldproc.getWidth();
		for (int i = 0; i < oldproc.getHeight(); i++) {
			
			int offset = i * w;
			//Erste und letzte Scanrow
			if (i == 0 || i == w - 1) {

				for (int j = 0; j < oldproc.getWidth(); j++) {
					int value = pixels[offset + j] & 255;
					if (value == OBJECT) {
						hasWhiteBorder = false;
					}
				}
			}
			// Erster und letzter Pixel pro Scanrow
			int firstvalue = pixels[offset + 0] & 255;
			int lastvalue = pixels[offset + oldproc.getWidth() - 1] & 255;
			if (firstvalue == OBJECT || lastvalue == OBJECT) {
				hasWhiteBorder = false;
			}

			if (!hasWhiteBorder) {
				i = oldproc.getHeight(); // Stop searching
			}
		}

		if (!hasWhiteBorder) {
			int fill = NewImage.FILL_WHITE;
			if(BACKGROUND==0){
				fill = NewImage.FILL_BLACK;
			}
			imp = NewImage.createByteImage("", img.getWidth() + 2,
					img.getHeight() + 2, 1, fill);
			ImageProcessor ip = imp.getProcessor();

			ByteProcessor newproc = (ByteProcessor) ip;

			byte[] newpixels = (byte[]) newproc.getPixels();

			// double value;
			for (int i = 0; i < oldip.getHeight(); ++i) {
				int offset = i * w;
				for (int j = 0; j < oldip.getWidth(); ++j) {
					newpixels[(i + 1) * (w + 2) + j + 1] = pixels[offset + j];
				}
			}
		} else
		{
			imp = img;
		}
	}

}
