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
import ij.process.ImageProcessor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class KnopImageCanvas.
 */
public class KnopImageCanvas extends Canvas {

	/** The imp. */
	protected ImagePlus imp;

	/**
	 * The main method.
	 * 
	 * @param args
	 *           the arguments
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(800, 800);
		KnopImageCanvas icp = new KnopImageCanvas(1000, 1000);

		KnopImageCanvas icp2 = new KnopImageCanvas(800, 800);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		// panel.add(icp);
		panel.add(icp2);

		icp2.doOpening = false;
		ImagePlus ip = IJ.openImage("/home/cyril/test_img/trunk.jpg");
		ImagePlus ip2 = IJ.openImage("/home/cyril/test_img/trunk.jpg");

		icp.setImage(ip);
		icp2.setImage(ip2);

		frame.add(panel);
		frame.pack();

		frame.show();
		icp2.fitImageToCanvas();

	}

	/** The canvas width. */
	int canvasWidth;

	/** The canvas height. */
	int canvasHeight;

	/** The image width. */
	int imageWidth;

	/** The image height. */
	int imageHeight;

	/** The last pixel. */
	double lastPixel;

	/** The selection rectangle. */
	Rectangle selectionRectangle;

	/** The selection color. */
	Color selectionColor = Color.green;

	/** The last selection. */
	Rectangle lastSelection;

	/** The last pixel y. */
	public int lastPixelY;

	/** The last pixel x. */
	public int lastPixelX;

	/** The magnification. */
	double magnification = -1.0;

	/** The last_click. */
	long last_click;

	/** The mode. */
	int mode = SELECTION_MODE;

	/** The fit to smaller dimension. */
	public static int FIT_TO_SMALLER_DIMENSION = 0;

	/** The fit to width. */
	public static int FIT_TO_WIDTH = 1;

	/** The mouse down. */
	Boolean mouseDown = false;

	/** The lock. */
	Boolean lock = new Boolean(false);

	/** The origin. */
	Point origin;

	/** The update lock. */
	Boolean updateLock = new Boolean(false);

	/** The x. */
	int x = 0;

	/** The y. */
	int y = 0;

	/** The zoom speed. */
	double zoomSpeed = 0.1;

	/** The reset on image change. */
	private boolean resetOnImageChange = false;

	/** The fit to canvas. */
	private boolean fitToCanvas = true;

	/** The fitting mode. */
	private int fittingMode = FIT_TO_SMALLER_DIMENSION;

	/** The x cam. */
	double xCam = 0.0;

	/** The y cam. */
	double yCam = 0.0;

	/** The do opening. */
	boolean doOpening = true;

	/** The selection mode. */
	public static int SELECTION_MODE = 0;

	/** The move mode. */
	public static int MOVE_MODE = 1;

	/** The mouse thread. */
	Executor mouseThread = Executors.newFixedThreadPool(1);

	/** The offscreen. */
	Image offscreen;

	/** The graphics2d. */
	Graphics2D graphics2d;

	/**
	 * Instantiates a new knop image canvas.
	 */
	public KnopImageCanvas() {

		this(0, 0);

	}

	/**
	 * Sets the reset on image change.
	 * 
	 * @param resetOnImageChange
	 *           the new reset on image change
	 */
	public void setResetOnImageChange(boolean resetOnImageChange) {
		this.resetOnImageChange = resetOnImageChange;
	}

	/**
	 * Gets the fitting mode.
	 * 
	 * @return the fitting mode
	 */
	public int getFittingMode() {
		return fittingMode;
	}

	/**
	 * Sets the fitting mode.
	 * 
	 * @param fittingMode
	 *           the new fitting mode
	 */
	public void setFittingMode(int fittingMode) {
		this.fittingMode = fittingMode;
	}

