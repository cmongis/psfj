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

package knop.psfj;

import java.util.Collection;
import java.util.prefs.Preferences;

// TODO: Auto-generated Javadoc
/**
 * The Class PSFj.
 */
public class PSFj {
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public static String getVersion() {
		return "2.5, build 245";
	}
	
	
	
	/** The fwhm key. */
	public static String[] FWHM_KEY = new String[] {
		"fwhmX","fwhmY","fwhmZ"
	};
	
	/** The chr shift key. */
	public static String[] CHR_SHIFT_KEY = new String[] {
		"deltaX","deltaY","deltaZ"
	};
	
	/** The xc. */
	public static String XC = "x";
	
	/** The yc. */
	public static String YC = "y";
	
	/** The zc. */
	public static String ZC = "z";
	
	/** The radius. */
	public static String RADIUS =  "r";
	
	/** The chr shift xy. */
	public static String CHR_SHIFT_XY = "deltaD";
	
	/** The chr shift xyz. */
	public static String CHR_SHIFT_XYZ = "delta3D";
	
	/** The z profile. */
	public static String Z_PROFILE = "z_profile";

	/** The Z0_ zmean. */
	public static String Z0_ZMEAN = "z0 - mean(z0)";
	
	
	/** The asymmetry key. */
	public static String ASYMMETRY_KEY = "asymmetry";
	
	/** The theta key. */
	public static String THETA_KEY = "theta";
	
	/** The theta in radian key. */
	public static String THETA_IN_RADIAN_KEY = "theta_rad";
	
	/** The Constant FRAME_SIZE_KEY. */
	public final static String FRAME_SIZE_KEY = "frame size";
	
	/** The Constant THRESHOLD_KEY. */
	public final static String THRESHOLD_KEY = "threshold";
	
	/** The Constant SIGMA_X_KEY. */
	public final static String SIGMA_X_KEY = "sigma x";
	
	/** The Constant SIGMA_Y_KEY. */
	public final static String SIGMA_Y_KEY = "sigma y";
	
	/** Centroid brightness */
        public final static String CENTROID_BRIGHTNESS_KEY = "centroid brightness";
        
        /** Centroid brightness name */
        public final static String CENTROID_BRIGHTNESS_NAME = "Centroid brightness";
        
        /** Fitted brightness */
        public final static String FITTED_BRIGHTNESS = "Fitted Brightness(A)";
        
        /** Fitted background */
        public final static String FITTED_BACKGROUND = "Fitted background(B)";
        
	/** The Constant NORMALIZED. */
	public static final int NORMALIZED = 1;
	
	/** The Constant NOT_NORMALIZED. */
	public static final int NOT_NORMALIZED = 2;
	
	/** The Constant AXES. */
	public static final int[] AXES = new int[] { 0,1,2 };
	
	/** The Constant X_AXIS. */
	public static final int X_AXIS = 0;
	
	/** The Constant Y_AXIS. */
	public static final int Y_AXIS = 1;
	
	/** The Constant Z_AXIS. */
	public static final int Z_AXIS = 2;

	/** The Constant R_COEFF_KEY. */
	public static final String[] R_COEFF_KEY = new String[] {
		"r_coeff_x","r_coeff_y","r_coeff_z"
	};

	/** The Constant BEAD_ID. */
	public static final String BEAD_ID = "bead";

	/** The Constant IS_FITTING_VALID. */
	public static final String IS_FITTING_VALID = "isValid";

	/** The Constant WAVELENGHT_KEY. */
	public static final String WAVELENGHT_KEY = "wavelegnth";
	
	/** The Constant BEAD_MONTAGE_KEY. */
	public static final String BEAD_MONTAGE_KEY = "montage";

	public static final String SOURCE_IMAGE = "source";
	
	/**
	 * Gets the heatmap name.
	 *
	 * @param column the column
	 * @param axe the axe
	 * @param channel the channel
	 * @return the heatmap name
	 */
	public static String getHeatmapName(String[] column, int axe, int channel) {
		if(column == CHR_SHIFT_KEY) return column[axe];
		else {
			return getHeatmapName(column[axe],channel);
		}
	}
	
	/**
	 * Gets the heatmap name.
	 *
	 * @param column the column
	 * @param channel the channel
	 * @return the heatmap name
	 */
	public static String getHeatmapName(String column, int channel) {
		if(channel < 0) return column;
		else return column + "_"+channel;
	}
	
	/**
	 * Gets the column id.
	 *
	 * @param column the column
	 * @param axe the axe
	 * @param normalized the normalized
	 * @param channel the channel
	 * @return the column id
	 */
	public static String getColumnID(String[] column, int axe, int normalized, int channel) {
		return getColumnID(column[axe], normalized, channel);
	}
	
	/**
	 * Gets the column id.
	 *
	 * @param column the column
	 * @param normalized the normalized
	 * @param channel the channel
	 * @return the column id
	 */
	public static String getColumnID(String column, int normalized,int channel) {
		return column+(channel == 0 ? "" : "_ch1")+((normalized == NORMALIZED) ? "_norm" :  "");
	}

	/**
	 * Gets the column name.
	 *
	 * @param column the column
	 * @param axe the axe
	 * @param normalized the normalized
	 * @return the column name
	 */
	public static String getColumnName(String[] column, int axe,
			int normalized) {
		// TODO Auto-generated method stub
		return getColumnID(column,axe,normalized,0);
	}
	
	/**
	 * Gets the column id.
	 *
	 * @param column the column
	 * @param normalized the normalized
	 * @return the column id
	 */
	public static String getColumnID(String column, int normalized) {
		return getColumnID(column, normalized, 0);
	}
	
	
	/**
	 * Gets the preferences.
	 *
	 * @return the preferences
	 */
	public static Preferences getPreferences() {
		return  Preferences.userRoot().node("/PSFj25");
	}
	
	/**
	 * Load configuration.
	 *
	 * @param key the key
	 * @param def the def
	 * @return the string
	 */
	public static String loadConfiguration(String key, String def) {
		return getPreferences().get(key, def);
	}
	
	/**
	 * Save configuration.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public static void saveConfiguration(String key, String value) {
		getPreferences().put(key, value);
	}


	/**
	 * Gets the default home directory.
	 *
	 * @return the default home directory
	 */
	public static String getDefaultHomeDirectory() {
		return System.getProperty("user.home");
	}
	
	
	public static boolean testList(Boolean... testList) {
		return testList(testList);
	}
	
	public static boolean testList(Collection <Boolean> testList) {
		for(Boolean test : testList) {
			if(!test) return false;
		}
		return true;
	}
	
}
