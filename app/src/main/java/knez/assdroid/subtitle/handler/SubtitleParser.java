package knez.assdroid.subtitle.handler;

import java.util.List;

import android.support.annotation.NonNull;

/** Can load subtitles from a file of a specific format */
public interface SubtitleParser {

    boolean canOpenSubtitleFile(@NonNull String subtitleFilename);

    @NonNull SubtitleContent parseSubtitle(@NonNull List<String> subtitleLines);

}
