package knez.assdroid.editor.vso;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.subtitle.handler.ass.AssTagsPrettifier;
import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleLineVsoFactory {

    @NonNull private final DateTimeFormatter subtitleTimeFormatter;

    public SubtitleLineVsoFactory(@NonNull DateTimeFormatter subtitleTimeFormatter) {
        this.subtitleTimeFormatter = subtitleTimeFormatter;
    }

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull List<SubtitleLine> subtitleLines,
            int textSizeDp,
            int otherSizeDp) {
        return createSubtitleLineVsos(subtitleLines, null, textSizeDp, otherSizeDp);
    }

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull List<SubtitleLine> subtitleLines,
            @Nullable TagPrettifier tagPrettifier,
            int textSizeDp,
            int otherSizeDp) {
        List<SubtitleLineVso> vsos = new ArrayList<>();
        for(SubtitleLine subtitleLine : subtitleLines) {
            String start = subtitleTimeFormatter.format(subtitleLine.getStart());
            String end = subtitleTimeFormatter.format(subtitleLine.getEnd());

            String textToShow = subtitleLine.getText();
            if(tagPrettifier != null) textToShow = tagPrettifier.prettifyTags(textToShow);

            vsos.add(
                    new SubtitleLineVso(
                            subtitleLine.getId(),
                            0,
                            textToShow,
                            start,
                            end,
                            subtitleLine.getActorName(),
                            subtitleLine.getStyle(),
                            subtitleLine.getLineNumber(),
                            textSizeDp,
                            otherSizeDp)
            );
        }

        return vsos;
    }

}
