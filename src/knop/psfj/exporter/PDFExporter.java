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

import ij.ImagePlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import knop.psfj.BeadFrame;
import knop.psfj.BeadFrame2D;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.resolution.Microscope;
import knop.psfj.resolution.ReportSections;
import knop.psfj.resolution.SideViewGenerator;
import knop.psfj.utils.FileUtils;
import knop.psfj.utils.MathUtils;
import knop.psfj.view.Message;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class PDFExporter.
 */
public class PDFExporter extends Observable {

	/** The manager. */
	BeadImageManager manager;

	/** The svg. */
	SideViewGenerator svg = new SideViewGenerator();

	/** The current document. */
	Document currentDocument;
	
	/** The current writer. */
	PdfWriter currentWriter;

	/** The total count. */
	int totalCount;
	
	/** The actual count. */
	int actualCount = 0;
	
	
	/**
	 * The main method.
	 *
	 * @param arg the arguments
	 */
	public static void main(String[] arg) {

		BeadImageManager manager = new BeadImageManager();
		manager.add("/home/cyril/test_img/6/6_gfp.tif");
		//manager.add("/home/cyril/test_img/6/6_mcherry.tif");

		manager.processProfiles();

		manager.exportInPDF(true);

	}

	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	public BeadImageManager getManager() {
		return manager;
	}

	/**
	 * Sets the manager.
	 *
	 * @param manager the new manager
	 */
	public void setManager(BeadImageManager manager) {
		this.manager = manager;
		totalCount = 0;
		for(BeadImage image : manager.getBeadImageList()) totalCount+=image.getTotalBeadCount();
	}

	/**
	 * Instantiates a new PDF exporter.
	 *
	 * @param manager the manager
	 */
	public PDFExporter(BeadImageManager manager) {
		setManager(manager);
	}

	
	/**
	 * Increment count.
	 */
	public void incrementCount() {
		actualCount ++;
		if(actualCount % 10 == 0 || actualCount == 0) {
			setChanged();
			Integer progress = new Integer(100 * actualCount
					/ totalCount);
			if(progress == 0) progress = 1;
			notifyObservers(new Message(manager, "progress changed",
					String.format("Generating PDF report : %d/%d",actualCount,totalCount), progress));
			
		}
		
		if(actualCount == totalCount) {
			setChanged();
			notifyObservers(new Message(manager, "progress changed",
					"Done.", new Integer(0)));
		}
		
	}
	
	
	
	
	
