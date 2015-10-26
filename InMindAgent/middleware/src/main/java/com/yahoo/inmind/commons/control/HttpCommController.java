package com.yahoo.inmind.commons.control;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oscarr on 2/25/15.
 */
public class HttpCommController implements HttpCommInterface{

    private static final String LOG_TAG = "HttpCommController";
    private Context context;
    protected static String PROTOCOL = "http://";
    private final List<RequestHandle> requestHandles = new LinkedList<>();
    private int statusCode;
    private Header[] headers;
    private byte[] response;
    private byte[] errorResponse;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient() {
        @Override
        protected AsyncHttpRequest newAsyncHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
            AsyncHttpRequest httpRequest = getHttpRequest(client, httpContext, uriRequest, contentType, responseHandler, context);
            return httpRequest == null
                    ? super.newAsyncHttpRequest(client, httpContext, uriRequest, contentType, responseHandler, context)
                    : httpRequest;
        }
    };


    public HttpCommController( Context context ){
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static String getPROTOCOL() {
        return PROTOCOL;
    }

    public static void setPROTOCOL(String PROTOCOL) {
        HttpCommController.PROTOCOL = PROTOCOL;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public byte[] getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(byte[] errorResponse) {
        this.errorResponse = errorResponse;
    }

    @Override
    public AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    @Override
    public void setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public List<RequestHandle> getRequestHandles() {
        return requestHandles;
    }

    @Override
    public void addRequestHandle(RequestHandle handle) {
        if (null != handle) {
            requestHandles.add(handle);
        }
    }

    public List<Header> getRequestHeadersList( String stringHeaders ) {
        List<Header> headers = new ArrayList<>();
        if (stringHeaders != null && stringHeaders.length() > 3) {
            String[] lines = stringHeaders.split("\\r?\\n");
            for (String line : lines) {
                try {
                    int equalSignPos = line.indexOf('=');
                    if (1 > equalSignPos) {
                        throw new IllegalArgumentException("Wrong header format, may be 'Key=Value' only");
                    }
                    String headerName = line.substring(0, equalSignPos).trim();
                    String headerValue = line.substring(1 + equalSignPos).trim();
                    Log.d(LOG_TAG, String.format("Added header: [%s:%s]", headerName, headerValue));
                    headers.add(new BasicHeader(headerName, headerValue));
                } catch (Throwable t) {
                    Log.e(LOG_TAG, "Not a valid header line: " + line, t);
                }
            }
        }
        return headers;
    }

    public Header[] getRequestHeaders( String stringHeaders ) {
        List<Header> headers = getRequestHeadersList( stringHeaders );
        return headers.toArray(new Header[headers.size()]);
    }

    public HttpEntity getRequestEntity( String bodyText ) {
        if ( bodyText!= null) {
            try {
                return new StringEntity(bodyText);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "cannot create String entity", e);
            }
        }
        return null;
    }


    @Override
    public String getDefaultURL() {
        return PROTOCOL + "www.cmu.edu";
    }


    @Override
    public ResponseHandlerInterface getResponseHandler() {
        return new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {}

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                HttpCommController.this.statusCode = statusCode;
                HttpCommController.this.headers = headers;
                HttpCommController.this.response = response;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                HttpCommController.this.statusCode = statusCode;
                HttpCommController.this.headers = headers;
                HttpCommController.this.response = errorResponse;
            }
        };
    }

    @Override
    public RequestHandle execute(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler) {
        return client.post( context, URL, headers, entity, null, responseHandler );
    }


    @Override
    public AsyncHttpRequest getHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
        return null;
    }

}
