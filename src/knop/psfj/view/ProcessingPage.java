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
package knop.psfj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.utils.TextUtils;

import org.swixml.XVBox;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessingPage.
 */
public class ProcessingPage extends WizardPage {

	/** The main panel. */
	JPanel mainPanel = new JPanel(new BorderLayout());

	/** The is finished. */
	boolean isFinished = false;
	
	/** The list. */
	JList list = new JList();
	
	/** The model. */
	BeadImageLabelListModel model = new BeadImageLabelListModel();
	
	/** The label. */
	JLabel label;
	
	/** The img. */
	ImageIcon img;
	
	/** The image container. */
	XVBox imageContainer = new XVBox();
	
	/** The label list. */
	HashMap<BeadImage,ProfilesAnalyserLabel> labelList = new HashMap<BeadImage, ProfilesAnalyserLabel>();
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		WizardWindow wizardWindow = new WizardWindow();
		BeadImageManager manager = new BeadImageManager();
		
		
		
		manager.addObserver(wizardWindow);
		
		
		wizardWindow.addPage(new ProcessingPage(manager));
		wizardWindow.setCurrentPage(0);
	}
	
	/**
	 * Update title.
	 *
	 * @param processing the processing
	 */
	public void updateTitle(boolean processing) {
		
		
		if(processing) {
			setTitle("Processing...");
			setExplaination("Please wait for the end of the processing...");
		}
		else {
			setTitle("Processing done.");
			setExplaination("Please click on the \"next\" button.");
		}
		
		notifyTitleUpdate();
		
	}
	
	/**
	 * Instantiates a new processing page.
	 *
	 * @param manager the manager
	 */
	public ProcessingPage(BeadImageManager manager) {
		setBeadImageManager(manager);
		
		
		JScrollPane listPane = new JScrollPane(list);
		//listPane.add(list);
		listPane.setVisible(true);
		list.setModel(model);
		mainPanel.add(listPane,BorderLayout.WEST);
		
		label = new JLabel(TextUtils.readTextRessource(this, "/licence.html"));
		//mainPanel.setBackground(Color.white);
		//mainPanel.setOpaque(true);
		
		
		
		try {
		img = new ImageIcon(getClass().getResource("/knoplablogo.png"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(img == null) {
			try {
			img = new ImageIcon(getClass().getResource("src/knoplablogo.png"));
			
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(img != null) {
			//imageContainer.add(new JLabel(img));
			//label.setOpaque(true);
			label.setBorder(new EmptyBorder(10, 10, 10, 10));
			//label.setBackground(Color.white);
			label.setMaximumSize(new Dimension(img.getIconWidth(),200));
			
		}
		imageContainer.add(label);
		mainPanel.add(imageContainer,BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object message) {
		if (message instanceof Message) {
			update((Message) message);
		}
	}

	/**
	 * Update.
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		
		
	
		if(message.getOrigin() instanceof BeadImageManager) {
			if(message.getName().equals("process finished")) {
				System.out.println("It's finished !!!!");
				
				updateTitle(false);
				
				isFinished = true;
				
				
				
			}
		}
		
		model.updateListFromManager();
		list.repaint();
		
		

	}

	/**
	 * Setup.
	 */
	public void setup() {

		model.clear();
		list.setCellRenderer(BeadImageLabel.getRenderer());
		model.setManager(getBeadImageManager());		
		model.updateListFromManager();
		

	}

	
	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#onDisplay()
	 */
	public void onDisplay() {
		setup();
		isFinished = false;
		updateTitle(true);
		new Thread() {
			public void run() {
				try {
					
					if(getBeadImageManager().countBeadImage() > 0) getBeadImageManager().processProfiles();
				}
				catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainPanel, "Error when processing result... try again ?", "Error",JOptionPane.ERROR_MESSAGE);
					notifyObservers(new Message(this,"go to previous page request"));
				}
			}
		}.start();
		
		
	}
	
	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isReady()
	 */
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return isFinished;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isBackPossible()
	 */
	@Override
	public boolean isBackPossible() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#isQuitOkay()
	 */
	@Override
	public boolean isQuitOkay() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see knop.psfj.view.WizardPage#getComponent()
	 */
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	/**
	 * The Class ProfilesAnalyserLabel.
	 */
	private class ProfilesAnalyserLabel extends XVBox implements Observer {

		/** The progress bar. */
		protected JProgressBar progressBar = new JProgressBar();
		
		/** The label. */
		protected JLabel label = new JLabel();
		
		/** The bead image. */
		protected BeadImage beadImage;

		
		/** The progress. */
		int progress = 0;
		
		/** The status. */
		String status = "";
		
		/**
		 * Instantiates a new profiles analyser label.
		 *
		 * @param i the i
		 */
		public ProfilesAnalyserLabel(BeadImage i) {
			beadImage = i;

			add(label);
			//add(progressBar);
			beadImage.addObserver(this);
			update("");
			update(0);

		}

		/**
		 * Reject from return.
		 *
		 * @return true, if successful
		 */
		public boolean rejectFromReturn() {
			return true;
		}
		
		
		/**
		 * Update.
		 */
		public void update() {
			String name;
			if(beadImage != null)
			name = beadImage.getImageName();
			else name = "";
			
			String text = String.format(
					"<html>" +
					"<div style='background:white;padding:5px;width:500px'>" +
					"<b>%s</b><br>%s<br>" +
					"<div style='height:15px;width:%d%%;background:#1E90FF;margin-top:7px'></div></div></html>", name,status,progress);
			
			label.setText(text);
		}
		
		/**
		 * Update.
		 *
		 * @param status the status
		 */
		public void update(String status) {
			this.status = status;
			update();
		}

		/**
		 * Update.
		 *
		 * @param progress the progress
		 */
		public void update(int progress) {
			this.progress=progress;
			update();
		}

		/* (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		@Override
		public void update(Observable arg0, Object arg1) {
			// TODO Auto-generated method stub
			if (arg1 instanceof Message) {
				Message message = (Message) arg1;

				if (message.getOrigin() == beadImage) {
					if(message.getIntData() != null)
						update(message.getIntData());
					if(message.getStringData() != null) {
						update(message.getStringData().toString());
					}
				}
				
				list.repaint();
			}
		}

	}

}
