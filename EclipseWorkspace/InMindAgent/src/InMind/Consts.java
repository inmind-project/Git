package InMind;

/**
 * Created by User on 24-Dec-14.
 */
public class Consts
{
    public static final int serverPort = 4493;
    static public final int sampleRate = 44100;

    static public final String commandChar = "^";
    static public final String messageSeparatorForPattern = "\\^";
    static public final String messagePattern = "(\\p{Alpha}*)"+Consts.messageSeparatorForPattern+"(.*)";

    //connection commands from server
    static public final String connectUdp = "ConnectUDP";
    static public final String stopUdp = "StopUDP";
    static public final String sayCommand = "Say";
    static public final String launchCommand = "Launch";
    static public final String startNewConnection = "StartNewConnection";
    static public final String closeConnection = "CloseConnection";

    //client requests
    static public final String requestSendAudio = "RequestSendAudio";
    static public final String sendingText = "SendingText";
}
