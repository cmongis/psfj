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

import ij.ImagePlus;
import knop.psfj.utils.MemoryUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class BeadFrameProcessorTester.
 */
public class BeadFrameProcessorTester extends BeadFrameProcessorAsync {

	/** The stats. */
	FovDataSet stats = new FovDataSet();
	
	/**
	 * Instantiates a new bead frame processor tester.
	 *
	 * @param list the list
	 */
	public BeadFrameProcessorTester(BeadFrameList list) {
		super(list);
		// TODO Auto-generated constructor stub
		
	}

	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrameProcessor#init()
	 */
	@Override
	public void init() {
		stats = new FovDataSet();
		
		super.init();
		
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.BeadFrameProcessor#refresh()
	 */
	@Override
	public void refresh() {
		long now = System.currentTimeMillis();
		stats.addValue("t", now-startTime);
		stats.addValue("speed", getBeadPerSecond());
		stats.addValue("memory",MemoryUtils.getTotalMemory()-MemoryUtils.getAvailableMemory());
		super.refresh();
	}
	
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		
		BeadImage image = new BeadImage();
		image.setFileAddress("/home/cyril/test_img/colocalisation/gfp1.tif");
		image.workFromMemory();
		image.autoThreshold();
		image.autoFrameSize();
		
		
		
		
		FovDataSet globalStats = new FovDataSet();
		
		
		for(int thread = 1; thread != 20; thread ++) {
			BeadFrameProcessorTester tester = new BeadFrameProcessorTester(image.getBeadFrameList());
			image.buildStackList();
			tester.setThreadNumber(thread);
			tester.process();
			
			FovDataSet runStats = tester.getStats();
			
			/*
			ImagePlus graph1 = new ImagePlus("thread = "+thread,tester.getStats().plotColumns("t", "speed", 400, 400));
			ImagePlus graph2 = new ImagePlus("thread = "+thread,tester.getStats().plotColumns("t", "memory", 400, 400));
			
			graph1.show();
			graph2.show();
			*/
			globalStats.addValue("thread", thread);
			globalStats.addValue("maxSpeed", runStats.getColumnStatistics("speed").getPercentile(50));
			globalStats.addValue("maxMemory", runStats.getColumnStatistics("memory").getPercentile(50));
			
			image.autoThreshold();
			
		}
		
		new ImagePlus("speed / thread",globalStats.plotColumns("thread", "maxSpeed", 600, 600)).show();
		new ImagePlus("memory / thread",globalStats.plotColumns("thread", "maxMemory", 600, 600)).show();
		
		globalStats = new FovDataSet();
		
		for(int gf = 1; gf <= 100; gf+=5) {
			BeadFrameProcessorTester tester = new BeadFrameProcessorTester(image.getBeadFrameList());
			image.buildStackList();
			tester.setThreadNumber(5);
			tester.process();
			
			tester.setGarbageCollectionFrequence(gf);
			
			
			FovDataSet runStats = tester.getStats();
			
			/*
			ImagePlus graph1 = new ImagePlus("thread = "+thread,tester.getStats().plotColumns("t", "speed", 400, 400));
			ImagePlus graph2 = new ImagePlus("thread = "+thread,tester.getStats().plotColumns("t", "memory", 400, 400));
			
			graph1.show();
			graph2.show();*/
			
			globalStats.addValue("gf", gf);
			globalStats.addValue("maxSpeed", runStats.getColumnStatistics("speed").getPercentile(50));
			globalStats.addValue("maxMemory", runStats.getColumnStatistics("memory").getPercentile(50));
			
			image.autoThreshold();
			
		}
		
		
		
		new ImagePlus("speed / gf",globalStats.plotColumns("gf", "maxSpeed", 600, 600)).show();
		new ImagePlus("memory / gf",globalStats.plotColumns("gf", "maxMemory", 600, 600)).show();
		
		
		
		/*
		for(int gf = 1; gf!=40;gf++) {
			BeadFrameProcessorTester tester = new BeadFrameProcessorTester(image.getBeadFrameList());
			tester.setGarbageCollectionFrequence(gf);
			tester.process();
			ImagePlus graph1 = new ImagePlus("gf = "+thread,tester.getStats().plotColumns("t", "speed", 400, 400));
			ImagePlus graph2 = new ImagePlus("gf = "+thread,tester.getStats().plotColumns("t", "memory", 400, 400));
		}*/
		
		
	}
	
	/**
	 * Gets the stats.
	 *
	 * @return the stats
	 */
	public FovDataSet getStats() {
		return stats;
	}
	
	
}
