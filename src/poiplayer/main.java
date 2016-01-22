/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poiplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author WheatBerry
 */
public class main {
    public static void main(String[] args){
        UI poi = new UI();
        String s = ReadPath();
//        System.out.println(s);
        File f = new File(s);
        poi.listManager.InitList(f.getPath());
        poi.UpdateMusicInfo();
    }
    private static String ReadPath(){
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
