package com.company;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static JLabel rLabel = new JLabel("R: 0  ");
    static JLabel gLabel = new JLabel("G: 0  ");
    static JLabel bLabel = new JLabel("B: 0  ");

    static SerialPort port;
    static PrintWriter output;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("RGB LED");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("RGB LED");



        final JSlider rSlider = new JSlider();
        final JSlider gSlider = new JSlider();
        final JSlider bSlider = new JSlider();

        rSlider.setValue(127);
        gSlider.setValue(127);
        bSlider.setValue(127);

        rSlider.setMaximum(255);
        gSlider.setMaximum(255);
        bSlider.setMaximum(255);

        JPanel rPanel = new JPanel(new FlowLayout());
        JPanel gPanel = new JPanel(new FlowLayout());
        JPanel bPanel = new JPanel(new FlowLayout());

        JButton programButton = new JButton("Write Program");

        rPanel.add(rLabel);
        rPanel.add(rSlider);

        gPanel.add(gLabel);
        gPanel.add(gSlider);

        bPanel.add(bLabel);
        bPanel.add(bSlider);

        mainPanel.add(titleLabel);
        mainPanel.add(rPanel);
        mainPanel.add(gPanel);
        mainPanel.add(bPanel);
        mainPanel.add(programButton);

        frame.add(mainPanel);

        rLabel.setText("R: " + rSlider.getValue());
        gLabel.setText("G: " + gSlider.getValue());
        bLabel.setText("B: " + bSlider.getValue());

        frame.pack();

        final JFrame programFrame = new JFrame();
        JPanel programPanel = new JPanel();
        JButton runProgramButton = new JButton("Run Program");
        final JTextArea programArea = new JTextArea(15, 45);

        programPanel.add(programArea);
        programPanel.add(runProgramButton);
        programFrame.add(programPanel);
        programFrame.setName("Program Writer");

        programFrame.pack();

/*
        SerialPort port = SerialPort.getCommPort("COM5");

        */
        SerialPort[] ports = SerialPort.getCommPorts();
        int i = 1;
        System.out.println("Select a port: ");
        for (SerialPort port : ports) {
            System.out.println(i + ". " + port.getSystemPortName());
            i++;
        }

        Scanner s = new Scanner(System.in);
        int chosenPortIndex = s.nextInt();

        port = ports[chosenPortIndex - 1];
        if (port.openPort()) {
            System.out.println("Port opened");
        } else {
            System.out.println("Error: port didn't open");
            System.exit(0);
        }

        output = new PrintWriter(port.getOutputStream());

        /*int x = 0;
        while(x < 500) {
            try {
                System.out.println("test");
                sendColor(formatRGBString(255, 0, 0));
                Thread.sleep(100);
                sendColor(formatRGBString(0, 255, 0));
                Thread.sleep(100);
                sendColor(formatRGBString(0, 0, 255));
                Thread.sleep(100);
                sendColor(formatRGBString(255, 255, 255));
                Thread.sleep(100);
                x++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        rSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                rLabel.setText("R: " + rSlider.getValue());
                System.out.println("Printing: " + formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
                sendColor(formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
            }
        });

        gSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gLabel.setText("G: " + gSlider.getValue());
                System.out.println("Printing: " + formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
                sendColor(formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
            }
        });

        bSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                bLabel.setText("B: " + bSlider.getValue());
                System.out.println("Printing: " + formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
                sendColor(formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
            }
        });

        programButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                programFrame.setVisible(true);
            }
        });

        runProgramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String[] lines = programArea.getText().split("\n");
                System.out.println(programArea.getText());

                if (lines[0].trim().equals("loop")) {
                    System.out.println("looping");
                    new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                for (String line : lines) {
                                    System.out.println("PARSING: " + line);
                                    parseProgramCode(line);
                                }
                            }
                        }
                    }.run();

                } else if (lines[0].startsWith("loop")) {
                    final int numOfTimes = Integer.parseInt(lines[0].replace("loop", "").trim());
                    System.out.println("looping " + numOfTimes + " times");
                    new Thread() {
                        @Override
                        public void run() {
                            int x = 0;
                            while (x < numOfTimes) {
                                for (String line : lines) {
                                    System.out.println("PARSING: " + line);
                                    parseProgramCode(line);
                                    x++;
                                }
                            }
                        }
                    }.run();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            for (String line : lines) {
                                System.out.println("PARSING: " + line);
                                parseProgramCode(line);
                            }
                        }
                    }.run();
                }
            }
        });

        frame.setVisible(true);

        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);


        /*
        Thread sendDataThread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) { e.printStackTrace(); }

                output = new PrintWriter(port.getOutputStream());
                while(true) {
                    //put loop here
                }
            }
        };
        */
    }

    public static String formatRGBString(int r, int g, int b) {
        String[] rgb = new String[3];
        rgb[0] = "" + r;
        rgb[1] = "" + g;
        rgb[2] = "" + b;

        for (int i = 0; i < rgb.length; i++) {
            while (rgb[i].length() < 3) {
                rgb[i] = "0" + rgb[i];
            }
        }

        return rgb[0] + " " + rgb[1] + " " + rgb[2];
    }

    public static void sendColor(String rgbString) {
        output.print(rgbString);
        output.flush();
    }

    public static void parseProgramCode(String line) {
        if (line.startsWith("sleep")) {
            try {
                Thread.sleep(Integer.parseInt(line.replace("sleep", "").trim()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (line.startsWith("setColor")) {
            sendColor(line.replace("setColor", "").trim());
        }
    }
}
