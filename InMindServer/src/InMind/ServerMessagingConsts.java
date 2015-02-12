package InMind;

/**
 * Created by Amos Azaria on 24-Dec-14.
 */
public class ServerMessagingConsts
{
    //messages between servers are in the format of userId^text or userId^command
    static public final String betweenServersMessagePattern = "([-a-zA-Z0-9]+)"+ Consts.messageSeparatorForPattern+"(.*)";
    //connection commands from remote server
    static public final String serverHandling = "handling"; //remote server is handling user
    static public final String serverNotHandling = "notHandling"; //remote server is not handling user
    static public final String serverReplying = "replying"; //remote server is now replying to user
}
