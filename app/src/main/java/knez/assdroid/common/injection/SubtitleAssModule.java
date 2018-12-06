package knez.assdroid.common.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import knez.assdroid.subtitle.handler.SubtitleFormatter;
import knez.assdroid.subtitle.handler.SubtitleParser;
import knez.assdroid.subtitle.handler.ass.AssFormatter;
import knez.assdroid.subtitle.handler.ass.AssParser;
import knez.assdroid.subtitle.handler.ass.TextSectionParser;

@Module
class SubtitleAssModule {

    @Provides @Singleton @IntoSet
    SubtitleParser getAssParser(TextSectionParser textSectionParser) {
        return new AssParser(textSectionParser);
    }

    @Provides @Singleton
    TextSectionParser getSubtitleSectionParser() {
        return new TextSectionParser();
    }

    @Provides @Singleton @IntoSet
    SubtitleFormatter getAssFormatter() {
        return new AssFormatter();
    }

}
