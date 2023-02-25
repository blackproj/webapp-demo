package it.uniba.cybersec.model;

public interface Salt {
	public int getId();
	public byte[] getRandom();
}