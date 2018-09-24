package knez.assdroid.util.preferences;

public interface PersistedValueWriter<T> {
    void set(T value);
    void delete();
}
