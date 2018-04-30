package knez.assdroid.subtitle.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleFile {

    private boolean currentSubtitleEdited;
    @Nullable private final Uri uriPath;
    @Nullable private final String name;
    @NonNull private final SubtitleContent subtitleContent;

    public SubtitleFile(boolean currentSubtitleEdited,
                        @Nullable Uri uriPath,
                        @Nullable String name,
                        @NonNull SubtitleContent subtitleContent) {
        this.currentSubtitleEdited = currentSubtitleEdited;
        this.uriPath = uriPath;
        this.name = name;
        this.subtitleContent = subtitleContent;
    }

    public boolean isCurrentSubtitleEdited() { return currentSubtitleEdited; }
    @Nullable public String getName() { return name; }
    @Nullable public Uri getUriPath() { return uriPath; }
    @NonNull public SubtitleContent getSubtitleContent() { return subtitleContent; }

}
