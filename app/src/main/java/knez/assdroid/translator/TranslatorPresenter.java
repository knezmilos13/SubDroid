package knez.assdroid.translator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    private long lineId;
    private boolean hadChanges;

    private SubtitleLine currentLine;
    @Nullable private SubtitleLine previousLine;
    @Nullable private SubtitleLine nextLine;

    public TranslatorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLine.Builder subtitleLineBuilder,
            @NonNull Timber.Tree logger) {
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
        this.lineId = lineId;
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
    }

    // TODO dugmici next/prev takodje mogu da se dodaju; takodje vidi tipa enter sta radi
    @Override
    public void onNextLineRequested() {
        if(viewInterface == null) return;
        if(nextLine == null) return;

        previousLine = currentLine;
        currentLine = nextLine;
        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);

        showActiveSubtitleLines();
        viewInterface.resetInputField(currentLine.getText());
    }

    @Override
    public void onCommitRequested() {
        if(viewInterface == null) return;

        String translationText = viewInterface.getTranslationText();
        // TODO: ovde ces tipa uzeti i druge razne vrednosti, npr. tajminge, pa sve u bilder dole

        subtitleLineBuilder.takeValuesFrom(currentLine);
        subtitleLineBuilder.setText(translationText);

        SubtitleLine updatedLine = subtitleLineBuilder.build();
        if(currentLine.isIdenticalTo(updatedLine)) return; // no changes then

        subtitleController.updateLine(updatedLine);

        currentLine = updatedLine;

        showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
        showActiveSubtitleLines();

        hadChanges = true;
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
    }

    @Override
    public long getCurrentLineId() {
        return lineId;
    }

    @Override
    public boolean hasHadChangesToSubtitleMade() {
        return hadChanges;
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