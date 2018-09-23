package knez.assdroid.common.injection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import knez.assdroid.editor.EditorActivity;
import knez.assdroid.editor.EditorActivityModule;
import knez.assdroid.settings.SettingsActivity;
import knez.assdroid.translator.TranslatorActivity;
import knez.assdroid.translator.TranslatorActivityModule;

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = {EditorActivityModule.class})
    abstract EditorActivity bindEditorActivity();

    @ContributesAndroidInjector(modules = {TranslatorActivityModule.class})
    abstract TranslatorActivity bindTranslatorActivity();

    @ContributesAndroidInjector
    abstract SettingsActivity bindSettingsActivity();

}
