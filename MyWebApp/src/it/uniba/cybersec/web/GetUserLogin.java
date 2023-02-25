package it.uniba.cybersec.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.uniba.cybersec.database.DatabaseManager;
import it.uniba.cybersec.utility.CipherHelper;
import net.jcip.annotations.ThreadSafe;

	/* 32) Servlet per ottenere la lista dei progetti presenti nel db e caricati dagli utenti

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) L'utente in fase di login può agire in due modi contraddistinti e supportati dalla servlet in analisi.
	  	 Il primo modo prevede il login all'interno della web-app con il solo cookie di sessione (JSESSIONID)
	  	 auto generato da parte del server e di durata 30 minuti (Apache Tomcat 8 standard default).
	  	 Il secondo modo prevede il login all'interno della web-app utilizzando un cookie utente costituito
	  	 dal doppietto "nome-valore", tale cookie verrà immagazzinato all'interno della cache del browser utente
	  	 e una corrispondente entry a tale cookie verrà immagazzinata all'interno del db.cookie.
	  	 Nella fattispecie, utilizzando il primo modo l'utente immette il proprio nome utente e la password e viene
	  	 eseguita una query all'interno del db.usr per ricercare la corrispondenza e verificare la legittimità
	  	 dell'utente. In caso di esistenza della corrispondenza l'utente viene redirettato alla pagina "login_ok.jsp"
	  	 e viene mostrato anche la sua foto profilo scelta in fase di caricamento. In caso di verifica negativa e non
	  	 corrispondenza l'utente viene redirettato alla pagina "login_ko.html".
	  	 Se l'utente invece seleziona la casella "remember me", la routine ottiene i cookie presenti all'interno
	  	 della sessione e della cache del browser utente e si verifica che il cookie di sessione sia costituito 
	  	 esclusivamente da una coppia chiave-valore. Tale verifica cerca di evitare l'introduzione di un cookie
	  	 creato ad arte da parte di un avversario che esegue spoofing.
	  	 Viene eseguita una query sul db per verificare la presenza dell'utente all'interno del db.usr e successivamente
	  	 si procede a creare una stringa randomica di lunghezza 32 bit che costituirà il "value" del cookie che verrà creato
	  	 per l'utente che esegue login, la stringa viene creata attraverso il metodo CipherHelper.getRandomString
	  	 Successivamente tale stringa verrà cifrata attraverso il metodo CipherHelper.encCk e per aumentare la sicurezza
	  	 associata all'utilizzo del cookie e la sua relativa scadenza viene inoltre richiesta la data ed ora di sistema
	  	 attuale che verrà incrementata di 15 minuti poichè trascorsi i 15 minuti dal login, il cookie non dovrà più
	  	 essere valido. Se per esempio la data ed ora di sistema attuale risulta essere 10/08/2022 @ 12.00 allora il
	  	 valore immagazzinato risulterà essere 10/08/2022 @ 12.15 espressa in millisecondi.
	  	 Viene verificato quindi se esiste una entry associata al cookie utente all'interno del db.cookie e se NON
	  	 presente si provvede ad inserirla attraverso il metodo db.mapUserForRememberMe dove tale metodo riceve in input
	  	 l'username, la stringa randomica e cifrata e la data ed ora di sistema incrementata di 15 minuti.
	  	 Diversamente se esiste già una entry associata all'utente poichè non è la prima volta che utilizza l'opzione
	  	 "rememberme" allora si procede ad aggiornare la entry nel db.cookie mediante il metodo updateCookie
	  	 che riceve in input gli stessi valori di db.mapUserForRememberMe.
	  	 Si procede pertanto ad invalidare la sessione attuale e creare il cookie utente con i valori specificati
	  	 e si setta la possibilità di utilizzo solo all'interno di connessioni sicure HTTPS e l'utente viene redirettato
	  	 alla pagina "login_ok.jsp".
	  	 
	  	 In ogni caso sia l'utente scelga l'opzione "remember me" oppure no, vengono collezionati i cookie presenti all'interno
	  	 della cache del browser utente alla ricerca di un cookie utente "rememberme" poichè viene verificato attraverso una
	  	 query su db.cookie che la data immagazzinata in tale cookie NON superiore a 15 minuti rispetto alla data ed ora attuale.
	  	 Nel qual caso in cui questo controllo fallisca, verrà eseguita una query su db.cookie per eliminare la entry relativa
	  	 al cookie utente immesso in precedenza.
	*/
@WebServlet("/GetUserLogin")
@ThreadSafe
public class GetUserLogin extends HttpServlet {
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
			String username = request.getParameter("username");
		    boolean rememberme = "true".equals(request.getParameter("rememberme"));
			byte[] password = request.getParameter("passphrase").getBytes();
			int idUser = db.getUserId(username);
			byte[] immagine = db.getUserImage(idUser);
			String base64Image = Base64.getEncoder().encodeToString(immagine);
			boolean validated = false;
			
			Cookie[] cookieJar = request.getCookies();
			if (cookieJar != null)
		        for (Cookie cookie : cookieJar)
		        	if(cookie.getName().equals("rememberme")) {
		        		long timeCookie = db.retrieveTimeCookie(username);				// grab time of user cookie stored on db.usr
		        		long sysTimeNow = System.currentTimeMillis();
		        		if(sysTimeNow > timeCookie)
		        			db.deleteCookie(username);									// cookie is expired, so delete it
		        	}
			
			if (rememberme == true) {
		        if (request.getCookies()[0] != null && request.getCookies()[0].getValue() != null) {
		        	String valueCkSess = request.getCookies()[0].getValue();
					final int mid = valueCkSess.length()/2;
					String[] value = {valueCkSess.substring(0, mid), valueCkSess.substring(mid)};

					if(value.length != 2) {
						response.sendRedirect("error.html");							// temptative to supply fake cookie
						return;
					}
		// user has pressed on remeberMe but this is first time which he logs into our system cause there are no cookie into db.cookies
		        } 
		            else {
		                validated = db.checkLogin(username, password);
		                if (!validated) {
		                	response.sendRedirect("login_ko.html");
		                	return;
		                }
		            }
		            String newRandom = CipherHelper.getRandomString(32);
		            String newRandomEnc = CipherHelper.encCk(newRandom, idUser);		// cipher value of cookie
		            long nowPlus15Minutes = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);
					boolean isUcPresent = db.retrieveUserCookie(username);
					if(!isUcPresent)						
						db.mapUserForRememberMe(username, newRandomEnc, nowPlus15Minutes);
		        	db.updateCookie(username, newRandomEnc, nowPlus15Minutes);

		            session.invalidate();
		            session = request.getSession(true);
		            session.setMaxInactiveInterval(60 * 15);
		            session.setAttribute("username", username);
		            Cookie loginCookie = new Cookie("rememberme", URLEncoder.encode(newRandomEnc, "UTF-8"));
		            loginCookie.setHttpOnly(true);
		            
		            response.addCookie(loginCookie);
					session.setAttribute("fetchImage", base64Image);
		            response.sendRedirect("login_ok.jsp");
		            return;
			}
		        else { 																	// No remember-me functionality selected
		            validated = db.checkLogin(username, password);
		            if(validated == true) {
			            session.setAttribute("username", username);
		    			session.setAttribute("fetchImage", base64Image);			
		            	response.sendRedirect("login_ok.jsp");
		            }
		            	else
		            		response.sendRedirect("error.html");		            
		        }
				Arrays.fill(password, (byte)0);
		} catch(Throwable throwable) {
			throwable.printStackTrace(); 
			response.sendRedirect("error.html");	
		} 												
	}
}