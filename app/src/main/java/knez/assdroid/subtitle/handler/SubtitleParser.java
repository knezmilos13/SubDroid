package knez.assdroid.subtitle.handler;

import java.util.List;

import android.support.annotation.NonNull;

import knez.assdroid.subtitle.data.ParsingError;
import solid.collections.Pair;

/** Can load subtitles from a file of a specific format */
public interface SubtitleParser {

    boolean canOpenSubtitleFile(@NonNull String subtitleFilename);

    @NonNull
    Pair<SubtitleContent, List<ParsingError>> parseSubtitle(@NonNull List<String> subtitleLines);

}
