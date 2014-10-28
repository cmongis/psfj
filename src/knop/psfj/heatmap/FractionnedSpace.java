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
package knop.psfj.heatmap;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Locale;

import knop.psfj.heatmap.FractionnedSpace.Fraction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// TODO: Auto-generated Javadoc
/**
 * The Class FractionnedSpace.
 */
public class FractionnedSpace extends ArrayList<Fraction> {

	/** The x. */
	double x;
	
	/** The y. */
	double y;
	
	/** The w. */
	double w;
	
	/** The h. */
	double h;

	/** The rows. */
	int rows;
	
	/** The columns. */
	int columns;

	/** The window size. */
	double windowSize;
	
	/** The step size. */
	double stepSize;
	
	/** The steps. */
	int steps;

	/** The min. */
	Double min;
	
	/** The max. */
	Double max;
	
	
	/**
	 * Instantiates a new fractionned space.
	 */
	public FractionnedSpace() {
		super();
	}
	
	/**
	 * Instantiates a new fractionned space.
	 *
	 * @param x the x
	 * @param y the y
	 * @param w the w
	 * @param h the h
	 * @param steps the steps
	 */
	public FractionnedSpace(double x, double y, double w, double h,
			int steps) {

		double stepSize = w / steps;
		this.stepSize = stepSize;
		// we move the windows on the X axis until we reach the end of the
		// frame

		for (double windowY = y; windowY < y - h + stepSize; windowY += stepSize) {
			rows++;
			columns = 0;
			for (double windowX = x; windowX < x + (w-stepSize); windowX += stepSize) {
				add(new Fraction(windowX, windowY, stepSize, stepSize));
				// by incrementing columns there, we count the number of
				// cell,
				// not the number of clumns.
				// the number of columns will be determined back;
				columns++;
			}
		}

		// the number of columns is deduced back
		//columns = columns / rows;

		System.out.println(String.format("Columns : %d, Rows : %d",
				columns, rows));

		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

		this.steps = steps;

	}

	/**
	 * Clear points.
	 */
	public void clearPoints() {
		// TODO Auto-generated method stub
		for(Fraction f : this) {
			f.clear();
		}
	}

	/**
	 * Gets the fraction.
	 *
	 * @param r the r
	 * @param c the c
	 * @return the fraction
	 */
	public Fraction getFraction(int r, int c) {
		return get(columns * r + c);
	}

	/**
	 * Insert point.
	 *
	 * @param x the x
	 * @param y the y
	 * @param value the value
	 * @return the fraction
	 */
	public Fraction insertPoint(double x, double y, double value) {
		return insertPoint(new Point(x, y, value));
	}

	// Inset a point to the corresponding fraction
	/**
	 * Insert point.
	 *
	 * @param p the p
	 * @return the fraction
	 */
	public Fraction insertPoint(Point p) {
		//System.out.println("Inserting "+p+"...");
		if (!isPointInRectangle(p, x, y, w, h)) {
			System.out.println(""+p+"is not not in the fractionned space.");
			return null;
		}

		// initialezed to -1 because one loop is always done
		int c = -1;
		int r = -1;

		// the corresponding fraction of the space is detected by making
		// counting
		// the number of step required to get to the X and Y coordinates

		// coordinates of a imaginary frame that goes along the X axis, and
		// then goes along the Y axis;
		double frameX = x;
		double frameY = y;

		// going though the x axis;
		while (frameX < p.getX()) {
			frameX += stepSize;
			c++;
		}

		// going through the Y axis
		while (frameY < p.getY()) {
			frameY += stepSize;
			r++;
		}
		getFraction(r, c).add(p);
		

		return getFraction(r, c);
	}

