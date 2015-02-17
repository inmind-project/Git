package InMind.Server;

import InMind.Consts;

/**
 * Created by Amos on 17-Feb-15.
 *
 * should be replaced by pocket-sphinx
 */
public class SignalInfoProvider
{
    int bytesSilentAtEnd = 0;
    int bytesTalkAtCurrentSample = 0;
    int bytesTotalTalkLength = 0;


    final int silentLengthNeeded = 500;  //in milliseconds
    final int considerSilent = 1500;  //TODO: may want to use mean squared error or other smart approaches.
    final int considerSpeech = 2200;
    final int minimalTalk = Consts.sampleRate / 5000; //require at least 0.001 sec of speech


    private double convertToMilliSeconds(int bytesOfSomething)
    {
        return bytesOfSomething*1000.0 / Consts.sampleRate;
    }

    class SignalInfo
    {
        public int vad; //was their noise in this sample
        public int finalPause; //in milliseconds
    }


    public SignalInfo obtainSampleInfo(byte[] asByte)
    {
        SignalInfo signalInfo = new SignalInfo();
        updateTalkAndSilent(asByte);

        signalInfo.finalPause = (int)convertToMilliSeconds(bytesSilentAtEnd);
        signalInfo.vad = (bytesTalkAtCurrentSample > minimalTalk) ? 1 : 0;
        return signalInfo;
    }

    private void updateTalkAndSilent(byte[] asByte)
    {
        bytesTalkAtCurrentSample = 0;
        for (int i = 0; 2 * i < asByte.length; i++)
        {
            short sample = (short) (asByte[2 * i + 1] << 8 | asByte[2 * i]); //little endian 16bit
            if (Math.abs(sample) < considerSilent)
                bytesSilentAtEnd++;
            else
            {
                bytesSilentAtEnd = 0;
                if (Math.abs(sample) > considerSpeech)
                {
                    bytesTotalTalkLength++;
                    bytesTalkAtCurrentSample++;
                }
            }
        }

    }

    public boolean isSilentButDidTalk(byte[] asByte)
    {
        try
        {
            updateTalkAndSilent(asByte);
            double silentLength = bytesSilentAtEnd / (double) Consts.sampleRate;
            if (silentLength * 1000 > silentLengthNeeded && bytesTotalTalkLength > minimalTalk)
                return true;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;

    }
}
