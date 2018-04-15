package knez.assdroid.help;

import knez.assdroid.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class HelpAboutAkt extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.akt_help_about);
		
	    TextView poljeLink = findViewById(R.id.poljeLink);
	    poljeLink.setMovementMethod(LinkMovementMethod.getInstance());
	}

}

