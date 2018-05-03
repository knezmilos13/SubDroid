package knez.assdroid.translator;

import android.support.annotation.NonNull;

import knez.assdroid.subtitle.SubtitleController;

public class TranslatorPresenter implements TranslatorMVP.PresenterInterface {

    @NonNull private final SubtitleController subtitleController;

    private TranslatorMVP.ViewInterface viewInterface;
    private int lineId;
    private boolean hadChanges;

    public TranslatorPresenter(
            @NonNull SubtitleController subtitleController) {
        this.subtitleController = subtitleController;
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull TranslatorMVP.ViewInterface viewInterface,
                         int lineId, boolean hadChanges) {
        this.viewInterface = viewInterface;
        this.lineId = lineId;
        this.hadChanges = hadChanges;

//        subtitleController.attachListener(this);

//        namestiRed(brojReda);
//        osveziNaslov();
    }

    @Override
    public void onDetach() {
        viewInterface = null;
//        subtitleController.detachListener(this);
    }


    // --------------------------------------------------------------------------- USER & APP EVENTS

    @Override
    public void onPrevLineRequested() {
        premotajNaPrethodniRed();
    }

    @Override
    public void onNextLineRequested() {
        premotajNaSledeciRed();
    }

    @Override
    public void onCommitRequested() {
//        commitujIzmene();
//        prikaziRedove();
    }

    @Override
    public void onCommitAndNextRequested() {
        commitujIzmene();
        premotajNaSledeciRed();
    }

    @Override
    public void onCopyCurrentLineToInputRequested() {
//        inputView.setText(tekuciRed.getText());
    }

    @Override
    public int getCurrentLineId() {
        return lineId;
    }

    @Override
    public boolean hasHadChangesToSubtitleMade() {
        return hadChanges;
    }


    // TODO sve ispod
    // ----------------------------------------------------------------------- Manipulacija prevodom

    /** Ucitava, prikazuje zadati red prevoda i u skladu sa time modifikuje ostatak interfejsa. */
    private void namestiRed(int lineNumber) {
//        ucitajRedove(lineNumber);
//        prikaziRedove();
//        inputView.setText("");
//        primeniUnosPodesavanja();
    }

    private void ucitajRedove(int tekuci) {
//		prethodniRed = tekuci>1 ? subtitleController.dajRedPrevoda(tekuci - 1) : null;
//		tekuciRed = subtitleController.dajRedPrevoda(tekuci);
//		sledeciRed = subtitleController.postojiLiRedPrevoda(tekuci + 1)? subtitleController.dajRedPrevoda(tekuci + 1) : null;
    }

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
//            namestiRed(sledeciRed.getLineNumber());
//        else
//            osveziTekuciRed();
    }

    private void premotajNaPrethodniRed() {
//        if(prethodniRed != null)
//            namestiRed(prethodniRed.getLineNumber());
//        else
//            osveziTekuciRed();
    }

    private void osveziTekuciRed() {
//        currentLineLabel.setText(tekuciRed.getText());
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