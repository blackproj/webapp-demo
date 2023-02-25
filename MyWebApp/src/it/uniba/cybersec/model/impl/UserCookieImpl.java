package it.uniba.cybersec.model.impl;

import it.uniba.cybersec.model.UserCookie;

public class UserCookieImpl implements UserCookie {
	private int id;
	private String random;
	
	
	public UserCookieImpl (int id, String random) {
		this.id = id;
		this.random = random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public int getId() {
		return id;
	}
	
	public String getRandom() {
		return random;
	}
}