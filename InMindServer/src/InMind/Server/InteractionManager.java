package InMind.Server;

/**
 * Created by Tony on 17-Feb-15.
 */

public class InteractionManager implements IInteractionManager
{
    int vadCount = 0;


    @Override
    public ActionToTake updatedAudioInfo(double offsetFromFirst, double sampleLength, int vad, double finalPause)
    {
        vadCount += vad;
        if (finalPause > 500 && vadCount >=3)
            return ActionToTake.goToGoogle;
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
        vadCount = 0;
    }

    @Override
    public void gotGoogleResponse()
    {

    }
}
