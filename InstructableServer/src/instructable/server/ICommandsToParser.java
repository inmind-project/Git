package instructable.server;

import java.util.List;

/**
 * Created by Amos Azaria on 20-Apr-15.
 */
public interface ICommandsToParser
{
    void addTrainingEg(String originalCommand, List<String> replaceWith);
}