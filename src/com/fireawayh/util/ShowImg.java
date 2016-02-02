package com.fireawayh.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

public class ShowImg extends JFrame implements Runnable {

    boolean loadFinished;
    private Image myImage;
    private TextField input = new TextField();
    private JFrame frame = new JFrame("Input Code");
    private JLabel l = new JLabel();
    private Button ok = new Button("Loading...");
    private String code;
    private String imgsrc;
    private String yunToken;
    private String newVcode;
    private String source_url;
    private FrameUtils fu = new FrameUtils();


    public ShowImg(String imgsrc, String yunToken, String newVcode, String source_url) {
        this.imgsrc = imgsrc;
        this.yunToken = yunToken;
        this.newVcode = newVcode;
        this.source_url = source_url;
    }

    /**
     * Construct the object
     */
    public ShowImg() {
        super();
    }

    public static void main(String[] av) {
        ShowImg r = new ShowImg();
        r.showFrame();
        r.loadURLImage("http://avatar.csdn.net/F/7/3/3_dwheger.jpg");

    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void run() {
        showFrame();
        loadURLImage(imgsrc);
    }

    public void showFrame() {
        frame.setLayout(new FlowLayout());
        frame.setSize(150, 180);
        frame.setAlwaysOnTop(true);
        input.setColumns(15);
        frame.add(l);
        frame.add(input);
        ok.setEnabled(false);
        frame.add(ok);
        frame.setLocation(fu.getCenter(frame));
        frame.setVisible(true);
        ok.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setCode(input.getText());
                frame.dispose();
                System.out.print("Get Code: " + getCode());
//                Thread t = new Thread(new YunOffline(yunToken, source_url, newVcode, input.getText()));
//                t.setName("New Thread With Code");
//                t.start();
            }
        });
    }

    //加载网络上图片
    public void loadURLImage(String sUrl) {
        Toolkit toolkit;
        loadFinished = false;
        toolkit = Toolkit.getDefaultToolkit();
        try {
            URL url = new URL(sUrl);
            myImage = toolkit.getImage(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        l.setIcon(new ImageIcon(myImage));
        ok.setEnabled(true);
        ok.setLabel("OK");
        ok.setName("OK!");
    }

    //加载本地图片
    public void loadLoaclImage(String sFile) {
        Toolkit toolkit;
        loadFinished = false;
        toolkit = Toolkit.getDefaultToolkit();
        myImage = toolkit.getImage(sFile);
        Graphics g = this.getGraphics();
        g.drawImage(myImage, 10, 10, this);
    }

    public void paint(Graphics g) {
        //判断是否加载完成
        if (loadFinished) {
            g.drawImage(myImage, 10, 10, this);
        } else {
            g.drawString("Loading", 30, 50);
        }
    }

    //图片加载状态通知函数
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (infoflags == ALLBITS) {
            loadFinished = true;
            repaint();
            return false;
        } else {
            return true;
        }
    }
}