package it.uniba.cybersec.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.uniba.cybersec.database.DatabaseManager;
import net.jcip.annotations.ThreadSafe;

	/* 33) Servlet per ottenere la lista dei progetti presenti nel db e caricati dagli utenti

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) L'utente che intende eseguire il logout dal lato front end, richiama questa servlet che provvede
	     ad ottenere la lista dei cookie presenti nella cache del browser utente e se presente il cookie
	     utente "remember me" si esegue una query sul db.cookie per rimuovere tale corrispondenza associata
	     al cookie e settare tale cookie all'interno del browser con valore 0. Successivamente si invalida
	     la sessione corrente e l'utente viene redirettato nella pagina "logout.jsp".
	*/
@WebServlet("/GetUserLogout")
@ThreadSafe
public class GetUserLogout extends HttpServlet {
	private static final long serialVersionUID = 1L;

  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession(false);			// Check, if there is a session active. Avoid to create a new one.
			DatabaseManager db = (DatabaseManager) session.getAttribute("DatabaseManager");
			
			if(db == null) {
				db = new DatabaseManager();
				
				session.setAttribute("DatabaseManager", db);
			}
			
			Cookie[] cookieJar = request.getCookies();
			if (cookieJar != null)
		        for (Cookie cookie : cookieJar) {
		        	if(cookie.getName().equals("rememberme")) {
		        		Cookie mycookie = new Cookie("rememberme", "0");	
		        		mycookie.setMaxAge(0);
		        		response.addCookie(mycookie);
		    			String username = (String) session.getAttribute("username");	
		        		db.deleteCookie(username);
		        	}
		        }
				session.invalidate();
				response.sendRedirect("logout.jsp");
		} catch(Throwable throwable) {
			throwable.printStackTrace(); 
			response.sendRedirect("error.html");	
		} 						
	}
}