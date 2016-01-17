package com.fireawayh.util;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * By FireAwayH on 16/01/2016.
 */
public class Logger extends JFrame implements Runnable {
    PrintStream aPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
    private Frame frame = new Frame("Console");
    private TextArea console = new TextArea();

    public static void main(String[] args) {
        Logger x = new Logger();
        x.logger();
    }

    public void logger() {
        frame.setSize(200, 180);
        frame.add(console);
        console.setSize(200, 180);
        frame.setVisible(true);
        System.setOut(aPrintStream);
        System.setErr(aPrintStream);
    }

    @Override
    public void run() {
        logger();
    }

    class FilteredStream extends FilterOutputStream {
        public FilteredStream(OutputStream aStream) {
            super(aStream);
        }

        public void write(byte b[]) throws IOException {
            String aString = new String(b);
            console.append(aString);
        }

        public void write(byte b[], int off, int len) throws IOException {
            String aString = new String(b, off, len);
            console.append(aString);
            FileWriter aWriter = new FileWriter("a.log", true);
            aWriter.write(aString);
            aWriter.close();
        }
    }
}
