package org.codeforamerica.shiba.pages.rest;

import com.google.gson.JsonObject;

public interface RestClient {
	
	public void send(JsonObject appJsonObject);
	
	public Boolean isEnabled();
}
