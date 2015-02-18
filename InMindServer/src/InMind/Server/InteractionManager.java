package InMind.Server;

/**
 * Created by Tony on 17-Feb-15.
 */

public class InteractionManager implements IInteractionManager
{
    int vadCount = 0;
    boolean sentCallToGoogle = false;
    int vadCountWhenSentCall = 0;
    boolean stoppedState = false;

    final int minimalVadRequired = 3;
    final int minimalFinalPause = 250;
    final int maximalFinalPause = 3000;


    @Override
    public ActionToTake updatedAudioInfo(double offsetFromFirst, double sampleLength, int vad, double finalPause)
    {
        vadCount += vad;
        if (stoppedState)
            return ActionToTake.commit;
        if (finalPause > maximalFinalPause && vadCount >= minimalVadRequired && sentCallToGoogle)
        {
            stoppedState = true;
            return ActionToTake.commit;
        }
        if (finalPause > minimalFinalPause && vadCount >= minimalVadRequired)
        {
            if (!sentCallToGoogle)
            {
                vadCountWhenSentCall = vadCount;
                sentCallToGoogle = true;
                return ActionToTake.goToGoogle;
            }
            else if (vadCount > vadCountWhenSentCall + minimalVadRequired)
            {
                vadCountWhenSentCall = vadCount;
                return ActionToTake.goToGoogle;
            }
        }
        return ActionToTake.none;
    }

    @Override
    public void start()
    {
        vadCount = 0;
    }

    @Override
    public void stop()
    {
        stoppedState = true;
    }

    @Override
    public void gotGoogleResponse()
    {

    }
}
