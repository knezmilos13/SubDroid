package knez.assdroid.subtitle.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleFile {

    private boolean edited;
    @Nullable private final Uri uriPath;
    @Nullable private String name;
    @Nullable private String extension;
    @NonNull private final SubtitleContent subtitleContent;

    public SubtitleFile(boolean edited,
                        @Nullable final Uri uriPath,
                        @Nullable final String name,
                        @Nullable final String extension,
                        @NonNull SubtitleContent subtitleContent) {
        this.edited = edited;
        this.uriPath = uriPath;
        this.name = name;
        this.extension = extension;
        this.subtitleContent = subtitleContent;
    }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    @Nullable public String getName() { return name; }
    @Nullable public String getExtension() { return extension; }
    @Nullable public Uri getUriPath() { return uriPath; }
    @NonNull public SubtitleContent getSubtitleContent() { return subtitleContent; }

}
