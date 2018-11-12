package knez.assdroid.common.db;

import androidx.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class RawLine {

    @Id private long id;
    private String line;
    private String section;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getLine() { return line; }
    @NonNull public String getSection() { return section; }

    public void setLine(@NonNull String line) { this.line = line; }
    public void setSection(@NonNull String section) { this.section = section; }

}
