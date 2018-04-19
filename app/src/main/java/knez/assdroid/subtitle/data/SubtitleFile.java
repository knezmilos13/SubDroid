package knez.assdroid.subtitle.data;

import android.net.Uri;
import android.support.annotation.Nullable;

public class SubtitleFile {

    private boolean currentSubtitleEdited;
    @Nullable private final Uri uriPath;
    @Nullable private final String filename;

    public SubtitleFile(boolean currentSubtitleEdited, @Nullable Uri uriPath, @Nullable String filename) {
        this.currentSubtitleEdited = currentSubtitleEdited;
        this.uriPath = uriPath;
        this.filename = filename;
    }

    public boolean isCurrentSubtitleEdited() { return currentSubtitleEdited; }
    @Nullable public String getFilename() { return filename; }
    @Nullable public Uri getUriPath() { return uriPath; }

}
