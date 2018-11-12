package knez.assdroid.util.adapter;

import androidx.annotation.NonNull;

public interface Filter {
    boolean filter(@NonNull Object entity);
}