package knez.assdroid.translator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import timber.log.Timber;

public class TranslatorPresenter extends CommonSubtitlePresenter
        implements TranslatorMVP.PresenterInterface {

    @NonNull private final SubtitleController subtitleController;
    @NonNull private final SubtitleLine.Builder subtitleLineBuilder;
    @NonNull private final Timber.Tree logger;

    private TranslatorMVP.ViewInterface viewInterface;
    private boolean hadChanges;
    private boolean currentLineEdited; // TODO trebace za instance state - ili da sacuvas prezenter onda ?

    private SubtitleLine currentLine;
    @Nullable private SubtitleLine previousLine;
    @Nullable private SubtitleLine nextLine;
    @NonNull private final Set<Integer> editedLineNumbers = new HashSet<>(); // TODO save instance state

    public TranslatorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLine.Builder subtitleLineBuilder,
            @NonNull Timber.Tree logger) {
        super(subtitleController);
        this.subtitleController = subtitleController;
        this.subtitleLineBuilder = subtitleLineBuilder;
        this.logger = logger;
    }

    // TODO: postojalo je podesavanje da se linija teksta automatski kopira; ovo je zgodno ako samo
    // editujes neki svoj postojeci prevod (mada ima copy dugme)
//    if(PodesavanjaPrevodilacUtil.isAlwaysCopyOn() && inputView.getText().toString().equals("")) {
//            inputView.setText(tekuciRed.getText());
//        }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull TranslatorMVP.ViewInterface viewInterface,
                         long lineId, boolean hadChanges) {
        super.onAttach(viewInterface);
        this.viewInterface = viewInterface;
        this.hadChanges = hadChanges;

//        subtitleController.attachListener(this); // TODO odvojen listener - ili implementiraj sve u nadklasi? kao prazne implementacije?

        SubtitleFile subtitleFile = subtitleController.getCurrentSubtitleFile();
        if(subtitleFile == null) {
            logger.e("No subtitle file loaded");
            // TODO: javi "tebra de ti je titl"
            viewInterface.closeScreen();
            return;
        }

        showSubtitleTitle(subtitleFile);

        SubtitleLine tempCurrentLine = subtitleController.getLineForId(lineId);
        if(tempCurrentLine == null) {
            logger.e("Translator activity started, but no lines available!");
            viewInterface.closeScreen();
            return;
        }

        currentLine = tempCurrentLine;
        previousLine = subtitleController.getLineForNumber(currentLine.getLineNumber() - 1);
        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);

        showActiveSubtitleLines();
        viewInterface.resetInputField(currentLine.getText());
        currentLineEdited = true;
        viewInterface.showCurrentLineEdited(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewInterface = null;
//        subtitleController.detachListener(this);
    }


    // --------------------------------------------------------------------------- USER & APP EVENTS

    @Override
    public void onPrevLineRequested() {
        if(viewInterface == null) return;
        if(previousLine == null) return;

        nextLine = currentLine;
        currentLine = previousLine;
        previousLine = subtitleController.getLineForNumber(currentLine.getLineNumber() - 1);

        showActiveSubtitleLines();
        viewInterface.resetInputField(currentLine.getText());

        currentLineEdited = true;
        viewInterface.showCurrentLineEdited(true);
    }

    @Override
    public void onNextLineRequested() {
        if(viewInterface == null) return;
        if(nextLine == null) return;

        previousLine = currentLine;
        currentLine = nextLine;
        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);

        showActiveSubtitleLines();
        viewInterface.resetInputField(currentLine.getText());

        currentLineEdited = true;
        viewInterface.showCurrentLineEdited(true);
    }

    @Override
    public void onCommitRequested() {
        if(viewInterface == null) return;

        String translationText = viewInterface.getTranslationText();

        subtitleLineBuilder.takeValuesFrom(currentLine);
        subtitleLineBuilder.setText(translationText);

        SubtitleLine updatedLine = subtitleLineBuilder.build();
        if(currentLine.isIdenticalTo(updatedLine)) return; // no changes then

        editedLineNumbers.add(currentLine.getLineNumber());
        subtitleController.updateLine(updatedLine);

        currentLine = updatedLine;

        showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
        showActiveSubtitleLines();

        hadChanges = true;

        currentLineEdited = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public void onCommitAndNextRequested() {
        onCommitRequested();
        onNextLineRequested();
    }

    @Override
    public void onCopyCurrentLineToInputRequested() {
        if(viewInterface == null) return;
        viewInterface.setInputText(currentLine.getText());

        currentLineEdited = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public int getCurrentLineNumber() {
        return currentLine.getLineNumber();
    }

    @Override
    public boolean hasHadChangesToSubtitleMade() {
        return hadChanges;
    }

    @Override
    public Set<Integer> getEditedLineNumbers() {
        return editedLineNumbers;
    }

    @Override
    public void onTextChanged(@NonNull String text) {
        boolean linesSame = currentLine.getText().equals(text);
        if(currentLineEdited && linesSame) currentLineEdited = false;
        else if(!currentLineEdited && !linesSame) currentLineEdited = true;
        else return; // no changes

        if(viewInterface != null) viewInterface.showCurrentLineEdited(currentLineEdited);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showActiveSubtitleLines() {
        if(viewInterface == null) return;

        viewInterface.showSubtitleTexts(
                currentLine.getText(),
                previousLine == null? null : previousLine.getText(),
                nextLine == null? null : nextLine.getText());

        // TODO: ovde svasta nesto kada bude bilo, tipa tajminzi?
    }

}