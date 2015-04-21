package instructable.server;

import instructable.server.hirarchy.ConceptContainer;
import instructable.server.hirarchy.EmailMessage;
import instructable.server.hirarchy.InstanceContainer;
import instructable.server.hirarchy.OutgoingEmail;

import java.util.Set;

/**
 * Created by Amos Azaria on 15-Apr-15.
 */
public class OutEmailCommandController
{
    private String myEmail;
    InstanceContainer instanceContainer;

    public OutgoingEmail getEmailBeingComposed(ExecutionStatus executionStatus)
    {
        OutgoingEmail emailBeingComposed = (OutgoingEmail)instanceContainer.getInstance(executionStatus, OutgoingEmail.strOutgoingEmailTypeAndName, OutgoingEmail.strOutgoingEmailTypeAndName);
        if (emailBeingComposed != null)
            return emailBeingComposed;
        else
        {
            executionStatus.add(ExecutionStatus.RetStatus.error, "there is no email being composed");
            return null;
        }
    }

    OutEmailCommandController(String myEmail, ConceptContainer conceptContainer, InstanceContainer instanceContainer)
    {
        this.myEmail = myEmail;
        conceptContainer.defineConceptFromClass(OutgoingEmail.class);
        this.instanceContainer = instanceContainer;
    }

    public void sendEmail(ExecutionStatus executionStatus)
    {
        OutgoingEmail email = getEmailBeingComposed(executionStatus);
        if (email != null)
            email.sendEmail(executionStatus);
    }

    public void createNewEmail(ExecutionStatus executionStatus)
    {
        instanceContainer.addInstance(executionStatus, OutgoingEmail.strOutgoingEmailTypeAndName, new OutgoingEmail(executionStatus, myEmail));
    }

    public Set<String> getComposedEmailFields()
    {
        Set<String> allFields = emailBeingComposed.getAllFieldNames();
        allFields.remove(EmailMessage.senderStr);
        return allFields;
    }
}
