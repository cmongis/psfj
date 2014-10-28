/**
 *
 *  findMax v1, 16 déc. 2008
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

/**
 * findMax is to be used to retrieve the coordinates. Coordinates are uncalibrated.
 * @author fab
 */
public class FindMax {
    /**
     *Starts the process of creation of a new findMax object
     */
    public FindMax(){

    }

    /**
     * Retrieves all coordinates for the current's ImagePlus maximum of intensity
     * @param ip ImagePlus on which to find the maximum
     * @return an array of integer of size 2 in 2D, 3 in 3D. x coordinate will be found within the array at index 0, y at index 1 and if applicable z at position 2
     */
    public int[] getAllCoordinates(ImagePlus ip){
        int[] coord;

        if (ip.getNSlices()==1){
            coord=get2DCenter(ip);
        }else{
            int[] coord2D=get2DCenter(new SideViewGenerator().getXYview(ip, SideViewGenerator.MAX_METHOD));
            coord=new int[3];
            coord[0]=coord2D[0];
            coord[1]=coord2D[1];
            coord[2]=getZmax(ip, coord[0], coord[1]);
        }
        return coord;
    }

    /**
     * Retrieves the x and y coordinates of the maximum intensity pixel on the current ImagePlus, on the current slice
     * @param ip ImagePlus on which to find the maximum
     * @return an array of integer of size 2. x coordinate will be found within the array at index 0, y at index 1.
     */
    public int[] get2DCenter(ImagePlus ip){
        int max=0;
        int[] coord=new int[2];

        for (int y=0; y<ip.getHeight(); y++){
            for (int x=0; x<ip.getWidth(); x++){
                int currVal=ip.getProcessor().getPixel(x, y);
                if (currVal>max){
                    coord[0]=x;
                    coord[1]=y;
                    max=currVal;
                }
            }
        }
        return coord;
    }

    /**
     * Retrieves the x coordinate of the maximum intensity pixel on the current ImagePlus, on the current slice and along the row of index yPos.
     * @param ip ImagePlus on which to find the maximum
     * @param yPos the coordinates of the row on which the maximum has to be found
     * @return an Integer carrying the x coordinate found
     */
    public int getXmax(ImagePlus ip, int yPos){
        int max=0;
        int coord=0;

        for (int x=0; x<ip.getWidth(); x++){
            int currVal=ip.getProcessor().getPixel(x, yPos);
            if (currVal>max){
                coord=x;
                max=currVal;
            }
        }
        return coord;
    }

    /**
     * Retrieves the y coordinate of the maximum intensity pixel on the current ImagePlus, on the current slice and along the column of index xPos.
     * @param ip ImagePlus on which to find the maximum
     * @param xPos the coordinates of the column on which the maximum has to be found
     * @return an Integer carrying the y coordinate found
     */
    public int getYmax(ImagePlus ip, int xPos){
        int max=0;
        int coord=0;

        for (int y=0; y<ip.getHeight(); y++){
            int currVal=ip.getProcessor().getPixel(xPos, y);
            if (currVal>max){
                coord=y;
                max=currVal;
            }
        }
        return coord;
    }


    /**
     * Retrieves the z coordinate of the maximum intensity pixel, knowing its x and y coordinates (provided as parameters xPos and yPos)
     * @param ip ImagePlus on which to find the maximum
     * @param xPos the coordinates of the column on which the maximum has to be found
     * @param yPos the coordinates of the row on which the maximum has to be found
     * @return an Integer carrying the z coordinate found
     */
    public int getZmax(ImagePlus ip, int xPos, int yPos){
        int max=0;
        int coord=1;

        for (int z=1; z<=ip.getNSlices(); z++){
            ip.setSlice(z);
            int currVal=ip.getProcessor().getPixel(xPos, yPos);
            if (currVal>max){
                coord=z;
                max=currVal;
            }
        }
        return coord;
    }
}
