package InMind.Server;

public class Main
{

    public static void main(String[] args)
    {

        InMindLogic logic = new InMindLogic();
        logic.runServer();


        while (true)
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

    }
}
