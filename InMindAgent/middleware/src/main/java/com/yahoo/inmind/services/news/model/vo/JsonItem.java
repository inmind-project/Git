package com.yahoo.inmind.services.news.model.vo;

public class JsonItem extends ValueObject {
	private String json = null;

	public JsonItem()
	{
		json = null;
	}

	public String getRawString()
	{
        return json;
	}
}
