package player;

import java.io.FileInputStream;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import org.jtransforms.fft.FloatFFT_1D;
import poiplayer.ListManager;
import poiplayer.ThreadController;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author WheatBerry
 */
public abstract class pThread extends Thread{
    public FileInputStream fis = null;
    FloatFFT_1D f = null;
    int[] w;
    public boolean qflag, pause;
    public int pos;
    ThreadController tc;//线程管理器
    ListManager listManager;//歌单管理器
    public float volumn = 1.0f;//音量系数
    public abstract int GetPosition();
    public abstract void Wave();
}
