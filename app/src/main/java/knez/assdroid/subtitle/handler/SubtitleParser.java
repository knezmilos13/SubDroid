package knez.assdroid.subtitle.handler;

import java.util.List;

import androidx.annotation.NonNull;

import knez.assdroid.subtitle.data.ParsingError;
import solid.collections.Pair;

/** Can load subtitles from a file of a specific format */
public interface SubtitleParser {

    boolean canOpenSubtitleExtension(@NonNull String subtitleExtension);

    @NonNull
    Pair<SubtitleContent, List<ParsingError>> parseSubtitle(@NonNull List<String> subtitleLines);

}
