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
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.PolygonFiller;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;


public class Blob {
	private int gray_background = 255;
	private int gray_object = 0;
	
	public final static int DRAW_HOLES = 1;
	public final static int DRAW_CONVEX_HULL = 2;
	public final static int DRAW_LABEL = 4;
	
	private Polygon outerContour;
	private ArrayList<Polygon> innerContours; //Holes
	private int label;
	
	//Features
	private Point  centerOfGrafity = null;
	private double perimeter = -1;
	private double perimeterConvexHull = -1;
	private double enclosedArea = -1;

	private double circularity = -1;
	private double thinnesRatio = -1;
	private double areaToPerimeterRatio = -1;
	private double temperature = -1;
	private double fractalBoxDimension = -1;
	private double fractalDimensionGoodness = -1;
	private double[][] centralMomentsLUT = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
	private double[][] momentsLUT = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
    

	public Blob(Polygon outerContour, int label) {
		this.outerContour = outerContour;
		this.label = label;
		innerContours = new ArrayList<Polygon>();
		
	}
	
	/**
	 * Draws the Blob with or without its holes.
	 * @param ip The ImageProcesser in which the blob has to be drawn.
	 * @param options Drawing Options are DRAW_HOLES, DRAW_CONVEX_HULL, DRAW_LABEL. Combinations with | are possible.
	 */
	public void draw(ImageProcessor ip, int options){
		ip.setColor(Color.BLACK);
		fillPolygon(ip, outerContour, gray_object);
		
		if((options&DRAW_HOLES)>0){
			for(int i = 0; i < innerContours.size(); i++) {
				ip.setColor(Color.WHITE);
				fillPolygon(ip, innerContours.get(i), gray_background);
			}
		}
		
		if((options&DRAW_CONVEX_HULL)>0){
			ip.setColor(Color.RED);
			ip.drawPolygon(getConvexHull());
		}
		
		if((options&DRAW_LABEL)>0){
			Point cog = getCenterOfGravity();
			ip.setColor(Color.MAGENTA);
			ip.drawString(""+getLabel(), cog.x, cog.y);
		}
	}
	
	/**
	 * Draws the Blob with its holes.
	 * @param ip The ImageProcesser in which the blob has to be drawn.
	 */
	public void draw(ImageProcessor ip){
		draw(ip,DRAW_HOLES);
	}
		
	/**
	 * Return the geometric center of gravity of the blob. It
	 * is calculated by the outer contour without consider possible
	 * holes.
	 * @return Geometric center of gravity of the blob
	 */
	public Point getCenterOfGravity() {
		if(centerOfGrafity != null){
			return centerOfGrafity;
		}
		centerOfGrafity = new Point();
	    
	    int[] x = outerContour.xpoints;
	    int[] y = outerContour.ypoints;
	    int sumx = 0;
	    int sumy = 0;
	    for(int i = 0; i < outerContour.npoints-1; i++){
	    	int cross = (x[i]*y[i+1]-x[i+1]*y[i]);
	    	sumx = sumx + (x[i]+x[i+1])*cross;
	    	sumy = sumy + (y[i]+y[i+1])*cross;
	    }
	    centerOfGrafity.x = (int)(sumx/(6*getEnclosedArea()));
	    centerOfGrafity.y = (int)(sumy/(6*getEnclosedArea()));
		return centerOfGrafity;
		
	}
	/**
	 * Region-Based Moments Definition of "Gorman et. al. Practical Algorithms for Image Analysis" (p. 157).
	 * (computational expensive!)
	 * @return Region-Based Moments of order (p + q)
	 * @param q (order = (p + q))
	 * @param p (order = (p + q))
	 */
	public double getMoment(int p, int q) {
		 if(p<=2 && q<=2){
			 if(momentsLUT[p][q]!=-1){
				 return momentsLUT[p][q];
			 }
		 }
		 int moment = 0;
		 Rectangle bounds = outerContour.getBounds();
		 for(int x = bounds.x; x < bounds.x+bounds.width;x++){
			 for(int y = bounds.y; y < bounds.y+bounds.height;y++){
				 if(outerContour.contains(x, y)){
					 moment += Math.pow(x, p) * Math.pow(y, q);
				 }
			 } 
		 }
		 momentsLUT[p][q] = moment;
		 return moment;
	}
	