	/**
	 * Write single file report.
	 */
	public void writeSingleFileReport() {
		
		
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		
		for(BeadImage image : manager.getBeadImageList()) {
			int beadPerPage = 100;
			int start = 0;
			int pageCount = 1;
			BeadFrameList frameList = image.getBeadFrameList();
			BeadFrameList toDo = frameList.get(start, beadPerPage);
			
			while(toDo.size() > 0) {
				String path = manager.getExportDirectory()+image.getImageNameWithoutExtension()+"_beads_page_"+pageCount+".pdf";
				executor.execute(writeSingleFileReport(toDo, path, pageCount));
				
				pageCount++;
				start+=beadPerPage;
				toDo = frameList.get(start, beadPerPage);
			}
			
		
			
		
			
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendFinishMessage("Done.");
		
	}
	
	
	/**
	 * Send finish message.
	 *
	 * @param message the message
	 */
	public void sendFinishMessage(String message) {
		manager.setProgress("Done", 0);
	}
	
	
	/**
	 * Write single file report.
	 *
	 * @param frameList the frame list
	 * @param path the path
	 * @param page the page
	 * @return the runnable
	 */
	public Runnable writeSingleFileReport(final BeadFrameList frameList, final String path, int page) {
		return new Runnable() {
			public void run() {
				
				Document document = null;
				PdfWriter writer = null;
				
				
				
				if (FileUtils.folderExists(path))
					FileUtils.deleteFile(path);
				
				try {
					document = new Document();
					
					
					writer = PdfWriter.getInstance(document,new FileOutputStream(path));
					document.open();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(writer == null || document == null) return;
				
				
				for (BeadFrame frame : frameList) {

					//System.out.println("profile : " + frame);
					fillReport(document, writer, frame, frame.getId());
					document.newPage();
					incrementCount();
				}
				document.close();
				writer.close();
			}
		};
		

	}


	
	/**
	 * Export bead image list.
	 *
	 * @param imageList the image list
	 * @param path the path
	 */
	public void exportBeadImageList(BeadFrameList imageList,String path) {
		Document document = new Document();
		try {
			
			int i = 0;
			int max = imageList.size();
			
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(path)));
			document.open();
			for(BeadFrame frame : imageList) {
				fillReport(document, writer, frame, frame.getId());
				
				
				setChanged();
				Integer progress = new Integer(100 * i++
						/ max);
				if(progress == 0) progress = 1;
				notifyObservers(new Message(this, "progress changed",
						String.format("Generating bead PDF reports : %d/%d",i,max), progress));
				document.newPage();
			}
			
			
			document.close();	
			writer.close();
			
			sendFinishMessage("Done.");
			
			
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		} catch (DocumentException e) {
			
			e.printStackTrace();
			setChanged();
			notifyObservers(new Message("this","error","Error when generating PDF."));
		}
	}
	
	
	/**
	 * Fill report.
	 *
	 * @param report the report
	 * @param writer the writer
	 * @param bead the bead
	 * @param id the id
	 */
	public static void fillReport(Document report, PdfWriter writer, BeadFrame bead,
			int id) {

		Microscope microscope = bead.getSource().getMicroscope();
		ReportSections rs = new ReportSections();

		writer.setStrictImageSequence(true);

		try {
			/*
			 * report.add(rs.bigTitle("Bead " + id));
			 * report.add(rs.littleNote(microscope.date)); /*
			 * report.add(rs.title("Profile view:")); ImagePlus img; try { img =
			 * svg.getPanelView(pp.getImagePlus(), SideViewGenerator.MAX_METHOD,
			 * true, true, 5, false, null, 0); } catch (NullPointerException e)
			 * { e.printStackTrace(); return; } float zoom2scaleTo256pxMax =
			 * 25600 / Math.max(img.getWidth(), img.getHeight());
			 * report.add(rs.imagePlus(img, zoom2scaleTo256pxMax));
			 * 
			 * report.add(rs.title("Microscope infos:"));
			 * report.add(rs.paragraph(microscope.getMicroscopeHeader()));
			 */

			
			id = bead.getId();
			String suffix = "";
			if (bead.isValid() == false)
				suffix = " (Rejected)";
			
			if(bead.isValid() && bead.getInvalidityReason() != null) {
				suffix = " (Rejected from the pair analysis)";
			}
			
			// Image logo = loadImage("knoplablogo.png");

			// report.add();
			// currentDocument.add(rs.littleNote(FileUtils.getTodayDate()));

			double[] resolutions = bead.getResolutions();
		
			// the resolutions extracted from the Bead Spread function are
			// assigned to
			// individual variables
			String resolutionX = microscope.formatDouble(bead.getResolution(0));
			String resolutionY = microscope.formatDouble(bead.getResolution(1));
			String resolutionZ = microscope.formatDouble(bead.getResolution(2));

			// the corrected resolution are calculated by the Microscope Object
			// (it holds
			// all the experiment data).
			String correctedResolutionX = microscope.formatDouble(bead.getCorrectionResolution(Microscope.X));
			String correctedResolutionY = microscope.formatDouble(bead.getCorrectionResolution( Microscope.Y));
			String correctedResolutionZ = microscope.formatDouble(bead.getCorrectionResolution(Microscope.Z));
			
			String asymmetry = MathUtils.roundToString(bead.getAsymetry(), 3);
			
			String theta = MathUtils.roundToString(bead.getThetaInDegrees(),1) + MathUtils.DEGREE_SYMBOL;
			
			String[][] content = {
					{ "FWHM", "min", "max", "z" ,"Asymmetry","Theta"},
					{ "Non corrected", resolutionX, resolutionY, resolutionZ, asymmetry,theta},

					{ "Corrected", correctedResolutionX, correctedResolutionY,
							correctedResolutionZ,"",""

					},
					{
							"Theoretical",
							microscope.formatDouble(microscope
									.getXYTheoreticalResolution()),
							microscope.formatDouble(microscope
									.getXYTheoreticalResolution()),
							microscope.formatDouble(microscope
									.getZTheoreticalResolution()),"","" } };

			Paragraph header = new Paragraph();

			Image sideView = rs.imagePlus(bead.getSideViewImage(), 100);
			sideView.setAlignment(Image.ALIGN_RIGHT | Image.TEXTWRAP);
			report.add(sideView);
			String microscopeId = bead.getSource().getMicroscope().getIdentifier();
			if(microscopeId.equals("") == false) microscopeId = String.format(" ( %s )",microscopeId);
			String date = "Date : " + FileUtils.getTodayDate();
			String origin = String.format("\n Origin : %s %s\n Frame size : %d pixels\n",bead.getSource().getImageName(),microscopeId,bead.getWidth());
			String shift;
			String correspondingBead = "\nCorresponding bead : "
					+ ((bead.getAlterEgo() == null) ? "Not found" : "Number "
							+ bead.getAlterEgo().getId() + " in "
							+ bead.getAlterEgo().getSource().getImageName());
			String coordinates = "\nCoordinates : "
					+ microscope.formatDouble(bead.getFovX()) + " (x), "
					+ microscope.formatDouble(bead.getFovY()) + " (y), "
					+ microscope.formatDouble(bead.getCentroidZ()*microscope.getCalibration().pixelDepth) + " (z)";
			String rejectionReason = (bead.getInvalidityReason() == null ? ""
					: "\nReason of rejection : " + bead.getInvalidityReason());

			if (bead.getAlterEgo() != null) {
				shift = "\nShift : ";
				shift += microscope.formatDouble(bead.getDeltaX());
				shift += " x " + microscope.formatDouble(bead.getDeltaY());
				shift += " x " + microscope.formatDouble(bead.getDeltaZ());
			} else {
				shift = "";
			}

			report.add(rs.title("Bead " + id + suffix));
			report.add(rs.paragraph(date + origin + coordinates
					+ correspondingBead + shift + rejectionReason));
			// report.add(rs.subtitle("Resolution table:"));

			report.add(rs.table(content, 100));

		
			int height = 130;
			
			ImagePlus xPlot;
			ImagePlus yPlot;
			
			if(bead instanceof BeadFrame2D) {
				BeadFrame2D bead2D = (BeadFrame2D) bead;
				xPlot = new ImagePlus("",bead2D.getOverlayWithTheoretical());
				yPlot = xPlot;
				report.add(rs.subtitle("XY profile & fitting parameters : "));
				report.add(rs.littleNote("(red : the orignal data, green : the fit, yellow : the two merged)",Paragraph.ALIGN_LEFT));
				
			}
			else {
				xPlot = bead.getXplot().getImagePlus();
				yPlot = bead.getYplot().getImagePlus();
				report.add(rs.subtitle("X profile & fitting parameters:"));
			}
			
			
			
			Image image = rs.imagePlus(xPlot, height);
			image.setAlignment(Image.ALIGN_LEFT | Image.TEXTWRAP);
			report.add(image);
			report.add(rs.paragraph(bead.getXParams()));

			if(bead instanceof BeadFrame2D == false) {
			report.add(rs.subtitle("Y profile & fitting parameters:"));
			image = rs.imagePlus(yPlot, height);
			image.setAlignment(Image.ALIGN_LEFT | Image.TEXTWRAP);
			report.add(image);
			report.add(rs.paragraph(bead.getYParams()));
			}
			report.add(rs.subtitle("Z profile & fitting parameters:"));
			image = rs.imagePlus(bead.getZplot().getImagePlus(), height);
			image.setAlignment(Image.ALIGN_LEFT | Image.TEXTWRAP);
			report.add(image);
			report.add(rs.paragraph(bead.getZParams()));

			if (!microscope.sampleInfos.equals("")
					|| !microscope.comments.equals(""))
				report.newPage();

			if (!microscope.sampleInfos.equals("")) {
				report.add(rs.title("Sample infos:"));
				report.add(rs.paragraph(microscope.sampleInfos));
			}

			if (!microscope.comments.equals("")) {
				report.add(rs.title("Comments:"));
				report.add(rs.paragraph(microscope.comments));
			}
			
			// logo.setAlignment(Image.ALIGN_RIGHT | Image.TEXTWRAP);
			// logo.scalePercent(10);

			// currentDocument.add(logo);

		} catch (DocumentException e) {
			e.printStackTrace();
		}

		System.gc();

	}

	/**
	 * Round str.
	 *
	 * @param d the d
	 * @param i the i
	 * @return the string
	 */
	public String roundStr(double d, int i) {
		return "" + MathUtils.round(d, i);
	}

}
