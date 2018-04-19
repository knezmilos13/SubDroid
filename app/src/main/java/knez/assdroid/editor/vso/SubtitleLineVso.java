package knez.assdroid.editor.vso;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import knez.assdroid.common.data.IdentifiableImpl;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.subtitle.RedPrevoda;

public class SubtitleLineVso extends IdentifiableImpl {

    @NonNull private final SubtitleLineSettings subtitleLineSettings;
    @NonNull private final RedPrevoda redPrevoda;
    @DrawableRes private final int backgroundDrawable;

    public SubtitleLineVso(@NonNull RedPrevoda redPrevoda,
                           @NonNull SubtitleLineSettings subtitleLineSettings,
                           @DrawableRes int backgroundDrawable) {
        super(redPrevoda.id);
        this.redPrevoda = redPrevoda;
        this.subtitleLineSettings = subtitleLineSettings;
        this.backgroundDrawable = backgroundDrawable;
    }

    @NonNull public RedPrevoda getRedPrevoda() { return redPrevoda; }
    @NonNull public SubtitleLineSettings getSubtitleLineSettings() { return subtitleLineSettings; }
    public int getBackgroundDrawable() { return backgroundDrawable; }

}
