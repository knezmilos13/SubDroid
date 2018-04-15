package knez.assdroid.common.injection;

import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import knez.assdroid.App;
import knez.assdroid.common.Navigator;
import knez.assdroid.common.util.AppConfig;
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

}
