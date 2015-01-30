package InMind.Server;

import InMind.Consts;

import java.io.PrintWriter;
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
                    dealWithText(asrRes);
                }

                if (m.group(1).equalsIgnoreCase(Consts.requestSendAudio))
                {
                    tcpServer.sendMessage(Consts.connectUdp + Consts.commandChar + udpDefaultPort);



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
                            try
                            {
                                ASR.AsrRes asrRes = null;
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
                                    dealWithText(asrRes);
                                }
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                    });



                    streamAudioServer.runServer(udpDefaultPort);

                    //asrRes = ASR.getGoogleASR(obtainedFile);

                }

            }
        }

        private void dealWithText(ASR.AsrRes asrRes)
        {
            if (asrRes != null && asrRes.text != null && !asrRes.text.isEmpty())
            {
                userConversation.dealWithMessage(asrRes, new MessageSender());
            }

            tcpServer.abandonClient();

            runServer();
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
