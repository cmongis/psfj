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
package knop.psfj.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


// TODO: Auto-generated Javadoc
/**
 * The Class MathUtils.
 */
public class MathUtils {

	
	
	/** The micrometers. */
	public static String MICROMETERS = "um";
	
	/** The degrees. */
	public static String DEGREES = "degrees";
	
	/** The degree symbol. */
	public static String DEGREE_SYMBOL = "°";
	
	/** The radiant. */
	public static String RADIANT = "rad";
	
	/** The pixels. */
	public static String PIXELS = "pixels";
	
	/** The pixel. */
	public static String PIXEL = "pixel";
	
	/** The plus minus. */
	public static String PLUS_MINUS = "+/-";
	//public static String PLUS_MINUS = "±";
	/** The units. */
	public static String[] units = new String[] { "m", "mm", MICROMETERS, "nm", "pm" };

	/**
	 * Round.
	 *
	 * @param d the d
	 * @return the int
	 */
	public static int round(double d) {
		return (int) Math.round(d);
			
		
		
		//return Math.round(new Float(d));
	}

	
	
	/**
	 * Round.
	 *
	 * @param nb2round the nb2round
	 * @param nbOfDigits the nb of digits
	 * @return the double
	 */
	public static double round(double nb2round, int nbOfDigits) {
		return Math.round(nb2round * Math.pow(10, nbOfDigits))
				/ Math.pow(10, nbOfDigits);
	}

	/**
	 * Round to string.
	 *
	 * @param nb2round the nb2round
	 * @param nbOfDigits the nb of digits
	 * @return the string
	 */
	public static String roundToString(double nb2round, int nbOfDigits) {
		if(nbOfDigits == 0) return ""+Math.round(nb2round);
		return new Double(round(nb2round, nbOfDigits)).toString();
	}

	/**
	 * Gets the lower unit.
	 *
	 * @param unit the unit
	 * @return the lower unit
	 */
	public static String getLowerUnit(String unit) {
		for (int i = 0; i != units.length; i++) {
			if (unit.equals(units[i]) && i != units.length - 1) {
				return units[i + 1];
			}
		}
		//System.err.println("Error when getting lower unit. Returning");
		return unit;
	}
	
	
	/**
	 * Format double.
	 *
	 * @param number the number
	 * @param unit the unit
	 * @return the string
	 */
	public static String formatDouble(double number, String unit) {
		return formatDouble(number, unit,-1);
	}
	
	/**
	 * Format double.
	 *
	 * @param number the number
	 * @param unit the unit
	 * @param numbersAfterDot the numbers after dot
	 * @return the string
	 */
	public static String formatDouble(double number, String unit,int numbersAfterDot) {
		if(unit.equals("")) {
			if(numbersAfterDot < 0) numbersAfterDot = 3;
			return MathUtils.roundToString(number, numbersAfterDot);
		}
		
		
		//
		else if (Math.abs(number) >= 100) {
			if(numbersAfterDot < 0 ) numbersAfterDot = 0;
			return MathUtils.roundToString(number, numbersAfterDot) + " " + unit;
		} else if (Math.abs(number)  < 100 && Math.abs(number)  > 10) {
			
			if(numbersAfterDot < 0 ) numbersAfterDot = 1;
			String result = MathUtils.roundToString(number, numbersAfterDot);
			
			/*
			if((1.0*MathUtils.round(number)) - number == 0.0) {
				result = ""+round(number);
			}*/
			return result + " " + unit;

		}

		else if (Math.abs(number)  < 10 && Math.abs(number)  > 1) {
			if(numbersAfterDot < 0) numbersAfterDot = 2;
			return MathUtils.roundToString(number, numbersAfterDot) + " " + unit;
		}
		else {
			
			if(getLowerUnit(unit).equals(unit)) {
				return MathUtils.roundToString(number, 0);
			}
			return formatDouble(number * 1000, getLowerUnit(unit),numbersAfterDot);
		}
	}

	/**
	 * Format statistics.
	 *
	 * @param stats the stats
	 * @param unit the unit
	 * @return the string
	 */
	public static String formatStatistics(DescriptiveStatistics stats, String unit) {
      String value = MathUtils.formatDouble(stats.getMean(), unit);
      System.out.println("number after dot : "+getNumberAfterDot(value));
      String variation = MathUtils.formatDouble(stats.getStandardDeviation(), unit,getNumberAfterDot(value));
      
      
      

      return String.format("%s %s %s",value,PLUS_MINUS,variation) ;
  }
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		System.out.println(MathUtils.round(1.44));
		System.out.println(MathUtils.round(1.49999));
		System.out.println(MathUtils.formatDouble(0.1, "um"));
		System.out.println(MathUtils.formatDouble(0.0034553,MICROMETERS));
		System.out.println(MathUtils.formatDouble(0.033454,MICROMETERS));
		System.out.println(MathUtils.formatDouble(0.31123,MICROMETERS));
		System.out.println(MathUtils.formatDouble(3.1123,MICROMETERS));
		System.out.println(MathUtils.formatDouble(30.05,MICROMETERS));
		System.out.println(MathUtils.formatDouble(300.404,MICROMETERS));
		System.out.println(MathUtils.formatDouble(17.0,MICROMETERS,1));
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for(int i = 0; i!= 100;i++) {
			stats.addValue(100 + 1.0*i/12);
		}
		System.out.println(stats);
		System.out.println(MathUtils.formatStatistics(stats, DEGREES));
		System.out.println(MathUtils.getNumberAfterDot("12.534 nm"));
		
		System.out.println(MathUtils.getNumberAfterDot("12.53"));
		System.out.println(MathUtils.getNumberAfterDot("12 nm"));
		System.out.println(MathUtils.getNumberAfterDot("12.34 nm"));
	}
	
	/**
	 * Checks if is metric unit.
	 *
	 * @param unit the unit
	 * @return true, if is metric unit
	 */
	public static boolean isMetricUnit(String unit) {
		for (String u : units) {
			
			if(u.equals(unit)) return true;
		}
		return false;
	}
	
	
	/**
	 * Gets the number after dot.
	 *
	 * @param number the number
	 * @return the number after dot
	 */
	public static int getNumberAfterDot(String number) {
		
		System.out.println(number + " -> ");
		
		int dotPosition = number.indexOf(".");
		int spacePosition = number.indexOf(" ") - 1;
		
		if(dotPosition < 0) return 0;
		if(spacePosition < 0) spacePosition = number.length()-1;
		return spacePosition - dotPosition;
		
		
		
		
	}
	
	
}
