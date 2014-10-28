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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Random;

import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;



// TODO: Auto-generated Javadoc
/**
 * The Class HeatMapGenerator.
 */
public class HeatMapGenerator extends Observable {

	/** The image width. */
	int imageWidth;
	
	/** The image height. */
	int imageHeight;

	/** The current column. */
	String currentColumn;

	/** The steps. */
	int steps = 10;

	/** The space. */
	SmouthFractionnedSpace space;

	/** The data set. */
	FovDataSet dataSet;

	/** The preview. */
	ImageProcessor preview = null;
	
	/** The space image. */
	ImageProcessor spaceImage = null;


	
	/** The raw heatmap. */
	ImageProcessor rawHeatmap = null;
	
	/** The undefined. */
	private double UNDEFINED = Double.NaN;

	/** The min. */
	double min;
	
	/** The max. */
	double max;

	/** The line. */
	double line;

	/** The plot min. */
	double plotMin = Double.NaN;
	
	/** The plot max. */
	double plotMax = Double.NaN;
	
	/** The plot line. */
	double plotLine = Double.NaN;

	/** The min label. */
	String minLabel = "";
	
	/** The max label. */
	String maxLabel = "";
	
	/** The line label. */
	String lineLabel = "";

	/** The unit. */
	String unit = "";

	/** The calibration. */
	Calibration calibration = new Calibration();

	/** The lut. */
	EasyLUT lut = new EasyLUT("psfj_fwhm");

	/** The median. */
	double median = -1;

	/** The step size. */
	double stepSize;
	
	/** The window size. */
	double windowSize;

	/** The scale division. */
	int scaleDivision = 4;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		BeadImage i = new BeadImage();
		i.setFileAddress("/home/cyril/test_img/6_small.tif");
		i.workFromMemory();

		FovDataSet dataSet = new FovDataSet();

		Random random = new Random();

		for (int j = 0; j != 3000; j++) {
			dataSet.addValue("x", random.nextDouble() * 10);
			dataSet.addValue("y", random.nextDouble() * 10);
			dataSet.addValue("deltaX", random.nextDouble() * 1.5 / 2);
		}

		// dataSet.setTheoriticalValue("deltaX_norm", 1.0);
		dataSet.setColumnsUnits(MathUtils.MICROMETERS,"x","y","deltaX");
		HeatMapGenerator generator = new HeatMapGenerator(dataSet);
		generator.setMinAndMax(-1, 0, 1);
		// generator.setMinAndMaxLabels("", lineLabel, maxLabel)
		generator.setUnit("");
		generator.setBeadImage(i);
		// generator.setLutMode(NORMALIZED);
		generator.setCurrentColumn("deltaX");

		// generator.show(gene);

		// generator.injectDataSet(new FovDataSet("/home/cyril/data2.txt"),
		// "fwhmZ");
		generator.show(generator.getAnnotatedHeatMap());

