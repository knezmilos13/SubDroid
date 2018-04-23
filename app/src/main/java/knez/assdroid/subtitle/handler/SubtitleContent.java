package knez.assdroid.subtitle.handler;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleContent {

    @NonNull private List<SubtitleLine> subtitleLines = new ArrayList<>();

    public void setSubtitleLines(List<SubtitleLine> subtitleLines) {
        this.subtitleLines = subtitleLines;
    }

}
