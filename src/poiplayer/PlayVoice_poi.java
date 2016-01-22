/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poiplayer;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


/**
 *
 * @author WheatBerry
 * 这是播放poi语音的，不需要做别的处理，所以用了jLayer封装好的Player，非常简单
 * 目前用在将文件或文件夹拖入播放器时的提示音
 */
public class PlayVoice_poi extends Thread{
    FileInputStream fis;
    java.net.URL poiVoice;
    public void play(){}
    @Override
    public void run(){
        try {
            Player p = null;
            p = new Player(getClass().getClassLoader().getResourceAsStream("voice/POI_08.mp3"));
            p.play();
        } catch (JavaLayerException ex) {
            Logger.getLogger(PlayVoice_poi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
