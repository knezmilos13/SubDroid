package knez.assdroid.subtitle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import solid.collections.SolidList;

public class SubtitleHandlerRepository {

    @NonNull private final SolidList<SubtitleParser> parsers;
    @NonNull private final SolidList<SubtitleFormatter> formatters;

    public SubtitleHandlerRepository(@NonNull SolidList<SubtitleParser> parsers,
                                     @NonNull SolidList<SubtitleFormatter> formatters) {
        this.parsers = parsers;
        this.formatters = formatters;
    }

    public boolean canOpenSubtitleFile(@NonNull String subtitleFilename) {
        for(SubtitleParser parser : parsers)
            if(parser.canOpenSubtitleFile(subtitleFilename)) return true;
        return false;
    }

    /** Returns parser that can open the given file. If no parsers can open it, returns null */
    @Nullable
    public SubtitleParser getParserForSubtitleFile(@NonNull String subtitleFilename) {
        for(SubtitleParser parser : parsers)
            if(parser.canOpenSubtitleFile(subtitleFilename)) return parser;
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
