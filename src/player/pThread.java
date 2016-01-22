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
    public boolean comp = false;
    public FileInputStream fis = null;
    Decoder d = null;
    Header h = null;
    Bitstream b = null;
    FloatFFT_1D f = null;
    int[] w;
    public boolean qflag, pause;
    public int pos;
    AudioDevice audio;
    public abstract int GetPosition();
    SampleBuffer s;
    int n;
    ThreadController tc;
    ListManager listManager;
}
