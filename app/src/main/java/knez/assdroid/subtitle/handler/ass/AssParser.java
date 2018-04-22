package knez.assdroid.subtitle.handler.ass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import knez.assdroid.subtitle.ParsiranjeException;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleParser;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import static knez.assdroid.subtitle.handler.ass.FormatConstants.*;

public class AssParser implements SubtitleParser {

    @NonNull private final DateTimeFormatter timeFormatter;
    // TODO: neka logika na kraju da li se dobro ucitalo ili ti neko potuiro neispravan fajl
    // tipa ako nema subtitle odeljak ili je prazan

	public AssParser() {
	    this.timeFormatter = DateTimeFormatter.ofPattern("H:mm:ss.SS");
    }

	@Override
	public boolean canOpenSubtitleFile(@NonNull String subtitleFilename) {
		return subtitleFilename.toLowerCase().endsWith(".ass");
	}

    @Override @NonNull
    public SubtitleContent parseSubtitle(@NonNull List<String> subtitleLines) throws ParsiranjeException {
		SubtitleContent subtitleContent = new SubtitleContent();
		Section currentSection = null;
		List<String> currentSectionLines = new ArrayList<>();

        for(String line : subtitleLines) {
            line = line.trim();

            // simplify - remove empty & comment lines
            if (line.equals("") || line.startsWith(";")) continue;

            if(line.startsWith("[")) {
                if(currentSection != null)
                    processSection(currentSection, currentSectionLines, subtitleContent);

                currentSection = determineSection(line);
                currentSectionLines.clear();
                continue;
            }

            currentSectionLines.add(line);
        }

        if(currentSection != null)
            processSection(currentSection, currentSectionLines, subtitleContent);

        return subtitleContent;
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

    private void processSection(@NonNull Section currentSection,
                                @NonNull List<String> currentSectionLines,
                                @NonNull SubtitleContent subtitleContent) {
        switch (currentSection) {
            case SUBTITLE_LINES:
                parseSubtitleLines(currentSectionLines, subtitleContent);
                break;
            case SCRIPT_INFO:
//                    return parsirajZaglavlje(linija);
                // TODO
                break;
            case STYLES:
//                    return parsirajStil(linija);
                // TODO
                break;
            case UNKNOWN:
                // TODO
                break;
            case FONTS:
                // TODO
                break;
            case GRAPHICS:
                // TODO
                break;
            default:
                // TODO
                break;
        }
    }

    /**
     * Parsira liniju koja specificira format svih linija sa prevodom. Belezi indekse gde je koji element i
     * to se kasnije koristi pri parsiranju redova prevoda. */
    private Map<String, Integer> getSubtitleContentIndexes(@NonNull String line) {
        String lineContent = line.substring(LINE_SUBTITLE_LINES_FORMAT.length());
        Map<String, Integer> indexMap = new HashMap<>();

        String tags[] = lineContent.split(",");
        for(int i = 0; i < tags.length; i++) tags[i] = tags[i].trim();

        int indexEffect = findTagIndex(TAG_SUBTITLE_FORMAT_EFFECT, tags);
        if(indexEffect != -1) indexMap.put(TAG_SUBTITLE_FORMAT_EFFECT, indexEffect);

        int indexEnd = findTagIndex(TAG_SUBTITLE_FORMAT_END, tags);
        if(indexEnd != -1) indexMap.put(TAG_SUBTITLE_FORMAT_END, indexEnd);

        int indexLayer = findTagIndex(TAG_SUBTITLE_FORMAT_LAYER, tags);
        if(indexLayer != -1) indexMap.put(TAG_SUBTITLE_FORMAT_LAYER, indexLayer);

        int indexMarginL = findTagIndex(TAG_SUBTITLE_FORMAT_MARGIN_L, tags);
        if(indexMarginL != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_L, indexMarginL);

        int indexMarginR = findTagIndex(TAG_SUBTITLE_FORMAT_MARGIN_R, tags);
        if(indexMarginR != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_R, indexMarginR);

        int indexMarginV = findTagIndex(TAG_SUBTITLE_FORMAT_MARGIN_V, tags);
        if(indexMarginV != -1) indexMap.put(TAG_SUBTITLE_FORMAT_MARGIN_V, indexMarginV);

        int indexName = findTagIndex(TAG_SUBTITLE_FORMAT_NAME, tags);
        if(indexName != -1) indexMap.put(TAG_SUBTITLE_FORMAT_NAME, indexName);

        int indexStart = findTagIndex(TAG_SUBTITLE_FORMAT_START, tags);
        if(indexStart != -1) indexMap.put(TAG_SUBTITLE_FORMAT_START, indexStart);

        int indexStyle = findTagIndex(TAG_SUBTITLE_FORMAT_STYLE, tags);
        if(indexStyle != -1) indexMap.put(TAG_SUBTITLE_FORMAT_STYLE, indexStyle);

        int indexText = findTagIndex(TAG_SUBTITLE_FORMAT_TEXT, tags);
        if(indexText != -1) indexMap.put(TAG_SUBTITLE_FORMAT_TEXT, indexText);

        return indexMap;
    }

    /** Returns index of the given String in the String array. */
    private int findTagIndex(String ovaj, String elementi[]) {
//        TODO probaj bez ove metode, nego koristi Arrays.binarySearch()
        for(int i = 0; i < elementi.length; i++)
            if(elementi[i].equals(ovaj)) return i;
        return -1;
    }


    // TODO: posebna klasa? za linije samo... a posle za druge stvari ako treba
    // ---------------------------------------------------------------------- SUBTITLE LINES PARSING

    private void parseSubtitleLines(
            @NonNull List<String> sectionLines, @NonNull SubtitleContent subtitleContent) {

	    if(sectionLines.size() == 0) {
	        // TODO: javi problem - missing subtitle content ili stagod
            return;
        }

        // First line in subtitle (events) section must be Format. This line defines the format of
        // the following subtitle lines.
        if(!sectionLines.get(0).startsWith(LINE_SUBTITLE_LINES_FORMAT)) {
	        // TODO: javi problem - nedostaje format linija koja po specifikaciji mora biti prva
//            dodajProblemNedostajucFormatPrevoda();
            return;
        }

        Map<String, Integer> formatIndexes = getSubtitleContentIndexes(sectionLines.get(0));
	    // TODO: vidi ako nema bar text, start i end, onda imas neispravan titl
        // takodje ako tekst nije zadnji (mora biti po specifikaciji)

        sectionLines.remove(0);


        List<SubtitleLine> subtitleLines = new ArrayList<>();
        SubtitleLine.Builder subtitleLineBuilder = new SubtitleLine.Builder();

        for(String line : sectionLines) {
            subtitleLineBuilder.reset();

            if (line.startsWith(LINE_SUBTITLE_LINES_COMMENT)) {
                subtitleLineBuilder.setIsComment(Boolean.TRUE);
                line = line.substring(LINE_SUBTITLE_LINES_COMMENT.length()).trim();
            } else if (line.startsWith(LINE_SUBTITLE_LINES_DIALOGUE)) {
                subtitleLineBuilder.setIsComment(Boolean.FALSE);
                line = line.substring(LINE_SUBTITLE_LINES_DIALOGUE.length()).trim();
            } else {
                // TODO: oznaci da si imao skroz nepoznatu liniju prevoda
//                dodajProblemNepoznatRedPrevoda();
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

            // ako nedostaje deo linije - dopuni praznim parcicima TODO kako je ovo moguce bre; ajde defanzivno jedino
//            while (lineParts.size() < odeljakaPrevoda)
//                lineParts.add("");


            // ---------- Required elements

            subtitleLineBuilder
                    .setLineNumber(subtitleLines.size() + 1)
                    .setText(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_TEXT)));

            try {
                subtitleLineBuilder.setStart(
                        LocalTime.parse(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_START)), timeFormatter));
            } catch (Exception ex) {
                // TODO: notify that the whole line is unusable because of this
                continue;
            }

