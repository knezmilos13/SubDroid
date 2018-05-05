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
    @NonNull private final Timber.Tree logger;

    private TranslatorMVP.ViewInterface viewInterface;
    private long lineId;
    private boolean hadChanges;

    private SubtitleLine currentLine;
    @Nullable private SubtitleLine previousLine;
    @Nullable private SubtitleLine nextLine;

    public TranslatorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull Timber.Tree logger) {
        this.subtitleController = subtitleController;
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
        showActiveSubtitleLine(lineId);
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
        premotajNaPrethodniRed(); // TODO
    }

    @Override
    public void onNextLineRequested() {
        premotajNaSledeciRed(); // TODO
    }

    @Override
    public void onCommitRequested() {
//        commitujIzmene(); // TODO
//        prikaziRedove();
    }

    @Override
    public void onCommitAndNextRequested() {
        commitujIzmene(); // TODO
        premotajNaSledeciRed();
    }

    @Override
    public void onCopyCurrentLineToInputRequested() {
//        inputView.setText(tekuciRed.getText()); // TODO
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

    private void showActiveSubtitleLine(long lineId) {
        SubtitleLine tempCurrentLine = subtitleController.getLineForId(lineId);
        if(tempCurrentLine == null) {
            logger.e("Translator activity started, but no lines available!");
            viewInterface.closeScreen();
            return;
        }
        currentLine = tempCurrentLine;

        previousLine = lineId > 1? subtitleController.getLineForNumber(currentLine.getLineNumber() - 1) : null;
        nextLine = subtitleController.getLineForNumber(currentLine.getLineNumber() + 1);

        if(viewInterface == null) return;

        viewInterface.showSubtitleTexts(
                currentLine.getText(),
                previousLine == null? null : previousLine.getText(),
                nextLine == null? null : nextLine.getText());

        viewInterface.resetInputField(currentLine.getText());
    }





    // TODO sve ispod

    /** Primenjuje izmene na tekucu liniju prevoda i osvezava naslov aktivnosti */
    private void commitujIzmene() {
//		if(PodesavanjaPrevodilacUtil.isCommitKeepOriginalOn() && inputView.getText().toString().equals("")) {
        // ako commit prazne linije ne menja nista, a jeste bila prazna linija... do nothing
//		} else {
//			tekuciRed.text = inputView.getText().toString(); // TODO: nece da moze setText - immutable tebra
//			subtitleController.updateRedPrevoda(tekuciRed);
//			radjeneIzmeneOvde = true;
//			if(!subtitleController.isPrevodMenjan()) {
//				subtitleController.setPrevodMenjan(true);
//				osveziNaslov();
//			}
//		}
    }

    private void premotajNaSledeciRed() {
//        if(sledeciRed != null)
//            showActiveSubtitleLine(sledeciRed.getLineNumber());
//        else
//            osveziTekuciRed();
    }

    private void premotajNaPrethodniRed() {
//        if(prethodniRed != null)
//            showActiveSubtitleLine(prethodniRed.getLineNumber());
//        else
//            osveziTekuciRed();
    }

    private void osveziTekuciRed() {
//        currentLineTextView.setText(tekuciRed.getText());
    }

    private void snimiPrevod() {
//		try {
//			subtitleController.snimiPrevod();
//		} catch (FileNotFoundException e) {
//			Loger.log(e);
//			e.printStackTrace();
//			//TODO ne postoji fajl... a ovo je save... da je saveas pa ajde
//			// u ovoj varijanti u prevodiocu moze da ga snimi negde na SD kao temp fajl i da ispise obavestenje
//		}
//		osveziNaslov();
    }

}