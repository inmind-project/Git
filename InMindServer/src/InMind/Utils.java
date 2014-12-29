package InMind;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;

/**
 * Created by User on 25-Dec-14.
 */
public class Utils
{

    public static void appendToFile(byte soundbytes[], int soundlength, Path filePath)
    {
        FileOutputStream out;
        try
        {
            out = new FileOutputStream(filePath.toFile(), true);
            byte[] toWrite = new byte[soundlength];
            System.arraycopy(soundbytes, 0, toWrite, 0, soundlength);
            out.write(toWrite);
            out.close();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void delIfExists(Path filePath)
    {
        try
        {
            // Delete if tempFile exists
            File fileTemp = filePath.toFile();
            if (fileTemp.exists())
            {
                fileTemp.delete();
            }
        } catch (Exception e)
        {
            // if any error occurs
            e.printStackTrace();
        }
    }
}