            try {
                subtitleLineBuilder.setEnd(
                        LocalTime.parse(lineParts.get(formatIndexes.get(TAG_SUBTITLE_FORMAT_END)), timeFormatter));
            } catch (Exception ex) {
                // TODO: notify that the whole line is unusable because of this
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
                    // TODO: stavi u greske
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
                    // TODO: stavi u greske
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
                    // TODO: stavi u greske
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
                    // TODO: stavi u greske
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
	}














	



//    private RedZaglavlja parsirajZaglavlje(String linija) {
//        return new RedZaglavlja(linija);
//    }
//
//	private RedStila parsirajStil(String linija) {
//		return new RedStila(linija); //TODO: ne radis nista sa linijom koja odredjuje format
//	}
//
//	private void javiZavrsenoParsiranje() {
//		if(mapaProblema.size() != 0) {
//			Resources r = context.getResources();
//			if(mapaProblema.get(PROB_FORMAT_PREVODA) != null) {
//				warnString = r.getString(R.string.parsiranje_fail_nema_prevod_format_linije);
//			}
//			Integer broj = (Integer) mapaProblema.get(PROB_NEPOZNAT_RED_PREVODA);
//			if(broj != null) {
//				warnString += (warnString.length() > 0? "\n" : "")
//						+ r.getString(R.string.parsiranje_fail_problem_nepoznat_red_prevoda, broj);
//			}
//			@SuppressWarnings("unchecked")
//			List<Integer> puknuti = (List<Integer>) mapaProblema.get(PROB_PARSIRANJE_PREVODA);
//			if(puknuti != null) {
//				StringBuilder spakovan = new StringBuilder();
//				for(Integer b : puknuti)
//					spakovan.append(b).append(",");
//				spakovan.deleteCharAt(spakovan.length()-1);
//				warnString += (warnString.length() > 0? "\n" : "")
//						+ r.getString(R.string.parsiranje_fail_problem_red_prevoda, spakovan);
//			}
//		}
//		kolbek.zavrsenoParsiranje(mapaProblema.size() != 0, warnString);
//	}
	
	// --------------------------------------- Problemi
	
	/** U mapu problema ovog parsera dodaje novi zapis o problemu sa parsiranjem redova. */
//	private void dodajProblemParsiranjePrevoda(int lineNumber) {
//		List<Integer> puknuti = (List<Integer>) mapaProblema.get(PROB_PARSIRANJE_PREVODA);
//		if(puknuti == null) {
//			puknuti = new ArrayList<>();
//			mapaProblema.put(PROB_PARSIRANJE_PREVODA, puknuti);
//		}
//		puknuti.add(lineNumber);
//	}
	
//	/** U mapu problema ovog parsera dodaje novi zapis o nedostajucem redu sa formatom prevoda. */
//	private void dodajProblemNedostajucFormatPrevoda() {
//		mapaProblema.put(PROB_FORMAT_PREVODA, new Object());
//	}
	
//	private void dodajProblemNepoznatRedPrevoda() {
//		int brojNepoznatih = (Integer) mapaProblema.get(PROB_NEPOZNAT_RED_PREVODA, 0);
//		mapaProblema.put(PROB_NEPOZNAT_RED_PREVODA, ++brojNepoznatih);
//	}

}