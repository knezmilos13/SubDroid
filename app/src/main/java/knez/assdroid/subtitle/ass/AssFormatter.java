package knez.assdroid.subtitle.ass;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import knez.assdroid.R;
import knez.assdroid.subtitle.ParsiranjeException;
import knez.assdroid.subtitle.RedPrevoda;
import knez.assdroid.subtitle.RedStila;
import knez.assdroid.subtitle.RedZaglavlja;
import knez.assdroid.subtitle.SubtitleFormatter;
import knez.assdroid.subtitle.SubtitleParser;
import knez.assdroid.util.Alatke;

public class AssFormatter implements SubtitleFormatter {

	private static final String UTF_BOM = "\uFEFF";

	// ------------------------------------------------------------------- Informacije za parsiranje prevoda
	private static final String SEKCIJA_PREVOD = "[Events]";

	private static final String SEKCIJA_PREVOD_DEFAULT_FORMAT =
			"Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text";
	private static final String SEKCIJA_PREVOD_RED_COMMENT = "Comment:";
	private static final String SEKCIJA_PREVOD_RED_DIALOGUE = "Dialogue:";


	// ------------------------------------------------------------------- Informacije za parsiranje stilova
	private static final String SEKCIJA_STIL = "[V4+ Styles]";
	//private static final String SEKCIJA_STIL_OLD = "[V4 Styles]"; ne treba - za poredjenje koristis lower case
	private static final String SEKCIJA_STIL_LOWER_CASE = "[v4+ styles]";
	private static final String SEKCIJA_STIL_OLD_LOWER_CASE = "[v4 styles]";

	// ------------------------------------------------------------------- Informacije za parsiranje zaglavlja
	private static final String SEKCIJA_ZAGLAVLJE = "[Script Info]";
	private static final String SEKCIJA_ZAGLAVLJE_LOWER_CASE = "[script info]";


	public AssFormatter() {

    }

	@Override
	public boolean canSaveToSubtitleFormat(@NonNull String extension) {
		return extension.toLowerCase().equals("ass");
	}

	@Override @NonNull
	public List<String> serializeSubtitle() {
		return null;
	}



	public void snimiPrevod(String putanjaPrevoda, Cursor redoviZaglavlja, Cursor redoviStila, Cursor redoviPrevoda)
			throws FileNotFoundException {
		File fajl = new File(putanjaPrevoda);
		PrintWriter p = new PrintWriter(fajl);

		p.print(UTF_BOM);

		p.println(SEKCIJA_ZAGLAVLJE);
		while(redoviZaglavlja.moveToNext()) {
			p.println(RedZaglavlja.kreirajStringIzKursora(redoviZaglavlja));
		}
		p.println();

		p.println(SEKCIJA_STIL);
		//TODO: ne stampas eksplicitno red za format stila - zajedno ti je na gomili sa ostalim
		// ali kad izmenis gore ucitavanje stila, onda menjaj i ovde stampanje stila
		while(redoviStila.moveToNext()) {
			p.println(RedStila.kreirajStringIzKursora(redoviStila)); //TODO: nece da moze. ili oce ako je AssRedStila
		}
		p.println();

		p.println(SEKCIJA_PREVOD);
		p.println(SEKCIJA_PREVOD_DEFAULT_FORMAT);
		while(redoviPrevoda.moveToNext()) {
			RedPrevoda red = RedPrevoda.kreirajIzKursora(redoviPrevoda);
			p.printf("%s%d,%s,%s,%s,%s,%04d,%04d,%04d,%s,%s\n", 
					(red.komentar? SEKCIJA_PREVOD_RED_COMMENT : SEKCIJA_PREVOD_RED_DIALOGUE) + " ",
					red.layer, 
					Alatke.formatirajVreme(red.start), 
					Alatke.formatirajVreme(red.end), 
					red.style, 
					red.actorName,
					red.marginL, 
					red.marginR, 
					red.marginV, 
					red.effect, 
					red.text);
		}
		p.close();
	}

}
