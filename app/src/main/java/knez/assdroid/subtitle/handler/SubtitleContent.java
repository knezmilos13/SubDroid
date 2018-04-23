package knez.assdroid.subtitle.handler;

import android.support.annotation.NonNull;

import java.util.List;

import knez.assdroid.subtitle.data.RawLinesSection;
import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleContent {

    @NonNull private final List<SubtitleLine> subtitleLines;
    @NonNull private final List<RawLinesSection> rawSections;

    public SubtitleContent(@NonNull List<SubtitleLine> subtitleLines,
                           @NonNull List<RawLinesSection> rawSections) {
        this.subtitleLines = subtitleLines;
        this.rawSections = rawSections;
    }

}
