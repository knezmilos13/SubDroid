package knez.assdroid.common.injection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import knez.assdroid.editor.EditorActivity;
import knez.assdroid.translator.TranslatorActivity;

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    abstract EditorActivity bindEditorActivity();


    @ContributesAndroidInjector
    abstract TranslatorActivity bindTranslatorActivity();

}
