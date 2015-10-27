package InMind.DialogFunctions;

import InMind.Server.UserConversation;
import InMind.Server.asr.ASR;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;


/**
 * Created by Amos Azaria on 29-Dec-14.
 */
public class crowdRule
{

    static class SayOrJSon
    {
        enum SayOrJSonType {say, execJson};

        public SayOrJSon(SayOrJSonType sayOrJSonType, String content)
        {
            this.sayOrJSonType = sayOrJSonType;
            this.content = content;
        }

        SayOrJSonType sayOrJSonType;
        public String content;
    }
    static final String ip = "localhost";//"45.55.172.104";//"localhost";//"45.55.172.104";
    static final int crowdPort = 1606;
    static final int listenPort = 1607;
    static final long maxWaitForCrowd = 10*60*1000;
    static HttpServer server;
    static Map<String, Object> notifiers = new HashMap<>();
    static Map<String, SayOrJSon> contents = new HashMap<>();

    static
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(listenPort), 0);
            HttpContext httpContext = server.createContext("/crowdListener", new HttpHandler()
            {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException
                {
                    try
                    {
                        Map<String,Object> parameters = dialogUtils.bodyAsParms(httpExchange);
                        //Map<String, Object> parameters = (Map<String, Object>) httpExchange.getAttribute(ParameterFilter.parametersStr);
                        String response;
                        if (!parameters.containsKey("userId"))
                            response = "Error! no 'userId' found";
                        else if (!parameters.containsKey("messageType"))
                            response = "Error! no 'messageType' found";
                        else if (!parameters.containsKey("content"))
                            response = "Error! no 'content' found";
                        else if (!parameters.get("messageType").equals("say") && !parameters.get("messageType").equals("execRule"))
                            response = "Error! 'messageType' must be 'say' or 'execRule'";
                        else
                        {
                            String userId = (String) parameters.get("userId");
                            contents.put(userId, new SayOrJSon(parameters.get("messageType").equals("say") ? SayOrJSon.SayOrJSonType.say : SayOrJSon.SayOrJSonType.execJson, (String) parameters.get("content")));
                            if (notifiers.containsKey(userId))
                            {
                                synchronized (notifiers.get(userId))
                                {
                                    notifiers.get(userId).notify();
                                }
                                response = "ok";
                            }
                            else
                                response = "error! notifier not found";
                        }
                        httpExchange.sendResponseHeaders(200, response.length());
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
            //httpContext.getFilters().add(new ParameterFilter());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static List<String> initiateCrowd(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        List<String> ret = new LinkedList<String>();

        try
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("userId", userId);
            parameters.put("messageType", "initiate");
            String response = dialogUtils.callServer("http://" + ip + ":" + crowdPort + "/", parameters, true);
            if (response.contains("ok"))
                return Collections.singletonList(FunctionInvoker.sayStr + "Great! Go ahead!");//response);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        fullInfo.put(UserConversation.stateName,UserConversation.dialogFinish);
        return Collections.singletonList(FunctionInvoker.sayStr+"Could not initialize crowd rule server.");
    }

    public static List<String> forwardToCrowdRule(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        List<String> ret = new LinkedList<String>();

        try
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("userId", userId);
            parameters.put("messageType", "userSays");
            parameters.put("userText", userText.text);
            String callResponse = dialogUtils.callServer("http://" + ip + ":" + crowdPort + "/", parameters, true);
            String responseForUser = FunctionInvoker.sayStr + "Sorry, but there seems to be a problem...";
            if (callResponse.contains("ok"))
            {
                //if response is ok
                //create a listener:
                //listens on userId so should work also with multiple users.
                if (!notifiers.containsKey(userId))
                    notifiers.put(userId, new Object());
                synchronized (notifiers.get(userId))
                {
                    notifiers.get(userId).wait(maxWaitForCrowd);
                }
                if (contents.containsKey(userId))
                {
                    SayOrJSon sayOrJSon = contents.get(userId);
                    if (sayOrJSon.sayOrJSonType == SayOrJSon.SayOrJSonType.say)
                    {
                        responseForUser = FunctionInvoker.sayStr + sayOrJSon.content;//FunctionInvoker.execJson + contents.get(userId);
                    }
                    else
                        responseForUser = FunctionInvoker.execJson + sayOrJSon.content;

                    contents.remove(userId);
                }
                else
                {
                    responseForUser = FunctionInvoker.sayStr + "Sorry, but I got no answer...";
                }
            }
            return Collections.singletonList(responseForUser);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return Collections.singletonList(FunctionInvoker.sayStr+"Connection with crowd rule server was lost.");
        }
    }
}
