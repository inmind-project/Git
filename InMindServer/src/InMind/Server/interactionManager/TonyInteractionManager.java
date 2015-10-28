//package InMind.Server.interactionManager;
//
///**
// * Created by Tony on 17-Feb-15.
// */
//
//public class TonyInteractionManager extends AInteractionManager
//{
//    // internal states
//    int iTurnID;
//    boolean bUserSpeaking;
//    boolean bSystemSpeaking;
//    boolean bInUtterance;
//    revokeStateType revokeState;
//    boolean bHasSeenNewSpeech;
//    boolean bHasResetTTS;
//    double activityDuration = 0;//Amos added
//
//    enum revokeStateType {standby, wait_for_hyp, hold, exit_wait, exit_hold};
//
//
//    // CONSTANT VALUES
//    final double dMaxUttDuration = 30000;
//    final double dMinUttDuration = 1000;
//    final double dActionThreshold = 300;
//    final double dListenThreshold = 1000;
//    final double dStopSpeakingThreshold = 150;
//    //Amos added
//    final double dMinVadDuration = 300;
//
//
//    public TonyInteractionManager(IIMRequiredAction imRequiredAction)
//    {
//        super(imRequiredAction);
//        initStates();
//    }
//
//    @Override
//    public void updatedAudioInfo(IMEvent eEvent)
//    {
//        // We  need to assume that event comes in asynchronously, therefore, we need to handle each event independently
//        // in any order
//        // event dependent processing
//        // if it is a VAD event
//        if (!(eEvent.type == IMEvent.IMEventType.ping))
//        {
//            if (eEvent.type == IMEvent.IMEventType.vad)
//            {
//                handleVADEvent(eEvent);
//            } else if (eEvent.type == IMEvent.IMEventType.asr) {
//                // if it is a ASR event
//                handleASREvent(eEvent);
//            } else {
//                System.out.println("IM: Unknown Type Event Received");
//                return;
//            }
//        }
//        // event independent processing
//        eventIndependentProcessing(eEvent);
//    }
//
//    @Override
//    public void start()
//    {
//        // begin a session
//        activityDuration = 0;
//    }
//
//    @Override
//    public void stop()
//    {
//        // This is called when we reset the IM for a new session (dialog) NOT for a new turn.
//        initStates();
//    }
//
//    @Override
//    public void finalResponseObtained(IMEvent eEvent)
//    {
//        System.out.println("Received final recognition hypothesis at state " + revokeState.name());
//
//
//        boolean bShouldSendToDM = (revokeState == revokeStateType.wait_for_hyp) || (revokeState == revokeStateType.exit_wait);
//
//        //update revoke.state
//        if (revokeState == revokeStateType.exit_hold  || revokeState == revokeStateType.exit_wait)
//        {
//            revokeState = revokeStateType.standby;
//        }  else if (revokeState == revokeStateType.wait_for_hyp) {
//            revokeState = revokeStateType.hold;
//        } else if (revokeState == revokeStateType.standby) {
//            System.out.println("Ignore final hypothesis at final hypothesis");
//        } else {
//            System.out.println("Unexpected final hypothesis at final ASR");
//        }
//
//        if (bShouldSendToDM)
//        {
//            // this response be sent to DM
//            bSystemSpeaking = true;
//            imRequiredAction.takeAction(ActionToTake.moveOn);
//        }
//    }
//    @Override
//    public void userStoppedStreaming()
//    {
//        System.out.println("Audio streaming time out detected. Stop listening");
//        if (revokeState == revokeStateType.standby) {
//            imRequiredAction.takeAction(ActionToTake.goToGoogle);
//        }
//
//        // update revokeState
//        if (revokeState == revokeStateType.standby || revokeState == revokeStateType.wait_for_hyp) {
//            revokeState = revokeStateType.exit_wait;
//        } else if(revokeState == revokeStateType.hold) {
//            revokeState = revokeStateType.exit_hold;
//        }
//        // update other states
//        bInUtterance = false;
//        bHasResetTTS = false;
//        bHasSeenNewSpeech = false;
//        bUserSpeaking = false;
//        imRequiredAction.takeAction(ActionToTake.commit);
//    }
//
//    /////////////////////////////////////////////
//    // private methods
//    /////////////////////////////////////////////
//    private void startNewTurn(IMEvent eEvent)
//    {
//        // this method is called when we started a new user utterance
//        iTurnID = iTurnID + 1;
//        bUserSpeaking = true;
//        bSystemSpeaking = false;
//        bInUtterance = true; //Amos changed from false to true
//        revokeState = revokeStateType.standby;
//        bHasSeenNewSpeech = false;
//        bHasResetTTS = false;
//    }
//
//
//    private void initStates()
//    {
//        // initialize the state variables
//        iTurnID = -1;
//        bUserSpeaking = false;
//        bSystemSpeaking = false;
//        bInUtterance = false;
//        revokeState = revokeStateType.standby;
//        bHasSeenNewSpeech = false;
//        bHasResetTTS = false;
//        activityDuration = 0;
//    }
//
//    private void handleVADEvent(IMEvent eEvent)
//    {
//        String vadValue = eEvent.feature.get(IMEvent.featureVad);
//        if (vadValue.equals("true"))
//        {
//            //System.out.println("User Started Speaking");
//            // Check if new speech occurred in the wait_for_hyp or hold
//            if  (revokeState == revokeStateType.wait_for_hyp || revokeState == revokeStateType.hold) {
//                bHasSeenNewSpeech = true;
//                System.out.println("VAD setting has_seen_new_speech to true");
//            }
//            // update user speaking states
//            bUserSpeaking = true;
//
//            activityDuration+=Double.parseDouble(eEvent.feature.get((IMEvent.featureDurationOfSample)));
//
//            // if not in utterance before, start a new utterance
//            if (!bInUtterance ) {
//                startNewTurn(eEvent);
//            }
//        }
//    }
//
//    private void handleASREvent(IMEvent eEvent)
//    {
//        double curFinalPauseDuration = Double.valueOf(eEvent.feature.get(IMEvent.featureFinalPause));
//        //partial ASR
//        //System.out.println("Received partial recognition hypothesis at state " + revokeState.name());
//
//        // update user speaking info and revoke.state
//        if (bUserSpeaking)
//        {
//            if (curFinalPauseDuration > dStopSpeakingThreshold)
//            {
//                //System.out.println("User stopped speaking");
//                bUserSpeaking = false;
//            }
//        }
//
//    }
//    private void eventIndependentProcessing(IMEvent eEvent)
//    {
//        double curDurationFromBegin = Double.valueOf(eEvent.feature.get(IMEvent.featureDurationFromBeginning));
//        double curFinalPauseDuration = Double.valueOf(eEvent.feature.get(IMEvent.featureFinalPause));
//
//        if (bInUtterance)
//        {
//            // check if utterance lasted for too long
//            if (curDurationFromBegin > dMaxUttDuration)
//            {
//                //System.out.println("User utterance reached maximum duration. Stop listening");
//                if (revokeState == revokeStateType.exit_wait || revokeState == revokeStateType.exit_hold) {
//                    System.out.println("This should never happen");
//                    return;
//                }
//
//                if (revokeState == revokeStateType.standby) {
//                    imRequiredAction.takeAction(ActionToTake.goToGoogle);
//                }
//
//                // update revokeState
//                if (revokeState == revokeStateType.standby || revokeState == revokeStateType.wait_for_hyp) {
//                    revokeState = revokeStateType.exit_wait;
//                } else if(revokeState == revokeStateType.hold) {
//                    revokeState = revokeStateType.exit_hold;
//                }
//                // update other states
//                bInUtterance = false;
//                bHasResetTTS = false;
//                bHasSeenNewSpeech = false;
//                bUserSpeaking = false;
//
//                imRequiredAction.takeAction(ActionToTake.commit);
//                return;
//            }
//
//            // check if pause is long enough for action threshold/listen threshold
//            if (!bUserSpeaking && curDurationFromBegin > dMinUttDuration && activityDuration >= dMinVadDuration)
//            {
//                // check action threshold
//                if (curFinalPauseDuration > dActionThreshold
//                        && revokeState == revokeStateType.standby)
//                {
//                    //System.out.println("End of utterance for action detected");
//                    //update internal states
//                    bHasSeenNewSpeech = false;
//                    revokeState = revokeStateType.wait_for_hyp;
//                    imRequiredAction.takeAction(ActionToTake.goToGoogle);
//                }
//
//                // check listen threshold
//                if (curFinalPauseDuration > dListenThreshold &&
//                        (revokeState == revokeStateType.wait_for_hyp || revokeState == revokeStateType.hold))
//                {
//                    //System.out.println("End of utterance for listen detected");
//                    bInUtterance = false;
//                    bHasSeenNewSpeech = false;
//
//                    // if we are in hold state, set to exit_hold
//                    if (revokeState == revokeStateType.hold) {
//                        revokeState = revokeStateType.exit_hold;
//                    } else if (revokeState == revokeStateType.wait_for_hyp) {
//                        revokeState = revokeStateType.exit_wait;
//                    } else {
//                        System.out.println("Unexpected revokeState at listen threshold");
//                    }
//                    // Commit: stop audio streaming
//                    imRequiredAction.takeAction(ActionToTake.commit);
//                }
//            }
//            // At last check if we need to send cancel TTS
//            boolean shouldRevoke = (bHasSeenNewSpeech);//Amos removed: && bSystemSpeaking);
//            if (shouldRevoke &&
//                    (revokeState == revokeStateType.wait_for_hyp || revokeState == revokeStateType.hold))
//            {
//                //System.out.println("New speech revoked the end of utterance");
//                if (revokeState == revokeStateType.hold)
//                {
//                    bHasResetTTS = true;
//                }
//                revokeState = revokeStateType.standby;
//                imRequiredAction.takeAction(ActionToTake.cancel);
//            }
//        }
//    }
//
//}