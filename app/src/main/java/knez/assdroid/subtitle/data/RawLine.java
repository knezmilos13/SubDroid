package knez.assdroid.subtitle.data;

import android.support.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class RawLine {

    @Id private long id;
    @NonNull private String line;
    @NonNull private String tag;

    @NonNull public String getLine() { return line; }
    @NonNull public String getTag() { return tag; }

    public void setLine(@NonNull String line) {
        this.line = line;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
