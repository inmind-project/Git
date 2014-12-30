package InMind.Server;

import InMind.DialogFunctions.FunctionInvoker;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
public class UserConversation
{


    static final String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    static final String basePath = "..\\Configurations\\";
    static final String commandChar = "^";
    static final String cstExtension = "csv";
    static final String conditionsAndSetChar = ";";
    static final String setEquality = "=";


    static final String stateName = "state";
    static final String callFunName = "callfun";


    String dialogFileBase = "";
    Map<String, Object> fullInfo;



    ScriptEngineManager mgr;
    ScriptEngine engine;
    public UserConversation()
    {
        fullInfo = new HashMap<String, Object>();
        clearDialog();
        mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("JavaScript");
    }


    public void dealWithMessage(ASR.AsrRes asrRes, InMindLogic.MessageReceiver.MessageSender messageSender)
    {
        List<String> forUser = new LinkedList<String>();

        if (dialogFileBase.isEmpty())
        {
            dialogFileBase = findDialogFile(asrRes.text);
            if (!dialogFileBase.isEmpty())
            {
                fullInfo.put(stateName, "start");
                //TODO: may also want to load information from DB. maybe add a "required" field to dialogIndex file
            }
        }

        if (dialogFileBase.isEmpty())
        {

            forUser = getSocialTalkResponse(asrRes.text);
            sendToUser(messageSender, forUser);
        }
        else
        {
            String toSay = executeDialogFile(asrRes.text);
            if (!toSay.isEmpty())
                forUser.add(toSay);

            sendToUser(messageSender, forUser);

            //check if need to call a function (callfun)
            if (fullInfo.containsKey(callFunName) && !fullInfo.get(callFunName).toString().isEmpty())
            {
                List<String> toSend = FunctionInvoker.toInvoke(dialogFileBase, fullInfo.get(callFunName).toString(), fullInfo, "n/a"); //TODO: add userId
                sendToUser(messageSender, toSend);
            }

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

    }

    private void sendToUser(InMindLogic.MessageReceiver.MessageSender messageSender, List<String> forUser)
    {
        if (forUser != null)
        {
            for (String command : forUser)
            {

                if (!command.isEmpty())
                    messageSender.sendMessage(command);//"Say^You Said:" + asrRes.text);
            }
        }
    }

    private String executeDialogFile(String userSentence)
    {
        String commandSay = "";
        String toSay = "";


        final int csvConditions = 0;
        final int csvPattern = 1;
        final int csvSay = 2;
        final int csvSet = 3;


        Path filePath = Paths.get(basePath, dialogFileBase + "." + cstExtension);//Is there a more elegant way to add the extension?
        String line = "";
        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            //line = bufferedReader.readLine(); //ignore first line
            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.isEmpty())
                    continue;

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);

