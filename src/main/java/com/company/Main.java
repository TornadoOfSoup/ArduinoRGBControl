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
import java.util.Scanner;

public class Main {

    static JLabel rLabel = new JLabel("R: 0  ");
    static JLabel gLabel = new JLabel("G: 0  ");
    static JLabel bLabel = new JLabel("B: 0  ");

    static SerialPort port;
    static PrintWriter output;

    static Thread programThread;

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
        JScrollPane scrollPane = new JScrollPane(programArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        //programPanel.add(programArea);
        programPanel.add(scrollPane);
        programPanel.add(runProgramButton);
        programFrame.add(programPanel);
        programFrame.setTitle("Program Writer");

        programFrame.pack();

/*
        SerialPort port = SerialPort.getCommPort("COM5");

        */
        SerialPort[] ports = SerialPort.getCommPorts();
        int chosenPortIndex = 0;
        if (ports.length > 1) {
            int i = 1;
            System.out.println("Select a port: ");
            for (SerialPort port : ports) {
                System.out.println(i + ". " + port.getSystemPortName());
                i++;
            }

            Scanner s = new Scanner(System.in);
            chosenPortIndex = s.nextInt() - 1;
        }

        port = ports[chosenPortIndex];
        if (port.openPort()) {
            System.out.println("Port opened");
            port.setBaudRate(115200);
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
                rLabel.setText(String.format("R: %3d", rSlider.getValue()));
                System.out.println("Printing: " + formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
                sendColor(formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
            }
        });

        gSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gLabel.setText(String.format("G: %3d", gSlider.getValue()));
                System.out.println("Printing: " + formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
                sendColor(formatRGBString(rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
            }
        });

        bSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                bLabel.setText(String.format("B: %3d", bSlider.getValue()));
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

                if (programThread != null) {
                    programThread.interrupt();
                }
                programThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            parseProgram(lines);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                };

                programThread.start();
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

    public static String hexToRGB(String hexString) {
        Color c = Color.decode(hexString);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        return formatRGBString(r, g, b);
    }

    public static void sendColor(String rgbString) {
        output.print(rgbString);
        output.flush();
    }

    public static void sendColor(Color color) {
        output.print(colorToRGBString(color));
        output.flush();
    }

    public static void setBackgroundLights(boolean on) {
        if (on) {
            output.print("bg on");
        } else {
            output.print("bg off");
        }
        output.flush();
    }

    public static void setBackgroundLights(int brightness) {
        if (brightness < 0 || brightness > 255) {
            throw new IllegalArgumentException("Number " + brightness + " out of range 0-255");
        } else {
            output.print("bg " + brightness);
            output.flush();
        }
    }

    public static String colorToRGBString(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        return formatRGBString(r, g, b);
    }

