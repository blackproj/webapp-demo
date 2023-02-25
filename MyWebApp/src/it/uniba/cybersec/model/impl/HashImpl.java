package it.uniba.cybersec.model.impl;

import java.util.Arrays;

import it.uniba.cybersec.model.Hash;

public class HashImpl implements Hash {
	private int id;
	private byte[] pwd;
	
	
	public HashImpl (int id, byte[] pwd) {
		this.id = id;
		this.pwd = pwd;
		Arrays.fill(this.pwd, (byte)0);
	}
	
	public int getId() {
		return id;
	}
	
	public byte[] getPassword() {
		return pwd;
	}
}