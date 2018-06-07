package knez.assdroid.editor.vso;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.subtitle.data.SubtitleLine;

public class SubtitleLineVsoFactory {

    @NonNull private final DateTimeFormatter subtitleTimeFormatter;

    public SubtitleLineVsoFactory(@NonNull DateTimeFormatter subtitleTimeFormatter) {
        this.subtitleTimeFormatter = subtitleTimeFormatter;
    }

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull List<SubtitleLine> subtitleLines, @NonNull SubtitleLineVso.SharedSettings settings) {
        return createSubtitleLineVsos(subtitleLines, null, settings);
    }

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull List<SubtitleLine> subtitleLines,
            @Nullable TagPrettifier tagPrettifier,
            @NonNull SubtitleLineVso.SharedSettings settings) {
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
                            false,
                            settings)
            );
        }

        return vsos;
    }

    /** Iterates through given SubtitleLineVso objects and recreates their text strings using new
     *  tag replacement */
    public void modifyTagReplacements(@NonNull List<SubtitleLine> subtitleLines,
                                      @NonNull List<SubtitleLineVso> vsos,
                                      @NonNull String tagReplacement,
                                      @NonNull TagPrettifier tagPrettifier) {
        // TODO ako nisu iste duzine odustani, i ako nisu isti idjevi
        for(int i = 0; i < subtitleLines.size(); i++) {
            String textToShow = subtitleLines.get(i).getText();
            textToShow = tagPrettifier.prettifyTags(textToShow);
            vsos.get(i).setText(textToShow);
        }
    }

    /** Iterates through given SubtitleLineVso objects and recreates their text strings using new
     *  tag replacement */
    public void removeTagReplacements(@NonNull List<SubtitleLine> subtitleLines,
                                      @NonNull List<SubtitleLineVso> vsos) {
        // TODO ako nisu iste duzine odustani, i ako nisu isti idjevi
        for(int i = 0; i < subtitleLines.size(); i++) {
            vsos.get(i).setText(subtitleLines.get(i).getText());
        }
    }

}
