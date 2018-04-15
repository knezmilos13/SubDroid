package knez.assdroid.logika;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import knez.assdroid.util.FormatValidator;

import android.database.Cursor;
import android.net.Uri;

public interface Parser {
	
	void zapocniParsiranje(Uri uri) throws IOException, ParsiranjeException;
	void snimiPrevod(String putanja, Cursor redoviZaglavlja, Cursor redoviStila, Cursor redoviPrevoda)
			throws FileNotFoundException;
	
	interface ParserCallback {
		void ucitaniRedoviPrevoda(List<RedPrevoda> redovi);
		void ucitaniRedoviStila(List<RedStila> redovi);
		void ucitaniRedoviZaglavlja(List<RedZaglavlja> redovi);
		void zavrsenoParsiranje(boolean problemi, String warnString);
	}
	
	class Fabrika {
		private Fabrika() {}
		public static Parser dajParserZaFajl(String fajl, ParserCallback kolbek) {
			if(fajl.toLowerCase().endsWith(FormatValidator.EKSTENZIJA_ASS)) {
				return new AssFileParser(kolbek);
			} //TODO SRT
			else throw new RuntimeException();
		}
	}

}
