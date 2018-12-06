package knez.assdroid.subtitle.handler;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import solid.collections.SolidList;

public class SubtitleHandlerRepository {

    @NonNull private final SolidList<SubtitleParser> parsers;
    @NonNull private final SolidList<SubtitleFormatter> formatters;

    public SubtitleHandlerRepository(@NonNull Collection<SubtitleParser> parsers,
                                     @NonNull Collection<SubtitleFormatter> formatters) {
        this.parsers = new SolidList<>(parsers);
        this.formatters = new SolidList<>(formatters);
    }

    public boolean canOpenSubtitleExtension(@NonNull String subtitleExtension) {
        for(SubtitleParser parser : parsers)
            if(parser.canOpenSubtitleExtension(subtitleExtension)) return true;
        return false;
    }

    /** Returns parser that can open a file with a given extension.
     *  If no parsers can open it, returns null */
    @Nullable
    public SubtitleParser getParserForSubtitleExtension(@NonNull String subtitleExtension) {
        for(SubtitleParser parser : parsers)
            if(parser.canOpenSubtitleExtension(subtitleExtension)) return parser;
        return null;
    }

    public boolean canSaveToSubtitleFormat(@NonNull String subtitleExtension) {
        for(SubtitleFormatter formatter : formatters)
            if(formatter.canSaveToSubtitleFormat(subtitleExtension)) return true;
        return false;
    }

    /** Returns formatter that can serialize to the given extension type.
     *  If no formatters can, returns null */
    @Nullable
    public SubtitleFormatter getFormatterForSubtitleFormat(@NonNull String subtitleExtension) {
        for(SubtitleFormatter formatter : formatters)
            if(formatter.canSaveToSubtitleFormat(subtitleExtension)) return formatter;
        return null;
    }

}
