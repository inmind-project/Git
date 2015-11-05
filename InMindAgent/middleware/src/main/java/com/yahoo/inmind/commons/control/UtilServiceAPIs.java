package com.yahoo.inmind.commons.control;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.yahoo.inmind.comm.generic.control.GooglePlayServicesEvent;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.services.generic.control.ResourceLocator;

import java.util.Arrays;


/**
 * Created by oscarr on 8/5/15.
 */
public class UtilServiceAPIs {
    private static SharedPreferences settings;
    private static String[] SCOPES = {CalendarScopes.CALENDAR};
    private static final String UUID_FILE = "uuid_file";

    public static String PREF_ACCOUNT_NAME = "accountName";
    public static final String API_KEY_YAHOO = "dj0yJmk9VVFwSmQ3VWZ1cTFMJmQ9WVdrOVVXNVVOMk41TnpZbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD00MA--";
    public static final String API_KEY_WUNDER = "a3a8e9e77a3fea18";
    public static final String API_KEY_HOTWIRE = "3hxp4qnd32gm8jptnyc2cq5f";
    public static final String QUERY_WOEID = "http://where.yahooapis.com/v1/places.q(%s)?appid=" + API_KEY_YAHOO;
    public static final String URL_FORECAST = "http://weather.yahooapis.com/forecastrss?w=%s&appid" + API_KEY_YAHOO;
    public static final String API_FORECAST_YQL_ENDPOINT = "https://query.yahooapis.com/v1/public/yql?";
    public static final String API_FORECAST_YQL_QUERY = "q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text=\"replace_place\")";
    public static final String API_FORECAST_YQL_FORMAT = "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    public static String API_FORECAST_YQL_URL = API_FORECAST_YQL_ENDPOINT + API_FORECAST_YQL_QUERY + API_FORECAST_YQL_FORMAT;
    public static final String API_WUNDERGROUND = "http://api.wunderground.com/api/"+ API_KEY_WUNDER +"/hourly/q/%s/%s.json";

    public static GoogleAccountCredential credential;
    public static boolean chooseAccountInProcess = false;
    public static boolean isAccountSelected = false;


    public static boolean initializeCredentials(Context context, String accountName){
        if( accountName == null ){
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences( context );
            accountName = mPrefs.getString("username", "inmind.yahoo.2015@gmail.com");
        }
        if( settings == null ) {
            settings = context.getSharedPreferences(UUID_FILE, Context.MODE_PRIVATE);
        }
        if( credential == null
                || ( credential != null && credential.getSelectedAccountName() == null )
                || ( credential != null && !credential.getSelectedAccountName().equals(accountName)) ) {
            credential = GoogleAccountCredential.usingOAuth2(
                    context, Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff())
                    .setSelectedAccountName(accountName);
            if( accountName != null ){
                isAccountSelected = true;
            }
            return true;
        }
        return false;
    }

    public static boolean configAccountName(Activity activity, String accountName){
        activity = validateActivity( activity, "Google Credentials cannot be initialize" );
        boolean isInitialized = initializeCredentials(activity.getApplicationContext(), accountName);
        credential.setSelectedAccountName( accountName );
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(UtilServiceAPIs.PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        return isInitialized;
    }


    public static boolean isDeviceOnline(Context context) {
        context = validateActivity( context, "Google Play Services cannot check whether device is online" );
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isGooglePlayServicesAvailable(Context context) throws IllegalStateException{
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        boolean result = true;
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            if( context instanceof Activity ) {
                showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, (Activity)context);
            }
            result = false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            result = false;
        }
        if( !result ){
            MessageBroker.getInstance(context).send(
                    context,
                    GooglePlayServicesEvent.build()
                    .setError(true)
                    .setNotification( "Google Play Services required: after installing, close and " +
                            "relaunch this app." ) );
        }
        return result;
    }

    public static void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode,
                                                                     Activity activity) throws IllegalStateException{
        activity = validateActivity( activity, "Google Play Services Error Dialog cannot be shown");
        final Activity act = activity;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        act,
                        Constants.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    public static void chooseAccount(Activity activity) throws IllegalStateException{
        activity = validateActivity( activity, "Google Account picker cannot be shown" );
        chooseAccountInProcess = true;
        initializeCredentials( activity.getApplication(), null );
        activity.startActivityForResult(
                credential.newChooseAccountIntent(), Constants.REQUEST_ACCOUNT_PICKER);
    }


    private static <T extends Context> T validateActivity(T activity, String message){
        if( activity == null ){
            activity = (T) ResourceLocator.getExistingInstance().getTopActivity();
        }
        if( activity == null ){
            throw new IllegalStateException( message + " . Caller Activity or Context " +
                    "should call Message Broker's subscribe() method." );
        }
        return activity;
    }
}
