package knez.assdroid.common.injection;

import android.content.Context;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import knez.assdroid.App;
import knez.assdroid.common.util.AppConfig;

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

    Context getContext(); // TODO: ko koristi ovo da predje na druge klase

}
