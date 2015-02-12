package InMind.DialogFunctions;

import InMind.Consts;
import InMind.ServerMessagingConsts;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Amos Azaria on 29-Dec-14.
 */
public class rapport
{
    static final String ip = "128.2.176.24";
    static final int port = 9096;
    public static List<String> forwardToRapport(Map<String, Object> fullInfo, String userId, String userText)
    {
        List<String> ret = new LinkedList<String>();

        if (userId == null)
            ret.add(FunctionInvoker.sayStr + "userId is required.");
        else
        {
            Socket socket = null;
            try
            {
                //send TCP message.
                InetAddress serverAddr = InetAddress.getByName(ip);
                System.out.println("Connecting to Rapport server...");
                socket = new Socket(serverAddr, port);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //send message to rapport server
                out.println(userId + Consts.commandChar + userText);

                //wait for "handling/notHandling" response
                //receive the message which the server sends back
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                if (response.equals(userId + ServerMessagingConsts.serverHandling))
                    ret.add(FunctionInvoker.sayStr + "Oh");
                else
                    ret.add(FunctionInvoker.sayStr + "Not Handling");
                socket.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (socket != null)
                        socket.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                ret.add(FunctionInvoker.sayStr + "Error with rapport server");
            }

        }
        return ret;
    }
}
