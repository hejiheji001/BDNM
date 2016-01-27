package com.fireawayh.util;

import java.awt.*;

/**
 * By FireAwayH on 17/01/2016.
 */
public class FrameUtils {

    public Point getCenter(Window f) {
        int windowWidth = f.getWidth();                    //获得窗口宽
        int windowHeight = f.getHeight();
        Toolkit kit = Toolkit.getDefaultToolkit();             //定义工具包
        Dimension screenSize = kit.getScreenSize();            //获取屏幕的尺寸
        int screenWidth = screenSize.width;                    //获取屏幕的宽
        int screenHeight = screenSize.height;
        return new Point(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
    }
}
