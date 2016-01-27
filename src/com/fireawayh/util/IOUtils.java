package com.fireawayh.util;

import org.apache.http.cookie.Cookie;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * By FireAwayH on 17/01/2016.
 */
public class IOUtils {

    public void saveUserInfo(String userName, String password, String savePath) {
        if(savePath.isEmpty()){
            savePath = "/";
        }
        saveStringToFile("Username=" + userName + "\r\n" + "Password=" + password + "\r\n" + "Savepath=" + savePath, "userinfo.properties");
    }

    public void appendStringToFile(String content, String filename){
        try {
            File file = new File(filename);
            if(file.exists()){
                String oldContent = getStringFromFile(filename);
                content += oldContent;
            }
            saveStringToFile(content, filename);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void saveStringToFile(String content, String filename){
        try {
            File file = new File(filename);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(content);
            out.flush();
            out.close();
            System.out.println("Save " + filename + " To File");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public String getStringFromFile(String filename) {
        String result = "";
        File file = new File(filename);
        if (file.exists()) {
            String tmp = "";
            StringBuffer sb = new StringBuffer();
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                while ((tmp = br.readLine()) != null) {
                    sb.append(tmp);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            result = sb.toString();
        }
        return result;
    }

    public boolean downloadNetFile(String link, String filename){
        boolean result = false;
        int byteread = 0;
        try {
            URL u = new URL(link);
            URLConnection uri = u.openConnection();
            InputStream inStream = uri.getInputStream();
            File folder = new File("./savedcodes/");
            if (!folder.exists() && !folder.isDirectory()) {
                folder.mkdirs();
            }
            File f = new File(folder, filename);
            FileOutputStream fs = new FileOutputStream(f);
            byte[] buffer = new byte[2048];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            fs.close();
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public boolean saveCookie(Object cookies) {
        try {
            File file = new File("cookie.file");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(cookies);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cookie[] getCookie(String fileName) {
        Cookie[] cookies = null;
            try {
                File file = new File(fileName);
                if (file.exists()) {
                    ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
                    Object x = oi.readObject();
                    Object[] xx = ((ArrayList<?>) x).toArray();
                    Cookie[] xxx = new Cookie[xx.length];
                    for (int i = 0; i < xx.length; i++) {
                        xxx[i] = (Cookie) xx[i];
                    }
                    cookies = xxx;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        return cookies;
    }

}
