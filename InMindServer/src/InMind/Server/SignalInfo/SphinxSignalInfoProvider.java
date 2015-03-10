package InMind.Server.SignalInfo;

import InMind.Consts;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import javax.sound.sampled.AudioFormat;

/**
 * Created by Amos Azaria on 09-Mar-15.
 */
public class SphinxSignalInfoProvider extends SimpleSignalInfoProvider
{
    static boolean isInitialized = false;
    static boolean startedInitializing = false;

    static ConfigurationManager cm;
    static Recognizer recognizer;
    static InMindByteDataSource inMindByteDataSource;
    static InMindDataProcessor inMindDataProcessor;
    static Thread recognizerThread;

    // must be called on the class to initialize sphinx
    static synchronized public void staticInitialize()
    {
        if (!isInitialized)
        {
            if (!startedInitializing)
            {
                startedInitializing = true;

                cm = new ConfigurationManager(SphinxSignalInfoProvider.class.getResource("hellongram.config.xml"));

                // allocate the recognizer
                System.out.println("Loading spinx recognizer...");
                recognizer = (Recognizer) cm.lookup("recognizer");
                recognizer.allocate();

                //speechClassifier = (SpeechClassifier) cm.lookup("speechClassifier");
                //speechClassifier.initialize();

                inMindDataProcessor = (InMindDataProcessor) cm.lookup("inMindDataProcessor");

                inMindByteDataSource = (InMindByteDataSource) cm.lookup("inMindByteDataSource");


                isInitialized = true;
                System.out.println("Spinx recognizer loaded.");

                recognizerThread = new Thread(() -> {
                    try
                    {
                        while (true)
                        {
                            recognizer.recognize();
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
                recognizerThread.start();
            }
        }
    }


    @Override
    protected void updateTalkAndSilent(byte[] currentSample, int currLength)
    {
        if (!isInitialized)
        {
            System.out.println("Error, call staticInitialize first!");
            staticInitialize();
        }


        boolean isSpeech = testIfSpeech(currentSample, currLength);
        System.out.println("has speech: " + isSpeech);
        if (!isSpeech)
        {
            bytesSilentAtEnd += currLength;
            bytesTalkAtCurrentSample = 0;
        }
        else
        {
            bytesSilentAtEnd = 0;
            bytesTotalTalkLength += currLength;
            bytesTalkAtCurrentSample = currLength;
        }
    }

    @Override
    public void startNewStream()
    {
        super.startNewStream();
        inMindByteDataSource.startNewStream(new AudioFormat((float) Consts.sampleRate, Consts.sampleSizeBits, Consts.channels, Consts.isSigned, Consts.bigEndian));
    }

    @Override
    public void endStream()
    {
        inMindByteDataSource.endStream();
    }

    private boolean testIfSpeech(byte[] currentSample, int currLength)
    {

        final int[] speechCount = {0};
        final int[] totalSamples = {0};

        inMindDataProcessor.giveInterface(new InMindDataProcessor.InMindDataProcessorInterface()
        {
            @Override
            public void hasSpeech(boolean hasSpeech)
            {
                if (hasSpeech)
                    speechCount[0]++;
                totalSamples[0]++;
            }
        });

        final Object lock = new Object();


        inMindByteDataSource.giveInterface(new InMindByteDataSource.NotificationsFromByteSource()
        {
            @Override
            public void waitingForMoreData()
            {
                synchronized (lock)
                {
                    lock.notify();
                }
            }
        });

        inMindByteDataSource.appendNewData(currentSample,currLength);

        try
        {
            synchronized (lock)
            {
                lock.wait();
            }
        } catch (InterruptedException e)
        {
            //e.printStackTrace();
        }

        System.out.println("talkSamples: " + speechCount[0] );
        System.out.println("totalSamples: " + totalSamples[0] );

        if (speechCount[0] >=  totalSamples[0] / 2) //don't return fraction, if there is at least half speech, all counts as speech.
            return true;
        return false;
    }
}
