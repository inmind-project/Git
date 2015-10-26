package com.yahoo.inmind.services.streaming.model;

import com.yahoo.inmind.services.streaming.view.StreamingSurfaceView;

/**
 * Created by oscarr on 10/8/15.
 */
public class StreamingSubscriptionVO {
    private String destination;
    private int sessionTimeToLive;


    //audio
    private String audioEncoder;
    /** The sampling rate **/
    private int audioSamplingRate;
    /** The bitrate in bit per seconds **/
    private int audioBitRate;

    //video
    private String videoEncoder;
    private int videoResolutionX;
    private int videoResolutionY;
    /** Frame rate in frame per second **/
    private int videoFramerate;
    /** Bit rate in bits per second **/
    private int videoBitrate;

    //camera
    private int cameraType;
    private boolean flashEnabled;
    private StreamingSurfaceView streamingSurfaceView;
    private int previewOrientation;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getSessionTimeToLive() {
        return sessionTimeToLive;
    }

    public void setSessionTimeToLive(int sessionTimeToLive) {
        this.sessionTimeToLive = sessionTimeToLive;
    }

    public String getAudioEncoder() {
        return audioEncoder;
    }

    public void setAudioEncoder(String audioEncoder) {
        this.audioEncoder = audioEncoder;
    }

    public int getAudioSamplingRate() {
        return audioSamplingRate;
    }

    public void setAudioSamplingRate(int audioSamplingRate) {
        this.audioSamplingRate = audioSamplingRate;
    }

    public int getAudioBitRate() {
        return audioBitRate;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public String getVideoEncoder() {
        return videoEncoder;
    }

    public void setVideoEncoder(String videoEncoder) {
        this.videoEncoder = videoEncoder;
    }

    public int getPreviewOrientation() {
        return previewOrientation;
    }

    public void setPreviewOrientation(int previewOrientation) {
        this.previewOrientation = previewOrientation;
    }

    public int getVideoResolutionX() {
        return videoResolutionX;
    }

    public void setVideoResolutionX(int videoResolutionX) {
        this.videoResolutionX = videoResolutionX;
    }

    public int getVideoResolutionY() {
        return videoResolutionY;
    }

    public void setVideoResolutionY(int videoResolutionY) {
        this.videoResolutionY = videoResolutionY;
    }

    public int getVideoFramerate() {
        return videoFramerate;
    }

    /** Frame rate in frames per second **/
    public void setVideoFramerate(int videoFramerate) {
        this.videoFramerate = videoFramerate;
    }

    public int getVideoBitrate() {
        return videoBitrate;
    }

    /** Bit rate in bits per second **/
    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public int getCameraType() {
        return cameraType;
    }

    public void setCameraType(int cameraType) {
        this.cameraType = cameraType;
    }

    public boolean isFlashEnabled() {
        return flashEnabled;
    }

    public void setFlashEnabled(boolean flashEnabled) {
        this.flashEnabled = flashEnabled;
    }

    public StreamingSurfaceView getStreamingSurfaceView() {
        return streamingSurfaceView;
    }

    public void setStreamingSurfaceView(StreamingSurfaceView streamingSurfaceView) {
        this.streamingSurfaceView = streamingSurfaceView;
    }
}
