package InMind.Server;

/**
 * Created by User on 17-Feb-15.
 */
public interface IInteractionManager
{
    enum ActionToTake {none, goToGoogle, cancel, commit};

    /*
    main function. Is called every time new audio arrives.
    offsetFromFirst holds the offset in milliseconds from the beginning of first sample.
    sampleLength is the length of current sample in milliseconds.
    vad equals 1 if sound was detected in current sample and 0 if not.
    finalPause indicates the pause at the ending of current speech in milliseconds.
    Response indicates whether the server should proceed (none),
    should go and aget Google ASR response (goToGoogle),
    should cancel all actions from previous start (cancel),
    or should commit and end current stream of audio (commit)
     */
    ActionToTake updatedAudioInfo(double offsetFromFirst, double sampleLength, int vad, double finalPause);

    /*
    will be called before the first information arrives
     */
    void start();

    /*
    stop interaction management and restart it.
     */
    void stop();

    /*
    just a notifier that the Google ASR response has arrived.
     */
    void gotGoogleResponse();
}
