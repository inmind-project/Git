package com.InMind;

import java.net.URI;

/**
 * Created by User on 16-Dec-14.
 */
public class InMindLogic {

    static final int udpDefaultPort = 50005;

    public void runServer() {

        //creates the object OnMessageReceived asked by the TCPServer constructor
        MessageReceiver messageReceiver = new MessageReceiver();
        TCPServer tcpServer = new TCPServer(messageReceiver);
        messageReceiver.tcpServer = tcpServer;
        tcpServer.start();
    }


    static public String getResponse(String userSentence)
    {
        String response = "";

        return response;
    }


    public class MessageReceiver implements TCPServer.OnMessageReceived {

        TCPServer tcpServer = null;
        //this method declared in the interface from TCPServer class is implemented here
        //this method is actually a callback method, because it will run every time when it will be called from
        //TCPServer class (at while)
        public void messageReceived(String message) {

            if (message.equals("Client Connected")) {
                tcpServer.sendMessage("ConnectUDP^" + udpDefaultPort);

                URI obtainedFile = StreamAudioServer.runServer(udpDefaultPort);

                ASR.AsrRes asrRes = ASR.getGoogleASR(obtainedFile);

                System.out.println(asrRes.text);

                String response = getResponse(asrRes.text);

                tcpServer.sendMessage(response);//"Say^You Said:" + asrRes.text);

                tcpServer.abandonClient();

                runServer();
            }
        }
    }
}