	/**
	 * Fit image to canvas.
	 */
	public void fitImageToCanvas() {

		if (fittingMode == FIT_TO_SMALLER_DIMENSION) {

			int biggerDimensionOfImg;
			int smallerDimensionOfImg;
			double canvasRatio = 1.0 * canvasWidth / canvasHeight;
			double imageRatio = 1.0 * imageWidth / imageHeight;

			if (canvasRatio > imageRatio) {
				setMagnification(1.0 * imageHeight / canvasHeight);
			}

			else {
				setMagnification(1.0 * imageWidth / canvasWidth);
			}

			// setMagnification(1.0 *
			// biggerDimensionOfImg/smallerDimensionOfCanvas);

		} else {
			setMagnification(1.0 * imageWidth / canvasWidth);
		}
		// System.out.println("fitting to canvas : " + 1.0 *
		// imageHeight/canvasHeight);
		// repaint();

	}

	/**
	 * Fit to space.
	 */
	public void fitToSpace() {
		setSize(getMaximumSize());
		repaint();
	}

	/**
	 * Instantiates a new knop image canvas.
	 * 
	 * @param dimension
	 *           the dimension
	 */
	public KnopImageCanvas(Dimension dimension) {
		super();
		setSize(dimension);
		addMouseListener(listener);
		addMouseWheelListener(wheelListener);
		addMouseMotionListener(motionListener);

		// this.createBufferStrategy(4);
	}

