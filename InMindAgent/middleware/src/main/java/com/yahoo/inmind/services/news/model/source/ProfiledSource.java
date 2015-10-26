package com.yahoo.inmind.services.news.model.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.yahoo.inmind.services.news.model.slingstone.UserProfile;
import com.yahoo.inmind.services.news.model.vo.JsonItem;

public abstract class ProfiledSource extends Source{	
	protected UserProfile mProfile;
	private ArrayList<Handler> mProfileHandlers = new ArrayList<Handler>();
	private ArrayList<Integer> mProfileMsgs = new ArrayList<Integer>();
	private ArrayList<Object> mProfileObjs = new ArrayList<Object>();
	
	@Override
	public void fetchData(ArrayList<JsonItem> list, int messageId)
	{
		//For notifying registered handlers of the readiness of the profile
		if (mProfileHandlers.size() != 0)
		{
			for (int i = 0 ; i < mProfileHandlers.size() ; i++)
			{
				Message msg = new Message();
				msg.what = mProfileMsgs.get(i);
				msg.obj = mProfileObjs.get(i);
				mProfileHandlers.get(i).sendMessage(msg);
			}
		}
	}
	
	//Register this function with a UI handler, so performProfileCustomization() will be called when the profile is ready.
	public void registerProfileReadyHandler(Handler handler, int what, Object obj)
	{
		mProfileHandlers.add(handler);
		mProfileMsgs.add((Integer) what);
		mProfileObjs.add(obj);
	}
	
	/** 
	 * 	Override this function to populate additional items.
	 *  This is used for more time-consuming process to populate items.
	 *  This function will be invoked on a background thread.
	 * */
	public void generateItemsFromProfile(){
		
	}
	
	/**	
	 * 	Override this function to customize GUI based on the user profile of this Source.
	 *  This function will be called after the user profile is retrieved.
	 * 	DrawerItems or other GUI components could be added to the UI from here according to the user profile.	 
	 *  This function will be invoked on a UI thread.
	 */
	public void showExtendedOptions() {
		
	}
	
	//This is a helper function.
	//Copy all of the key-value pairs under "jobjUser/name" to the Profile object.
	protected void fillProfile(JSONObject jobjUser, String name, HashMap<String, String> prof) {
        if( prof != null ) {
            JSONObject jobjCapWiki = (JSONObject) jobjUser.get(name);
            //Always assuming the values are strings, so we can handle the case when it is really a string.
            for (Entry<String, String> ent : (Set<Entry<String, String>>) jobjCapWiki.entrySet()) {
                prof.put(ent.getKey(), String.valueOf(ent.getValue()));
            }
        }
	}

	
	//Use to add filters to the current Source, e.g. appending parameters to the existing URL for JsonUtil retrieval.
	protected ProfiledSource filter(String string) {
		url += string;
		return this;
	}
}
