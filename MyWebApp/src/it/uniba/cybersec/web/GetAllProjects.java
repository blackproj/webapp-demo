package it.uniba.cybersec.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.uniba.cybersec.database.DatabaseManager;
import it.uniba.cybersec.model.Project;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

	/* 31) Servlet per ottenere la lista dei progetti presenti nel db e caricati dagli utenti

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) Viene eseguita una query sul db.project e restituita la lista dei progetti che sono stati caricati
	     da parte di utenti legittimi. Tutti gli utenti legittimi, una volta eseguito il login, hanno facoltà 
	     di richiedere qualsiasi file presente all'interno del db.project	     
	*/
@WebServlet("/GetAllProjects")
@ThreadSafe
public class GetAllProjects extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@GuardedBy("itself")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		try {
			HttpSession session = request.getSession();
			DatabaseManager db = (DatabaseManager)session.getAttribute("DatabaseManager");
			
			if(db == null) {
				db = new DatabaseManager();
			
				session.setAttribute("DatabaseManager", db);
			}
																						// need to pass this to the 2nd servlet
			String username = (String) session.getAttribute("username");	
			session.setAttribute("username", username);									// username got from 1st servlet
				
			List<Project> allProj = db.getAllProject();
			session.setAttribute("allProj", allProj);
			
			response.sendRedirect("get_all_proj.jsp");
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			response.sendRedirect("error.html");
		}
	}
}