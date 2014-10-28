/*
 * 
 */


import ij.IJ;
import ij.plugin.PlugIn;
import knop.psfj.BeadImageManager;
import knop.psfj.view.BeadImageLoaderPage;
import knop.psfj.view.CalibrationPage;
import knop.psfj.view.ExportDataPage;
import knop.psfj.view.FocusChooserPage;
import knop.psfj.view.HeatMapPage;
import knop.psfj.view.ProcessingPage;
import knop.psfj.view.ThresholdChooserPage;
import knop.psfj.view.WizardWindow;


/*

This file is part of PatroloJ.

PatroloJ is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PatroloJ is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PatroloJ.  If not, see <http://www.gnu.org/licenses/>.

Copyright (C) 2013 Cyril MONGIS

* 
*/

public class PSFj_ implements PlugIn{
	/*
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		PatroloJView view;
		if (WindowManager.getImageCount()!=0){
			ImagePlus ipl = WindowManager.getCurrentImage();
			view = new PatroloJView(ipl);
		}
		else {
			view = new PatroloJView();
		}
	}


	
	
	public static void main(String[] arg) {
		PatroloJView view = new PatroloJView(true);
	}*/
	
	
	public void run(String arg0) {
		BeadImageManager manager = new BeadImageManager();
		manager.add(IJ.getImage());
		
		WizardWindow wizardWindow = new WizardWindow(false);
		manager.addObserver(wizardWindow);
		wizardWindow.addPage(new BeadImageLoaderPage(wizardWindow, manager));
		wizardWindow.addPage(new FocusChooserPage(manager));
		wizardWindow.addPage(new CalibrationPage(manager));
		wizardWindow.addPage(new ThresholdChooserPage(manager));
		wizardWindow.addPage(new ExportDataPage(manager));
		wizardWindow.addPage(new ProcessingPage(manager));
		wizardWindow.addPage(new HeatMapPage(manager));
		wizardWindow.setCurrentPage(0);
		wizardWindow.show();
		
	}
	
	public static void main(String[] arg0) {
		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();
		manager.addObserver(wizardWindow);
		wizardWindow.addPage(new BeadImageLoaderPage(wizardWindow, manager));
		wizardWindow.addPage(new FocusChooserPage(manager));
		wizardWindow.addPage(new CalibrationPage(manager));
		wizardWindow.addPage(new ThresholdChooserPage(manager));
		wizardWindow.addPage(new ExportDataPage(manager));
		wizardWindow.addPage(new ProcessingPage(manager));
		wizardWindow.addPage(new HeatMapPage(manager));
		wizardWindow.setCurrentPage(0);
	}
}
