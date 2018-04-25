package knez.assdroid.subtitle.data;

import android.net.Uri;
import android.support.annotation.NonNull;

import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleFile {

    private boolean currentSubtitleEdited;
    @NonNull private final Uri uriPath;
    @NonNull private final String filename;
    @NonNull private final SubtitleContent subtitleContent;

    public SubtitleFile(boolean currentSubtitleEdited,
                        @NonNull Uri uriPath,
                        @NonNull String filename,
                        @NonNull SubtitleContent subtitleContent) {
        this.currentSubtitleEdited = currentSubtitleEdited;
        this.uriPath = uriPath;
        this.filename = filename;
        this.subtitleContent = subtitleContent;
    }

    public boolean isCurrentSubtitleEdited() { return currentSubtitleEdited; }
    @NonNull public String getFilename() { return filename; }
    @NonNull public Uri getUriPath() { return uriPath; }
    @NonNull public SubtitleContent getSubtitleContent() { return subtitleContent; }

}
