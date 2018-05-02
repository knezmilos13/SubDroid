package knez.assdroid.subtitle.handler;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleContent {

    @NonNull private final List<SubtitleLine> subtitleLines;
    @NonNull private final Map<String, List<String>> rawSections;

    public SubtitleContent(@NonNull List<SubtitleLine> subtitleLines,
                           @NonNull Map<String, List<String>> rawSections) {
        this.subtitleLines = subtitleLines;
        this.rawSections = rawSections;
    }

    @NonNull public List<SubtitleLine> getSubtitleLines() { return subtitleLines; }
    @NonNull public Map<String, List<String>> getRawSections() { return rawSections; }

}
