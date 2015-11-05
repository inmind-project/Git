package com.yahoo.inmind.services.news.model.i13n;

import com.yahoo.inmind.services.news.model.vo.ValueObject;

import java.util.Date;

public class UUIDEvent extends ValueObject {
	public String userid;
	public String uuids;
	public Date time;

	public UUIDEvent(String currentUserName, String uuidStr) {
		userid = currentUserName;
		uuids = uuidStr;
		this.time = new Date();
	}	
}
