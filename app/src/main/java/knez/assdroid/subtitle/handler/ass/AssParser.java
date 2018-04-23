package knez.assdroid.subtitle.handler.ass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.RawLinesSection;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleParser;
import solid.collections.Pair;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import static knez.assdroid.subtitle.handler.ass.FormatConstants.*;

public class AssParser implements SubtitleParser {

    @NonNull private final DateTimeFormatter timeFormatter;

	public AssParser() {
	    this.timeFormatter = DateTimeFormatter.ofPattern("H:mm:ss.SS");
    }

	@Override
	public boolean canOpenSubtitleFile(@NonNull String subtitleFilename) {
		return subtitleFilename.toLowerCase().endsWith(".ass");
	}

    @Override @NonNull
    public Pair<SubtitleContent, List<ParsingError>> parseSubtitle(@NonNull List<String> fileLines) {
	    // ----- First: split all file lines into different sections

		Section currentSection = null;
		List<String> currentSectionLines = new ArrayList<>();
		Map<Section, List<String>> allSections = new HashMap<>();

        for(String line : fileLines) {
            line = line.trim();

            // simplify - remove empty & comment lines
            if (line.equals("") || line.startsWith(";")) continue;

            if(line.startsWith("[")) {
                if(currentSection != null)
                    allSections.put(currentSection, currentSectionLines);

                currentSection = determineSection(line);
                currentSectionLines.clear();
                continue;
            }

            currentSectionLines.add(line);
        }

        if(currentSection != null)
            allSections.put(currentSection, currentSectionLines);


        // ----- Parse each section in its own specific way

        List<ParsingError> allParsingErrors = new ArrayList<>();
        List<SubtitleLine> subtitleLines = new ArrayList<>();
        List<RawLinesSection> rawLinesSections = new ArrayList<>();

        if(!allSections.containsKey(Section.SUBTITLE_LINES)) {
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            Pair<List<SubtitleLine>, List<ParsingError>> parsingResult =
                    parseSubtitleLines(allSections.get(Section.SUBTITLE_LINES));
            subtitleLines.addAll(parsingResult.first);
            allParsingErrors.addAll(parsingResult.second);
        }

        if(!allSections.containsKey(Section.SCRIPT_INFO)) {
            // We can work without this section, but still best report that something is wrong
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.NON_SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            rawLinesSections.add(new RawLinesSection(
                    allSections.get(Section.SCRIPT_INFO), Section.SCRIPT_INFO.toString()));
        }

        if(!allSections.containsKey(Section.STYLES)) {
            // We can work without this section, but still best report that something is wrong
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.NON_SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            rawLinesSections.add(new RawLinesSection(
                    allSections.get(Section.STYLES), Section.STYLES.toString()));
        }

        if(allSections.containsKey(Section.FONTS))
            rawLinesSections.add(new RawLinesSection(
                    allSections.get(Section.FONTS), Section.FONTS.toString()));

        if(allSections.containsKey(Section.GRAPHICS))
            rawLinesSections.add(new RawLinesSection(
                    allSections.get(Section.GRAPHICS), Section.GRAPHICS.toString()));

        if(allSections.containsKey(Section.UNKNOWN))
            rawLinesSections.add(new RawLinesSection(
                    allSections.get(Section.UNKNOWN), Section.UNKNOWN.toString()));

        return new Pair<>(new SubtitleContent(subtitleLines, rawLinesSections), allParsingErrors);
    }


    // ----------------------------------------------------------------------------- SECTION PARSING

    private Section determineSection(@NonNull String line) {
        String lineLowerCase = line.toLowerCase();

        if(lineLowerCase.equals(SECTION_SUBTITLE_LINES.toLowerCase()))
            return Section.SUBTITLE_LINES;
        else if(lineLowerCase.equals(SECTION_STYLE.toLowerCase())
                || lineLowerCase.equals(SECTION_STYLE_OLD.toLowerCase()))
            return Section.STYLES;
        else if(lineLowerCase.equals(SECTION_SCRIPT_INFO.toLowerCase()))
            return Section.SCRIPT_INFO;
        else if(lineLowerCase.equals(SECTION_FONTS.toLowerCase()))
            return Section.FONTS;
        else if(lineLowerCase.equals(SECTION_GRAPHICS.toLowerCase()))
            return Section.GRAPHICS;
        else
            return Section.UNKNOWN;
    }

    /**
     * Parsira liniju koja specificira format svih linija sa prevodom. Belezi indekse gde je koji element i
     * to se kasnije koristi pri parsiranju redova prevoda. */
    private Map<String, Integer> getSubtitleContentIndexes(@NonNull String line) {
        String lineContent = line.substring(LINE_SUBTITLE_LINES_FORMAT.length());
        Map<String, Integer> indexMap = new HashMap<>();

        String tags[] = lineContent.split(",");
        for(int i = 0; i < tags.length; i++) tags[i] = tags[i].trim();

        int indexEffect = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_EFFECT);
        if(indexEffect > -1) indexMap.put(TAG_SUBTITLE_FORMAT_EFFECT, indexEffect);

        int indexEnd = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_END);
        if(indexEnd > -1) indexMap.put(TAG_SUBTITLE_FORMAT_END, indexEnd);

        int indexLayer = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_LAYER);
        if(indexLayer > -1) indexMap.put(TAG_SUBTITLE_FORMAT_LAYER, indexLayer);

        int indexMarginL = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_MARGIN_L);
        if(indexMarginL > -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_L, indexMarginL);

        int indexMarginR = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_MARGIN_R);
        if(indexMarginR > -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_R, indexMarginR);

        int indexMarginV = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_MARGIN_V);
        if(indexMarginV > -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_V, indexMarginV);

        int indexName = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_NAME);
        if(indexName > -1) indexMap.put(TAG_SUBTITLE_FORMAT_NAME, indexName);

        int indexStart = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_START);
        if(indexStart > -1) indexMap.put(TAG_SUBTITLE_FORMAT_START, indexStart);

        int indexStyle = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_STYLE);
        if(indexStyle > -1) indexMap.put(TAG_SUBTITLE_FORMAT_STYLE, indexStyle);

        int indexText = Arrays.binarySearch(tags, TAG_SUBTITLE_FORMAT_TEXT);
        if(indexText > -1) indexMap.put(TAG_SUBTITLE_FORMAT_TEXT, indexText);

        return indexMap;
    }


    // ---------------------------------------------------------------------- SUBTITLE LINES PARSING

    private Pair<List<SubtitleLine>, List<ParsingError>> parseSubtitleLines(
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

}
