package knez.assdroid.translator;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.preferences.StringPreference;
import timber.log.Timber;

@Module
public class TranslatorActivityModule {

    @Provides
    TranslatorMVP.PresenterInterface provideTranslatorPresenter(
            SubtitleController subtitleController,
            SubtitleLine.Builder subLineBuilder,
            Timber.Tree logger,
            FileHandler fileHandler,
            @Named("tagReplacement") StringPreference tagReplacementPreference) {
        return new TranslatorPresenter(
                subtitleController,
                subLineBuilder,
                logger,
                fileHandler,
                tagReplacementPreference);
    }

}
