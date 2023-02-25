package it.uniba.cybersec.model.impl;

import java.util.Arrays;

import it.uniba.cybersec.model.Salt;

public class SaltImpl implements Salt {
	private int id_usr;
	private byte[] random;
	

	public SaltImpl(int id_usr, byte[] random) {
		this.id_usr = id_usr;
		this.random = random;
		Arrays.fill(this.random, (byte)0);
	}
	
	public int getId() {
		return id_usr;
	}
	
	public byte[] getRandom() {
		return random;
	}
}