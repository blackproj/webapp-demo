package it.uniba.cybersec.model.impl;

import java.io.InputStream;

import it.uniba.cybersec.model.Project;

public class ProjectImpl implements Project {
	private int id;
	private int id_usr;
	private InputStream proposta;	
	
	
	public ProjectImpl(int id, int id_usr, InputStream proposta) {
		this.id = id;
		this.id_usr = id_usr;
		this.proposta = proposta;
	}
	
	public int getId() {
		return id;
	}
	
	public int getId_usr() {
		return id_usr;
	}

	public InputStream getProposta() {
		return proposta;
	}
}