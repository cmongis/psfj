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
import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.util.ArrayList;

/*
 * This library extracts connected components . For this purpose it uses the
 * following algorithm : ﻿F. Chang, “A linear-time
 * component-labeling algorithm using contour tracing technique,” Computer
 * Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
 */

public class ManyBlobs extends ArrayList<Blob> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImagePlus imp;
	private int BACKGROUND = 255;
	private int OBJECT = 0;

	
	/**
	 * @param imp Binary Image
	 */
	public ManyBlobs(ImagePlus imp) {
		setImage(imp);
	}
	
	private void setImage(ImagePlus imp) {
		this.imp = imp;
		ImageStatistics stats = imp.getStatistics();
		
		System.out.println("stats.histogram[0] : " + stats.histogram[0]);
		System.out.println("stats.histogram[255] : " + stats.histogram[255]);
		System.out.println("stats.pixelCount : " + stats.pixelCount);
		
		if ((stats.histogram[0] + stats.histogram[255]) != stats.pixelCount) {
			throw new java.lang.IllegalArgumentException("Not a binary image");
		}
		
		if(imp.isInvertedLut()){
			BACKGROUND = 0;
			OBJECT = 255;
		}
	}
	
	
	
	/**
	 * Start the Connected Component Algorithm
	 * @see  ﻿F. Chang, “A linear-time component-labeling algorithm using contour tracing technique,” Computer Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
	 */
	public void findConnectedComponents() {
		ConnectedComponentLabeler labeler = new ConnectedComponentLabeler(this,imp,BACKGROUND,OBJECT);
		labeler.doConnectedComponents();

	}


}