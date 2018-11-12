package knez.assdroid.subtitle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParsingError {

    public enum ErrorLocation { SUBTITLE_SECTION, NON_SUBTITLE_SECTION }
    public enum ErrorLevel { FILE_INVALID, SECTION_INVALID, LINE_INVALID, VALUE_SANITIZED;}

    @NonNull private final ErrorLocation errorLocation;
    @NonNull private final ErrorLevel errorLevel;
    @Nullable private final String invalidData;

    public ParsingError(@NonNull ErrorLocation errorLocation,
                        @NonNull ErrorLevel errorLevel) {
        this(errorLocation, errorLevel, null);
    }

    public ParsingError(@NonNull ErrorLocation errorLocation,
                        @NonNull ErrorLevel errorLevel,
                        @Nullable String invalidData) {
        this.errorLocation = errorLocation;
        this.errorLevel = errorLevel;
        this.invalidData = invalidData;
    }

}
