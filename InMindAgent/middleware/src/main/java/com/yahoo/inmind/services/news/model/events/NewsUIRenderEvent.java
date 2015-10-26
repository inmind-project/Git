package com.yahoo.inmind.services.news.model.events;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yahoo.inmind.comm.generic.model.BaseEvent;


/**
 * Created by oscarr on 12/29/14.
 */
public class NewsUIRenderEvent extends BaseEvent {
    private TextView uiTVTitle;
    private TextView uiTVSummary;
    private TextView uiTVRank;
    private TextView uiTVReason;
    private TextView uiTVScore;
    private TextView uiTVPublisher;
    private TextView uiTVFeat;
    private TextView uiTVFeat2;
    private ImageView uiIVImg;
    private ImageButton uiIBShareFb;
    private ImageButton uiIBShareTwitter;
    private ImageButton uiIBShareTumblr;
    private ImageButton uiIBShareMore;
    private ImageButton uiIBDislike;
    private ImageButton uiIBLike;

    public TextView getUiTVTitle() {
        return uiTVTitle;
    }

    public TextView setUiTVTitle(TextView uiTVTitle) {
        this.uiTVTitle = uiTVTitle;
        return uiTVTitle;
    }

    public TextView getUiTVSummary() {
        return uiTVSummary;
    }

    public TextView setUiTVSummary(TextView uiTVSummary) {
        this.uiTVSummary = uiTVSummary;
        return uiTVSummary;
    }

    public TextView getUiTVRank() {
        return uiTVRank;
    }

    public TextView setUiTVRank(TextView uiTVRank) {
        this.uiTVRank = uiTVRank;
        return uiTVRank;
    }

    public TextView getUiTVReason() {
        return uiTVReason;
    }

    public TextView setUiTVReason(TextView uiTVReason) {
        this.uiTVReason = uiTVReason;
        return uiTVReason;
    }

    public TextView getUiTVScore() {
        return uiTVScore;
    }

    public TextView setUiTVScore(TextView uiTVScore) {
        this.uiTVScore = uiTVScore;
        return uiTVScore;
    }

    public TextView getUiTVPublisher() {
        return uiTVPublisher;
    }

    public TextView setUiTVPublisher(TextView uiTVPublisher) {
        this.uiTVPublisher = uiTVPublisher;
        return uiTVPublisher;
    }

    public TextView getUiTVFeat() {
        return uiTVFeat;
    }

    public TextView setUiTVFeat(TextView uiTVFeat) {
        this.uiTVFeat = uiTVFeat;
        return uiTVFeat;
    }

    public TextView getUiTVFeat2() {
        return uiTVFeat2;
    }

    public TextView setUiTVFeat2(TextView uiTVFeat2) {
        this.uiTVFeat2 = uiTVFeat2;
        return uiTVFeat2;
    }

    public ImageView getUiIVImg() {
        return uiIVImg;
    }

    public ImageView setUiIVImg(ImageView uiIVImg) {
        this.uiIVImg = uiIVImg;
        return uiIVImg;
    }

    public ImageButton getUiIBShareFb() {
        return uiIBShareFb;
    }

    public ImageButton setUiIBShareFb(ImageButton uiIBShareFb) {
        this.uiIBShareFb = uiIBShareFb;
        return uiIBShareFb;
    }

    public ImageButton getUiIBShareTwitter() {
        return uiIBShareTwitter;
    }

    public ImageButton setUiIBShareTwitter(ImageButton uiIBShareTwitter) {
        this.uiIBShareTwitter = uiIBShareTwitter;
        return uiIBShareTwitter;
    }

    public ImageButton getUiIBShareTumblr() {
        return uiIBShareTumblr;
    }

    public ImageButton setUiIBShareTumblr(ImageButton uiIBShareTumblr) {
        this.uiIBShareTumblr = uiIBShareTumblr;
        return uiIBShareTumblr;
    }

    public ImageButton getUiIBShareMore() {
        return uiIBShareMore;
    }

    public ImageButton setUiIBShareMore(ImageButton uiIBShareMore) {
        this.uiIBShareMore = uiIBShareMore;
        return uiIBShareMore;
    }

    public ImageButton setUiIBDislike(ImageButton uiIBDislike) {
        this.uiIBDislike = uiIBDislike;
        return uiIBDislike;
    }

    public ImageButton setUiIBLike(ImageButton uiIBLike) {
        this.uiIBLike = uiIBLike;
        return uiIBLike;
    }

    public ImageButton getUiIBDislike() {
        return uiIBDislike;
    }

    public ImageButton getUiIBLike() {
        return uiIBLike;
    }
}
