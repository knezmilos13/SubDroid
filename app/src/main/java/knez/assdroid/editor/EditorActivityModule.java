package knez.assdroid.editor;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import knez.assdroid.util.preferences.BooleanPreference;
import knez.assdroid.util.preferences.IntPreference;
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
            @Named("tagReplacement") StringPreference tagReplacementPreference,
            @Named("subLineTextSize")IntPreference subLineTextSizePreference,
            @Named("subLineOtherSize") IntPreference subLineOtherSizePreference,
            @Named("subLineShowTimings") BooleanPreference subLineShowTimingsPreference,
            @Named("subLineShowStyleActor") BooleanPreference subLineShowStyleActorPreference,
            @Named("simplifyTags") BooleanPreference simplifyTagsPreference) {
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
