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

import ij.IJ;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import knop.psfj.resolution.DataTricks;
import knop.psfj.utils.MathUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import au.com.bytecode.opencsv.CSVReader;

// TODO: Auto-generated Javadoc
/**
 * The Class FovDataSet.
 */
public class FovDataSet extends HashMap<String, FovDataSet.ValueList> {

    /**
     * The image name.
     */
    String imageName;

    /**
     * The image path.
     */
    String imagePath;

    /**
     * The image full path.
     */
    String imageFullPath;

    /**
     * The column order.
     */
    ArrayList<String> columnOrder = new ArrayList<String>();

    /**
     * The metadata.
     */
    HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * The metadata order.
     */
    ArrayList<String> metadataOrder = new ArrayList<String>();

    /**
     * The separator.
     */
    String separator = ",";

    /**
     * The statistics.
     */
    HashMap<String, DescriptiveStatistics> statistics = new HashMap<String, DescriptiveStatistics>();

    /**
     * The scale to theoritical.
     */
    int SCALE_TO_THEORITICAL = 0;

    /**
     * The scale to min max.
     */
    int SCALE_TO_MIN_MAX = 1;

    /**
     * The scaling mode.
     */
    int scalingMode = SCALE_TO_THEORITICAL;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        FovDataSet fdt = new FovDataSet();

        FovDataSet fdt2 = new FovDataSet();

        Random rn = new Random();

        /*
		 * for (int i = 0; i != 10; i++) { fdt.addValue("bead", rn.nextInt(15));
		 * fdt.addValue("x", rn.nextDouble()); fdt.addValue("y", rn.nextDouble());
		 * fdt.addValue("z", rn.nextDouble());
		 * 
		 * fdt.addValue("fwhmX", rn.nextDouble() * 3); fdt.addValue("fwhmY",
		 * rn.nextDouble() * 2); fdt.addValue("fwhmZ", rn.nextDouble() * 5); }
         */
        for (int i = 0; i != 10; i++) {
            // fdt.addValue("Index",i);
            fdt.addValue("xSource", rn.nextInt(100));
            fdt2.addValue("xSource", rn.nextDouble() * 200);
            fdt.addValue("xTarget", rn.nextInt(100));
            fdt2.addValue("xTarget", rn.nextInt(200));
            fdt.addValue("a name", "toi");
            fdt2.addValue("a name", "et moi");
        }
        fdt.setSeparator("\t");
        System.out.println(fdt.getColumnStatistics("xSource"));
        fdt.mergeDataSet(fdt2);
        System.out.println(fdt.exportToString());

