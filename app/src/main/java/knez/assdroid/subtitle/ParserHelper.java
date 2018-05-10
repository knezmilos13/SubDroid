package knez.assdroid.subtitle;

public class ParserHelper {

    // TODO: vidi gde ces ga kako ces ga, svakako ne kao static
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

}