	/**
	 * Central Moments Definition of "Gorman et. al. Practical Algorithms for Image Analysis" (p. 158).
	 * (computational expensive!)
	 * @return Central Moment of Order (p + q)
	 * @param q (order = (p + q))
	 * @param p (order = (p + q))
	 */
	public double getCentralMoments(int p, int q){
		
		 if(p<=2 && q<=2){
			 if(centralMomentsLUT[p][q]!=-1){
				 return centralMomentsLUT[p][q];
			 }
		 }
		
		double centralMoment = 0;
		double m00 = getMoment(0,0);
		double xc = getMoment(1,0)/m00; //Centroid x
		double yc = getMoment(0,1)/m00; //Centroid y
		/*
		if(p==0 && q == 0){
			centralMoment = m00;
		}
		else if(( p==0 && q==1) || ( p==1 && q==0)){
			centralMoment = 0;
		}
		else if(p==1 && q == 1) {
			centralMoment = (getMoment(1, 1)-getMoment(0,1)*(getMoment(1, 0)/getMoment(0,0)));
		}
		else if(p==2 && q==0){
			centralMoment = (getMoment(2, 0) - getMoment(1, 0)*(getMoment(1, 0)/getMoment(0,0)));
		}
		else if(p==0 && q==2){
			centralMoment = (getMoment(0, 2) - getMoment(0, 1)*(getMoment(0, 1)/getMoment(0,0)));
		}
		else
		{
		
			IJ.log("HIER");
			*/
			Rectangle bounds = outerContour.getBounds();
			for(int x = bounds.x; x < bounds.x+bounds.width;x++){
				for(int y = bounds.y; y < bounds.y+bounds.height;y++){
					if(outerContour.contains(x, y)){
						centralMoment += Math.pow(x-xc, p) * Math.pow(y-yc, q);
					}
				} 
			}
			/*
		}
		
		IJ.log(""+p+","+q+": " + centralMoment);
		*/
		centralMomentsLUT[p][q] = centralMoment;
		return centralMoment;
	}
	
	private double getOrientation(){
		double c00 = getCentralMoments(0, 0);
		double c20 = getCentralMoments(2,0)/c00;
		double c02 = getCentralMoments(0,2)/c00;
		double c11 = getCentralMoments(1,1)/c00;
		if(c11==0){
			IJ.log("First central order moment ist zero. No orientation is calculated. 0 is returned by default");
		}
	
		double tanalpha = 2.0*c11/(c20-c02);
		return -0.5*Math.atan(tanalpha)*(360.0/(2*Math.PI));
	}
	
	/**
	 * @return The Orientation of the Major Axis from the Blob in grad (measured counter clockwise from the positive x axis).
	 */
	public double getOrientationMajorAxis(){
		return getOrientation();
	}
	
	/**
	 * @return The Orientation of the Major Axis from the Blob in grad (measured counter clockwise from the positive x axis).
	 */
	public double getOrientationMinorAxis(){
		return getOrientation()-90;
	}
	
	private double getEigenvalue(boolean major) {
		double c00 = getCentralMoments(0, 0);
		double c20 = getCentralMoments(2,0)/c00;
		double c02 = getCentralMoments(0,2)/c00;
		double c11 = getCentralMoments(1,1)/c00;
		
		int sign = 1;
		if(!major){
			sign = -1;
		}
		double value = 0.5*(c20+c02)+sign*0.5*Math.sqrt(4*Math.pow(c11, 2)+Math.pow(c20-c02, 2));
		return value;
	}
	
	/**
	 * @return Return the Eigenvalue from the major axis (computational expensive!)
	 */
	public double getEigenvalueMajorAxis() {
		return getEigenvalue(true);
	}
	
	/**
	 * @return Return the Eigenvalue from the minor axis (computational expensive!)
	 */
	public double getEigenvalueMinorAxis() {
		return getEigenvalue(false);
	}
	
