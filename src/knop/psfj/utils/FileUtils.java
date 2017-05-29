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

import ij.IJ;

import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

// TODO: Auto-generated Javadoc
/**
 * The Class FileUtils.
 */
public class FileUtils {

	/**
	 * The main method.
	 * 
	 * @param args
	 *           the arguments
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame();
		//System.setProperty("apple.awt.fileDialogForDirectories", "true");

		System.out.println(getDirectory("/home/cyril/test_img/",
				"/home/cyril/test_img/"));
		System.out.println(FileUtils
				.getFileNameWithoutExtension("tattayoyo.13fdsfs.fsd.tif"));
	}

	/**
	 * Folder exists.
	 * 
	 * @param folder
	 *           the folder
	 * @return true, if successful
	 */
	public static boolean folderExists(String folder) {
		File file = new File(folder);
		return file.exists();

	}

	/**
	 * Delete file.
	 * 
	 * @param path
	 *           the path
	 * @return true, if successful
	 */
	public static boolean deleteFile(String path) {
		File file = new File(path);

		if (file.isDirectory()) {
			for (File subfile : file.listFiles()) {
				deleteFile(subfile.getAbsolutePath());
			}
		}

		file.delete();

		return true;
	}

	/**
	 * Creates the folder.
	 * 
	 * @param folder
	 *           the folder
	 * @return true, if successful
	 */
	public static boolean createFolder(String folder) {
		File file = new File(folder);
		if (!file.exists()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
				IJ.error("Error when creating folder : " + folder);
				return false;
			}
		}
		return true;
	}

	/**
	 * Open folder.
	 * 
	 * @param folder
	 *           the folder
	 */
	public static void openFolder(String folder) {
		Desktop desktop = Desktop.getDesktop();

		try {
			desktop.open(new File(folder));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the file name without extension.
	 * 
	 * @param filename
	 *           the filename
	 * @return the file name without extension
	 */
	public static String getFileNameWithoutExtension(String filename) {
		Pattern pattern = Pattern.compile("(.*)[.]\\w*$");

		Matcher m = pattern.matcher(filename);

		if (m.matches()) {
			return m.group(1);
		} else
			return filename;

	}

	/**
	 * Gets the today date.
	 * 
	 * @return the today date
	 */
	public static String getTodayDate() {
		Date d = new Date();
		return d.toString();
	}

	/**
	 * Gets the directory.
	 * 
	 * @param title
	 *           the title
	 * @param def
	 *           the def
	 * @return the directory
	 */
	public static String getDirectory(String title, String def) {
		// Java2.setSystemLookAndFeel();
		
		if (!IJ.isWindows()) {

		//	System.setProperty("apple.awt.fileDialogForDirectories", "true");

			JFrame frame = new JFrame();
			System.setProperty("apple.awt.fileDialogForDirectories", "true");

			if (def == null || new File(def).exists() == false) {
				def = System.getProperty("user.home");
			}

			FileDialog d = new FileDialog(frame);

			d.setTitle(title);
			d.setDirectory(new File(def).getAbsolutePath());
			d.setFile(new File(def).getName());

			d.setFilenameFilter(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return new File(dir.getAbsolutePath() + name).isDirectory();
				}

			});

			//d.setMultipleMode(false);

			d.setMode(FileDialog.LOAD);

			d.setVisible(true);

			frame.dispose();
			if(d.getFile() == null) return null;
			
			return new File(d.getDirectory() +  d.getFile()).getAbsolutePath();
			
		} else {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(title);
                        
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			File fdir = null;
			if (def != null)
				fdir = new File(def);
			if (fdir != null)
				fc.setCurrentDirectory(fdir);

			int returnVal = fc.showSaveDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return null;

			return fc.getSelectedFile().getAbsolutePath();
		}
		
	}

	/**
	 * Load image ressource.
	 * 
	 * @param source
	 *           the source
	 * @param file
	 *           the file
	 * @return the buffered image
	 */
	public static BufferedImage loadImageRessource(Object source, String file) {
		BufferedImage image = null;
		try {

			image = ImageIO.read(source.getClass().getResource(file));

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			try {
				image = ImageIO.read(source.getClass().getResource("src" + file));

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		return image;

	}

}
