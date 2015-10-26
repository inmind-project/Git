package com.yahoo.inmind.commons.control;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.util.List;

/**
 * Created by oscarr on 2/25/15.
 */
public interface HttpCommInterface {
    List<RequestHandle> getRequestHandles();
    void addRequestHandle(RequestHandle handle);
    Header[] getRequestHeaders(String stringHeaders);
    HttpEntity getRequestEntity(String bodyText);
    AsyncHttpClient getAsyncHttpClient();
    void setAsyncHttpClient(AsyncHttpClient client);
    AsyncHttpRequest getHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context);
    ResponseHandlerInterface getResponseHandler();
    String getDefaultURL();
    RequestHandle execute(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler);
}
