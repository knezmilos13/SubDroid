package knez.assdroid.common;

import android.content.Context;
import android.support.annotation.NonNull;

public class Navigator {

    @NonNull private final Context context;

    public Navigator(@NonNull final Context context) {
        this.context = context;
    }
//
//    /** Note: will always close all other screens */
//    public void showLoginScreen(LoginActivity.AuthErrorMessage authErrorMessage) {
//        Intent mainIntent = new Intent(context, LoginActivity.class);
//
//        int flags = Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK;
//        mainIntent.addFlags(flags);
//        mainIntent.putExtra(LoginActivity.EXTRA_FAIL_MESSAGE, authErrorMessage);
//
//        context.startActivity(mainIntent);
//    }
//
//    public void showItemsScreen(boolean closeOtherScreens) {
//        Intent intent = new Intent(context, ItemListActivity.class);
//        if(closeOtherScreens)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }
//
//    public void startItemDetailsScreen(int itemId) {
//
//    }

}
