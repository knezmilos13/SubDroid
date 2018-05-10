package knez.assdroid.subtitle.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleFile {

    private boolean edited;
    @Nullable private final Uri uriPath; // TODO treba li ti
    @Nullable private String name;
    @NonNull private final SubtitleContent subtitleContent;

    public SubtitleFile(boolean edited,
                        @Nullable Uri uriPath,
                        @Nullable String name,
                        @NonNull SubtitleContent subtitleContent) {
        this.edited = edited;
        this.uriPath = uriPath;
        this.name = name;
        this.subtitleContent = subtitleContent;
    }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    @Nullable public String getName() { return name; }
    public void setName(@Nullable String name) { this.name = name; }

    @Nullable public Uri getUriPath() { return uriPath; }

    @NonNull public SubtitleContent getSubtitleContent() { return subtitleContent; }

}