	/**
	 * Checks if is point in rectangle.
	 *
	 * @param p the p
	 * @param x the x
	 * @param y the y
	 * @param w the w
	 * @param h the h
	 * @return true, if is point in rectangle
	 */
	public boolean isPointInRectangle(Point p, double x, double y,
			double w, double h) {
		if (p.getX() < x || p.getX() > x + w) {
			return false;
		}
		if (p.getY() < y || p.getY() > y + h) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Gets the pixel value.
	 *
	 * @param f the f
	 * @return the pixel value
	 */
	public float getPixelValue(Fraction f) {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		
		
		float median = Float.NaN;
	
		//System.out.println(f);
		if(f.size() != 0) {
			
			for(Point p : f) {
				//System.out.println(p.getValue());
				if(stats == null) System.out.println(stats);
				if(p == null) continue;
				stats.addValue(p.getValue());
			}
			
			median = new Float(stats.getPercentile(50));
			//System.out.println("Resultat : "+median);
			
		}
		return median;
		
	}

	
	/**
	 * Gets the mean number of beads.
	 *
	 * @return the mean number of beads
	 */
	public double getMeanNumberOfBeads() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for(Fraction f : this) {
			stats.addValue(f.size());
		}
		return stats.getMean();
	}
	
	
	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public ImageProcessor getImage() {

		FloatProcessor ip;
				
				

		if(rows == 0 || columns ==0) {
			ip = new FloatProcessor(2,2);
			ip.setPixels(new float[] { 0,0,0,0 });
		}
		else {
			ip = new FloatProcessor(columns, rows);
			float[] pixels = new float[rows * columns];
			
			for (int i = 0; i != size(); i++) {
				double pixel = getPixelValue(get(i));
				
				
				pixels[i] = (float)pixel;
				//System.out.println(pixels[i]);
			}
	
			ip.setPixels(pixels);
	
			//new ImagePlus("", ip.resize(columns*10))..show();
		}
		return ip;

	}
	
	
	/**
	 * The Class Fraction.
	 */
	public class Fraction extends ArrayList<Point> {

		/** The x. */
		double x;
		
		/** The y. */
		double y;
		
		/** The w. */
		double w;
		
		/** The h. */
		double h;

		/**
		 * Instantiates a new fraction.
		 *
		 * @param x_ the x_
		 * @param y_ the y_
		 * @param w_ the w_
		 * @param h_ the h_
		 */
		Fraction(double x_, double y_, double w_, double h_) {
			super();

			setX(x_);
			setY(y_);
			setW(w_);
			setH(h_);

			
			
			
		}

		/**
		 * Checks if is in.
		 *
		 * @param p the p
		 * @return true, if is in
		 */
		public boolean isIn(Point p) {
			
			double pX = p.getX();
			double pY = p.getY();
			
			if( pX >= x && pX <= x+w && pY <= y && pY >= y-h) {
				return true;
			}
			else {
				return false;
			}
		}
		
		
		/**
		 * Gets the x.
		 *
		 * @return the x
		 */
		public double getX() {
			return x;
		}

		/**
		 * Sets the x.
		 *
		 * @param x the new x
		 */
		public void setX(double x) {
			this.x = x;
		}

		/**
		 * Gets the y.
		 *
		 * @return the y
		 */
		public double getY() {
			return y;
		}

		/**
		 * Sets the y.
		 *
		 * @param y the new y
		 */
		public void setY(double y) {
			this.y = y;
		}

		/**
		 * Gets the w.
		 *
		 * @return the w
		 */
		public double getW() {
			return w;
		}

		/**
		 * Sets the w.
		 *
		 * @param w the new w
		 */
		public void setW(double w) {
			this.w = w;
		}

		/**
		 * Gets the h.
		 *
		 * @return the h
		 */
		public double getH() {
			return h;
		}

		/**
		 * Sets the h.
		 *
		 * @param h the new h
		 */
		public void setH(double h) {
			this.h = h;
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#toString()
		 */
		public String toString() {
			String result = "";

			result += String.format(new Locale("en"),
					"[Fraction %.2f x %.2f (%.2f , %.2f) : %d]", w, h, x, y,
					size());
			return result;
		}

	}

	/**
	 * The Class Point.
	 */
	public class Point {

		/** The x. */
		double x;

		/** The y. */
		double y;
		
		/** The value. */
		double value;

		/**
		 * Instantiates a new point.
		 *
		 * @param x the x
		 * @param y the y
		 * @param value the value
		 */
		public Point(double x, double y, double value) {
			this.x = x;
			this.y = y;
			this.value = value;
		}

		/**
		 * Gets the x.
		 *
		 * @return the x
		 */
		public double getX() {

			return x;
		}

		/**
		 * Sets the x.
		 *
		 * @param x the new x
		 */
		public void setX(double x) {
			this.x = x;
		}

		/**
		 * Gets the y.
		 *
		 * @return the y
		 */
		public double getY() {
			return y;
		}

		/**
		 * Sets the y.
		 *
		 * @param y the new y
		 */
		public void setY(double y) {
			this.y = y;
		}

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public double getValue() {
			return value;
		}

		/**
		 * Sets the value.
		 *
		 * @param value the new value
		 */
		public void setValue(double value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return String.format("[Point (%f,%f) : %f]", x, y, value);
		}

	}
	
	
	
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return String.format("[Space : size(%.3fx%.3f), divisions(%dx%d), position(%.3f,%.3f)]", w,h,columns,rows,x,y);
	}
}




