package knez.assdroid.util;

import java.util.Formatter;

public class Alatke {

	/** Parsira vreme iz zadatog stringa. Ako ne uspe, vraca defaultVrednost i setuje ulazno/izlazni
	 * parametar outFailed na true. U suprotnom ga ne dira. */
	public static int parsirajVreme(String vreme, int defaultVrednost, Boolean[] outFailed) {
		try {
			int sati, minuti, sekunde, centasekundi;
			String[] delovi1 = vreme.split(":");
			sati = Integer.parseInt(delovi1[0]);
			minuti = Integer.parseInt(delovi1[1]);
			String[] delovi2 = delovi1[2].split("\\.");
			sekunde = Integer.parseInt(delovi2[0]);
			centasekundi = Integer.parseInt(delovi2[1]);

			return centasekundi*10 + sekunde * 1000 + minuti * 60000 + sati * 3600000;
		} catch (Exception numex) { // number format & array index out of bounds
			Loger.log(numex);
			outFailed[0] = true;
			return backupParsiranje(vreme, defaultVrednost);
		}
	}

	/** Uzece koje god brojeve moze da ucita i parsirace ih kakvi god da su. */
	private static int backupParsiranje(String vreme, int defaultVrednost) {
		String izvuceniBrojevi = "";
		for(int i = 0; i < vreme.length(); i++) {
			if(Character.isDigit(vreme.charAt(i)))
				izvuceniBrojevi += vreme.charAt(i);
		}
		try {
			int iscupanBroj = Integer.parseInt(izvuceniBrojevi);
			int centasekundi = iscupanBroj % 100;
			int sekundi = (iscupanBroj % 10000) / 100;
			int minuti = (iscupanBroj % 1000000) / 10000;
			int sati = iscupanBroj/1000000;
			
			return centasekundi*10 + sekundi * 1000 + minuti * 60000 + sati * 3600000;
		} catch (NumberFormatException numex) {
			Loger.log(numex);
			return defaultVrednost;
		}
	}

	public static String formatirajVreme(long vreme) {		
		int sati = (int)(vreme / 3600000);
		int ostalo = (int)(vreme % 3600000);
		int minuti = ostalo / 60000;
		ostalo = ostalo % 60000;
		int sekunde = ostalo / 1000;
		ostalo = ostalo % 1000;
		int centasekunde = ostalo / 10;

		StringBuilder sb = new StringBuilder();
		Formatter form = new Formatter(sb);
		form.format("%d:%02d:%02d.%02d", sati, minuti, sekunde, centasekunde);
		form.close();

		return sb.toString();
	}

	public static String izbaciTagove(String pacijent, String tagZamena) {
		StringBuilder sb = new StringBuilder();
		int pocetakTag = -1;
		int pocetakNormalanTekst = 0;
		for(int i = 0; i < pacijent.length(); i++) {
			if(pacijent.charAt(i) == '{') {
				if(pocetakTag != -1)
					continue; //neko je zezno sintaksu, imas dva puta { {
				else {
					sb.append(pacijent.substring(pocetakNormalanTekst, i));
					pocetakTag = i;
					continue;
				}
			} else if(pacijent.charAt(i) == '}') {
				if(pocetakTag == -1)
					continue; //neko je zezno sintaksu, imas } a da nisi imao {
				else {
					sb.append(tagZamena);
					pocetakNormalanTekst = i+1;
					pocetakTag = -1;
				}
			}
		}
		if(pocetakNormalanTekst < pacijent.length()) {
			sb.append(pacijent.substring(pocetakNormalanTekst, pacijent.length()));
		}

		return sb.toString();
	}

	/** Parsira prosledjen string i vraca broj koji je procitao. Ako ne uspe, vraca prosledjenu default
	 *  vrednost i setuje ulazno/izlazni parametar outFailed na true. Ako uspe, outFailed se ne dira. */
	public static int parsirajInteger(String broj, int defaultVrednost, Boolean outFailed[]) {
		try {
			return Integer.parseInt(broj);
		} catch (NumberFormatException numex) {
			outFailed[0] = true;
			Loger.log(numex);
			return defaultVrednost;
		}
	}
	
	public static void trimujElemente(String niz[]) {
		for(int i = 0; i < niz.length; i++)
			niz[i] = niz[i].trim();
	}

}
