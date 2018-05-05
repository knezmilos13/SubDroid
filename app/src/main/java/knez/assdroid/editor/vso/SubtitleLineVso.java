package knez.assdroid.editor.vso;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import knez.assdroid.common.data.IdentifiableImpl;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleLineVso extends IdentifiableImpl {

    // TODO: gresis sa VSO-om. ne sme da ima ceo subtitle line, nije u tome poenta.
    // treba da ima vec formatiran timestamp; i treba da ima vec dodate actor, ovo/ono
    // pa ako ih ima da se prikazu, a ako ih nema da se ne prikazu, a ne layout item da razmisllja
    // onda ti ne treba mozda ni settings recimo

    @NonNull private final SubtitleLineSettings subtitleLineSettings;
    @DrawableRes private final int backgroundDrawable;
    @NonNull private final String text;

    public SubtitleLineVso(
            long id,
            @NonNull SubtitleLineSettings subtitleLineSettings,
            @DrawableRes int backgroundDrawable,
            @NonNull String text) {
        super(id);
        this.subtitleLineSettings = subtitleLineSettings;
        this.backgroundDrawable = backgroundDrawable;
        this.text = text;
    }

    @NonNull public SubtitleLineSettings getSubtitleLineSettings() { return subtitleLineSettings; }
    public int getBackgroundDrawable() { return backgroundDrawable; }
    @NonNull public String getText() { return text; }

}
