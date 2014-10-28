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
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;

import knop.psfj.BeadImage;
import knop.psfj.FovDataSet;
import knop.psfj.utils.MathUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ThetaHeatMapGenerator.
 */
public class ThetaHeatMapGenerator extends HeatMapGenerator {

	/** The asymmetry generator. */
	HeatMapGenerator asymmetryGenerator;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		/*
		BeadImageManager manager = new BeadImageManager();
		manager.add("/home/cyril/test_img/6_small.tif");
		manager.autoFocus(0);
		manager.autoThreshold();
		manager.autoBeadEnlargementFactor();
		manager.processProfiles();
		*/
		ImagePlus ip = new ImagePlus("",ThetaHeatMapGenerator.generateHueScale(1200));
		ip.show();
		
		IJ.saveAs(ip,"png","/home/cyril/theta_scale.png");
		//IJ.saveAs(ip, "png","/Users/cyril/Dropbox/PatroloJ Paper (1)/Paper files/Nature Methods Communications/Files for submission/revised version/revised fig .ai files/theta_scale.png");
		
		// ;

	}

	/**
	 * Instantiates a new theta heat map generator.
	 *
	 * @param dataSet the data set
	 */
	public ThetaHeatMapGenerator(FovDataSet dataSet) {
		super(dataSet);

		setCurrentColumn("theta");
		setMinAndMax(-90, 0, 90);
		setCurrentLUT("psfj_theta");
		setUnit(MathUtils.DEGREES);
		asymmetryGenerator = new HeatMapGenerator(dataSet);

		EasyLUT blackToWhite = new EasyLUT();
		blackToWhite.setColorModel(LUT.createLutFromColor(Color.white));
		asymmetryGenerator.setCurrentLUT(blackToWhite);
		asymmetryGenerator.setMinAndMax(0, 0.5, 1);
		asymmetryGenerator.setCurrentColumn("asymmetry");

		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see knop.psfj.heatmap.HeatMapGenerator#getColoredHeatMap(int)
	 */
	@Override
	public ImageProcessor getColoredHeatMap(int width) {
		ImageProcessor theta = super.getRawHeatMap();
		ImageProcessor asymmetry = asymmetryGenerator.getRawHeatMap();

		ImageProcessor finalIp = new ColorProcessor(theta.getWidth(),
				theta.getHeight());

		int[] pixels = (int[]) finalIp.getPixels();
		int newPixel;

		float[] thetas = (float[]) theta.getPixels();
		float[] asymmetries = (float[]) asymmetry.getPixels();
		float hue;
		float saturation;
		for (int i = 0; i != pixels.length; i++) {

			saturation = asymmetries[i];
			// System.out.println(String.format("hue %.3f, saturation : %.3f\n",hue,saturation));
			newPixel = fromHueToRGB(thetas[i], saturation);// Color.HSBtoRGB(hue,
																			// 1-saturation, 1);
			pixels[i] = newPixel;

		}

		// ImageProcessorUtils.show(theta);
		// ImageProcessorUtils.show(asymmetry);

		return finalIp.resize(width);

	}

	/**
	 * Draw text.
	 *
	 * @param ip the ip
	 * @param f the f
	 * @param text the text
	 * @param c the c
	 * @param xc the xc
	 * @param yc the yc
	 */
	public static void drawText(ImageProcessor ip, Font f, String text, Color c,
			int xc, int yc) {
		int fontWidth = ip.getStringWidth(text);
		int fontHeight = f.getSize();

	}

	/* (non-Javadoc)
	 * @see knop.psfj.heatmap.HeatMapGenerator#getAnnotatedHeatMap()
	 */
	@Override
	public ImageProcessor getAnnotatedHeatMap() {
		ImageProcessor heatmap = getColoredHeatMap(700);
		ImageProcessor scale = generateHueScale(400);

		Font font = new Font(Font.SANS_SERIF,Font.PLAIN,heatmap.getWidth()/22);
		int margin  =  heatmap.getWidth()/15;
		ImageProcessor finalIp = new ColorProcessor(heatmap.getWidth()
				+ 800+ margin*4, heatmap.getHeight()+font.getSize()+margin*4);
		finalIp.setFont(font);
		finalIp.setColor(Color.white);
		finalIp.fill();
		
		//drawing the title
		finalIp.setColor(Color.black);
		finalIp.setJustification(ImageProcessor.CENTER_JUSTIFY);
		finalIp.drawString("Asymmetry weighted " + dataSet.getColumnName(currentColumn),margin+heatmap.getWidth()/2,margin+font.getSize());
		
		int heatmapX = margin;
		int heatmapY = margin*2+font.getSize();
		
		//drawing the map
		finalIp.copyBits(heatmap, heatmapX,heatmapY, Blitter.COPY);
		finalIp.copyBits(scale, margin*2+heatmap.getWidth() + 10, heatmapY, Blitter.COPY);

		drawScale(finalIp,heatmap.getWidth(),heatmapX+margin/2,heatmapX+heatmap.getHeight(),margin/2,margin/2);
		
		return finalIp;

	}

	/**
	 * Generate hue scale.
	 *
	 * @param width the width
	 * @return the image processor
	 */
	private static ImageProcessor generateHueScale(int width) {
		ColorProcessor ip = new ColorProcessor(width, width*2-(width/18*7));
		ip.setColor(Color.white);
		ip.fill();
		ip.setFont(new Font(Font.DIALOG, Font.BOLD, width / 18));
		
		
		int margin = width / 18;
		width = width - (margin * 3) - ip.getStringWidth("1.0");
		int height = width*2;

		int xc = margin*3+ip.getStringWidth("1.0");
		int yc = ip.getHeight()/2;

		int rainbowRadius = (width - margin * 2);

		int beadLongAxis = width / 30;

		
		ip.setAntialiasedText(true);
		
		
		int b = 0;

		double a0 = 0.3;
		double aPace = 0.2;
		double aLimit = 0.9;
		// for each asymmetry
		for (double asymmetry = a0; asymmetry <= aLimit; asymmetry += aPace) {

			// calculating the radius for the position of the bead (polar
			// coordinate)
			
			ip.setJustification(ImageProcessor.CENTER_JUSTIFY);
			
			//double r = 0.8 * rainbowRadius * ((asymmetry-0.4)*2);
			double r = 1.2 *  rainbowRadius * (1-asymmetry);
			double t = -(90 + 90) / 180 * Math.PI + Math.PI / 2;
			
			ip.setColor(Color.black);
			
			int xText = -margin-ip.getStringWidth("" + MathUtils.round(asymmetry, 2))
					+ MathUtils.round(xc + r *  cos(t));
			int yText = MathUtils.round(yc + r * sin(t)) + ip.getFont().getSize()
					/ 2;

			
			ip.drawString(MathUtils.roundToString(asymmetry, 2), xText, yText);
			//System.out.println(xText);
			
			// drawing scale
			
			for (double a = -90; a <= 90; a += 0.2) {
				t = -(a + 90) / 180 * Math.PI + Math.PI / 2;
				int xP = MathUtils.round(xc + r * cos(t));
				int yP = MathUtils.round(yc + r * sin(t));
				ip.drawPixel(xP, yP);

			}

			

			// drawing beads

			// for angle going from -90 to 90 degrees in radian
			
			double degree_margin = 1.1;
			
			for (double a = -90; a <= 90; a += 180 / 6) {

				t = -(a + 90) / 180 * Math.PI + Math.PI / 2;

				if (b == 0) {

					xText = MathUtils.round(xc + rainbowRadius * degree_margin * cos(t));
					yText = MathUtils.round(yc + rainbowRadius * degree_margin * sin(t));

					ip.setColor(Color.black);
					
					ip.drawString(MathUtils.round(a) + MathUtils.DEGREE_SYMBOL, xText, yText + 16);

				}

				// calculating the cartesian coordinates of the bead
				int xE = MathUtils.round(xc + r * cos(t));
				int yE = MathUtils.round(yc + r * sin(t));

				// color depending of asymmetry and theta
				int color = fromHueToRGB((float) a, new Double(asymmetry).floatValue());
				
				drawEllipse(ip, xE, yE, MathUtils.round(1.0 * beadLongAxis / asymmetry),
						MathUtils.round(beadLongAxis), (float) t,
						new Color(color));

			}

			b++;
		}
		Font f = ip.getFont();
		ip = (ColorProcessor)ip.rotateLeft();
		ip.setFont(f);
		ip.setAntialiasedText(true);
		
		ip.drawString("Asymmetry",yc+margin*2,MathUtils.round(xc+rainbowRadius+f.getSize()/2));
		
		ip = (ColorProcessor)ip.rotateRight();
		/*
		 * ip.drawRect(margin, margin + height / 2 + margin, width / 2, 8);
		 * 
		 * for (double asymmetry = 0; asymmetry <= 1; asymmetry += 0.5) {
		 * ip.drawString(MathUtils.roundToString(asymmetry, 2),
		 * MathUtils.round(margin + width / 2 * asymmetry), margin + height / 2 +
		 * margin + ip.getFont().getSize() * 2); }
		 */
		return ip;

	}

	/**
	 * From hue to rgb.
	 *
	 * @param angle the angle
	 * @param asymmetry the asymmetry
	 * @return the int
	 */
	public static int fromHueToRGB(float angle, float asymmetry) {
		
		float brightness = (asymmetry <= 0 ? 0 : 1);
		if(brightness ==0) return Color.HSBtoRGB(0, new Float(.5), new Float(.5));
		if(asymmetry < 0.5) asymmetry = new Float(0.5);
		asymmetry = new Float((asymmetry-0.5)/0.5);
		
		float saturation = new Float(1-asymmetry);
		//saturation = new Float(saturation+0.5)/2;
		//if(saturation < 0.5) saturation = new Float(1);
		return Color.HSBtoRGB((angle+60) / 180, saturation, brightness);
	}

	/**
	 * Draw ellipse.
	 *
	 * @param ip the ip
	 * @param xc the xc
	 * @param yc the yc
	 * @param la the la
	 * @param sa the sa
	 * @param theta the theta
	 * @param c the c
	 */
	public static void drawEllipse(ImageProcessor ip, int xc, int yc, int la,
			int sa, float theta, Color c) {

		Polygon p = new Polygon();

		ImageProcessor ellipse = ip;// ;new ColorProcessor(la+2,sa+2);
		ellipse.setColor(c);

		//ellipse.drawRect(0, 0, 20, 20);
		for (float t = 0; t <= Math.PI * 2; t += 0.01) {

			int x0 = xc;
			int y0 = yc;

			// float r = new Float(sa+(la*Math.cos(t*4)));

			double a = la;
			double b = sa;

			int x1 = x0
					+ MathUtils.round(a * cos(t) * cos(theta)
							- (b * sin(t) * sin(theta)));
			int y1 = y0
					+ MathUtils.round(a * cos(t) * sin(theta)
							+ (b * sin(t) * cos(theta)));

			// double angle =

			p.addPoint(x1, y1);

		}
		
		ip.fillPolygon(p);

	}

	/**
	 * Sin.
	 *
	 * @param d the d
	 * @return the double
	 */
	private static double sin(double d) {
		return Math.sin(d);
	}
	
	/**
	 * Cos.
	 *
	 * @param d the d
	 * @return the double
	 */
	private static double cos(double d) {
		return Math.cos(d);
	}

	/* (non-Javadoc)
	 * @see knop.psfj.heatmap.HeatMapGenerator#setBeadImage(knop.psfj.BeadImage)
	 */
	@Override
	public void setBeadImage(BeadImage image) {
		super.setBeadImage(image);
		asymmetryGenerator.setBeadImage(image);
	}

}
