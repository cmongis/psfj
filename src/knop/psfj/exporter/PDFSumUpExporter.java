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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.PSFj;
import knop.psfj.graphics.PsfJGraph;
import knop.psfj.resolution.Microscope;
import knop.psfj.resolution.ReportSections;
import knop.psfj.utils.FileUtils;
import knop.psfj.view.Message;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class PDFSumUpExporter.
 */
public class PDFSumUpExporter {

	/** The manager. */
	BeadImageManager manager;

	/** The current document. */
	Document currentDocument;

	/** The current writer. */
	PdfWriter currentWriter;

	/**
	 * Instantiates a new PDF sum up exporter.
	 * 
	 * @param manager
	 *           the manager
	 */
	public PDFSumUpExporter(BeadImageManager manager) {
		this.manager = manager;
	}

	/**
	 * The main method.
	 * 
	 * @param arg
	 *           the arguments
	 */
	public static void main(String[] arg) {
		final BeadImageManager manager = new BeadImageManager();

		// manager.add("/home/cyril/test_img/colocalisation/gfp1.tif");
		// manager.add("/home/cyril/test_img/colocalisation/mc1.tif");
		// manager.add("/home/cyril/test_img/6_small.tif");
		manager.add("/home/cyril/test_img/6/6_gfp.tif");
		// manager.add("/home/cyril/test_img/6_mcherry.tif");
		// manager.add("/home/cyril/test_img/6_mcherry.tif");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		manager.autoThreshold();
		// manager.autoThreshold(0);
		// manager.autoThreshold(1);
		manager.autoBeadEnlargementFactor();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// manager.verifyBeadImageParameters();
		System.out.println(manager.getBeadImage(0).getMiddleImage());
		manager.processProfiles();

		PDFSumUpExporter exporter = new PDFSumUpExporter(manager);

	
		exporter.export();
		exporter.closeDocument();
		FileUtils.openFolder(exporter.getDefaultDocumentName());

	}

	/**
	 * Export.
	 */
	public void export() {
		export(getDefaultDocumentName());
	}

	/**
	 * Export.
	 * 
	 * @param path
	 *           the path
	 */
	public void export(String path) {
		manager.setProgress("Generating ...", 10);
		openDocument(path);
		if (currentDocument == null)
			return;
		
		
		
		
		try {
			addTitle(currentDocument);
			ReportSections rs = new ReportSections();
			boolean isDualColorAnalysis = manager.getAnalysisType() == BeadImageManager.DUAL_CHANNEL;
			if (!isDualColorAnalysis) {
				addMicroscopeInfos(currentDocument, manager.getMicroscope(0));
				addResolutionTable(currentDocument, -1, manager.getMicroscope(0),
						manager.getAnalyzedBeadCount(), manager.getKeptBeadsCount(),
						manager.getIgnoredBeadsCount());

				manager.setProgress("Generating ...", 70);

				addGraphics(currentDocument, manager, 230, "z_profile");
				addBeadImgesInfos(currentDocument, manager);
				currentDocument.newPage();

				// adding heatmaps
				for (int axe = 0; axe != 3; axe++) {
					addGraphics(currentDocument, manager, 220,
							PSFj.getHeatmapName(PSFj.FWHM_KEY, axe, -1));
				}

				addGraphics(currentDocument, manager, 220,
						PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, -1));
				addGraphics(currentDocument, manager, 220,
						PSFj.getHeatmapName(PSFj.THETA_KEY, -1));

			} else {

				int channel = 0;
				for (BeadImage image : manager.getBeadImageList()) {
					addMicroscopeInfos(currentDocument, image.getMicroscope());
					addResolutionTable(currentDocument, channel,
							image.getMicroscope(), image.getTotalBeadCount(),
							image.getTotalBeadCount() - image.getDeletedBeadsCount(),
							image.getIgnoredFrameNumber());
					// addGraphics(currentDocument, manager, 230, "z_profile_" +
					// channel);

					// adding z profile heatmaps
					addGraphics(currentDocument, manager, 230,
							PSFj.getHeatmapName(PSFj.Z_PROFILE, channel));

					currentDocument.newPage();

					// adding asymmetry fwhm heatmaps
					for (int axe : PSFj.AXES)
						addGraphics(currentDocument, manager, 220,
								PSFj.getHeatmapName(PSFj.FWHM_KEY, axe, channel));

					// adding asymmetry and theta heatmaps
					addGraphics(currentDocument, manager, 220,
							PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, channel));
					addGraphics(currentDocument, manager, 220,
							PSFj.getHeatmapName(PSFj.THETA_KEY, channel));

					currentDocument.newPage();

					channel++;
				}
				manager.setProgress("Generating ...", 70);

				currentDocument.add(rs.bigTitle("Channel Comparaison"));
				currentDocument.add(rs.title("Summary"));
				currentDocument.add(rs.paragraph(String.format(
						"%s pairs used for statistics.", manager.getPairedBeads()))); // MathUtils.formatDouble(manager.getDistanceThreshold(),manager.getMicroscope(0).getUnit()),)));

				addGraphics(currentDocument, manager, 220,
						PSFj.getHeatmapName(PSFj.CHR_SHIFT_XY, -1));
				addGraphics(currentDocument, manager, 220,
						PSFj.getHeatmapName(PSFj.CHR_SHIFT_XYZ, -1));
				currentDocument.newPage();

				for (int axe : PSFj.AXES)
					addGraphics(currentDocument, manager, 225,
							PSFj.getHeatmapName(PSFj.CHR_SHIFT_KEY, axe, -1));

				addGraphics(currentDocument, manager, 220, PSFj.BEAD_MONTAGE_KEY);
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.setProgress("Finished", 100);
		manager.setProgress("", 0);

		closeDocument();
	}

