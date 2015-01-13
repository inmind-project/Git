package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws Exception
    {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("c:\\InMind\\Git\\TempSandbox\\words.txt")));
        String line;
        StringBuilder allWords = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
        {
            allWords.append(line+"|");
        }


        String sAllwords = allWords.toString();
        String pat = ".{0,5}" + "("+sAllwords+")";
        long start = System.currentTimeMillis();
        Pattern p = Pattern.compile(pat);
        Matcher m = p.matcher("afdssazebra");
        boolean match = m.matches();
        long took = System.currentTimeMillis() - start;
        System.out.println("match: " + match + ", time: " + took + " millisec");


        //System.out.println(MicrosoftASR.callFromFile(Paths.get("C:\\InMind\\git\\UserData\\InputAt311214-124907.050.raw")));

//        String valStr="as+fjk- 5";
//        String tmpValStr = valStr.replaceAll("[!?,\\+\\-\\*/]", " ");
//        String[] allRefsToVariables = tmpValStr.split("[ ]+");
//
        //ScriptEngineManager mgr = new ScriptEngineManager();
        //ScriptEngine engine = mgr.getEngineByName("JavaScript");
        //String foo = "Math.floor(Math.random() * 6) + 1";
        ////Boolean t = (Boolean)engine.eval(foo);
        ////Boolean b = new Boolean(engine.eval(foo).toString());
        //System.out.println(engine.eval(foo));

        //isSilentButDidTalk("C:\\InMind\\git\\UserData\\InputAt231214-122558.982.raw");

        //reflection();
        //String format = (new SimpleDateFormat("File-ddMMyy-hhmmss.SSS.raw")).format(new Date());
        //regexify();
    }

    private static boolean isSilentButDidTalk(String fileName)
    {
        int silentLengthNeeded = 500;
        int sampleRate = 44100;
        int considerSilent = 1500;
        int considerSpeech = 3000;

        int silentSampleLength = 0;
        int talkSampleLength = 0;
        try
        {
            byte[] asByte = Files.readAllBytes(Paths.get(fileName));
            //short[] asShort = new short[asByte.length/2];
            for (int i = 0; 2*i < asByte.length; i++)
            {
                short sample = (short) (asByte[2*i+1] << 8 | asByte[2*i]); //little endian 16bit
                if (Math.abs(sample) < considerSilent)
                    silentSampleLength++;
                else
                {
                    silentSampleLength = 0;
                    if (Math.abs(sample) > considerSpeech)
                    {
                        talkSampleLength++;
                    }
                }
            }
            double silentLength = silentSampleLength/(double)sampleRate;
            if (silentLength*1000 > silentLengthNeeded && talkSampleLength > sampleRate / 100)
                return true;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;

    }

    private static void reflection()
    {
        Method method = null;
        try {
            Package pack = Main.class.getPackage();
            method = Class.forName(pack.getName() + "." + "Main").getMethod("regexify");
                method.invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void regexify() {
        String inputText = "tell me exactly what I am happy to hear you say";
        List<String> response = new LinkedList<String>();


        URI filePath = URI.create("file:///C:/InMind/git/Configurations/logic.csv");
        String line = "";
        String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath)));

            while ((line = bufferedReader.readLine()) != null) {

                // use comma as separator
                String[] row = line.split(cvsSplitBy, -1);
                Pattern p = Pattern.compile((row[0]), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(inputText);
                if (m.matches()) {
                    row[1] = row[1].replace("\"\"", "\"");
                    row[1] = row[1].replaceAll("^\"|\"$", "");
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


        } catch (Exception ex) {
            System.out.println("Logic: error in response");
        }


        System.out.println(response);
    }

    private static String reflect(String text) {

        URI filePath = URI.create("file:///C:/InMind/git/Configurations/reflection.csv");
        String line = "";
        String cvsSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader bufferedReader = null;



        String alreadyReplaced = "~";

        try {
            bufferedReader = new BufferedReader(new FileReader(new File(filePath)));

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
}





