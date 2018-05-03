package knez.assdroid.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import knez.assdroid.translator.TranslatorActivity;
import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.podesavanja.KategorijePodesavanjaAktivnost;

public class Navigator {

    @NonNull private final Context context;

    public Navigator(@NonNull final Context context) {
        this.context = context;
    }

    public void showSettingsScreen() {
        Intent settingsIntent = new Intent(context, KategorijePodesavanjaAktivnost.class); // TODO
        context.startActivity(settingsIntent);
    }

    public void showHelpScreen() {
        Intent helpIntent = new Intent(context, KategorijeHelpaAktivnost.class); // TODO
        context.startActivity(helpIntent);
    }

    public void showTranslatorScreen(int id) {
        Intent translatorIntent = new Intent(context, TranslatorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(TranslatorActivity.INPUT_LINE_ID, id);
        context.startActivity(translatorIntent, bundle);
    }

}
