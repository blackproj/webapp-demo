package it.uniba.cybersec.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class PasswordHelper {
	private byte[] saltToSave;
	private byte[] hashToSave;
	
	/* Getter & setter
	*/
	public byte[] getSaltToSave() {
		return saltToSave;
	}
	
	public byte[] getHashToSave() {
		return hashToSave;
	}
	
	public void setSaltToSave(byte[] saltToSave) {
		this.saltToSave = saltToSave;
	}

	public void setHashToSave(byte[] hashToSave) {
		this.hashToSave = hashToSave;
	}

	/* 23) Cifra la pwd utente ricevuta in input e restituisce in output l'hash tra pwd in chiaro e sale randomico
	 
	  - Il metodo genera un sale randomico di 12 byte, provvede quindi a cifrare il sale attraverso la classe CipherHelper
	  - con il metodo encPwd, dopo si concatena la pwd utente in chiaro con il sale randomico generato e se ne calcola l'hash.
	  - Il valore di hash della computazione viene restituito in output per essere successivamente salvato all'interno del
	  - db mediante il metodo db.registerHash
	*/
	public void setPassword(byte[] pass, int id_usr) throws Exception {
		byte[] salt = generateSalt(12);
		CipherHelper ch = new CipherHelper();
		byte[] encryptedSalt = ch.encPwd(salt, id_usr);
		setSaltToSave(encryptedSalt);																	// La pwd dell'utente
		byte[] input = appendArrays(pass, salt);														// il sale e l'hash
		Arrays.fill(pass, (byte)0);																		// calcolato, in quanto
		Arrays.fill(salt, (byte)0);																		// dati sensibili
		MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");									// devono essere cancellati
		byte[] hashVal = msgDigest.digest(input);														// non appena vengono utilizzati
		Arrays.fill(input, (byte)0);	
		setHashToSave(hashVal);
	}
	
	/* 24) Decifra la pwd utente e restituisce in output l'hash della concatenazione tra pwd in chiaro e sale randomico
	 
	  - Il metodo riceve in input la pwd in chiaro dell'utente dal front-end, il sale dell'utente cifrato e immagazzinato nel db,
	  - l'hash dell'utente immagazzinato nel db e l'id utente. Si decifra il sale utente mediante la classe CipherHelper ed il
	  - metodo ch.decPwd per poi concatenare tale sale decifrato insieme alla pwd in chiaro dell'utente. Si calcola quindi l'hash
	  - di tale valore e lo si confronta con il valore di hash immagazzinato all'interno del db in fase di registrazione dell'utente.
	  - Se i due valori di hash sono uguali, l'utente è legittimo ed ha immesso la pwd corretta.
	  - Il metodo checkPassword restituisce un booleano in output, dove true se l'utente è legittimo, false altrimenti.
	*/
	public boolean checkPassword(byte[] pass, byte[] userSalt, byte[] userHash, int id_usr) throws Exception {
		CipherHelper ch = new CipherHelper();
		byte[] decipheredSalt = ch.decPwd(userSalt, id_usr);
		Arrays.fill(userSalt, (byte)0);																	// Il sale dell'utente
		byte[] input = appendArrays(pass, decipheredSalt);												// i valori di hash calcolati
		MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");									// così come la pwd utente
		byte[] hashVal1 = msgDigest.digest(input);														// in quanto dati sensibili
		Arrays.fill(pass, (byte)0);																		// devono essere cancellati
		Arrays.fill(input, (byte)0);																	// non appena vengono utilizzati
		boolean arraysEqual = Arrays.equals(hashVal1, userHash);
		Arrays.fill(userHash, (byte)0);	
		Arrays.fill(hashVal1, (byte)0);	
		return arraysEqual;
	}
	
	/* 25) Genera un sale randomico di dimensione n bit
	 
	  - Il metodo è molto simile al numero 21 CipherHelper.getRandomString(int n)
	*/
	protected static byte[] generateSalt(int n) throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		byte[] values = new byte[n];
		random.nextBytes(values);
		return values;
	}
	
	/* 26) Crea un nuovo array X, ricevuti in input due array A e B, concatenando B ad A
	*/
	protected byte[] appendArrays(byte[] first, byte[] second) {
		byte[] result = ArrayUtils.addAll(first, second);
		return result;
	}
 }	