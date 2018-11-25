package knez.assdroid.translator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import knez.assdroid.common.mvp.CommonSubtitleMvp;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.preferences.PersistedValueReader;
import timber.log.Timber;

public class TranslatorPresenter extends CommonSubtitlePresenter
        implements TranslatorMvp.PresenterInterface {

    @NonNull private final SubtitleLine.Builder subtitleLineBuilder;
    @NonNull private final Timber.Tree logger;
    @Nullable private final TagPrettifier tagPrettifier;

    private TranslatorMvp.ViewInterface viewInterface;
    private boolean currentLineHadUncommittedChanges;
    @NonNull private final HashSet<Long> editedLineIds = new HashSet<>();

    private SubtitleLine currentLine;
    @Nullable private SubtitleLine previousLine;
    @Nullable private SubtitleLine nextLine;

    TranslatorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLine.Builder subtitleLineBuilder,
            @NonNull Timber.Tree logger,
            @NonNull FileHandler fileHandler,
            @NonNull PersistedValueReader<String> tagReplacementPreference) {
        super(subtitleController, fileHandler);
        this.subtitleLineBuilder = subtitleLineBuilder;
        this.logger = logger;
        this.tagPrettifier = subtitleController.
                getTagPrettifierForCurrentSubtitle(tagReplacementPreference.get());
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull TranslatorMvp.ViewInterface viewInterface) {
        onAttach(viewInterface, 1);
    }

    @Override
    public void onAttach(@NonNull TranslatorMvp.ViewInterface viewInterface,
                         @NonNull TranslatorMvp.InternalState internalState) {
        this.viewInterface = viewInterface;
        this.currentLineHadUncommittedChanges = internalState.isCurrentLineHadUncommittedChanges();

        this.editedLineIds.clear();
        this.editedLineIds.addAll(internalState.getEditedLineIds());

//        SubtitleFile subtitleFile = subtitleController.getCurrentSubtitleFile();
//        if(subtitleFile == null) {
//            logger.e("No subtitle file loaded");
//            viewInterface.closeScreenNoSubtitle();
//            return;
//        }
//
//        showSubtitleTitle(subtitleFile);
//
//        SubtitleLine tempCurrentLine = subtitleController.getLineForId(internalState.getCurrentLineId());
//        if(tempCurrentLine == null) {
//            logger.e("Translator activity started, but no lines available!");
//            viewInterface.closeScreenNoSubtitle();
//            return;
//        }
//
//        currentLine = tempCurrentLine;
//        previousLine = subtitleController.getLineForNumber(currentLine.getLineNumber() - 1);
//        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);
//
//        showActiveSubtitleLines();
//        viewInterface.resetInputField(currentLine.getText());
//        viewInterface.showCurrentLineEdited(currentLineHadUncommittedChanges);
    }

    @Override
    public void onAttach(@NonNull TranslatorMvp.ViewInterface viewInterface, long lineId) {
        onAttach(viewInterface, new TranslatorMvp.InternalState(false, lineId, new HashSet<>()));
    }

    @Override
    public void onDetach() {
        viewInterface = null;
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
        if(viewInterface == null /*|| subtitleController.isWritingFile()*/) return;

        String translationText = viewInterface.getTranslationText().trim();
        if(translationText.equals("")) translationText = currentLine.getText();

        subtitleLineBuilder.takeValuesFrom(currentLine);
        subtitleLineBuilder.setText(translationText);

        SubtitleLine updatedLine = subtitleLineBuilder.build();
        if(currentLine.isIdenticalTo(updatedLine)) return; // no changes then

        editedLineIds.add(currentLine.getId());
        subtitleController.updateLine(updatedLine);

        currentLine = updatedLine;

//        showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
        showActiveSubtitleLines();

        currentLineHadUncommittedChanges = false;
        viewInterface.showCurrentLineEdited(false);
    }

    @Override
    public void onCommitAndNextRequested() {
//        if(subtitleController.isWritingFile()) return;
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
    public Set<Long> getEditedLineIds() {
        return editedLineIds;
    }

    @Override
    public void onTextChanged(@NonNull String text) {
        if(text.contains("\n")) {
            text = text.replace("\n", "\\N");
            if(viewInterface != null) viewInterface.setInputText(text);
        }

        boolean linesSame = currentLine.getText().equals(text);

        if(text.equals("")) currentLineHadUncommittedChanges = false;
        else if(currentLineHadUncommittedChanges && linesSame) currentLineHadUncommittedChanges = false;
        else if(!currentLineHadUncommittedChanges && !linesSame) currentLineHadUncommittedChanges = true;
        else return; // no changes

        if(viewInterface != null) viewInterface.showCurrentLineEdited(currentLineHadUncommittedChanges);
    }

    @Override @NonNull
    public TranslatorMvp.InternalState getInternalState() {
        return new TranslatorMvp.InternalState(
                currentLineHadUncommittedChanges, currentLine.getId(), editedLineIds);
    }

    @Override @Nullable
    public CommonSubtitleMvp.ViewInterface getViewInterface() {
        return viewInterface;
    }

    @Override
    public void onSettingsChanged(@NonNull HashSet<String> changedSettings) {
        super.onSettingsChanged(changedSettings);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showActiveSubtitleLines() {
        if(viewInterface == null) return;

        String currentLineText = currentLine.getText();

        String prevLineText = null;
        if(previousLine != null)
            prevLineText = tagPrettifier == null?
                    previousLine.getText() : tagPrettifier.prettifyTags(previousLine.getText());

        String nextLineText = null;
        if(nextLine != null)
            nextLineText = tagPrettifier == null?
                    nextLine.getText() : tagPrettifier.prettifyTags(nextLine.getText());

        viewInterface.showSubtitleTexts(currentLineText, prevLineText, nextLineText);
    }

}