package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class SubtitleLine {

    private final Integer id = null; // TODO sta je ovo
    private final int lineNumber;
    @Nullable private final Integer layer;
    @Nullable private final Integer marginL;
    @Nullable private final Integer marginV;
    @Nullable private final Integer marginR;
    @NonNull private final LocalTime start;
    @NonNull private final LocalTime end;
    @Nullable private final String style;
    @Nullable private final String actorName;
    @Nullable private final String effect;
    @NonNull private final String text;
    @Nullable private final Boolean isComment;
	@NonNull private final List<String> tags;

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

    public Integer getId() { return id; }
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
    @Nullable public Boolean getComment() { return isComment; }
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


//	public ContentValues dajVrednostiZaBazu() {
//		ContentValues cv = new ContentValues();
//		cv.put(K_LINE, lineNumber);
//		cv.put(K_LAYER, layer);
//		cv.put(K_MARGIN_L, marginL);
//		cv.put(K_MARGIN_V, marginV);
//		cv.put(K_MARGIN_R, marginR);
//		cv.put(K_START, start);
//		cv.put(K_END, end);
//		cv.put(K_STYLE, style);
//		cv.put(K_ACTOR_NAME, actorName);
//		cv.put(K_EFFECT, effect);
//		cv.put(K_TEXT, text);
//		cv.put(K_KOMENTAR, isComment ? 1 : 0);
//		return cv;
//	}

//	public static final String IME_TABELE = "red_prevoda";
//	public static final String K_ID = "_id";
//	public static final String K_LINE = "line_number";
//	public static final String K_LAYER = "layer";
//	public static final String K_MARGIN_L = "margin_left";
//	public static final String K_MARGIN_V = "margin_vertical";
//	public static final String K_MARGIN_R = "margin_right";
//	public static final String K_START = "start_time";
//	public static final String K_END = "end_time";
//	public static final String K_STYLE = "style";
//	public static final String K_ACTOR_NAME = "actor_name";
//	public static final String K_EFFECT = "effect";
//	public static final String K_TEXT = "text";
//	public static final String K_KOMENTAR = "comment";
//	public static final String SEMA =
//			"CREATE TABLE " + IME_TABELE + " ("
//			+ K_ID + " INTEGER PRIMARY KEY,"
//			+ K_LINE + " INTEGER,"
//			+ K_LAYER + " INTEGER DEFAULT 0,"
//			+ K_MARGIN_L + " INTEGER DEFAULT 0,"
//			+ K_MARGIN_R + " INTEGER DEFAULT 0,"
//			+ K_MARGIN_V + " INTEGER DEFAULT 0,"
//			+ K_START + " INTEGER DEFAULT 0,"
//			+ K_END + " INTEGER DEFAULT 0,"
//			+ K_STYLE + " TEXT," //TODO integer, referenca na id iz tabele stilovi
//			+ K_ACTOR_NAME + " TEXT,"
//			+ K_EFFECT + " TEXT,"
//			+ K_TEXT + " TEXT,"
//			+ K_KOMENTAR + " INTEGER DEFAULT 0);";


//	public static SubtitleLine kreirajIzKursora(Cursor kurs) {
//		if(!indeksiSetovani) setujIndekse(kurs);
//		SubtitleLine red = new SubtitleLine();
//		red.id = kurs.getInt(indexID);
//		red.lineNumber = kurs.getInt(indexLine);
//		red.start = kurs.getInt(indexStart);
//		red.end = kurs.getInt(indexEnd);
//		red.style = kurs.getString(indexStyle);
//		red.actorName = kurs.getString(indexActor);
//		red.layer = kurs.getInt(indexLayer);
//		red.marginL = kurs.getInt(indexMarginL);
//		red.marginR = kurs.getInt(indexMarginR);
//		red.marginV = kurs.getInt(indexMarginV);
//		red.text = kurs.getString(indexText);
//		red.effect = kurs.getString(indexEffect);
//		red.isComment = kurs.getInt(indexKomentar) == 1;
//		return red;
//	}

//	private static int indexID, indexLine, indexLayer, indexMarginL, indexMarginR, indexMarginV, indexStart,
//	indexEnd, indexStyle, indexActor, indexEffect, indexText, indexKomentar;
//	private static boolean indeksiSetovani = false;
//	public static void setujIndekse(Cursor kursor) {
//		if(indeksiSetovani) {
//			return;
//		}
//		indexID = kursor.getColumnIndex(K_ID);
//		indexLine = kursor.getColumnIndex(K_LINE);
//		indexLayer = kursor.getColumnIndex(K_LAYER);
//		indexMarginL = kursor.getColumnIndex(K_MARGIN_L);
//		indexMarginR = kursor.getColumnIndex(K_MARGIN_R);
//		indexMarginV = kursor.getColumnIndex(K_MARGIN_V);
//		indexStart = kursor.getColumnIndex(K_START);
//		indexEnd = kursor.getColumnIndex(K_END);
//		indexStyle = kursor.getColumnIndex(K_STYLE);
//		indexActor = kursor.getColumnIndex(K_ACTOR_NAME);
//		indexEffect = kursor.getColumnIndex(K_EFFECT);
//		indexText = kursor.getColumnIndex(K_TEXT);
//		indexKomentar = kursor.getColumnIndex(K_KOMENTAR);
//		indeksiSetovani = true;
//	}
}
