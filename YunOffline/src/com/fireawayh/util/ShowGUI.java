package com.fireawayh.util;

import com.fireawayh.main.YunOffline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * By FireAwayH on 16/01/2016.
 */
public class ShowGUI extends JFrame {
    private TextField passwordText = new TextField();
    private TextField idText = new TextField();
    private TextField sourceURL = new TextField();
    private Frame frame = new Frame("Baidu Yun Offline");
    private Dialog dialog = new Dialog(frame, "Hint", true);
    private IOUtils iou = new IOUtils();
    private FrameUtils fu = new FrameUtils();

    public static void main(String[] args) {
        new ShowGUI().showGui();
    }

    public void showGui() {
        Thread t = new Thread(new Logger());
        t.start();

        Label idLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Label sourceLabel = new Label("Source:");
        idText.setColumns(15);
        passwordText.setColumns(15);
        sourceURL.setColumns(15);
        Button start = new Button("Start!");
        Button stop = new Button("Stop ALl!");
        frame.add(idLabel);
        frame.add(idText);
        frame.add(passwordLabel);
        frame.add(passwordText);
        frame.add(sourceLabel);
        frame.add(sourceURL);
        frame.add(start);
        frame.add(stop);
        frame.setLayout(new FlowLayout());
        frame.setSize(250, 150);
        frame.setResizable(false);
        frame.setLocation(fu.getCenter(frame));
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );

        try {
            Properties prop = new Properties();
            File file = new File("userinfo.properties");
            if (file.exists()) {
                prop.load(new FileInputStream(file));
                idText.setText(prop.getProperty("Username"));
                passwordText.setText(prop.getProperty("Password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                System.out.println(Thread.activeCount());
//                Map x = Thread.getAllStackTraces();
//                Set y = x.keySet();
//                Iterator z = y.iterator();
//                while (z.hasNext()) {
//                    Thread o = (Thread)z.next();
//                    if(o.isAlive()) {
//                        System.out.println(o.getName());
//                    }
//                }
            }
        });

        start.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Label l = new Label();
                        l.setAlignment(Label.CENTER);
                        dialog.setSize(150, 50);
                        dialog.setLocation(fu.getCenter(dialog));
                        dialog.addWindowListener(
                                new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        dialog.removeAll();
                                        dialog.dispose();
                                    }
                                }
                        );

                        String userName = idText.getText();
                        String password = passwordText.getText();
                        String source = sourceURL.getText();

                        if (userName.isEmpty()) {
                            l.setText("Username id required");
                            dialog.add(l);
                            dialog.setVisible(true);
                            return;
                        }

                        if (password.isEmpty()) {
                            l.setText("Password required");
                            dialog.add(l);
                            dialog.setVisible(true);
                            return;
                        }

                        if (source.isEmpty()) {
                            l.setText("Source required");
                            dialog.add(l);
                            dialog.setVisible(true);
                            return;
                        }

//                        TODO
                        iou.saveUserInfo(userName, password, "/");

                        Thread t = new Thread(new YunOffline(userName, password, source, true));
                        t.setName("New Thread Download");
                        t.start();
                    }
                }
        );
    }
}
