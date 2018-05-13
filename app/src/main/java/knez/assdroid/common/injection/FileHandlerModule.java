package knez.assdroid.common.injection;

import android.content.ContentResolver;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.util.FileHandler;

@Module
class FileHandlerModule {

    @Provides @Singleton
    ContentResolver getContentResolver(Context context) {
        return context.getContentResolver();
    }

    @Provides @Singleton
    FileHandler getFileHandler(ContentResolver contentResolver) {
        return new FileHandler(contentResolver);
    }

}
