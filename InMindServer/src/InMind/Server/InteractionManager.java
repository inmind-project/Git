package InMind.Server;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Tony on 17-Feb-15.
 */

public class InteractionManager extends AInteractionManager
{
    // internal states
    Map<String,String> s2sState;


    // CONSTANT VALUES
    final int iMaxUttDuration = 30000;
    final int iMinUttDuration = 1000;
    final int iActionThreshold = 300;
    final int iListenThreshold = 1000;

    InteractionManager(IIMRequiredAction imRequiredAction)
    {
        super(imRequiredAction);
        s2sState = new HashMap<String, String>(8);
        initStates();
    }

    @Override
    public void updatedAudioInfo(IMEvent eEvent)
    {
        // We  need to assume that event comes in asynchronously, therefore, we need to handle each event independently
        // in any order
        // event dependent processing
        // if it is a VAD event
        if (!(eEvent.type == IMEvent.IMEventType.ping))
        {
            if (eEvent.type == IMEvent.IMEventType.vad)
            {
                handleVADEvent(eEvent);
            } else if (eEvent.type == IMEvent.IMEventType.asr) {
                // if it is a ASR event
                handleASREvent(eEvent);
            } else {
                System.out.println("IM: Unknown Type Event Received");
                return;
            }
        }
        // event independent processing
        ActionToTake actionToTake = eventIndependentProcessing(eEvent);
        if (actionToTake != ActionToTake.none)
            imRequiredAction.takeAction(actionToTake);
    }

    @Override
    public void start()
    {
        // this method is called when we started a new user utterance
        int lastUttId = Integer.parseInt(s2sState.get("dialog.last_turn_id"));
        int newUttId = lastUttId + 1;
        s2sState.put("asr.in_utt", "true");
        s2sState.put("system.speaking", "false");
        s2sState.put("user.speaking", "false");
        s2sState.put("dialog.last_turn_id", String.valueOf(newUttId));
        s2sState.put("revoke.state", "standby");
        s2sState.put("revoke.has_seen_new_speech", "false");
        s2sState.put("revoke.has_reset_TTS", "false");
    }

    @Override
    public void stop()
    {
       // This is called when we reset the IM for a new session (dialog)
        s2sState.clear();
        initStates();
    }

    @Override
    public void finalResponseObtained(IMEvent eEvent)
    {
        System.out.println("Received final recognition hypothesis at state " + s2sState.get("revoke.state"));

        //check if we need to calculate turn related feature
        if (s2sState.get("revoke.state").equals("exit_hold") || s2sState.get("revoke.state").equals("exit_wait"))
        {
            ;// TODO calculate turn related feature if necessary
        }

        boolean bShouldSendToDM = s2sState.get("revoke.state").equals("wait_for_hyp") || s2sState.get("revoke.state").equals("exit_wait");

        //update revoke.state
        if (s2sState.get("revoke.state").equals("exit_hold") || s2sState.get("revoke.state").equals("exit_wait"))
        {
            s2sState.put("revoke.state", "standby");
        }  else if (s2sState.get("revoke.state").equals("wait_for_hyp")) {
            s2sState.put("revoke.state", "hold");
        } else if (s2sState.get("revoke.state").equals("standby")) {
            System.out.println("Ignore final hypothesis at final hypothesis");
        } else {
            System.out.println("Unexpected final hypothesis at final ASR");
        }


        if (bShouldSendToDM)
        {
            // command that the response be sent to DM
            imRequiredAction.takeAction(ActionToTake.moveOn);
        }
        else
        {
            imRequiredAction.takeAction(ActionToTake.cancel);
        }

    }

    @Override
    public void userStoppedStreaming() //TODO: Amos added this. Tony, update if needed.
    {
        imRequiredAction.takeAction(ActionToTake.goToGoogle);
        imRequiredAction.takeAction(ActionToTake.moveOn);
    }

    /////////////////////////////////////////////
    // private methods
    /////////////////////////////////////////////
    private void initStates()
    {
        // initialize the state variables
        s2sState.put("asr.in_utt", "false");
        s2sState.put("system.speaking", "false");
        s2sState.put("user.speaking", "false");
        s2sState.put("dialog.last_turn_id", "-1");
        s2sState.put("revoke.state", "standby");
        s2sState.put("revoke.has_seen_new_speech", "false");
        s2sState.put("revoke.has_reset_TTS", "false");
    }

