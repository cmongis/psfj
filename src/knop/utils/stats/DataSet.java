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
package knop.utils.stats;



import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import knop.psfj.utils.MathUtils;
import knop.psfj.utils.TextUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import au.com.bytecode.opencsv.CSVReader;

// TODO: Auto-generated Javadoc
/**
 * The Class DataSet.
 */
public class DataSet extends HashMap<String, ArrayList<Object>> {

	/** The image name. */
	String imageName;
	
	/** The image path. */
	String imagePath;
	
	/** The image full path. */
	String imageFullPath;

	/** The column order. */
	ArrayList<String> columnOrder = new ArrayList<String>();

	/** The separator. */
	char separator = ',';
	
	/** The file address. */
	String fileAddress;
	
	/** The metadata. */
	HashMap<String, String> metadata = new HashMap<String, String>();
	
	/** The metadata order. */
	ArrayList<String> metadataOrder = new ArrayList<String>();

	/** The statistics. */
	HashMap<String, DescriptiveStatistics> statistics = new HashMap<String, DescriptiveStatistics>();

	/** The no data. */
	public static Object NO_DATA = "";
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		DataSet fdt = new DataSet();

		// fdt.importFile("/home/cyril/data.txt");

		Random rn = new Random();

		for (int i = 0; i != 10; i++) {
			
			
		
			fdt.addValue("c1",i);
			if(i % 2 == 0) fdt.addValue("c2",2);
			if(i % 3 == 0) fdt.addValue("c3",3);
			if(i % 4 == 0) fdt.addValue("c4",4);
			if(i % 5 == 0) fdt.addValue("c5",5);
			/*
			fdt.addValue("fwhmX", rn.nextDouble() * 3);
			fdt.addValue("fwhmY", rn.nextDouble() * 2);
			fdt.addValue("fwhmZ", rn.nextDouble() * 5);*/
			
		}
		
		
		//fdt.addValue("control", "positive");
	
		//fdt.threshold("x", 2, 0);
		//fdt.setTheoriticalValue("fwhmX", 3.2);
		//fdt.setTheoriticalValue("fwhmY", 2.3);
		//fdt.setTheoriticalValue("fwhmZ", 4.5);

