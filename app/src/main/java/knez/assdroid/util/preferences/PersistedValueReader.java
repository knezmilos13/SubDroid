package knez.assdroid.util.preferences;

public interface PersistedValueReader<T> {
    T get();
    boolean isSet();
}