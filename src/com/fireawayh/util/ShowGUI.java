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
public class ShowGUI extends JFrame implements Runnable {
    private TextField passwordText = new TextField();
    private TextField idText = new TextField();
    private TextField sourceURL = new TextField();
    private TextField path = new TextField();
    private Frame frame = new Frame("Baidu Yun Offline");
    private Dialog dialog = new Dialog(frame, "Hint", true);
    private IOUtils iou = new IOUtils();
    private FrameUtils fu = new FrameUtils();

    public static void main(String[] args) {
        new ShowGUI().showGui();
    }

    @Override
    public void run() {
//        showGui();
    }

    public void showGui() {
        showGui(null, false, null);
    }

    public void showGui(String file) {
        showGui(new String[]{"Batch Rename by rename.txt", file}, false, null);
    }

    public void showGui(String[] source) {
        showGui(source, false, null);
    }

    public void showGui(java.util.List<String[]> list) {
        showGui(new String[]{"Playlist with " + list.size() + " songs"}, true, list);
    }

    public void showGui(String[] sourceArr, boolean isList, java.util.List<String[]> list) {
        Thread t = new Thread(new Logger());
        t.start();

        Label idLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Label sourceLabel = new Label("Source:");
        Label pathLabel = new Label("YunPan Path:");
        idText.setColumns(15);
        passwordText.setColumns(15);
        sourceURL.setColumns(15);
        path.setColumns(15);
        Button start = new Button("Start!");
        Button stop = new Button("Stop ALl!");
        frame.add(idLabel);
        frame.add(idText);
        frame.add(passwordLabel);
        frame.add(passwordText);
        frame.add(sourceLabel);
        frame.add(sourceURL);
        frame.add(pathLabel);
        frame.add(path);
        sourceURL.setText(sourceArr[0]);
        frame.add(start);
        frame.add(stop);
        frame.setLayout(new FlowLayout());
        frame.setSize(250, 200);
        frame.setResizable(false);
        frame.setLocation(fu.getCenter(frame));
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        frame.dispose();
                        Thread.currentThread().interrupt();
                        t.interrupt();
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
                path.setText(prop.getProperty("Path"));
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
                        String pathText = path.getText();

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

                        if (!source.contains("rename") && pathText.isEmpty()) {
                            l.setText("Path of YunPan required. / means root path");
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

                        iou.saveUserInfo(userName, password, pathText);

                        if (isList) {
                            Thread t = new Thread(new YunOffline(userName, password, pathText, true, list));
                            t.setName("New Thread Download");
                            t.start();
                        } else {
                            if (sourceArr[0].indexOf("rename") > -1) {
                                Thread t = new Thread(new YunOffline(userName, password, sourceArr[1], true));
                                t.setName("New Thread Rename");
                                t.start();
                            } else {
                                sourceArr[0] = source;
                                sourceArr[1] = pathText;
                                Thread t = new Thread(new YunOffline(userName, password, sourceArr, pathText, true));
                                t.setName("New Thread Download");
                                t.start();
                            }
                        }
                    }
                }
        );
    }
}
