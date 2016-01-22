package poiplayer;

import com.sun.awt.AWTUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author WheatBerry
 */
public class UI {
    JFrame p;//主面板
    JPanel bg, poipic;//bg是底下的大蓝框，poipic是poi头像
    JPanel waveContainer;//显示波形的容器
    JPanel info;//显示info的容器
    JButton btn_prev, btn_play, btn_next;//上一首、播放/暂停、下一首
    JButton btn_i, btn_f, btn_x;//info、打开文件夹、关闭（右上三个小按钮）
    JLabel SongName;//显示当前歌曲名字
    int mx, my, mx2;//用于拖放窗口时记录鼠标位置
    PopupMenu pm;//poi头像的右键菜单
    Dimension screen;//获取屏幕大小
    Image poiImg, bgImg, moveBgImg1, moveBgImg2, playBgImg1, playBgImg2;//各种背景图片
    Image triL, triR, playImg, pauseImg;//三个播放控制按钮的图片
    Image i1Img, i2Img, f1Img, f2Img, x1Img, x2Img;//右上三个小按钮的图片
    Image volumnImg;//音量标识
    BufferedImage waveImg;//波形所用的图片
    int stPrev, stPlay, stNext;//三个播放控制按钮的状态标识
    boolean stBtni, stBtnf, stBtnx;//右上三个小按钮的状态标识
    java.net.URL poiImgURL, bgImgURL, MoveBgImgURL, playBgImgURL, MoveBgImg2URL, playBgImg2URL;//各背景图片URL
    java.net.URL triLURL, triRURL, playImgURL, pauseImgURL;//播放、上一首、下一首按钮的图片URL
    java.net.URL i1URL, i2URL, f1URL, f2URL, x1URL, x2URL;//右上角小图标的图片URL
    java.net.URL waveImgURL;//波形图片的URL
    java.net.URL volumnImgURL;//音量标识图片URL
    boolean alwaysTop;//置顶标识
    boolean isPlaying;//播放标识
    ThreadController tc;//播放线程控制器
    int[] waveArr;//波形数组
    JLabel title;//歌曲名字
    UIUpdater uu;//用于动态显示波形的线程
    String lastPath;//用于记录上一次加载的目录
    ListManager listManager;//歌单控制器
    DropTarget dt;//用于拖放
    DropTargetListener dtl;//用于拖放的Listener
    JPanel volumnPanel;
    int volumn = 100;
    
