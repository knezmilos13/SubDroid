package knez.assdroid.common.db;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleContentDao {

    @NonNull private final BoxStore boxStore;

    public SubtitleContentDao(@NonNull BoxStore boxStore) {
        this.boxStore = boxStore;
    }

    public void storeSubtitleContent(@NonNull SubtitleContent subtitleContent) {
        List<SubtitleLine> subtitleLines = subtitleContent.getSubtitleLines();

        clearSubtitle();

        Box<SubtitleLine> boxSubLines = boxStore.boxFor(SubtitleLine.class);
        Box<RawLine> boxRawLines = boxStore.boxFor(RawLine.class);

        for(SubtitleLine subtitleLine : subtitleLines) {
            boxSubLines.put(subtitleLine);
        }

        // TODO: testirati da li ovo radi
        // TODO: ucitati na pocetku sve
        // TODO: omoguciti save/save as stagod
        RawLine rawLineEntity = new RawLine();
        Set<String> rawSectionsKeySet = subtitleContent.getRawSections().keySet();
        for(String key : rawSectionsKeySet) {
            List<String> rawLines = subtitleContent.getRawSections().get(key);
            for(String rawLine : rawLines) {
                rawLineEntity.setLine(rawLine);
                rawLineEntity.setTag(key);
                rawLineEntity.setId(0);
                boxRawLines.put(rawLineEntity);
            }

        }

    }

    public void clearSubtitle() {
        boxStore.boxFor(SubtitleLine.class).removeAll();
        boxStore.boxFor(RawLine.class).removeAll();
    }

    @NonNull
    public SubtitleContent loadSubtitleContent() {
        List<SubtitleLine> subtitleLines = boxStore.boxFor(SubtitleLine.class).getAll();
        List<RawLine> rawLines = boxStore.boxFor(RawLine.class).getAll();

        // Raw lines are stored flat in the database, so group them by section they belong to

        Map<String, List<String>> rawLinesSectionsMap = new HashMap<>();
        for(RawLine rawLine : rawLines) {
            String section = rawLine.getTag(); // TODO rename tag u section?
            if(!rawLinesSectionsMap.containsKey(section))
                rawLinesSectionsMap.put(section, new ArrayList<>());
            rawLinesSectionsMap.get(section).add(rawLine.getLine());
        }

        return new SubtitleContent(subtitleLines, rawLinesSectionsMap);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

//    @NonNull
//    private Dao<RawLinesSectionEntity, Integer> getRawLineSectionsDao() throws SQLException {
//        return databaseHelper.getDao(RawLinesSectionEntity.class);
//    }
//
//    @NonNull
//    private Dao<RawLine, Integer> getRawLineDao() throws SQLException {
//        return databaseHelper.getDao(RawLine.class);
//    }
//
//    @NonNull
//    private Dao<SubtitleLine, Integer> getSubtitleLineDao() throws SQLException {
//        return databaseHelper.getDao(SubtitleLine.class);
//    }

}