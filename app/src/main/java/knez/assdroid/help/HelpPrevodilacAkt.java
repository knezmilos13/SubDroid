package knez.assdroid.help;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import knez.assdroid.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpPrevodilacAkt extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.akt_web_view);
		WebView brauzer = findViewById(R.id.webview);
		
		Locale lokal = Locale.getDefault();
		String jezik = lokal.getLanguage();
		
		try {
			if(!Arrays.asList(getResources().getAssets().list("help")).contains(jezik)) {
				jezik = "sr";
			}
		} catch (IOException e) {
			jezik = "sr";
		}

		brauzer.loadUrl("file:///android_asset/help/" + jezik + "/translator/translatorHelp.html");
	}

}

