package knez.assdroid.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JavaUtil {

    @NonNull
    public static String[] getQueryPartsFromInput(@NonNull final String input) {
        String[] originalParts = input.trim().split("\\s+"); // needs trim, otherwise " bla" becomes [ "", "bla" ]
        List<String> finalParts = new ArrayList<>(originalParts.length);

        for (String originalPart : originalParts) {
            String part = originalPart.trim().toLowerCase();
            if (part.length() > 1) finalParts.add(part);
        }

        Collections.sort(finalParts);

        return finalParts.toArray(new String[finalParts.size()]);
    }

    public static boolean areSortedStringArraysEqual(
            @NonNull final String[] first,
            @NonNull final String[] second) {
        if(first.length != second.length) return false;

        for(int i = 0; i < first.length; i++)
            if(!first[i].equals(second[i])) return false;

        return true;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     * @param tokens an array objects to be joined. Strings will be formed from
     *     the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Copied code from TextUtils.
     * @see TextUtils#join(CharSequence, Iterable)
     */
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = tokens.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(delimiter);
                sb.append(it.next());
            }
        }
        return sb.toString();
    }

    /**
     * Copied code from TextUtils.
     * @see TextUtils#isEmpty(CharSequence)
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

}
