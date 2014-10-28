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

import ij.process.ByteProcessor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import knop.psfj.BeadImageManager;
import knop.psfj.PSFj;
import knop.psfj.graphics.FullHeatMap;
import knop.psfj.graphics.PsfJGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class HeatMapTable.
 */
public class HeatMapTable extends JPanel {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		BeadImageManager manager = new BeadImageManager();
		manager.add("/home/cyril/test_img/6_small.tif");
		//manager.add("/home/cyril/test_img/colocalisation/mc_01_220beads_small.tif");
		manager.processProfiles();
		
		
		HeatMapTable table = new HeatMapTable(manager);
		
		BufferedImage image = (new ByteProcessor(100,100)).getBufferedImage();
		
		table.addHeatmaps();
		JFrame frame = new JFrame();
		frame.setSize(400,400);
		frame.setLayout(new BorderLayout(5,5));
		
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(table,BorderLayout.NORTH);
		panel.add(new JPanel(),BorderLayout.CENTER);
		
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.show();
	
		
		
		
	}
	
	/** The manager. */
	BeadImageManager manager;
	
	/** The card boxes. */
	HashMap<String,CardBox> cardBoxes = new HashMap<String, HeatMapTable.CardBox>();
	
	
	/** The container. */
	JPanel container = new JPanel();
	
	/**
	 * Instantiates a new heat map table.
	 *
	 * @param manager the manager
	 */
	public HeatMapTable(BeadImageManager manager) {
		this();
		this.manager = manager;
		
	}
	
	/**
	 * Instantiates a new heat map table.
	 */
	public HeatMapTable() {
		super(new BorderLayout());
		add(container,BorderLayout.NORTH);
		add(new JPanel(),BorderLayout.CENTER);
		container.setLayout(new BoxLayout(container,BoxLayout.PAGE_AXIS));
		
	}
	
	/**
	 * Sets the manager.
	 *
	 * @param m the new manager
	 */
	public void setManager(BeadImageManager m) {
		this.manager = m;
		
		container.removeAll();
		
	}
	
	/**
	 * Adds the heatmaps.
	 */
	public void addHeatmaps() {
		
		
		
		if(manager.getAnalysisType() == BeadImageManager.SINGLE_CHANNEL) {
			
			
			addBox("fwhm","Full Width Half Maximum (min, max and Z)","green is close to the theoretical");
			addBox("other","Other","");
			
			for(int i=0;i!=3;i++)
			addGraphs("fwhm",PSFj.getHeatmapName(PSFj.FWHM_KEY, i, -1));
			
			addGraphs("other",
					PSFj.getHeatmapName(PSFj.Z_PROFILE, -1),
					PSFj.getHeatmapName(PSFj.ASYMMETRY_KEY, -1),
					PSFj.getHeatmapName(PSFj.THETA_KEY, -1)
					);
			
			
		}
		
		
		else {
			
			addBox("chr","Chromatic shifts","");
			addBox("channel0",manager.getBeadImage(0).getMicroscope().getWaveLengthAsString(),"");
			addBox("channel0_bis","","");
			addBox("channel1",manager.getBeadImage(1).getMicroscope().getWaveLengthAsString(),"");
			addBox("channel1_bis","","");
			
			
			
			addGraphs("channel0","fwhmX_0","fwhmY_0","fwhmZ_0");
			addGraphs("channel0_bis","z_profile_0","asymmetry_0","theta_0");
			addGraphs("channel1","fwhmX_1","fwhmY_1","fwhmZ_1");
			addGraphs("channel1_bis","z_profile_1","asymmetry_1","theta_1");
			
			addGraphs("chr","deltaD","delta3D","deltaZ");
			
		}
		
		
		
		
		
	}
	
	
	/**
	 * Adds the graphs.
	 *
	 * @param args the args
	 */
	public void addGraphs(String...args) {
		if(args.length > 1) {
			for(int i = 1;i!=args.length;i++) {
				addGraph(args[0],args[i]);
			}
		}
	}
	
	/**
	 * Adds the graph.
	 *
	 * @param boxId the box id
	 * @param graphId the graph id
	 */
	public void addGraph(String boxId, String graphId) {
		PsfJGraph graph = manager.getGraph(graphId);
		if(graph == null) {
			System.err.println("Couln't graph "+graphId);
			return;
		}
		if(FullHeatMap.class.isAssignableFrom(graph.getClass())) {
			FullHeatMap heatmap = (FullHeatMap) graph;
			addCard(boxId,new Card(heatmap.getTitle(),heatmap.getGenerator(PSFj.NOT_NORMALIZED).getColoredHeatMap(120).getBufferedImage(),""));
			System.gc();
		}
		else {
			addCard(boxId,new Card(graph.getTitle(),graph.getGraph().getBufferedImage(),""));
		}
		
	}
	
	/**
	 * Adds the card.
	 *
	 * @param boxId the box id
	 * @param card the card
	 */
	public void addCard(String boxId, Card card) {
		cardBoxes.get(boxId).addCard(card);
	}
	
	/**
	 * Adds the box.
	 *
	 * @param cardBox the card box
	 */
	public void addBox(CardBox cardBox) {
		
		container.add(cardBox);
	}
	
	
	
	/**
	 * Adds the box.
	 *
	 * @param id the id
	 * @param title the title
	 * @param description the description
	 * @return the string
	 */
	public String addBox(String id, String title, String description) {
		CardBox cardBox = new CardBox(title);
		cardBoxes.put(id, cardBox);
		container.add(cardBox);
		return id;
	}
	
	/**
	 * The Class Card.
	 */
	public class Card extends JPanel {
		
		/**
		 * Instantiates a new card.
		 *
		 * @param title the title
		 * @param image the image
		 * @param description the description
		 */
		public Card(String title, BufferedImage image, String description) {
			super(new BorderLayout());
			setBorder(new EmptyBorder(2,2,2,2));
			
			
			
			String formatedTitle = String.format("<html><div style='width:90px;text-align:center'> %s </div></html>",title);
			
			JLabel titleLabel = new JLabel(formatedTitle);
			titleLabel.setPreferredSize(new Dimension(100,10*3));
			titleLabel.setFont(new Font("Default",Font.BOLD,10));
			JLabel descriptionLabel = new JLabel(description);
			
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			
			add(titleLabel,BorderLayout.NORTH);
			add(new JLabel(new ImageIcon(image)),BorderLayout.CENTER);
			add(descriptionLabel,BorderLayout.SOUTH);
			setMaximumSize(getPreferredSize());
			
		}
	}
	
	
	
	
	
	/**
	 * The Class CardBox.
	 */
	public class CardBox extends JPanel {
		
		/** The title. */
		JLabel title;
		
		/** The card container. */
		JPanel cardContainer;
		
		/**
		 * Instantiates a new card box.
		 *
		 * @param title the title
		 */
		public CardBox(String title) {
			
			super(new BorderLayout());
			
			this.title = new JLabel(title);
			this.title.setFont(new Font("Default",Font.BOLD,16));
			this.title.setBorder(new EmptyBorder(10,10,2,2));
			//this.setOpaque(true);
			//this.setBackground(Color.white);
			cardContainer = new JPanel();
			cardContainer.setLayout(new BoxLayout(cardContainer,BoxLayout.LINE_AXIS));
			add(this.title,BorderLayout.NORTH);
			add(cardContainer,BorderLayout.CENTER);
		}
		
		/**
		 * Adds the card.
		 *
		 * @param card the card
		 */
		public void addCard(Card card) {
			cardContainer.add(card);
		}
		
		
	}
	
	
	
	
	
}
