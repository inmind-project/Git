package com.InMind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    // returns responses
    // may have multiple (say something and launch something.
    static public List<String> getResponse(String userSentence)
    {
        List<String> response = new LinkedList<String>();


        Path filePath = Paths.get("..\\Configurations\\logic.csv");//URI filePath = URI.create("file:///C:/InMind/git/Configurations/logic.csv");
        String line = "";
        String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader bufferedReader = null;

        try{
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null) {

                // use comma as separator
                String[] row = line.split(cvsSplitBy,-1);
                Pattern p = Pattern.compile((row[0]), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(userSentence);
                if (m.matches())
                {
                    row[1] = row[1].replace("\"\"","\"");
                    row[1] = row[1].replaceAll("^\"|\"$","");
                    for (int i = 1; ; i++) {
                        if (row[1].contains("%" + i)) {
                            row[1] = row[1].replace("%" + i, m.group(i));
                        } else if (row[1].contains("%r" + i)) //replace with reflection
                        {
                            row[1] = row[1].replace("%r" + i, reflect(m.group(i)));
                        } else
                            break;
                    }
                    response.add("Say^" + row[1]);
                    if (!row[2].isEmpty())
                        response.add(row[2]);
                    break;
                }

            }

        }catch (Exception ex) {
            System.out.println("Logic: error in response");
        }
        return response;
    }

    private static String reflect(String text) {

        Path filePath = Paths.get("..\\Configurations\\reflection.csv"); //URI filePath = URI.create("file:///C:/InMind/git/Configurations/reflection.csv");
        String line = "";
        String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader bufferedReader = null;
        String alreadyReplaced = "~";

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null) {

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                String wordsToMatch = "(?i)"+row[0]; //ignore case
                String toMatch = "^"+wordsToMatch+" | "+wordsToMatch+" | "+wordsToMatch+"$|^"+wordsToMatch+"$";
                String excludeAlreadyReplaced = toMatch+"(?=([^"+alreadyReplaced+"]*"+alreadyReplaced+"[^"+alreadyReplaced+"]*"+alreadyReplaced+")*[^"+alreadyReplaced+"]*$)";

                text = text.replaceAll(excludeAlreadyReplaced,"~"+row[1]+"~");
            }
        } catch (Exception ex) {
            System.out.println("Logic: error in reflection");
            return text;
        }

        return text.replaceAll(alreadyReplaced," ").trim();

    }


    public class MessageReceiver implements TCPServer.OnMessageReceived {

        TCPServer tcpServer = null;
        //this method declared in the interface from TCPServer class is implemented here
        //this method is actually a callback method, because it will run every time when it will be called from
        //TCPServer class (at while)
        public void messageReceived(String message) {

            if (message.equals("Client Connected")) {
                tcpServer.sendMessage("ConnectUDP^" + udpDefaultPort);

                Path obtainedFile = StreamAudioServer.runServer(udpDefaultPort);

                ASR.AsrRes asrRes = ASR.getGoogleASR(obtainedFile);

                System.out.println(asrRes.text);

                List<String> response = getResponse(asrRes.text);
                for(String command : response) {

                    tcpServer.sendMessage(command);//"Say^You Said:" + asrRes.text);
                }

                tcpServer.abandonClient();

                runServer();
            }
        }
    }
}
