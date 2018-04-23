package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;

import java.util.List;

import solid.collections.SolidList;

/** Groups lines that won't get processed. Kept in original format in memory and saved only if the
 *  output format is the same as the input. Tag is added by the parser so that the formatter could
 *  return these lines to the right position (if that is important format-wise) */
public class RawLinesSection {

	@NonNull private final SolidList<String> lines;
    @NonNull private final String tag;

    public RawLinesSection(@NonNull List<String> lines, @NonNull String tag) {
        this.lines = new SolidList<>(lines);
        this.tag = tag;
    }

    @NonNull public SolidList<String> getLines() { return lines; }
    @NonNull public String getTag() { return tag; }

}
