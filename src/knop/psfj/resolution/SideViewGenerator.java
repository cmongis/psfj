/**
 *
 *  sideViewGenerator v1, 16 déc. 2008
    Fabrice P Cordelieres, fabrice.cordelieres at gmail.com

    Copyright (C) 2008 Fabrice P. Cordelieres

    License:
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package knop.psfj.resolution;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.plugin.Slicer;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;

/**
 * Starting from a stack provided as an ImagePlus, sideViewGenerator will generate the three side views (orthogonal), as MIP (xy, xz and yz).
 * @author Fabrice Cordelières
 */
public class SideViewGenerator {
    public static final int XZ_VIEW=0;
    public static final int YZ_VIEW=1;

    public static final int AVG_METHOD = 0;
    public static final int MAX_METHOD = 1;
    public static final int MIN_METHOD = 2;
    public static final int SUM_METHOD = 3;
    public static final int SD_METHOD = 4;
    public static final int MEDIAN_METHOD = 5;


    /**
     *
     */
    public SideViewGenerator(){
    }

    /**
     * getXYview returns the "top-view" of a stack
     * @param ip the ImagePlus of the stack to be represented
     * @param projType projection type as an integer
     * @return an ImagePlus containing the xy view
    */
     public ImagePlus getXYview(ImagePlus ip, int projType){
        Calibration cal=ip.getCalibration();
        ip.setCalibration(new Calibration());
        
        ZProjector zp=new ZProjector(ip);
        zp.setMethod(projType);
        zp.doProjection();
        ImagePlus output=zp.getProjection();

        ip.setCalibration(cal);
        output.setCalibration(cal);

        return output;
    }

    /**
     * getXZview returns a "side-view" of a stack
     * @param ip the ImagePlus of the stack to be represented
     * @param projType projection type as an integer
     * @param keepCalibration true to take into account the disparity of calibration in XY/Z, false otherwise
     * @return an ImagePlus containing the xz view
    */
     public ImagePlus getXZview(ImagePlus ip, int projType, boolean keepCalibration){
        return sideView(ip, projType, keepCalibration, XZ_VIEW);
    }

    /**
     * getYZview returns a "side-view" of a stack
     * @param ip the ImagePlus of the stack to be represented
     * @param projType projection type as an integer
     * @param keepCalibration true to take into account the disparity of calibration in XY/Z, false otherwise
     * @return an ImagePlus containing the yz view
    */
     public ImagePlus getYZview(ImagePlus ip, int projType, boolean keepCalibration){
        return sideView(ip, projType, keepCalibration, YZ_VIEW);
    }

     /**
      * getPanelView generates and returns a panel containing the XY, YZ and XZ view (MIP)
      * @param ip the ImagePlus of the stack from hich to extract the panel
      * @param projType projection type as an integer
      * @param keepCalibration true to take into account the disparity of calibration in XY/Z, false otherwise
      * @param addScaleBar true to draw a scale bar on the XY view
      * @param size size of the scale bar in the Calibration's unit
      * @param addCross true to draw a cross on all views
      * @param coordCross array of integers containing the 3 coordinates to be used for drawing the cross
      * @param crossRadius width and height of the cross to be drawn
      * @return an ImagePlus containing the panel view
      */
     public ImagePlus getPanelView(ImagePlus ip, int projType, boolean keepCalibration, boolean addScaleBar, int size, boolean addCross, double[] coordCross, int crossRadius){
         Font font=new Font("Times New Roman", Font.BOLD, 12);
         Calibration cal=ip.getCalibration();
         double xzRatio=cal.pixelDepth/cal.pixelWidth;
         double yzRatio=cal.pixelDepth/cal.pixelHeight;

         ImageProcessor xy=getXYview(ip, projType).getProcessor();
         if (addCross){
             int[] coord=new int[2];
             coord[0]=(int) (coordCross[0]+.5);
             coord[1]=(int) (coordCross[1]+.5);
             addCross(xy, coord, crossRadius);
         }
         xy.setColor(Color.white);
         xy.setFont(font);
        // xy.drawString("XY", 3, 15);
        // if (addScaleBar) addScaleBar(xy, cal, size);
         
         if(ip.getStack().getSize() == 1) {
         	return new ImagePlus("",new ByteProcessor(20,20));
         }
         ImageProcessor xz=getXZview(ip, projType, keepCalibration).getProcessor();
         if (addCross){
             int[] coord=new int[2];
             coord[0]=(int) (coordCross[0]+.5);
             coord[1]=keepCalibration? (int) (xzRatio*(coordCross[2]+.5)):(int) (coordCross[2]+.5);
             addCross(xz, coord, crossRadius);
         }
         xz.setColor(Color.white);
         xz.setFont(font);
        // xz.drawString("XZ", 3, 15);
         
         ImageProcessor yz=getYZview(ip, projType, keepCalibration).getProcessor().rotateRight();
         yz.flipHorizontal();
         if (addCross){
             int[] coord=new int[2];
             coord[0]=keepCalibration? (int) (yzRatio*(coordCross[2]+.5)):(int) (coordCross[2]+.5);
             coord[1]=(int) (coordCross[1]+.5);
             addCross(yz, coord, crossRadius);
         }
         yz.setColor(Color.white);
         yz.setFont(font);
        // yz.drawString("YZ", 3, 15);
         

         ImageProcessor iproc=xy.createProcessor(xy.getWidth()+10+yz.getWidth(), xy.getHeight()+10+xz.getHeight());
         iproc.setColorModel(iproc.getDefaultColorModel());
         iproc.setColor(Color.white);
         iproc.fill();
         iproc.insert(xy, 0, 0);
         iproc.insert(yz, xy.getWidth()+10, 0);
         iproc.insert(xz, 0, xy.getHeight()+10);
         
         return new ImagePlus ("Panel view", iproc);
     }

