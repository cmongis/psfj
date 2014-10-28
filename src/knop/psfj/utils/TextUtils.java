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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class TextUtils.
 */
public class TextUtils {
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		HashMap<String,String> patterns = new HashMap<String, String>();
		
		patterns.put("%NA", "0.664");
		
		System.out.println(TextUtils.readTextRessource(patterns, "/mini-report.html", patterns));
		
		
	}
	
	
	/**
	 * Write string to file.
	 *
	 * @param file the file
	 * @param data the data
	 * @param append the append
	 */
	public static void writeStringToFile(String file, String data,
			boolean append) {
		FileWriter fstream;
		try {
			fstream = new FileWriter(file, append);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Read text ressource.
	 *
	 * @param source the source
	 * @param path the path
	 * @return the string
	 */
	public static String readTextRessource(Object source, String path) {
		BufferedReader bReader;
		try {
			FileReader reader = new FileReader("src" + path);
			bReader = new BufferedReader(reader, 1000000);

		} catch (Exception e) {
			InputStreamReader reader = new InputStreamReader(source.getClass()
					.getResourceAsStream(path));
			bReader = new BufferedReader(reader);
		}
		System.gc();
		int count = 0;

		StringBuffer buffer = new StringBuffer(900000);
		try {

			String line = "";
			while (line != null) {
				line = bReader.readLine();
				if (line != null) {
					buffer.append(line + "\n");
				}
			}

			bReader.close();
			bReader = null;
			System.gc();

			return buffer.toString();

		} catch (Exception e) {
			System.out.println("Couldn't load "+path);
			e.printStackTrace();
			return null;
			
		}
	}

	
	/**
	 * Read text ressource.
	 *
	 * @param source the source
	 * @param path the path
	 * @param toReplace the to replace
	 * @return the string
	 */
	public static String readTextRessource(Object source, String path, HashMap<String,String> toReplace) {
		BufferedReader bReader;
		try {
			FileReader reader = new FileReader("src" + path);
			bReader = new BufferedReader(reader, 1000000);

		} catch (Exception e) {
			InputStreamReader reader = new InputStreamReader(source.getClass()
					.getResourceAsStream(path));
			bReader = new BufferedReader(reader);
		}
		System.gc();
		int count = 0;

		StringBuffer buffer = new StringBuffer(900000);
		try {

			String line = "";
			while (line != null) {
				line = bReader.readLine();
				if (line != null) {
					for(String pattern : toReplace.keySet()) {
						if(line.contains(pattern)) {
							
							if(toReplace.get(pattern) == null) 
								toReplace.put(pattern,"null");
							
							String replacedString = line.replace(pattern,toReplace.get(pattern));
							if(replacedString != null) {
								line = replacedString;
							}
						}
					}
					buffer.append(line + "\n");
				}
			}

			bReader.close();
			bReader = null;
			System.gc();

			return buffer.toString();

		} catch (Exception e) {
			System.out.println("Couldn't load "+path);
			e.printStackTrace();
			return null;
			
		}
	}
	
	
	/**
	 * Read file.
	 *
	 * @param file the file
	 * @return the string
	 */
	public static String readFile(String file) {
		String result = "";
		System.out.println("Reading file " + file + "...");
		try {
			FileReader reader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(reader);
			String line = "";
			while (line != null) {

				line = bReader.readLine();
				if (line != null) {
					result += line + "\n";
				}
			}

			bReader.close();
			reader.close();
			reader = null;
			bReader = null;

			System.gc();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("File read.");

		return result;
	}
	
}
