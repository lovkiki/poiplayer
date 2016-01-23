package poiplayer;

import javax.swing.JFrame;
import player.*;

/**
 *
 * @author WheatBerry
 * 这是用来控制播放线程的类
 */
public class ThreadController {
    pThread pt;//用来播放的线程
    ListManager.SupFormat sf;//用于选择解码器
    int[] wave;//控制UI中的波形显示的数组，会从UI中接收进来并进行修改
    int pos;//记录播放位置，以提供暂停、继续的功能
    boolean qflag;//退出标志
    boolean pause;//暂停标志
    ListManager listManager;//得到歌单的控制器
    UI jf;//用来更新主界面
    
    //构造函数，w接收波形数组后交给wave修改，ListManager是歌单控制器 
    public ThreadController(int[] w, ListManager lm, UI _jf){
        pause = false;
        qflag = false;
        wave = w;
        pos = 0;
        listManager = lm;
        jf = _jf;
    }
    
    //新建线程，播放
    public boolean Play(){
        sf = listManager.GetFormat();//获取当前文件的格式
        if(InitThread()){
            if(pt != null){
                if(pause){
                    pt.pause = true;
                    System.out.println("Ready to start, pt.pause=" + pt.pause + " pt.pos=" + pt.pos);
                }
                SetVolumn(jf.volumn);
                pt.start();
            }
            return true;
        }
        else return false;
    }
    //初始化线程
    private boolean InitThread(){
        if(sf != null){
            switch(sf){
                case mp3:
                    pt = new PlayerThread_mp3(wave, pos, this, listManager);
                    break;
                case flac:
                    pt = new PlayerThread_flac(wave, pos, this, listManager);
                    break;
//                case ape:
//                    pt = new PlayerThread_ape();
//                    break;
//                case ogg:
//                    pt = new PlayerThread_ogg();
//                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }
    //停止播放
    public void Stop(){
        if(pt != null){
            pt.qflag = true;
            pt = null;
        }
        pause = false;
    }
    public void Pause(){
        if(pt != null){
            pt.qflag = true;
            pos = pt.GetPosition();
            pt = null;
        }
        pause = true;
    }
    //线程播放完成后执行
    public void Complete(){
        if(pt != null){
            pt.pause = false;
            pt = null;
        }
        pause = false;
        listManager.MoveNext();
        jf.UpdateMusicInfo();
        Play();
    }
    public void SetVolumn(float volumn){
        if(pt != null){
            pt.volumn = volumn;
            System.out.println(volumn);
        }
    }
}
