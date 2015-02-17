package InMind.Server;

/**
 * Created by User on 17-Feb-15.
 */
public interface IInteractionManager
{
    enum actionToTake {none, goToGoogle, cancel, commit};

    /*
    main function. Is called every time new audio arrives.
    Response indicates whether the server should procceed (none),
    should go and aget Google ASR response (goToGoogle),
    should cancel all actions from previous start (cancel),
    or should commit and end current stream of audio (commit)
     */
    actionToTake updatedAudioInfo(int vad, int finalPause);

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