    private void handleVADEvent(IMEvent eEvent)
    {
        String vadValue = eEvent.feature.get(IMEvent.featureVad);
        if (vadValue.equals("true"))
        {
            System.out.println("User Started Speaking");
            // Check if new speech occurred in the wait_for_hyp or hold
            if  (s2sState.get("revoke.state").equals("wait_for_hyp") || s2sState.get("revoke.state").equals("hold")) {
                s2sState.put("has_seen_new_speech", "true");
                System.out.println("VAD setting has_seen_new_speech to true");
            }
            // update user speaking states
            s2sState.put("user.speaking", "true");
        }
    }

    private void handleASREvent(IMEvent eEvent)
    {
        //partial ASR
        System.out.println("Received partial recognition hypothesis at state " + s2sState.get("revoke.state"));

        // update user speaking info and revoke.state
        if (s2sState.get("user.speaking").equals("true"))
        {
            if (Double.valueOf(eEvent.feature.get(IMEvent.featureFinalPause)) > 150)
            {
                System.out.println("User stopped speaking");
                s2sState.put("user.speaking", "false");
            }
        } else {
            // TODO detect new speech here
            if (s2sState.get("revoke.state").equals("wait_for_hyp") || s2sState.get("revoke.state").equals("hold"))
            {
                s2sState.put("has_seen_new_speech", "true");
                System.out.println("Partial ASR setting has_seen_new_speech to true");
            }
        }
    }
    private ActionToTake eventIndependentProcessing(IMEvent eEvent)
    {

        ActionToTake returnAction = ActionToTake.none;

        if (s2sState.get("asr.in_utt").equals("true"))
        {
            // check if utterance lasted for too long
            // check if we have been waiting for a partial for too long
            // check if pause is long enough for action threshold/listen threshold
            if (s2sState.get("user.speaking").equals("false") &&
                    Double.valueOf(eEvent.feature.get(IMEvent.featureDuration)) > iMinUttDuration)
            {
                // check action threshold
                if (Double.valueOf(eEvent.feature.get(IMEvent.featureFinalPause)) > iActionThreshold &&
                        s2sState.get("revoke.state").equals("standby"))
                {
                    System.out.println("End of utterance for action detected");
                    //update internal states
                    s2sState.put("revoke.has_seen_new_speech", "false");
                    s2sState.put("revoke.state", "wait_for_hyp");
                    returnAction =  ActionToTake.goToGoogle;
                }

                // check listen threshold
                if (Double.valueOf(eEvent.feature.get(IMEvent.featureFinalPause)) > iListenThreshold &&
                        (s2sState.get("revoke.state").equals("wait_for_hyp") || s2sState.get("revoke.state").equals("hold")))
                {
                    System.out.println("End of utterance for listen detected");
                    s2sState.put("asr.in_utt", "false");
                    s2sState.put("has_seen_new_speech", "false");

                    // if we are in hold state, set to exit_hold
                    if (s2sState.get("revoke.state").equals("hold")) {
                        s2sState.put("revoke.state", "exit_hold");
                    } else if (s2sState.get("revoke.state").equals("wait_for_hyp")) {
                        s2sState.put("revoke.state", "exit_wait");
                    } else {
                        System.out.println("Unexpected revoke.state at listen threshold");
                    }
                }
            }
            // At last check if we need to revoke
            boolean shouldRevoke = (s2sState.get("revoke.has_seen_new_speech").equals("true")) && (s2sState.get("system.speaking").equals("true"));

            if (shouldRevoke &&
                    (s2sState.get("revoke.state").equals("wait_for_hyp") || s2sState.get("revoke.state").equals("hold")))
            {
                System.out.println("New speech revoked the end of utterance");
                if (s2sState.get("revoke.state").equals("hold"))
                {
                    returnAction = ActionToTake.cancel;
                    s2sState.put("has_reset_TTS", "true");
                }
                s2sState.put("revoke.state", "standby");
            }
        }
        return returnAction;
    }



}
