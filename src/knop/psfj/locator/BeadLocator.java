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

import java.awt.Rectangle;
import java.util.ArrayList;
import knop.psfj.BeadFrameList;
import knop.psfj.BeadImage;

/**
 *
 * @author cyril
 */
public abstract class BeadLocator {
     BeadImage image;
     
     
     public BeadLocator() {
         
     }
     
     public void setBeadImage(BeadImage image) {
         this.image = image;
     }
     
     public BeadLocator(BeadImage image) {
         this.image = image;
         
     }
     
     public BeadImage getImage() {
         return image;
     }
     
     public abstract ArrayList<Rectangle> getBeadLocation();
     
     public abstract BeadFrameList getBeadFrameList();
     
     public abstract boolean isFocusPlaneDependent();
     
}
