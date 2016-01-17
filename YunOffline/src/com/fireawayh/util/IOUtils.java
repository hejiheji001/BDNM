package com.fireawayh.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * By FireAwayH on 17/01/2016.
 */
public class IOUtils {

    public void saveUserInfo(String userName, String password) {
        try {
            File file = new File("userinfo.properties");
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("Username=" + userName + "\r\n" + "Password=" + password); // \r\n即为换行
            out.flush();
            out.close();
            System.out.println("Save Userinfo To File");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
