package com.yahoo.inmind.services.news.model.slingstone;

import java.util.HashMap;

public class UserProfile {
    HashMap<String, String> demographics;
    HashMap<String, String> mCapWiki;
    HashMap<String, String> mPosDecWiki;
    HashMap<String, String> mPosDecYct;
    HashMap<String, String> mCapYct;
    HashMap<String, String> mNegDecWiki;
    HashMap<String, String> mNegDecYct;
    HashMap<String, String> mFbWiki;
    HashMap<String, String> mFbYct;
    HashMap<String, String> mNegInfWiki;
    HashMap<String, String> mNegInfYct;
    HashMap<String, String> mUserProp;

	public UserProfile(){
        demographics = new HashMap<>();
		mCapWiki = new HashMap<>();
		mPosDecWiki = new HashMap<>();
		mPosDecYct = new HashMap<>();
		mCapYct = new HashMap<>();
        mNegDecWiki = new HashMap<>();
        mNegDecYct = new HashMap<>();
        mFbWiki = new HashMap<>();
        mFbYct = new HashMap<>();
        mNegInfWiki = new HashMap<>();
        mNegInfYct = new HashMap<>();
        mUserProp = new HashMap<>();
	}

    public HashMap<String, String> getDemographics() {
        return demographics;
    }

    public void setDemographics(HashMap<String, String> demographics) {
        this.demographics = demographics;
    }

    public HashMap<String, String> getmCapWiki() {
        return mCapWiki;
    }

    public void setmCapWiki(HashMap<String, String> mCapWiki) {
        this.mCapWiki = mCapWiki;
    }

    public HashMap<String, String> getmPosDecWiki() {
        return mPosDecWiki;
    }

    public void setmPosDecWiki(HashMap<String, String> mPosDecWiki) {
        this.mPosDecWiki = mPosDecWiki;
    }

    public HashMap<String, String> getmPosDecYct() {
        return mPosDecYct;
    }

    public void setmPosDecYct(HashMap<String, String> mPosDecYct) {
        this.mPosDecYct = mPosDecYct;
    }

    public HashMap<String, String> getmCapYct() {
        return mCapYct;
    }

    public void setmCapYct(HashMap<String, String> mCapYct) {
        this.mCapYct = mCapYct;
    }

    public HashMap<String, String> getmNegDecWiki() {
        return mNegDecWiki;
    }

    public void setmNegDecWiki(HashMap<String, String> mNegDecWiki) {
        this.mNegDecWiki = mNegDecWiki;
    }

    public HashMap<String, String> getmNegDecYct() {
        return mNegDecYct;
    }

    public void setmNegDecYct(HashMap<String, String> mNegDecYct) {
        this.mNegDecYct = mNegDecYct;
    }

    public HashMap<String, String> getmFbWiki() {
        return mFbWiki;
    }

    public void setmFbWiki(HashMap<String, String> mFbWiki) {
        this.mFbWiki = mFbWiki;
    }

    public HashMap<String, String> getmFbYct() {
        return mFbYct;
    }

    public void setmFbYct(HashMap<String, String> mFbYct) {
        this.mFbYct = mFbYct;
    }

    public HashMap<String, String> getmNegInfWiki() {
        return mNegInfWiki;
    }

    public void setmNegInfWiki(HashMap<String, String> mNegInfWiki) {
        this.mNegInfWiki = mNegInfWiki;
    }

    public HashMap<String, String> getmNegInfYct() {
        return mNegInfYct;
    }

    public void setmNegInfYct(HashMap<String, String> mNegInfYct) {
        this.mNegInfYct = mNegInfYct;
    }

    public HashMap<String, String> getmUserProp() {
        return mUserProp;
    }

    public void setmUserProp(HashMap<String, String> mUserProp) {
        this.mUserProp = mUserProp;
    }
}
