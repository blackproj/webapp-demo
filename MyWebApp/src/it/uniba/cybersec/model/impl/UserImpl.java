package it.uniba.cybersec.model.impl;

import java.io.InputStream;

import it.uniba.cybersec.model.User;

public class UserImpl implements User {
	private int id;
	private String username;
	private String name;
	private String surname;
	private InputStream image;	
	
	
	public UserImpl(int id, String username, String name, String surname, InputStream image) {
		this.id = id;
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.image = image;
	}
	
	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
	
	public String getNome() {
		return name;
	}
	
	public String getCognome() {
		return surname;
	}

	public InputStream getImmagine() {
		return image;
	}
}