package InMind.Server.SignalInfo;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.endpoint.SpeechClassifier;

/**
 * Created by Amos Azaria on 09-Mar-15.
 */
public class InMindDataProcessor extends BaseDataProcessor
{
    interface InMindDataProcessorInterface
    {
        void hasSpeech(boolean speech);
    }

    InMindDataProcessorInterface inMindInterface = null;

    public void giveInterface(InMindDataProcessorInterface amosInterface)
    {
        this.inMindInterface = amosInterface;
    }

    @Override
    public Data getData() throws DataProcessingException
    {
        if (inMindInterface != null)
            inMindInterface.hasSpeech(((SpeechClassifier)getPredecessor()).isSpeech());
        return getPredecessor().getData();
    }
}

