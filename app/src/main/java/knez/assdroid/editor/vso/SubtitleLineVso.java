package knez.assdroid.editor.vso;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import knez.assdroid.common.data.IdentifiableImpl;

public class SubtitleLineVso extends IdentifiableImpl {

    @DrawableRes private final int backgroundDrawable;
    @NonNull private String text;
    @NonNull private final String start;
    @NonNull private final String end;
    @Nullable private final String actorName;
    @Nullable private final String style;
    private final int lineNumber;
    private int textSize;
    private int otherSize;
    private final boolean showTimings;
    private final boolean showActorAndStyle;

    public SubtitleLineVso(
            long id,
            @DrawableRes int backgroundDrawable,
            @NonNull String text,
            @NonNull String start,
            @NonNull String end,
            @Nullable String actorName,
            @Nullable String style,
            int lineNumber,
            int textSize,
            int otherSize,
            boolean showTimings,
            boolean showActorAndStyle) {
        super(id);
        this.backgroundDrawable = backgroundDrawable;
        this.text = text;
        this.start = start;
        this.end = end;
        this.actorName = actorName;
        this.style = style;
        this.lineNumber = lineNumber;
        this.textSize = textSize;
        this.otherSize = otherSize;
        this.showTimings = showTimings;
        this.showActorAndStyle = showActorAndStyle;
    }

    public int getBackgroundDrawable() { return backgroundDrawable; }
    @NonNull public String getText() { return text; }
    @NonNull public String getStart() { return start; }
    @NonNull public String getEnd() { return end; }
    @Nullable public String getActorName() { return actorName; }
    @Nullable public String getStyle() { return style; }
    public int getLineNumber() { return lineNumber; }
    public int getTextSize() { return textSize; }
    public int getOtherSize() { return otherSize; }
    public boolean isShowActorAndStyle() { return showActorAndStyle; }
    public boolean isShowTimings() { return showTimings; }

    public void setTextSize(int textSize) { this.textSize = textSize; }
    public void setOtherSize(int otherSize) { this.otherSize = otherSize; }
    public void setText(@NonNull String text) { this.text = text; }

    public boolean isIdenticalTo(@NonNull SubtitleLineVso line) {
        return getId() == line.getId()
                && backgroundDrawable == line.getBackgroundDrawable()
                && lineNumber == line.getLineNumber()
                && start.equals(line.getStart())
                && end.equals(line.getEnd())
                && Objects.equals(style, line.getStyle())
                && Objects.equals(actorName, line.getActorName())
                && text.equals(line.getText())
                && textSize == line.getTextSize()
                && otherSize == line.getOtherSize();
    }

}
