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

import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * The Class SmouthFractionnedSpace.
 */
public class SmouthFractionnedSpace extends FractionnedSpace {

	

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		SmouthFractionnedSpace space = new SmouthFractionnedSpace(-5, -5, 10, 10, 10);
		
		long start = System.currentTimeMillis();
		
		
		/*
		
		Random rn = new Random();
		for (int i = 0; i != 2000; i++) {
			space.insertPoint(rn.nextDouble() * 10-5,
					rn.nextDouble() * 10-5,
					rn.nextDouble()+0.5);
		}
		*/
		
		
		space.insertPoint(-4, -4, 10);
		space.insertPoint(0,0,10);
		space.insertPoint(4,4,10);
		
		long end = System.currentTimeMillis();
		
		ImagePlus ip = new ImagePlus("",space.getImage());
		 
		//space.insertPoint(0.24, 0.24, 3);
		
		//System.out.println("Space populated in "+(end-start)+"ms");
		
		ip.show();
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.heatmap.FractionnedSpace#insertPoint(double, double, double)
	 */
	@Override
	public Fraction insertPoint(double x, double y, double value) {
		return insertPoint(new Point(x,y,value));
	}
	
	/**
	 * Instantiates a new smouth fractionned space.
	 *
	 * @param x the x
	 * @param y the y
	 * @param w the w
	 * @param h the h
	 * @param beadNumber the bead number
	 */
	public SmouthFractionnedSpace(double x, double y, double w, double h,
			int beadNumber) {
		
		int beadPerFieldOfView = 20;
		
		windowSize = 0;
		stepSize = 0;
		System.out.printf("Bead number = %d\nFOV : %.2f x %.2f µm\n",beadNumber,w,h);
		
		if(beadNumber != 0) {
		
			windowSize =  Math.sqrt((w * h) / beadNumber * beadPerFieldOfView);
			stepSize = windowSize / beadPerFieldOfView;// / steps;
			
		}
		
		if(windowSize != 0 && stepSize != 0) {
		
			
			System.out.println("windowSize = "+windowSize);
			System.out.println("StepSize = "+stepSize);
			// creating multiple fractions that will hold the beads inside
			for (double windowY = y ; windowY >= y - h + windowSize; windowY -= stepSize) {
				rows++;
				columns = 0;
				for (double windowX = x; windowX <= x + w
						- windowSize; windowX += stepSize) {
					columns++;
					add(new Fraction(windowX,windowY,windowSize,windowSize));
				}
			}
		}
		else {
			columns = 0;
			rows = 0;
			windowSize = 0;
			stepSize = 0;
		}
		System.out.println(rows);
		System.out.println(columns);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		this.steps = steps;
	}
	
	
	/* (non-Javadoc)
	 * @see knop.psfj.heatmap.FractionnedSpace#insertPoint(knop.psfj.heatmap.FractionnedSpace.Point)
	 */
	public Fraction insertPoint(Point p) {
		
		int NOT_FOUND = -10;
		
		int minC = NOT_FOUND;
		int minR = NOT_FOUND;
		
		int maxC = NOT_FOUND;
		int maxR = NOT_FOUND;
		
		int c = 0;
		int r = 0;
		boolean flag = false;
		for (double windowY = y ; windowY >  y-h + windowSize; windowY -= stepSize) {
			
			
			
			if(p.getY() <= windowY && p.getY() > windowY - windowSize) {
				if(flag == false) {
					flag = true;
					minR = r-1;
				}
			}
			else {
				
				if(flag == true) {
					maxR = r+1;
					break;
				}
			}
			r++;
		}
		
		if(flag==true && maxR == NOT_FOUND) maxR = rows;
		
		flag = false;
		
		for (double windowX = x ; windowX < x + w - windowSize; windowX += stepSize) {
			if(p.getX() >= windowX && p.getX() < windowX + windowSize) {
				if(flag == false) {
					flag = true;
					minC = c-1;
				}
			}
			else {
				if(flag == true) {
					maxC = c+1;
					break;
				}
			}
			c++;
		}
		
		if(flag == true && maxC == NOT_FOUND) {
			maxC = columns;
		}
		
		if(minR < 0) minR = 0;
		if(minC < 0) minC = 0;
		
		if(maxC > columns) maxC = columns;
		if(maxR > rows) maxR = rows;
		
		if(minC == NOT_FOUND || maxC == NOT_FOUND || minR == NOT_FOUND || maxR == NOT_FOUND) return null;
		
		/*
		System.out.println((p.getX()-(x-(w/2)))/stepSize);
		System.out.println("minC : "+minC);
		System.out.println("maxC : "+maxC);
		System.out.println("columns : "+columns);
		*/
		//System.out.println(String.format("minR : %d, minC : %d\nmaxR : %d, maxC : %d\ncolumns : %d, rows %d",minR,minC,maxR,maxC,rows,columns));
		
		for(c=minC;c!=maxC;c++) {
			for(r = minR;r!=maxR;r++) {
				Fraction f = getFraction(r,c);
				if(f.isIn(p)) f.add(p);
				//System.out.println(p);
				//System.out.println(f);
				//System.out.println(f.isIn(p));
			}
		}
		
		
		return null;
		
	}
	
	
	/**
	 * Insert point2.
	 *
	 * @param p the p
	 * @return the fraction
	 */
	public Fraction insertPoint2(Point p) {
		
		for(Fraction f : this) {
			if(f.isIn(p)) {
				
				f.add(p);
			}
		}
		
		return null;
		
	}
	

}
