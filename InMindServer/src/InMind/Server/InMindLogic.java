package InMind.Server;

import InMind.Consts;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amos Azaria on 16-Dec-14.
 */
public class InMindLogic
{

    static final int udpDefaultPort = 50005;

    UserConversation userConversation = new UserConversation();

    public void runServer()
    {

        //creates the object OnMessageReceived asked by the TCPServer constructor
        MessageReceiver messageReceiver = new MessageReceiver();
        TCPServer tcpServer = new TCPServer(messageReceiver);
        messageReceiver.tcpServer = tcpServer;
        tcpServer.start();
    }


    public interface SendMessageToUser
    {
        public void sendMessage(String message);
    }


    public class MessageReceiver implements TCPServer.OnMessageReceived
    {

        TCPServer tcpServer = null;

        //this method declared in the interface from TCPServer class is implemented here
        //this method is actually a callback method, because it will run every time when it will be called from
        //TCPServer class (at while)
        public void messageReceived(String message)
        {
            System.out.println("InMindLogic" + "Dealing with message:" + message);
            Pattern p = Pattern.compile(Consts.messagePattern);
            Matcher m = p.matcher(message);
            boolean found = m.find();
            if (found)
            {
                ASR.AsrRes asrRes = null;

                if (m.group(1).equalsIgnoreCase(Consts.sendingText))
                {
                    asrRes = new ASR.AsrRes();
                    asrRes.confidence = 1;
                    asrRes.text = m.group(2).trim();//TODO: might want to remove punctuation.
                }

                if (m.group(1).equalsIgnoreCase(Consts.requestSendAudio))
                {
                    tcpServer.sendMessage(Consts.connectUdp + Consts.commandChar + udpDefaultPort);

                    StreamAudioServer streamAudioServer = new StreamAudioServer();

                    Path obtainedFile = streamAudioServer.runServer(udpDefaultPort);

                    tcpServer.sendMessage(Consts.stopUdp + Consts.commandChar);

                    asrRes = ASR.getGoogleASR(obtainedFile);

                    System.out.println(asrRes.text);
                }

                if (asrRes != null)
                {
                    userConversation.dealWithMessage(asrRes, new MessageSender());

                    tcpServer.abandonClient();

                    runServer();
                }
            }
        }

        public class MessageSender implements SendMessageToUser
        {
            @Override
            public void sendMessage(String message)
            {
                if (tcpServer != null)
                    tcpServer.sendMessage(message);
            }
        }

    }
}
