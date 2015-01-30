package InMind.Server;

import InMind.Consts;

import javax.sound.sampled.SourceDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static InMind.Utils.delIfExists;
import static InMind.Utils.appendToFile;

// This class is in-charge of receiving the audio stream from the user (via UDP).
class StreamAudioServer
{

    //AudioInputStream audioInputStream;
    // static AudioInputStream ais;
    // static AudioFormat format;
    //static boolean status = true;
    //static int port = 50005;
    //static int sampleRate = 44100;
    static final Path folderPath = Paths.get("..\\UserData");//c:\\Server\\Git\\UserData");//TODO: fix to relative Paths.get("..\\..\\..\\..\\..\\..\\UserData");
    static final String fileStart = "InputAt";
    static final int timeout = 1000; //in milliseconds
    static final long maxRecordingTimeLength = 60*1000; //in milliseconds

    //static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;
    Path filePath = null;

    StreamingAlerts streamingAlerts = null;

    public interface StreamingAlerts
    {
        void audioArrived(byte[] audio);
        void audioEnded();
    }

    public StreamAudioServer(StreamingAlerts streamingAlerts )
    {
        this.streamingAlerts = streamingAlerts;
    }

    public Path runServer(int udpPort)
    {
        Path retFileWithRaw = null;

        silentSampleLength = 0;
        talkSampleLength = 0;
        filePath = null;

        try
        {

            filePath = Paths.get(folderPath.toString(), fileStart + (new SimpleDateFormat("ddMMyy-hhmmss.SSS").format(new Date())) + ".raw");

            delIfExists(filePath);

            DatagramSocket serverSocket = new DatagramSocket(udpPort);

            serverSocket.setSoTimeout(timeout);

            /**
             * Formula for lag = (byte_size/sample_rate)*2 Byte size 9728 will
             * produce ~ 0.45 seconds of lag. Voice slightly broken. Byte size 1400
             * will produce ~ 0.06 seconds of lag. Voice extremely broken. Byte size
             * 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken
             * than 9728.
             */

            byte[] receiveData = new byte[4096];

            // format = new AudioFormat(sampleRate, 16, 1, true, false);
            // dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            // sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            // sourceDataLine.open(format);
            // sourceDataLine.start();

            // FloatControl volumeControl = (FloatControl)
            // sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            // volumeControl.setValue(1.00f);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // ByteArrayInputStream baiss = new
            // ByteArrayInputStream(receivePacket.getData());

            System.out.println("receiving information");

            long expiringTime = System.currentTimeMillis() + maxRecordingTimeLength;
            while (System.currentTimeMillis() < expiringTime)
            {
                //System.out.println("Waiting!");
                try
                {
                    serverSocket.receive(receivePacket);
                } catch (SocketTimeoutException ex)
                {
                    System.out.println("Time out!");
                    break;
                }

                if (streamingAlerts != null)
                    streamingAlerts.audioArrived(receivePacket.getData());

                //System.out.println("Received Packet!" + receivePacket.getLength());
                appendToFile(receivePacket.getData(), receivePacket.getLength(), filePath);

                if (isSilentButDidTalk(receivePacket.getData()))
                {
                    break;
                }
                // ais = new AudioInputStream(baiss, format,
                // receivePacket.getLength());
                // toSpeaker(receivePacket.getData());
            }
            if (streamingAlerts != null)
                streamingAlerts.audioEnded();
            System.out.println("receive complete!");
            //sourceDataLine.drain();
            //sourceDataLine.close();
            serverSocket.close();

            retFileWithRaw = filePath;

        } catch (Exception ex)
        {
            retFileWithRaw = null;
            System.out.println("StreamAudio: Error");
            ex.printStackTrace();
        }
        return retFileWithRaw;
    }

    int silentSampleLength = 0;
    int talkSampleLength = 0;

    private boolean isSilentButDidTalk(byte[] asByte)
    {
        final int silentLengthNeeded = 500;  //in milliseconds
        final int considerSilent = 1500;  //TODO: may want to use mean squared error or other smart approaches.
        final int considerSpeech = 3000;
        final int minimalTalk = Consts.sampleRate / 1000; //require at least 0.001 sec of speech

        try
        {
            //byte[] asByte = Files.readAllBytes(filePath);
            //short[] asShort = new short[asByte.length/2];
            for (int i = 0; 2*i < asByte.length; i++)
            {
                short sample = (short) (asByte[2*i+1] << 8 | asByte[2*i]); //little endian 16bit
                if (Math.abs(sample) < considerSilent)
                    silentSampleLength++;
                else
                {
                    silentSampleLength = 0;
                    if (Math.abs(sample) > considerSpeech)
                    {
                        talkSampleLength++;
                    }
                }
            }
            double silentLength = silentSampleLength/(double) Consts.sampleRate;
            if (silentLength*1000 > silentLengthNeeded && talkSampleLength > minimalTalk)
                return true;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;

    }

//    public static void toSpeaker(byte soundbytes[], int soundlength)
//    {
//        try
//        {
//            sourceDataLine.write(soundbytes, 0, soundlength);
//        } catch (Exception e)
//        {
//            System.out.println("Not working in speakers...");
//            e.printStackTrace();
//        }
//    }

}