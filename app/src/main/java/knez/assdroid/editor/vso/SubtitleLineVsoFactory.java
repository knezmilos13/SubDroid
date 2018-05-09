package knez.assdroid.editor.vso;

import android.support.annotation.NonNull;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.subtitle.data.SubtitleLine;
import solid.collections.SolidList;

public class SubtitleLineVsoFactory {

    @NonNull private final DateTimeFormatter subtitleTimeFormatter;

    public SubtitleLineVsoFactory(@NonNull DateTimeFormatter subtitleTimeFormatter) {
        this.subtitleTimeFormatter = subtitleTimeFormatter;
    }

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull SolidList<SubtitleLine> subtitleLines,
            @NonNull SubtitleLineSettings subtitleLineSettings) {

        List<SubtitleLineVso> vsos = new ArrayList<>();
        for(SubtitleLine subtitleLine : subtitleLines) {
            String start = subtitleTimeFormatter.format(subtitleLine.getStart());
            String end = subtitleTimeFormatter.format(subtitleLine.getEnd());
            vsos.add(
                    new SubtitleLineVso(
                            subtitleLine.getId(),
                            subtitleLineSettings,
                            0,
                            subtitleLine.getText(),
                            start,
                            end,
                            subtitleLine.getActorName(),
                            subtitleLine.getStyle(),
                            subtitleLine.getLineNumber())
            );
        }

        return vsos;
    }

}
