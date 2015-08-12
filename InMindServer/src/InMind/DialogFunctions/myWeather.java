package InMind.DialogFunctions;

import InMind.Server.asr.ASR;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Amos Azaria on 29-Dec-14.
 */
public class myWeather
{
    public static List<String> getWeatherInfo(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        final String locationTag = "location";
        List<String> ret = new LinkedList<String>();
        String location = null;
        if (fullInfo.containsKey(locationTag) && fullInfo.get(locationTag) != null)
            location = (String) fullInfo.get(locationTag);

        if (location == null || location.isEmpty())
            ret.add(FunctionInvoker.sayStr + "It seems that I am missing some information.");
        else
        {
            ret.add(FunctionInvoker.sayStr + "It is sunny in " + location + "!");
            ret.add(FunctionInvoker.sayStr + "I always say sunny because I have no knowledge base...");
        }
        return ret;
    }
}
