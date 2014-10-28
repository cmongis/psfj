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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JWindow;

// TODO: Auto-generated Javadoc
/**
 * The Class SplashScreen.
 */
public class SplashScreen extends JWindow{
	
	/** The image. */
	private BufferedImage image; 
	
	/**
	 * Instantiates a new splash screen.
	 *
	 * @param file the file
	 * @param time the time
	 */
	public SplashScreen(String file, long time){
		super();
		
		URL url = getClass().getResource(file);
		File fileO = new File(file);
		try{
			System.out.println(url);
			image = ImageIO.read(getClass().getResource(file));
			setSize(new Dimension(image.getWidth(), image.getHeight()));
			setLocationRelativeTo(null);
			setAlwaysOnTop(true);
			setVisible(true);
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());
			try {
				image = ImageIO.read(getClass().getResource("src"+file));
				setSize(new Dimension(image.getWidth(), image.getHeight()));
				setLocationRelativeTo(null);
				setAlwaysOnTop(true);
				setVisible(true);
			}
			catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
		if(time>0){
			TimerTask dispose = new TimerTask(){
				public void run(){
					dispose();
					}	
			};
			Timer timer = new Timer();
			timer.schedule(dispose, time);
			try{
				Thread.sleep(time);
			}catch(Exception e){e.printStackTrace();}
		}
	}
	
	/**
	 * Instantiates a new splash screen.
	 *
	 * @param file the file
	 */
	public SplashScreen(String file){
		this(file,0);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Window#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g){
		if(image.getColorModel().hasAlpha()){
			try{
				Robot robot = new Robot();
				BufferedImage fond = robot.createScreenCapture(getBounds());
				MediaTracker tracker = new MediaTracker(this);
				tracker.addImage(fond,0);
				tracker.waitForAll();
				g.drawImage(fond, 0,0,null);
			}catch(Exception e){e.printStackTrace();}
		}
		g.drawImage(image,0,0,null);	
	}
}