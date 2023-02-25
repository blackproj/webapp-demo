package it.uniba.cybersec.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jcip.annotations.ThreadSafe;

	/* 36) Filtro pre-process richieste provenienti da servlet, da risposta servlet oppure entrambe 

	  A) Il filtro modifica il flusso di esecuzione della web-app poichè filtra tutto il traffico generato
	  	 dalle servlet presenti. Si colloca a monte dell'esecuzione della web-app
	  B) Il filtro qui definito viene utilizzato per verificare che un utente che NON ha eseguito il login
	  	 non possa accedere a risorse protette, quali la visualizzazione della lista delle proposte progettuali
	  	 oppure il caricamento di una nuova proposta progettuale. Un utente che non esegue il login può
	  	 esclusivamente accedere alla home del progetto "/MyWebApp/index.jsp", la pagina di registrazione
	  	 "/MyWebApp/register_user.jsp" oppure alla pagina di login "/MyWebApp/login.jsp".
	  	 Se invece l'utente esegue il login in maniera corretta, inizia la validità della sua sessione e
	  	 il filtro provvede a lasciar passare la richiesta proveniente dalla servlet specifica, per esempio
	  	 dal caricamento proposta progettuale attraverso il metodo chain.doFilter.
	  	 All'interno di questo filtro viene inoltre verificata la validità della sessione corrente, se scaduta
	  	 o con cookie utente scaduto nel caso in cui l'utente abbia scelto l'opzione "remember me", il filtro
	  	 rediretta l'utente alla pagina di "login.jsp" per accedere nuovamente.
	  	 
	  	 N.B: Per poter utilizzare il filtro è necessario specificare attraverso la direttiva @WebFilter a quali
	  	 	  webpages deve essere applicato, in questo caso viene applicato all'intera web-app specificando la
	  	 	  root della web-app stessa e deve essere specificato lo stesso filtro all'interno del file "web.xml"
	  	 	  della propria web-app così come segue:
	  	 	    	<filter>
  						<filter-name>LoginFilter</filter-name>
    						<filter-class>it.uniba.cybersec.web.filter.LoginFilter</filter-class>
  					</filter>
	*/
@WebFilter("/*")
@ThreadSafe
public class LoginFilter implements Filter {
    private HttpServletRequest httpRequest;
    private static final String[] loginRequiredURL = {"/upload_file", "/get_all_proj", 
    		"/get_user_proj", "/login_ok", "/proposta_ok"};
    	
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("username") != null);
		
        String loginURI = httpRequest.getContextPath() + "/login";
        String homeURI = httpRequest.getContextPath() + "/";
        boolean isLoginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean isHomepageRequest = httpRequest.getRequestURI().equals(homeURI);
        boolean isLoginPage = httpRequest.getRequestURI().endsWith("login.jsp");
 
        if (isLoggedIn && (isLoginRequest || isLoginPage || isHomepageRequest))
            httpRequest.getRequestDispatcher("/login_ok.jsp").forward(request, response);
        	else if (!isLoggedIn && isLoginRequired()) {
        		String loginPage = "/login.jsp";
        		RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(loginPage);
        		dispatcher.forward(request, response);
        	} 
        		else {
        			chain.doFilter(request, response);
        }
    }
		
   
    private boolean isLoginRequired() {
        String requestURL = httpRequest.getRequestURL().toString();
        for (String loginRequiredURL : loginRequiredURL)
            if (requestURL.contains(loginRequiredURL))
                return true;
 
        return false;
    }
 
    public void destroy() {
    }
 
    public void init(FilterConfig fConfig) throws ServletException {
    }
}