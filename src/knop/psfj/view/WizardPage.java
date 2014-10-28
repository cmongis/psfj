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

import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

import knop.psfj.BeadImageManager;
import knop.psfj.utils.FileUtils;

import org.swixml.SwingEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class WizardPage.
 */
public abstract class WizardPage implements Observer{
	
	
	/** The bead image manager. */
	BeadImageManager beadImageManager;
	
	
	/** The title. */
	String title = "No title";
	
	/** The explaination. */
	String explaination = "Please put here some explaination :-)";
	
	
	/** The observable. */
	private ObserverManager observable = new ObserverManager();
	
	
	
	public static final String MSG_CHECK_IF_READY = "check if ready";
	
	/**
	 * Instantiates a new wizard page.
	 *
	 * @param manager the manager
	 */
	public WizardPage(BeadImageManager manager) {
		this();
		setBeadImageManager(manager);
	}
	
	/**
	 * Sets the bead image manager.
	 *
	 * @param beadImageManager the new bead image manager
	 */
	public void setBeadImageManager(BeadImageManager beadImageManager) {
		
		this.beadImageManager = beadImageManager;
	}


	/**
	 * Assign icon to button.
	 *
	 * @param id the id
	 * @param iconPath the icon path
	 */
	public void assignIconToButton(String id, String iconPath) {
		assignIconToButton(id,new ImageIcon(FileUtils.loadImageRessource(this, iconPath)));
	}

	
	/**
	 * Assign system icon to button.
	 *
	 * @param id the id
	 * @param iconId the icon id
	 */
	public void assignSystemIconToButton(String id, String iconId) {
		try {
		assignIconToButton(id,(ImageIcon)UIManager.getIcon(iconId));
		}
		catch(Exception e) {
			System.err.println("System icon couldn't be assigned... well it's not a big lose.");
		}
	}
	
	/**
	 * Assign icon to button.
	 *
	 * @param id the id
	 * @param icon the icon
	 */
	public void assignIconToButton(String id, ImageIcon icon) {
		JButton c =  (JButton)engine.getIdMap().get(id);
		
		if(c == null) {
			System.err.println(id + " not found ! Cannot assign icon");
			return;
		}
		c.setIcon(icon);
	}
	
	
	/**
	 * Assign icon to label.
	 *
	 * @param id the id
	 * @param iconPath the icon path
	 */
	public void assignIconToLabel(String id, String iconPath) {
		assignIconToLabel(id,new ImageIcon(FileUtils.loadImageRessource(this, iconPath)));
	}
	
	/**
	 * Assign icon to label.
	 *
	 * @param label the label
	 * @param iconPath the icon path
	 */
	public void assignIconToLabel(JLabel label, String iconPath) {
		JLabel c =  label;
		
		if(c == null) {
			System.err.println("Cannot assign icon");
			return;
		}
		c.setIcon(new ImageIcon(FileUtils.loadImageRessource(this, iconPath)));
	}
	
	/**
	 * Assign icon to label.
	 *
	 * @param id the id
	 * @param icon the icon
	 */
	public void assignIconToLabel(String id, ImageIcon icon) {
		JLabel c =  (JLabel)engine.getIdMap().get(id);
		
		if(c == null) {
			System.err.println(id + " not found ! Cannot assign icon");
			return;
		}
		c.setIcon(icon);
	}

	public boolean askBeforeLeavingForward() {
		return true;
	}
	
	
	/**
	 * Checks if is ready.
	 *
	 * @return true, if is ready
	 */
	public abstract boolean isReady();
	
	/**
	 * Checks if is back possible.
	 *
	 * @return true, if is back possible
	 */
	public abstract boolean isBackPossible();
	
	/**
	 * Checks if is quit okay.
	 *
	 * @return true, if is quit okay
	 */
	public abstract boolean isQuitOkay();
	
	/**
	 * Gets the component.
	 *
	 * @return the component
	 */
	public abstract Component getComponent();
	
	/**
	 * On display.
	 */
	public abstract void onDisplay();
	
	
	/** The engine. */
	SwingEngine engine;
	
	
	/**
	 * Instantiates a new wizard page.
	 */
	public WizardPage() {
		engine = new SwingEngine(this);
		
	}
	
	
	
	/**
	 * Inits the.
	 */
	public void init() {
		
	}
	
	


	/**
	 * Gets the bead image manager.
	 *
	 * @return the bead image manager
	 */
	public BeadImageManager getBeadImageManager() {
		return beadImageManager;
	}
	
	/**
	 * Gets the swing engine.
	 *
	 * @return the swing engine
	 */
	public SwingEngine getSwingEngine() {
		return engine;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	
	
	
	/**
	 * Gets the explaination.
	 *
	 * @return the explaination
	 */
	public String getExplaination() {
		return explaination;
	}

	/**
	 * Notify title update.
	 */
	public void notifyTitleUpdate() {
		notifyObservers(new Message(this,"title updated"));
	}
	/**
	 * Ask the Wizard Window Object to check if the page is ready to go on again
	 */
	public void notifyReadyStateChange() {
		notifyObservers(new Message(this,MSG_CHECK_IF_READY));
	}
	
	
	/**
	 * Update title.
	 *
	 * @param title the title
	 */
	public void updateTitle(String title) {
		setTitle(title);
		notifyTitleUpdate();
	}
	
	/**
	 * Update explaination.
	 *
	 * @param explaination the explaination
	 */
	public void updateExplaination(String explaination) {
		setExplaination(explaination);
		notifyTitleUpdate();
	}
	
	/**
	 * Sets the explaination.
	 *
	 * @param explaination the new explaination
	 */
	public void setExplaination(String explaination) {
		this.explaination = explaination;
	}

	/**
	 * On resize.
	 *
	 * @param e the e
	 */
	public void onResize(Component e) {
	}
	
	
	/**
	 * Accept back ward.
	 *
	 * @return true, if successful
	 */
	public boolean acceptBackWard() {
		return true;
	}
	
	/**
	 * On leaving back ward.
	 */
	public void onLeavingBackWard() {
		
	}
	
	/**
	 * On leaving forward.
	 */
	public void onLeavingForward() {
		
	}
	
	/**
	 * Adds the observer.
	 *
	 * @param o the o
	 */
	public void addObserver(Observer o) {
		observable.addObserver(o);
	}
	
	/**
	 * Notify observers.
	 *
	 * @param o the o
	 */
	public void notifyObservers(Object o ) {
		observable.notifyObservers(o);
	}
	
	
	
	/**
	 * The Class ObserverManager.
	 */
	private class ObserverManager extends Observable {
		
		/* (non-Javadoc)
		 * @see java.util.Observable#notifyObservers(java.lang.Object)
		 */
		public void notifyObservers(Object o) {
			super.setChanged();
			super.notifyObservers(o);
		}
	}
	
	
}
