package knez.assdroid.subtitle.handler.ass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleParser;
import solid.collections.Pair;

import androidx.annotation.NonNull;

import static knez.assdroid.subtitle.handler.ass.FormatConstants.*;

public class AssParser implements SubtitleParser {

    @NonNull private final TextSectionParser textSectionParser;

    public AssParser(@NonNull TextSectionParser textSectionParser) {
	    this.textSectionParser = textSectionParser;
    }

	@Override
	public boolean canOpenSubtitleExtension(@NonNull String subtitleExtension) {
		return subtitleExtension.toLowerCase().equals("ass");
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
                    allSections.put(currentSection, new ArrayList<>(currentSectionLines));

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
        Map<String, List<String>> rawLinesSections = new HashMap<>();

        if(!allSections.containsKey(Section.SUBTITLE_LINES)) {
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            Pair<List<SubtitleLine>, List<ParsingError>> parsingResult =
                    textSectionParser.parseSubtitleLines(allSections.get(Section.SUBTITLE_LINES));
            subtitleLines.addAll(parsingResult.first);
            allParsingErrors.addAll(parsingResult.second);
        }

        if(!allSections.containsKey(Section.SCRIPT_INFO)) {
            // We can work without this section, but still best report that something is wrong
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.NON_SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            rawLinesSections.put(
                    Section.SCRIPT_INFO.toString(), allSections.get(Section.SCRIPT_INFO));
        }

        if(!allSections.containsKey(Section.STYLES)) {
            // We can work without this section, but still best report that something is wrong
            allParsingErrors.add(new ParsingError(
                    ParsingError.ErrorLocation.NON_SUBTITLE_SECTION,
                    ParsingError.ErrorLevel.SECTION_INVALID));
        } else {
            rawLinesSections.put(Section.STYLES.toString(), allSections.get(Section.STYLES));
        }

        if(allSections.containsKey(Section.FONTS))
            rawLinesSections.put(Section.FONTS.toString(), allSections.get(Section.FONTS));

        if(allSections.containsKey(Section.GRAPHICS))
            rawLinesSections.put(Section.GRAPHICS.toString(), allSections.get(Section.GRAPHICS));

        if(allSections.containsKey(Section.UNKNOWN))
            rawLinesSections.put(Section.UNKNOWN.toString(), allSections.get(Section.UNKNOWN));

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

}
