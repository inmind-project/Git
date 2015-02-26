package InMind.Server;

import javax.sound.sampled.SourceDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

// This class is in-charge of receiving the audio stream from the user (via UDP).
class AudioReceiver
{

    static final int timeout = 1000; //in milliseconds
    static final long maxRecordingTimeLength = 60 * 1000; //in milliseconds
    static public final int udpBufferSize = 4096;


    static SourceDataLine sourceDataLine;

    StreamingAlerts streamingAlerts = null;
    boolean receivedAudioAlready = false;
    boolean shouldWaitForAudio = true;


    public interface StreamingAlerts
    {
        //void rawFilePath(Path filePathForSavingAudio);

        void firstAudioArriving(); //called when first audio is arriving (will be followed by audioArrived.

        void audioArrived(byte[] audioReceived, int length); //audio received ready for decoding.

        void stoppedListeningForAudio(); //audio stopped, no additional changes will be made.

        void timedOut();//connection timed out.
        //void cancelAndRestartAudio(); //cancel call, will be followed by audioArrived, with all audio from beginning.

        //void tryMovingOn();//will be called when audio is assumed to end (call Google ASR)
    }

    public AudioReceiver(StreamingAlerts streamingAlerts)
    {
        this.streamingAlerts = streamingAlerts;
    }

    public void closeConnection()
    {
        shouldWaitForAudio = false;
    }

    public void runServer(int udpPort)
    {


        try
        {

            //streamingAlerts.rawFilePath(filePath);


            DatagramSocket serverSocket = new DatagramSocket(udpPort);

            serverSocket.setSoTimeout(timeout);

            /**
             * Formula for lag = (byte_size/sample_rate)*2 Byte size 9728 will
             * produce ~ 0.45 seconds of lag. Voice slightly broken. Byte size 1400
             * will produce ~ 0.06 seconds of lag. Voice extremely broken. Byte size
             * 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken
             * than 9728.
             */

            byte[] receiveData = new byte[udpBufferSize];



            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);


            System.out.println("receiving information");

            long expiringTime = System.currentTimeMillis() + maxRecordingTimeLength;


            while (System.currentTimeMillis() < expiringTime && shouldWaitForAudio)
            {
                //System.out.println("Waiting!");
                try
                {
                    serverSocket.receive(receivePacket);
                } catch (SocketTimeoutException ex)
                {
                    System.out.println("Time out!");
                    streamingAlerts.timedOut();
                    break;
                }

                byte[] audioReceived = receivePacket.getData();

                if (!receivedAudioAlready)
                {
                    streamingAlerts.firstAudioArriving();
                    receivedAudioAlready = true;
                }

                streamingAlerts.audioArrived(audioReceived, receivePacket.getLength()); //TODO: should be async

            }
            if (streamingAlerts != null)
                streamingAlerts.stoppedListeningForAudio();
            System.out.println("receive complete!");

            serverSocket.close();


        } catch (Exception ex)
        {
            System.out.println("StreamAudio: Error");
            ex.printStackTrace();
        }
        return;
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