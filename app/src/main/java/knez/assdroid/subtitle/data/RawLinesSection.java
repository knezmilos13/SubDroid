package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;

import java.util.List;

/** Groups lines that won't get processed. Kept in original format in memory and saved only if the
 *  output format is the same as the input. Tag is added by the parser so that the formatter could
 *  return these lines to the right position (if that is important format-wise) */
public class RawLinesSection {

    private List<String> lines;

    private String tag;

    public RawLinesSection(@NonNull List<String> lines, @NonNull String tag) {
        this.lines = lines;
        this.tag = tag;
    }
// TODO solid list? also tag je "section name" ili tako nesto
    @NonNull public String getTag() { return tag; }
    public List<String> getLines() { return lines; }

}
