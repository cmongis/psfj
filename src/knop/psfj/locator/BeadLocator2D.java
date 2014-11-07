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
package knop.psfj.locator;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.util.ArrayList;
import knop.psfj.BeadFrame;
import knop.psfj.BeadFrame2D;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;
import static knop.psfj.BeadImage.getEnlargedFrame;

/**
 *
 * @author cyril
 */
public class BeadLocator2D extends BeadLocator {

    @Override
    public ArrayList<Rectangle> getBeadLocation() {
        ArrayList<Rectangle> beadLocation = new ArrayList<Rectangle>();
        ImageProcessor mask = null;
        ManyBlobs blobs = null;

        for (int i = 4; i <= 30 && blobs == null; i += 3) {
            try {
                mask = getImage().getSegmentedImage();

                beadLocation = new ArrayList<Rectangle>();
                if (mask == null) {
                    System.err.println("No mask could be retreived.");
                    beadLocation = null;
                    return new ArrayList<Rectangle>();
                }
                blobs = new ManyBlobs(new ImagePlus("", mask));
                blobs.findConnectedComponents();

            } catch (NullPointerException e) {
                blobs = null;

                System.out.println(getImage().getMiddleImage().getMax());

                System.err.println("Detection error.\nThreshold :"
                        + getImage().getThresholdValue());
                getImage().setProgress(0, "Detection error.... raising threshold...");
                getImage().setThresholdValue(getImage().getThresholdValue() + i);

            }

        }

        if (blobs == null) {
            return new ArrayList<Rectangle>();
        }

        for (Blob b : blobs) {

            Rectangle r = b.getOuterContour().getBounds();

            if (r.getX() - r.getWidth() <= 1 || r.getY() - r.getHeight() <= 1) {
                continue;
            }

            if (r.getX() + r.getWidth() >= mask.getWidth()
                    || r.getY() + r.getHeight() >= mask.getHeight()) {
                continue;
            }

            beadLocation.add(r);
        }

        System.out
                .println("Dot filtered : " + beadLocation.size() + " found.");
        return beadLocation;

    }

    @Override
    public BeadFrameList getBeadFrameList() {

        
        BeadFrameList beadFrames = new BeadFrameList();
        
        ArrayList<Rectangle> beadRectangleList = new ArrayList<Rectangle>();
        BeadImage image = getImage();
        ArrayList<Rectangle> beadLocation = image.getBeadLocation();
        System.out.println("Building bead frames...");

        // Filling the dotFrames
        int i = 0;
        for (Rectangle r : beadLocation) {
            beadRectangleList.add(image.getEnlargedFrame(r));
        }

        // deleting overlapping frames
        ArrayList<Rectangle> toRemove = new ArrayList<Rectangle>();

        image.setStatus("Filtering beads...");

        int total = beadRectangleList.size();

        // comparing all points together to see if there is overlap
        for (i = 0; i != total; i++) {
            if (100 * i / total % 10 == 0) {
                image.setProgress(i, total);
            }
            Rectangle r = beadRectangleList.get(i);
            // setProgress(100*i++ / total,"Filtering beads...");
            for (int j = 0; j != total; j++) {
                Rectangle r2 = beadRectangleList.get(j);
                if (r != r2) {

                    // if there is overlap, they are added to a blacklist
                    if (r2.contains(r.getCenterX(), r.getCenterY())
                            && !toRemove.contains(r) && !toRemove.contains(r2)) {
                        double maxR1 = image.getBeadMaxIntensity(getEnlargedFrame(
                                getBeadLocation().get(i), 1));
                        double maxR2 = image.getBeadMaxIntensity(getEnlargedFrame(
                                getBeadLocation().get(j), 1));
                        // System.out.println(maxR1 + " versus " + maxR2);
                        if (maxR1 < maxR2) {
                            toRemove.add(r);
                        } else {
                            toRemove.add(r2);
                        }
                    }
                }
            }
        }

        image.getMiddleImage().resetRoi();

			// constructing a new frame list deleting those from the blacklist
        int id = 1;
       
        for (Rectangle r : beadRectangleList) {

            BeadFrame f = new BeadFrame2D(id, r);
            f.setSource(image);
            if (toRemove.contains(r)) {
                f.setValid(false, "Overlapping with an other bead frame");
            } else {

                beadFrames.add(f);
                id++;
            }
        }

        image.setIgnoredFrameNumber(toRemove.size());

        image.setFrameNumber(beadFrames.size());

        image.setProgress(100);
        image.setStatus("Done.");

        System.gc();
        return beadFrames;
    }

    @Override
    public boolean isFocusPlaneDependent() {
        return true;
    }

}
