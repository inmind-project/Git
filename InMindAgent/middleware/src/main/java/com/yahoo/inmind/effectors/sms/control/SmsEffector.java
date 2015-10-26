package com.yahoo.inmind.effectors.sms.control;

import android.content.Context;
import android.os.Handler;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.effectors.generic.control.EffectorObserver;

/**
 * Created by oscarr on 9/29/15.
 */
public class SmsEffector extends EffectorObserver{

    public SmsEffector(Handler handler, Context context) {
        super(handler, context);
        this.actions.add(Constants.ACTION_TEXT_MESSAGE );
    }
}
