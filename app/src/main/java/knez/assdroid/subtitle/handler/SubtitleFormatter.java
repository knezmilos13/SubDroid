package knez.assdroid.subtitle.handler;

import android.support.annotation.NonNull;

import java.util.List;

/** Can save subtitles to a file in a specific format */
public interface SubtitleFormatter {

    boolean canSaveToSubtitleFormat(@NonNull String extension);

    @NonNull List<String> serializeSubtitle(@NonNull SubtitleContent subtitleContent);

}
