package it.uniba.cybersec.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.uniba.cybersec.database.DatabaseManager;
import net.jcip.annotations.ThreadSafe;

	/* 34) Servlet per ottenere la lista dei progetti presenti nel db e caricati dagli utenti

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) La routine prevede una query all'interno del db.project per ottenere la proposta progettuale
	  	 inserita da parte di un utente legittimo ed associata al proprio id utente univoco che lo
	  	 identifica all'interno del db.usr la proposta progettuale verrà acquisita sotto forma di flusso
	  	 di byte. Il rendering della proposta selezionata verrà successivamente effettuato nel lato front 
	  	 end all'interno della JSP "get_user_proj.jsp" sfruttando l'Expression Language poichè permette
	  	 la risoluzione dinamica degli oggetti e metodi all'interno della JSP stessa.
	*/
@WebServlet("/GetUserProj")
@ThreadSafe
public class GetUserProj extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			DatabaseManager db = (DatabaseManager)session.getAttribute("DatabaseManager");
			
			if(db == null) {
				db = new DatabaseManager();
			
				session.setAttribute("DatabaseManager", db);
			}
			
			String idUsr = request.getParameter("id_usr"); 								// id_usr got from 1st servlet
			int id_usr = Integer.parseInt(idUsr);
			String projUser = db.getUserProject(id_usr);
			session.setAttribute("projUser", projUser);

			response.sendRedirect("get_user_proj.jsp");
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			response.sendRedirect("error.html");
		}
	}
}