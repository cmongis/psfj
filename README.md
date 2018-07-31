# PSFj
Know your microscope fluorescence

PSFj analyzes a stack of images of fluorescent beads to calculate the resolution of your microscope across the fields of view. PSFj is written in JAVA and compatible with Windows, MacOS X and Linux.


## Download and instructions
Please visit PSFj website [http://www.knoplab.de/psfj/
](http://www.knoplab.de/psfj)


## Developer manual

### Dependencies

All jar dependencies are located in the lib folder. You can use this Netbeans or Eclipse to edit the code.


### Introduction to the data processing

The data processing occurs in several steps using divers configurations elements.
PSFj loads images and detects beads located on the image stack. It then extracts the beads into substacks of images of the size dictated by the variable **Frame size** in order to calculate the x,y and z resolutions. It uses a 2D fitting of the bead image focal plane in order to calculate the x and y resultions, and a 1D fit for the z resolution. Once done, the software compiles the informations of each beads into tables and graphics. In the case, of multichannel analysis, a additional step tries to associate the signals coming from different channels to the same bead. In other words, for each bead detected in one channel, the software tries to find the corresponding bead of the other channel.

After that, the bead data is subject to several filtering that ensure that the displayed results come from fitting presenting a good signal to noise ratio.


### The three most important classes

In order to manipulate at a code level, it's important to understand the role of the different classes involved in the processing.

#### BeadImageManager

This classes deals with data loading, processing and export in a high level. The BeadImageManager was created to allow easy management of the process.



#### Microscope

This classes simply holds the microscope configuration. This classes is widely used to convert pixel data into the metric system.

#### BeadImage

This classes takes care of loading the pixel data from a single stack, and defines diverse parameter of the stack like the focal plane, the segmentation threshold or the frame size.

#### In practice...
~~~java


BeadImageManager manager = new BeadImageManager();

// an object representing the image is created, however, the image is not yet loaded
BeadImage image = new BeadImage("path/to/image");

image.setMicroscope(new Microscope(new IniFile("path/to/microscope_configuration.ini")));

// loads the image from the memory
image.workFromMemory();

// Calculates the focal plane automatically
image.autoFocus();

// set the bead frame to 20 times the thoretical x/y resolution
image.setBeadEnlargement(20);

manager.add(image);

// make sure all the parameters are set
manager.verifyBeadImageParameters();

// do the processing
manager.processFiles();

// will export a PDF sum up in the same folder as the image
manager.exportPDFSumUp(false);

~~~

A good example is to look at the PSFj.java file which more extensively the BeadImageManager and also deals with the multichannel cases.

### Diving into the data model

... coming soon.

## Licencing

PSFj is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.