	/**
	 * Adds the graphics.
	 * 
	 * @param document
	 *           the document
	 * @param manager
	 *           the manager
	 * @param size
	 *           the size
	 * @param graphs
	 *           the graphs
	 */
	private void addGraphics(Document document, BeadImageManager manager,
			int size, String... graphs) {
		ReportSections rs = new ReportSections();
		for (String graphName : graphs) {
			PsfJGraph graph = manager.getGraph(graphName);
			try {
				document.add(rs.title(graph.getTitle()));
				document.add(rs.imagePlus(graph, size));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Adds the title.
	 * 
	 * @param document
	 *           the document
	 */
	private void addTitle(Document document) {

		ReportSections rs = new ReportSections();
		try {
			document.addTitle("PSFj Report");
			document.addAuthor("PSFj");

			document.add(rs.bigTitle("PSFj Report"));
			document.add(rs.littleNote(FileUtils.getTodayDate()));

		} catch (Exception e) {
			System.err.println("Error when adding title to the report");
			e.printStackTrace();
		}
	}

	/**
	 * Adds the resolution table to the current document
	 * 
	 * @param document
	 *           the document
	 * @param channel
	 *           the channel
	 * @param m
	 *           the m
	 * @param totalBeads
	 *           the total beads
	 * @param keptBeads
	 *           the kept beads
	 * @param ignoredBeads
	 *           the ignored beads
	 */
	private void addResolutionTable(Document document, int channel,
			Microscope m, int totalBeads, int keptBeads, int ignoredBeads) {

		ReportSections rs = new ReportSections();

		//System.out.println(PSFj.getHeatmapName(PSFj.FWHM_KEY, 0, channel));

		String fwhmXmedian = knop.psfj.utils.MathUtils.formatStatistics(manager
				.getHeatmapStatistics(
						PSFj.getHeatmapName(PSFj.FWHM_KEY, 0, channel),
						PSFj.NOT_NORMALIZED), m.getUnit());
		String fwhmYmedian = knop.psfj.utils.MathUtils.formatStatistics(manager
				.getHeatmapStatistics(
						PSFj.getHeatmapName(PSFj.FWHM_KEY, 1, channel),
						PSFj.NOT_NORMALIZED), m.getUnit());
		String fwhmZmedian = knop.psfj.utils.MathUtils.formatStatistics(manager
				.getHeatmapStatistics(
						PSFj.getHeatmapName(PSFj.FWHM_KEY, 2, channel),
						PSFj.NOT_NORMALIZED), m.getUnit());

		String fwhmXth = m.formatDouble(m.getXYTheoreticalResolution());
		String fwhmYth = m.formatDouble(m.getXYTheoreticalResolution());
		String fwhmZth = m.formatDouble(m.getZTheoreticalResolution());

		String planarity = manager.getHeatmapStatisticsAsString(
				PSFj.getHeatmapName(PSFj.Z_PROFILE, channel), PSFj.NOT_NORMALIZED,
				m.getUnit());
		String asymmetry = manager.getHeatmapStatisticsAsString(
				PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, channel),
				PSFj.NOT_NORMALIZED, "");

		int detectedBeads = totalBeads + ignoredBeads;
		int filteredOut = totalBeads - keptBeads;

		int frameSize = manager.getBeadImage(channel).getFrameSize();

		int threshold = manager.getBeadImage(channel).getThresholdValue();
		String beadCount = String
				.format(
						"%d beads used for statistics : %d detected, %d analyzed, %d filtered out after analysis.\nSubstack size : %d pixels\nDetection threshold : %d",
						keptBeads, detectedBeads, totalBeads, filteredOut, frameSize,
						threshold);

		String[][] sumUpTable = new String[][]{
				{"FWHM", "min", "max", "Z"},
				// {"Non corrected (median)",data.getColumnMedian("fwhmX_nc"),data.getColumnMedian("fwhmY_nc"),data.getColumnMedian("fwhmZ_nc")},
				{"Bead size corrected (median)", fwhmXmedian, fwhmYmedian,
						fwhmZmedian}, {"Theoritical", fwhmXth, fwhmYth, fwhmZth}};

		String[][] sumUpTable2 = new String[][]{

		{"Other", "Planarity", "Asymmetry"}, {"", planarity, asymmetry}};

		// String asymParath =
		// String.format("Planarity : %s,  asymmetry : %s",planarity,asymmetry);

		try {
			document.add(rs.title("Summary"));
			document.add(rs.table(sumUpTable, 100));
			document.add(rs.table(sumUpTable2, new Float(100.0 * 2f / 3)));
			// document.add(rs.paragraph(asymParath));
			document.add(rs.paragraph(beadCount));
		} catch (DocumentException e) {
			System.err.println("Error when adding summary table");
			e.printStackTrace();
		}

	}

	/**
	 * Adds the microscope infos.
	 * 
	 * @param document
	 *           the document
	 * @param m
	 *           the m
	 */
	private void addMicroscopeInfos(Document document, Microscope m) {
		ReportSections rs = new ReportSections();
		String title = "Microscope infos ";
		String identifier = manager.getMicroscope(0).getIdentifier();
		if (identifier.equals("")) {
			title += ":";
		} else {
			title += " ( " + identifier + ") : ";
		}
		try {
			currentDocument.add(rs.title(title));
			currentDocument.add(rs.paragraph(m.getMicroscopeHeader()));
		} catch (DocumentException e) {
			e.printStackTrace();
			System.err.println("Error when adding ");
		}

	}

	/**
	 * Adds the bead imges infos.
	 * 
	 * @param document
	 *           the document
	 * @param manager
	 *           the manager
	 */
	private void addBeadImgesInfos(Document document, BeadImageManager manager) {
		ReportSections rs = new ReportSections();
		String fileList = "";
		for (BeadImage image : manager.getBeadImageList()) {

			String name = image.getImageName();

			fileList += String
					.format(
							"\n\t - %s : %d beads detected, %d beads analyzed, %d filtered out",
							name,
							image.getTotalBeadCount() + image.getIgnoredFrameNumber(),
							image.getTotalBeadCount(), image.getDeletedBeadsCount());
		}
		try {
			document.add(rs.paragraph("File List : " + fileList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the default document name.
	 * 
	 * @return the default document name
	 */
	public String getDefaultDocumentName() {

		if (manager.isDualColorAnalysis()) {
			return manager.getExportDirectory() + "dual_color_"
					+ manager.getBeadImage(0).getImageNameWithoutExtension() + "_"
					+ manager.getBeadImage(1).getImageNameWithoutExtension()
					+ "_psf_sum_up.pdf";
		} else
			return manager.getExportDirectory() + manager.getAnalysisName()
					+ "_psf_sum_up.pdf";
	}

	/**
	 * Open document.
	 * 
	 * @param path
	 *           the path
	 */
	public void openDocument(String path) {
		currentDocument = new Document();

		try {

			if (FileUtils.folderExists(path))
				FileUtils.deleteFile(path);

			currentWriter = PdfWriter.getInstance(currentDocument,
					new FileOutputStream(path));
			currentDocument.open();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			currentWriter = null;
			currentDocument = null;
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			currentWriter = null;
			currentDocument = null;
			e.printStackTrace();
		}

		if (currentWriter == null)
			manager
					.update(
							manager,
							new Message(
									manager,
									"error",
									"Error when creating the document. Please close your PDF reader before exporting this document."));

	}

	/**
	 * Close document.
	 */
	public void closeDocument() {
		System.out.println("Closing document.");
		if (currentDocument != null) {
			currentDocument.close();
			currentWriter.close();
			currentDocument = null;
			currentWriter = null;
			System.gc();
		}
	}

}
