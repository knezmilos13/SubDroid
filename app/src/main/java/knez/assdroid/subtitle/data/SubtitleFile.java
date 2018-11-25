package knez.assdroid.subtitle.data;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleFile {

    private final boolean edited;
    @Nullable private final Uri uriPath;
    @Nullable private final String name;
    @Nullable private final String extension;
    @NonNull private final SubtitleContent subtitleContent;
    private final boolean headerLoaded;
    private final boolean contentLoaded;

    public SubtitleFile() {
        this(false, null, null, null, null, false, false);
    }

    public SubtitleFile(boolean edited,
                        @Nullable final Uri uriPath,
                        @Nullable final String name,
                        @Nullable final String extension,
                        @Nullable SubtitleContent subtitleContent,
                        boolean headerLoaded,
                        boolean contentLoaded) {
        this.edited = edited;
        this.uriPath = uriPath;
        this.name = name;
        this.extension = extension;
        this.subtitleContent = subtitleContent;
        this.headerLoaded = headerLoaded;
        this.contentLoaded = contentLoaded;
    }

    public boolean isEdited() { return edited; }
    public boolean isContentLoaded() { return contentLoaded; }
    public boolean isHeaderLoaded() { return headerLoaded; }

    @Nullable public String getName() { return name; }
    @Nullable public String getExtension() { return extension; }
    @Nullable public Uri getUriPath() { return uriPath; }
    @Nullable public SubtitleContent getSubtitleContent() { return subtitleContent; }

}
