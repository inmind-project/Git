package InMind.DialogFunctions;

import InMind.Consts;
import InMind.Server.asr.ASR;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Amos Azaria on 25-Mar-15.
 *
 * Message format:
 * [userid]^ASR^[text]
 * e.g.
 * 438274678ds4^ASR^hello world
 *
 * We also need a cancel / abort message (sent by the IM), I suggest the following:
 * [userid]^cancel
 * e.g.
 * 438274678ds4^cancel
 *
 */
public class AsrOnly
{
    static final String distractionIp = "128.2.210.187";
    static final int distractionPort = 61616;
    static final String distractionOutTopic = "asrinput";

    public static List<String> forwardToDistraction(Map<String, Object> fullInfo, String userId, ASR.AsrRes userText)
    {
        String messageToSend = userId+ Consts.commandChar + Consts.asrStr + Consts.commandChar + userText.text;
        Thread brokerThread = new Thread(new AsrMessageSender(distractionIp,distractionPort,distractionOutTopic, messageToSend));
        brokerThread.start();
        return new LinkedList<String>();
    }


    public static class AsrMessageSender implements Runnable {

        String textMessage;
        String ip;
        int port;
        String topic;

        AsrMessageSender(String ip, int port, String topic, String message)
        {
            this.textMessage = message;
            this.ip = ip;
            this.port = port;
            this.topic = topic;
        }

        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://"+ip+":"+port);//"vm://localhost");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createTopic(topic);//.createQueue("TEST.FOO");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a messages
                //String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                //String text = "fdkasjd543tre43efh^ASR^Hello world";
                TextMessage message = session.createTextMessage(textMessage);

                // Tell the producer to send the message
                System.out.println("Sent message: "+ textMessage + ". " + message.hashCode());
                producer.send(message);

                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }
}
