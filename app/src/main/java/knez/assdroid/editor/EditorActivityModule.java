package knez.assdroid.editor;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import knez.assdroid.util.preferences.IntPreference;
import knez.assdroid.util.preferences.PersistedValue;
import knez.assdroid.util.preferences.PersistedValueReader;
import knez.assdroid.util.preferences.StringPreference;
import timber.log.Timber;

@Module
public class EditorActivityModule {

    @Provides
    EditorMvp.PresenterInterface getEditorPresenter(
            SubtitleController subtitleController,
            SubtitleLineVsoFactory subtitleLineVsoFactory,
            FileHandler fileHandler,
            @Named("singleThreadExecutor") ExecutorService singleThreadExecutor,
            @Named("mainThreader") Threader mainThreader,
            Timber.Tree logger,
            @Named("tagReplacement") PersistedValue<String> tagReplacementPreference,
            @Named("subLineTextSize")PersistedValue<Integer> subLineTextSizePreference,
            @Named("subLineOtherSize") PersistedValue<Integer> subLineOtherSizePreference,
            @Named("subLineShowTimings") PersistedValue<Boolean> subLineShowTimingsPreference,
            @Named("subLineShowStyleActor") PersistedValue<Boolean> subLineShowStyleActorPreference,
            @Named("simplifyTags") PersistedValue<Boolean> simplifyTagsPreference) {
        return new EditorPresenter(
                subtitleController,
                subtitleLineVsoFactory,
                fileHandler,
                singleThreadExecutor,
                mainThreader,
                logger,
                tagReplacementPreference,
                subLineTextSizePreference,
                subLineOtherSizePreference,
                subLineShowTimingsPreference,
                subLineShowStyleActorPreference,
                simplifyTagsPreference);
    }

}