		System.out.println(fdt.exportToString());
		System.out.println(fdt.exportToString());
		System.out.println(fdt.exportToString());
	//	System.out.println(fdt.getColumnStatistics("y"));

	}

	/**
	 * Instantiates a new data set.
	 */
	public DataSet() {
		super();
	}

	/**
	 * Instantiates a new data set.
	 *
	 * @param file the file
	 */
	public DataSet(String file) {
		super();
		importFile(file);
		fileAddress = file;
	}

	/**
	 * Gets the column list.
	 *
	 * @return the column list
	 */
	public ArrayList<String> getColumnList() {

		return columnOrder;
	}

	/**
	 * Gets the column size.
	 *
	 * @return the column size
	 */
	public int getColumnSize() {
		if (getColumnCount() > 0) {
			return getColumnSize(columnOrder.get(0));
		} else {
			return 0;
		}
	}

	/**
	 * Gets the column size.
	 *
	 * @param column the column
	 * @return the column size
	 */
	public int getColumnSize(String column) {
		return getList(column).size();
	}

	/**
	 * Adds the value.
	 *
	 * @param column the column
	 * @param value the value
	 */
	public void addValue(String column, Object value) {
		verifyLevel(column);
		getList(column).add(value);
		// getColumnStatistics(column).addValue(value);
	}

	/**
	 * Adds the value.
	 *
	 * @param column the column
	 * @param value the value
	 */
	public void addValue(String column, double value) {
		addValue(column,new Double(value));
	}
	
	/*
	 * 
	 * public DescriptiveStatistics getColumnStatistics(String column) {
	 * if(statistics.containsKey(column) == false) {
	 * System.out.println("creating new column "+column);
	 * statistics.put(column,new DescriptiveStatistics()); } return
	 * statistics.get(column); }
	 */

	/**
	 * Adds the value.
	 *
	 * @param n the n
	 * @param value the value
	 */
	public void addValue(int n, double value) {
		if (n < columnOrder.size()) {
			verifyLevel(getColumnName(n));
			getList(n).add(new Double(value));
			statistics.get(getColumnName(n)).addValue(value);
		} else {
			System.err.println(String.format(
					"Column number %d doesn't exist !", n));
		}
	}

	/**
	 * Gets the value.
	 *
	 * @param column the column
	 * @param i the i
	 * @return the value
	 */
	public Object getValue(String column, int i) {
		try {
			return getList(column).get(i);
		}
		catch(Exception e) {
			System.err.println("Column \"" + column + "\" index "+i+" doesn't exist");
			return "NE";
		}
	}

	/**
	 * Gets the list.
	 *
	 * @param column the column
	 * @return the list
	 */
	public ValueList getList(String column) {
		if (containsKey(column)) {

			statistics.put(column, new DescriptiveStatistics());

			return (ValueList) get(column);
		}

		else {
			return addColumn(column);
		}
	}

	/**
	 * Adds the column.
	 *
	 * @param column the column
	 * @return the value list
	 */
	public synchronized ValueList addColumn(String column) {
		if (containsKey(column) == true)
			get(column);
		ValueList valueList = new ValueList();
		columnOrder.add(column);
		put(column, valueList);
		int maxSize = getMaximumColumnSize();
		if(maxSize > 1) {
			while(valueList.size() < maxSize-1) valueList.add("");
		}
		
		return valueList;
	}

	

	
	/**
	 * Gets the list.
	 *
	 * @param i the i
	 * @return the list
	 */
	public ValueList getList(int i) {
		System.out.println(i);
		if (i > 0 && i < columnOrder.size())
			return getList(columnOrder.get(i));
		else
			return null;
	}

	/*
	 * Theoritacal values
	 */

	/**
	 * Sets the theoritical value.
	 *
	 * @param column the column
	 * @param value the value
	 */
	public void setTheoriticalValue(String column, Object value) {
		getList(column).setTheriticalValue(value);
	}

	/**
	 * Gets the theoritical value.
	 *
	 * @param column the column
	 * @return the theoritical value
	 */
	public Object getTheoriticalValue(String column) {
		return getList(column).getTheriticalValue();
	}

	/**
	 * Gets the column count.
	 *
	 * @return the column count
	 */
	public int getColumnCount() {
		return size();
	}

	/*
	 * Merging related function
	 */

	/**
	 * Merge data set.
	 *
	 * @param dataSet the data set
	 */
	public void mergeDataSet(DataSet dataSet) {
		for (int i = 0; i != dataSet.getColumnSize(); i++) {
		for (String column : dataSet.getColumnList()) {
			
				addValue(column, dataSet.getValue(column, i));
			}
			//if (getTheoriticalValue(column) == null) {
			//	setTheoriticalValue(column, dataSet.getTheoriticalValue(column));
			//}
		}
	}

	/* Curating related function */

	/*
	 * Metadata related function
	 */

	/**
	 * Sets the meta data value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void setMetaDataValue(String key, String value) {

		if (!metadata.containsKey(key)) {
			metadataOrder.add(key);
		}

		metadata.put(key, value);
	}

	/**
	 * Sets the meta data value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void setMetaDataValue(String key, double value) {
		setMetaDataValue(key, "" + MathUtils.round(value, 6));
	}

	/**
	 * Gets the meta data value.
	 *
	 * @param key the key
	 * @return the meta data value
	 */
	public String getMetaDataValue(String key) {
		return metadata.get(key);
	}

	/**
	 * Gets the meta data value as double.
	 *
	 * @param key the key
	 * @return the meta data value as double
	 */
	public Double getMetaDataValueAsDouble(String key) {
		String value = getMetaDataValue(key);

		try {
			Double d = Double.parseDouble(value);
			return d;
		} catch (Exception e) {
			System.err.println(String.format(
					"Couldn't transform '%s' into Double. Returning 0.0.",
					value));
			return new Double(0.0);
		}

	}

	/**
	 * Gets the threoritical value as string.
	 *
	 * @param column the column
	 * @return the threoritical value as string
	 */
	public String getThreoriticalValueAsString(String column) {
		Object value = getTheoriticalValue(column);
		if (value instanceof Double) {
			return ""
					+ MathUtils.round((Double) getTheoriticalValue(column), 5);
		} else {
			return value.toString();
		}
	}

	/**
	 * Gets the meta data keys.
	 *
	 * @return the meta data keys
	 */
	public ArrayList<String> getMetaDataKeys() {
		return metadataOrder;
	}

	/*
	 * Exporting related functions
	 */

	/**
	 * Export to string.
	 *
	 * @return the string
	 */
	public String exportToString() {
		System.out.println("Exporting to string");
		putColumnInSaveLevel();
		StringBuffer result = new StringBuffer(5000);
		result.append(getHeader() + "\n");

		for (int i = 0; i != getColumnSize(); i++) {
			result.append(getLine(i) + "\n");
		}

		result.append("\n");

		for (String s : getMetaDataKeys()) {
			result.append(s + separator + metadata.get(s) + "\n");
		}

		return result.toString();

	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	protected String getHeader() {

		String result = "";
		System.out.println(columnOrder);
		for (String column : columnOrder) {
			result += column + separator;
		}

		for (String column : columnOrder) {
			if (getTheoriticalValue(column) != null) {
				result += column + "_th" + separator;
			}
		}

		return deleteLastTab(result);

	}

	/**
	 * Format value.
	 *
	 * @param d the d
	 * @return the string
	 */
	protected String formatValue(Object d) {

		String s = d.toString();

		if (d instanceof Double) {

			if (s.endsWith(".0")) {
				return s.substring(0, s.length() - 2);
			}

			else {
				return MathUtils.roundToString(((Double) d), 4);
			}

		}

		else {
			return s;
		}
	}

	/**
	 * Gets the line.
	 *
	 * @param i the i
	 * @return the line
	 */
	protected String getLine(int i) {
		String result = "";
		for (String column : columnOrder) {

			Object value = getValue(column, i);
			result += formatValue(value) + separator;
		}

		for (String column : columnOrder) {
			if (getTheoriticalValue(column) != null) {

				Object value = getTheoriticalValue(column);

				result += formatValue(value) + separator;
			}
		}

		return deleteLastTab(result);

	}

	// TODO : this function is not good, rewrite
	/**
	 * Format number.
	 *
	 * @param d the d
	 * @return the double
	 */
	protected double formatNumber(double d) {
		return 0.0;
	}

	/**
	 * Delete last tab.
	 *
	 * @param s the s
	 * @return the string
	 */
	protected String deleteLastTab(String s) {
		if (s.endsWith("\t"))
			return s.substring(0, s.length() - 1);
		else
			return s;
	}

	/*
	 * Importing related function
	 */

	/**
	 * Gets the column name.
	 *
	 * @param i the i
	 * @return the column name
	 */
	public String getColumnName(int i) {
		return columnOrder.get(i);
	}

	/**
	 * Import file.
	 *
	 * @param file the file
	 */
	public void importFile(String file) {

		try {
			CSVReader reader = new CSVReader(new FileReader(file), separator);
			String[] nextLine;

			boolean headerFlag = true; // flag raised for the header
			boolean dataFlag = true; // flat raised when parsing data (if false,
										// we parse metadata

			ArrayList<String> parsedColumnNames = new ArrayList<String>();

			while ((nextLine = reader.readNext()) != null) {

				// the first line should be column headers
				if (headerFlag && nextLine.length > 1) {
					System.out.println("turning down headerFlag");
					// a loop that add column header to the FovDataSet
					for (int i = 0; i != nextLine.length; i++) {
						String column = nextLine[i];
						parsedColumnNames.add(column);
						if (!column.endsWith("_th")) {
							addColumn(column);
						}
					}
					// turning down the header flag
					headerFlag = false;
					System.out.println(getColumnList());
					continue;
				}

				// then we parse data
				if (!headerFlag && dataFlag && nextLine.length > 1) {

					for (int i = 0; i != nextLine.length; i++) {

						String stringValue = nextLine[i];
						Object value;
						String column = parsedColumnNames.get(i);

						// trying to parse a value
						try {
							value = Double.parseDouble(stringValue);
						} catch (Exception e) {

							// System.err.println("Error : invalid double : "+stringValue);
							value = stringValue;

						}

						if (column.endsWith("_th")) {
							setTheoriticalValue(
									column.substring(0, column.length() - 3),
									value);
						} else {
							addValue(column, value);
						}
					}
				}

				// usualy, a blank line should be added after the data columns
				// which mark their end
				if (!headerFlag && dataFlag && nextLine.length == 1) {
					dataFlag = false;
					System.out.println("turning down data flag");
					continue;
				}

				if (!dataFlag && !headerFlag && nextLine.length == 2) {

					setMetaDataValue(nextLine[0], nextLine[1]);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		// System.out.println(exportToString());
	}

	/**
	 * Gets the column statistics.
	 *
	 * @param column the column
	 * @return the column statistics
	 */
	public DescriptiveStatistics getColumnStatistics(String column) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i != getColumnSize(column); i++) {

			Object value = getValue(column, i);
			double doubleValue;
			try {
				doubleValue = (Double) value;
				stats.addValue(doubleValue);
			} catch (Exception e) {
			}
		}
		return stats;
	}

	/*
	 * Value List class
	 */

	/**
	 * The Class ValueList.
	 */
	public class ValueList extends ArrayList<Object> {
		
		/** The theritical value. */
		Object theriticalValue;

		/**
		 * Gets the theritical value.
		 *
		 * @return the theritical value
		 */
		public Object getTheriticalValue() {
			return theriticalValue;
		}

		/**
		 * Sets the theritical value.
		 *
		 * @param theriticalValue the new theritical value
		 */
		public void setTheriticalValue(Object theriticalValue) {
			this.theriticalValue = theriticalValue;
		}
	}

	/**
	 * Removes the line.
	 *
	 * @param line the line
	 */
	public void removeLine(int line) {
		
		try {
		for(String column : getColumnList()) {
			getList(column).remove(line);
		}
		}
		catch(Exception e) {
			System.out.println("Couln't remove line " + line);
		}
	}
	
	/**
	 * Threshold.
	 *
	 * @param column the column
	 * @param up the up
	 * @param down the down
	 */
	public void threshold(String column, double up, double down) {
		
		for(int i = 0; i!=getColumnSize();i++) {
			
			if(getDoubleValue(column,i) < down || getDoubleValue(column,i) > up) {
				removeLine(i);
				i-=2;
			}
		}
		
		
	}
	
	

	/**
	 * Gets the double value.
	 *
	 * @param column the column
	 * @param i the i
	 * @return the double value
	 */
	public double getDoubleValue(String column,int i) {
		return (Double) getValue(column,i);
	}

	/**
	 * Gets the column median.
	 *
	 * @param column the column
	 * @return the column median
	 */
	public String getColumnMedian(String column) {
		// TODO Auto-generated method stub
		return ""
				+ MathUtils.round(
						getColumnStatistics(column).getPercentile(50), 5);
	}

	/**
	 * Checks if is new line by adding value to.
	 *
	 * @param column the column
	 * @return true, if is new line by adding value to
	 */
	protected boolean isNewLineByAddingValueTo(String column) {
		return (getMinimumColumnSize() < getColumnSize(column));
	}

	/**
	 * Put column in save level.
	 */
	protected void putColumnInSaveLevel() {
		int minimumSize = getMinimumColumnSize();
		int maximumSize = getMaximumColumnSize();
		
		
		if(minimumSize == maximumSize) return;
		System.out.println("Egalizing...");
		
		for (String column : getColumnList()) {
			if (getColumnSize(column) == minimumSize) {
				getList(column).add(new Double(0));
			}
		}
		
	}

	
	
	
	/**
	 * Gets the minimum column size.
	 *
	 * @return the minimum column size
	 */
	public int getMinimumColumnSize() {

		DescriptiveStatistics stats = new DescriptiveStatistics();

		for (String column : getColumnList()) {
			stats.addValue(getColumnSize(column));
		}

		return MathUtils.round(stats.getMin());
	}

	/**
	 * Gets the maximum column size.
	 *
	 * @return the maximum column size
	 */
	public int getMaximumColumnSize() {
		DescriptiveStatistics stats = new DescriptiveStatistics();

		for (String column : getColumnList()) {
			stats.addValue(getColumnSize(column));
		}

		return MathUtils.round(stats.getMax());
	}
	
	/**
	 * Verify level.
	 *
	 * @param column the column
	 */
	public void verifyLevel(String column) {
		if(isNewLineByAddingValueTo(column)) putColumnInSaveLevel();
		
		
	}

	/**
	 * Gets the column mean.
	 *
	 * @param column the column
	 * @return the column mean
	 */
	public double getColumnMean(String column) {
		// TODO Auto-generated method stub
		return getColumnStatistics(column).getPercentile(50);
	}
	
	
	/**
	 * Gets the columns median.
	 *
	 * @param id the id
	 * @param columns the columns
	 * @return the columns median
	 */
	public DataSet getColumnsMedian(String id, String[] columns) {
		DataSet dataset = new DataSet();
		
		dataset.addValue("id",id);
		
		for(String column : columns) {
			dataset.addValue(column, getColumnStatistics(column).getPercentile(50));
		}
		
		return dataset;
	}
	

	/**
	 * Gets the columns median.
	 *
	 * @param id the id
	 * @return the columns median
	 */
	public DataSet getColumnsMedian(String id) {
		ArrayList<String> choosenColumn = new ArrayList<String>();
		
		for(String column : getColumnList()) {
			if(getValue(column, 0) instanceof Double || getValue(column, 0) instanceof Integer) {
				choosenColumn.add(column);
			}
		}
		
		return getColumnsMedian(id, choosenColumn.toArray(new String[choosenColumn.size()]));
		
		
	}
	
	/**
	 * Save.
	 */
	public void save() {
		TextUtils.writeStringToFile(fileAddress, exportToString(), false);
	}

	/**
	 * Save as.
	 *
	 * @param path the path
	 */
	public void saveAs(String path) {
		TextUtils.writeStringToFile(path, exportToString(), false);
	}
	
	
}
