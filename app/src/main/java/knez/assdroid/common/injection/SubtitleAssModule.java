package knez.assdroid.common.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.subtitle.handler.ass.AssFormatter;
import knez.assdroid.subtitle.handler.ass.AssParser;
import knez.assdroid.subtitle.handler.ass.TextSectionParser;

@Module
class SubtitleAssModule {

    @Provides @Singleton
    AssParser getAssParser(TextSectionParser textSectionParser) {
        return new AssParser(textSectionParser);
    }

    @Provides @Singleton
    TextSectionParser getSubtitleSectionParser() {
        return new TextSectionParser();
    }

    @Provides @Singleton
    AssFormatter getAssFormatter() {
        return new AssFormatter();
    }

}
