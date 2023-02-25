package it.uniba.cybersec.database;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import java.util.List;

import it.uniba.cybersec.model.Hash;
import it.uniba.cybersec.model.Project;
import it.uniba.cybersec.model.Salt;
import it.uniba.cybersec.model.User;
import it.uniba.cybersec.model.impl.HashImpl;
import it.uniba.cybersec.model.impl.ProjectImpl;
import it.uniba.cybersec.model.impl.SaltImpl;
import it.uniba.cybersec.model.impl.UserImpl;
import it.uniba.cybersec.utility.PasswordHelper;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DatabaseManager {
	private String url;	

	public DatabaseManager() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("it.uniba.cybersec.database.configuration");
			url = bundle.getString("database.url");
			String driver = bundle.getString("database.driver");
			Class.forName(driver);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	/* 	I metodi che agiscono nel db.cookie sono stati programmati intenzionalmente con valore di ritorno void poichè
	  	la complessità della Servlet Login (GetUserLogin) che li implementa era già elevata e non si voleva appesantire
	  	ulteriormente il codice li presente.
	*/
	
	/* 1) Inserisce un nuovo utente -con username univoco- all'interno del database + avatar profilo
	 
	   	  - La verifica dell'username univoco viene fatta attraverso il metodo 
	   	  boolean getUsernameLogin(String username) in fondo a questa classe
	   	  
	   	  N.B -> L'utente add_user utilizzato per collegarsi al database ha
	   	  esclusivamente il permesso di fare inserimenti (INSERT) e viene utilizzato
	   	  per eseguire inserimenti solo nella tabella utenti
	*/
	public User registerUser(String username, String nome, String cognome, InputStream immagine) throws SQLException {
		if(username == null || username.length() == 0 || username.length() > 30)						// Controlli sui parametri
			throw new IllegalArgumentException("Username non valido");									// inseriti dall'utente
		
		if(cognome == null || cognome.length() == 0 || cognome.length() > 25)
			throw new IllegalArgumentException("Cognome non valido");
		
		if(nome == null || nome.length() == 0 || nome.length() > 25)
			throw new IllegalArgumentException("Nome non valido");
		
		try (
			Connection conn = DriverManager.getConnection(url, "add_user", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO usr (username, name, surname, image) VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);													// La direttiva
		) {																								// RETURN_GENERATED_KEYS
			stmt.setString(1, username);																// permette di auto incrementare
			stmt.setString(2, nome);																	// l'id all'interno del database
			stmt.setString(3, cognome);
			stmt.setBlob(4, immagine);
			stmt.execute();
			try (
				ResultSet rs = stmt.getGeneratedKeys();													// vedi sopra
			) {
				rs.next();
				int id = rs.getInt(1);
				
				return new UserImpl(id, username, nome, cognome, immagine);								// Restituisco un oggetto UserImpl
			} catch (SQLException exception) {															
				throw exception;
		    }
		} catch(SQLException exception) {
			exception.printStackTrace();
			throw exception;
		}
	}
	
	/* 2) Inserisce il sale randomico e cifrato relativo alla pwd utente all'interno del database
	 
 	  	  - La generazione del sale randomico avviene all'interno della classe PasswordHelper
 	  	  - attraverso il metodo byte[] generateSalt(int n)
 	  	  - La cifratura viene eseguita all'interno della classe PasswordHelper
 	  	  - attraverso il metodo setPassword istanziando un'altra classe CipherHelper
 	  	  - utilizzando il suo metodo ch.encPwd(salt, id_usr)
 	  	  - tale metodo prende in input il sale randomico non cifrato e l'id utente presente nel database
 	  	   	  
	   	  N.B -> L'utente add_salt utilizzato per collegarsi al database ha
	   	  esclusivamente il permesso di fare inserimenti (INSERT) e viene utilizzato
	   	  per eseguire inserimenti solo nella tabella dei sali
 	*/
	public Salt registerSalt(int id_usr, byte[] rand) throws SQLException {
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");
		
		if(rand == null || rand.length < 0)
			throw new IllegalArgumentException("Sale non valido");
		
		try (
			Connection conn = DriverManager.getConnection(url, "add_salt", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO salt (id_usr, rand) VALUES (?, ?)");
		) {
			stmt.setInt(1, id_usr);
			stmt.setBytes(2, rand);																		
			Arrays.fill(rand, (byte)0);																	// Il sale dell'utente
			stmt.execute();																				// in quanto dato sensibile
																										// deve essere cancellato
			return new SaltImpl(id_usr, rand);															// non appena viene utilizzato
		} catch (SQLException exception) {
			throw exception;
		}
	}
	
	/* 3) Inserisce l'hash calcolato dalla concatenazione di pwd utente e sale randomico cifrato ut all'interno database
	 
 	  	  - Il calcolo dell'hash avviene all'interno della classe PasswordHelper
 	  	  - attraverso il metodo digest della classe MessageDigest (java.security)
 	   	  
 	    N.B -> L'utente add_hash utilizzato per collegarsi al database ha
 	  	esclusivamente il permesso di fare inserimenti (INSERT) e viene utilizzato
 	  	per eseguire inserimenti solo nella tabella dell'hash utenti
	*/
	public Hash registerHash(int id_usr, byte[] hashedpw) throws SQLException {
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");
		
		if(hashedpw == null || hashedpw.length < 0)
			throw new IllegalArgumentException("Password non valida");
		
		try (
			Connection conn = DriverManager.getConnection(url, "add_hash", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO hash (id_usr, pwd) VALUES (?, ?)");
		) {
			stmt.setInt(1, id_usr);
			stmt.setBytes(2, hashedpw);
			Arrays.fill(hashedpw, (byte)0);																// L'hash dell'utente
			stmt.execute();																				// in quanto dato sensibile
																										// deve essere cancellato
			return new HashImpl(id_usr, hashedpw);														// non appena viene utilizzato
		} catch (SQLException exception) {
			throw exception;
		}
	}

	/* 4) Verifica se l'utente che esegue il login sia un utente registrato nel db e fornisca la pwd corretta
	   
 	  - Vengono effettuati diverse query al db per ottenere il sale dell'utente e l'hash dell'utente
 	  - una volta ottenuti, attraverso la classe PasswordHelper e il metodo checkPassword della stessa
 	  - viene istanziata la classe CipherHelper che attraverso il suo metodo decPwd(userSalt, id_usr)
  	  - prende in input il sale dell'utente cifrato e l'id dell'utente ed esegue la decifratura del sale
  	  - successivamente all'interno del metodo checkPassword si concatena la pwd che l'utente ha fornito
  	  - al front-end con il sale decifrato. Viene calcolato l'hash della pwd fornita e il sale decifrato
  	  - e questo viene confrontato con il valore di hash che si trova immagazzinato nel db.
  	  - Se i due valori di hash sono uguali, allora l'utente che esegue il login nel front-end è legittimo
  	  - e questo metodo checkLogin restituisce true.
	 */
	public boolean checkLogin(String username, byte[] password) throws Exception, SQLException {
		boolean userAccepted = false;
		if(username == null || username.length() == 0 || username.length() > 30)
			throw new IllegalArgumentException("Username non valido");
		if(password == null || password.length < 0)
			throw new IllegalArgumentException("Password non valida");
		
		int idUser = getUserId(username); 
		byte[] mysalt = getUserSalt(idUser);			
		byte[] myhash = getUserHash(idUser);
		PasswordHelper tmp = new PasswordHelper();
		userAccepted = tmp.checkPassword(password, mysalt, myhash, idUser);								// La password in chiaro
		Arrays.fill(password, (byte)0);																	// che l'utente ha inserito
		Arrays.fill(mysalt, (byte)0);																	// nel front-end, il sale cifrato
		Arrays.fill(myhash, (byte)0);																	// e l'hash immagazzinato nel db,
		tmp = null;																						// sono tutti dati sensibili
																										// e devono essere cancellati
		return userAccepted;																			// non appena vengono utilizzati
	}																	
	
	/* 5) Inserisce una entry nel db per tenere traccia del cookie di un utente, la prima volta che esso preme "remember me"
 	  
 	  - Il metodo riceve in input, l'username, una stringa randomica che corrisponde al "value" del cookie
 	  - ed un long che corrisponde alla data ed ora di sistema nel momento in cui viene lanciato
 	  - il metodo a cui vengono sommati 15 minuti. Se per esempio è il giorno 05-08-2022 e sono le 17 e viene
 	  - lanciato questo metodo, allora expireCk conterrà la data 05-08-2022 @ 17.15 (espressa in millisecondi)
  	  - Tale valore expireCk verrà usato successivamente in fase di login per verificare o meno se il cookie risulta
  	  - scaduto e se lo sarà, verrà fatta una query sul db per eliminare la suddetta entry e verrà settato il valore
  	  - del cookie a zero nella sessione dell'utente.
  	  
   	    N.B -> L'utente add_cookie utilizzato per collegarsi al database ha
 		esclusivamente il permesso di fare inserimenti (INSERT) e viene utilizzato
 		per eseguire inserimenti solo nella tabella dei cookie
	 */
	public void mapUserForRememberMe(String username, String newRandom, long expireCk) throws SQLException {
		int id_usr = getUserId(username);
		if(id_usr == 0 || id_usr < 0) 
			throw new IllegalArgumentException("ID utente non valido");
		
		if(username == null || username.length() == 0 || username.length() > 30)
			throw new IllegalArgumentException("Username non valido");
		
		if(newRandom == null || newRandom.length() < 0)
			throw new IllegalArgumentException("Stringa random del cookie non valida");
		
		try (
			Connection conn = DriverManager.getConnection(url, "add_cookie", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO cookie (id_usr, rand, expire) VALUES (?, ?, ?)");
		) {
			stmt.setInt(1, id_usr);
			stmt.setString(2, newRandom);
			stmt.setLong(3, expireCk);
			stmt.execute();
		} catch (SQLException exception) {
			throw exception;
		}
	}
	
	/* 6) Ricerca il cookie simbolico che è stato inserito nel db dal metodo mapUserForRememberMe (il metodo numero 5)
	  
	  - Il metodo restituisce un booleano per verificare che effettivamente l'utente abbia o meno un cookie simbolico
	  
 	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public boolean retrieveUserCookie(String username) throws SQLException {
		int id_usr = getUserId(username);
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");
		
		boolean status = false;
		
		if(username == null || username.length() == 0 || username.length() > 30)
			throw new IllegalArgumentException("Il nome utente non è valido");

		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cookie WHERE id_usr = ?");
		) {
			stmt.setInt(1, id_usr);
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				status = rs.next();
			} catch (SQLException exception) {
				exception.printStackTrace();
				throw exception; 
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
			return status;
	}
	
	/* 7) Restituisce il tempo di scadenza del cookie utente (simbolico), immagazzinato nel db
	  	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public long retrieveTimeCookie(String username) throws SQLException {
		int id_usr = getUserId(username);
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");
		
		long tmp = 0;
		
		if(username == null || username.length() == 0 || username.length() > 30)
			throw new IllegalArgumentException("Il nome utente non è valido");

		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cookie where id_usr = ?");
		) {
			stmt.setInt(1, id_usr);
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				if(rs.next()) {
					long timeExpire = rs.getLong("expire");
					tmp = timeExpire;
					return timeExpire;
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
				throw exception;	
			}
		} catch (SQLException exception) {
			throw exception;
		}
		return tmp;
	}
	
	/* 8) Aggiorna il valore del cookie simbolico all'interno del db
	  
	  - Il metodo riceve in input, l'username, una stringa randomica che corrisponde al "value" del cookie
 	  - ed un long che corrisponde alla data ed ora di scadenza del cookie. Il suddetto metodo viene chiamato
 	  - sempre dalla Servlet di Login utente (GetUserLogin) nel momento in cui esso seleziona la casella
 	  - "remember me", in questo modo il suo cookie viene prorogato di ulteriori 15 minuti.
	  
	    N.B -> L'utente upd_cookie utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare UPDATE e viene utilizzato per
		aggiornare la entry presente nel db
	 */
    public void updateCookie(String username, String newRandom, long expireCk) throws SQLException {
        int id_usr = getUserId(username);
        if(id_usr == 0 || id_usr < 0) 
			throw new IllegalArgumentException("ID utente non valido");
		
		if(newRandom == null || newRandom.length() < 0)
			throw new IllegalArgumentException("Stringa random del cookie non valida");
		
		try (
			Connection conn = DriverManager.getConnection(url, "upd_cookie", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("UPDATE cookie SET rand = ?, expire = ? where id_usr = ? ");
		) {
			stmt.setString(1, newRandom);
			stmt.setLong(2, expireCk);
			stmt.setInt(3, id_usr);
			stmt.execute();			
		} catch (SQLException exception) {
			throw exception;
		}
	}
	
	/* 9) Cancella la entry relativa al cookie simbolico dell'utente dal db
	  
	  - Il metodo cancella la entry relativa al cookie simbolico dell'utente se il valore di "expireCk" è maggiore
	  - di 15 minuti rispetto alla data di sistema attuale (espressa in millisecondi). Se per esempio è il giorno 05-08-2022 
	  - e sono le 17.46, mentre il valore di expireCk è 05-08-2022 @ 17.30 allora viene lanciato questo metodo deleteCookie.
	  
	    N.B -> L'utente del_cookie utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare DELETE e viene utilizzato per
		cancellare le entry presenti nel db
	 */
	public void deleteCookie(String username) throws SQLException {
		int id_usr = getUserId(username);
		try(
			Connection conn = DriverManager.getConnection(url, "del_cookie", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM cookie WHERE id_usr = ?");
		) {
			stmt.setInt(1, id_usr);
			
			stmt.execute();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;
		}
	}
	
	/* 10) Ottiene l'username utente dato il suo id immagazzinato nel db
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public int getUserId(String username) throws SQLException {
		int gotId = 0;
		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT id FROM usr WHERE username = ?");
		) {
			stmt.setString(1, username);
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				rs.next();
				int id = rs.getInt(1);
				gotId = id;
			} catch (SQLException exception) {
				exception.printStackTrace();
				throw exception; 
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
			return gotId;
	}
	
	/* 11) Verifica che l'utente che si sta registrando fornisca un username univoco NON presente nel db
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public boolean getUsernameLogin(String username) throws SQLException {
		boolean status = false;
		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT username FROM usr WHERE USERNAME = ?");
		) {
			stmt.setString(1, username);
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				status = rs.next();
			} catch (SQLException exception) {
				exception.printStackTrace();
				throw exception; 
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
			return status;
	}
	
	/* 12) Ritrova il sale cifrato dell'utente all'interno del db, attraverso il suo id
	  
	  - Il metodo esegue JOIN all'interno della query per essere maggiormente sicuri
	  - della correttezza del risultato della query
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public byte[] getUserSalt(int id) throws SQLException {
		byte[] result = null;
		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT rand FROM salt s JOIN hash h ON s.id_usr = h.id_usr where s.id_usr = ?");
		) {
			stmt.setInt(1, id);
			try (
				ResultSet rs = stmt.executeQuery();
			) { 
				if(rs.next()) {
					Blob blob = rs.getBlob("rand");
					byte[] userSalt = blob.getBytes(1, (int)blob.length());
					result = userSalt;																	
					blob.free();																		
				}																						
			}																							
		} catch (SQLException exception) {																
			exception.printStackTrace();
			throw exception;	
		}
		return result;
	}
	
	/* 13) Ritrova l'hash dell'utente all'interno del db, attraverso il suo id
	  
	  - Il metodo esegue JOIN all'interno della query per essere maggiormente sicuri
	  - della correttezza del risultato della query
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public byte[] getUserHash(int id) throws SQLException {
		byte[] result = null;
		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT pwd FROM hash h JOIN salt s ON h.id_usr = s.id_usr WHERE s.id_usr = ?");
		) {
			stmt.setInt(1, id);
			try (
				ResultSet rs = stmt.executeQuery();
			) { 
				if(rs.next()) {
					Blob blob = rs.getBlob("pwd");														
					byte[] userHash = blob.getBytes(1, (int)blob.length());								
					result = userHash;																	
					blob.free();																		
				}																						
			}	
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
		return result;
	}

	/* 14) Ritrova l'immagine avatar dell'utente all'interno del db, attraverso il suo id
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public byte[] getUserImage(int id) throws SQLException {
		byte[] result = null;
		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT image FROM usr WHERE id = ?");
		) {
			stmt.setInt(1, id);
			try (
				ResultSet rs = stmt.executeQuery();
			) { 
				if(rs.next()) {
					Blob blob = rs.getBlob("IMAGE");
					byte[] imageData = blob.getBytes(1, (int)blob.length());
					blob.free();
					result = imageData;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
		return result;
	}
	
	/* 15) Inserisce una proposta progettuale plain-text all'interno del db
	  
	  - Il metodo restituisce un booleano e riceve in input, l'username dell'utente ed un InputStream 
	  - ovvero un flusso di byte, rappresentanti il file txt che l'utente prova a caricare. 
	  - Il file in questione viene precedentemente analizzato attraverso la libreria Apache Tika 
	  - richiamata nella classe UploadAction mediante la funzione booleana
	  - checkMetadataText(InputStream is) e successivamente ne viene fatto il parse ed
	  - eventualmente il sanity checking nella stessa classe attraverso il metodo 
	  - extractContentTextAndStore(InputStream stream, String outputFilename).
	  - Se il file immesso dall'utente è effettivamente un txt, viene analizzato il suo
	  - contenuto, eventualmente eseguito sanity checking e quindi immagazzinato nel db.
	  
	  - Tale metodo non dovrebbe contribuire a generare una TOCTOU poichè lo stream del file
	  - viene aperto una sola volta all'interno della Servlet di caricamento proposta (FileUpload)
	  - ed è per questo motivo che questa situazione non viene gestita.
	  
   	    N.B -> L'utente add_proposal utilizzato per collegarsi al database ha
 		esclusivamente il permesso di fare inserimenti (INSERT) e viene utilizzato
 		per eseguire inserimenti solo nella tabella dei progetti
	 */
	public boolean addProject(String username, InputStream project) throws SQLException {
		int id_usr = getUserId(username);
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");

		boolean status = false;
		
		if(username == null || username.length() == 0 || username.length() > 30)
			throw new IllegalArgumentException("Il nome utente non è valido");

		try (
			Connection conn = DriverManager.getConnection(url, "add_proposal", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO project (id_usr, file) VALUES (?, ?)",
					Statement.RETURN_GENERATED_KEYS);													// La direttiva
		) {																								// RETURN_GENERATED_KEYS
			stmt.setInt(1, id_usr);																		// permette di auto incrementare
			stmt.setBlob(2, project);																	// l'id all'interno del database
			stmt.execute();
			try (
					ResultSet rs = stmt.getGeneratedKeys();												// vedi sopra
				) {
					rs.next();					
				} catch (SQLException exception) {
					throw exception;
			    }
			status = true;
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
			return status;
	}
	
	/* 16) Ottiene la lista di tutti i progetti salvati da tutti gli utenti nel db in ordine crescente di id utente
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public List<Project> getAllProject() throws SQLException {					
		List<Project> result = new ArrayList<Project>();
		
		try(
		    Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM project ORDER BY id_usr");
		) {
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				while(rs.next()) {
					int id = rs.getInt("id");
					int id_usr = rs.getInt("id_usr");
					Blob blob = rs.getBlob("file");
					byte[] fileData = blob.getBytes(1, (int)blob.length());
					blob.free();
					InputStream fileIs = new ByteArrayInputStream(fileData);	
					Project project = new ProjectImpl(id, id_usr, fileIs); 
					result.add(project);
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
				throw exception;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;
		}
		return result;
	}
	
	/* 17) Ritrova il progetto dell'utente all'interno del db, attraverso il suo id utente
	  
	    N.B -> L'utente show utilizzato per collegarsi al database ha
		esclusivamente il permesso di fare SELECT e viene utilizzato
		per eseguire query per interpellare il db
	 */
	public String getUserProject(int id_usr) throws SQLException {
		if(id_usr == 0 || id_usr < 0)
			throw new IllegalArgumentException("ID_usr non valido");
		String result = null;

		try (
			Connection conn = DriverManager.getConnection(url, "show", "safehome1!");
			PreparedStatement stmt = conn.prepareStatement("SELECT file FROM project WHERE id_usr = ?");
		) {
			stmt.setInt(1, id_usr);
			try (
				ResultSet rs = stmt.executeQuery();
			) { 
				if(rs.next()) {
					Blob blob = rs.getBlob("file");
					String userFile = new String(blob.getBytes(1l, (int) blob.length()));
					blob.free();
					result = userFile;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;	
		}
		return result;
	}
}