                Pattern p = Pattern.compile((row[csvPattern]), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(userSentence);
                if (m.matches())
                {

                    boolean matchesConditions = true;
                    if (!row[csvConditions].isEmpty())
                    {
                        String conditions = removeExtraQuotes(row[csvConditions]);
                        String[] conditionList = conditions.split(conditionsAndSetChar);
                        for (String cond : conditionList)
                        {
                            if (!checkCondition(cond,fullInfo,m))
                            {
                                matchesConditions = false;
                                break;
                            }
                        }
                    }

                    if (matchesConditions)
                    {
                        //retFile= row[csvFile];
                        toSay = refactorStringToSay(row[csvSay], m);

                        String toSet = removeExtraQuotes(row[csvSet]);
                        String[] setList = toSet.split(conditionsAndSetChar);
                        for (String singleSet : setList)
                        {
                            if (!singleSet.isEmpty())
                            {
                                String[] varVal = singleSet.split(setEquality);
                                if (varVal.length != 2)
                                    throw new Exception("Error in:" + singleSet);
                                Object setTo = evaluateVal(varVal[1], fullInfo, m);
                                fullInfo.put(varVal[0], setTo);
                            }
                        }

                        break;
                    }
                }


            }

        } catch (Exception ex)
        {

            ex.printStackTrace();
            System.out.println("UserConversation: error in dialog file: " + ex.getMessage());
        }


        if (!toSay.isEmpty())
            commandSay = commandSay(toSay);
        return commandSay;
    }

    private void clearDialog()
    {
        dialogFileBase = "";
        fullInfo.clear();
        fullInfo.put(stateName, "");
    }

    private String findDialogFile(String userSentence)
    {
        final int csvPattern = 0;
        final int csvFile = 1;
        String retFile = "";


        Path filePath = Paths.get(basePath, "dialogIndex.csv");//URI filePath = URI.create("file:///C:/Server/git/Configurations/logic.csv");
        String line = "";
        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null)
            {

                if (line.isEmpty())
                    continue;
                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                String forPattern = row[csvPattern];
                if (!forPattern.isEmpty())
                {
                    Pattern p = Pattern.compile((forPattern), Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(userSentence);
                    if (m.matches())
                    {
                        retFile = row[csvFile];
                        break;
                    }
                }

            }

        } catch (Exception ex)
        {
            System.out.println("UserConversation: error in finding file");
        }
        return retFile;
    }


    // returns responses
    // may have multiple (say something and launch something.
    static public List<String> getSocialTalkResponse(String userSentence)
    {
        final int csvPattern = 0;
        final int csvSay = 1;
        final int csvAdditionalCommand = 2;

        List<String> response = new LinkedList<String>();


        Path filePath = Paths.get(basePath, "socialTalk.csv");//URI filePath = URI.create("file:///C:/Server/git/Configurations/logic.csv");
        String line = "";
        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null)
            {

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                Pattern p = Pattern.compile((row[csvPattern]), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(userSentence);
                if (m.matches())
                {
                    String toSay = refactorStringToSay(row[csvSay], m);
                    if (!row[csvSay].isEmpty())
                    {
                        response.add(commandSay(toSay));
                    }
                    if (!row[csvAdditionalCommand].isEmpty())
                        response.add(row[csvAdditionalCommand]);
                    break;
                }

            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println("Logic: error in response");
        }
        return response;
    }

    private static String commandSay(String toSay)
    {
        return "Say" + commandChar + toSay;
    }


    private static String refactorStringToSay(String orgSentence, Matcher m)
    {
        return refactorUsingM(removeExtraQuotes(orgSentence), m);
    }
    // removes extra quotes and takes care of %1 and %r1
    private static String refactorUsingM(String orgSentence, Matcher m)
    {
        if (orgSentence.contains("%"))
        {
            for (int i = 1; i <= m.groupCount(); i++)
            {
                if (orgSentence.contains("%" + i))
                { //replace without reflection
                    orgSentence = orgSentence.replace("%" + i, m.group(i));
                }
                if (orgSentence.contains("%r" + i)) //replace with reflection
                {
                    orgSentence = orgSentence.replace("%r" + i, reflect(m.group(i)));
                }
            }
        }
        return orgSentence;
    }

    private static String removeExtraQuotes(String string)
    {
        string = string.replace("\"\"", "\""); //replace double quotes " " with single "
        string = string.replaceAll("^\"|\"$", ""); //remove quotes from beginning or end of sentence.
        return string;
    }

    private static String reflect(String text)
    {

        final int reflector = 0;
        final int reflectee = 1;

        Path filePath = Paths.get(basePath, "reflection.csv"); //URI filePath = URI.create("file:///C:/Server/git/Configurations/reflection.csv");
        String line = "";
        BufferedReader bufferedReader = null;
        String alreadyReplaced = "~";

        try
        {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath.toString())));

            while ((line = bufferedReader.readLine()) != null)
            {

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                String wordsToMatch = "(?i)" + row[reflector]; //ignore case
                String toMatch = "^" + wordsToMatch + " | " + wordsToMatch + " | " + wordsToMatch + "$|^" + wordsToMatch + "$";
                String excludeAlreadyReplaced = toMatch + "(?=([^" + alreadyReplaced + "]*" + alreadyReplaced + "[^" + alreadyReplaced + "]*" + alreadyReplaced + ")*[^" + alreadyReplaced + "]*$)";

                text = text.replaceAll(excludeAlreadyReplaced, "~" + row[reflectee] + "~");
            }
        } catch (Exception ex)
        {
            System.out.println("Logic: error in reflection");
            return text;
        }

        return text.replaceAll(alreadyReplaced, " ").trim();

    }

    /// returns whether the condition is true or false.
    private boolean checkCondition(String cond, Map<String, Object> fullInfo, Matcher m) throws Exception
    {
        final String condEquality = "=";
        final String condEquality2 = "==";
        final String condNotEqual = "!=";
        final String condGreater = ">";
        final String condGreaterEqual = ">=";
        final String condSmaller = "<";
        final String condSmallerEqual = "<=";
        String[] condRelationList = new String[]{condEquality2,condNotEqual,condGreater,condGreaterEqual,condSmaller,condSmallerEqual,condEquality}; //condEquality must be last

        String relation = null;
        //find relation
        for (String relationOption : condRelationList)
        {
            if (cond.contains(relationOption))
            {
                relation = relationOption;
                break;
            }
        }
        if (relation == null) //no relation found
            throw new Exception("no relation found in " + cond);

        String[] varVal = cond.split(relation);
        if (varVal.length != 2)
            throw new Exception("could not parse: " + cond);

        Object val = evaluateVal(varVal[1],fullInfo,m);
        Object storedVar = fullInfo.get(varVal[0].trim());

        if (storedVar instanceof String)
        {

            boolean matches = storedVar == val || (storedVar != null && storedVar.equals(val));
            if (relation.equals(condEquality) || relation.equals(condEquality2))
                return matches;
            if (relation.equals(condNotEqual))
                return !matches;
        }

        //must be a number.
        if (relation == condEquality)
            relation = condEquality2;
        return  (Boolean)engine.eval(storedVar.toString() + relation + val.toString());//can throw exception, it's ok

    }

    private Object evaluateVal(String valStr, Map<String, Object> fullInfo,Matcher m) throws Exception
    {
        valStr = valStr.trim();

        if (valStr == "null")
            return null;

        valStr = refactorUsingM(valStr, m);

        if (valStr.startsWith("\"") && valStr.endsWith("\"")) //if is surrounded by quotes, this is a string (no referring to other strings allowed).
            return removeExtraQuotes(valStr);

        //dealing with numeric

        Object requiredVal;
        if (fullInfo.containsKey(valStr)) //check if referring to a different variable
            return fullInfo.get(valStr);

        String tmpValStr = valStr.replaceAll("[!?,\\+\\-\\*/]", " ");
        String[] allRefsToVariables = tmpValStr.split("[ ]+");
        //this may be slow if fullInfo is big. Could look for words separated by space,+,- etc. to speedup if fullInfo is very big.
        for (String refToVar : allRefsToVariables)
        {
            if (fullInfo.containsKey(refToVar))
            {
                if (fullInfo.get(refToVar) == null)
                    throw new Exception("tried to reference null: " + refToVar);
                valStr = valStr.replaceAll(refToVar, fullInfo.get(refToVar).toString());
            }
        }

        return  engine.eval(valStr);
    }
}
