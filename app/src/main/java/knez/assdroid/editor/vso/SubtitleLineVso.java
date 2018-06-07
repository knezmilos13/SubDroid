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
    @NonNull private final SharedSettings sharedSettings;
    private boolean isPrimarySearchResult;

    public SubtitleLineVso(
            long id,
            @DrawableRes int backgroundDrawable,
            @NonNull String text,
            @NonNull String start,
            @NonNull String end,
            @Nullable String actorName,
            @Nullable String style,
            int lineNumber,
            boolean isPrimarySearchResult,
            @NonNull SharedSettings sharedSettings) {
        super(id);
        this.backgroundDrawable = backgroundDrawable;
        this.text = text;
        this.start = start;
        this.end = end;
        this.actorName = actorName;
        this.style = style;
        this.lineNumber = lineNumber;
        this.isPrimarySearchResult = isPrimarySearchResult;
        this.sharedSettings = sharedSettings;
    }

    public int getBackgroundDrawable() { return backgroundDrawable; }
    @NonNull public String getText() { return text; }
    @NonNull public String getStart() { return start; }
    @NonNull public String getEnd() { return end; }
    @Nullable public String getActorName() { return actorName; }
    @Nullable public String getStyle() { return style; }
    public int getLineNumber() { return lineNumber; }
    @NonNull public SharedSettings getSharedSettings() { return sharedSettings; }
    public boolean isPrimarySearchResult() { return isPrimarySearchResult; }

    public void setText(@NonNull String text) { this.text = text; }
    public void setPrimarySearchResult(boolean primarySearchResult) {
        isPrimarySearchResult = primarySearchResult;
    }

    public boolean isIdenticalTo(@NonNull SubtitleLineVso line) {
        return this == line || (
                getId() == line.getId()
                && backgroundDrawable == line.getBackgroundDrawable()
                && lineNumber == line.getLineNumber()
                && start.equals(line.getStart())
                && end.equals(line.getEnd())
                && Objects.equals(style, line.getStyle())
                && Objects.equals(actorName, line.getActorName())
                && text.equals(line.getText())
                && sharedSettings.isIdenticalTo(line.getSharedSettings())
        );
    }

    public static class SharedSettings {
        private int textSize;
        private int otherSize;
        private boolean showTimings;
        private boolean showActorAndStyle;
        @Nullable private String searchQuery = null;

        public SharedSettings(
                int textSize,
                int otherSize,
                boolean showTimings,
                boolean showActorAndStyle,
                @Nullable String searchQuery) {
            this.textSize = textSize;
            this.otherSize = otherSize;
            this.showTimings = showTimings;
            this.showActorAndStyle = showActorAndStyle;
            this.searchQuery = searchQuery;
        }

        public int getTextSize() { return textSize; }
        public int getOtherSize() { return otherSize; }
        public boolean isShowActorAndStyle() { return showActorAndStyle; }
        public boolean isShowTimings() { return showTimings; }
        @Nullable public String getSearchQuery() { return searchQuery; }

        public void setTextSize(int textSize) { this.textSize = textSize; }
        public void setOtherSize(int otherSize) { this.otherSize = otherSize; }
        public void setShowActorAndStyle(boolean showActorAndStyle) {
            this.showActorAndStyle = showActorAndStyle;
        }
        public void setShowTimings(boolean showTimings) {
            this.showTimings = showTimings;
        }
        public void setSearchQuery(@Nullable String searchQuery) { this.searchQuery = searchQuery; }

        public boolean isIdenticalTo(@NonNull SharedSettings settings) {
            return this == settings || (
                    textSize == settings.getTextSize()
                            && otherSize == settings.getOtherSize()
                            && showActorAndStyle == settings.isShowActorAndStyle()
                            && showTimings == settings.isShowTimings()
                            && Objects.equals(searchQuery, settings.getSearchQuery())
            );
        }
    }

}
