package knez.assdroid.common.injection;

import org.threeten.bp.format.DateTimeFormatter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;

@Module
class ViewFactoryModule {

    @Provides @Singleton
    SubtitleLineVsoFactory getSubtitleLineVsoFactory(
            @Named("subtitleTimeFormatter") DateTimeFormatter subtitleTimeFormatter) {
        return new SubtitleLineVsoFactory(subtitleTimeFormatter);
    }

    @Provides @Named("subtitleTimeFormatter")
    DateTimeFormatter getSubtitleTimeFormatter() {
        return DateTimeFormatter.ofPattern("H:mm:ss.SS");
    }

}