     /**
      * Generates either the XZ or YZ side-view over the ImagePlus, taking into account the calibration or not
      * @param ip the ImagePlus of the stack to be represented
      * @param projection type as an integer
      * @param keepCalibration true to take into account the disparity of calibration in XY/Z, false otherwise
      * @param view should be 0 for XZ view and 1 for YZ view
      * @return an ImagePlus containing the view
      */
     private ImagePlus sideView(ImagePlus ip, int projType, boolean keepCalibration, int view){
        Calibration cal=ip.getCalibration();

        ip.setCalibration(new Calibration());
        
        ImagePlus reslicedStack=null;
        
        if (view==XZ_VIEW){
            reslicedStack=new Slicer().reslice(ip);
        }else{
            for (int i=0; i<ip.getWidth(); i++){
                Line line=new Line(i, 0, i, ip.getHeight()-1);
                ip.setRoi(line);
                ImagePlus slice=new Slicer().reslice(ip);
                if (i==0) reslicedStack=NewImage.createImage("YZ view", slice.getWidth(), slice.getHeight(), ip.getWidth(), slice.getBitDepth(), NewImage.FILL_BLACK);
                reslicedStack.setSlice(i+1);
                reslicedStack.setProcessor("YZ view", slice.getProcessor());
            }
            ip.killRoi();
        }
        ip.setCalibration(cal);
        
        ZProjector zp=new ZProjector(reslicedStack);
        zp.setMethod(projType);
        zp.doProjection();

        ImagePlus output=zp.getProjection();
        
        if (keepCalibration){
            ImageProcessor iproc=output.getProcessor();
            iproc.setInterpolate(true);

            if (view==XZ_VIEW){
                iproc=iproc.resize(output.getWidth(), (int) (output.getHeight()*cal.pixelDepth/cal.pixelWidth));
            }else{
                iproc=iproc.resize(output.getWidth(), (int) (output.getHeight()*cal.pixelDepth/cal.pixelHeight));
            }
            output=new ImagePlus("sideView", iproc);
        }else{
            if (view==XZ_VIEW){
                cal.pixelHeight=cal.pixelDepth;
                cal.pixelDepth=1;
            }else{
                cal.pixelWidth=cal.pixelHeight;
                cal.pixelHeight=cal.pixelDepth;
                cal.pixelDepth=1;
            }
            output.setCalibration(cal);
        }

        return output;
     }

     /**
      * Draws a scale bar on the ImageProcessor used as argument (adapted from the original ImageJ ScaleBar class)
      * @param ip ImageProcessor on which to draw the scale bar
      * @param cal Calibration of the current ImageProcessor
      * @param barWidth width of the scale bar expressed in the current Calibration's units
      */
     public void addScaleBar(ImageProcessor ip, Calibration cal, int barWidth){
         int fraction =20;

         int barWidthInPixels=(int) (barWidth/cal.pixelWidth);
         int barHeightInPixels=4;

         String barString=barWidth+" "+cal.getUnits();

         int stringWidth=ip.getStringWidth(barString);
         int fontSize=12;

         int width=ip.getWidth();
         int height=ip.getHeight();
         int x=width-width/fraction-barWidthInPixels;
         int y=height-height/fraction-barHeightInPixels-fontSize;
         int xOffset=(int) (barWidthInPixels-stringWidth)/2;
         int yOffset=(int) (barHeightInPixels+fontSize+fontSize/4);

         ip.setColor(Color.white);
         ip.setRoi(x, y, barWidthInPixels, barHeightInPixels);
         ip.fill();
         ip.drawString(barString, x+xOffset, y+yOffset);
     }

     /**
      * Draws a cross on the ImageProcessor used as argument
      * @param ip ImageProcessor on which to draw the cross
      * @param coord a 2D array containing the coordinated of the centre of the cross
      * @param radius width/height of the cross
      */
     public void addCross(ImageProcessor ip, int[] coord, int radius){
         ip.setColor(Color.white);
         ip.setLineWidth((int) Math.max(2, Math.max(ip.getWidth(), ip.getHeight())/500));
         ip.multiply(0.5);
         ip.drawLine(coord[0], (int) (coord[1]-radius), coord[0], (int) (coord[1]+radius));
         ip.drawLine((int) (coord[0]-radius), coord[1], (int) (coord[0]+radius), coord[1]);
     }


}
