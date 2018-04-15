package knez.assdroid.util;

public class FormatValidator {
	
	public static final String EKSTENZIJA_ASS = ".ass";
	public static final String EKSTENZIJA_SRT = ".srt";
	
	public static boolean formatPrihvatljiv(String fileName) {
		return fileName.toLowerCase().endsWith(".ass");//TODO SRT
	}

}
