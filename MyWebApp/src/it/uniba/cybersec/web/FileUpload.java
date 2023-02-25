package it.uniba.cybersec.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.uniba.cybersec.database.DatabaseManager;
import it.uniba.cybersec.utility.UploadAction;
import net.jcip.annotations.ThreadSafe;

	/* 30) Servlet per eseguire l'upload del file proposta *txt da parte dell'utente

	  A) Il metodo doGet chiama la doPost
	  B) Connessione al database attraverso pattern Singleton
	  C) Si prevengono attacchi DDoS attraverso la direttiva @MultipartConfig nel quale vengono specificate le dimensioni
	     massime del file che l'utente può sottomettere. Se l'utente eccede tali dimensioni, il server chiude la connessione
	  D) Viene verificato il contenuto del file sottomesso dall'utente attraverso il metodo checkMetadataText della classe
	  	 UploadAction, se il file sottomesso dall'utente NON è un file strettamente testuale, l'utente viene redirettato
	  	 alla pagina "proposta_ko.html". Se il file supera il check da parte del metodo sopra citato, si processa il file
	  	 in forma di flusso di byte e viene ulteriormente analizzato attraverso il metodo extractContentTextAndStore,
	  	 se il parser rileva delle parole contenute all'interno della blacklist, il metodo richiamato provvede ad eseguire
	  	 la sanificazione. Successivamente all'interno della servlet, si estrae il path nel quale immagazzinare il file mediante 
	  	 la ServletContext, il path risulta essere una posizione sicura ed è definito all'interno del file "web.xml" di questa
	  	 web-app. Si è agito in questo modo per cercare di evitare inconsistenza all'interno del db.project infatti il nome
	  	 del file sottomesso dall'utente viene scartato e rimpiazzato da uno che presenta il nome utente seguito da un
	  	 underscore ed un numero UUID generato casualmente, seguito dall'estensione *txt.
	  	 La sintassi del nome file è la seguente: nomeutente@mail.com_831235469028ab332.txt
		 Infine l'utente viene redirettato alla pagina "proposta_ok.jsp".
	*/
@WebServlet("/FileUpload")
@MultipartConfig(fileSizeThreshold = 1024*1024*7, maxFileSize = 1024*1024*7, maxRequestSize = 1024*1024*7*7)	// upload file's size up to 5 mb (multiply 1 mb * 5 measured in bytes)
@ThreadSafe
public class FileUpload extends HttpServlet {
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
			final Part filePart = request.getPart("user_file");
			
			if(filePart != null) {
				status = true;
				isInputUser = filePart.getInputStream();
				if(UploadAction.checkMetadataText(isInputUser) == false) {
					response.sendRedirect("proposta_ko.html");
					return;
				}
		
				isInputUser = filePart.getInputStream();
				ServletContext sc = request.getServletContext();
				String loginUsername = (String) session.getAttribute("username");
				session.setAttribute("username", loginUsername);
				UUID uuid = UUID.randomUUID();
			    final String fntrimmed = loginUsername + "_" + uuid.toString() + ".txt";
			    final String outputFilename = sc.getInitParameter("file-upload") + File.separator + fntrimmed;
				InputStream outStream = null;
			    
			    String passToDb = UploadAction.extractContentTextAndStore(isInputUser, outputFilename);
			    
				outStream = new ByteArrayInputStream(passToDb.getBytes());
			    if(outStream != null && db.addProject(loginUsername, outStream)) {
					outStream.close();
					isInputUser.close();
			    	response.sendRedirect("proposta_ok.jsp");
			    	return;
			    }
			    	else
			    		status = false;										// got an error during creation of project file
			}
				if(status == false) {
					response.sendRedirect("proposta_ko.html");
					isInputUser.close();
					return;
				}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			response.sendRedirect("error.html");
		}		
	}
}