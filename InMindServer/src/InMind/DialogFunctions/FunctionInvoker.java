package InMind.DialogFunctions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 24-Dec-14.
 */
public class FunctionInvoker
{

    static final String sayStr = "Say^"; //TODO: should be only once

    static public List<String> toInvoke(String dialogFileBase, String funName, Map<String, Object> fullInfo, String userId)
    {
        List<String> toSend = null;
        try
        {
            Package pack = FunctionInvoker.class.getPackage();
            Method method = Class.forName(pack.getName() + "." + dialogFileBase).getMethod(funName, Map.class, String.class);
            toSend = (List<String>) method.invoke(null, fullInfo, userId);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return toSend;
    }
}
