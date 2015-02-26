package InMind.Server;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static InMind.Utils.appendToFile;
import static InMind.Utils.delIfExists;

/**
 * Created by Amos Azaria on 17-Feb-15.
 * This class is in-charge of everything to do with the audio input and obtaining the text from ASR.
 * This class also saves to file.
 */
public class AudioTopDirector
{
    IInteractionManager interactionManager;
    SignalInfoProvider signalInfoProvider;
    IControllingOrders controllingOrders;
    int udpPort;
    ByteArrayBuffer allAudioFromBeginning;

    static final Path folderPath = Paths.get("..\\UserData");
    static final String fileStart = "InputAt";
    Path filePath;
    ASR asr;
    AudioReceiver streamAudioServer;

    interface IControllingOrders
    {
        void dealWithAsrRes(ASR.AsrRes asrRes);
        void cancelAllAction();
        void closeAudioConnection();
    }

    AudioTopDirector(int udpPort, IControllingOrders controllingOrders)
    {
        allAudioFromBeginning = new ByteArrayBuffer();
        this.controllingOrders = controllingOrders;
        this.interactionManager = new InteractionManager(new IMCommandExecutor());
        signalInfoProvider = new SignalInfoProvider();
        this.udpPort = udpPort;
    }


    private class IMCommandExecutor implements AInteractionManager.IIMRequiredAction
    {
        boolean hasValidGoogleCall = false;
        int validGoogleCallId = 0;

        @Override
        public void takeAction(IInteractionManager.ActionToTake actionToTake)
        {
            try
            {

            switch (actionToTake)
            {
                case none:
                    break;
                case commit:
                    streamAudioServer.closeConnection();
                    //controllingOrders.closeAudioConnection();
                    break;
                case goToGoogle:
                    invalidateOldCallIfExistsAndStreamNew();
                    getAsrResAsync();
                    break;
                case cancel:
                    invalidateOldCallIfExistsAndStreamNew();
                    break;
                case moveOn:
                    break;

            }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }

        private void dealWithAsrResponse(ASR.AsrRes asrRes, int myGoogleCallId)
        {
            if (filePath != null) //write json response text file
            {
                PrintWriter pw = null;
                try
                {
                    pw = new PrintWriter(filePath.toString() + ".txt");
                    pw.print(asrRes.fullJsonRes);
                    pw.flush();
                    pw.close();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println(asrRes.text);
            boolean wasNotCanceled = myGoogleCallId == validGoogleCallId;
            if (wasNotCanceled)
            {
                interactionManager.stop();
                streamAudioServer.closeConnection();
                //controllingOrders.closeAudioConnection();
                controllingOrders.dealWithAsrRes(asrRes);
            }
        }

        private void invalidateOldCallIfExistsAndStreamNew()
        {
            validGoogleCallId++;
            if (hasValidGoogleCall)
            {
                hasValidGoogleCall = false;
                asr = new ASR();
                try
                {
                    asr.beginTransmission();
                    if (asr.isConnectionOpen())
                        asr.sendDataAsync(allAudioFromBeginning.getRawData());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


        private void getAsrResAsync() throws IOException
        {
            if (asr.isConnectionOpen())
            {
                hasValidGoogleCall = true;
                asr.closeAndGetResponseAsync(new ASR.IAsrGetResponse()
                {
                    int myGoogleCallId = validGoogleCallId;

                    @Override
                    public void gotResponse(ASR.AsrRes asrRes)
                    {
                        dealWithAsrResponse(asrRes, myGoogleCallId);
                    }
                });
            }

        }
    }

    private IMCommandExecutor imCommandExecutor = new IMCommandExecutor();

    void runServer()
    {
        interactionManager.start();
        asr = new ASR();
        filePath = Paths.get(folderPath.toString(), fileStart + (new SimpleDateFormat("ddMMyy-hhmmss.SSS").format(new Date())) + ".raw");
        delIfExists(filePath);

        streamAudioServer = new AudioReceiver(new AudioReceiver.StreamingAlerts()
        {
            @Override
            public void firstAudioArriving()
            {
                try
                {
                    asr.beginTransmission();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }


            @Override
            public void audioArrived(byte[] audioReceived, int length)
            {
                try
                {
                    appendToFile(audioReceived, length, filePath);
                    allAudioFromBeginning.write(audioReceived, 0, length);
                    if (asr.isConnectionOpen())
                        asr.sendDataAsync(audioReceived);
                    SignalInfoProvider.SignalInfo signalInfo = signalInfoProvider.obtainSampleInfo(audioReceived, length);
                    IMEvent imEventVad = new IMEvent(IMEvent.IMEventType.vad);
                    imEventVad.feature.put(IMEvent.featureVad, (signalInfo.vad == 1) ? "true":"false");
                    imEventVad.feature.put(IMEvent.featureFinalPause, ((Double)signalInfo.finalPause).toString());
                    imEventVad.feature.put(IMEvent.featureDuration, ((Double)signalInfo.sampleLength).toString());
                    interactionManager.updatedAudioInfo(imEventVad);//signalInfo.offSetFromFirst, signalInfo.sampleLength, signalInfo.vad, signalInfo.finalPause);

                } catch (Exception e)
                {
                    interactionManager.stop();
                    e.printStackTrace();
                }
            }


            @Override
            public void stoppedListeningForAudio()
            {
                try
                {
                    //if (asr.isConnectionOpen())
                    //{
                        controllingOrders.closeAudioConnection();
                    //}
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void timedOut()
            {
                    //if (!hasValidGoogleCall)
                        //getAsrResAsync();
                    interactionManager.userStoppedStreaming();
            }
        });


        streamAudioServer.runServer(udpPort);
    }
}
