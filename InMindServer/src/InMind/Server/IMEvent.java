package InMind.Server;

/**
 * Created by Tony on 2/23/15.
 */

import java.util.HashMap;
import java.util.Map;
public class IMEvent {
    IMEventType type;
    Map<String,String> feature;

    enum IMEventType {ping, vad, asr}

    final static String featureVad = "VAD"; //"true" or "false"
    final static String featureFinalPause = "final_pause"; //double. final pause in milliseconds.
    final static String featureDuration = "duration"; //double. Duration of this sample.


    IMEvent(IMEventType mType)
    {
        type = mType;
        feature = new HashMap<String, String>();
    }

}

