/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.jtransforms.fft.FloatFFT_1D;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import poiplayer.ListManager;
import poiplayer.ThreadController;

/**
 *
 * @author WheatBerry
 */
public class PlayerThread_flac extends pThread{
    int n;
    byte[] bt;
    int bitRate;
    public PlayerThread_flac(int[] wave, int _pos, ThreadController _tc, ListManager lm){
        listManager = lm;
        tc = _tc;
        w = wave;
        qflag = false;
        pos = _pos;
        n = 0;
    }
    @Override
    public void run(){
        FLACDecoder flacDecoder = null;
        AudioFormat af;  
        DataLine.Info dli;  
        SourceDataLine sdl = null;
        StreamInfo si;
        Frame frame;
        ByteData bd = null;
        try {
            fis = new FileInputStream(listManager.GetPath(listManager.GetCursor()));
            flacDecoder = new FLACDecoder(fis);
            Metadata[] md = null;
            md = flacDecoder.readMetadata();
            
            si = flacDecoder.getStreamInfo();
            af = new AudioFormat(si.getSampleRate(), si.getBitsPerSample(), si.getChannels(), true, false);  
            bitRate = si.getSampleRate();
            System.out.println(af.toString());
            dli = new DataLine.Info(SourceDataLine.class, af);
            sdl = (SourceDataLine) AudioSystem.getLine(dli);  
            sdl.open(af);
            sdl.start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //继续播放，跳过前面的部分
        while(pos-- > 0){
            try {
                frame = flacDecoder.readNextFrame();
            } catch (IOException ex) {
                Logger.getLogger(PlayerThread_flac.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //正常播放
        while(!flacDecoder.isEOF() && !qflag){
            try {
                frame = flacDecoder.readNextFrame();
                if(frame == null){
                    break;
                }
                else {
                    bd = flacDecoder.decodeFrame(frame, bd);
                    bt = bd.getData();
                    Wave();
                    sdl.write(bt, 0, bd.getLen());
                }
            } catch (IOException ex) {
                Logger.getLogger(PlayerThread_flac.class.getName()).log(Level.SEVERE, null, ex);
            }
            n++;
        }
        //渐弱
        while(!flacDecoder.isEOF() && !qflag){
            try {
                frame = flacDecoder.readNextFrame();
                if(frame == null){
                    break;
                }
                else {
                    bd = flacDecoder.decodeFrame(frame, bd);
                    bt = bd.getData();
                    Wave();
                    sdl.write(bt, 0, bd.getLen());
                }
            } catch (IOException ex) {
                Logger.getLogger(PlayerThread_flac.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void Wave() {
        float[] a = new float[bt.length * 2];
        int[] aa = new int[bt.length];
        for(int i = 0; i < bt.length; i ++){
            a[i] = (float)bt[i];
        }
        f = new FloatFFT_1D(bt.length);
        f.complexForward(a);
        for(int j = 0; j < 20; j ++){
            int i = j;
            int temp = bt.length / 160;
            int t = (int)(Math.sqrt(a[j*temp]*a[j*temp]+a[j*temp+1]*a[j*temp+1]) / bitRate * 40);
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

    @Override
    public int GetPosition() {
        return 0;
    }
}
