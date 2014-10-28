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
package knop.psfj.view;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;

import knop.psfj.resolution.DataTricks;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageCanvasPlus.
 */
public class ImageCanvasPlus extends ImageCanvas {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame();

		ImageCanvasPlus icp = new ImageCanvasPlus(IJ.openImage("/home/cyril/test.jpg"));
		
		ImagePlus ip = IJ.openImage("/home/cyril/brace.jpeg");
		
		frame.add(icp);
		frame.pack();
		frame.show();
		
		icp.setImage(ip);
		
		
	}

	
	
	/** The drawing width. */
	int drawingWidth = 400;
	
	/** The drawing height. */
	int drawingHeight = 400;
	
	
	/** The zoom level. */
	double zoomLevel = 1.0;
	
	/** The original imp. */
	ImagePlus originalImp;
	
	
	
	/**
	 * Instantiates a new image canvas plus.
	 *
	 * @param imp the imp
	 */
	public ImageCanvasPlus(ImagePlus imp) {
		super(imp);
		originalImp = imp;
		addMouseListener(listener);
		// addMouseMotionListener(motionListener);
		setDrawingSize(drawingWidth, drawingHeight);
		// zoomIn(700,700);
		scroll(700, 700);
		addMouseWheelListener(wheelListener);
		setSourceRect(new Rectangle(0, drawingHeight, getWidth(), getHeight()));
		onMouseDown.start();
		
		
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Sets the image.
	 *
	 * @param imp the new image
	 */
	public void setImage(ImagePlus imp) {
		setImage(imp.getProcessor());
	}
	
	/**
	 * Sets the image.
	 *
	 * @param ip the new image
	 */
	public void setImage(ImageProcessor ip) {
		getImage().setProcessor(ip);
		//setImageUpdated();
		
		if(imageWidth != ip.getWidth() || imageHeight != ip.getHeight()) {
		
			imageWidth = ip.getWidth();
			imageHeight = ip.getHeight();
			setMagnification(1.0);
			setImageUpdated();
			setSourceRect(new Rectangle(0,0,drawingWidth,drawingHeight));
		}
		
		
		
	
		
		repaint();
	}
	
	/**
	 * Round.
	 *
	 * @param d the d
	 * @return the int
	 */
	public int round(double d) {
		return DataTricks.round(d);
	}
	
	/**
	 * Zoom in.
	 */
	public void zoomIn() {
		Rectangle r = getSrcRect();
		int centerX = DataTricks.round(r.getX());
		int centerY = DataTricks.round(r.getY());
		
		setMagnification(getMagnification()*1.2);
	
		
		adjustSourceRect(getMagnification(), centerX, centerY);
		
		//r.resize(round(getWidth()/2), round(r.getHeight()/2));
		
		//System.out.println(r.getWidth());
		//ImageProcessor newIp = originalImp.getProcessor().duplicate();
		
		
		/*
		int newWidth = dataTricks.round(newIp.getWidth()*zoomLevel);
		
		newIp = newIp.resize(newWidth);
		System.out.println(newWidth);
		
		//getImage().setProcessor(newIp);
		
		setImageUpdated();
		repaint();
		*/
		//zoomIn(centerX,centerY);
		repaint();
		
		
		
	}
	
	/**
	 * Adjust source rect.
	 *
	 * @param newMag the new mag
	 * @param x the x
	 * @param y the y
	 */
	protected void adjustSourceRect(double newMag, int x, int y) {
        //IJ.log("adjustSourceRect1: "+newMag+" "+dstWidth+"  "+dstHeight);
        int w = (int)Math.round(dstWidth/newMag);
        if (w*newMag<dstWidth) w++;
        int h = (int)Math.round(dstHeight/newMag);
        if (h*newMag<dstHeight) h++;
        x = offScreenX(x);
        y = offScreenY(y);
        Rectangle r = new Rectangle(x-w/2, y-h/2, w, h);
        if (r.x<0) r.x = 0;
        if (r.y<0) r.y = 0;
        if (r.x+w>imageWidth) r.x = imageWidth-w;
        if (r.y+h>imageHeight) r.y = imageHeight-h;
        srcRect = r;
        setMagnification(newMag);
        //IJ.log("adjustSourceRect2: "+srcRect+" "+dstWidth+"  "+dstHeight);
    }
	
	/**
	 * Zoom out.
	 */
	public void zoomOut() {
		Rectangle r = getSrcRect();
		setMagnification(getMagnification()*0.8);
		
		int width = round(r.getWidth());
		int height = round(r.getHeight());
		
		System.out.println(r);
		
		
		if(width > 500 || height > 500) {
			setDrawingSize(width, height);
		}
		else {
			setDrawingSize(drawingWidth,drawingHeight);
		}
		
		adjustSourceRect(getMagnification(), round(r.getX()), round(r.getY()));
		
		
		
		
		repaint();
	}
	
	/** The on mouse down. */
	Thread onMouseDown = new Thread() {
		
		Rectangle origin;
		Point originPoint;
		public void run() {
			
			
			
			
			while (true) {

				synchronized (mouseDown) {
					
					
					
					if (mouseDown) {
						
						//the point of origin is null when the mouse is pressed (normaly)
						if(this.origin == null) {
							this.origin = getSrcRect();
							this.originPoint = getMousePosition();
						}
						Point p = getMousePosition();
						if(p == null) continue;
						int dx = DataTricks.round(p.getX()-originPoint.getX());
						int dy = DataTricks.round(p.getY()-originPoint.getY());
						
						
						
						int displayedWidth = round(imageWidth*(getMagnification()));
						int displayedHeight = round(imageHeight*(getMagnification()));
						
						
						
						System.out.println(displayedWidth);
						
						int newX = DataTricks.round(origin.getX()-dx);
						int newY = DataTricks.round(origin.getY()-dy);
						
						
						if(magnification > 1.0) {
						
						
						if(newX < 0) newX = 0;
						if(newY < 0) newY = 0;
						
						int limitX = round(displayedWidth-getWidth());
						int limitY = round(displayedHeight-getHeight());
						
						if(newX > limitX)
							newX = limitX;
						if(newY > limitY) 
							newY = limitY;
						
						}
						
						System.out.println(String.format("(%d,%d)",newX,newY));
						
						//if(newX > 0 && newX < getWidth() && newY > 0 && newY < getHeight()) 
						
						point(newX,newY);
						

					} else {
						
						this.origin = null;
						this.originPoint = null;
						synchronized (lock) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	};

	/** The mouse down. */
	Boolean mouseDown = false;
	
	/** The lock. */
	Object lock = new Object();

	/**
	 * Point.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public void point(int x, int y) {
		Rectangle r = getSrcRect();
		int width = round(r.getWidth());
		int height = round(r.getHeight());
		
		
		
		
		setSourceRect(new Rectangle(x, y, width, height));
		

		
		
		repaint();
	}

	
	
	/** The wheel listener. */
	MouseWheelListener wheelListener = new MouseWheelListener() {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if(e.getWheelRotation() < 0) {
				zoomIn();
			}
			else {
				zoomOut();
			}
		}
	};
	
	
	/** The listener. */
	MouseListener listener = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

			mouseDown = false;
			System.out.println("Mouse Up !!!");
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			System.out.println("Mouse Down !");
			mouseDown = true;

			
			//setImage(IJ.openImage("/home/cyril/brace.jpeg"));
			
			synchronized (lock) {
				lock.notify();
			}

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			setCursor(defaultCursor);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			setCursor(handCursor);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	};

}
