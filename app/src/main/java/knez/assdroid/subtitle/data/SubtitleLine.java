package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import knez.assdroid.common.db.DbConverters;

@Entity
public class SubtitleLine {

    @Id private long id;

    private int lineNumber;
    private Integer layer;
    private Integer marginL;
    private Integer marginV;
    private Integer marginR;

    @NonNull @Convert(converter = DbConverters.LocalTimeConverter.class, dbType = Long.class)
    private LocalTime start;

    @NonNull @Convert(converter = DbConverters.LocalTimeConverter.class, dbType = Long.class)
    private LocalTime end;

    @Nullable private String style;
    @Nullable private String actorName;
    @Nullable private String effect;
    @NonNull private String text;
    @Nullable private Boolean isComment;

    @NonNull @Convert(converter = DbConverters.StringListConverter.class, dbType = String.class)
    private List<String> tags;

	private SubtitleLine(@NonNull Builder builder) {
        lineNumber = builder.lineNumber;
        layer = builder.layer;
        marginL = builder.marginL;
        marginV = builder.marginV;
        marginR = builder.marginR;
        start = builder.start;
        end = builder.end;
        style = builder.style;
        actorName = builder.actorName;
        effect = builder.effect;
        text = builder.text;
        isComment = builder.isComment;
        tags = builder.tags;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getLineNumber() { return lineNumber; }
    @Nullable public Integer getLayer() { return layer; }
    @Nullable public Integer getMarginL() { return marginL; }
    @Nullable public Integer getMarginV() { return marginV; }
    @Nullable public Integer getMarginR() { return marginR; }
    @NonNull public LocalTime getStart() { return start; }
    @NonNull public LocalTime getEnd() { return end; }
    @Nullable public String getStyle() { return style; }
    @Nullable public String getActorName() { return actorName; }
    @Nullable public String getEffect() { return effect; }
    @NonNull public String getText() { return text; }
    @Nullable public Boolean getIsComment() { return isComment; }
	@NonNull public List<String> getTags() { return tags; }


	// ------------------------------------------------------------------------------------- BUILDER

    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {

        // Required elements
        private int lineNumber;
        private String text;
        private LocalTime start;
        private LocalTime end;

        @Nullable private Integer layer;
        @Nullable private Integer marginL;
        @Nullable private Integer marginV;
        @Nullable private Integer marginR;
        @Nullable private String style;
        @Nullable private String actorName;
        @Nullable private String effect;
        @Nullable private Boolean isComment;
        @NonNull private final List<String> tags = new ArrayList<>();

        public Builder() {}

        public void reset() {
            lineNumber = 0;
            layer = null;
            marginL = null;
            marginV = null;
            marginR = null;
            start = null;
            end = null;
            style = null;
            effect = null;
            text = null;
            isComment = null;
            tags.clear();
        }

        public Builder setLineNumber(int val) { lineNumber = val; return this; }
        public Builder setLayer(@Nullable Integer val) { layer = val; return this; }
        public Builder setMarginL(@Nullable Integer val) { marginL = val; return this; }
        public Builder setMarginV(@Nullable Integer val) { marginV = val; return this; }
        public Builder setMarginR(@Nullable Integer val) { marginR = val; return this; }
        public Builder setStart(@Nullable LocalTime val) { start = val; return this; }
        public Builder setEnd(@Nullable LocalTime val) { end = val; return this; }
        public Builder setStyle(@Nullable String val) { style = val; return this; }
        public Builder setActorName(@Nullable String val) { actorName = val; return this; }
        public Builder setEffect(@Nullable String val) { effect = val; return this; }
        public Builder setText(@Nullable String val) { text = val; return this; }
        public Builder setIsComment(@Nullable Boolean val) { isComment = val; return this; }
        public Builder addTag(@NonNull String tag) { tags.add(tag); return this; }

        public SubtitleLine build() {
            return new SubtitleLine(this);
        }
    }

}
