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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class IniFile.
 */
public class IniFile extends HashMap<String, Object> {
	
	
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		IniFile ini = new IniFile("/home/cyril/test2.ini");
		System.out.println(ini.getStringValue("salut", "les", "m"));
		ini.save();
		
	}
	
	
	/** The address. */
	private String address;
	
	/** The auto save. */
	boolean autoSave = false;
	
	/** The section re. */
	private Pattern sectionRe = Pattern.compile("\\[([\\w\\d\\s\\_\\-]*)\\].*");
	//private Pattern sectionRe = Pattern.compile("([bla]{0,20})");
	/** The string var re. */
	private Pattern stringVarRe = Pattern.compile("^([\\w\\d\\_\\-\\.]*)\\s=\\s?(.*)$");
	
	
	/** The current section. */
	private String currentSection;
	
	/**
	 * Instantiates a new ini file.
	 */
	public IniFile() {}
	
	/**
	 * Instantiates a new ini file.
	 *
	 * @param a the a
	 */
	public IniFile(String a) {
		address = a;
		mergeFile(a);
	}
	
	/**
	 * Sets the auto save.
	 *
	 * @param autoSave the new auto save
	 */
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
	
	/**
	 * Merge file.
	 *
	 * @param file the file
	 */
	public void mergeFile(String file) {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(file);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				handleLine(strLine);
			}
			// Close the input stream
			in.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("The ini file doesn't exist. It will be created when saving.");
		}
		
		catch (Exception e) {// Catch exception if any
			
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/* Parsing related Methods */
	
	/**
	 * Handle line.
	 *
	 * @param line the line
	 */
	private void handleLine(String line) {
		if(sectionRe.matcher(line) != null) handleSection(line);
		if(stringVarRe.matcher(line) != null) handleStringKey(line);
	}
	
	/**
	 * Handle section.
	 *
	 * @param line the line
	 */
	public void handleSection(String line) {
		Matcher m = sectionRe.matcher(line);
		if(m.find()) {
			currentSection = m.group(1);
		}
		
	}
	
	/**
	 * Handle string key.
	 *
	 * @param line the line
	 */
	public void handleStringKey(String line) {
		Matcher m = stringVarRe.matcher(line);
		if(m.find()) {
			addStringValue(currentSection, m.group(1), m.group(2));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	public String toString() {
		String result = "";
		for(String section : keySet()) {
			result += get(section).toString();
		}
		return result;
	}
	
	/* Data access related methods */
	
	
	/**
	 * Adds the string value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param value the value
	 */
	public void addStringValue(String section, String key, String value) {

		getSection(section).addStringValue(key, value);
		if(autoSave) {
			save();
		}
	}

	/**
	 * Gets the string value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param def the def
	 * @return the string value
	 */
	public String getStringValue(String section, String key, String def) {
		return getSection(section).getStringValue(key, def);
	}

	/**
	 * Gets the string value.
	 *
	 * @param section the section
	 * @param key the key
	 * @return the string value
	 */
	public String getStringValue(String section, String key) {
		return getSection(section).getStringValue(key, "");
	}
	
	/**
	 * Gets the double value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param def the def
	 * @return the double value
	 */
	public Double getDoubleValue(String section, String key, int def) {
		return getDoubleValue(section,key,new Double(def));
	}
	
	/**
	 * Gets the double value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param def the def
	 * @return the double value
	 */
	public Double getDoubleValue(String section, String key, Double def) {
		try {
			return Double.parseDouble(getSection(section).getStringValue(key, def.toString()));
		}
		catch(Exception e) {
			System.err.println(String.format("Error when loading value [%s] : %s",section,key));
			return def;
		}
	}
	
	/**
	 * Gets the section.
	 *
	 * @param section the section
	 * @return the section
	 */
	public Section getSection(String section) {
		if (containsKey(section) == false) {
			Section sectionObj = new Section(section);
			put(section,sectionObj);
			return sectionObj;
		}

		return (Section) get(section);
	}

	
	/**
	 * Adds the integer value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param value the value
	 */
	public void addIntegerValue(String section, String key, Integer value) {
		addStringValue(section,key, value.toString());
		
	}
	
	/**
	 * Gets the integer value.
	 *
	 * @param section the section
	 * @param key the key
	 * @param def the def
	 * @return the integer value
	 */
	public Integer getIntegerValue(String section, String key, Integer def) {
		String v = getStringValue(section,key,"");
		if (v == "") {
			return def;
		}
		else {
			return Integer.decode(v);
		}
	}
	
	/**
	 * Exists.
	 *
	 * @return true, if successful
	 */
	public boolean exists() {
		return (new File(address)).exists();
	}
	
	/**
	 * Save.
	 */
	public void save() {
		saveAs(address);
	}
	
	/**
	 * Save as.
	 *
	 * @param address the address
	 */
	public void saveAs(String address) {
		if(address == null) {
			System.err.println("No address has been set for this object. Please use saveAs function.");
		}
		else {
			try{
				  // Create file 
				
				  try {
				
				  File p  = new File((new File(address)).getParent());
				  if(p.exists() == false) {
					  p.mkdirs();
				  }
				  
				  }
				  catch(NullPointerException e) {
					  System.err.println("The parent directory can not be created. Trying to write the file anyway...");
				  }
				  
				  FileWriter fstream = new FileWriter(address);
				  BufferedWriter out = new BufferedWriter(fstream);
				  out.write(toString());
				  out.close();
			}
			catch(Exception e) {
				System.err.println("Couldn't write the configuration file !");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The Class Section.
	 */
	private class Section extends HashMap<String, Object> {

		/** The name. */
		String name;

		/**
		 * Instantiates a new section.
		 *
		 * @param name the name
		 */
		public Section(String name) {
			this.name = name;
		}

		/**
		 * Adds the string value.
		 *
		 * @param key the key
		 * @param value the value
		 */
		public void addStringValue(String key, String value) {
			put(key, value);
		}

		/**
		 * Gets the string value.
		 *
		 * @param key the key
		 * @param def the def
		 * @return the string value
		 */
		public String getStringValue(String key, String def) {
			if (containsKey(key) == false) {
				return def;
			} else {
				String toReturn;
				try {
					toReturn = (String) get(key);
				} catch (Exception e) {
					System.err.printf(
							"The key \"%s\" doesn't contains a string.", key);
					toReturn = def;
				}
				return toReturn;
			}
		}
		
		
		
		
		/* (non-Javadoc)
		 * @see java.util.AbstractMap#toString()
		 */
		public String toString() {
			String result = "["+this.name+"]\n";
			
			for(String key : keySet()) {
				result += key + " = "  + get(key)+"\n";
			}
			result+="\n";
			return result;
		}
	}

	
}
