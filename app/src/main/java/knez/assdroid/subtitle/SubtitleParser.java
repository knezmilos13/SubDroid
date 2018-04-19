package knez.assdroid.subtitle;

import java.io.IOException;
import java.util.List;

import android.net.Uri;
import android.support.annotation.NonNull;

import knez.assdroid.subtitle.data.SubtitleFile;

/** Can load subtitles from a file of a specific format */
public interface SubtitleParser {

    boolean canOpenSubtitleFile(@NonNull String subtitleFilename);

    @NonNull SubtitleFile parseSubtitle(@NonNull List<String> subtitleLines) throws ParsiranjeException;

}
