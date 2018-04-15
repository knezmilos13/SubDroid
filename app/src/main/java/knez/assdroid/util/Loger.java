package knez.assdroid.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;

public class Loger {

	public static void log(Exception sta) {
		try {
			File fajl = new File(Environment.getExternalStorageDirectory(),"ASSLOG.txt");
			if(!fajl.exists()) {
				fajl.createNewFile();
			}
			PrintWriter p = new PrintWriter(new FileWriter(fajl, true));
			sta.printStackTrace(p);
			p.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
