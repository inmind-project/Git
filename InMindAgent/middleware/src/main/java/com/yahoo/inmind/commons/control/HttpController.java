package com.yahoo.inmind.commons.control;

import android.util.Log;

import com.goebl.david.Webb;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by oscarr on 2/25/15.
 */
public class HttpController {

    private static Webb webb;


    //Using goebl DavidWebb
    public static String getHttpPostResponse( String url, Map<String, Object> params, Object body, int timeout ){
        com.goebl.david.Response<String> response;
        String result = "";
        if( webb == null ) {
            webb = Webb.create();
        }

        try {
            com.goebl.david.Request request = webb.post( url );
            if( body == null ){
                if( params != null ) {
                    Iterator<String> it = params.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        request.param(key, params.get(key));
                    }
                }
            } else{
                request.body( body );
            }

            response = request
                    //.compress() //check this
                    .connectTimeout( 10000 ) //timeout )
                    .asString();

            if (response.isSuccess()) {
                result = response.getBody();
            } else {
                Log.e("Util.HttpController", ""+response.getStatusCode() );
                Log.e("Util.HttpController", response.getResponseMessage());
                Log.e("Util.HttpController", response.getErrorBody().toString());
            }
        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return result;
    }


    public static String getHttpGetResponse( String url, Map<String, Object> params, Object body, int timeout ){
        com.goebl.david.Response<String> response;
        String result = "";
        if( webb == null ) {
            webb = Webb.create();
        }

        try {
            com.goebl.david.Request request = webb.get( url );
            if( body == null ){
                if( params != null ) {
                    Iterator<String> it = params.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        request.param(key, params.get(key));
                    }
                }
            } else{
                request.body( body );
            }

            response = request
                    //.compress() //check this
                    .connectTimeout( timeout ) //timeout )
                    .asString();

            if (response.isSuccess()) {
                result = response.getBody();
            } else {
                Log.e("Util.HttpController", ""+response.getStatusCode() );
                Log.e("Util.HttpController", response.getResponseMessage());
                Log.e("Util.HttpController", response.getErrorBody().toString());
            }
        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return result;
    }




    // http://www.androidhive.info/2014/12/android-uploading-camera-image-video-to-server-with-progress-bar/
    // http://jason.pureconcepts.net/2014/11/install-apache-php-mysql-mac-os-x-yosemite/
    @SuppressWarnings("deprecation")
    public static String uploadFile(byte[] sourceFile, String url, String name, String mimeType) {
        String responseString = "";
        mimeType = "audio/acc";
        if( mimeType == null || mimeType.equals("") ){
            mimeType = "image/jpeg";
        }

        try {
            Log.e("","Sending Message: " + Calendar.getInstance().toString());
            HttpPost httppost = new HttpPost(url);
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            HttpClient httpclient = new DefaultHttpClient();

            entity.addBinaryBody("image", sourceFile, ContentType.create( mimeType ), name);
            httppost.setEntity( entity.build() );
            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            response.getEntity().consumeContent();

//            Log.e("","7. time: '" + (System.currentTimeMillis() - time ));
//            time = System.currentTimeMillis();
//
//            //response
//            HttpEntity r_entity = response.getEntity();
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == 200) {
//                // Server response
//                responseString = EntityUtils.toString(r_entity);
//            } else {
//                responseString = "Error occurred! Http Status Code: " + statusCode;
//            }
//            Log.e("","8. time: '" + (System.currentTimeMillis() - time ));
//            time = System.currentTimeMillis();
        }catch (Exception e){
            e.printStackTrace();
        }

        return responseString;

    }





    /** ========================================================================================== **/


    public static String getHttpGetResponseWebb( String url, Map<String, String> payload ){
        com.goebl.david.Response<String> response;
        String result = "", content = "";
        if( webb == null ) {
            webb = Webb.create();
        }

        try {
            Iterator<String> it = payload.keySet().iterator();
            while( it.hasNext()  ){
                String key = it.next();
                content += key + "=" + URLEncoder.encode( payload.get(key), "UTF-8");
                if( it.hasNext() ){
                    content += "&";
                }
            }
            response = webb
                    .get( url+"?"+ content )
                    .connectTimeout(10 * 1000)
                    .asString();

            if (response.isSuccess()) {
                result = response.getBody();
            } else {
                Log.e("Util.HttpController", ""+response.getStatusCode() );
                Log.e("Util.HttpController", response.getResponseMessage());
                Log.e("Util.HttpController", response.getErrorBody().toString());
            }


        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return result;
    }



    public static String getHttpGetResponse( String url, Map<String, Object> payload ){
        InputStream inputStream;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            String content = "";
            if( payload != null ) {
                Iterator<String> it = payload.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    content += key + "=" + URLEncoder.encode( (String )payload.get(key) , "UTF-8");
                    if (it.hasNext()) {
                        content += "&";
                    }
                }
            }

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute( new HttpGet( url + (content.equals("")? "" : "?"+ content ) ) );
            //HttpResponse httpResponse = httpclient.execute( new HttpGet( url ) );

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
        }

        return result;
    }



// convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }



}


