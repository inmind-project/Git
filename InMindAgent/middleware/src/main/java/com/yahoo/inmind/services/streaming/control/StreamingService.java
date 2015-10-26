package com.yahoo.inmind.services.streaming.control;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yahoo.inmind.comm.streaming.model.StreamingErrorEvent;
import com.yahoo.inmind.comm.streaming.model.StreamingEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.services.generic.control.GenericService;
import com.yahoo.inmind.services.streaming.control.audio.AudioQuality;
import com.yahoo.inmind.services.streaming.view.StreamingSurfaceView;
import com.yahoo.inmind.services.streaming.control.rtsp.RtspClient;
import com.yahoo.inmind.services.streaming.control.video.VideoQuality;
import com.yahoo.inmind.services.streaming.model.StreamingSubscriptionVO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oscar Romero
 */
public class StreamingService extends GenericService implements
        RtspClient.Callback,
        Session.Callback,
        SurfaceHolder.Callback{

    private Session mSession;
    private RtspClient mClient;
    private StreamingSurfaceView mStreamingSurfaceView;

    public StreamingService(){
    }

    public void subscribe( StreamingSubscriptionVO subscriptionVO ){
        buildSession( subscriptionVO );
        mSession.setDestination(subscriptionVO.getDestination());
    }

    public void unsubscribe( StreamingSubscriptionVO subscriptionVO ) {
        mSession.removeDestination( Util.extractIpAddress(subscriptionVO.getDestination())[0]);
    }


    /**
     * Configures the SessionBuilder
     */
    public void buildSession( StreamingSubscriptionVO subscriptionVO ){
        if( subscriptionVO.getStreamingSurfaceView() != null ) {
            mStreamingSurfaceView = subscriptionVO.getStreamingSurfaceView();
            mStreamingSurfaceView.getHolder().addCallback(this);
            if( mSession != null ) {
                mSession.setSurfaceView(mStreamingSurfaceView);
            }
        }
        if( mSession == null ) {
            mSession = SessionBuilder.getInstance()
                    .setContext(getApplicationContext())
                    .setSurfaceView(mStreamingSurfaceView)
                    .setAudioEncoder(getAudioEncoderInt(subscriptionVO.getAudioEncoder()))
                    .setAudioQuality(new AudioQuality(subscriptionVO.getAudioSamplingRate(),
                            subscriptionVO.getAudioBitRate()))
                    .setVideoEncoder(getVideoEncoderInt(subscriptionVO.getVideoEncoder()))
                    .setVideoQuality(new VideoQuality(subscriptionVO.getVideoResolutionX(),
                            subscriptionVO.getVideoResolutionY(), subscriptionVO.getVideoFramerate(),
                            subscriptionVO.getVideoBitrate()))
                    .setPreviewOrientation(subscriptionVO.getPreviewOrientation())
                    .setPreviewOrientation(0)
                    .setCamera( subscriptionVO.getCameraType() )
                    .setDestination( subscriptionVO.getDestination() )
                    .setFlashEnabled( subscriptionVO.isFlashEnabled() )
                    .setTimeToLive( subscriptionVO.getSessionTimeToLive() )
                    .setCallback(this)
                    .build();

            // Configures the RTSP client
            mClient = new RtspClient();
            mClient.setSession(mSession);
            mClient.setCallback(this);

            // Use this to force streaming with the MediaRecorder API
            //mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIARECORDER_API);

            // Use this to stream over TCP, EXPERIMENTAL!
            //mClient.setTransportMode(RtspClient.TRANSPORT_TCP);

            // Use this if you want the aspect ratio of the surface view to
            // respect the aspect ratio of the camera preview
            //mSurfaceView.setAspectRatioMode(StreamingSurfaceView.ASPECT_RATIO_PREVIEW);
        }
    }


    private int getAudioEncoderInt(String audioEnconder){
        if( audioEnconder.equals(Constants.STREAMING_AUDIO_ENCODER_AAC )) return SessionBuilder.AUDIO_AAC;
        if( audioEnconder.equals(Constants.STREAMING_AUDIO_ENCODER_AMRNB )) return SessionBuilder.AUDIO_AMRNB;
        return SessionBuilder.AUDIO_NONE;
    }

    private int getVideoEncoderInt(String videoEnconder){
        if( videoEnconder.equals(Constants.STREAMING_VIDEO_ENCODER_H263 )) return SessionBuilder.VIDEO_H263;
        if( videoEnconder.equals(Constants.STREAMING_VIDEO_ENCODER_H264 )) return SessionBuilder.VIDEO_H264;
        return SessionBuilder.VIDEO_NONE;
    }


    public void toogleStream( String userName, String password, String destinationString ){
        if (!mClient.isStreaming()) {
            startStreaming( userName, password, destinationString );
        } else {
            stopStreaming( destinationString );
        }
    }


    public void startStreaming( String userName, String password, String destinationString){
        String[] destination = Util.extractIpAddress(destinationString);
        mClient.setCredentials(userName, password);
        mClient.setServerAddress(destination[0], Integer.parseInt(destination[1]));
        mClient.setDestination( destinationString );
        mClient.setStreamPath("/" + destination[2]);
        mClient.startStream();
    }

    /**
     * Stops the stream and disconnects from the RTSP server
     */
    public void stopStreaming(String destination){
        if( destination == null ){
            for( String dest : mClient.getSession().getDestinations() ){
                mClient.getSession().removeDestination( dest );
            }
            return;
        }else {
            mClient.getSession().removeDestination(destination);
        }
        if( mClient.getSession().getDestinations().isEmpty() ){
            // Stops the stream and disconnects from the RTSP server
            mClient.stopStream();
        }
    }

    public void toggleFlash(){
        mSession.toggleFlash();
    }

    public void switchCamera(){
        mSession.switchCamera();
    }

    @Override
    public void doAfterBind() {}

    @Override
    public void onBitrateUpdate( long bitrate ) {
        mb.send(StreamingEvent.build()
                .setType( Constants.STREAMING_BITRATE_UPDATE )
                .setBitRate( bitrate / 1000 ) );
    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        StreamingErrorEvent error = new StreamingErrorEvent();
        switch (reason) {
            case Session.ERROR_CAMERA_ALREADY_IN_USE:
                error.setErrorType( Constants.STREAMING_ERROR_CAMERA_ALREADY_IN_USE );
                break;
            case Session.ERROR_CAMERA_HAS_NO_FLASH:
                error.setErrorType( Constants.STREAMING_ERROR_CAMERA_HAS_NO_FLASH );
                break;
            case Session.ERROR_INVALID_SURFACE:
                error.setErrorType( Constants.STREAMING_ERROR_INVALID_SURFACE );
                break;
            case Session.ERROR_STORAGE_NOT_READY:
                error.setErrorType( Constants.STREAMING_ERROR_STORAGE_NOT_READY );
                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
                VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
                error.setErrorType(Constants.STREAMING_ERROR_CONFIGURATION_NOT_SUPPORTED);
                error.setVideoQuality(quality.toString());
                e.printStackTrace();
                return;
            case Session.ERROR_OTHER:
                error.setErrorType( Constants.STREAMING_ERROR_OTHER );
                break;
        }
        error.setMessage( e.getMessage() );
        mb.send( error );
    }

    @Override
    public void onPreviewStarted() {
        mb.send(StreamingEvent.build()
                .setType(Constants.STREAMING_PREVIEW_STARTED)
                .setCamera(mSession.getCamera()));

    }

    @Override
    public void onSessionConfigured() {
        mb.send(StreamingEvent.build().setType(Constants.STREAMING_SESSION_CONFIGURED));
    }

    @Override
    public void onSessionStarted() {
        mb.send( StreamingEvent.build().setType(Constants.STREAMING_SESSION_STARTED) );
    }

    @Override
    public void onSessionStopped() {
        mb.send( StreamingEvent.build().setType( Constants.STREAMING_SESSION_STOPPED ) );
    }

    @Override
    public void onRtspUpdate(int message, Exception e) {
        StreamingErrorEvent errorEvent = new StreamingErrorEvent();
        switch (message) {
            case RtspClient.ERROR_CONNECTION_FAILED:
                errorEvent.setErrorType( Constants.STREAMING_ERROR_RTSP_CONNECTION_FAILED );
                errorEvent.setMessage("Connection with remote streaming server has failed");
                break;
            case RtspClient.ERROR_WRONG_CREDENTIALS:
                errorEvent.setErrorType( Constants.STREAMING_ERROR_RSTP_WRONG_CREDENTIALS );
                errorEvent.setMessage("Wrong credentials to connect with remote streaming server");
                break;
        }
        mb.send(errorEvent);
    }

    public void setVideoQuality(String videoQuality) {
        Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
        Matcher matcher = pattern.matcher( videoQuality );
        matcher.find();
        int width = Integer.parseInt(matcher.group(1));
        int height = Integer.parseInt(matcher.group(2));
        int framerate = Integer.parseInt(matcher.group(3));
        int bitrate = Integer.parseInt(matcher.group(4)) * 1000;
        mSession.setVideoQuality(new VideoQuality(width, height, framerate, bitrate));
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mb.send(StreamingEvent.build().setType(Constants.STREAMING_SURFACE_CHANGED));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
        mb.send(StreamingEvent.build().setType(Constants.STREAMING_SURFACE_CREATED));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if( mStreamingSurfaceView != null ) {
            stopStreaming(mStreamingSurfaceView.getDestination());
        }
        mb.send(StreamingEvent.build().setType(Constants.STREAMING_SURFACE_DESTROYED));
    }

    public void startPreview() {
        if( mSession != null ) {
            mSession.startPreview();
        }
    }

    @Override
    public void onDestroy(){
        if( mSession != null ) {
            mSession.stop();
            mSession.release();
            mSession = null;
        }
        if( mClient != null ){
            stopStreaming( null );
            mClient.release();
            mClient = null;
        }
        if( mStreamingSurfaceView != null ){
            mStreamingSurfaceView.getHolder().removeCallback( this );
            mStreamingSurfaceView = null;
        }
    }
}
