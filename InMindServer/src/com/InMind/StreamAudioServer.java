package com.InMind;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.net.URI;

class StreamAudioServer {

    //AudioInputStream audioInputStream;
    // static AudioInputStream ais;
    // static AudioFormat format;
    //static boolean status = true;
    //static int port = 50005;
    //static int sampleRate = 44100;
    static URI filePath = URI.create("file:///C:/InMind/temp/fromJava.raw");//"C:\\InMind\\temp\\fromJava.raw"; //TODO: each connection should have a new file
    static int timeout = 1000;

    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;

    public static URI runServer(int udpPort) {

        URI fileWithRaw = null;

        try {

            delIfExists();

            DatagramSocket serverSocket = new DatagramSocket(udpPort);

            serverSocket.setSoTimeout(timeout);

            /**
             * Formula for lag = (byte_size/sample_rate)*2 Byte size 9728 will
             * produce ~ 0.45 seconds of lag. Voice slightly broken. Byte size 1400
             * will produce ~ 0.06 seconds of lag. Voice extremely broken. Byte size
             * 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken
             * than 9728.
             */

            byte[] receiveData = new byte[4096];

            // format = new AudioFormat(sampleRate, 16, 1, true, false);
            // dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            // sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            // sourceDataLine.open(format);
            // sourceDataLine.start();

            // FloatControl volumeControl = (FloatControl)
            // sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            // volumeControl.setValue(1.00f);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // ByteArrayInputStream baiss = new
            // ByteArrayInputStream(receivePacket.getData());

            while (true) {
                System.out.println("Waiting!");
                try {
                    serverSocket.receive(receivePacket);
                } catch (SocketTimeoutException ex) {
                    System.out.println("Time out!");
                    break;
                }

                System.out.println("Received Packet!" + receivePacket.getLength());
                toFile(receivePacket.getData(), receivePacket.getLength());
                // ais = new AudioInputStream(baiss, format,
                // receivePacket.getLength());
                // toSpeaker(receivePacket.getData());
            }
            //sourceDataLine.drain();
            //sourceDataLine.close();
            serverSocket.close();

            fileWithRaw = filePath;

        } catch (Exception ex) {
            fileWithRaw = null;
            System.out.println("StreamAudio: Error");
        }
        return fileWithRaw;
    }

    public static void toSpeaker(byte soundbytes[], int soundlength) {
        try {
            sourceDataLine.write(soundbytes, 0, soundlength);
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }

    public static void toFile(byte soundbytes[], int soundlength) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(new File(filePath), true);
            byte[] toWrite = new byte[soundlength];
            System.arraycopy(soundbytes, 0, toWrite, 0, soundlength);
            out.write(toWrite);
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void delIfExists() {
        try {
            // Delete if tempFile exists
            File fileTemp = new File(filePath);
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();
        }
    }

}