package it.uniba.cybersec.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import java.util.regex.Matcher; 
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.uniba.cybersec.database.DatabaseManager;
import it.uniba.cybersec.utility.PasswordHelper;
import it.uniba.cybersec.utility.UploadAction;
import net.jcip.annotations.ThreadSafe;

	/* 35) Servlet che esegue la registrazione di un nuovo utente all'interno della web-app

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) La direttiva @MultipartConfig impedisce all'utente di eseguire upload di file immagine molto grandi
	  D) La routine prevede innanzitutto la verifica del nome utente scelto dall'utente che esegue la
	     registrazione nella web-app andando ad eseguire una query all'interno del db.usr poichè il nome
	     utente deve essere univoco, questa verifica mira ad evitare inconsistenza all'interno del db.usr 
	     dovuta a due utenti con lo stesso username. Se l'utente quindi immette un nome utente già utilizzato
	     allora esso verrà redirettato alla pagina "register_ko_user.html". Successivamente si controlla
		 attraverso il metodo UploadAction.checkMetadataImage se l'immagine che l'utente intende utilizzare
		 è effettivamente un file immagine e risulta tra i formati consentiti dalle specifiche quindi
		 unicamente *png, *jpg, *jpeg, la verifica viene eseguita attraverso i metadati del file immagine.
		 Se l'utente sottomette un formato immagine non consentito oppure cerca di eseguire upload di un
		 altro tipo di file per esempio da parte di un avversario, il parser restituisce un errore e l'utente
		 viene redirettato alla pagina "image_ko.html". Inoltre viene eseguita una verifica sul nome utente
		 poichè per cercare di evitare inconsistenza dovuta a caratteri speciali immessi all'interno del campo
		 nome utente, si verifica tale username che non contenga i seguenti caratteri speciali:
		 <>%?!^+*ç~`{}°#§;,:|£%&/()=$" se l'utente inserisce uno dei seguenti caratteri ed invia i dati alla
		 servlet, viene redirettato alla pagina "error.html". L'utente in fase di registrazione iscrive la
		 password che intende utilizzare per eseguire il login e deve inserirla due volte per conferma, se le
		 due password inserite sono differenti, l'utente viene redirettato alla pagina "register_ko_pass.html".
		 Una volta che l'utente legittimo supera tutti i controlli ed ha immesso i dati nel formato corretto
		 ed ha scelto una immagine consentita, viene eseguita una query sul db.usr con i seguenti parametri:
		 db.registerUser(username, nome, cognome, isInputUser) nella tabella utenti (usr). Successivamente si
		 esegue la generazione di un sale randomico da aggiungere alla password in chiaro fornita dall'utente
		 e si inserisce all'interno del db.salt referenziata attraverso l'id utente univoco come chiave esterna.
		 All'interno della classe CipherHelper si procede a concatenare il sale randomico generato alla pwd
		 e ne viene eseguito l'hash del risultato. Tale valore di hash viene immagazzinato all'interno del db.hash
		 referenziando tale tabella attraverso l'id utente come chiave esterna e l'utente viene infine redirettato
		 nella pagina "register_ok.jsp".
	*/
@WebServlet("/RegisterUser")																					// fileSizeThreshold dimensione file dopo il quale questo file viene scritto su hd
@MultipartConfig(fileSizeThreshold = 1024*1024*5, maxFileSize = 1024*1024*5, maxRequestSize = 1024*1024*5*5)	// upload file's size up to 5 mb (multiply 1 mb * 5 measure in bytes)
@ThreadSafe
public class RegisterUser extends HttpServlet {								// maxRequestSize is maximum size allowed for a multipart/form-data request, in bytes
	private static final long serialVersionUID = 1L;
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			DatabaseManager db = (DatabaseManager) session.getAttribute("DatabaseManager");
			
			if(db == null) {
				db = new DatabaseManager();

				session.setAttribute("DatabaseManager", db);
			}
			
			boolean status = false;
			InputStream isInputUser = null;
			final Part filePart = request.getPart("photo");
			
			if(filePart != null) {
				status = true;
				isInputUser = filePart.getInputStream();
				if(UploadAction.checkMetadataImage(isInputUser) == false) {
					response.sendRedirect("image_ko.html");
					return;
				}
		
				isInputUser = filePart.getInputStream();
								
				String username = request.getParameter("username");
				
				Pattern p = Pattern.compile("[<>%?!\\^+*ç~`{}°#§;,:\\|£%&/()=\"\\$]");
				Matcher m = p.matcher(username);
				boolean charNotPermitted = m.find();
				
				if (charNotPermitted == true) {
					response.sendRedirect("error.html");
					return;
				}
				
				if(db.getUsernameLogin(username) == true) {
					response.sendRedirect("register_ko_user.html");
					return;
				}
				
				byte[] password = request.getParameter("password").getBytes();
				byte[] password_check = request.getParameter("password_check").getBytes();
				if(Arrays.equals(password, password_check)) {
					String nome = request.getParameter("nome");
					String cognome = request.getParameter("cognome");
					db.registerUser(username, nome, cognome, isInputUser);
					
					isInputUser.close();
					int id = db.getUserId(username);
					Arrays.fill(password_check, (byte)0);
					PasswordHelper tmp = new PasswordHelper();
					tmp.setPassword(password, id);
					Arrays.fill(password, (byte)0);
					byte[] usr_salt = tmp.getSaltToSave();
					byte[] usr_hash = tmp.getHashToSave();
					
					db.registerSalt(id, usr_salt);
					db.registerHash(id, usr_hash);
					response.sendRedirect("register_ok.jsp");
					return;
				}
			}
					if(status == false) {
						response.sendRedirect("image_ko.html");
						return;
					}	
						response.sendRedirect("register_ko_pass.html");
						return;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			response.sendRedirect("error.html");
		}		
	}
}