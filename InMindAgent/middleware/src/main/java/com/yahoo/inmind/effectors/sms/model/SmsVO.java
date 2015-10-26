package com.yahoo.inmind.effectors.sms.model;

import android.content.Context;
import android.os.Handler;

import com.yahoo.inmind.effectors.generic.control.EffectorObserver;

/**
 * Created by oscarr on 9/29/15.
 */
public class SmsVO extends EffectorObserver{
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     * @param context
     */
    public SmsVO(Handler handler, Context context) {
        super(handler, context);
    }
}
