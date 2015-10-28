//package InMind.Server.interactionManager;
//
///**
// * Created by Tony on 2/23/15.
// */
//
//import java.util.HashMap;
//import java.util.Map;
//public class IMEvent {
//    IMEventType type;
//    public Map<String,String> feature;
//
//    public enum IMEventType {ping, vad, asr}
//
//    public final static String featureVad = "VAD"; //"true" or "false"
//    public final static String featureFinalPause = "final_pause"; //double. final pause in milliseconds.
//    public final static String featureDurationFromBeginning = "duration"; //double. Duration from beginning.
//    public final static String featureDurationOfSample = "sample_duration"; //double. Duration of this sample.
//
//
//    public IMEvent(IMEventType mType)
//    {
//        type = mType;
//        feature = new HashMap<String, String>();
//    }
//
//}
//
