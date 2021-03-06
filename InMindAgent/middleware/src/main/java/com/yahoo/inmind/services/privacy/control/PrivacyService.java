package com.yahoo.inmind.services.privacy.control;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yahoo.inmind.comm.generic.model.MBRequest;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.services.generic.control.GenericService;

public class PrivacyService extends GenericService {
    public PrivacyService() {
        super( null );
    }

    @Override
    public void doAfterBind() {
        super.doAfterBind();
        // here goes your code...
        // ...
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = super.onBind( intent );
        // here goes your code:
        // ...
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * This method extracts the caller
     * @param caller
     * @return
     */
    private String extractCaller(Object caller) {
        if( caller == null )
            throw new NullPointerException("Caller must not be null!");
        return caller instanceof Class? ((Class) caller).getCanonicalName()
                : caller.getClass().getCanonicalName();
    }

    /**
     * This method extracts the resource
     * @param request
     * @return
     */
    private String extractResource(Object request){
        return request instanceof MBRequest ? Constants.getID(((MBRequest) request).getRequestId())
                : request instanceof Class? ((Class) request).getCanonicalName()
                : request.getClass().getCanonicalName();
    }


    /**
     *  This method will grant or deny permissions to the caller to access a resource. Also, it will
     *  logs and monitors the flow of messages between InMind components.
     *
     * @param caller    this is the object who calls the MessageBroker's method
     * @resource        a resource can be the name of a class or file (e.g., xml); the name of a
     *                  service, sensor, or effector; or a MBRequest identifier (i.e., MSG_XXX_XX...)
     * @param mbMethod  this is the MessageBroker's method that was called by caller
     * @return
     */
    public boolean checkPermissions( Object caller, Object resource, String mbMethod ){
        String callerStr = extractCaller(caller);
        String resourceStr = extractResource(resource);
        Log.e("", "Caller class: " + callerStr + "  resource: " + resourceStr + "  and method: "
                + mbMethod );

        // check privacy
        // .....

        // monitoring and logging
        // ...

        return true;
    }

}
