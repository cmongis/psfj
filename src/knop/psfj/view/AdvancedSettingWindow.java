/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.psfj.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knop.psfj.BeadImageManager;
import knop.psfj.locator.BeadLocator2D;
import knop.psfj.locator.BeadLocator3D;
import org.swixml.SwingEngine;

/**
 *
 * @author cyril
 */
public class AdvancedSettingWindow {

    /**
     * The engine.
     */
    SwingEngine engine;
    
    JFrame frame;

    BeadImageManager manager = new BeadImageManager();
    
    JLabel titleLabel;
    
    JCheckBox localizationCheckBox;
    
    public AdvancedSettingWindow() {
        engine = new SwingEngine(this);
        try {

            engine.render("knop/psfj/view/AdvancedSettingWindow.xml").setVisible(false);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

       
        } catch (Exception ex) {
            Logger.getLogger(AdvancedSettingWindow.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.white);
        titleLabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20));
        titleLabel.setText("<html><div style='padding:10px'>Advanced Settings</div></html>");
        
        localizationCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updateModel();
            }
        });
        
        
    }
    
    public void show() {
        updateView();
        frame.show();
    }
    
    public static void main(String[] args) {
        new AdvancedSettingWindow().show();
    }

    
    public void updateModel() {
        
        
        
        if(localizationCheckBox.isSelected()) {
            if(manager.getLocator() instanceof BeadLocator2D) {
                manager.setLocator(new BeadLocator3D());
            }
        }
        else {
            if(manager.getLocator() instanceof BeadLocator3D) {
                manager.setLocator(new BeadLocator2D());
            }
        }
    }
    
    public void updateView() {
        localizationCheckBox.setSelected(manager.getLocator() instanceof BeadLocator3D);
    }
    
    public void close() {
        frame.show(false);
    }

    void setManager(BeadImageManager beadImageManager) {
        manager = beadImageManager;
    }
}
