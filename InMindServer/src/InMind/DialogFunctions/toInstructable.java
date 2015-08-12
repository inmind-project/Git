package InMind.DialogFunctions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Amos Azaria on 11-Aug-15.
 */
public class toInstructable
{
    static private final int portToUse = 18892;
    static private final String contextRealtimeAgent = "realtimeAgent";
    static private final String actionParam = "action";
    static private final String actionUserSays = "actionUserSays";
    static private final String actionResendRequested = "actionResendRequested";
    static private final String actionNewRealUser = "actionNewRealUser";
    static private final String userSaysParam = "userSays";
    static private final String userIdParam = "userId";
    static private final String usernameParm = "username";
    static private final String encPwd = "encPwd";
    static private final String emailParm = "email";
    static private final String realPwd = "realPwd";
    static public final String successContains = "successfully";

    public static List<String> toInstructable(Map<String, Object> fullInfo, String userId, String userText)
    {
        try
        {
            //split userId to username and password //would actually be more efficient to just loop through all characters...
            String username = IntStream.range(0, userId.length()).filter(i -> (i % 2) == 0).mapToObj(i -> (Character.toString(userId.charAt(i)))).collect(Collectors.joining(""));
            String enctyptionPwd = IntStream.range(0, userId.length()).filter(i -> (i % 2) == 1).mapToObj(i -> (Character.toString(userId.charAt(i)))).collect(Collectors.joining(""));

            if (!fullInfo.get("state").equals("has"))
            {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(actionParam, actionNewRealUser);
                parameters.put(userIdParam, username); //use username as userId for now
                parameters.put(usernameParm, username);
                parameters.put(encPwd, enctyptionPwd);
                String response = callServer(parameters);
                if (!response.contains(successContains))
                {
                    //check if already set email/password, if not ask for it
                    fullInfo.put("state", "getEmail");
                    return Arrays.asList(FunctionInvoker.sayStr+"I'm afraid that I don't have your email address yet.", FunctionInvoker.sayStr+"Please type in your email address above, and click send.");
                }
                else
                    fullInfo.put("state", "has"); //and move on
            }
            Map<String, String> parameters = new HashMap<>();
            parameters.put(actionParam, actionUserSays);
            parameters.put(userIdParam, username); //use username as userId for now
            parameters.put(usernameParm, username);
            parameters.put(encPwd, enctyptionPwd);
            parameters.put(userSaysParam, userText);
            String response = callServer(parameters);
            String[] res = response.split("\n");
            return Arrays.asList(res).stream().map(s -> FunctionInvoker.sayStr +s).collect(Collectors.toList());
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return Collections.singletonList(FunctionInvoker.sayStr+"Could not connect to instructable server.");
        }

    }

    public static List<String> saveEmailPassword(Map<String, Object> fullInfo, String userId, String userText)
    {
        try
        {
            //split userId to username and password //would actually be more efficient to just loop through all characters...
            String username = IntStream.range(0, userId.length()).filter(i -> (i % 2) == 0).mapToObj(i -> (Character.toString(userId.charAt(i)))).collect(Collectors.joining(""));
            String enctyptionPwd = IntStream.range(0, userId.length()).filter(i -> (i % 2) == 1).mapToObj(i -> (Character.toString(userId.charAt(i)))).collect(Collectors.joining(""));

            String email = fullInfo.get("email").toString();
            String password = fullInfo.get("password").toString();

            Map<String, String> parameters = new HashMap<>();
            parameters.put(actionParam, actionNewRealUser);
            parameters.put(userIdParam, username); //use username as userId for now
            parameters.put(usernameParm, username);
            parameters.put(encPwd, enctyptionPwd);
            parameters.put(emailParm, email);
            parameters.put(realPwd, password);
            String response = callServer(parameters);
            return Collections.singletonList(FunctionInvoker.sayStr+response);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return Collections.singletonList(FunctionInvoker.sayStr+"Could not connect to instructable server.");
        }
    }

    static private final String USER_AGENT = "Mozilla/5.0";

    private static String callServer(Map<String, String> parameters) throws Exception
    {
        String url = "http://localhost:" + portToUse + "/" + contextRealtimeAgent;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //using post
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        boolean firstParam = true;
        StringBuilder parms = new StringBuilder();
        for (String parm : parameters.keySet())
        {
            if (!firstParam)
                parms.append("&"); //if not first
            parms.append(parm + "=" + parameters.get(parm));
            firstParam = false;
        }

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(parms.toString());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode != 200)
        {
            System.out.println("S: error. (response code is: " + responseCode + ")");
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine + "\n");
        }
        in.close();

        return response.toString();

    }
}
