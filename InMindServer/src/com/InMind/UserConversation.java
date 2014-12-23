package com.InMind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 23-Dec-14.
 */
public class UserConversation {


    static final String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    static final String basePath = "..\\Configurations\\";
    static final String commandChar = "^";
    static final String conditionsAndSetChar = ";";
    static final String condAndSetEqual = "=";

    static final String stateName = "state";
    static final String callFunName = "callfun";


    String dialogFile = "";
    Map<String,Object> fullInfo;

    public UserConversation()
    {
        fullInfo = new HashMap<String, Object>();
        clearDialog();
    }


    public void dealWithMessage(ASR.AsrRes asrRes, InMindLogic.MessageReceiver.MessageSender messageSender)
    {
        List<String> sendToUser = new LinkedList<String>();

        if (dialogFile.isEmpty()) {
            dialogFile = findDialogFile(asrRes.text);
            if (!dialogFile.isEmpty()) {
                fullInfo.put(stateName, "start");
                //TODO: may also want to load information from DB. maybe add a "required" field to dialogIndex file
            }
        }

        if (dialogFile.isEmpty()) {

            sendToUser = getResponse(asrRes.text);
        }
        else
        {
            String toSay = executeDialogFile(asrRes.text);
            if (!toSay.isEmpty())
                sendToUser.add(toSay);

            if (fullInfo.get(stateName).equals("return"))
            {
                clearDialog();
                dealWithMessage(asrRes, messageSender); //recall this function and send relevant information
                return;
            }

            if (fullInfo.get(stateName).equals("finish"))
            {
                clearDialog();
            }

        }

        for (String command : sendToUser) {

            if (!command.isEmpty())
                messageSender.sendMessage(command);//"Say^You Said:" + asrRes.text);
        }

    }

    private String executeDialogFile(String userSentence) {
        String commandSay = "";
        String toSay = "";


        final int csvConditions = 0;
        final int csvPattern = 1;
        final int csvSay = 2;
        final int csvSet = 3;


        Path filePath = Paths.get(basePath,dialogFile);//URI filePath = URI.create("file:///C:/InMind/git/Configurations/logic.csv");
        String line = "";
        BufferedReader bufferedReader = null;

        try{
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            //line = bufferedReader.readLine(); //ignore first line
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                // use comma as separator
                String[] row = line.split(cvsSplitBy,-1);
                boolean matchesConditions = true;
                if (!row[csvConditions].isEmpty())
                {
                    String conditions = removeExtraQuotes(row[csvConditions]);
                    String[] conditionList = conditions.split(conditionsAndSetChar);
                    for (String cond : conditionList)
                    {
                        String[] varVal = cond.split(condAndSetEqual);
                        if (varVal.length != 2)
                            continue;
                        Object requiredVal;
                        if (varVal[1].startsWith("\"") && varVal[1].endsWith("\"")) //if is surrounded by quotes, this is a string, remove quotes.
                            requiredVal = new String(varVal[1].substring(1,varVal[1].length()-1));
                        else
                            requiredVal = new Double(varVal[1]);
                        Object storedVal = fullInfo.get(varVal[0]);
                        if (storedVal == null || !storedVal.equals(requiredVal))
                        {
                            matchesConditions = false;
                            break;
                        }
                    }
                }

                if (matchesConditions) {
                    Pattern p = Pattern.compile((row[csvPattern]), Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(userSentence);
                    if (m.matches()) {
                        //retFile= row[csvFile];
                        toSay = refactorSentenceToSay(row[csvSay], m);

                        String toSet = removeExtraQuotes(row[csvSet]);
                        String[] setList = toSet.split(conditionsAndSetChar);
                        for (String singleSet : setList)
                        {
                            String[] varVal = singleSet.split(condAndSetEqual);
                            if (varVal.length != 2)
                                continue;
                            Object setTo;
                            if (varVal[1].startsWith("\"") && varVal[1].endsWith("\"")) //if is surrounded by quotes, this is a string, remove quotes.
                                setTo = new String(varVal[1].substring(1,varVal[1].length()-1));
                            else
                                setTo = new Double(varVal[1]); //TODO: may be referring to itself or other variables!
                            fullInfo.put(varVal[0],setTo);
                            //TODO: check if is callfun!!!
                        }

                        break;
                    }
                }

            }

        }catch (Exception ex) {
            System.out.println("UserConversation: error in dialog file");
        }


        if (!toSay.isEmpty())
            commandSay = commandSay(toSay);
        return commandSay;
    }

