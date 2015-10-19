package InMind.DialogFunctions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by Amos Azaria on 08-Oct-15.
 */
public class dialogUtils
{
    static private final String USER_AGENT = "Mozilla/5.0";
    public static String callServer(String url, Map<String, String> parameters) throws Exception
    {
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
