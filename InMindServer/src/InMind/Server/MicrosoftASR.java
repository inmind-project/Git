package InMind.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by User on 31-Dec-14.
 */
public class MicrosoftASR
{
    //Microsoft ASR works better when the amplitude is tuned.
    public static native String fromByteArr(byte[] rawData, double reduceFactor);

    public static String callFromFile(Path sPath)
    {
        final double defaultReduce = 0.1;
        System.loadLibrary("ASRMicrosoftCpp");
        String decodedText = "";
        try
        {
            decodedText = MicrosoftASR.fromByteArr(Files.readAllBytes(sPath),defaultReduce);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return decodedText;
    }
}
