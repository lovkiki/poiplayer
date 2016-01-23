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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import org.jtransforms.fft.FloatFFT_1D;
import org.kc7bfi.jflac.metadata.StreamInfo;
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
    SampleBuffer s;
    private int n = 0;
    FloatControl fc;
    byte[] byteBuf = new byte[4096];
    byte[] bt;
    int bitRate;
    public PlayerThread_mp3(int[] wave, int _pos, ThreadController _tc, ListManager lm){
        listManager = lm;
        tc = _tc;
        w = wave;
        qflag = false;
        pos = _pos;
    }
    @Override
    public void run(){
        AudioFormat af;  
        DataLine.Info dli;  
        SourceDataLine sdl = null;
        StreamInfo si;
        try {
            d = new Decoder();
            fis = new FileInputStream(listManager.GetPath(listManager.GetCursor()));
            b = new Bitstream(fis);
            h = b.readFrame();
            d.decodeFrame(h, b);
            b.unreadFrame();
            bitRate = d.getOutputFrequency();
            af = new AudioFormat(d.getOutputFrequency(), 16, d.getOutputChannels(), true, false);  
            dli = new DataLine.Info(SourceDataLine.class, af);
            sdl = (SourceDataLine) AudioSystem.getLine(dli);  
            sdl.open(af);
            sdl.start();
            
            FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
            tc.fc = fc;
            tc.SetVolumn(tc.volumn);
            //暂停后跳过先前的部分
            while(pos-- > 0 && pause){
                b.readFrame();
                b.closeFrame();
                n++;
            }

            //正常播放
            while(!qflag){
                h = b.readFrame();
                if (h == null) {
                    break;
                }
                s = (SampleBuffer)d.decodeFrame(h, b);
                byte[] byteData = toByteArray(s.getBuffer(), 0, s.getBufferLength());
                bt = byteData;
                sdl.write(byteData, 0, s.getBufferLength() * 2);
                Wave();
                b.closeFrame();
                n++;
            }
            sdl.flush();
            sdl.close();
            //完成自动退出
            if(!qflag){
                tc.Complete();
            }
        } catch (FileNotFoundException | JavaLayerException ex) {
            Logger.getLogger(PlayerThread_mp3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PlayerThread_mp3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int GetPosition(){
        return n;
    }
    @Override
    public void Wave(){
        float[] a = new float[bt.length * 2];
        int[] aa = new int[bt.length];
        for(int i = 0; i < bt.length; i ++){
            a[i] = (float)bt[i];
        }
        f = new FloatFFT_1D(bt.length);
        f.complexForward(a);
        for(int j = 0; j < 20; j ++){
            int i = j;
            int temp = bt.length / 20;
            int t = (int)(Math.sqrt(a[j*temp]*a[j*temp]+a[j*temp+1]*a[j*temp+1]) / bitRate * 80);
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
    private byte[] getByteArray(int length){
        if (byteBuf.length < length)
        {
            byteBuf = new byte[length+1024];
        }
        return byteBuf;
    }

    private byte[] toByteArray(short[] samples, int offs, int len){
        byte[] b = getByteArray(len*2);
        int idx = 0;
        short s;
        while (len-- > 0)
        {
            s = samples[offs++];
            b[idx++] = (byte)s;
            b[idx++] = (byte)(s>>>8);
        }
        return b;
    }
}
