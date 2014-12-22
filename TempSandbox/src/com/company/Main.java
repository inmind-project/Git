package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        String format = (new SimpleDateFormat("File-ddMMyy-hhmmss.SSS.raw")).format(new Date());
        int a = 5;
        //regexify();
    }

    private static void regexify() {
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
