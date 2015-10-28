package InMind.Server.interactionManager;

import InMind.Server.SignalInfo.SimpleSignalInfoProvider;

/**
 * Created by Amos Azaria on 17-Feb-15.
 */

public abstract class AInteractionManager implements IInteractionManager
{
    /*
        Response indicates whether the server should proceed (none),
        should go and get Google ASR response (goToGoogle),
        moveOn means the current Google message should be passed on to DM (moveOn)
        should cancel all actions from previous start (cancel),
        or should commit and end current stream of audio (commit)
 */
    public enum ActionToTake {none, goToGoogle, moveOn, cancel, commit};

    public interface IIMRequiredAction
    {
        void takeAction(ActionToTake actionToTake);
    }
    //callback interface
    protected IIMRequiredAction imRequiredAction;
    AInteractionManager(IIMRequiredAction imRequiredAction)
    {
        this.imRequiredAction = imRequiredAction;
    }
}

interface IInteractionManager
{
    /*
    main function. Is called every time new audio arrives.
    offsetFromFirst holds the offset in milliseconds from the beginning of first sample.
    sampleLength is the length of current sample in milliseconds.
    vad equals 1 if sound was detected in current sample and 0 if not.
    finalPause indicates the pause at the ending of current speech in milliseconds.
     */

    // there will be 3 types of events
    // 1. ping. a constant ping signal that wakes up the IM at high frequency (e.g 50ms)
    // 2. VAD. a VAD event contains the the output from VAD
    // 3. ASR. a partial ASR message from the pocket sphinx
    //void updatedAudioInfo(IMEvent eEvent);
    void updatedAudioInfo(SimpleSignalInfoProvider.SignalInfo signalInfo);

    /*
    will be called before the first information arrives
     */
    void start();

    /*
    stop interaction management and restart it. maybe called RESET instead?
     */
    void stop();

    /*
        Give the final response (Google) to IM after google request. The function returns
        a boolean that indicate if this final response should be used.
    */
    void finalResponseObtained();//IMEvent eEvent);

    /*
        User stopped streaming over the audio
     */
    void userStoppedStreaming();
}
