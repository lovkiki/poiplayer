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
    Decoder d;
    Header h;
    Bitstream b;
    AudioDevice audio;
    SampleBuffer s;
    private int n;
    public PlayerThread_mp3(int[] wave, int _pos, ThreadController _tc, ListManager lm){
        listManager = lm;
        tc = _tc;
        w = wave;
        qflag = false;
        pos = _pos;
    }
    @Override
    public void run(){
        try {
            d = new Decoder();
            fis = new FileInputStream(listManager.GetPath(listManager.GetCursor()));
            b = new Bitstream(fis);
            FactoryRegistry r = FactoryRegistry.systemRegistry();
            audio = r.createAudioDevice();
            audio.open(d);
            n = Integer.MAX_VALUE;
            short[] ts = new short[4];
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
                    break;
                }
                s = (SampleBuffer)d.decodeFrame(h, b);
                Wave();
                for(int i = 0; i < s.getBufferLength() / 4; i++){
                    audio.write(ts, 0, 4);
                }
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
        return Integer.MAX_VALUE - n;
    }
    @Override
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
