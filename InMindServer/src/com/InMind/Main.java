package com.InMind;

public class Main {

    public static void main(String[] args) {

        InMindLogic logic = new InMindLogic();
        logic.runServer();


        //freeze...
        while (true)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
}