	/**
	 * Instantiates a new knop image canvas.
	 * 
	 * @param w
	 *           the w
	 * @param h
	 *           the h
	 */
	public KnopImageCanvas(int w, int h) {
		this(new Dimension(w, h));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#setSize(java.awt.Dimension)
	 */
	public void setSize(Dimension dimension) {
		// System.out.println("Setting size : " + dimension);
		offscreen = null;
		super.setSize(dimension);
		setCanvasWidth(dimension.width);
		setCanvasHeight(dimension.height);

	}

	/**
	 * Gets the magnification.
	 * 
	 * @return the magnification
	 */
	public double getMagnification() {
		return magnification;
	}

	/**
	 * Sets the magnification.
	 * 
	 * @param magnification
	 *           the new magnification
	 */
	public void setMagnification(double magnification) {
		System.out.println("Setting magnification to " + magnification);
		this.magnification = magnification;

	}

	/**
	 * Round.
	 * 
	 * @param d
	 *           the d
	 * @return the int
	 */
	public int round(double d) {
		return Math.round(new Float(d));
	}

	/**
	 * Gets the real width.
	 * 
	 * @return the real width
	 */
	public int getRealWidth() {
		return round(imageWidth * magnification);
	}

	/**
	 * Gets the real height.
	 * 
	 * @return the real height
	 */
	public int getRealHeight() {
		return round(imageHeight * magnification);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#repaint()
	 */
	public void repaint() {
		super.repaint();
		// System.out.println("calling me again...");
	}

	/**
	 * Point.
	 * 
	 * @param x
	 *           the x
	 * @param y
	 *           the y
	 */
	public void point(int x, int y) {
		this.x = x;
		this.y = y;
		// System.out.println(x);
		// repaint();

	}

	/**
	 * Gets the canvas width.
	 * 
	 * @return the canvas width
	 */
	public int getCanvasWidth() {
		return canvasWidth;
	}

	/**
	 * Sets the canvas width.
	 * 
	 * @param canvasWidth
	 *           the new canvas width
	 */
	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	/**
	 * Gets the canvas height.
	 * 
	 * @return the canvas height
	 */
	public int getCanvasHeight() {
		return canvasHeight;
	}

	/**
	 * Sets the canvas height.
	 * 
	 * @param canvasHeight
	 *           the new canvas height
	 */
	public void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}

	/**
	 * Zoom in.
	 * 
	 * @param f
	 *           the f
	 */
	public void zoomIn(double f) {
		if (magnification > 30)
			return;
		// Point p = getMousePosition();
		// System.out.println(getMousePosition());
		f = Math.abs(f);
		magnification += 0.1 * f;
		// System.out.println(magnification);
	}

	/**
	 * Gets the mouse position on image.
	 * 
	 * @return the mouse position on image
	 */
	public Point getMousePositionOnImage() {
		// TODO Auto-generated method stub

		Point p = getMousePosition();
		p.x = lastPixelX;
		p.y = lastPixelY;

		return p;
	}

	/**
	 * Zoom out.
	 * 
	 * @param f
	 *           the f
	 */
	public void zoomOut(double f) {

		f = Math.abs(f);
		magnification -= 0.1 * f;
		if (magnification < 0.1)
			magnification = 0.1;
	}

	/**
	 * Draw processor.
	 * 
	 * @param g
	 *           the g
	 */
	public void drawProcessor(Graphics g) {

		try {
			if (imp == null)
				return;

			// super.update(g);
			double xLimitLeft = getCanvasWidth() * magnification / imageWidth - 1;
			double xLimitRight = -1 * xLimitLeft;
			double yLimitUp = getCanvasHeight() * magnification / imageHeight - 1;
			double yLimitDown = -1 * yLimitUp;
			// System.out.printf("limX(%.3f , %.3f)\n", xLimitLeft, xLimitRight);
			// System.out.printf("limY(%.3f , %.3f)\n", yLimitUp, yLimitDown);

			// Graphics2D g2 = (Graphics2D) g;
			// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			// g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			// RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			if (xCam < xLimitLeft)
				xCam = xLimitLeft;
			if (xCam > xLimitRight)
				xCam = xLimitRight;
			if (yCam < yLimitUp)
				yCam = yLimitUp;
			if (yCam > yLimitDown)
				yCam = yLimitDown;

			if (imageWidth / magnification < getCanvasWidth())
				xCam = 0;
			if (imageHeight / magnification < getCanvasHeight())
				yCam = 0;

			// System.out.printf("camF(%.3f , %.3f)\n",xCam,yCam);

			int cameraX = round((imageWidth / 2) + (1.0 * imageWidth / 2 * xCam)
					- (getCanvasWidth() * magnification) / 2);
			int cameraY = round((imageHeight / 2) + (1.0 * imageHeight / 2 * yCam)
					- (getCanvasHeight() * magnification) / 2);
			int cameraW = round(getCanvasWidth() * magnification);
			int cameraH = round(getCanvasHeight() * magnification);
			System.out.println(String.format("Camera %dx%d : c(%d,%d)",cameraW,cameraH,cameraX,cameraY));
			g.drawImage(imp.getImage(), 0, 0, getCanvasWidth(), getCanvasHeight(),
					cameraX, cameraY, cameraX + cameraW, cameraY + cameraH, null);
			// g.drawImage(ip.getBufferedImage(), 0, 0, null);

			if (selectionRectangle != null) {
				g.setColor(selectionColor);
				g.drawRect(selectionRectangle.x, selectionRectangle.y,
						selectionRectangle.width, selectionRectangle.height);

			}// g.drawImage(imp.getImage(), round(cameraX), y,
				// round(imageWidth * magnification), round(imageHeight
				// * magnification), null);
				// paint(g);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sets the image.
	 * 
	 * @param imp
	 *           the new image
	 */
	public void setImage(ImagePlus imp) {

		this.imp = imp;
		imageWidth = imp.getWidth();
		imageHeight = imp.getHeight();

		if (resetOnImageChange) {
			if (fitToCanvas)
				fitImageToCanvas();
			else
				setMagnification(1.0);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {

		if (getCanvasHeight() == 0 || getCanvasWidth() == 0)
			return;
		// create an accelerated image of the right size to store our sprite in
		GraphicsConfiguration gc = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		if (offscreen == null) {
			// offscreen = gc.createCompatibleImage(getCanvasWidth(),
			// getCanvasHeight(), Transparency.BITMASK);
			offscreen = ((Graphics2D) g).getDeviceConfiguration()
					.createCompatibleImage(getCanvasWidth(), getCanvasHeight());
			graphics2d = (Graphics2D) offscreen.getGraphics();

			/*
			 * graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			 * RenderingHints.VALUE_ANTIALIAS_ON); }
			 */
			graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,

			RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		}

		// super.update(graphics2d)

		graphics2d.setColor(getBackground());
		graphics2d.fillRect(0, 0, getCanvasWidth(), getCanvasHeight());

		drawProcessor(graphics2d);

		g.drawImage(offscreen, 0, 0, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Canvas#update(java.awt.Graphics)
	 */
	public void update(Graphics g) {

		// System.out.println("updating");
		paint(g);

	}

	/** The on mouse down. */
	Runnable onMouseDown = new Runnable() {

		public void run() {
			Point origin = new Point(x, y);
			Point originPoint = getMousePosition();;
			Point lastPoint = null;

			double xCam0, yCam0;

			xCam0 = xCam;
			yCam0 = yCam;

			double xLimit = 0.0;
			double yLimit = 0.0;

			Point lastPosition = null;

			long lastRefresh = System.currentTimeMillis();
			long refreshRate = 1000 / 24;

			while (mouseDown) {

				if (mouseDown) {

					// the point of origin is null when the mouse is pressed
					// (normaly)

					if (origin == null) {
						origin = new Point(x, y);
						originPoint = getMousePosition();

					}

					Point p = getMousePosition();

					if (p == null)
						continue;
					int dx = MathUtils.round(p.getX() - originPoint.getX());
					int dy = MathUtils.round(p.getY() - originPoint.getY());

					int newX = MathUtils.round(origin.getX() + dx);
					int newY = MathUtils.round(origin.getY() + dy);
					if (mode == MOVE_MODE) {
						xCam = xCam0 + 1.0 * (dx) * magnification / imageWidth * 2
								* -1;
						yCam = yCam0 + 1.0 * (dy) * magnification / imageHeight * 2
								* -1;
						// System.out.println(String.format("d(%d,%d)", dx, dy));
						// System.out.printf("Cam(%.3f - %.3f)\n", xCam, yCam);

					} else {

						int xMouse0 = MathUtils.round(originPoint.getX());
						int yMouse0 = MathUtils.round(originPoint.getY());

						if (dx < 0)
							xMouse0 = xMouse0 + dx;
						if (dy < 0)
							yMouse0 = yMouse0 + dy;

						selectionRectangle = new Rectangle(xMouse0, yMouse0,
								Math.abs(dx), Math.abs(dy));
					}

					// if(newX > 0 && newX < getWidth() && newY > 0 && newY
					// < getHeight())

					long now = System.currentTimeMillis();

					if (lastPosition == null) {
						lastPosition = p;
					} else if (!p.equals(lastPosition)
							&& now - lastRefresh > refreshRate) {
						lastPosition = p;
						lastRefresh = now;
						repaint();
					}

					// point(newX, newY);
				}

			}
		}
	};

	/** The wheel listener. */
	MouseWheelListener wheelListener = new MouseWheelListener() {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			System.out.println("wheel rotation : "+e.getWheelRotation());
			System.out.println("screel amount : " +e.getScrollAmount());
			System.out.println("wheel event " + e);
			if (e.getWheelRotation() < 0) {
				zoomIn(e.getWheelRotation());
				repaint();
			} else {
				zoomOut(e.getWheelRotation());
				repaint();
			}
		}
	};

	/** The listener. */
	MouseListener listener = new MouseListener() {

		@Override
		public void mouseReleased(MouseEvent e) {

			mouseDown = false;
			/*
			 * if (doOpening && System.currentTimeMillis() - last_click < 200) {
			 * new KnopImageWindow(imp.getProcessor()).show(); }
			 */

			if (mode == SELECTION_MODE && selectionRectangle != null) {

				int rx = screenXToImageX(selectionRectangle.x);
				int ry = screenYToImageY(selectionRectangle.y);
				int rw = screenXToImageX(selectionRectangle.x
						+ selectionRectangle.width)
						- rx;
				int rh = screenYToImageY(selectionRectangle.y
						+ selectionRectangle.height)
						- ry;

				lastSelection = new Rectangle(rx, ry, rw, rh);
				System.out.println(lastSelection);
			}
			selectionRectangle = null;
			// System.out.println("Mouse Up !!!");
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.isMetaDown()) {
				mode = SELECTION_MODE;
			} else {
				mode = MOVE_MODE;
			}

			System.out.println("Mouse Down !");
			mouseDown = true;
			last_click = System.currentTimeMillis();

			mouseThread.execute(onMouseDown);

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			// setCursor(Cursor.DEFAULT_CURSOR);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			// setCursor(Cursor.HAND_CURSOR);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * Sets the image.
	 * 
	 * @param ip
	 *           the new image
	 */
	public void setImage(ImageProcessor ip) {
		// TODO Auto-generated method stub
		if (ip == null)
			return;
		setImage(new ImagePlus("", ip));
	}

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	public ImagePlus getImage() {
		return this.imp;
	}

	/** The motion listener. */
	public MouseMotionListener motionListener = new MouseMotionListener() {

		@Override
		public void mouseMoved(MouseEvent e) {
			Point p = e.getPoint();

			int xi = screenXToImageX(round(p.getX()));
			int yi = screenYToImageY(round(p.getY()));

			if (xi < 0 || yi < 0 || xi > imageWidth || yi > imageHeight) {
				lastPixel = -1;
				return;
			}

			// System.out.printf("pointer(%d,%d)\n",xi,yi);

			lastPixel = imp.getProcessor().getPixelValue(xi, yi);
			lastPixelX = xi;
			lastPixelY = yi;

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	};

	// return the x position on the image from the x position on the canvas
	/**
	 * Screen x to image x.
	 * 
	 * @param screenX
	 *           the screen x
	 * @return the int
	 */
	public int screenXToImageX(int screenX) {
		return round((imageWidth / 2) + (imageWidth / 2) * xCam
				- (getCanvasWidth() / 2) * magnification + screenX * magnification
				- 0.5);
	}

	/**
	 * Screen x to absolute.
	 * 
	 * @param screenX
	 *           the screen x
	 * @return the double
	 */
	public double screenXToAbsolute(int screenX) {
		return (1.0 * screenXToImageX(screenX) / imageWidth * 2) - 1;
	}

	/**
	 * Screen y to absolute.
	 * 
	 * @param screenY
	 *           the screen y
	 * @return the double
	 */
	public double screenYToAbsolute(int screenY) {
		return (1.0 * screenYToImageY(screenY) / imageHeight * 2) - 1;
	}

	// return the x position on the image from the x position on the canvas
	/**
	 * Screen y to image y.
	 * 
	 * @param screenY
	 *           the screen y
	 * @return the int
	 */
	public int screenYToImageY(int screenY) {
		return round(imageHeight / 2 + imageHeight / 2 * yCam
				- (getCanvasHeight() / 2 * magnification) + screenY * magnification
				- 0.5);
	}

	/**
	 * Gets the last pixel value.
	 * 
	 * @return the last pixel value
	 */
	public double getLastPixelValue() {
		return lastPixel;
	}

	/**
	 * Sets the enable opening.
	 * 
	 * @param b
	 *           the new enable opening
	 */
	public void setEnableOpening(boolean b) {
		doOpening = b;
	}

	/**
	 * Sets the canvas fitting enabled.
	 * 
	 * @param b
	 *           the new canvas fitting enabled
	 */
	public void setCanvasFittingEnabled(boolean b) {
		fitToCanvas = b;
	}

	/**
	 * Gets the last selection rectangle.
	 * 
	 * @return the last selection rectangle
	 */
	public Rectangle getLastSelectionRectangle() {
		return lastSelection;
	}

	/**
	 * Last click on image.
	 * 
	 * @return the point
	 */
	public Point lastClickOnImage() {
		return new Point(lastPixelX, lastPixelY);
	}

	/**
	 * Sets the mode.
	 * 
	 * @param m
	 *           the new mode
	 */
	public void setMode(int m) {
		mode = m;
	}

}
