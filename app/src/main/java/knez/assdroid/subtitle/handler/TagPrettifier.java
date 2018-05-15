package knez.assdroid.subtitle.handler;

import android.support.annotation.NonNull;

public interface TagPrettifier {
    @NonNull String prettifyTags(@NonNull String source);
}

