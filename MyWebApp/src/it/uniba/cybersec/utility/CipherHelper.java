package it.uniba.cybersec.utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.jcip.annotations.NotThreadSafe;

	/* 18) Cifra il sale utente in chiaro e lo immagazzina nel magazzino delle chiavi (localizzato in una posizione sicura)

 	- Il metodo riceve in input il sale generato randomicamente attraverso la classe PasswordHelper e l'id utente.
 	- Innanzitutto si procede con istanziare l'oggetto KeyStore (ks) (il magazzino delle chiavi che dovrà essere stato
 	- precedentemente creato). Tale ks è lo stesso che è stato utilizzato per immagazzinare il certificato self-signed
 	- utilizzato per ottenere la connessione cifrata sicura HTTPS al server.
 	- Si procede con ottenere uno stream di byte dal file che rappresenta il ks e attraverso la direttiva ks.load
 	- si carica quindi il ks fornendo in input la password del ks per accedervi.
 	- Attraverso il metodo ProtectionParameter della classe KeyStore viene protetto il contenuto proprio del ks.
 	- Viene pertanto istanziato il cifrario AES attingendo dal package nativo javax.crypto. Si istanzia quindi un
 	- KeyGenerator, tale oggetto permetterà attraverso il suo metodo init di generare una chiave simmetrica di 128 bit.
 	- Questa chiave verrà utilizzata per immagazzinare la entry relativa all'utente n-esimo della web-app all'interno
 	- del magazzino delle chiavi. Ciascuna entry all'interno del magazzino delle chiavi (con la sola eccezione
 	- per il certificato self-signed) è composta da un doppietto chiave-valore dove per la chiave viene immagazzinato
 	- l'id utente, mentre il valore è rappresentato dalla chiave stessa generata dal metodo SecretKeyEntry della
 	- classe KeyStore. Viene quindi codificata la chiave segreta (skey) e si ottiene la sua rappresentazione in
 	- byte[] raw, a questo punto viene istanziato il cifrario in modalità ENCRYPT e la entry salvata all'interno del
 	- magazzino delle chiavi attraverso il metodo ks.store. 
 	- La cifratura vera e propria viene infine svolta dal metodo doFinal della classe cipher.
	 */

