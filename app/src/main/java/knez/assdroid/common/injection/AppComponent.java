package knez.assdroid.common.injection;

import android.content.Context;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import knez.assdroid.App;
import knez.assdroid.common.Navigator;
import knez.assdroid.common.util.AppConfig;
import knez.assdroid.editor.EditorPresenter;
import knez.assdroid.logika.SubtitleHandler;
import timber.log.Timber;

@Singleton
@Component(modules = {
        AppContextModule.class, ViewFactoryModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance Builder appInstance(App app);
        @BindsInstance Builder appConfigInstance(AppConfig appConfig);
        @BindsInstance Builder refWatcherInstance(RefWatcher refWatcher);
        AppComponent build();
    }

    Timber.Tree getLogger();
    RefWatcher getRefWatcher();
    Navigator getNavigator();
    EditorPresenter getEditorPresenter();
    SubtitleHandler getSubtitleHandler();
    Context getContext(); // TODO: ko koristi ovo da predje na druge klase

}