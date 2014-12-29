package InMind.Server;

import java.nio.file.Path;

/**
 * Created by User on 16-Dec-14.
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

            if (message.equals("Client Connected"))
            {
                tcpServer.sendMessage("ConnectUDP^" + udpDefaultPort);

                StreamAudioServer streamAudioServer = new StreamAudioServer();

                Path obtainedFile = streamAudioServer.runServer(udpDefaultPort);

                tcpServer.sendMessage("StopUDP^");

                ASR.AsrRes asrRes = ASR.getGoogleASR(obtainedFile);

                System.out.println(asrRes.text);

                userConversation.dealWithMessage(asrRes, new MessageSender());

                tcpServer.abandonClient();

                runServer();
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
