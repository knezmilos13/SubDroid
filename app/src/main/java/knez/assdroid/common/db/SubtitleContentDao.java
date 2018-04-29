package knez.assdroid.common.db;

import android.support.annotation.NonNull;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import knez.assdroid.subtitle.data.RawLine;
import knez.assdroid.subtitle.data.RawLinesSection;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;

public class SubtitleContentDao {

    @NonNull private final BoxStore boxStore;

    public SubtitleContentDao(@NonNull BoxStore boxStore) {
        this.boxStore = boxStore;
    }

    public void storeSubtitleContent(@NonNull SubtitleContent subtitleContent) {
        List<SubtitleLine> subtitleLines = subtitleContent.getSubtitleLines();

        clearDatabase();

        Box<SubtitleLine> boxSubLines = boxStore.boxFor(SubtitleLine.class);
        Box<RawLine> boxRawLines = boxStore.boxFor(RawLine.class);

        for(SubtitleLine subtitleLine : subtitleLines) {
            boxSubLines.put(subtitleLine);
        }

        // TODO: testirati da li ovo radi
        // TODO: ucitati na pocetku sve
        // TODO: omoguciti save/save as stagod
        RawLine rawLineEntity = new RawLine();
        for(RawLinesSection rawSection : subtitleContent.getRawSections()) {
            List<String> rawLines = rawSection.getLines();
            for(String rawLine : rawLines) {
                rawLineEntity.setLine(rawLine);
                rawLineEntity.setTag(rawSection.getTag());
                rawLineEntity.setId(0);
                boxRawLines.put(rawLineEntity);
            }

        }

    }

    private void clearDatabase() {
        boxStore.boxFor(SubtitleLine.class).removeAll();
        // TODO i ostale kutije
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
