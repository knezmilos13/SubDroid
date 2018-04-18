package knez.assdroid.common;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.podesavanja.KategorijePodesavanjaAktivnost;

public class Navigator {

    @NonNull private final Context context;

    public Navigator(@NonNull final Context context) {
        this.context = context;
    }

    public void showSettingsScreen() {
        Intent settingsIntent = new Intent(context, KategorijePodesavanjaAktivnost.class);
        context.startActivity(settingsIntent);
    }

    public void showHelpScreen() {
        Intent helpIntent = new Intent(context, KategorijeHelpaAktivnost.class);
        context.startActivity(helpIntent);
    }

}
