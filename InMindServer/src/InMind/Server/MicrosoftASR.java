package InMind.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by User on 31-Dec-14.
 */
public class MicrosoftASR
{
    public static native String fromByteArr(byte[] rawData);

    public static String callFromFile(Path sPath)
    {
        System.loadLibrary("ASRMicrosoftCpp");
        String decodedText = "";
        try
        {
            decodedText = MicrosoftASR.fromByteArr(Files.readAllBytes(sPath));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return decodedText;
    }
}
