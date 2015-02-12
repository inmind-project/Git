package InMind.DialogFunctions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Amos Azaria on 24-Dec-14.
 */
public class myBus
{

    public static List<String> getBusInfo(Map<String, Object> fullInfo, String userId, String userText)
    {
        final String sourceTag = "source";
        final String targetTag = "target";
        List<String> ret = new LinkedList<String>();
        String source = null;
        String target = null;
        if (fullInfo.containsKey(sourceTag) && fullInfo.get(sourceTag) != null)
            source = (String) fullInfo.get(sourceTag);
        if (fullInfo.containsKey(targetTag) && fullInfo.get(targetTag) != null)
            target = (String) fullInfo.get(targetTag);

        if (source == null || source.isEmpty() || target == null || target.isEmpty())
            ret.add(FunctionInvoker.sayStr + "It seems that I am missing some information.");
        else
        {
            ret.add(FunctionInvoker.sayStr + "There is a bus leaving from " + source + " to " + target + " in 5 minutes.");
            ret.add(FunctionInvoker.sayStr + "Just kidding! I don't have a data base...");
        }
        return ret;
    }
}
