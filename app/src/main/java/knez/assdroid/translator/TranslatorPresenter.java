package knez.assdroid.translator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.util.FileHandler;
import timber.log.Timber;

public class TranslatorPresenter extends CommonSubtitlePresenter
        implements TranslatorMVP.PresenterInterface {

    @NonNull private final SubtitleLine.Builder subtitleLineBuilder;
    @NonNull private final Timber.Tree logger;

    private TranslatorMVP.ViewInterface viewInterface;
    private boolean currentLineHadUncommittedChanges;
    @NonNull private final HashSet<Integer> editedLineNumbers = new HashSet<>();

    private SubtitleLine currentLine;
    @Nullable private SubtitleLine previousLine;
    @Nullable private SubtitleLine nextLine;

    public TranslatorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLine.Builder subtitleLineBuilder,
            @NonNull Timber.Tree logger,
            @NonNull FileHandler fileHandler) {
        super(subtitleController, fileHandler);
        this.subtitleLineBuilder = subtitleLineBuilder;
        this.logger = logger;
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull TranslatorMVP.ViewInterface viewInterface) {
        onAttach(viewInterface, 1);
    }

    @Override
    public void onAttach(@NonNull TranslatorMVP.ViewInterface viewInterface,
                         @NonNull TranslatorMVP.InternalState internalState) {
        this.viewInterface = viewInterface;
        this.currentLineHadUncommittedChanges = internalState.isCurrentLineHadUncommittedChanges();

        this.editedLineNumbers.clear();
        this.editedLineNumbers.addAll(internalState.getEditedLineNumbers());

        SubtitleFile subtitleFile = subtitleController.getCurrentSubtitleFile();
        if(subtitleFile == null) {
            logger.e("No subtitle file loaded");
            viewInterface.closeScreenNoSubtitle();
            return;
        }

        showSubtitleTitle(subtitleFile);

        SubtitleLine tempCurrentLine = subtitleController.getLineForId(internalState.getCurrentLineId());
        if(tempCurrentLine == null) {
            logger.e("Translator activity started, but no lines available!");
            viewInterface.closeScreenNoSubtitle();
            return;
        }

        subtitleController.attachListener(this);

        currentLine = tempCurrentLine;
        previousLine = subtitleController.getLineForNumber(currentLine.getLineNumber() - 1);
        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);

        showActiveSubtitleLines();
        viewInterface.resetInputField(currentLine.getText());
        viewInterface.showCurrentLineEdited(currentLineHadUncommittedChanges);
    }

    @Override
    public void onAttach(@NonNull TranslatorMVP.ViewInterface viewInterface, long lineId) {
        onAttach(viewInterface, new TranslatorMVP.InternalState(false, lineId, new HashSet<>()));
    }

    @Override
    public void onDetach() {
        viewInterface = null;
        subtitleController.detachListener(this);
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

        currentLineHadUncommittedChanges = false;
        viewInterface.showCurrentLineEdited(false);
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

        currentLineHadUncommittedChanges = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public void onCommitRequested() {
        if(viewInterface == null || subtitleController.isWritingFile()) return;

        String translationText = viewInterface.getTranslationText().trim();
        if(translationText.equals("")) translationText = currentLine.getText();

        subtitleLineBuilder.takeValuesFrom(currentLine);
        subtitleLineBuilder.setText(translationText);

        SubtitleLine updatedLine = subtitleLineBuilder.build();
        if(currentLine.isIdenticalTo(updatedLine)) return; // no changes then

        editedLineNumbers.add(currentLine.getLineNumber());
        subtitleController.updateLine(updatedLine);

        currentLine = updatedLine;

        showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
        showActiveSubtitleLines();

        currentLineHadUncommittedChanges = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public void onCommitAndNextRequested() {
        if(subtitleController.isWritingFile()) return;
        onCommitRequested();
        onNextLineRequested();
    }

    @Override
    public void onCopyCurrentLineToInputRequested() {
        if(viewInterface == null) return;
        viewInterface.setInputText(currentLine.getText());

        currentLineHadUncommittedChanges = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public int getCurrentLineNumber() {
        return currentLine.getLineNumber();
    }

    @Override
    public boolean hasHadChangesToSubtitleMade() {
        return currentLineHadUncommittedChanges;
    }

    @Override
    public Set<Integer> getEditedLineNumbers() {
        return editedLineNumbers;
    }

    @Override
    public void onTextChanged(@NonNull String text) {
        String inputtedText = text.trim();
        boolean linesSame = currentLine.getText().equals(inputtedText);

        if(inputtedText.equals("")) currentLineHadUncommittedChanges = false;
        else if(currentLineHadUncommittedChanges && linesSame) currentLineHadUncommittedChanges = false;
        else if(!currentLineHadUncommittedChanges && !linesSame) currentLineHadUncommittedChanges = true;
        else return; // no changes

        if(viewInterface != null) viewInterface.showCurrentLineEdited(currentLineHadUncommittedChanges);
    }

    @Override @NonNull
    public TranslatorMVP.InternalState getInternalState() {
        return new TranslatorMVP.InternalState(
                currentLineHadUncommittedChanges, currentLine.getId(), editedLineNumbers);
    }

    @Override @Nullable
    public CommonSubtitleMVP.ViewInterface getViewInterface() {
        return viewInterface;
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showActiveSubtitleLines() {
        if(viewInterface == null) return;

        viewInterface.showSubtitleTexts(
                currentLine.getText(),
                previousLine == null? null : previousLine.getText(),
                nextLine == null? null : nextLine.getText());
    }

}