package knez.assdroid.util.gui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;

@SuppressWarnings("WeakerAccess") // library type class
public class FadeAnimationHelper {

    private static int shortAnimTime = 0;

    public static void animateViewSwitch(
            final boolean show,
            @NonNull final View original,
            @NonNull final View toShow,
            final int animTime) {

        original.animate().cancel();
        toShow.animate().cancel();

        if(!show) {
            original.setAlpha(0);
            original.setVisibility(View.VISIBLE);
        }
        original.animate().setDuration(animTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) { // zove se i po cancelu
                if (show) {
                    original.setVisibility(View.GONE);
                    original.setAlpha(1); // vrati alpha na 1 da bi mogao da cackas rucno po potrebi
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                original.setVisibility(show? View.GONE : View.VISIBLE);
                original.setAlpha(1);
            }
        });

        if(show) {
            toShow.setAlpha(0);
            toShow.setVisibility(View.VISIBLE);
        }
        toShow.animate().setDuration(animTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    toShow.setVisibility(View.GONE);
                    toShow.setAlpha(1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                toShow.setVisibility(show? View.VISIBLE : View.GONE);
                toShow.setAlpha(1);
            }
        });
    }

    public static void animateViewSwitch(final boolean show,
                                         @NonNull final View original,
                                         @NonNull final View toShow) {
        animateViewSwitch(show, original, toShow, getShortAnimTime(original.getContext()));
    }

    public static void fadeView(final boolean show,
                                @NonNull final View view,
                                boolean instant) {
        fadeView(show, view, instant, getShortAnimTime(view.getContext()));
    }

    public static void fadeView(final boolean show,
                                @NonNull final View view,
                                final boolean instant,
                                final int animTime) {
        view.animate().cancel();

        if(instant) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            if(show) {
                view.setAlpha(0); // krece iz providnosti
                view.setVisibility(View.VISIBLE);
            }
            view.animate().setDuration(animTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        view.setVisibility(View.GONE);
                        view.setAlpha(1); // vrati alpha na 1 ako rucno budes VISIBLE/GONE
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    view.setVisibility(show? View.VISIBLE : View.GONE);
                    view.setAlpha(1);
                }
            });
        }
    }

    public static int getShortAnimTime(@NonNull final Context context) {
        if(shortAnimTime == 0)
            shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        return shortAnimTime;
    }

}
