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

import ij.IJ;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import knop.psfj.PSFj;
import knop.psfj.SplashScreen;
import knop.psfj.utils.MemoryUtils;

import org.swixml.SwingEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class WizardWindow.
 */
public class WizardWindow implements Observer {

	/** The engine. */
	SwingEngine engine;

	/** The next button. */
	JButton nextButton;
	
	/** The previous button. */
	JButton previousButton;

	/** The title label. */
	JLabel titleLabel;
	
	/** The explaination label. */
	JLabel explainationLabel;

	/** The page container. */
	JPanel pageContainer;

	/** The progress bar. */
	JProgressBar progressBar;
	
	/** The progress label. */
	JLabel progressLabel;

	/** The runtime. */
	Runtime runtime = Runtime.getRuntime();

	/** The main frame. */
	JFrame mainFrame;

	/** The memory progress bar. */
	protected JProgressBar memoryProgressBar;
	
	/** The memory label. */
	protected JLabel memoryLabel;

	/** The pages. */
	ArrayList<WizardPage> pages = new ArrayList<WizardPage>();
	
	/** The current page. */
	private int currentPage = 0;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new WizardWindow();
	}

	/**
	 * Instantiates a new wizard window.
	 */
	public WizardWindow() {
		this(true);
	}

	/**
	 * Instantiates a new wizard window.
	 *
	 * @param appMode the app mode
	 */
	public WizardWindow(final boolean appMode) {
		engine = new SwingEngine(this);

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		engine = new SwingEngine(this);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			engine.render("knop/psfj/view/WizardWindow.xml").setVisible(false);
			memoryUpdateThread.start();

			
			
			
			//size of the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//screenSize.setSize(screenSize.getWidth()-40, screenSize.getHeight()-40);
			mainFrame.setSize(new Dimension(screenSize.width-40,screenSize.height-40));
			
			//height of the task bar
			Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(mainFrame.getGraphicsConfiguration());
			int taskBarSize = scnMax.bottom;
			
			//available size of the screen 
			mainFrame.setLocation((screenSize.width - mainFrame.getWidth())/2, 0);
			
			
		
			mainFrame.setTitle(String.format(mainFrame.getTitle(),
					PSFj.getVersion()));
			
			explainationLabel.setOpaque(true);
			explainationLabel.setBackground(Color.white);

			

			if (appMode) {
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				try {
				applyAppleCompatibility();
				}
				catch (ClassNotFoundException e) {
					System.out.println("This is not an apple computer.");
				}
				mainFrame.addWindowListener(new WindowListener() {

					@Override
					public void windowOpened(WindowEvent arg0) {
					}
					
					@Override
					public void windowIconified(WindowEvent arg0) {
					}

					@Override
					public void windowDeiconified(WindowEvent arg0) {
					}

					@Override
					public void windowDeactivated(WindowEvent arg0) {
					}

					@Override
					public void windowClosing(WindowEvent arg0) {
						System.out.println("closing !");
					}

					@Override
					public void windowClosed(WindowEvent arg0) {
						
						if(appMode) System.exit(0);
					}

					@Override
					public void windowActivated(WindowEvent arg0) {

					}
				});

				mainFrame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we) {
						System.exit(0);
					}
				});
				
				
				sp.setLocationRelativeTo(mainFrame);
				

			} else {
				mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Show.
	 */
	public void show() {
		mainFrame.setVisible(true);
		
		if(sp != null) sp.setVisible(false);
	}

	/**
	 * Apply apple compatibility.
	 *
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void applyAppleCompatibility() throws ClassNotFoundException {
		try {
			
			OSXAdapter.setQuitHandler(this, this.getClass().getMethod("onQuit", null));
			
		} catch (Exception e) {

		}
	}

	/**
	 * Adds the page.
	 *
	 * @param p the p
	 */
	public void addPage(WizardPage p) {

		pages.add(p);
		p.addObserver(this);

	}
	
	/**
	 * On quit.
	 */
	public void onQuit() {
		System.out.println("oups there it is !");
	}
	
	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public WizardPage getCurrentPage() {
		if (currentPage < pages.size())
			return pages.get(currentPage);
		else
			return null;
	}

	/**
	 * Update title.
	 */
	public void updateTitle() {
		explainationLabel
		.setText("<html><div style='width:600px;padding:10px'><span style='font-size:2em'>"
				+ getCurrentPage().getTitle()
				+ "</span><br><br>"
				+ getCurrentPage().getExplaination() + "</div></html>");
		
	}
	
	/**
	 * Sets the current page.
	 *
	 * @param i the new current page
	 */
	public void setCurrentPage(int i) {
		
		
		
		if(i > currentPage && getCurrentPage().askBeforeLeavingForward() == false)return;
		
		
		if(i!=0) showLoading(true);
		if (i >= 0 && i < pages.size()) {

			if (currentPage < i && getCurrentPage() != null) {
				getCurrentPage().onLeavingForward();
			} else if (currentPage > i && getCurrentPage() != null) {
				getCurrentPage().onLeavingBackWard();
			}

			currentPage = i;
		} else {
			showLoading(false);
			return;
		}
		System.out.println("Page changing ! " + currentPage);
		pageContainer.removeAll();
		pageContainer.add(getCurrentPage().getComponent());
		
		// titleLabel.setText(getCurrentPage().getTitle());
		
		
		new Thread(){
			public void run() {
		//pageContainer.updateUI();
		//pageContainer.repaint();
				
				getCurrentPage().onDisplay();
				//pageContainer.setVisible(true);
				updateTitle();
				
				updateButtons();

				showLoading(false);
			}
		}.start();
		
	}

	/**
	 * Notify view change.
	 */
	public void notifyViewChange() {
		updateButtons();
	}

	/**
	 * Sets the progress.
	 *
	 * @param status the status
	 * @param value the value
	 */
	public void setProgress(String status, int value) {

		progressBar.setVisible((value > 0));
		progressBar.setEnabled((value > 0));
		progressLabel.setVisible((value > 0));

		progressBar.setValue(value);
		progressLabel.setText(status);

		progressBar.repaint();
		progressLabel.repaint();

	}

	/**
	 * Update buttons.
	 */
	public void updateButtons() {

		if (currentPage == 0 || getCurrentPage().isBackPossible() == false) {
			previousButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
		}

		if (itIsTheEnd()) {

			if (!previousButton.getText().equals(" Restart ")) {
				previousButton.setText(" Restart ");

				nextButton.setText(" Quit ");

			}

			previousButton.setEnabled(true);
			nextButton.setEnabled(true);

		} else {
			if (!previousButton.getText().equals(" Previous ")) {
				previousButton.setText(" Previous ");
				nextButton.setText(" Next ");

			}
			nextButton.setEnabled(getCurrentPage().isReady());
		}

	}

	/**
	 * It is the end.
	 *
	 * @return true, if successful
	 */
	private boolean itIsTheEnd() {
		return currentPage == getPageNumber() - 1;
	}

	/**
	 * Gets the page number.
	 *
	 * @return the page number
	 */
	private int getPageNumber() {
		// TODO Auto-generated method stub
		return pages.size();
	}

	/**
	 * Gets the swing engine.
	 *
	 * @return the swing engine
	 */
	public SwingEngine getSwingEngine() {
		// TODO Auto-generated method stub
		return engine;
	}

	/** The on next button pressed. */
	public AbstractAction onNextButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub

			if (itIsTheEnd()) {
				if (IJ.showMessageWithCancel("Quit",
						"Are you sure you want to quit ?")) {
					mainFrame.dispose();
					System.exit(0);
					return;
				} else
					return;
			}

			if (currentPage == (pages.size() - 1)) {
				return;
			}

			else {
				if (getCurrentPage().isReady()) {

					setCurrentPage(currentPage + 1);
					updateButtons();
				}
			}
		}
	};

	/** The on previous button pressed. */
	public AbstractAction onPreviousButtonPressed = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (itIsTheEnd()) {
				showLoading(true);
				setCurrentPage(0);
				getCurrentPage().update(null, new Message(WizardWindow.this,"reset"));
				return;
			}
			if (currentPage != 0 && getCurrentPage().isBackPossible()) {
				setCurrentPage(currentPage - 1);
				updateButtons();
			}
		}
	};

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

		if (arg instanceof Message) {
			update((Message) arg);
		}

		if (getCurrentPage() != null) {
			getCurrentPage().update(o, arg);

			updateButtons();
		} else {
			System.err.println("getCurrentPage() == null ???");
		}

		updateMemoryUsage();

	}

	/** The memory update thread. */
	Thread memoryUpdateThread = new Thread() {
		public void run() {

			while (true) {
				updateMemoryUsage();
				try {
					sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}

		}
	};

	/** The error queue. */
	ArrayList<Message> errorQueue = new ArrayList<Message>();
	
	
	/**
	 * Update.
	 *
	 * @param message the message
	 */
	public void update(Message message) {

		
		
		
		if (message.getName().contains("error")) {
			
			if(errorQueue.contains(message) == false) {
			
			JOptionPane.showMessageDialog(mainFrame, message.getStringData(),
					"Error", JOptionPane.ERROR_MESSAGE);
			errorQueue.add(message);
			}
		}
		
		if(message.getName().contains("warning")) {
			JOptionPane.showMessageDialog(mainFrame, message.getStringData(),
					"Warning", JOptionPane.WARNING_MESSAGE);
			
			}
		
		if(message.getName() == WizardPage.MSG_CHECK_IF_READY) {
			updateButtons();
		}
		
		
		if (message.getName() == "change page request") {
			setCurrentPage(message.getIntData());
		}
		if (message.getName().equals("go to next page request")
				&& getCurrentPage().isReady()) {
			setCurrentPage(currentPage + 1);
		}

		if (message.getName().equals("go to previous page request")) {
			setCurrentPage(currentPage - 1);
		}

		if (message.getName().contains("progress changed")
				|| message.getName().contains("heatmap")) {
			if (message.getIntData() != null && message.getStringData() != null) {
				setProgress(message.getStringData(), message.getIntData());
			}
		}
		
		if (message.getName().contains("title updated")) {
			updateTitle();
		}
		
		progressBar.setVisible(progressBar.getValue() > 0);
		
	}

	/**
	 * Update memory usage.
	 */
	public synchronized void updateMemoryUsage() {
		// System.out.println("max memory : "+
		// (runtime.maxMemory()-runtime.freeMemory()));
		
		int free = (int) MemoryUtils.getAvailableMemory();
		int max = (int) MemoryUtils.getTotalMemory();
		int used = (max - free);

		int progress = 100 * (used) / max;

		// System.out.println(progress);
		memoryProgressBar.setValue(progress);
		memoryLabel.setText(String.format("%d / %d MB", used, max));
		// memoryProgressBar.setString(String.format("%d/%d MB",max,free));

	}

	/** The on windows resize. */
	ComponentAdapter onWindowsResize = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			if (getCurrentPage() != null)
				for(ComponentListener listener : getCurrentPage().getComponent().getListeners(ComponentListener.class)) {
					listener.componentResized(e);
				}
		}
	};

	SplashScreen sp;
	
	public void showLoading(final boolean show) {
		//sp.setLocationRelativeTo(mainFrame);
		
		if(sp == null) sp = new SplashScreen("/pleasewait.gif", (long)0);
	
		sp.setVisible(show);
		
		sp.setLocation(mainFrame.getX()+mainFrame.getWidth()/2-sp.getWidth()/2, mainFrame.getY()+mainFrame.getHeight()/2-sp.getHeight()/2);
		
		
		
	}
	
	
}