		// System.out.println(dataSet.getTheoriticalValue("fwhmZ"));

	}

	/**
	 * Sets the unit.
	 *
	 * @param string the new unit
	 */
	public void setUnit(String string) {
		this.unit = string;

	}

	/**
	 * Instantiates a new heat map generator.
	 *
	 * @param dataSet the data set
	 */
	public HeatMapGenerator(FovDataSet dataSet) {
		this.dataSet = dataSet;

	}

	/**
	 * Instantiates a new heat map generator.
	 *
	 * @param dataSet the data set
	 * @param image the image
	 */
	public HeatMapGenerator(FovDataSet dataSet, BeadImage image) {
		this(dataSet);
		defineSpace(image.getImageWidth(), image.getImageHeight(),
				image.getCalibration());
	}

	/**
	 * Show.
	 */
	public void show() {
		show(getAnnotatedHeatMap());
	}

	/**
	 * Show.
	 *
	 * @param ip the ip
	 */
	public void show(ImageProcessor ip) {
		ImagePlus imp = new ImagePlus("", ip);

		new ImageWindow(imp);
		IJ.setupDialog(imp, 0);
	}

	/**
	 * Gets the preview.
	 *
	 * @return the preview
	 */
	public ImageProcessor getPreview() {
		return preview;
	}

	/**
	 * Update preview async.
	 */
	public void updatePreviewAsync() {
		putWaitingImage();
		new Thread() {
			public void run() {
				updatePreview();
			}
		}.start();
	}

	/**
	 * Put waiting image.
	 */
	public void putWaitingImage() {
		ImageProcessor p = new ByteProcessor(250, 30);
		p.setColor(250);
		p.drawString("Generating HeatMap...", 20, 20);
		preview = p;
		setChanged();
		notifyObservers(new Message(this, "heatmap generation ongoing"));
	}

	/**
	 * Update preview.
	 */
	public void updatePreview() {
		// updateSpace();

		preview = getAnnotatedHeatMap();
		for (int i = 0; preview == null && i < 10; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println("???");
		setChanged();
		notifyObservers(new Message(this, "heatmap generation finished", "Done.",
				new Integer(0)));
	}

	/**
	 * Gets the current column.
	 *
	 * @return the current column
	 */
	public String getCurrentColumn() {
		// TODO Auto-generated method stub
		return currentColumn;
	}

	/**
	 * Inject data set.
	 *
	 * @param dataSet the data set
	 * @param column1 the column1
	 */
	public void injectDataSet(FovDataSet dataSet, String column1) {

		median = -1;
		String action = "#Generating heatmap";

		setChanged();
		notifyObservers(new Message(this, action, "Generating Heatmap...",
				new Integer(10)));

		int columnSize = dataSet.getColumnSize(column1);
		// System.out.println("Injecting "+columnSize+" elements in this heatmap");
		if (dataSet == null) {
			System.err.println("No Data set !!!!");
			return;
		}

		

		for (int i = 0; i != dataSet.getColumnSize(column1); i++) {
			setChanged();
			notifyObservers(new Message(this, action, "Generating Heatmap...",
					new Integer(10 + 90 * i / columnSize)));
			double value = dataSet.getDoubleValue(column1, i);

			// System.out.println("Inserting "+value+" in "+ dataSet.getValue("x",
			// i) + "," + dataSet.getValue("y", i));
			space.insertPoint(dataSet.getDoubleValue("x", i), dataSet.getDoubleValue("y", i),
					value);
		}

	}

	/**
	 * Gets the median.
	 *
	 * @return the median
	 */
	public double getMedian() {
		if (median == -1) {
			median = dataSet.getColumnStatistics(currentColumn).getPercentile(50);
		}
		return median;
	}

	/**
	 * Sets the min and max.
	 *
	 * @param min the min
	 * @param line the line
	 * @param max the max
	 */
	public void setMinAndMax(double min, double line, double max) {
		this.min = min;
		this.max = max;
		this.line = line;
	}

	/**
	 * Sets the min and max labels.
	 *
	 * @param minLabel the min label
	 * @param lineLabel the line label
	 * @param maxLabel the max label
	 */
	public void setMinAndMaxLabels(String minLabel, String lineLabel,
			String maxLabel) {
		this.minLabel = minLabel;
		this.maxLabel = maxLabel;
		this.lineLabel = lineLabel;
	}

	/**
	 * Sets the plot min and max.
	 *
	 * @param plotMin the plot min
	 * @param plotLine the plot line
	 * @param plotMax the plot max
	 */
	public void setPlotMinAndMax(double plotMin, double plotLine, double plotMax) {
		this.plotMin = plotMin;
		this.plotMax = plotMax;
		this.plotLine = plotLine;
	}

	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Gets the line.
	 *
	 * @return the line
	 */
	public double getLine() {
		return line;
	}

	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Gets the plot min.
	 *
	 * @return the plot min
	 */
	public double getPlotMin() {
		if (Double.isNaN(plotMin))
			return getMin();
		else
			return plotMin;
	}

	/**
	 * Gets the plot max.
	 *
	 * @return the plot max
	 */
	public double getPlotMax() {
		if (Double.isNaN(plotMax))
			return getMax();
		else
			return plotMax;
	}

	/**
	 * Gets the plot line.
	 *
	 * @return the plot line
	 */
	public double getPlotLine() {
		if (Double.isNaN(plotLine))
			return getLine();
		else
			return plotLine;
	}

	/**
	 * Gets the min label.
	 *
	 * @return the min label
	 */
	public String getMinLabel() {
		return minLabel;
	}

	/**
	 * Gets the line label.
	 *
	 * @return the line label
	 */
	public String getLineLabel() {
		return lineLabel;
	}

	/**
	 * Gets the max label.
	 *
	 * @return the max label
	 */
	public String getMaxLabel() {
		return maxLabel;
	}

	/**
	 * Gets the space image.
	 *
	 * @return the space image
	 */
	public ImageProcessor getSpaceImage() {
		if (spaceImage == null) {
			spaceImage = getSpace().getImage();
		}
		return spaceImage;
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Gets the raw heat map.
	 *
	 * @return the raw heat map
	 */
	public ImageProcessor getRawHeatMap() {
		ImageProcessor ip;
		
		//if(rawHeatmap != null) return rawHeatmap;
		
		
		if (getSpace().columns * getSpace().rows < 4) {
			ip = new FloatProcessor(400, 400);
			ip.setColor(Color.white);
			ip.fill();
			ip.setColor(Color.black);
			ip.setFont(new java.awt.Font("Arial", Font.BOLD, 20));
			ip.drawString("There is not enough\nbeads for a heatmap.", 100, 100);
			
		} else {

			ip = getSpaceImage().duplicate();

			System.out.println("resizing to " + imageWidth);

			ip = ip.resize(imageWidth, imageHeight, true);
			
			double min = getMin();
			double max = getMax();

			ip.setLut(getCurrentLUT().getLUT(min, max));
			
			
			
		}

		//rawHeatmap = ip;
		
		return ip;
	}

	/**
	 * Gets the colored heat map.
	 *
	 * @return the colored heat map
	 */
	public ImageProcessor getColoredHeatMap() {
		return getColoredHeatMap(imageWidth);
	}

	/**
	 * Gets the colored heat map.
	 *
	 * @param column the column
	 * @return the colored heat map
	 */
	public ImageProcessor getColoredHeatMap(String column) {
		return getColoredHeatMap(column, imageWidth);
	}

	/**
	 * Gets the colored heat map.
	 *
	 * @param column the column
	 * @param width the width
	 * @return the colored heat map
	 */
	public ImageProcessor getColoredHeatMap(String column, int width) {
		setCurrentColumn(column);
		return getColoredHeatMap(width);
	}

	/**
	 * Gets the colored heat map.
	 *
	 * @param width the width
	 * @return the colored heat map
	 */
	public ImageProcessor getColoredHeatMap(int width) {
		
		//if(coloredHeatmap != null && coloredHeatmap.getWidth() == width) return coloredHeatmap;
		
		ImageProcessor ip = getRawHeatMap();
		if (width != imageWidth)
			ip = ip.resize(width); //, width * imageHeight / imageWidth);

		double min = getMin();
		double max = getMax();

		ip.setLut(getCurrentLUT().getLUT(min, max));

		ImageProcessor cp = ip;
	//	coloredHeatmap = ip;
		return cp;
	}

	/**
	 * Gets the annotated heat map.
	 *
	 * @param column the column
	 * @return the annotated heat map
	 */
	public ImageProcessor getAnnotatedHeatMap(String column) {
		setCurrentColumn(column);
		return getAnnotatedHeatMap();
	}

	/**
	 * Gets the colored heat map with scale.
	 *
	 * @param width the width
	 * @return the colored heat map with scale
	 */
	public ImageProcessor getColoredHeatMapWithScale(int width) {

		
		
		
		int fontSize = width / 20;

		java.awt.Font font = new java.awt.Font(java.awt.Font.SANS_SERIF,
				java.awt.Font.PLAIN, fontSize);

		int headerSize = fontSize;
		int margin = width / 30;

		// getting the colored heatmap
		ImageProcessor ip = getColoredHeatMap(width);

		// getting min and max values for the lut

		double min = getMin();
		double max = getMax();

		ImageProcessor lut = getLutImage(ip.getHeight(), margin, (float) min,
				(float) max);
		ip.setFont(font);

		// setting some coordinnates

		int heatmapX = margin;
		int heatmapY = margin;

		int lutX = ip.getWidth() + margin * 2;
		int lutY = heatmapY;

		

		int scaleHeight = ip.getHeight() / 30;

		int finalWidth = heatmapY + margin * 2 + ip.getWidth() + lut.getWidth()
				+ ip.getStringWidth("1000000 nm");
		int finalHeight = margin * 2 + fontSize + ip.getHeight() + scaleHeight;

		ImageProcessor finalIp = new ColorProcessor(finalWidth, finalHeight);
		finalIp.setColor(Color.white);
		finalIp.fill();

		// drawing heatmap
		finalIp.copyBits(ip, heatmapX, heatmapY, Blitter.COPY);

		// drawing lut
		finalIp.copyBits(lut, lutX, lutY, Blitter.COPY);
	
		// drawing lut scale
		finalIp.setFont(font);
		int lutScaleX = lutX + lut.getWidth();
		int lutScaleThikness = width/100;
		int lutScaleLength = margin;

		int leftScaleStep = MathUtils.round(lut.getHeight() / scaleDivision);
		finalIp.setColor(Color.black);
		for (int i = 0; i != scaleDivision + 1; i++) {
			int leftScaleY = lutY + leftScaleStep * i
					- (lutScaleThikness * i / (scaleDivision));

			// finalIp.drawRect(leftScaleX, lutY+leftScaleStep*i, leftScaleLength,
			// leftScaleThikness);
			finalIp.fill(new Roi(new Rectangle(lutScaleX, leftScaleY,
					lutScaleLength, lutScaleThikness)));
			finalIp
					.drawString(
							MathUtils
									.formatDouble(
											getMin()
													+ ((getMax() - getMin())
															* (scaleDivision - i) / (scaleDivision)),
											getUnit()), lutScaleX + margin*2, leftScaleY
									+ finalIp.getFont().getSize() / 2);
		}
		
		
		
		// drawing heatmap scale
		
		int scaleX = margin*2;
		int scaleY = ip.getHeight()-margin;

		
		
		
		//int scaleMeasureInUm = roundUp(MathUtils.round(1.0*getColumnCount()*stepSize/5));
		
		
		///int scaleMeasureInPixel = MathUtils.round(scaleMeasureInUm/stepSize);
		
	
		drawScale(finalIp,width,scaleX,scaleY,scaleHeight,margin);
		
		

		return finalIp;
	}

	/**
	 * Draw scale.
	 *
	 * @param finalIp the final ip
	 * @param finalImageWidth the final image width
	 * @param scaleX the scale x
	 * @param scaleY the scale y
	 * @param scaleHeight the scale height
	 * @param margin the margin
	 */
	public void drawScale(ImageProcessor finalIp, int finalImageWidth, int scaleX, int scaleY,int scaleHeight,int margin) {
		
		
		if(getColumnCount() == 0) return;
		
		int width = finalImageWidth;
		double pixelRatio = width/getColumnCount();
		double pixelSize = getSpace().stepSize;
		System.out.println("pixel ratio = "+pixelRatio);
		System.out.println("pixel size = "+pixelSize);
		double imageWidthInUm = 1.0*getColumnCount()*pixelSize;
		System.out.println("image width in um = "+imageWidthInUm);
		
		int scaleMeasureInUm = roundUp(MathUtils.round(imageWidthInUm/10));
		if(scaleMeasureInUm == 0) scaleMeasureInUm = 1;
		
		System.out.println("scale measure = " + scaleMeasureInUm);
		int scaleMeasureInPixel = MathUtils.round(1.0*scaleMeasureInUm * pixelRatio / pixelSize);
		System.out.println("scale measure in Pixel = "+scaleMeasureInPixel);
		int scaleMeasure = roundUp(MathUtils.round(1.0 * imageWidth
				* stepSize / 10));
		int scaleWidth = MathUtils.round(width * scaleMeasure
				/ (imageWidth * calibration.pixelWidth));

		
		
		finalIp.setColor(Color.black);

		finalIp.setJustification(ImageProcessor.LEFT_JUSTIFY);

		for (int i = 0; i != scaleMeasureInPixel; i++) {
			finalIp.drawRect(scaleX + i, scaleY, 1, scaleHeight);
		}
		finalIp.drawString("" + scaleMeasureInUm + MathUtils.MICROMETERS, scaleX,
				scaleY -margin/2);
	}
	
	
	/**
	 * Gets the annotated heat map.
	 *
	 * @return the annotated heat map
	 */
	public ImageProcessor getAnnotatedHeatMap() {
		
		
		
		// getting the colored heatmap
		ImageProcessor ip = getColoredHeatMapWithScale(900);
		
		Font font = ip.getFont();
		int fontHeight = font.getSize();
		
		int margin = ip.getWidth()/30;
		
		int plotW = 400;
		int plotH = 200;

		ImageProcessor xProjectionPlot = plotColumns("x", currentColumn, plotW,
				plotH);
		ImageProcessor yProjectionPlot = plotColumns("y", currentColumn, plotW,
				plotH);
		
		xProjectionPlot = xProjectionPlot.resize(xProjectionPlot.getWidth()*2,xProjectionPlot.getHeight()*2,true);
		yProjectionPlot = yProjectionPlot.resize(yProjectionPlot.getWidth()*2,yProjectionPlot.getHeight()*2,true);
		
		plotW = xProjectionPlot.getWidth();
		plotH = yProjectionPlot.getHeight();
		
		
		
		int plotX = ip.getWidth()+margin;
		int plotY = fontHeight+margin;
		int plotY2 = plotY+xProjectionPlot.getHeight()+fontHeight+margin*2;
		
		// width of the final image
		int finalWidth = ip.getWidth()+xProjectionPlot.getWidth()+margin*2;
		
		// biggest height, plots or heatmap ?
		int higher;
		
		// if heatmap, so be it
		if(ip.getHeight() > plotH*2+margin) higher = ip.getHeight();
		else higher = plotH*2+margin;
		
		int finalHeight = fontHeight+higher+margin*3;
		

		
		ImageProcessor finalIp = new ColorProcessor(finalWidth,finalHeight);
		
		finalIp.setColor(Color.white);
		finalIp.fill();
		
		finalIp.copyBits(ip, margin, plotY, Blitter.COPY);
		
		
		
		finalIp.copyBits(xProjectionPlot, plotX, plotY, Blitter.COPY);
		finalIp.copyBits(yProjectionPlot, plotX, plotY2, Blitter.COPY);
		
		finalIp.setFont(font);
		finalIp.setColor(Color.black);
		
		finalIp.setJustification(ImageProcessor.CENTER_JUSTIFY);
		finalIp.drawString(getColumnName(currentColumn),margin+ip.getWidth()/2,margin+fontHeight);
		finalIp.drawString("Projection along the X-axis",plotX+plotW/2,plotY);
		finalIp.drawString("Projection along the Y-axis",plotX+plotW/2,plotY2);
		
		System.gc();
		
		return finalIp;
		
		
	}
	
	
	

	/**
	 * Gets the window size.
	 *
	 * @return the window size
	 */
	public double getWindowSize() {
		return getSpace().windowSize;
	}
	
	/**
	 * Gets the step size.
	 *
	 * @return the step size
	 */
	public double getStepSize() {
		return getSpace().stepSize;
	}

	/**
	 * Gets the mean number of beads per square.
	 *
	 * @return the mean number of beads per square
	 */
	public double getMeanNumberOfBeadsPerSquare() {
		return getSpace().getMeanNumberOfBeads();
	}

	/**
	 * Gets the calibration.
	 *
	 * @return the calibration
	 */
	private Calibration getCalibration() {
		// TODO Auto-generated method stub
		return calibration;
	}

	/**
	 * Gets the lut image.
	 *
	 * @param lutHeight the lut height
	 * @param min the min
	 * @param max the max
	 * @return the lut image
	 */
	public ImageProcessor getLutImage(int lutHeight, float min, float max) {
		return getLutImage(lutHeight, 10, min, max);
	}

	/**
	 * Gets the lut image.
	 *
	 * @param lutHeight the lut height
	 * @param lutWidth the lut width
	 * @param min the min
	 * @param max the max
	 * @return the lut image
	 */
	public ImageProcessor getLutImage(int lutHeight, int lutWidth, float min,
			float max) {
		int lutX = 0;
		int lutY = 0;

		FloatProcessor lut = new FloatProcessor(lutWidth, lutHeight);
		lut.setLut(getCurrentLUT().getLUT(min, max));
		float step = (max - min) / lutHeight;

		for (int y = lutY; y != (lutY + lutHeight); y++) {
			float value = max - y * step;

			lut.setValue((double) value);
			lut.drawRect(lutX, y, lutWidth, 1);
		}
		return lut;

	}

	/**
	 * Draw image.
	 *
	 * @param source the source
	 * @param destination the destination
	 * @param dx the dx
	 * @param dy the dy
	 */
	public void drawImage(ImageProcessor source, ImageProcessor destination,
			int dx, int dy) {
		for (int x = 0; x != source.getWidth(); x++) {
			for (int y = 0; y != source.getHeight(); y++) {

				destination.setValue(source.get(x, y));
				destination.drawPixel(dx + x, dy + y);
			}
		}
	}

	/**
	 * Gets the heap map.
	 *
	 * @param factor the factor
	 * @return the heap map
	 */
	public ImageProcessor getHeapMap(int factor) {
		ImageProcessor image = space.getImage();
		return image.resize(image.getWidth() * factor);
	}

	/**
	 * Define space.
	 *
	 * @param width the width
	 * @param height the height
	 * @param calibration the calibration
	 */
	public void defineSpace(int width, int height, Calibration calibration) {

		this.imageWidth = width;
		this.imageHeight = height;
		this.calibration = calibration;

	}

	/*
	 * public void setSteps(int steps) { if(calibration != null) { this.steps =
	 * steps; defineSpace(calibration,imageWidth,imageHeight); space = null; } }
	 */

	/**
	 * Gets the space.
	 *
	 * @return the space
	 */
	public synchronized FractionnedSpace getSpace() {
		if (space == null) {
			double spaceWidth = 1.0 * imageWidth * getCalibration().pixelWidth;
			double spaceHeight = 1.0 * imageHeight * getCalibration().pixelHeight;

			double spaceX = -1.0 * spaceWidth / 2;
			double spaceY = spaceHeight / 2;

			space = new SmouthFractionnedSpace(spaceX, spaceY, spaceWidth,
					spaceHeight, dataSet.getColumnSize());

			spaceImage = null;
			injectDataSet(dataSet, currentColumn);
		}
		return space;
	}

	/**
	 * Sets the current column.
	 *
	 * @param string the new current column
	 */
	public void setCurrentColumn(String string) {

		if (currentColumn != null && currentColumn.equals(string))
			return;

		currentColumn = string;
		median = -1;
		spaceImage = null;
		space = null;
		rawHeatmap = null;
		

	}

	/**
	 * Gets the current lut.
	 *
	 * @return the current lut
	 */
	public EasyLUT getCurrentLUT() {
		return lut;
	}

	/**
	 * Sets the current lut.
	 *
	 * @param lut the new current lut
	 */
	public void setCurrentLUT(EasyLUT lut) {
		this.lut = lut;
	}

	/**
	 * Sets the current lut.
	 *
	 * @param lut the new current lut
	 */
	public void setCurrentLUT(String lut) {
		setCurrentLUT((EasyLUT) new EasyLUT(lut));
	}

	/**
	 * Sets the bead image.
	 *
	 * @param image the new bead image
	 */
	public void setBeadImage(BeadImage image) {
		// beadImage = image;
		defineSpace(image.getImageWidth(), image.getImageHeight(),
				image.getCalibration());
	}

	/**
	 * Round up.
	 *
	 * @param n the n
	 * @return the int
	 */
	int roundUp(int n) {
		return (n + 4) / 5 * 5;
	}

	/**
	 * Gets the column name.
	 *
	 * @return the column name
	 */
	public String getColumnName() {
		return getColumnName(currentColumn);
	}
	
	/**
	 * Gets the column name.
	 *
	 * @param column the column
	 * @return the column name
	 */
	private String getColumnName(String column) {
		return dataSet.getColumnName(column);
	}

	/**
	 * Gets the column size.
	 *
	 * @return the column size
	 */
	private int getColumnSize() {
		return dataSet.getColumnSize();
	}

	/**
	 * Plot columns.
	 *
	 * @param xColumn the x column
	 * @param yColumn the y column
	 * @param width the width
	 * @param height the height
	 * @return the image processor
	 */
	public ImageProcessor plotColumns(String xColumn, String yColumn, int width,
			int height) {

		// sorting the value
		ArrayList<KnPoint> pointList = new ArrayList<KnPoint>();

		for (int i = 0; i != getColumnSize(); i++) {
			pointList.add(new KnPoint(dataSet.getDoubleValue(xColumn, i), dataSet
					.getDoubleValue(yColumn, i)));
		}

		Collections.sort(pointList);

		// putting the value in arrays of double
		double[] x = new double[pointList.size()];
		double[] y = new double[pointList.size()];

		for (int i = 0; i != pointList.size(); i++) {

			KnPoint k = pointList.get(i);
			x[i] = k.x;

			y[i] = k.y;

		}

		String unit = getColumnUnit(yColumn);

		double max = getPlotMax();
		double min = getPlotMin();
		double line = getPlotLine();
		
		double xLimitMin;
		double xLimitMax;
		
		double half;
		double coeff;
		
		if(xColumn.contains("y")) {
			half = 1.0*calibration.pixelHeight*imageHeight/2;
			coeff = 1;
		}
		else {
			half = 1.0*calibration.pixelWidth*imageWidth/2;
			coeff = 1;
		}
		
		xLimitMin = -1.0*coeff * half;
		xLimitMax = coeff * half;
		
		
		double[] thLineX = new double[] { xLimitMin,xLimitMax };
		double[] thLineY = new double[] { line, line };
		//double[] thLine = new double[y.length];
		/*
		for (int i = 0; i != thLine.length; i++) {
			thLine[i] = line;
		}
		*/
		String xLabel = getColumnName(xColumn) + (getColumnUnit(xColumn).equals("") ? "" : " (" + getColumnUnit(xColumn) + ")");
		String yLabel = getColumnName(yColumn) + (getColumnUnit(yColumn).equals("") ? "" : " (" + getColumnUnit(yColumn) + ")");
		
		Plot p = new Plot("",xLabel,yLabel);
		p.setSize(width, height);
		
				p.setXLabelFont(new java.awt.Font(Font.DIALOG, Font.PLAIN,height/16));
				p.setYLabelFont(new java.awt.Font(Font.DIALOG, Font.PLAIN,height/16));
	//	 Plot p = new Plot("", getColumnName(xColumn) + " (µm)",
		//getColumnName(yColumn) + " " + unit, x, y);
		//Plot p = new Plot("", getColumnName(xColumn) + " ("
			//	+ getColumnUnit(xColumn) + ")",
				//getColumnName(yColumn) + " " + unit, x, y);
		//p.setColor(Color.black);
		//p.addPoints(x, y, Plot.LINE);
		//p.setColor(Color.blue);
		
		// p.setFont(new java.awt.Font("Default",18,Font.NORMAL));
		
		if (x.length > 0)
			p.setLimits(xLimitMin, xLimitMax, min, max);
		p.setColor(Color.red);
		p.setLineWidth(1);
		p.addPoints(thLineX, thLineY, Plot.LINE);
		
		
		p.setColor(Color.blue);
		p.setLineWidth(1);

		p.addPoints(x, y, Plot.LINE);

		return p.getProcessor();

	}

	/**
	 * Gets the column unit.
	 *
	 * @param column the column
	 * @return the column unit
	 */
	public String getColumnUnit(String column) {
		return dataSet.getColumnUnit(column);
	}

	/**
	 * The Class KnPoint.
	 */
	private class KnPoint implements Comparable<KnPoint> {
		
		/** The x. */
		double x;
		
		/** The y. */
		double y;

		/**
		 * Instantiates a new kn point.
		 *
		 * @param x the x
		 * @param y the y
		 */
		KnPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(KnPoint o) {
			if (x > o.x) {
				return 1;
			} else if (x < o.x) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Gets the column count.
	 *
	 * @return the column count
	 */
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return getSpace().columns;
	}

	/**
	 * Gets the row count.
	 *
	 * @return the row count
	 */
	public int getRowCount() {
		return getSpace().rows;
	}

	/**
	 * Gets the scale division.
	 *
	 * @return the scale division
	 */
	public int getScaleDivision() {
		return scaleDivision;
	}

	/**
	 * Sets the scale division.
	 *
	 * @param scaleDivision the new scale division
	 */
	public void setScaleDivision(int scaleDivision) {
		this.scaleDivision = scaleDivision;
	}

	/**
	 * Gets the currnet column statistics.
	 *
	 * @return the currnet column statistics
	 */
	public DescriptiveStatistics getCurrnetColumnStatistics() {
		
		
		
		return dataSet.getColumnStatistics(currentColumn);
	}
	
	

}
