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
package knop.psfj.exporter;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.PSFj;
import knop.psfj.resolution.Microscope;
import knop.psfj.utils.MathUtils;
import knop.psfj.utils.TextUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;





// TODO: Auto-generated Javadoc
/**
 * The Class HTMLDataSetExporter.
 */
public class HTMLDataSetExporter {
	
	/** The manager. */
	BeadImageManager manager;

	
	
	
	
	/**
	 * Instantiates a new HTML data set exporter.
	 *
	 * @param m the m
	 */
	public HTMLDataSetExporter(BeadImageManager m) {
		manager = m;
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		
	
		
	}
	
	/**
	 * Load xml from string.
	 *
	 * @param xml the xml
	 * @return the document
	 * @throws Exception the exception
	 */
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	  
	    return builder.parse(is);
	}
	
	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	public String getStatistics() {
		if(manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL)
		return getStatistics(0);
		else
			return getStatisticsForComparaison();
	}
	
	
	/**
	 * Gets the kept beads count.
	 *
	 * @param bead the bead
	 * @return the kept beads count
	 */
	public int getKeptBeadsCount(int bead) {
		if(manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL) {
			return manager.getKeptBeadsCount();
		}
		else {
			return manager.getBeadImage(bead).getValidBeadCount();
		}
	}
	
	/**
	 * Gets the deleted beads count.
	 *
	 * @param bead the bead
	 * @return the deleted beads count
	 */
	public int getDeletedBeadsCount(int bead) {
		if(manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL) {
			return manager.getKeptBeadsCount();
		}
		else {
			return manager.getBeadImage(bead).getValidBeadCount();
		}
	}
	
	
	
	/**
	 * Gets the statistics for comparaison.
	 *
	 * @return the statistics for comparaison
	 */
	public String getStatisticsForComparaison() {
		StringBuffer result = new StringBuffer(1000);
		
		HashMap<String,String> values = new HashMap<String, String>();
		
		
		DescriptiveStatistics[] stats = new DescriptiveStatistics[3];
		
		String[] axes = new String[] { "X","Y","Z" };
		
		BeadImage image = manager.getBeadImage(0);
		BeadImage image2 = manager.getBeadImage(1);
		
		BeadFrameList list = image.getBeadFrameList();
		stats[0] = list.getDeltaXStatistics();
		stats[1] = list.getDeltaYStatistics();
		stats[2] = list.getDeltaZStatistics();
		
		Microscope m = image.getMicroscope();
		
		for(int i = 0;i!=3;i++) {
			values.put("%delta"+axes[i]+"mean", ""+m.formatDouble(stats[i].getMean()));
			values.put("%delta"+axes[i]+"median", ""+m.formatDouble(stats[i].getPercentile(50)));
			values.put("%delta"+axes[i]+"stdDev", ""+m.formatDouble(stats[i].getStandardDeviation()));
		}
		
		values.put("%microscope_id", m.getIdentifier());
		values.put("%totalBead1", ""+image.getValidBeadCount());
		values.put("%totalBead2", ""+image2.getValidBeadCount());
		values.put("%bead_number", ""+manager.getPairedBeads());
		values.put("%distanceThreshold", MathUtils.roundToString(image.getDistanceThreshold(),2));
		values.put("%deletedBeads",""+manager.getDeletedBeads());
		
		values.put("%threshold_1%",""+manager.getBeadImage(0).getThresholdValue());
		values.put("%substack_size_1%", ""+manager.getBeadImage(0).getFrameSize());
		
		values.put("%threshold_2%",""+manager.getBeadImage(1).getThresholdValue());
		values.put("%substack_size_2%", ""+manager.getBeadImage(1).getFrameSize());
		
		
		for(int i = 1;i!= 3;i++) {
			values.put("%wavelength"+i, manager.getBeadImage(i-1).getMicroscope().getWaveLengthAsString());
			values.put("%fwhmX"+i, manager.getHeatmapStatisticsAsString(
					PSFj.getHeatmapName(PSFj.FWHM_KEY,0, i-1),
					PSFj.NOT_NORMALIZED,
					manager.getMicroscope(0).getUnit()));
			values.put("%fwhmY"+i, manager.getHeatmapStatisticsAsString(
					PSFj.getHeatmapName(PSFj.FWHM_KEY,1, i-1),
					PSFj.NOT_NORMALIZED,
					manager.getMicroscope(0).getUnit()));
					;
			values.put("%fwhmZ"+i, 
					manager.getHeatmapStatisticsAsString(
							PSFj.getHeatmapName(PSFj.FWHM_KEY,2, i-1),
							PSFj.NOT_NORMALIZED,
							manager.getMicroscope(0).getUnit()));
			
			
			values.put("%fwhmXth"+i, m.formatDouble(manager.getBeadImage(i-1)
					.getMicroscope()
					.getXYTheoreticalResolution()));
			
			values.put("%fwhmYth"+i, m.formatDouble(manager.getBeadImage(i-1)
					.getMicroscope()
					.getXYTheoreticalResolution()));
			
			values.put("%fwhmZth"+i, m.formatDouble(manager.getBeadImage(i-1)
					.getMicroscope()
					.getZTheoreticalResolution()));
		
			String planrity = manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.Z_PROFILE, i-1), PSFj.NOT_NORMALIZED,m.getUnit());
			String asymmetry = manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, i-1), PSFj.NOT_NORMALIZED,"");
			
			values.put("%asymmetry"+i+"%",asymmetry);
			values.put("%planarity"+i+"%",planrity);
			
		}
		
		values.put("%NA", m.getNAAsString());
		values.put("%refraction", m.getRefractionIndexAsString());
		values.put("%bead_size",m.getBeadSizeAsString());
		values.put("%voxel_size",m.getVoxelSizeAsString());
		
		result.append(TextUtils.readTextRessource(values, "/mini-report-multichannel.html",values));
		
		return result.toString();
	}
	
	/**
	 * Gets the statistics.
	 *
	 * @param bead the bead
	 * @return the statistics
	 */
	public String getStatistics(int bead) {
		
		StringBuffer result = new StringBuffer(1000);
		
		boolean isSingleChannel = (manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL);
		
		HashMap<String,String> valueToReplace = new HashMap<String, String>();
		Microscope m = manager.getMicroscope(bead);
		
		
		

		valueToReplace.put("%microscope_id", m.getIdentifier());
		valueToReplace.put("%bead_number", ""+manager.getKeptBeadsCount());
		valueToReplace.put("%totalBeads", ""+(manager.getKeptBeadsCount()+manager.getDeletedBeads()));
		valueToReplace.put("%wave_length", m.getWaveLengthAsString());
		valueToReplace.put("%NA", m.getNAAsString());
		valueToReplace.put("%refraction", m.getRefractionIndexAsString());
		valueToReplace.put("%bead_size",m.getBeadSizeAsString());
		valueToReplace.put("%voxel_size",m.getVoxelSizeAsString());
		valueToReplace.put("%deletedBeads",""+manager.getDeletedBeads());
		valueToReplace.put("%threshold%",""+manager.getBeadImage(0).getThresholdValue());
		valueToReplace.put("%substack_size%", ""+manager.getBeadImage(0).getFrameSize());
		
		FovDataSet dataSet;
		if(isSingleChannel) {
			dataSet = manager.getDataSet();
		}
		else {
			dataSet = manager.getBeadImage(bead).getBeadFrameList().getDataSet();
			
		}
		
	
		DescriptiveStatistics zProfileStats = dataSet.getColumnStatistics("z_profile");
		
		valueToReplace.put("%fwhm_X",manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.FWHM_KEY, 0, -1), PSFj.NOT_NORMALIZED, m.getUnit()));
		valueToReplace.put("%fwhm_th_X", ""+MathUtils.formatDouble(m.getXYTheoreticalResolution(),m.getUnit()));
		//String encoded=ImageProcessorUtils.getEncodedPNGBase64(manager.getGraphList().get(0).getGraph(1).resize(100));
		//System.out.println(encoded);
		//valueToReplace.put("%heatmap%",encoded);
		valueToReplace.put("%fwhm_Y",manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.FWHM_KEY, 1, -1), PSFj.NOT_NORMALIZED, m.getUnit()));
		valueToReplace.put("%fwhm_th_Y", ""+MathUtils.formatDouble(m.getXYTheoreticalResolution(),m.getUnit()));
		
		valueToReplace.put("%fwhm_Z",manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.FWHM_KEY, 2, -1), PSFj.NOT_NORMALIZED, m.getUnit()));
		valueToReplace.put("%fwhm_th_Z", ""+MathUtils.formatDouble(m.getZTheoreticalResolution(),m.getUnit()));
		
		valueToReplace.put(("%z_profile"), ""+MathUtils.formatDouble(zProfileStats.getPercentile(50),m.getUnit()));
		
		String planrity = manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.Z_PROFILE, -1), PSFj.NOT_NORMALIZED,m.getUnit());
		String asymmetry = manager.getHeatmapStatisticsAsString(PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, -1), PSFj.NOT_NORMALIZED,"");
		
		valueToReplace.put("%asymmetry%",asymmetry);
		valueToReplace.put("%planarity%",planrity);
		
		result.append(TextUtils.readTextRessource(valueToReplace, "/mini-report.html",valueToReplace));
		
		
		TextUtils.writeStringToFile("/home/cyril/test.html", result.toString(), false);
		
		return result.toString();
	}
	
}
