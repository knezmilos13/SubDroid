package knez.assdroid.editor.vso;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.subtitle.data.SubtitleLine;
import solid.collections.SolidList;

public class SubtitleLineVsoFactory {

    public List<SubtitleLineVso> createSubtitleLineVsos(
            @NonNull SolidList<SubtitleLine> subtitleLines,
            @NonNull SubtitleLineSettings subtitleLineSettings) {

        List<SubtitleLineVso> vsos = new ArrayList<>();
        for(SubtitleLine subtitleLine : subtitleLines) {
            vsos.add(new SubtitleLineVso(
                    subtitleLine.getLineNumber(), subtitleLineSettings, 0, subtitleLine.getText()));
        }

        return vsos;
    }

}