    public UI(){
        listManager = new ListManager();
        waveArr = new int[20];
        screen = Toolkit.getDefaultToolkit().getScreenSize();
        alwaysTop = false;
        isPlaying = false;
        InitComponent();
        tc = new ThreadController(waveArr, listManager, this);
        p.setVisible(true);
        dtl = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {}
            @Override
            public void dragOver(DropTargetDragEvent dtde) {}
            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            @Override
            public void dragExit(DropTargetEvent dte) {}
            @Override
            public void drop(DropTargetDropEvent dtde) {
                DataFlavor[] dataFlavors = dtde.getCurrentDataFlavors();
                if(dataFlavors[0].match(DataFlavor.javaFileListFlavor)){
                    dtde.acceptDrop(dtde.getDropAction());
                    Transferable tr = dtde.getTransferable();
                    Object obj = null;
                    try {
                        obj =  tr.getTransferData(DataFlavor.javaFileListFlavor);
                    } catch (UnsupportedFlavorException ex) {
                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    List<File> files = (List<File>)obj;
                    if(files.size() > 0){
                        if(files.get(0).isFile()){
                            lastPath = files.get(0).getParent();
                        }
                        else {
                            lastPath = files.get(0).getPath();
                        }
                        listManager.InitList(lastPath);
                        UpdateMusicInfo();
                        WritePath(lastPath);
                        PlayVoice_poi playVoice_poi = new PlayVoice_poi();
                        playVoice_poi.start();
                    }
                }
            }
        };
        dt = new DropTarget(p, dtl);
    }
    private void InitComponent(){
        p = new JFrame();
        p.setSize(400, 400);
        p.setUndecorated(true);
        p.setLayout(null);
        p.setLocationRelativeTo(null);
        p.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        AWTUtilities.setWindowOpaque(p, false);
        
        //打包图片进jar，用下面这个方法，马克
        //别的方法好像读不出来
        try {
            poiImgURL = UI.class.getResource("assets/poi_128x.png");
            bgImgURL = UI.class.getResource("assets/bg2.png");
            MoveBgImgURL = UI.class.getResource("assets/chilun_off2.png");
            playBgImgURL = UI.class.getResource("assets/dachilun_off2.png");
            MoveBgImg2URL = UI.class.getResource("assets/chilun_on2.png");
            playBgImg2URL = UI.class.getResource("assets/dachilun_on2.png");
            
            waveImgURL = UI.class.getResource("assets/wave.png");
            
            triLURL = UI.class.getResource("assets/btn_prev_hover.png");
            triRURL = UI.class.getResource("assets/btn_next_hover.png");
            playImgURL = UI.class.getResource("assets/btn_play_hover2.png");
            pauseImgURL = UI.class.getResource("assets/btn_pause_hover2.png");
            
            i1URL = UI.class.getResource("assets/i1.png");
            i2URL = UI.class.getResource("assets/i2.png");
            f1URL = UI.class.getResource("assets/f1.png");
            f2URL = UI.class.getResource("assets/f2.png");
            x1URL = UI.class.getResource("assets/x1.png");
            x2URL = UI.class.getResource("assets/x2.png");
            
            volumnImgURL = UI.class.getResource("assets/volumn.png");
            
            poiImg = ImageIO.read(poiImgURL);
            bgImg = ImageIO.read(bgImgURL);
            moveBgImg1 = ImageIO.read(MoveBgImgURL);
            playBgImg1 = ImageIO.read(playBgImgURL);
            moveBgImg2 = ImageIO.read(MoveBgImg2URL);
            playBgImg2 = ImageIO.read(playBgImg2URL);
            
            triL = ImageIO.read(triLURL);
            triR = ImageIO.read(triRURL);
            playImg = ImageIO.read(playImgURL);
            pauseImg = ImageIO.read(pauseImgURL);
            
            i1Img = ImageIO.read(i1URL);
            i2Img = ImageIO.read(i2URL);
            f1Img = ImageIO.read(f1URL);
            f2Img = ImageIO.read(f2URL);
            x1Img = ImageIO.read(x1URL);
            x2Img = ImageIO.read(x2URL);
            
            waveImg = ImageIO.read(waveImgURL);
            
            volumnImg = ImageIO.read(volumnImgURL);
            
        } catch (IOException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        poipic = new JPanel(){
            @Override
            public void paint(Graphics g){
                g.drawImage(poiImg, 0, 0, 128, 128, poipic);
            }
        };
        poipic.setLocation(20, 0);
        poipic.setSize(128, 128);
        
        //右键菜单
        pm = new PopupMenu();
        MenuItem exitMI = new MenuItem("退出");
        final MenuItem topMI = new MenuItem("置顶");
        MenuItem buyMI = new MenuItem("poi");
        final MenuItem hideWaveMI = new MenuItem("隐藏波形");
        buyMI.setEnabled(false);
        //pm.add(buy);
        pm.add(topMI);
        pm.add(hideWaveMI);
        pm.add(exitMI);
        poipic.add(pm);
        
        //拖动
        poipic.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                mx = e.getX();
                my = e.getY();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                OnMouseClicked(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        poipic.addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e) {
                OnMouseDrag(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        exitMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuExit(e);
            }
        });
        topMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(alwaysTop){
                    topMI.setLabel("置顶");
                    p.setAlwaysOnTop(false);
                    alwaysTop = false;
                }
                else {
                    topMI.setLabel("取消置顶");
                    p.setAlwaysOnTop(true);
                    alwaysTop = true;
                }
            }
        });
        hideWaveMI.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(waveContainer.isVisible()){
                    hideWaveMI.setLabel("显示波形");
                    uu.Disable();
                    waveContainer.setVisible(false);
                }
                else {
                    hideWaveMI.setLabel("隐藏波形");
                    uu.Enable();
                    waveContainer.setVisible(true);
                }
            }
        });
        
        bg = new JPanel(){
            @Override
            public void paint(Graphics g){
                g.drawImage(bgImg, 0, 0, 300, 98, poipic);
            }
        };
        bg.setLocation(0, 128);
        bg.setSize(300, 98);
        bg.setLayout(null);
        
        waveContainer = new JPanel(){
            public void paint(Graphics g){
                PaintWave(g);
            }
        };
        waveContainer.setSize(120, 70);
        waveContainer.setBackground(Color.gray);
        waveContainer.setLocation(145, 58);
        
        uu = new UIUpdater(waveContainer, p);
        uu.Enable();
        uu.start();
        
        stPrev = stPlay = stNext = 0;
        
        //播放控制按钮
        btn_prev = new JButton(){
            @Override
            public void paint(Graphics g){
                switch(stPrev){
                    case 0:
                        g.drawImage(moveBgImg1, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triL, 0, 0, 32, 32, btn_prev);
                        break;
                    case 1:
                        g.drawImage(moveBgImg2, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triL, 0, 0, 32, 32, btn_prev);
                        break;
                    case 2:
                        g.drawImage(moveBgImg2, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triL, 2, 2, 28, 28, btn_prev);
                        break;
                }
            }
        };
        btn_play = new JButton(){
            @Override
            public void paint(Graphics g){
                switch(stPlay){
                    case 0:
                        g.drawImage(playBgImg1, 0, 0, 40, 40, btn_prev);
                        g.drawImage(playImg, 0, 0, 40, 40, btn_prev);
                        break;
                    case 1:
                        g.drawImage(playBgImg2, 0, 0, 40, 40, btn_prev);
                        g.drawImage(playImg, 0, 0, 40, 40, btn_prev);
                        break;
                    case 2:
                        g.drawImage(playBgImg2, 0, 0, 40, 40, btn_prev);
                        g.drawImage(playImg, 2, 2, 36, 36, btn_prev);
                        break;
                    case 3:
                        g.drawImage(playBgImg1, 0, 0, 40, 40, btn_prev);
                        g.drawImage(pauseImg, 0, 0, 40, 40, btn_prev);
                        break;
                    case 4:
                        g.drawImage(playBgImg2, 0, 0, 40, 40, btn_prev);
                        g.drawImage(pauseImg, 0, 0, 40, 40, btn_prev);
                        break;
                    case 5:
                        g.drawImage(playBgImg2, 0, 0, 40, 40, btn_prev);
                        g.drawImage(pauseImg, 2, 2, 36, 36, btn_prev);
                        break;
                }
            }
        };
        btn_next = new JButton(){
            @Override
            public void paint(Graphics g){
                switch(stNext){
                    case 0:
                        g.drawImage(moveBgImg1, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triR, 0, 0, 32, 32, btn_prev);
                        break;
                    case 1:
                        g.drawImage(moveBgImg2, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triR, 0, 0, 32, 32, btn_prev);
                        break;
                    case 2:
                        g.drawImage(moveBgImg2, 0, 0, 32, 32, btn_prev);
                        g.drawImage(triR, 2, 2, 28, 28, btn_prev);
                        break;
                }
            }
        };
        
        btn_prev.setSize(40, 40);
        btn_prev.setLocation(20,
                poipic.getHeight() + bg.getHeight() / 2 - btn_prev.getHeight() / 2);
        
        btn_play.setSize(48, 48);
        btn_play.setLocation(25 + btn_prev.getWidth(),
                poipic.getHeight() + bg.getHeight() / 2 - btn_play.getHeight() / 2);
        
        btn_next.setSize(40, 40);
        btn_next.setLocation(30 + btn_prev.getWidth() + btn_play.getWidth(),
                poipic.getHeight() + bg.getHeight() / 2 - btn_next.getHeight() / 2);
        
        btn_prev.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tc.Stop();
                listManager.MovePrev();
                UpdateMusicInfo();
                if(isPlaying){
                    tc.Play();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                stPrev = 2;
                btn_prev.repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if(stPrev != 0){
                    stPrev = 1;
                }
                btn_prev.repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                stPrev = 1;
                btn_prev.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                stPrev = 0;
                btn_prev.repaint();
            }
        });
        btn_play.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!isPlaying){
                    if(tc.Play()){
                        isPlaying = true;
                    }
                }
                else {
                    tc.Pause();
                    isPlaying = false;
                }
                if(isPlaying){
                    if(stPlay != 3){
                        stPlay = 4;
                    }
                }
                else {
                    if(stPlay != 0){
                        stPlay = 1;
                    }
                }
                btn_play.repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if(isPlaying){
                    stPlay = 5;
                }
                else{
                    stPlay = 2;
                }
                btn_play.repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                if(isPlaying){
                    stPlay = 4;
                }
                else {
                    stPlay = 1;
                }
                btn_play.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(isPlaying){
                    stPlay = 3;
                }
                else {
                    stPlay = 0;
                }
                btn_play.repaint();
            }
        });
        btn_next.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tc.Stop();
                listManager.MoveNext();
                UpdateMusicInfo();
                if(isPlaying){
                    tc.Play();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                stNext = 2;
                btn_next.repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if(stNext != 0){
                    stNext = 1;
                }
                btn_next.repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                stNext = 1;
                btn_next.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                stNext = 0;
                btn_next.repaint();
            }
        });
        
        stBtni = stBtnf = stBtnx = false;
        
        //右上角三个小按钮
        btn_i = new JButton(){
            @Override
            public void paint(Graphics g){
                if(stBtni){
                    g.drawImage(i2Img, 0, 0, 16, 16, btn_i);
                }
                else {
                    g.drawImage(i1Img, 0, 0, 16, 16, btn_i);
                }
            }
        };
        btn_f = new JButton(){
            @Override
            public void paint(Graphics g){
                if(stBtnf){
                    g.drawImage(f2Img, 0, 0, 16, 16, btn_i);
                }
                else {
                    g.drawImage(f1Img, 0, 0, 16, 16, btn_i);
                }
            }
        };
        btn_x = new JButton(){
            @Override
            public void paint(Graphics g){
                if(stBtnx){
                    g.drawImage(x2Img, 0, 0, 16, 16, btn_i);
                }
                else {
                    g.drawImage(x1Img, 0, 0, 16, 16, btn_i);
                }
            }
        };
        btn_i.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(info.isVisible()){
                    info.setVisible(false);
                }
                else {
                    info.setVisible(true);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                stBtni = true;
                btn_i.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                info.setVisible(false);
                stBtni = false;
                btn_i.repaint();
            }
        });
        btn_f.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChooseFile();
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                stBtnf = true;
                btn_f.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                stBtnf = false;
                btn_f.repaint();
            }
        });
        btn_x.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                stBtnx = true;
                btn_x.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                stBtnx = false;
                btn_x.repaint();
            }
        });
        btn_i.setToolTipText("关于");
        btn_f.setToolTipText("选择文件夹");
        btn_x.setToolTipText("退出");
        
        btn_f.setSize(16, 16);
        btn_f.setLocation(228, 132);
        btn_i.setSize(16, 16);
        btn_i.setLocation(244, 132);
        btn_x.setSize(16, 16);
        btn_x.setLocation(260, 132);
        
        title = new JLabel();
        title.setSize(150, 20);
        title.setLocation(23, 134);
        
        info = new JPanel(){
            @Override
            public void paint(Graphics g){
                Color c = new Color(0x66, 0xcc, 0xff);
                g.setColor(c);
                g.fillRect(0, 0, 100, 40);
                g.setColor(Color.black);
                g.drawRect(0, 0, 100, 40);
                info.paintComponents(g);
            }
        };
        info.setSize(100, 40);
        info.setLocation(180, 150);
        JLabel ver = new JLabel();
        JLabel ver2 = new JLabel();
        ver.setText(" Version");
        ver.setLocation(5, 0);
        ver.setSize(100, 10);
        ver.setFont(new java.awt.Font("Dialog", 0, 10));
        ver2.setText(" β 0.3");
        ver2.setLocation(30, 10);
        ver2.setSize(100, 10);
        ver2.setFont(new java.awt.Font("Dialog", 0, 10));
        JLabel author = new JLabel();
        JLabel author2 = new JLabel();
        author.setText(" @Author");
        author.setLocation(5, 20);
        author.setSize(100, 10);
        author.setFont(new java.awt.Font("Dialog", 0, 10));
        author2.setText(" WheatBerry");
        author2.setLocation(30, 30);
        author2.setSize(100, 10);
        author2.setFont(new java.awt.Font("Dialog", 0, 10));
        info.setLayout(null);
        info.add(ver, BorderLayout.WEST);
        info.add(author, BorderLayout.WEST);
        info.add(ver2, BorderLayout.WEST);
        info.add(author2, BorderLayout.WEST);
        info.setVisible(false);
        
        volumnPanel = new JPanel(){
            public void paint(Graphics g){
                g.drawImage(volumnImg, 0, 0, volumnPanel);
                g.setColor(Color.gray);
                g.drawRect(20, 6, 99, 3);
                g.drawLine(99, 0, 99, 16);
                g.setColor(Color.ORANGE);
                g.fillRect(mx2 - 3, 0, 6, 15);
                g.setColor(Color.yellow);
                g.drawRect(mx2 - 3, 0, 6, 15);
            }
        };
        volumnPanel.setSize(100, 16);
        volumnPanel.setLocation(175, 165);
        mx2 = 85;
        volumnPanel.repaint();
        volumnPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mx2 = e.getX();
                if(mx2 > 95){
                    mx2 = 95;
                }
                if(mx2 < 23){
                    mx2 = 23;
                }
                float d = (float)(mx2 - 23) / 62;
                tc.SetVolumn(d);
                volumnPanel.repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        
        //向jframe添加控件，注意顺序
        p.add(btn_prev);
        p.add(btn_play);
        p.add(btn_next);
        p.add(btn_i);
        p.add(btn_f);
        p.add(btn_x);
        p.add(title);
        
        p.add(info);
        p.add(volumnPanel);
        p.add(bg);
        p.add(poipic);
        p.add(waveContainer);
        
    }
    
    //InitComponent分离出来的窗口拖动处理
    private void OnMouseDrag(MouseEvent e){
        Point p1 = e.getLocationOnScreen();
        int x = p1.x - mx - 20;
        int y = p1.y - my;
        if(x < 0){
            x = 0;
        }
        if(y < 0){
            y = 0;
        }
        if(x > screen.width - bg.getWidth()){
            x = screen.width - bg.getWidth();
        }
        if(y > screen.height - poipic.getHeight() - bg.getHeight()){
            y = screen.height - poipic.getHeight() - bg.getHeight();
        }
        p.setLocation(x, y);
    }
    
    //poipic的右键菜单
    private void OnMouseClicked(MouseEvent e){
        if(e.getButton() == MouseEvent.BUTTON3){
            pm.show(poipic, e.getX(), e.getY());
        }
    }
    
    //右键菜单的退出
    private void MenuExit(ActionEvent e){
        System.exit(0);
    }
    
    //波形作图
    private void PaintWave(Graphics g){
        for(int i = 0; i < 20; i ++) {
            int h = waveArr[i];
            g.drawImage(waveImg.getSubimage(i * 6, 0, 5, 100), i * 6, 70 - h, waveContainer);
        }
    }
    
    //修改波形数组
    public void SetWaveArr(int[] arr){
        System.arraycopy(arr, 0, waveArr, 0, 20);
    }
    
    //记录加载路径
    private void WritePath(String s){
        FileWriter fileWriter;
        try {
            String osName = System.getProperties().getProperty("os.name");
            if (osName.contains("Windows")){
                fileWriter = new FileWriter(".\\path.txt", false);
            }
            else {
                fileWriter = new FileWriter("./path.txt", false);
            }
            fileWriter.write(s);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //选择文件或文件夹
    private void ChooseFile(){
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()){
                    return true;
                }
                else if(f.getName().endsWith(".mp3")){
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                return null;
            }
        });
        if(lastPath != null){
            chooser.setCurrentDirectory(new File(lastPath).getParentFile());
        }
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.showOpenDialog(p);
        if(chooser.getSelectedFile() != null) {
            String selectPath;
            if(chooser.getSelectedFile().isFile()){
                selectPath = chooser.getSelectedFile().getParent();
            }
            else {
                selectPath = chooser.getSelectedFile().getPath();
            }
            lastPath = selectPath;
            listManager.InitList(selectPath);
            UpdateMusicInfo();
            chooser.setVisible(false);
            WritePath(selectPath);
        }
    }
    
    //更新歌曲信息
    public void UpdateMusicInfo(){
        String s = "[" + (listManager.GetCursor() + 1) + "/" + listManager.GetCount() + "] ";
        s += listManager.GetName(listManager.GetCursor());
        title.setText(s);
    }
    
    //读取上次的目录(本来用作入口的，现在闲置)
    public void go(String[] args){
        String s = ReadPath();
        File f = new File(s);
        this.listManager.InitList(f.getPath());
        this.UpdateMusicInfo();
    }
    
    //读取上次的目录
    private String ReadPath(){
        String osName = System.getProperties().getProperty("os.name");
        File f;
        if (osName.contains("Windows")){
            f = new File(".\\path.txt");
        }
        else {
            f = new File("./path.txt");
        }
        if(f.exists()){
            try {
                FileReader fileReader;
                BufferedReader br;
                if (osName.contains("Windows")){
                    fileReader = new FileReader(".\\path.txt");
                }
                else {
                    fileReader = new FileReader("./path.txt");
                }
                br = new BufferedReader(fileReader);
                String s = br.readLine();
//                System.out.println(s);
                return s;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
