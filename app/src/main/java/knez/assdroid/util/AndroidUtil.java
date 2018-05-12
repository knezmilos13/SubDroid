package knez.assdroid.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: util klase su shit

@SuppressWarnings({"unused", "WeakerAccess"}) // library-type class, normal to have unused stuff
public class AndroidUtil {

    public static String fixLocalhostAddress(String address) {
        if(address.startsWith("http://127.0.0.1")) {
            return address.replace("http://127.0.0.1", "http://10.0.2.2");
        } else
            return address;
    }

    public static void closeKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) return;

        InputMethodManager inputManager =
                (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private static AtomicBoolean isRunningTest;
    public static synchronized boolean isRunningEspressoTest() {
        if (null == isRunningTest) {
            boolean istest;

            try {
                Class.forName ("android.support.test.espresso.Espresso");
                istest = true;
            } catch (ClassNotFoundException e) {
                istest = false;
            }

            isRunningTest = new AtomicBoolean(istest);
        }

        return isRunningTest.get();
    }

    /**
     * Vraca equal ako su oba null, ili ako je jedan null a drugi prazan. Za sve ostalo poredi
     * vrednosti svih kljuceva, rekurzivno (ako imas bundle u bundleu)
     *
     * @param one -
     * @param two -
     * @return - TRUE ako su svi elementi prosli equals poredjenje
     */
    public static boolean equalBundles(@Nullable Bundle one, @Nullable Bundle two) {
        if (one == null) return two == null || two.size() == 0;
        if (two == null) return one.size() == 0;

        if (one.size() != two.size())
            return false;

        Set<String> setOne = one.keySet();
        Object valueOne;
        Object valueTwo;

        for (String key : setOne) {
            valueOne = one.get(key);
            valueTwo = two.get(key);
            if (valueOne instanceof Bundle && valueTwo instanceof Bundle &&
                    !equalBundles((Bundle) valueOne, (Bundle) valueTwo)) {
                return false;
            } else if (valueOne == null) {
                if (valueTwo != null || !two.containsKey(key))
                    return false;
            } else if (!valueOne.equals(valueTwo))
                return false;
        }

        return true;
    }

}
