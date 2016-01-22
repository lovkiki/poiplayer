/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poiplayer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author WheatBerry
 */
public class UIUpdater extends Thread{
    private boolean enable = false;
    private boolean dead = false;
    private JComponent jc;
    private JFrame parent;
    public UIUpdater(JComponent _jc, JFrame _parent){
        jc = _jc;
        parent = _parent;
    }
    @Override
    public void run(){
        while(!dead){
            try {
                if(enable){
                    jc.repaint();
                    parent.repaint();
                }
                sleep(35);
            } catch (InterruptedException ex) {
                Logger.getLogger(UIUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void Enable(){
        enable = true;
    }
    public void Disable(){
        enable = false;
    }
    public void Dead(){
        dead = true;
    }
}
