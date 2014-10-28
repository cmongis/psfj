/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package knop.psfj.resolution;

import ij.ImagePlus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import knop.psfj.graphics.PsfJGraph;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;


public class ReportSections {
    /**
     * Creates a new instance of ReportSection, an easy way to generate a pdf report
     */
    public ReportSections(){

    }

    /**
     * Generates an image of the RT-MFM's logo to be added to the report
     * @return and Image containing the RT-MFM's logo
     */
    public Image logoRTMFM(){
        Image logo = null;
        try {
            logo = Image.getInstance(getClass().getResource("/home/cyril/bioinformatics.gif"));
            logo.setAlignment(Image.ALIGN_CENTER);
            logo.scalePercent(50);

        } catch (BadElementException ex) {
            Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(NullPointerException ex) {
        	 Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        }
        return logo;
    }

    /**
     * Generates the main title to be added to the pdf document
     * @param title a string to be used as title
     * @return the title ready to be added to the pdf document
     */
    public Paragraph bigTitle(String title){
        Paragraph reportTitle=new Paragraph();
        reportTitle.add(new Chunk(title, new Font(Font.HELVETICA, 18, Font.BOLD)));
        reportTitle.setAlignment(Paragraph.ALIGN_CENTER);
        reportTitle.setLeading(20);
        reportTitle.setSpacingBefore(15);
        reportTitle.setSpacingAfter(15);
        return reportTitle;
    }

    /**
     * Generates the paragraph title to be added to the pdf document
     * @param title a string to be used as title
     * @return the paragraph title ready to be added to the pdf document
     */
    public Paragraph title(String title){
        Paragraph reportTitle=new Paragraph();
        Font font=new Font(Font.HELVETICA, 14, Font.BOLD);
        //font.setStyle(Font.UNDERLINE);
        reportTitle.add(new Chunk(title, font));
        reportTitle.setAlignment(Paragraph.ALIGN_LEFT);
        reportTitle.setSpacingBefore(15);
        return reportTitle;
    }

    public Paragraph subtitle(String subtitle) {
    	Paragraph reportTitle=new Paragraph();
        Font font=new Font(Font.HELVETICA, 12, Font.BOLD);
        //font.setStyle(Font.UNDERLINE);
        reportTitle.add(new Chunk(subtitle, font));
        reportTitle.setAlignment(Paragraph.ALIGN_LEFT);
        reportTitle.setSpacingBefore(5);
        return reportTitle;
    }
    
    /**
     * Generates the paragraph title to be added to the pdf document
     * @param title a string to be used as title
     * @return the paragraph title ready to be added to the pdf document
     */
    public Paragraph paragraph(String title){
        Paragraph paragraph=new Paragraph();
        Font font=new Font(Font.HELVETICA, 12, Font.NORMAL);
        paragraph.add(new Chunk(title, font));
        paragraph.setAlignment(Paragraph.ALIGN_LEFT);
        paragraph.setSpacingBefore(15);
        return paragraph;
    }
    
    
    public Paragraph littleNote(String note) {
   	 return littleNote(note,Paragraph.ALIGN_CENTER);
    }
    
    public Paragraph littleNote(String note,int alignement) {
    	Paragraph paragraph = new Paragraph();
    	Font font = new Font(Font.HELVETICA, 10, Font.NORMAL);
    	paragraph.add(new Chunk(note,font));
    	paragraph.setAlignment(alignement);
    	paragraph.setSpacingAfter(0);
    	return paragraph;
    }
    
    /**
     * Generates an Image ready to be added to the pdf document, based on an ImagePlus
     * @param image the ImagePlus from which the Image object will be generated
     * @param zoom the zoom to be applied to the image (0.0-100.0) as a float
     * @return an Image ready to be added to the pdf document
     */
    
    public Image imagePlus(PsfJGraph graph, float zoom) {
    	return imagePlus(new ImagePlus("",graph.getGraph().resize(1700)),zoom);
    }
    
    
    public Image imagePlus(ImagePlus image, float zoom){
        Image img = null;
        
        try {
            img = Image.getInstance(image.getImage(), null);
            img.setAlignment(Image.ALIGN_CENTER);
            //img.scalePercent(zoom);
            float height = zoom;
            img.scaleAbsolute(height*image.getWidth()/image.getHeight(),height);
           
            //img.scalePercent(zoom);
            
            img.setSpacingBefore(0);
            img.setSpacingAfter(0);
        } catch (BadElementException ex) {
            Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportSections.class.getName()).log(Level.SEVERE, null, ex);
        }
       // img.setSpacingBefore(15);
        return img;
    }

    /**
     * Generated a PdfPTable ready to be added to the pdf document
     * @param content a String[][] array, first index being the rows, second the columns.
     * @param widthPercentage size of the table, as a percentage of the page's width
     * @return an PdfPTable ready to be added to the pdf document
     */
    public PdfPTable table(String[][] content, float widthPercentage){
        PdfPTable table = new PdfPTable(content.length);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.setWidthPercentage(widthPercentage);
        for (int j=0; j<content[0].length; j++){
            for (int i=0; i<content.length; i++){
                Font font=new Font(Font.HELVETICA, 12, Font.NORMAL);
                if (i==0 || j==0) font=new Font(Font.HELVETICA, 12, Font.BOLD);
                table.addCell(new Paragraph(new Chunk(content[i][j], font)));
            }
        }
        table.setHorizontalAlignment(PdfTable.ALIGN_LEFT);
        table.setSpacingBefore(15);
        return table;
    }


}
