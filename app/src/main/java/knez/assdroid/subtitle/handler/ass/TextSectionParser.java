package knez.assdroid.subtitle.handler.ass;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleLine;
import solid.collections.Pair;

import static knez.assdroid.subtitle.handler.ass.FormatConstants.*;

/** Parses the actual textual part of the subtitle */
public class TextSectionParser {

    @NonNull private final DateTimeFormatter timeFormatter;

	public TextSectionParser() {
	    this.timeFormatter = DateTimeFormatter.ofPattern("H:mm:ss.SS");
    }

    Pair<List<SubtitleLine>, List<ParsingError>> parseSubtitleLines(
            @NonNull List<String> rawLines) {

        List<ParsingError> parsingErrors = new ArrayList<>();
        List<SubtitleLine> subtitleLines = new ArrayList<>();
        Pair<List<SubtitleLine>, List<ParsingError>> result =
                new Pair<>(subtitleLines, parsingErrors);

        // First line in subtitle (events) section must be Format. This line defines the format of
        // the following subtitle lines.
        if(rawLines.size() == 0 || !rawLines.get(0).startsWith(LINE_SUBTITLE_LINES_FORMAT)) {
            parsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
            return result;
        }

        // Text, start and end are the required elements. If any of them is missing from the format
        // specification, give up, since that means we are not capable of producing a working subtitle
        Map<String, Integer> formatIndexes = getSubtitleContentIndexes(rawLines.get(0));
        if(!formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_TEXT)
                || !formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_START)
                || !formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_END)) {
            parsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
            return result;
        }

        rawLines.remove(0);


        SubtitleLine.Builder subtitleLineBuilder = new SubtitleLine.Builder();

        for(String line : rawLines) {
            subtitleLineBuilder.reset();

            if (line.startsWith(LINE_SUBTITLE_LINES_COMMENT)) {
                subtitleLineBuilder.setIsComment(Boolean.TRUE);
                line = line.substring(LINE_SUBTITLE_LINES_COMMENT.length()).trim();
            }
            else if (line.startsWith(LINE_SUBTITLE_LINES_DIALOGUE)) {
                subtitleLineBuilder.setIsComment(Boolean.FALSE);
                line = line.substring(LINE_SUBTITLE_LINES_DIALOGUE.length()).trim();
            }
            else if(line.startsWith(LINE_SUBTITLE_LINES_PICTURE)) {
                subtitleLineBuilder.addTag(LINE_SUBTITLE_LINES_PICTURE);
                line = line.substring(LINE_SUBTITLE_LINES_PICTURE.length()).trim();
            }
            else if(line.startsWith(LINE_SUBTITLE_LINES_SOUND)) {
                subtitleLineBuilder.addTag(LINE_SUBTITLE_LINES_SOUND);
                line = line.substring(LINE_SUBTITLE_LINES_SOUND.length()).trim();
            }
            else if(line.startsWith(LINE_SUBTITLE_LINES_MOVIE)) {
                subtitleLineBuilder.addTag(LINE_SUBTITLE_LINES_MOVIE);
                line = line.substring(LINE_SUBTITLE_LINES_MOVIE.length()).trim();
            }
            else if(line.startsWith(LINE_SUBTITLE_LINES_COMMAND)) {
                subtitleLineBuilder.addTag(LINE_SUBTITLE_LINES_COMMAND);
                line = line.substring(LINE_SUBTITLE_LINES_COMMAND.length()).trim();
            }
            else {
                parsingErrors.add(new ParsingError(
                        ParsingError.ErrorLocation.SUBTITLE_SECTION,
                        ParsingError.ErrorLevel.LINE_INVALID,
                        line));
                continue;
            }

            int numCommas = 0;
            int lastEndIndex = 0;
            List<String> lineParts = new LinkedList<>();
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == ',') {
                    lineParts.add(line.substring(lastEndIndex, i));
                    lastEndIndex = i + 1; // +1 in order to skip the following comma
                    numCommas++;
                    if (numCommas == formatIndexes.size() - 1) {
                        lineParts.add(line.substring(i + 1)); // also +1 for comma
                        break;
                    }
                }
            }

            // A part of line is missing. Skip the line.
            if(lineParts.size() < formatIndexes.size()) {
                parsingErrors.add(new ParsingError(
                        ParsingError.ErrorLocation.SUBTITLE_SECTION,
                        ParsingError.ErrorLevel.LINE_INVALID,
                        line));
                continue;
            }


            // ---------- Required elements

            subtitleLineBuilder
                    .setLineNumber(subtitleLines.size() + 1)
                    .setText(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_TEXT)));

            try {
                subtitleLineBuilder.setStart(
                        LocalTime.parse(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_START)), timeFormatter));
            } catch (Exception ex) {
                parsingErrors.add(new ParsingError(
                        ParsingError.ErrorLocation.SUBTITLE_SECTION,
                        ParsingError.ErrorLevel.LINE_INVALID,
                        line));
                continue;
            }

            try {
                subtitleLineBuilder.setEnd(
                        LocalTime.parse(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_END)), timeFormatter));
            } catch (Exception ex) {
                parsingErrors.add(new ParsingError(
                        ParsingError.ErrorLocation.SUBTITLE_SECTION,
                        ParsingError.ErrorLevel.LINE_INVALID,
                        line));
                continue;
            }


            // ---------- Non-required elements
            // In theory a different version of ASS specification could remove/replace these.
            // They are not critical for our app.

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_EFFECT))
                subtitleLineBuilder.setEffect(
                        lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_EFFECT)));

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_LAYER)) {
                int layer;
                try {
                    layer = Integer.parseInt(
                            lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_LAYER)));
                } catch (Exception ex) {
                    parsingErrors.add(new ParsingError(
                            ParsingError.ErrorLocation.SUBTITLE_SECTION,
                            ParsingError.ErrorLevel.VALUE_SANITIZED,
                            line));
                    layer = VALUE_LAYER_DEFAULT;
                }
                subtitleLineBuilder.setLayer(layer);
            }

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_MARGIN_L)) {
                int marginL;
                try {
                    marginL = Integer.parseInt(
                            lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_MARGIN_L)));
                } catch (Exception ex) {
                    parsingErrors.add(new ParsingError(
                            ParsingError.ErrorLocation.SUBTITLE_SECTION,
                            ParsingError.ErrorLevel.VALUE_SANITIZED,
                            line));
                    marginL = VALUE_MARGIN_L_DEFAULT;
                }
                subtitleLineBuilder.setMarginL(marginL);
            }

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_MARGIN_R)) {
                int marginR;
                try {
                    marginR = Integer.parseInt(
                            lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_MARGIN_R)));
                } catch (Exception ex) {
                    parsingErrors.add(new ParsingError(
                            ParsingError.ErrorLocation.SUBTITLE_SECTION,
                            ParsingError.ErrorLevel.VALUE_SANITIZED,
                            line));
                    marginR = VALUE_MARGIN_R_DEFAULT;
                }
                subtitleLineBuilder.setMarginR(marginR);
            }

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_MARGIN_V)) {
                int marginV;
                try {
                    marginV = Integer.parseInt(
                            lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_MARGIN_V)));
                } catch (Exception ex) {
                    parsingErrors.add(new ParsingError(
                            ParsingError.ErrorLocation.SUBTITLE_SECTION,
                            ParsingError.ErrorLevel.VALUE_SANITIZED,
                            line));
                    marginV = VALUE_MARGIN_V_DEFAULT;
                }
                subtitleLineBuilder.setMarginV(marginV);
            }

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_NAME))
                subtitleLineBuilder.setActorName(
                        lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_NAME)));

            if(formatIndexes.containsKey(TAG_SUBTITLE_FORMAT_STYLE))
                subtitleLineBuilder.setStyle(
                        lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_STYLE)));

            subtitleLines.add(subtitleLineBuilder.build());
        }

        return result;
    }

    /** Parses line that specifies column order for subtitle ("events") section. Determines which
     *  value is at which position. */
    private Map<String, Integer> getSubtitleContentIndexes(@NonNull String line) {
        String lineContent = line.substring(LINE_SUBTITLE_LINES_FORMAT.length());
        Map<String, Integer> indexMap = new HashMap<>();

        List<String> tags = Arrays.asList(lineContent.split(","));
        for(int i = 0; i < tags.size(); i++) tags.set(i, tags.get(i).trim());

        int indexEffect = tags.indexOf(TAG_SUBTITLE_FORMAT_EFFECT);
        if(indexEffect != -1) indexMap.put(TAG_SUBTITLE_FORMAT_EFFECT, indexEffect);

        int indexEnd = tags.indexOf(TAG_SUBTITLE_FORMAT_END);
        if(indexEnd != -1) indexMap.put(TAG_SUBTITLE_FORMAT_END, indexEnd);

        int indexLayer = tags.indexOf(TAG_SUBTITLE_FORMAT_LAYER);
        if(indexLayer != -1) indexMap.put(TAG_SUBTITLE_FORMAT_LAYER, indexLayer);

        int indexMarginL = tags.indexOf(TAG_SUBTITLE_FORMAT_MARGIN_L);
        if(indexMarginL != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_L, indexMarginL);

        int indexMarginR = tags.indexOf(TAG_SUBTITLE_FORMAT_MARGIN_R);
        if(indexMarginR != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_R, indexMarginR);

        int indexMarginV = tags.indexOf(TAG_SUBTITLE_FORMAT_MARGIN_V);
        if(indexMarginV != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_V, indexMarginV);

        int indexName = tags.indexOf(TAG_SUBTITLE_FORMAT_NAME);
        if(indexName != -1) indexMap.put(TAG_SUBTITLE_FORMAT_NAME, indexName);

        int indexStart = tags.indexOf(TAG_SUBTITLE_FORMAT_START);
        if(indexStart != -1) indexMap.put(TAG_SUBTITLE_FORMAT_START, indexStart);

        int indexStyle = tags.indexOf(TAG_SUBTITLE_FORMAT_STYLE);
        if(indexStyle != -1) indexMap.put(TAG_SUBTITLE_FORMAT_STYLE, indexStyle);

        int indexText = tags.indexOf(TAG_SUBTITLE_FORMAT_TEXT);
        if(indexText != -1) indexMap.put(TAG_SUBTITLE_FORMAT_TEXT, indexText);

        return indexMap;
    }

}
