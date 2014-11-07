/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.psfj.locator;

import Objects3D.Object3D;
import ij.ImagePlus;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import knop.psfj.BeadFrame2D;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import knop.psfj.resolution.Counter3D;

/**
 *
 * @author cyril
 */
public class BeadLocator3D extends BeadLocator implements Observer{

    
    Counter3D counter;
    
    @Override
    public ArrayList<Rectangle> getBeadLocation() {
        ArrayList<Rectangle> beadLocation = new ArrayList<Rectangle>();
        
        
        System.out.println("Searching for centroids");
        
        counter = new Counter3D(new ImagePlus("",getImage().getStack()),getImage().getThresholdValue(),1,65000);
        
        counter.addObserver(this);
        try {
        counter.getObjects();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        //counter.getCentroidList();
        System.out.println(counter.getFound() + " objects found.");
        Vector<Object3D> objectList = counter.getObjectsList();
        
        for(Object3D o : objectList) {
            Rectangle r = new Rectangle();
            
            double x =  o.c_mass[0] - (1.0*o.bound_cube_width/2.0);
            double y =  o.c_mass[1] - (1.0*o.bound_cube_height/2.0);
            
            double width = o.bound_cube_width;
            double height = o.bound_cube_height;
            
            r.setRect(x, y, width, height);
            
            beadLocation.add(r);
            
        }
        image.setFrameNumber(beadLocation.size());
        image.setIgnoredFrameNumber(0);
        System.out.println("Search finihsed");
        
        return beadLocation;
    }

    @Override
    public BeadFrameList getBeadFrameList() {
        BeadFrameList beadFrames = new BeadFrameList();
        
        int id = 1;
        
        for(Rectangle rectangle : image.getBeadLocation()) {
            BeadFrame2D frame = new BeadFrame2D(id++,image.getEnlargedFrame(rectangle));
            beadFrames.add(frame);
            frame.setSource(getImage());
        }
        
        
        return beadFrames;
        
    }

    @Override
    public boolean isFocusPlaneDependent() {
        return false;
    }

    @Override
    public void update(Observable o, Object arg) {
        
        BeadImage image = getImage();
        
        if(image.getProgress()!= counter.getProgress())
            image.setProgress(counter.getProgress());
        
        
    }
    
    
    
    
}
