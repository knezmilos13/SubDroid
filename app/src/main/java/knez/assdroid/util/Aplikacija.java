package knez.assdroid.util;

import android.app.Application;
import android.content.Context;

// Ova klasa ce drzati referencu na aplikacioni kontekst koji ce biti neophodan na raznim mestima
// Da bi se dobio aplikacioni kontekst po startovanju aplikacije, nasledjuje se klasa Application
// i referencira se u manifest fajlu koji ce se onda postarati da kreira ovu klasu na samom pocetku
public class Aplikacija extends Application {

	private static Context instancaKonteksta;
	
	public static Context dajKontekst()	{
		return instancaKonteksta;
	}

	// mislim da mi ne treba nista vise ovde sem pokretanja i gasenja servisa
	public void onCreate(){
		instancaKonteksta=getApplicationContext();
		super.onCreate();
	}

}
