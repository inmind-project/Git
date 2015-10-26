package com.yahoo.inmind.services.news.model.source;

import com.yahoo.inmind.services.news.model.vo.JsonItem;

import java.util.ArrayList;

public abstract class Source {

    public static String BUNDLE_RELOAD = "reload";

	protected String url = null;

	/**
     * Begin downloading data. Parse JsonUtil and fill the list with items.
     * 	This function will be executed in the background thread.
     * @param list list to be filled in
     * @messageId this is the id of the message from the thread that posts the request
    */
	public abstract void fetchData(ArrayList<JsonItem> list, int messageId);

}
