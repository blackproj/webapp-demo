package it.uniba.cybersec.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class UploadAction {
	/* 27) Verifica i metadati di un file immagine attraverso Apache Tika

	  - Il metodo riceve in input uno stream di byte che corrisponde all'immagine di cui l'utente intende
	  - eseguire upload per la sua foto profilo durante la fase di registrazione.
	  - Attraverso la facade della libreria Apache Tika è possibile andare a verificare che il file sottomesso
	  - dall'utente sia legittimo e sia uno di quelli consentiti. SOLO file immagini e con estensioni consentite
	  - *png, *jpg, *jpeg. Apache Tika risulta particolarmente efficace da estrapolare ed individuare i metadati
	  - codificati all'interno del file durante la creazione dello stesso. Modificando l'estensione del file non
	  - è possibile ingannare Tika.
	  - Il metodo restituisce un booleano in output, dove true se il file immagine è legittimo e consentito, 
	  - false altrimenti.
	*/
	public static boolean checkMetadataImage(InputStream is) throws IOException, TikaException, SAXException {
		boolean legitFile = false;
		
		ContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		Parser parser = new AutoDetectParser();

		try {
			parser.parse(is, handler, metadata, new ParseContext());	
		} catch (SAXException | TikaException uploadActionException) {
			System.out.println("Exception from uploadActionException");
			uploadActionException.printStackTrace();
			return false;
		}
		
		String fileToBeUploaded = metadata.get(Metadata.CONTENT_TYPE);

		if(fileToBeUploaded.equalsIgnoreCase("image/png") || fileToBeUploaded.equalsIgnoreCase("image/jpeg")) {
			legitFile = true;
			return legitFile;
		}
			// else the fileToBeUploaded is not legit so return false (as you declared it)
			return legitFile;
	}	
	
	/* 28) Verifica i metadati di un file testuale attraverso Apache Tika
	*/
	public static boolean checkMetadataText(InputStream is) throws IOException, TikaException, SAXException {
		boolean legitFile = false;
		
		ContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		Parser parser = new AutoDetectParser();
		
		try {
			parser.parse(is, handler, metadata, new ParseContext());	
		} catch (SAXException | TikaException uploadActionException) {
			System.out.println("Exception from uploadActionException");
			uploadActionException.printStackTrace();
			return false;
		}
		
		String fileToBeUploaded = metadata.get(Metadata.CONTENT_TYPE);

		if(fileToBeUploaded.contains("text/plain")) {
			legitFile = true;
			return legitFile;
		}
			// else the fileToBeUploaded is not legit so return false (as you declared it)
			return legitFile;	
	}
	
	/* 29) Parsing di un file testuale e salvataggio in posizione sicura con eventuale sanificazione

	  - Il metodo riceve in input uno stream di byte che corrisponde al file di testo sottomesso dall'utente
	  - e una stringa rappresentante il file di output (eventualmente sanificato nel caso di testo non consentito).
	  - Viene istanziato un oggetto Apache Tika attraverso la sua facade e si da in input a Tika il file di testo
	  - sottomesso dall'utente, in forma di stringa.
	  - Si istanzia un file di output di tipo RandomAccessFile per cercare di limitare quanto più possibile il bug
	  - TOCTOU, infatti si inizializza il buffer del file solo una volta e si chiude al termine della computazione
	  - di questo metodo. Questo file di output verrà immagazzinato all'interno di una posizione sicura, acquisita
	  - attraverso la ServletContext che lancia il metodo. La posizione sicura risulta C:\tomcat8\webapps\data
	  - Il metodo verifica che NON sia presente del testo che risulta in blacklist. Pertanto se il file di testo
	  - risulta legittimo dal punto di vista del contenuto, il file viene immagazzinato all'interno di un file di
	  - output. Se invece all'interno del testo risultasse delle parole non accettate, queste verranno sanificate
	  - e sostituite attraverso degli spazi. Questo controllo permette di evitare attacchi del tipo stored XSS.
	  - Poichè la struttura dati della stringa è immutabile, viene utilizzato uno StringBuilder per costruire il
	  - nuovo testo sanificato.
	*/
	public static String extractContentTextAndStore(InputStream stream, String outputFilename) throws IOException, TikaException, SAXException {
		Tika tika = new Tika();
		String t = tika.parseToString(stream); // this is my content from InputStream (the entire file buffered over a single line)
		String passToDb = null;
		
		try (
			 RandomAccessFile fileToWrite = new RandomAccessFile(outputFilename, "rw")
		) {
			StringBuilder sb = new StringBuilder();
			 for(int i = 0; i < t.length(); ++i) {
				 if(t.contains("hack") || t.contains("h4ck") || t.contains("steal") || t.contains("bound") ||
						 t.contains("upload") || t.contains("localhost") || t.contains("mime") || t.contains("dest") ||
						 t.contains("assert") || t.contains("overflow") || t.contains("cast") || t.contains("compile") ||
						 t.contains("}") || t.contains("end") || t.contains("delete") || t.contains("thread") ||
						 t.contains("default") || t.contains("document") || t.contains("integer") || t.contains("sql") ||
						 t.contains("return") || t.contains("array") || t.contains("alt") || t.contains("forge") ||
						 t.contains("store") || t.contains("crypt") || t.contains("decrypt") || t.contains("sh") || 
						 t.contains("exe") || t.contains("py") || t.contains("cookie") || t.contains("jsp") ||
						 t.contains("script") || t.contains("write") || t.contains("grab")) {

					 t = t.replace("hack", " ");
					 t = t.replace("h4ck", " ");
					 t = t.replace("steal", " ");
					 t = t.replace("bound", " ");
					 t = t.replace("upload", " ");
					 t = t.replace("localhost", " ");
					 t = t.replace("mime", " ");
					 t = t.replace("dest", " ");
					 t = t.replace("assert", " ");
					 t = t.replace("overflow", " ");
					 t = t.replace("cast", " ");
					 t = t.replace("compile", " ");
					 t = t.replace("{", " ");
					 t = t.replace("}", " ");
					 t = t.replace("end", " ");
					 t = t.replace("delete", " ");
					 t = t.replace("thread", " ");
					 t = t.replace("default", " ");
					 t = t.replace("document", " ");
					 t = t.replace("integer", " ");
					 t = t.replace("sql", " ");
					 t = t.replace("return", " ");
					 t = t.replace("array", " ");
					 t = t.replace("alt", " ");
					 t = t.replace("forge", " ");
					 t = t.replace("store", " ");
					 t = t.replace("crypt", " ");
					 t = t.replace("decrypt", " ");
					 t = t.replace("sh", " ");
					 t = t.replace("exe", " ");
					 t = t.replace("py", " ");
					 t = t.replace("cookie", " ");
					 t = t.replace("jsp", " ");
					 t = t.replace("script", " ");
					 t = t.replace("write", " ");
					 t = t.replace("grab", " ");
					 sb.append(t);
					 t = sb.toString();
					 passToDb = sb.toString();
					 fileToWrite.writeBytes(t);
				 }

			 if(passToDb == null) {					// then the file was legit with all accepted strings/words
				 fileToWrite.writeBytes(t);			// no substitutions are made here
				 return t;
			 }
		  }
		} catch (IOException exception) {
			exception.printStackTrace();
		}	
			return passToDb;
	}
}