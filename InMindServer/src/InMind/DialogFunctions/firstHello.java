package InMind.DialogFunctions;

import InMind.Server.asr.ASR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Amos Azaria on 30-Dec-14.
 */
public class firstHello
{
    static final String agentNameKey = "agentName";
    public static List<String> hello(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        List<String> toSayList = new LinkedList<String>();
        String agentName = null;
        //TODO: Should first check the DB if there is a name for the user.
        if (agentName == null)
            agentName = nameGenerator.generateNewName();//should add more details on requested name.
        //TODO: Should save name to the DB.

        fullInfo.put(agentNameKey, agentName);
        toSayList.add(FunctionInvoker.sayStr + "Hello, my name is " + agentName + ".");
        toSayList.add(FunctionInvoker.sayStr + "This is my unique name.");
        toSayList.add(FunctionInvoker.sayStr + "I am your personal agent.");
        toSayList.add(FunctionInvoker.sayStr + "Do you like my name?");
        return toSayList;
    }

    public static List<String> saveName(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        //TODO: should save the name to the DB as the user name.
        return null;
    }

    public static List<String> getAgentName(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        //TODO: should get from the DB.
        List<String> toSayList = new LinkedList<String>();
        toSayList.add(FunctionInvoker.sayStr + "My name is "+ fullInfo.get(agentNameKey) +".");
        toSayList.add(FunctionInvoker.sayStr + "What is your name?");
        return toSayList;
    }
}

    class nameGenerator
    {




        static int minLen = 5;
        static int maxLen = 9;
        static String year = "1980";
        static Boolean CreateMale = false;

        static int minNameCount = 1000;

        //static String allText;
        static List<String> allNameRows;
        static int[][][] trigrams = new int[27][27][27];
        static double[][][] trigramNorm = new double[27][27][27];
        static Random rand = new Random();
        static int retryWhenFail = 1000;
        public static String generateNewName()
        {
            String newName = null;
            try
            {
                CreateTrigramsOccur();
                newName = GetName(minLen, maxLen);
            } catch (IOException e)
            {
                e.printStackTrace();
                newName="noname";
            }
            return newName;
        }

        private static String GetName(int minLen, int maxLen)
        {
            int[] currTrigram = { 0, 0, 0 };
            String newName = "";
            Boolean stop = false;
            int tryToStop = 0;
            while (!stop)
            {
                double r = rand.nextDouble();
                double accumProb = 0;
                for (int k = 0; k < trigrams[2].length; k++)
                {
                    double kprob = trigramNorm[currTrigram[1]][currTrigram[2]][k];
                    accumProb += kprob;
                    if (r < accumProb)
                    {
                        if (k == 0)
                        {
                            tryToStop++;
                            if (newName.length() >= minLen || tryToStop > retryWhenFail)
                                stop = true;
                            break;
                        }
                        else if (newName.length() >= maxLen - 1)
                        {
                            if (trigramNorm[currTrigram[2]][k][0] > 0)
                            {
                                stop = true;
                            }
                            else if (tryToStop < retryWhenFail)
                        {
                            tryToStop++;
                            break;
                        }
                        }
                        tryToStop = 0;
                        char letter = (char)(k + 'a' - 1);
                        newName += letter;
                        AddLetter(currTrigram, letter);
                        break;
                    }
                }
            }
            return newName;
//            Console.Write(newName + ",");
//            if (allText.IndexOf('\n' + newName + ',') == -1)
//                Console.WriteLine();//"New");
//            else
//                Console.WriteLine(" Old");
        }

        private static void CreateTrigramsOccur() throws IOException
        {
            allNameRows = Files.readAllLines(Paths.get("..\\names\\yob" + year + ".txt"));
            //allNameRows = allText.split("\n");
            for (String nameRow : allNameRows)
            {
                if (nameRow == "")
                    continue;
                nameRow = nameRow.toLowerCase();
                String[] nameFeatures = nameRow.split(",");
                String name = nameFeatures[0].toLowerCase() + (char)('a' - 1); // +(char)('a' - 1);
                Boolean male = nameFeatures[1] == "m";
                int occur = Integer.parseInt(nameFeatures[2]);
                if (male != CreateMale || occur < minNameCount)
                    continue;
                int[] currTrigram = { 0, 0, 0 };
                for (char letter : name.toCharArray())
                {
                    AddLetter(currTrigram, letter);
                    trigrams[currTrigram[0]][currTrigram[1]][currTrigram[2]] += occur;
                }
            }

            //int countZero = 0;

            for (int i = 0; i < trigrams[0].length; i++)
            {
                for (int j = 0; j < trigrams[1].length; j++)
                {
                    double occursInij = 0;
                    for (int k = 0; k < trigrams[2].length; k++)
                    {
                        occursInij += trigrams[i][j][k];
                        //if (trigrams[i, j, k] < 100)
                        //countZero++;
                    }
                    for (int k = 0; k < trigrams[2].length; k++)
                    {
                        if (occursInij > 0)
                            trigramNorm[i][j][k] = trigrams[i][j][k] / occursInij;
                    }
                }
            }
        }

        private static void AddLetter(int[] currTrigram, char letter)
        {
            currTrigram[0] = currTrigram[1];
            currTrigram[1] = currTrigram[2];
            currTrigram[2] = letter - 'a' + 1;
        }
    }



