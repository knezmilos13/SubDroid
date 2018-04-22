package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;

public class ParsingError {

    public enum ErrorLocation { SUBTITLE_SECTION, NON_SUBTITLE_SECTION }
    public enum ErrorLevel { FILE_INVALID, SECTION_INVALID, LINE_INVALID, LINE_SANITIZED;}
    public enum ErrorType { MISSING_SECTION_CONTENT, INVALID_FORMAT }

    @NonNull private final ErrorLocation errorLocation;
    @NonNull private final ErrorType errorType;
    @NonNull private final ErrorLevel errorLevel;

    public ParsingError(@NonNull ErrorLocation errorLocation,
                        @NonNull ErrorLevel errorLevel,
                        @NonNull ErrorType errorType) {
        this.errorLocation = errorLocation;
        this.errorLevel = errorLevel;
        this.errorType = errorType;
    }

}