	/**
	 * @return The Elongation of the Blob based on its eigenvalues (computational expensive!)
	 */
	public double getElongation() {
		return Math.sqrt(1-getEigenvalueMinorAxis()/getEigenvalueMajorAxis());
	}
	
	/**
	 * Calculates the first k Fourier Descriptor
	 * @param k	Highest Fourier Descriptor
	 */
	private double[] getFirstKFourierDescriptors(int k) {
	
		/*
		 * a[2*k] = Re[k], 
		 * a[2*k+1] = Im[k], 0<=k<n
		 */
		double[] contourSignal = new double[2*outerContour.npoints];
	
		int j = 0;
		for(int i = 0; i < outerContour.npoints; i++) {
			contourSignal[j] = outerContour.xpoints[i];
			contourSignal[j+1] = outerContour.ypoints[i];
			j=j+2;
		}
		DoubleFFT_1D ft = new DoubleFFT_1D(outerContour.npoints);
		ft.complexForward(contourSignal);
	
		for(int i = k+1; i < contourSignal.length; i++){
				contourSignal[i] = 0;
		}
		/*
		ft.complexInverse(contourSignal, false);
		int[] xpoints = new int[contourSignal.length/2];
		int[] ypoints = new int[contourSignal.length/2];
		
		j=0;
		for(int i = 0; i < contourSignal.length; i=i+2) {
			xpoints[j] = (int)( (1.0/outerContour.npoints)* contourSignal[i]);
			ypoints[j] = (int)((1.0/outerContour.npoints) * contourSignal[i+1]);
			j++;
		}
		*/
		
		return contourSignal;
	}
	
	private void fillPolygon(ImageProcessor ip, Polygon p, int fillValue) {
		PolygonRoi proi = new PolygonRoi(p, PolygonRoi.FREEROI);
		Rectangle r = proi.getBounds();
		PolygonFiller pf = new PolygonFiller();
		pf.setPolygon(proi.getXCoordinates(), proi.getYCoordinates(), proi.getNCoordinates());
		//ip.setValue(fillValue);
		ip.setRoi(r);
		ImageProcessor objectMask = pf.getMask(r.width, r.height);
		ip.fill(objectMask);
	}
	
	/**
	 * @return The outer contour of an object
	 */
	public Polygon getOuterContour() {
		return outerContour;
	}
	
	/**
	 * @return Arraylist of the inner contours.
	 */
	public ArrayList<Polygon> getInnerContours() {
		return innerContours;
	}
	
	/**
	 * Adds an inner contour (hole) to blob.
	 * @param contour Contour of the hole.
	 */
	public void addInnerContour(Polygon contour) {
		innerContours.add(contour);
	}


	public int getLabel() {
		return label;
	}
	
	/**
	 * @return The perimeter of the outer contour.
	 */
	public double getPerimeter() {
		if(perimeter!=-1){
			return perimeter;
		}
		PolygonRoi roi = new PolygonRoi(outerContour, Roi.FREEROI);
		perimeter = roi.getLength();
		return perimeter;
	}
	
	/**
	 * @return The perimeter of the convex hull
	 */
	public double getPerimeterConvexHull() {
		if(perimeterConvexHull!=-1){
			return perimeterConvexHull;
		}
		PolygonRoi convexRoi = null;
		
		Polygon hull = getConvexHull();
		perimeterConvexHull = 0;
		try {
		convexRoi = new PolygonRoi(hull, Roi.POLYGON);
		perimeterConvexHull = convexRoi.getLength();
		}catch(Exception e){
			perimeterConvexHull = getPerimeter();
			IJ.log("Blob ID: "+ getLabel() +" Error calculating the perimeter of the convex hull. Returning the regular perimeter");
		}
		
		
		return perimeterConvexHull;
	}
	
	/**
	 * Returns the convex hull of the blob.
	 * @return The convex hull as polygon
	 */
	public Polygon getConvexHull() {
		PolygonRoi roi = new PolygonRoi(outerContour, Roi.POLYGON);
		Polygon hull = roi.getConvexHull();
		if(hull==null){
			return getOuterContour();
		}
		return hull;
	}
	
