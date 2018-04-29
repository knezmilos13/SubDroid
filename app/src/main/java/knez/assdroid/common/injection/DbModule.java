package knez.assdroid.common.injection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.objectbox.BoxStore;
import knez.assdroid.common.db.SubtitleContentDao;
import knez.assdroid.subtitle.data.MyObjectBox;

@Module(includes = SubtitleAssModule.class)
public class DbModule {

    @Provides @Singleton
    BoxStore getBoxStore(Context context) {
        return MyObjectBox.builder().androidContext(context).build();
    }

    @Provides @Singleton
    SubtitleContentDao getSubtitleContentDao(BoxStore boxStore) {
        return new SubtitleContentDao(boxStore);
    }

}