    private void clearDialog() {
        dialogFile = "";
        fullInfo.clear();
        fullInfo.put(stateName,"");
    }

    private String findDialogFile(String userSentence) {
        final int csvPattern = 0;
        final int csvFile = 1;
        String retFile = "";


        Path filePath = Paths.get(basePath,"dialogIndex.csv");//URI filePath = URI.create("file:///C:/InMind/git/Configurations/logic.csv");
        String line = "";
        BufferedReader bufferedReader = null;

        try{
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null) {

                if (line.isEmpty())
                    continue;
                // use comma as separator
                String[] row = line.split(cvsSplitBy,-1);
                String forPattern = row[csvPattern];
                if (!forPattern.isEmpty()) {
                    Pattern p = Pattern.compile((forPattern), Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(userSentence);
                    if (m.matches()) {
                        retFile = row[csvFile];
                        break;
                    }
                }

            }

        }catch (Exception ex) {
            System.out.println("UserConversation: error in finding file");
        }
        return retFile;
    }


    // returns responses
    // may have multiple (say something and launch something.
    static public List<String> getResponse(String userSentence)
    {
        final int csvPattern = 0;
        final int csvSay = 1;
        final int csvAdditionalCommand = 2;

        List<String> response = new LinkedList<String>();


        Path filePath = Paths.get(basePath,"logic.csv");//URI filePath = URI.create("file:///C:/InMind/git/Configurations/logic.csv");
        String line = "";
        BufferedReader bufferedReader = null;

        try{
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null) {

                // use comma as separator
                String[] row = line.split(cvsSplitBy,-1);
                Pattern p = Pattern.compile((row[csvPattern]), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(userSentence);
                if (m.matches())
                {
                    String toSay = refactorSentenceToSay(row[csvSay], m);
                    if (!row[csvSay].isEmpty()) {
                        response.add(commandSay(toSay));
                    }
                    if (!row[csvAdditionalCommand].isEmpty())
                        response.add(row[csvAdditionalCommand]);
                    break;
                }

            }

        }catch (Exception ex) {
            System.out.println("Logic: error in response");
        }
        return response;
    }

    private static String commandSay(String toSay) {
        return "Say"+commandChar+toSay;
    }

    private static String refactorSentenceToSay(String toSay, Matcher m) {
        toSay = removeExtraQuotes(toSay);
        if (toSay.contains("%")) {
            for (int i = 1; i <= m.groupCount() ; i++) {
                if (toSay.contains("%" + i)) { //replace without reflection
                    toSay = toSay.replace("%" + i, m.group(i));
                }
                if (toSay.contains("%r" + i)) //replace with reflection
                {
                    toSay = toSay.replace("%r" + i, reflect(m.group(i)));
                }
            }
        }
        return toSay;
    }

    private static String removeExtraQuotes(String string) {
        string = string.replace("\"\"", "\""); //replace double quotes " " with single "
        string = string.replaceAll("^\"|\"$", ""); //remove quotes from beginning or end of sentence.
        return string;
    }

    private static String reflect(String text) {

        final int reflector = 0;
        final int reflectee = 1;

        Path filePath = Paths.get(basePath,"reflection.csv"); //URI filePath = URI.create("file:///C:/InMind/git/Configurations/reflection.csv");
        String line = "";
        BufferedReader bufferedReader = null;
        String alreadyReplaced = "~";

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null) {

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                String wordsToMatch = "(?i)"+row[reflector]; //ignore case
                String toMatch = "^"+wordsToMatch+" | "+wordsToMatch+" | "+wordsToMatch+"$|^"+wordsToMatch+"$";
                String excludeAlreadyReplaced = toMatch+"(?=([^"+alreadyReplaced+"]*"+alreadyReplaced+"[^"+alreadyReplaced+"]*"+alreadyReplaced+")*[^"+alreadyReplaced+"]*$)";

                text = text.replaceAll(excludeAlreadyReplaced,"~"+row[reflectee]+"~");
            }
        } catch (Exception ex) {
            System.out.println("Logic: error in reflection");
            return text;
        }

        return text.replaceAll(alreadyReplaced," ").trim();

    }
}