	/**
	 * @return The enclosed area of the outer contour (without substracting the holes).
	 */
	public double getEnclosedArea() {
		if(enclosedArea!=-1){
			return enclosedArea;
		}
		//Gaußsche Trapezformel
		int summe = 0;
		int[] xpoints = outerContour.xpoints;
		int[] ypoints = outerContour.ypoints;
		for(int i = 0; i < outerContour.npoints-1; i++){
			summe = summe + Math.abs(ypoints[i]+ypoints[i+1])*(xpoints[i]-xpoints[i+1]);
		}
		enclosedArea = summe/2;
		return enclosedArea;
	}
	
	/**
	 * Calculates the circularity of the outer contour: (perimeter*perimeter) / (enclosed area)
	 * @return (perimeter*perimeter) / (enclosed area)
	 */
	public double getCircularity() {
		if(circularity!=-1){
			return circularity;
		}
		double perimeter = getPerimeter();
		double size = getEnclosedArea();
		circularity = (perimeter*perimeter) / size;
		return circularity;
	}
	
	/**
	 * @return Thinnes Ratio defined as: (4*Math.PI)/Circularity
	 */
	public double getThinnesRatio() {
		if(thinnesRatio!=-1){
			return thinnesRatio;
		}
		thinnesRatio = (4*Math.PI)/getCircularity();
		thinnesRatio = (thinnesRatio>1)?1:thinnesRatio;
		return thinnesRatio;
	}
	/**
	 * @return Area to perimeter ratio
	 */
	public double getAreaToPerimeterRatio() {
		if(areaToPerimeterRatio != -1){
			return areaToPerimeterRatio;
		}
		areaToPerimeterRatio = getEnclosedArea()/getPerimeter();
		return areaToPerimeterRatio;
	}
	/**
	 * @return Contour Temperatur (normed). It has a strong relationship to the fractal dimension.
	 * @see Datails in Luciano da Fontoura Costa, Roberto Marcondes Cesar,
	 * Jr.Shape Classification and Analysis: Theory and Practice, Second Edition, 2009, CRC Press 
	 */
	public double getContourTemperature() {
		if(temperature!=-1){
			return temperature;
		}
		double chp = getPerimeterConvexHull();
		double peri = getPerimeter();
		temperature = 1/(Math.log((2*peri)/(Math.abs(peri-chp)))/Math.log(2));
		return temperature;
	}
	/**
	 * @return Calculates the fractal box dimension of the blob.
	 * @param boxSizes ordered array of Box-Sizes
	 */
	public double getFractalBoxDimension(int[] boxSizes) {
		if(fractalBoxDimension !=-1){
			return fractalBoxDimension;
		}
		FractalBoxCounterBlob boxcounter = new FractalBoxCounterBlob();
		boxcounter.setBoxSizes(boxSizes);
		double[] FDandGOF = boxcounter.getFractcalDimension(this);
		fractalBoxDimension = FDandGOF[0];
		fractalDimensionGoodness = FDandGOF[1];
		return fractalBoxDimension;
	}
	
	/**
	 * @return The fractal box dimension of the blob.
	 */
	public double getFractalBoxDimension() {
		if(fractalBoxDimension !=-1){
			return fractalBoxDimension;
		}
		FractalBoxCounterBlob boxcounter = new FractalBoxCounterBlob();
		double[] FDandGOF  = boxcounter.getFractcalDimension(this);
		fractalBoxDimension = FDandGOF[0];
		fractalDimensionGoodness = FDandGOF[1];
		return fractalBoxDimension;
	}
	
	/**
	 * @return The goodness of the "best fit" line of the fractal box dimension estimation.
	 */
	public double getFractalDimensionGoodness(){
		return fractalDimensionGoodness;
	}
	/**
	 * @return The number of inner contours (Holes) of a blob.
	 */
	public int getNumberofHoles() {
		return innerContours.size();
	}
}
