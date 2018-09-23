package knez.assdroid.common.injection;

import android.content.Context;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import knez.assdroid.App;
import knez.assdroid.common.util.AppConfig;
import knez.assdroid.editor.EditorMVP;
import knez.assdroid.translator.TranslatorMVP;
import knez.assdroid.util.preferences.StringPreference;
import timber.log.Timber;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class, ActivityBuilderModule.class, AppContextModule.class,
        SubtitleModule.class, ViewFactoryModule.class, DbModule.class, PreferencesModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance Builder appInstance(App app);
        @BindsInstance Builder appConfigInstance(AppConfig appConfig);
        @BindsInstance Builder refWatcherInstance(RefWatcher refWatcher);
        AppComponent build();
    }

    void inject(App app);

    Timber.Tree getLogger();
    RefWatcher getRefWatcher();
    EditorMVP.PresenterInterface getEditorPresenter();
    TranslatorMVP.PresenterInterface getTranslatorPresenter();
    Context getContext(); // TODO: ko koristi ovo da predje na druge klase
    @Named("theme") StringPreference getThemePreference();

}
