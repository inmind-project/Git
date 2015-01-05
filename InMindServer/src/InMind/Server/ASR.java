package InMind.Server;

import InMind.Consts;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Amos Azaria on 16-Dec-14.
 */
public class ASR
{


    static public class AsrRes
    {
        public String text;
        public double confidence;
    }

    ;

    /**
     * Send post to google
     */
    static public AsrRes getGoogleASR(Path audioFile)
    {
        AsrRes res = new AsrRes();
        res.confidence = 0;
        res.text = "";
        try
        {
            String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2",
                    url = "https://www.google.com/speech-api/v2/recognize?output=json&lang=en-us&key=AIzaSyChZTv4KdGD56Uuh7uMBXy-YEdaAsSBmpw&client=chromium&maxresults=6&pfilter=2";

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            // add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "audio/l16; rate="+ Consts.sampleRate);
            //con.setRequestProperty("AcceptEncoding", "");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(Files.readAllBytes(audioFile));
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Google response: " + response.toString());
            PrintWriter pw = new PrintWriter(audioFile.toString() + ".txt");
            pw.print(response.toString());
            pw.flush();
            pw.close();
            //remove first empty result (13 chars): {"result":[]}
            JSONObject jsonObj = new JSONObject(response.toString().substring(13));
            JSONObject bestRes = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("alternative").getJSONObject(0);
            res.text = bestRes.getString("transcript");
            res.confidence = bestRes.getDouble("confidence");

            // print result
            System.out.println(response.toString());
        } catch (Exception Ex)
        {
        }

        return res;

    }
}
