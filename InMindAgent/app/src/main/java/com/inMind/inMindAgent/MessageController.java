package com.inMind.inMindAgent;

import android.os.Handler;
import android.util.Log;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.rules.control.DecisionRuleValidator;
import com.yahoo.inmind.commons.rules.model.DecisionRule;

import InMind.Consts;

public class MessageController
{
    public static void dealWithMessage(String command, String args, MessageBroker messageBroker, Handler talkHandler)
    {
        //call middleware;
        if (command.equalsIgnoreCase(Consts.news))
        {
            NewsCommunicator.dealWithMessage(args, messageBroker, talkHandler);
        }
        else if (command.equalsIgnoreCase(Consts.execJson))
        {
            Log.d("json, executing rule with middleware", args);
            DecisionRuleValidator.getInstance().registerRule(Util.fromJson(args, DecisionRule.class) );
        }
    }

}
