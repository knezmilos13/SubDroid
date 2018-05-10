package knez.assdroid.util.adapter;

import android.support.annotation.NonNull;

public interface Filter {
    boolean filter(@NonNull Object entity);
}