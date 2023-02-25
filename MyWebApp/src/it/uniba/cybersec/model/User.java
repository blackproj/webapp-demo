package it.uniba.cybersec.model;

import java.io.InputStream;

public interface User {
	public int getId();
	public String getUsername();
	public String getNome();
	public String getCognome();
	public InputStream getImmagine();
}