package poiplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author WheatBerry
 * 这是用来管理播放目录的，并且包含了当前所支持的格式
 */
public class ListManager {
    
    private Model m = Model.LOOP_ALL;//这是用来控制循环方式的
    private String slash = "/";//根据不同的系统用不同的斜杠，我也不知道有没有用，总之用着先
    public ArrayList<String> list = new ArrayList();//歌单
    private String path = "";//路径
    private int count = 0;//总数
    private int cursor = 0;//当前播放的序号（指针）
    
    //windows下将slash换为"\\"
    private void FormatSlash(){
        String osName = System.getProperties().getProperty("os.name");
        if (osName.contains("Windows")){
            slash = "\\";
        }
    }
    
    //初始化列表，获取文件路径
    public void InitList(String _path){
        list.clear();
        path = _path;
        FormatSlash();
        File mfile = new File(path);
        File[] arr = mfile.listFiles();
        
        
        //currently supported format
        SupFormat[] supFormat = SupFormat.values();
        
        //convert to string
        String[] supFS = new String[supFormat.length];
        for(int i = 0; i < supFormat.length; i++){
            supFS[i] = "." + supFormat[i].name();
        }
        
        //get file
        if(arr != null){
            for (File arr1 : arr) {
                if (arr1.isFile()) {
                    for(int i = 0; i < supFS.length; i++){
                        //if file format is supported, add into list
                        if(arr1.getName().endsWith(supFS[i])){
                            list.add(arr1.getName());
                            break;
                        }
                    }
                }
            }
        }
        count = list.size();
    }
    
    //随机列表
    public void RandomList(){
        Random r = new Random();
        String[] l = new String[list.size()];
        for(int i = 0; i < l.length; i++){
            l[i] = list.get(i);
        }
        for(int i = 0; i < l.length; i++){
            int n = r.nextInt();
            String t = l[i];
            l[i] = l[n];
            l[n] = t;
        }
        list.clear();
        list.addAll(Arrays.asList(l));
    }
    
    //设置播放模式
    public void SetModel(Model _m){
        m = _m;
    }
    
    //下一首
    public void MoveNext(){
        if(cursor == list.size() - 1) {
            cursor = 0;
        }
        else cursor ++;
    }
    
    //上一首
    public void MovePrev(){
        if(cursor == 0) {
            cursor = list.size() - 1;
        }
        else cursor --;
    }
    
    //获取指定项的路径
    public String GetPath(int index){
        if(list.size() > 0) {
            if(index < 0) {
                index = list.size() - 1;
            }
            else if(index > list.size() - 1){
                index = 0;
            }
            return path + slash + list.get(index);
        }
        else return null;
    }
    
    //获取指定项的文件名
    public String GetName(int index){
        if(list.size() > 0) {
            if(index < 0) {
                index = list.size() - 1;
            }
            else if(index > list.size() - 1){
                index = 0;
            }
            return list.get(index);
        }
        else return null;
    }
    
    //列表总数
    public int GetCount(){
        return count;
    }
    
    //当前cursor
    public int GetCursor(){
        return cursor;
    }
    
    //获取文件名格式
    public ListManager.SupFormat GetFormat(){
        if(list.size() > 0){
            String s = GetName(cursor);
            if(s.contains("mp3")){
                return ListManager.SupFormat.mp3;
            }
            else if(s.contains("flac")){
                return ListManager.SupFormat.flac;
            }
//
//            else if(s.contains("ape")){
//                return ListManager.SupFormat.ape;
//            }
//            else if(s.contains("ogg")){
//                return ListManager.SupFormat.ogg;
//            }
            else return null;
        }
        else return null;
    }

    //循环模式
    enum Model{
        LOOP_ALL,
        LOOP_SINGLE,
        RANDOM,
    }
    
    //支持的格式
    enum SupFormat{
        mp3,
//        ogg,
        flac,
//        ape
    }
}
