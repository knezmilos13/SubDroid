package knez.assdroid.subtitle.handler.ass;

public interface FormatConstants {

    String SECTION_SUBTITLE_LINES = "[Events]";
    String SECTION_STYLE = "[V4+ styles]";
    String SECTION_STYLE_OLD = "[V4 styles]"; // TODO da lepo zajebemo ovo? ne znam ni sta da radim sa njime. Konvertovati u + ako moze
    String SECTION_SCRIPT_INFO = "[Script info]";
    String SECTION_FONTS = "[Fonts]";
    String SECTION_GRAPHICS = "[Graphics]";

    // Starting parts of various line types
    String LINE_SUBTITLE_LINES_FORMAT = "Format:";
    String LINE_SUBTITLE_LINES_COMMENT = "Comment:";
    String LINE_SUBTITLE_LINES_DIALOGUE = "Dialogue:";
    String LINE_SUBTITLE_LINES_PICTURE = "Picture:";
    String LINE_SUBTITLE_LINES_MOVIE = "Movie:";
    String LINE_SUBTITLE_LINES_SOUND = "Sound:";
    String LINE_SUBTITLE_LINES_COMMAND = "Command:";

    String TAG_SUBTITLE_FORMAT_LAYER = "Layer";
    String TAG_SUBTITLE_FORMAT_START = "Start";
    String TAG_SUBTITLE_FORMAT_END = "End";
    String TAG_SUBTITLE_FORMAT_STYLE = "Style";
    String TAG_SUBTITLE_FORMAT_NAME = "Name";
    String TAG_SUBTITLE_FORMAT_MARGIN_L = "MarginL";
    String TAG_SUBTITLE_FORMAT_MARGIN_R = "MarginR";
    String TAG_SUBTITLE_FORMAT_MARGIN_V = "MarginV";
    String TAG_SUBTITLE_FORMAT_EFFECT = "Effect";
    String TAG_SUBTITLE_FORMAT_TEXT = "Text";

    String FORMAT_LINE_IN_SUBTITLE_SECTION_DEFAULT =
            "Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text";

    int VALUE_LAYER_DEFAULT = 0;
    int VALUE_MARGIN_L_DEFAULT = 0;
    int VALUE_MARGIN_R_DEFAULT = 0;
    int VALUE_MARGIN_V_DEFAULT = 0;
    String VALUE_STYLE_DEFAULT = "Default";
    String VALUE_ACTOR_DEFAULT = "";
    String VALUE_EFFECT_DEFAULT = "";

    String TIME_FORMAT = "H:mm:ss.SS";

}