    public static void parseProgram (final String[] lines) throws InterruptedException {
        if (lines[0].trim().equalsIgnoreCase("loop")) {
            System.out.println("looping");
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        for (String line : lines) {
                            parseProgramLine(line);
                        }
                    }
                }
            }.start();

        } else if (lines[0].startsWith("loop")) { //TODO add a way to loop parts of the code
            final int numOfTimes = Integer.parseInt(lines[0].replace("loop", "").trim());
            System.out.println("looping " + numOfTimes + " times");
            new Thread() {
                @Override
                public void run() {
                    int x = 0;
                    while (x < numOfTimes) {
                        for (String line : lines) {
                            parseProgramLine(line);
                        }
                        x++;
                    }
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    for (String line : lines) {
                        parseProgramLine(line);
                    }
                }
            }.start();
        }
    }

    public static void parseProgramBETA (final String[] lines) throws InterruptedException {
        if (lines[0].trim().equalsIgnoreCase("loop")) {
            System.out.println("looping");
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        int i = 0;
                        for (String line : lines) {
                            if (i != 0 && line.toLowerCase().startsWith("loop")) {
                                int endIndex = -1; //TODO this code is nasty, clean it up probably
                                int numOfLoops = getNumberOfLoops(line);
                                int sizeOfLoop;

                                for (int j = i; j < lines.length; j++) { //find out how many lines are being looped
                                    if (lines[j].equalsIgnoreCase("end")) {
                                        endIndex = j;
                                        break;
                                    }
                                }
                                if (endIndex == -1) {
                                    endIndex = lines.length - 1;
                                }
                                sizeOfLoop = endIndex - i;

                                ArrayList<String> loop = new ArrayList<String>();
                                for (int j = i; j < endIndex; j++) {
                                    loop.add(lines[j]);
                                }
                                try {
                                    for (int loops = 0; loops < numOfLoops; loops++) {
                                        parseProgram((String[]) loop.toArray());
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                i += sizeOfLoop;
                            } else {
                                parseProgramLine(line);
                            }
                            i++;
                        }
                    }
                }
            }.start();

        } else if (lines[0].startsWith("loop")) { //TODO add a way to loop parts of the code
            final int numOfTimes = Integer.parseInt(lines[0].replace("loop", "").trim());
            System.out.println("looping " + numOfTimes + " times");
            new Thread() {
                @Override
                public void run() {
                    int x = 0;
                    while (x < numOfTimes) {
                        int i = 0;
                        for (String line : lines) {
                            if (i != 0 && line.toLowerCase().startsWith("loop")) {
                                int endIndex = -1; //TODO this code is nasty, clean it up probably
                                int numOfLoops = getNumberOfLoops(line);
                                int sizeOfLoop;

                                for (int j = i; j < lines.length; j++) { //find out how many lines are being looped
                                    if (lines[j].equalsIgnoreCase("end")) {
                                        endIndex = j;
                                        break;
                                    }
                                }
                                if (endIndex == -1) {
                                    endIndex = lines.length - 1;
                                }
                                sizeOfLoop = endIndex - i;

                                ArrayList<String> loop = new ArrayList<String>();
                                for (int j = i; j < endIndex; j++) {
                                    loop.add(lines[j]);
                                }
                                try {
                                    for (int loops = 0; loops < numOfLoops; loops++) {
                                        parseProgram((String[]) loop.toArray());
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                i += sizeOfLoop;
                            } else {
                                parseProgramLine(line);
                            }
                            i++;
                        }
                        x++;
                    }
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    int i = 0;
                    for (String line : lines) {
                        if (i != 0 && line.toLowerCase().startsWith("loop")) {
                            int endIndex = -1; //TODO this code is nasty, clean it up probably
                            int numOfLoops = getNumberOfLoops(line);
                            int sizeOfLoop;

                            for (int j = i; j < lines.length; j++) { //find out how many lines are being looped
                                if (lines[j].equalsIgnoreCase("end")) {
                                    endIndex = j;
                                    break;
                                }
                            }
                            if (endIndex == -1) {
                                endIndex = lines.length - 1;
                            }
                            sizeOfLoop = endIndex - i;

                            ArrayList<String> loop = new ArrayList<String>();
                            for (int j = i + 1; j < endIndex; j++) {
                                loop.add(lines[j]);
                            }
                            try {
                                for (int loops = 0; loops < numOfLoops; loops++) {
                                    parseProgram(loop.toArray(new String[sizeOfLoop]));
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            i += sizeOfLoop;
                        } else {
                            parseProgramLine(line);
                        }
                        i++;
                    }
                }
            }.start();
        }
    }
    public static void parseProgramLine(String line) {
        System.out.println("PARSING: " + line);

        if (line.toLowerCase().startsWith("sleep")) {
            try {
                Thread.sleep(Integer.parseInt(line.replace("sleep", "").trim()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (line.toLowerCase().startsWith("setcolor")) {
            String[] parts = line.toLowerCase().replace("setcolor", "").split(" ");
            if (parts.length == 1) {
                sendColor(hexToRGB(parts[0])); //hex
            } else {
                sendColor(line.toLowerCase().replace("setcolor", "").trim());
            }
        } else if (line.toLowerCase().startsWith("strobe")) {
            if (line.toLowerCase().startsWith("strobe bg")) {
                String[] parts = line.toLowerCase().replace("strobe bg ", "").split(" ");
                int timeBetweenColors = Integer.parseInt(parts[0]);
                int numberOfTimes = Integer.parseInt(parts[1]);
                for (int i = 0; i < numberOfTimes; i++) {
                    try {
                        setBackgroundLights(true);
                        Thread.sleep(timeBetweenColors);
                        setBackgroundLights(false);
                        Thread.sleep(timeBetweenColors);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String[] parts = line.toLowerCase().replace("strobe ", "").split(" ");
                //[0] colorA, [1] colorB, [2] timeBetweenThem, [3] numberOfTimes
                String colorA = hexToRGB(parts[0]);
                String colorB = hexToRGB(parts[1]);
                int timeBetweenColors = Integer.parseInt(parts[2]);
                int numberOfTimes = Integer.parseInt(parts[3]);

                for (int i = 0; i < numberOfTimes; i++) {
                    try {
                        sendColor(colorA);
                        Thread.sleep(timeBetweenColors);
                        sendColor(colorB);
                        Thread.sleep(timeBetweenColors);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (line.toLowerCase().startsWith("fade") && !line.toLowerCase().startsWith("fadebg")) {
            String[] parts = line.toLowerCase().replace("fade ", "").split(" ");
            try {
                fade(parts);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

        } else if (line.toLowerCase().startsWith("fadebg")) {
            String[] parts = line.toLowerCase().replace("fadebg ", "").split(" ");
            try {
                fadeBg(parts);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        } else if (line.toLowerCase().startsWith("hitfade") && !line.toLowerCase().startsWith("hitfadebg")) {
            String[] parts = line.toLowerCase().replace("hitfade ", "").split(" ");
            String color = parts[0];
            int time = Integer.parseInt(parts[1]);

            try {
                fade(new String[]{"0x000000", color, "" + Math.round(time * 0.1)});
                fade(new String[]{color, "0x000000", "" + Math.round(time * 0.9)});
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            sendColor("000 000 000");
        } else if (line.toLowerCase().startsWith("hitfadebg")) {
            String[] parts = line.toLowerCase().replace("hitfadebg ", "").split(" ");
            int brightness = Integer.parseInt(parts[0]);
            int time = Integer.parseInt(parts[1]);

            try {
                fadeBg(new String[]{"" + 0, "" + brightness, "" + Math.round(time * 0.1)});
                fadeBg(new String[]{"" + brightness, "" + 0, "" + Math.round(time * 0.9)});
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            setBackgroundLights(false);
        } else if (line.toLowerCase().startsWith("breathe") && !line.toLowerCase().startsWith("breathebg")) {
            String[] parts = line.toLowerCase().replace("breathe ", "").split(" ");

            final String color = parts[0];
            final int timeForFade;
            final int totalTime;
            final String colorB;

            if (parts.length < 4) {
                colorB = "0x000000";
                timeForFade = Integer.parseInt(parts[1]);
                totalTime = Integer.parseInt(parts[2]);
            } else {
                colorB = parts[1];
                timeForFade = Integer.parseInt(parts[2]);
                totalTime = Integer.parseInt(parts[3]);
            }

            Thread breatheThread = new Thread() {
                @Override
                public void run() {
                    while(!Thread.interrupted()) {
                        try {
                            fade(new String[]{colorB, color, "" + timeForFade});
                            fade(new String[]{color, colorB, "" + timeForFade});
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            };

            breatheThread.start();
            try {
                Thread.sleep(totalTime);
                if (breatheThread.isAlive()) {
                    breatheThread.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (line.toLowerCase().startsWith("breathebg")) {
            String[] parts = line.toLowerCase().replace("breathebg ", "").split(" ");

            final String brightness = parts[0];
            final int timeForFade;
            final int totalTime;
            final String brightnessB;

            if (parts.length < 4) {
                brightnessB = "0";
                timeForFade = Integer.parseInt(parts[1]);
                totalTime = Integer.parseInt(parts[2]);
            } else {
                brightnessB = parts[1];
                timeForFade = Integer.parseInt(parts[2]);
                totalTime = Integer.parseInt(parts[3]);
            }

            Thread breatheBgThread = new Thread() {
                @Override
                public void run() {
                    while(!Thread.interrupted()) {
                        try {
                            fadeBg(new String[]{brightnessB, brightness, "" + timeForFade});
                            fadeBg(new String[]{brightness, brightnessB, "" + timeForFade});
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            };

            breatheBgThread.start();
            try {
                Thread.sleep(totalTime);
                if (breatheBgThread.isAlive()) {
                    breatheBgThread.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (line.toLowerCase().startsWith("bg")) {
            if (line.equalsIgnoreCase("bg on")) {
                setBackgroundLights(true);
            } else if (line.equalsIgnoreCase("bg off")) {
                setBackgroundLights(false);
            } else {
                int brightness = Integer.parseInt(line.replace("bg ", ""));
                setBackgroundLights(brightness);
            }
        }
    }

    public static Color colorXPercentBetweenTwoColors(Color colorA, Color colorB, double percent) {
        int r = (int) Math.round((colorA.getRed() * percent) + (colorB.getRed() * (1 - percent)));
        int g = (int) Math.round((colorA.getGreen() * percent + colorB.getGreen() * (1 - percent)));
        int b = (int) Math.round((colorA.getBlue() * percent) + (colorB.getBlue() * (1 - percent)));
        Color colorC = new Color(r, g, b);

        //System.out.println(colorA.toString() + " + " + colorB.toString() + " = " + colorC.toString());
        return colorC;
    }

    public static void fade(String[] parts) throws InterruptedException {
        Color colorA = Color.decode(parts[0]);
        Color colorB = Color.decode(parts[1]);
        int time = Integer.parseInt(parts[2]);

        int numOfCycles = 100;
        if (time < 6000) {
            numOfCycles = time / 80;
        }

        int timeBetweenCycles = (int) Math.round((double) time / numOfCycles);
        double percentageOfColorA = 1;
        for (int i = 0; i < numOfCycles; i++) {
            sendColor(colorXPercentBetweenTwoColors(colorA, colorB, percentageOfColorA));
            percentageOfColorA = percentageOfColorA - (1.0/numOfCycles);
            Thread.sleep(timeBetweenCycles);
        }
    }

    public static void fadeBg(String[] parts) throws InterruptedException {
        int brightnessA = Integer.parseInt(parts[0]);
        int brightnessB = Integer.parseInt(parts[1]);
        int difference = Math.abs(brightnessA - brightnessB);
        int time = Integer.parseInt(parts[2]);

        int numOfCycles = 100;
        if (time < 6000) {
            numOfCycles = time / 80;
        }

        int timeBetweenCycles = (int) Math.round((double) time / numOfCycles);
        double percentageOfBrightnessA = 1.0;
        for (int i = 0; i < numOfCycles; i++) {
            //setBackgroundLights((int) Math.round(difference * percentageOfBrightnessA + Math.min(brightnessA, brightnessB)));
            setBackgroundLights((int) Math.round((brightnessA * percentageOfBrightnessA) + (brightnessB * (1 - percentageOfBrightnessA))));
            percentageOfBrightnessA = percentageOfBrightnessA - (1.0 / numOfCycles);
            Thread.sleep(timeBetweenCycles);
        }
    }

    /**
     * A helper method to clean up the looping code
     * @param line
     * @return
     */
    public static int getNumberOfLoops(String line) {
        if (line.equalsIgnoreCase("loop")) { //figure out how many loops
            return Integer.MAX_VALUE;
        }
        else {
            return Integer.parseInt(line.toLowerCase().replace("loop ", ""));
        }
    }
}
