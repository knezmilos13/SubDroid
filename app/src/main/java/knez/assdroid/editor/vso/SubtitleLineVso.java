package knez.assdroid.editor.vso;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import knez.assdroid.common.data.IdentifiableImpl;
import knez.assdroid.editor.data.SubtitleLineSettings;

public class SubtitleLineVso extends IdentifiableImpl {

    @NonNull private final SubtitleLineSettings subtitleLineSettings;
    @DrawableRes private final int backgroundDrawable;
    @NonNull private final String text;
    @NonNull private final String start;
    @NonNull private final String end;
    @Nullable private final String actorName;
    @Nullable private final String style;
    private final int lineNumber;

    public SubtitleLineVso(
            long id,
            @NonNull SubtitleLineSettings subtitleLineSettings,
            @DrawableRes int backgroundDrawable,
            @NonNull String text,
            @NonNull String start,
            @NonNull String end,
            @Nullable String actorName,
            @Nullable String style,
            int lineNumber) {
        super(id);
        this.subtitleLineSettings = subtitleLineSettings;
        this.backgroundDrawable = backgroundDrawable;
        this.text = text;
        this.start = start;
        this.end = end;
        this.actorName = actorName;
        this.style = style;
        this.lineNumber = lineNumber;
    }

    @NonNull public SubtitleLineSettings getSubtitleLineSettings() { return subtitleLineSettings; }
    public int getBackgroundDrawable() { return backgroundDrawable; }
    @NonNull public String getText() { return text; }
    @NonNull public String getStart() { return start; }
    @NonNull public String getEnd() { return end; }
    @Nullable public String getActorName() { return actorName; }
    @Nullable public String getStyle() { return style; }
    public int getLineNumber() { return lineNumber; }

    public boolean isIdenticalTo(@NonNull SubtitleLineVso line) {
        return getId() == line.getId()
                && subtitleLineSettings == line.getSubtitleLineSettings()
                && backgroundDrawable == line.getBackgroundDrawable()
                && lineNumber == line.getLineNumber()
                && start.equals(line.getStart())
                && end.equals(line.getEnd())
                && Objects.equals(style, line.getStyle())
                && Objects.equals(actorName, line.getActorName())
                && text.equals(line.getText());
    }

}
