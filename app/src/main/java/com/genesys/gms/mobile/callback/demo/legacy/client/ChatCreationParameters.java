package com.genesys.gms.mobile.callback.demo.legacy.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

public class ChatCreationParameters {
	public String firstName;
	public String lastName;
	public String userDisplayName;
	public String email;
	public String subject;
	public boolean receiveCometEvents = true;
	public List<NameValuePair> misc = new ArrayList<NameValuePair>();
}
