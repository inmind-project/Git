package InMind.Server;

import InMind.Consts;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amos Azaria on 16-Dec-14.
 */
public class InMindLogic
{

    static final int udpFirstPort = 50005;
    static final int udpLastPort = 52005;
    int currentPort = udpFirstPort;
    int nextPort()
    {
        currentPort++;
        if (currentPort > udpLastPort)
            currentPort = udpFirstPort;
        return currentPort;
    }

    Map<String,UserConversation> userConversationMap = new HashMap<String,UserConversation>();

    public void runServer()
    {

        while (true)
        {
            try
            {
                //creates the object OnMessageReceived asked by the TCPServer constructor
                MessageReceiver messageReceiver = new MessageReceiver();
                TCPServer tcpServer = new TCPServer(messageReceiver);
                messageReceiver.tcpServer = tcpServer;
                tcpServer.listenForConnection(); //blocking until accept
                tcpServer.start();

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }


    public interface SendMessageToUser
    {
        public void sendMessage(String message);
    }


    public class MessageReceiver implements TCPServer.OnMessageReceived
    {

        TCPServer tcpServer = null;
        String userId = null;

        //this method declared in the interface from TCPServer class is implemented here
        //this method is actually a callback method, because it will run every time when it will be called from
        //TCPServer class (at while)
        public void messageReceived(String message)
        {
            try
            {
                System.out.println("InMindLogic" + "Dealing with message:" + message);
                Pattern p = Pattern.compile(Consts.clientMessagePattern);
                Matcher m = p.matcher(message);
                boolean found = m.find();
                if (found)
                {
                    userId = m.group(1);

                    ASR.AsrRes asrRes = null;

                    if (m.group(2).equalsIgnoreCase(Consts.sendingText))
                    {
                        asrRes = new ASR.AsrRes();
                        asrRes.confidence = 1;
                        asrRes.text = m.group(2).trim();//TODO: might want to remove punctuation.
                        dealWithText(userId, asrRes);
                    }

                    if (m.group(2).equalsIgnoreCase(Consts.requestSendAudio))
                    {
                        int portToUse = nextPort();
                        tcpServer.sendMessage(Consts.connectUdp + Consts.commandChar + portToUse);


                        StreamAudioServer streamAudioServer = new StreamAudioServer(new StreamAudioServer.StreamingAlerts()
                        {
                            ASR asr = new ASR();
                            Path obtainedFile = null;

                            @Override
                            public void rawFilePath(Path filePathForSavingAudio)
                            {
                                obtainedFile = filePathForSavingAudio;
                            }

                            @Override
                            public void audioArrived(byte[] audio)
                            {
                                try
                                {
                                    if (!asr.isConnectionOpen())
                                        asr.beginTransmission();
                                    asr.sendDataAsync(audio);

                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void audioEnded()
                            {
                                ASR.AsrRes asrRes = null;
                                try
                                {
                                    if (asr.isConnectionOpen())
                                    {
                                        tcpServer.sendMessage(Consts.stopUdp + Consts.commandChar);
                                        asrRes = asr.closeAndGetResponse();
                                        if (obtainedFile != null) //write json response text file
                                        {
                                            PrintWriter pw = new PrintWriter(obtainedFile.toString() + ".txt");
                                            pw.print(asrRes.fullJsonRes);
                                            pw.flush();
                                            pw.close();
                                        }
                                        System.out.println(asrRes.text);
                                    }
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                                dealWithText(userId, asrRes);
                            }
                        });


                        streamAudioServer.runServer(portToUse);

                        //asrRes = ASR.getGoogleASR(obtainedFile);
                    }

                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private void dealWithText(String userId, ASR.AsrRes asrRes)
        {
            if (asrRes != null && asrRes.text != null && !asrRes.text.isEmpty() && userId != null)
            {
                UserConversation userConversation = null;
                if (userConversationMap.containsKey(userId))
                    userConversation = userConversationMap.get(userId);
                else
                {
                    userConversation = new UserConversation(userId);
                    userConversationMap.put(userId,userConversation);
                }

                userConversation.dealWithMessage(asrRes, new MessageSender());
            }

            tcpServer.abandonClient();

            //runServer();
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