@NotThreadSafe
public final class CipherHelper {
	protected byte[] encPwd(byte[] salt, int id_usr) throws KeyStoreException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] pwdks = "keystorepassword".toCharArray();
			String path = "C:\\safe\\mysecurestore.jks";
			FileInputStream fis = new FileInputStream(path);
			ks.load(fis, pwdks);
			KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(pwdks);
			
			Cipher cipher = Cipher.getInstance("AES");
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();
			KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(skey);
			String alias = String.valueOf(id_usr);
			ks.setEntry(alias, secretKeyEntry, protectionParam);
			
			byte[] raw = skey.getEncoded();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Arrays.fill(raw, (byte)0);																	// La chiave randomica 
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);													// in quanto dato sensibile
																										// deve essere cancellata
			FileOutputStream fos = new FileOutputStream("C:\\safe\\mysecurestore.jks");					// non appena viene utilizzata
			ks.store(fos, pwdks);																		// Inoltre la chiave per
			clearArray(pwdks);																			// accedere al KeyStore
																										// è anch'essa un dato sensibile
		    byte[] encryptedSalt = cipher.doFinal(salt);												// e deve essere cancellata
			return encryptedSalt;																		// non appena viene utilizzata
			
		} catch(Throwable throwable) {
			throwable.printStackTrace(); 
		}
		return null;	
	}

	/* 19) 	Decifra il sale utente cifrato (acquisito dal db.tabella_sali) e restituisce in output il sale decifrato
	*/
	protected byte[] decPwd(byte[] encryptedSalt, int id_usr) throws KeyStoreException {
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] pwdks = "keystorepassword".toCharArray();
			String path = "C:\\safe\\mysecurestore.jks";
			FileInputStream fis = new FileInputStream(path);
			ks.load(fis, pwdks);
			KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(pwdks);
			clearArray(pwdks);																			// La chiave per accedere
																										// al KeyStore in quanto
			String alias = String.valueOf(id_usr);														// dato sensibile deve
			KeyStore.SecretKeyEntry secretKeyEnt = (KeyStore.SecretKeyEntry)ks.getEntry(alias, protectionParam);
			SecretKey mysecretKey = secretKeyEnt.getSecretKey();										// essere cancellata non
			byte[] raw = mysecretKey.getEncoded();														// appena viene utilizzata
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");										// Inoltre la chiave randomica
			Arrays.fill(raw, (byte)0);																	// è anch'essa un dato sensibile
																										// e deve essere cancellata
			Cipher cipher = Cipher.getInstance("AES");													// non appena viene utilizzata
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			
			byte[] decipheredSalt = cipher.doFinal(encryptedSalt);
			return decipheredSalt;
		} catch(Throwable throwable) {
			throwable.printStackTrace();
		}
		return null;
	}
	
	/* 20) 	Cifra il valore del cookie utente e lo immagazzina nel magazzino delle chiavi (localizzato in una posizione sicura)
	  
	  - Il metodo diversamente dal 18, concatena una stringa "ck" per distinguere la entry immessa da questo metodo all'interno
	  - del magazzino delle chiavi. Inoltre è necessario codificare il valore del cookie attraverso la codifica UTF-8
	*/
	public static String encCk(String value, int id_usr) throws KeyStoreException {
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] pwdks = "keystorepassword".toCharArray();
			String path = "C:\\safe\\mysecurestore.jks";
			FileInputStream fis = new FileInputStream(path);
			ks.load(fis, pwdks);
			KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(pwdks);
				
			Cipher cipher = Cipher.getInstance("AES");
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();
			KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(skey);
			String alias = String.valueOf(id_usr) + "ck";
			ks.setEntry(alias, secretKeyEntry, protectionParam);
				
			byte[] raw = skey.getEncoded();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Arrays.fill(raw, (byte)0);																	// La chiave randomica
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);													// in quanto dato sensibile
																										// deve essere cancellata
			FileOutputStream fos = new FileOutputStream("C:\\safe\\mysecurestore.jks");					// non appena viene utilizzata
			ks.store(fos, pwdks);																		// Inoltre la chiave per
			clearArray(pwdks);																			// accedere al KeyStore
																										// è anch'essa un dato sensibile
			byte[] encoded = value.getBytes("UTF8");													// e deve essere cancellata non
			byte[] encryptedValue = cipher.doFinal(encoded);											// appena viene utilizzata
			String encryptedSaltStr = new String(encryptedValue, StandardCharsets.UTF_8);
			Arrays.fill(encoded, (byte)0);																// Il valore codificato e cifrato
			Arrays.fill(encryptedValue, (byte)0);														// del cookie, così come il valore
			return encryptedSaltStr;																	// cifrato del cookie stesso
																										// sono dati sensibili e devono
		} catch(Throwable throwable) {																	// essere cancellati non appena
			throwable.printStackTrace(); 																// vengono utilizzati
		}
		return null;
	}
	
	/* 21) Genera una stringa randomica di dimensione n ricevuta in input come parametro
	 
	  - Il metodo istanzia un oggetto SecureRandom, come generatore di numeri casuali pseudo randomico.
	  - Viene eseguita la codifica in base64 e si estrae la relativa stringa tokenizzata attraverso una serie
	  - di numeri generati casualmente mediante il metodo nextBytes della classe SecureRandom.
	*/
	public static String getRandomString(int n) throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	    byte values[] = new byte[n];
	    random.nextBytes(values);
	    Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	    String token = encoder.encodeToString(values);
	    return token;
	}
	
	/* 22) Scrive uno zero su ciascuna entry dell'array, per svuotare l'array contenente dati sensibili
	*/
	private static void clearArray(char[] a) {
		for (int i = 0; i < a.length; ++i)
			a[i] = 0;
	}
}