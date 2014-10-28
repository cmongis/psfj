/**
 *
 *  dataTricks v1, 15 oct. 2009
    Fabrice P Cordelieres, fabrice.cordelieres at gmail.com

    Copyright (C) 2009 Fabrice P. Cordelieres

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

import ij.measure.Calibration;
import ij.process.ImageProcessor;

/**
 * dataTricks contains commonly used tools to work on data
 * @author fab
 */
public class DataTricks {
    /** Used in extremum retrieval to indicate that the minimum should be returned **/
    public static final int MIN=0;

    /** Used in extremum retrieval to indicate that the maximum should be returned **/
    public static final int MAX=1;


    /**
     * Rounds any double to the user provided number of digits
     * @param nb2round number to round
     * @param nbOfDigits number of digits
     * @return a rounded double
     */
    public static double round(double nb2round, int nbOfDigits){
        return Math.round(nb2round*Math.pow(10, nbOfDigits))/Math.pow(10, nbOfDigits);
    }

    /**
     * Calculates a distance (either 2D or 3D) between 2 sets of coordinates
     * @param coord1 coordinates set of the first centre
     * @param coord2 coordinates set of the second centre
     * @param cal image calibration
     * @return the calibrated distance between the pair of points
     */
    public static double dist(double[] coord1, double[] coord2, Calibration cal){
        double calX=cal.pixelWidth;
        double calY=cal.pixelHeight;
        double calZ=cal.pixelDepth;

        if (coord1.length==2){
            return Math.sqrt((coord2[0]-coord1[0])*(coord2[0]-coord1[0])*calX*calX+(coord2[1]-coord1[1])*(coord2[1]-coord1[1])*calY*calY);
        }else{
            return Math.sqrt((coord2[0]-coord1[0])*(coord2[0]-coord1[0])*calX*calX+(coord2[1]-coord1[1])*(coord2[1]-coord1[1])*calY*calY+(coord2[2]-coord1[2])*(coord2[2]-coord1[2])*calZ*calZ);
        }
    }

    /**
     * Determines the minumum value of an array of doubles
     * @param input the input double array
     * @return the minimum value, as a double
     */
    public static double min(double[] input){
        return extremum(MIN, input);
    }

    /**
     * Determines the minumum value of an array of integers
     * @param input the input double array
     * @return the minimum value, as an integer
     */
    public static int min(int[] input){
        return extremum(MIN, input);
    }

    /**
     * Determines the minumum value of an array of doubles
     * @param input the input double array
     * @return the minimum value, as a double
     */
    public static double max(double[] input){
        return extremum(MAX, input);
    }

    /**
     * Determines the minumum value of an array of integers
     * @param input the input double array
     * @return the minimum value, as an integer
     */
    public static int max(int[] input){
        return extremum(MAX, input);
    }

    /**
     * Transtypes an integer array into a double array
     * @param array the input array
     * @return the converted array
     */
    public static double[] transTypeInt2Double(int[] array){
        double[] out=new double[array.length];
        for (int i=0; i<array.length; i++) out[i]=array[i];
        return out;
    }

    private static double extremum(int type, double[] input){
        double out=input[0];
        for (int i=1; i<input.length; i++){
            switch(type){
                case MIN: out=Math.min(out, input[i]); break;
                case MAX: out=Math.max(out, input[i]); break;
            }
        }
        return out;
    }

    public static int round(double d) {
    	return Math.round(new Float(d));
    }
    
    
    private static int extremum(int type, int[] input){
        int out=input[0];
        for (int i=1; i<input.length; i++){
            switch(type){
                case MIN: out=Math.min(out, input[i]); break;
                case MAX: out=Math.max(out, input[i]); break;
            }
        }
        return out;
    }

    /**
     * Extracts the position of the first non zero value within an integer array
     * @param input the input integer array
     * @return the position of the first non zero value within the array, as an integer
     */
    public static int findFirstNonZero(int[] input){
        int out=0;
        while (input[out]==0 && out<input.length) out++;
        return out;
    }

    /**
     * Extracts the position of the last non zero value within an integer array
     * @param input the input integer array
     * @return the position of the last non zero value within the array, as an integer
     */
    public static int findLastNonZero(int[] input){
        int out=input.length-1;
        while (input[out]==0 && out>=0) out--;
        return out;
    }
    
    public static double mean(ImageProcessor ip) {
    	return mean(ip,-1);
    }
    
    
    public static double mean(ImageProcessor ip, int threshold) {
    	double mean = ip.getPixel(0,0);
    	int count = 0;
    	double pixel;
    	
    	for(int x = 0;x!=ip.getWidth();x++) {
    		for(int y = 0;y!=ip.getHeight();y++) {
    			
    			pixel = ip.getPixel(x, y);
    			if(pixel > threshold) {
	    			if(count > 1)
	    				mean = mean + (pixel-mean) / count;
	    			
	    			else if(count == 1) 
	    				mean = (pixel+mean)/2;
	    			else 
	    				mean = pixel;
	    			
	    			count++;
    			}
    		}
    	}
    	
    	
    	return mean;
    }
    
}
