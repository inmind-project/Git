package com.inMind.inMindAgent;

import android.content.Context;

import com.yahoo.inmind.middleware.control.MessageBroker;

public class MessageController
{
    public void dealWithMessage(String command, String args, MessageBroker messageBroker)
    {
        if (command.equalsIgnoreCase("News"))
        {
            NewsCommunicator.dealWithMessage(args, messageBroker);
        }
        //call middleware;
    }

}
