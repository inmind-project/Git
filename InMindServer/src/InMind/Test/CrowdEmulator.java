package InMind.Test;

import InMind.DialogFunctions.dialogUtils;
import InMind.ParameterFilter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amos Azaria on 16-Oct-15.
 */
public class CrowdEmulator
{
    static String jsonString =
            "{ \"conditions\": [ { \"proposition\": { \"attribute\":\"ACCELEROMETER_VECTOR_SUM\", \"componentName\": \"ACCELEROMETER\", \"operator\": \"OPERATOR_LOWER_THAN\", \"value\": \"2.0\" }, \"term\": \"x\" } ], \"actions\": [ { \"attributes\": { \"ALARM_REFERENCE_TIME\": \"ALARM_TIME_NOW\", \"ALARM_CONDITION_AT\": true, \"ALARM_RINGTONE_TYPE\": 2 }, \"componentName\": \"ALARM\" }, { \"attributes\": { \"TOAST_MESSAGE\": \"Cell phone is falling towards the ground\" }, \"componentName\": \"TOAST\" } ] }";

    static public void main(String args[]) throws Exception
    {

        HttpServer server = HttpServer.create(new InetSocketAddress(1606), 0);
        HttpContext httpContext = server.createContext("/", new com.sun.net.httpserver.HttpHandler()
        {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException
            {
                Map<String, Object> parameters = (Map<String, Object>) httpExchange.getAttribute(ParameterFilter.parametersStr);
                String response = "ok";
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                System.out.println("received call, messageType=" + parameters.get("messageType"));
                if (parameters.get("messageType").equals("userSays"))
                {
                    String userText = (String)parameters.get("userText");
                    System.out.println("userSays=" + userText);
                    try
                    {
                        Map<String, String> retParameters = new HashMap<>();
                        if (userText.equalsIgnoreCase("yes"))
                        {
                            retParameters.put("messageType", "execRule");
                            retParameters.put("content", jsonString);
                        }
                        else
                        {
                            retParameters.put("messageType", "say");
                            retParameters.put("content", "Is that because that you keep dropping it?");
                        }
                        retParameters.put("userId", (String)parameters.get("userId"));
                        dialogUtils.callServer("http://localhost:" + 1607 + "/crowdListener", retParameters);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });
        httpContext.getFilters().add(new ParameterFilter());
        server.start();
    }
}
