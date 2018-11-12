package knez.assdroid.subtitle.handler.ass;

import androidx.annotation.NonNull;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleFormatter;

import static knez.assdroid.subtitle.handler.ass.FormatConstants.*;

public class AssFormatter implements SubtitleFormatter {

    @NonNull private final DateTimeFormatter timeFormatter;

    public AssFormatter() {
        this.timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
    }

    @Override
    public boolean canSaveToSubtitleFormat(@NonNull String extension) {
        return extension.toLowerCase().equals("ass");
    }

    // TODO nije u praksi sposoban da serijalizaje novi titl. Moras da generises sve one script info
    // linije itd.

    @Override @NonNull
    public List<String> serializeSubtitle(@NonNull SubtitleContent subtitleContent) {
        List<String> result = new ArrayList<>();

        Map<String, List<String>> rawLinesSections = subtitleContent.getRawSections();

        result.add(SECTION_SCRIPT_INFO);

        // TODO: vidi ove "tagove". Negde stavljas stringove koje koristis u parsiranju, negde stavljas
        // ove enume koji su deo ass specifikacije; a kako ce to da skonta recimo neki drugi format?
        List<String> infoLines = rawLinesSections.get(Section.SCRIPT_INFO.name());
        if(infoLines == null || infoLines.isEmpty()) {
            // TODO moraces da generises neku default konfiguraciju minimalnu
        } else {
            result.addAll(infoLines);
            result.add(""); // newline for prettied formatting
        }

        List<String> styleLines = rawLinesSections.get(Section.STYLES.name());
        if(styleLines == null || styleLines.isEmpty()) {
            // TODO proveri u dokumentaciji - da li je stil sekcija obavezna ili ne? ako jeste digni SECTION_STYLE odozdo
        } else {
            result.add(SECTION_STYLE);
            result.addAll(styleLines); // NOTE: style format line should already be present
            result.add("");  // newline for prettied formatting
        }

        List<String> fontLines = rawLinesSections.get(Section.FONTS.name());
        if(fontLines != null && !fontLines.isEmpty()) {
            result.add(SECTION_FONTS);
            result.addAll(fontLines);
            result.add("");  // newline for prettied formatting
        }

        List<String> graphicsLines = rawLinesSections.get(Section.GRAPHICS.name());
        if(graphicsLines != null && !graphicsLines.isEmpty()) {
            result.add(SECTION_GRAPHICS);
            result.addAll(graphicsLines);
            result.add("");  // newline for prettied formatting
        }

        result.add(SECTION_SUBTITLE_LINES);
        result.add(FORMAT_LINE_IN_SUBTITLE_SECTION_DEFAULT);

        List<SubtitleLine> subtitleLines = subtitleContent.getSubtitleLines();
        for(SubtitleLine subtitleLine : subtitleLines) {
            StringBuilder builder = new StringBuilder();

            if(subtitleLine.getIsComment() != null && subtitleLine.getIsComment())
                builder.append(LINE_SUBTITLE_LINES_COMMENT);
            else if(subtitleLine.getTags().size() == 0) {
                builder.append(LINE_SUBTITLE_LINES_DIALOGUE);
            } else {
                if(subtitleLine.getTags().contains(LINE_SUBTITLE_LINES_COMMAND))
                    builder.append(LINE_SUBTITLE_LINES_COMMAND);
                else if(subtitleLine.getTags().contains(LINE_SUBTITLE_LINES_PICTURE))
                    builder.append(LINE_SUBTITLE_LINES_PICTURE);
                else if(subtitleLine.getTags().contains(LINE_SUBTITLE_LINES_MOVIE))
                    builder.append(LINE_SUBTITLE_LINES_MOVIE);
                else if(subtitleLine.getTags().contains(LINE_SUBTITLE_LINES_SOUND))
                    builder.append(LINE_SUBTITLE_LINES_SOUND);
                else // has unknown tags, probably intended for another format, just add normal line
                    builder.append(LINE_SUBTITLE_LINES_DIALOGUE);
            }

            builder.append(" ");

            Integer layer = subtitleLine.getLayer();
            builder.append(layer == null? VALUE_LAYER_DEFAULT : layer).append(",");

            builder.append(subtitleLine.getStart().format(timeFormatter)).append(",");
            builder.append(subtitleLine.getEnd().format(timeFormatter)).append(",");

            String style = subtitleLine.getStyle();
            builder.append(style == null? VALUE_STYLE_DEFAULT : style).append(",");

            String actor = subtitleLine.getActorName();
            builder.append(actor == null? VALUE_ACTOR_DEFAULT : actor).append(",");

            Integer marginL = subtitleLine.getMarginL();
            builder.append(
                    String.format(Locale.US, "%04d", marginL == null? VALUE_MARGIN_L_DEFAULT : marginL))
                    .append(",");

            Integer marginR = subtitleLine.getMarginL();
            builder.append(
                    String.format(Locale.US, "%04d", marginR == null? VALUE_MARGIN_R_DEFAULT : marginR))
                    .append(",");

            Integer marginV = subtitleLine.getMarginL();
            builder.append(
                    String.format(Locale.US, "%04d", marginV == null? VALUE_MARGIN_V_DEFAULT : marginV))
                    .append(",");

            String effect = subtitleLine.getEffect();
            builder.append(effect == null? VALUE_EFFECT_DEFAULT : effect).append(",");

            builder.append(subtitleLine.getText());

            result.add(builder.toString());
        }

        return result;
    }

}