        //fdt.exportToCrappyFormat("/home/cyril/test.xls");
    }

    public void addValue(String column, Object o) {
        getList(column).add(o);
    }

    public void addValue(String column, String string) {
        getList(column).add(string);
    }

    /**
     * Instantiates a new fov data set.
     */
    public FovDataSet() {
        super();
    }

    /**
     * Instantiates a new fov data set.
     *
     * @param file the file
     */
    public FovDataSet(String file) {
        super();
        importFile(file);
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
     * @param normalized the normalized
     * @param value the value
     */
    public void addValue(String column, int normalized, double value) {
        addValue(PSFj.getColumnID(column, normalized), value);
    }

    /**
     * Adds the value.
     *
     * @param columnId the column id
     * @param axe the axe
     * @param normalized the normalized
     * @param value the value
     */
    public void addValue(String[] columnId, int axe, int normalized, double value) {
        addValue(PSFj.getColumnName(columnId, axe, normalized), value);
    }

    /**
     * Adds the value.
     *
     * @param column the column
     * @param value the value
     */
    public synchronized void addValue(String column, double value) {
        if (value == Double.NaN) {
            getList(column).add(Double.NaN);
        } else {
            getList(column).add(new Double(value));
        }
        // getColumnStatistics(column).addValue(value);
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
            getList(n).add(new Double(value));
            statistics.get(getColumnName(n)).addValue(value);
        } else {
            System.err.println(String
                    .format("Column number %d doesn't exist !", n));
        }
    }

    /**
     * Gets the value.
     *
     * @param column the column
     * @param axe the axe
     * @param normalized the normalized
     * @param i the i
     * @return the value
     */
    public Double getDoubleValue(String[] column, int axe, int normalized, int i) {
        return getDoubleValue(PSFj.getColumnID(column, axe, normalized, 0), i);
    }

    /**
     * Gets the value.
     *
     * @param column the column
     * @param normalized the normalized
     * @param i the i
     * @return the value
     */
    public Double getDoubleValue(String column, int normalized, int i) {
        return getDoubleValue(PSFj.getColumnID(column, normalized), i);
    }

    public Object getValue(String column, int i) {
        return getList(column).get(i);
    }

    /**
     * Gets the value.
     *
     * @param column the column
     * @param i the i
     * @return the value
     */
    public Double getDoubleValue(String column, int i) {
        try {
            return (Double) getList(column).get(i);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Gets the value as str.
     *
     * @param column the column
     * @param i the i
     * @return the value as str
     */
    public String getValueAsStr(String column, int i) {
        return MathUtils.formatDouble(getDoubleValue(column, i), getColumnUnit(column));
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
        } else {
            return addColumn(column);
        }
    }

    /**
     * Sets the column name.
     *
     * @param column the column
     * @param name the name
     */
    public void setColumnName(String column, String name) {
        if (containsKey(column)) {
            get(column).setName(name);
        } else {
            System.err.println("A column that doesn't exist cannot be named.");
        }
    }

    /**
     * Gets the column name.
     *
     * @param column the column
     * @return the column name
     */
    public String getColumnName(String column) {
        if (getList(column).getName() == null) {
            return column;
        }
        return getList(column).getName();
    }

    /**
     * Adds the column.
     *
     * @param columns the columns
     */
    public void addColumn(String... columns) {
        for (String c : columns) {
            addColumn(c);
        }
    }

    /**
     * Adds the column.
     *
     * @param column the column
     * @param axe the axe
     * @param normalized the normalized
     * @return the value list
     */
    public ValueList addColumn(String[] column, int axe, int normalized) {
        return addColumn(PSFj.getColumnName(column, axe, normalized));
    }

    /**
     * Sets the columns units.
     *
     * @param unit_then_columns the new columns units
     */
    public void setColumnsUnits(String... unit_then_columns) {

        if (unit_then_columns.length > 1) {
            String unit = unit_then_columns[0];
            for (int i = 1; i != unit_then_columns.length; i++) {
                setColumnUnit(unit_then_columns[i], unit);
            }
        }
    }

    /**
     * Adds the column.
     *
     * @param column the column
     * @return the value list
     */
    public synchronized ValueList addColumn(String column) {
        if (containsKey(column) == true) {
            return get(column);
        }
        ValueList valueList = new ValueList();
        columnOrder.add(column);
        put(column, valueList);
        return valueList;
    }

    /**
     * Gets the list.
     *
     * @param i the i
     * @return the list
     */
    public ValueList getList(int i) {
        //	System.out.println(i);
        if (i > 0 && i < columnOrder.size()) {
            return getList(columnOrder.get(i));
        } else {
            return null;
        }
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
    public void setTheoriticalValue(String column, Double value) {
        getList(column).setTheriticalValue(value);
    }

    /**
     * Gets the theoritical value.
     *
     * @param column the column
     * @return the theoritical value
     */
    public Double getTheoriticalValue(String column) {
        try {
            return getList(column).getTheriticalValue();
        } catch (Exception e) {
            System.err.println("No theorical value for " + column
                    + "... returning median.");
            return getColumnStatistics(column).getPercentile(50);
        }
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
    public void mergeDataSet(FovDataSet dataSet) {
        mergeDataSet(dataSet, false);
    }

    /**
     * Merge data set.
     *
     * @param dataSet the data set
     * @param recalculateZNormalization the recalculate z normalization
     */
    public void mergeDataSet(FovDataSet dataSet,
            boolean recalculateZNormalization) {

        //Adding columns
        for (String column : dataSet.getColumnList()) {
            addColumn(column);
            setColumnName(column, dataSet.getColumnName(column));
            setColumnUnit(column, dataSet.getColumnUnit(column));
            if (getTheoriticalValue(column) == null && dataSet.getTheoriticalValue(column) != null) {

                setTheoriticalValue(column, dataSet.getTheoriticalValue(column));
            }
        }

        for (String column : dataSet.getColumnList()) {
            for (int i = 0; i != dataSet.getColumnSize(column); i++) {
                addValue(column, dataSet.getValue(column, i));
            }
        }

        if (recalculateZNormalization) {
            recalculateZProfileNormalisation();
        }

    }

    /**
     * Recalculate z profile normalisation.
     *
     * @param fwhmZmean the fwhm zmean
     */
    public void recalculateZProfileNormalisation(double fwhmZmean) {
        if (containsKey(PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED))) {

            System.out.println("Recalculting z_profile normalisation");

            String zProfileNormID = PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NORMALIZED);

            //clearing the normalized z profile column
            getList(zProfileNormID).clear();
            getList(PSFj.Z0_ZMEAN).clear();

            double fwhmZ = getTheoriticalValue(PSFj.getColumnID(PSFj.FWHM_KEY, PSFj.Z_AXIS, PSFj.NOT_NORMALIZED, 0));
            // adds the normalized z_profile
            for (int i = 0; i != get(PSFj.Z_PROFILE).size(); i++) {

                double z_profile = getDoubleValue(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED, i);

                get(zProfileNormID).add((z_profile - fwhmZmean) / fwhmZ);
                get(PSFj.Z0_ZMEAN).add(z_profile - fwhmZmean);
            }

            System.out.println("calculation done.");
        }
    }

    /**
     * Recalculate z profile normalisation.
     */
    public void recalculateZProfileNormalisation() {
        String zProfileID = PSFj.getColumnID(PSFj.Z_PROFILE, PSFj.NOT_NORMALIZED);
        recalculateZProfileNormalisation(getColumnStatistics(zProfileID).getMean());
    }

    /**
     * Merge meta data.
     *
     * @param dataSet the data set
     */
    public void mergeMetaData(FovDataSet dataSet) {
        for (String metaDataKey : getMetaDataKeys()) {
            dataSet.setMetaDataValue(metaDataKey,
                    dataSet.getMetaDataValue(metaDataKey));
        }
    }

    /* Curating related function */
    /**
     * Delete outlayers.
     */
    public void deleteOutlayers() {

        FovDataSet curated = new FovDataSet();

        // we inspect each line
        for (int i = 0; i != getColumnSize(); i++) {

            // each line is declared as valid
            boolean isValid = true;

            // then we parcours each column
            for (String column : getColumnList()) {

                // if there is no theoritical value we cannot decide if there is
                // outlayers
                if (getTheoriticalValue(column) == null
                        || getTheoriticalValue(column) == 0.0) {
                    continue;
                }
                Double value = getDoubleValue(column, i);
                Double th = getTheoriticalValue(column);

                if (value > 20 * th) {
                    isValid = false;
                    break;
                } else if (value < th / 20) {
                    isValid = false;
                    break;
                }

            }

            // now that we verified each column
            // we add the value to the curated
            if (isValid) {
                for (String column : getColumnList()) {
                    curated.addValue(column, getDoubleValue(column, i));
                }
            }

        }

        this.clear();
        mergeDataSet(curated);

    }

    /**
     * Sets the value.
     *
     * @param id the id
     * @param column the column
     * @param value the value
     */
    public void setValue(Integer id, String column, Double value) {
        getList(column).set(id, value);
    }

    /*
	 * Metadata related function
     */
    /**
     * Sets the meta data value.
     *
     * @param key the key
     * @param value the value
     */
    public void setMetaDataValue(String key, int value) {
        setMetaDataValue(key, "" + value);
    }

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
     * Adds the meta data space.
     */
    public void addMetaDataSpace() {
        metadataOrder.add("");
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

    public void setMetaDataValue(String key, String unit, double value) {

        if (unit == null) {
            setMetaDataValue(key, value);

        } else {
            setMetaDataValue(String.format("%s (%s)", key, unit), value);
        }
    }

    /**
     * Gets the meta data value.
     *
     * @param key the key
     * @return the meta data value
     */
    public String getMetaDataValue(String key) {
        if (metadata.get(key) == null) {
            return "";
        } else {
            String r = metadata.get(key);
            return r;
        }

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
            if (value == "") {
                return null;
            }
            Double d = Double.parseDouble(value);
            return d;
        } catch (Exception e) {
            System.err.println(String.format(
                    "Couldn't transform '%s' into Double. Returning 0.0.", value));
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
        return "" + MathUtils.round(getTheoriticalValue(column), 5);
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

        String[] columns = new String[columnOrder.size()];

        return exportToString(separator, columnOrder.toArray(columns));
    }

    /**
     * Export to string.
     *
     * @param c the c
     * @return the string
     */
    public String exportToString(Collection<String> c) {
        String[] list = new String[c.size()];
        list = c.toArray(list);
        return exportToString(list);
    }

    /**
     * Export to string.
     *
     * @param columns the columns
     * @return the string
     */
    public String exportToString(String... columns) {
        return exportToString(separator, columns);
    }

    /**
     * Export to string.
     *
     * @param separator the separator
     * @param columnOrder the column order
     * @return the string
     */
    public String exportToString(String separator, String[] columnOrder) {
        String oldSeparator = this.separator;
        this.separator = separator;
        //	System.out.println("Exporting to string");

        //creating a buffer
        StringBuffer result = new StringBuffer(100000);

        //adding the meta data at the begining of the file
        if (getMetaDataKeys().size() > 0) {

            for (String s : getMetaDataKeys()) {
                result.append(s + separator + metadata.get(s) + "\n");
            }

            result.append("\n");
        }

        // putting the header
        result.append(getHeader(columnOrder) + "\n");

        // adding the data
        for (int i = 0; i != getColumnSize(); i++) {

            result.append(getLine(i, columnOrder) + "\n");
        }

        separator = oldSeparator;
        return result.toString();

    }

    /**
     * Export to crappy format.
     *
     * @param path the path
     */
    public void exportToCrappyFormat(String path) {

        StringBuffer buffer = new StringBuffer(10000);
        buffer.append("Index\txSource\tySource\txTarget\tyTarget\n");

        Vector<Point> sourceList = new Vector<Point>();
        Vector<Point> targetList = new Vector<Point>();

        for (int i = 0; i != getColumnSize(); i++) {
            int xSource = MathUtils.round(getDoubleValue("xSource", i));
            int ySource = MathUtils.round(getDoubleValue("ySource", i));
            int xTarget = MathUtils.round(getDoubleValue("xTarget", i));
            int yTarget = MathUtils.round(getDoubleValue("yTarget", i));

            Point source = new Point(xSource, ySource);
            Point target = new Point(xTarget, yTarget);

            sourceList.add(source);
            targetList.add(target);

        }
        savePoints(sourceList, targetList, path);
    }

    /**
     * Export to xls.
     *
     * @param path the path
     */
    public void exportToXLS(String path) {

        ResultsTable table = new ResultsTable();

        short row = 0;
        short column = 0;

        for (String header : columnOrder) {
            String value = header;
            table.setHeading(column, value);
            column++;
        }

        for (row = 0; row != getColumnSize(); row++) {
            table.incrementCounter();
            for (column = 0; column != columnOrder.size(); column++) {

                table.addValue(column, getDoubleValue(columnOrder.get(column), row));
            }
        }

        table.show("??");
        try {
            table.saveAs(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*------------------------------------------------------------------*/
    /**
     * Save the landmark points into a file.
     *
     * @param sourceList the source list
     * @param targetList the target list
     * @param filename the filename
     */
    private void savePoints(Vector<Point> sourceList, Vector<Point> targetList,
            String filename) {

        try {
            final FileWriter fw = new FileWriter(filename);

            Point sourcePoint;
            Point targetPoint;
            String n;
            String xSource;
            String ySource;
            String xTarget;
            String yTarget;
            fw.write("Index\txSource\tySource\txTarget\tyTarget\n");
            for (int k = 0; (k < sourceList.size()); k++) {
                n = "" + k;
                while (n.length() < 5) {
                    n = " " + n;
                }
                sourcePoint = (Point) sourceList.elementAt(k);
                xSource = "" + sourcePoint.x;
                while (xSource.length() < 7) {
                    xSource = " " + xSource;
                }
                ySource = "" + sourcePoint.y;
                while (ySource.length() < 7) {
                    ySource = " " + ySource;
                }
                targetPoint = (Point) targetList.elementAt(k);
                xTarget = "" + targetPoint.x;
                while (xTarget.length() < 7) {
                    xTarget = " " + xTarget;
                }
                yTarget = "" + targetPoint.y;
                while (yTarget.length() < 7) {
                    yTarget = " " + yTarget;
                }
                fw.write(n + "\t" + xSource + "\t" + ySource + "\t" + xTarget
                        + "\t" + yTarget + "\n");
            }
            fw.close();
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
        } catch (SecurityException e) {
            IJ.error("Security exception" + e);
        }
    }

    /* end savePoints */

    /**
     * Gets the header.
     *
     * @param columnOrder the column order
     * @return the header
     */
    protected String getHeader(String[] columnOrder) {

        String result = "";

        for (String column : columnOrder) {
            // adding column name
            result += getColumnName(column);

            // if unit there is
            if (getColumnUnit(column).equals("") == false) {
                result += " (" + getColumnUnit(column) + ")";
            }
            // seprator
            result += separator;

            /*
			if (getTheoriticalValue(column) != null
					&& getTheoriticalValue(column) > 0) {
				result += "Theoretical " + getColumnName(column) + separator;
			}*/
        }

        // adding theoretical values
        /*
		for (String column : columnOrder) {
			if (getTheoriticalValue(column) != null
					&& getTheoriticalValue(column) > 0) {
				result += column + "_th" + separator;
			}
		}*/
        return deleteLastTab(result);

    }

    protected String formatValue(Object o) {
        if (o instanceof Double) {
            return formatValue((Double) o);
        } else {
            return o.toString();
        }
    }

    /**
     * Format value.
     *
     * @param d the d
     * @return the string
     */
    protected String formatValue(Double d) {

        String s = d.toString();

        if (s.endsWith(".0")) {
            return s.substring(0, s.length() - 2);
        } else {
            return new Double(DataTricks.round(d, 3)).toString();
        }
    }

    /**
     * Gets the line.
     *
     * @param i the i
     * @param columnOrder the column order
     * @return the line
     */
    protected String getLine(int i, String[] columnOrder) {
        String result = "";
        for (String column : columnOrder) {
            //System.out.println(column);
            Object value = getValue(column, i);
            result += formatValue(value) + separator;

            /*
			if (getTheoriticalValue(column) != null) {

				value = getTheoriticalValue(column);

				result += formatValue(value) + separator;
			}*/
        }

        /*for (String column : columnOrder) {
			
		}*/
        return deleteLastTab(result);

    }

    /**
     * Gets the line for xls.
     *
     * @param i the i
     * @return the line for xls
     */
    protected String getLineForXLS(int i) {
        String result = "";
        int cellLength = 10;
        for (String column : columnOrder) {

            Double value = getDoubleValue(column, i);
            result += "\t" + formatValue(value);
        }

        for (String column : columnOrder) {
            if (getTheoriticalValue(column) != null) {

                Double value = getTheoriticalValue(column);

                result += "\t" + formatValue(value);// formatValue(value) +
                // separator;
            }
        }

        return result;

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
        if (s.endsWith(separator)) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
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
            CSVReader reader = new CSVReader(new FileReader(file),
                    separator.charAt(0));
            String[] nextLine;

            boolean headerFlag = true; // flag raised for the header
            boolean dataFlag = true; // flat raised when parsing data (if false,
            // we parse metadata

            ArrayList<String> parsedColumnNames = new ArrayList<String>();

            while ((nextLine = reader.readNext()) != null) {

                // the first line should be column headers
                if (headerFlag && nextLine.length > 1) {
                    //System.out.println("turning down headerFlag");
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
                        Double value;
                        String column = parsedColumnNames.get(i);

                        // trying to parse a value
                        try {
                            value = Double.parseDouble(stringValue);
                        } catch (Exception e) {
                            System.err.println("Error : invalid double : "
                                    + stringValue);
                            value = new Double(-1.0);
                        }

                        if (column.endsWith("_th")) {
                            setTheoriticalValue(
                                    column.substring(0, column.length() - 3), value);
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
        } catch (IOException e) {
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
        if (containsKey(column) == false) {
            return stats;
        }
        for (int i = 0; i != getColumnSize(column); i++) {
            if (getDoubleValue(column, i) != Double.NaN
                    && getDoubleValue(column, i).isNaN() == false) {
                stats.addValue(getDoubleValue(column, i));
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

        /**
         * The theritical value.
         */
        Double theriticalValue;

        /**
         * The name.
         */
        String name;

        /**
         * The unit.
         */
        String unit = "";

        /**
         * Gets the theritical value.
         *
         * @return the theritical value
         */
        public Double getTheriticalValue() {
            return theriticalValue;
        }

        /**
         * Sets the name.
         *
         * @param name the name
         * @return the value list
         */
        public ValueList setName(String name) {

            this.name = name;
            return this;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the theritical value.
         *
         * @param theriticalValue the new theritical value
         */
        public void setTheriticalValue(Double theriticalValue) {
            this.theriticalValue = theriticalValue;
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
         * Sets the unit.
         *
         * @param u the u
         * @return the value list
         */
        public ValueList setUnit(String u) {
            unit = u;
            return this;
        }

    }

    /**
     * Gets the min and max.
     *
     * @param yColumn the y column
     * @return the min and max
     */
    public double[] getMinAndMax(String yColumn) {
        double[] minAndMax = new double[3];

        DescriptiveStatistics stats = getColumnStatistics(yColumn);

        double line;
        double min;
        double max;

        if (yColumn.equals("z_profile")) {
            minAndMax[0] = -1;
            minAndMax[1] = 1;
            minAndMax[2] = 0;
            return minAndMax;
        }

        // scale to the theoritical value
        if (scalingMode == SCALE_TO_THEORITICAL) {

            // there is a theoretical value
            if (getTheoriticalValue(yColumn) != null) {
                line = getTheoriticalValue(yColumn);
                max = line * 2;
                min = line / 2;
            } // no theoritical value but the min and max are fixed
            else if (getMetaDataValueAsDouble(yColumn + "_interval_min") != null) {
                min = getMetaDataValueAsDouble(yColumn + "_interval_min");
                max = getMetaDataValueAsDouble(yColumn + "_interval_max");
                line = -1;
            } // no theoritical value, then we can only scale to min and max
            else {
                min = stats.getMin();
                max = stats.getMax();
                line = -1;
            }
        } else {

            min = stats.getMin();
            max = stats.getMax();
            if (getTheoriticalValue(yColumn) != null) {
                line = getTheoriticalValue(yColumn);
            } else {
                line = -1;
            }
        }

        minAndMax[0] = min;
        minAndMax[1] = max;
        minAndMax[2] = line;

        return minAndMax;
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
            pointList.add(new KnPoint(getDoubleValue(xColumn, i), getDoubleValue(yColumn, i)));
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

        double min = getMin(yColumn);

        if (yColumn.contains("deltaD") || yColumn.contains("delta3D")) {
            min = 0;
        }

        String unit = getColumnUnit(yColumn);

        double max = getMax(yColumn);
        double line = getLine(yColumn);

        double[] thLine = new double[y.length];

        for (int i = 0; i != thLine.length; i++) {
            thLine[i] = line;
        }

        // Plot p = new Plot("", getColumnName(xColumn) + " (µm)",
        // getColumnName(yColumn) + " " + unit, x, y);
        Plot p = new Plot("", getColumnName(xColumn) + " (µm)",
                getColumnName(yColumn) + " " + unit);

        p.addPoints(x, y, Plot.LINE);
        p.setLimits(x[0], x[x.length - 1], min, max);
        p.setColor(Color.green);

        p.setLineWidth(4);

        p.setFont(new Font("Arial", 18, Font.BOLD));
        p.addPoints(x, thLine, Plot.LINE);
        p.setColor(Color.blue);
        p.setLineWidth(1);
        p.setSize(width, height);

        return p.getProcessor();

    }

    /**
     * Gets the unit.
     *
     * @param yColumn the y column
     * @return the unit
     */
    private String getUnit(String yColumn) {
        // TODO Auto-generated method stub
        if (yColumn.contains("norm")) {
            return "";
        } else {
            return "(µm)";
        }
    }

    /**
     * The Class KnPoint.
     */
    private class KnPoint implements Comparable<KnPoint> {

        /**
         * The x.
         */
        double x;

        /**
         * The y.
         */
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
     * Gets the column median.
     *
     * @param column the column
     * @return the column median
     */
    public String getColumnMedian(String column) {
        // TODO Auto-generated method stub
        DescriptiveStatistics stats = getColumnStatistics(column);
        //System.out.println("stddev : " + stats.getStandardDeviation());
        return ""
                + MathUtils.formatDouble(stats.getPercentile(50),
                        getColumnUnit(column))
                + " +/- "
                + MathUtils.formatDouble(stats.getStandardDeviation(),
                        getColumnUnit(column));
    }

    /**
     * Gets the column unit.
     *
     * @param column the column
     * @return the column unit
     */
    public String getColumnUnit(String column) {
        return getList(column).getUnit();
    }

    /**
     * Sets the column unit.
     *
     * @param column the column
     * @param unit the unit
     */
    public void setColumnUnit(String column, String unit) {
        if (getList(column) != null) {
            getList(column).setUnit(unit);
        }
    }

    /**
     * Gets the scaling mode.
     *
     * @return the scaling mode
     */
    public int getScalingMode() {
        return scalingMode;
    }

    /**
     * Sets the scaling mode.
     *
     * @param scalingMode the new scaling mode
     */
    public void setScalingMode(int scalingMode) {
        this.scalingMode = scalingMode;
    }

    /**
     * Gets the min.
     *
     * @param column the column
     * @return the min
     */
    public double getMin(String column) {
        if (column.contains("fwhm")) {
            return getTheoriticalValue(column) / 2;
        }

        if (column.contains("delta") || column.contains("norm")) {
            return -1;
        }

        DescriptiveStatistics columnStats = getColumnStatistics(column);
        double median = columnStats.getPercentile(50);
        double stdDev = columnStats.getStandardDeviation();
        if (stdDev == 0) {
            return -1;
        }
        return median - (stdDev * 2);

    }

    /**
     * Gets the max.
     *
     * @param column the column
     * @return the max
     */
    public double getMax(String column) {
        if (column.contains("fwhm")) {
            return getTheoriticalValue(column) * 2;
        }

        if (column.contains("norm")) {
            return 1;
        }

        if (column.equals("z_profile") || column.contains("delta")) {
            return 1;
        }

        DescriptiveStatistics columnStats = getColumnStatistics(column);
        double median = columnStats.getPercentile(50);
        double stdDev = columnStats.getStandardDeviation();
        if (stdDev == 0) {
            return 1;
        }
        return median + (stdDev * 2);
    }

    /**
     * Gets the line.
     *
     * @param column the column
     * @return the line
     */
    public double getLine(String column) {
        if (column.contains("fwhm")) {
            return getTheoriticalValue(column);
        }
        if (column.equals("z_profile") || column.contains("norm")
                || column.contains("delta")) {
            return 0;
        }

        double median = getColumnStatistics(column).getPercentile(50);

        return median;
    }

    /**
     * Gets the min label.
     *
     * @param column the column
     * @return the min label
     */
    public String getMinLabel(String column) {
        if (column.contains("fwhm")) {
            return "Th. value / 2";
        }
        if (column.equals("z_profile") || column.contains("delta")) {
            return "";
        }

        return "";

    }

    /**
     * Gets the max label.
     *
     * @param column the column
     * @return the max label
     */
    public String getMaxLabel(String column) {
        if (column.contains("fwhm")) {
            return "Th. value x 2";
        }
        if (column.equals("z_profile") || column.contains("delta")) {
            return "";
        }

        return "";
    }

    /**
     * Gets the line label.
     *
     * @param column the column
     * @return the line label
     */
    public String getLineLabel(String column) {
        if (column.contains("fwhm")) {
            return "Th. value";
        }
        if (column.equals("z_profile") || column.contains("norm")
                || column.contains("delta")) {
            return "";
        }

        return "median";
    }

    /**
     * Sets the separator.
     *
     * @param string the new separator
     */
    public void setSeparator(String string) {
        separator = string;

    }

    /**
     * Adds the column.
     *
     * @param column the column
     * @param normalized the normalized
     * @return the value list
     */
    public ValueList addColumn(String column, int normalized) {
        // TODO Auto-generated method stub
        return addColumn(PSFj.getColumnID(column, normalized));
    }

}
