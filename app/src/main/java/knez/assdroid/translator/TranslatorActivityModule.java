package knez.assdroid.translator;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.preferences.PersistedValue;
import knez.assdroid.util.preferences.StringPreference;
import timber.log.Timber;

@Module
public class TranslatorActivityModule {

    @Provides
    TranslatorMvp.PresenterInterface provideTranslatorPresenter(
            SubtitleController subtitleController,
            SubtitleLine.Builder subLineBuilder,
            Timber.Tree logger,
            FileHandler fileHandler,
            @Named("tagReplacement") PersistedValue<String> tagReplacementPreference) {
        return new TranslatorPresenter(
                subtitleController,
                subLineBuilder,
                logger,
                fileHandler,
                tagReplacementPreference);
    }

}
