/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import org.jtransforms.fft.FloatFFT_1D;
import poiplayer.ListManager;
import poiplayer.ThreadController;

/**
 *
 * @author WheatBerry
 */
public class PlayerThread_mp3 extends pThread{
    public PlayerThread_mp3(int[] wave, int _pos, ThreadController _tc, ListManager lm){
        listManager = lm;
        tc = _tc;
        w = wave;
        qflag = false;
        pos = _pos;
        System.out.println("New mp3 thread initialized, qflag=" + qflag + " pause=" + pause + " pos=" + pos);
    }
    @Override
    public void run(){
        System.out.println("Start running, pause=" + pause + " pos=" + pos);
        try {
            d = new Decoder();
            fis = new FileInputStream(listManager.GetPath(listManager.GetCursor()));
            b = new Bitstream(fis);
            FactoryRegistry r = FactoryRegistry.systemRegistry();
            audio = r.createAudioDevice();
            audio.open(d);
            n = Integer.MAX_VALUE;
            //暂停后跳过先前的部分
            while(pos-- > 0 && pause){
                b.readFrame();
                b.closeFrame();
                n--;
            }
            //正常播放
            while(n-- > 0 && !qflag){
                h = b.readFrame();
                if (h == null) {
                    System.out.println("Exit");
                    break;
                }
                s = (SampleBuffer)d.decodeFrame(h, b);
                Wave();
                
                for(int i = 0; i <s.getBufferLength(); i++){
                    s.getBuffer()[i] /= 8;
                }
                audio.write(s.getBuffer(), 0, s.getBufferLength());
                b.closeFrame();
            }
            //渐弱
            int tn = 0;
            if(qflag){
                tn = 30;
            }
            while(tn-- > 0 && qflag){
                h = b.readFrame();
                if (h == null) {
                    System.out.println("Exit");
                    break;
                }
                double temp;
                s = (SampleBuffer)d.decodeFrame(h, b);
                for(int i = 0; i < s.getBufferLength(); i++){
                    temp = (double)s.getBuffer()[i];
                    temp = temp * ((double)tn/30);
                    s.getBuffer()[i] = (short)temp;
                }
                audio.write(s.getBuffer(), 0, s.getBufferLength());
                Wave();
                b.closeFrame();
            }
            audio.flush();
            //未完成退出
            if(qflag){
                audio.close();
                audio = null;
                b.close();
            }
            //完成自动退出
            else {
                audio.close();
                audio = null;
                b.close();
                tc.Complete();
            }
        } catch (FileNotFoundException | JavaLayerException ex) {
            Logger.getLogger(PlayerThread_mp3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int GetPosition(){
        System.out.println("Paused at pos=" + (Integer.MAX_VALUE - n));
        return Integer.MAX_VALUE - n;
    }
    public void Wave(){
        float[] a = new float[4608];
                int[] aa = new int[2304];
                for(int i = 0; i < 2304; i ++){
                    a[i] = (float)s.getBuffer()[i];
                }
                f = new FloatFFT_1D(2304);
                f.complexForward(a);
                for(int j = 0; j < 20; j ++){
                    int i = j;
                    int t = (int)Math.sqrt(a[j*50]*a[j*50]+a[j*50+1]*a[j*50+1]) / h.bitrate() * 10;
                    if(t < w[i] && w[i] > 0){
                        w[i] = w[i] - 1;
                    }
                    else {
//                        w[i] = (int)(t / Math.log(t/2));
                        w[i] = t;
                    }
                    if(w[i] > 70){
                        w[i] = 70;
                    }
                }
                for(int i = 1; i < 19; i++){
                    int t = (w[i-1] + w[i+1]) / 3;
                    if(w[i] < t){
                        w[i] = t;
                    }
                }
    }
}
