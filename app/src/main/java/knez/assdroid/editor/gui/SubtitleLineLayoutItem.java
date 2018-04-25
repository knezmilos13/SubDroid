package knez.assdroid.editor.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import knez.assdroid.R;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.subtitle.ParserHelper;

public class SubtitleLineLayoutItem extends FrameLayout {

    // TODO rename all this shit
    @BindView(R.id.stavka_prevod_prvi_red) protected View prviRed;
    @BindView(R.id.stavka_prevod_drugi_red) protected View drugiRed;
    @BindView(R.id.stavka_prevod_redni_broj_1) protected TextView redniBroj1;
    @BindView(R.id.stavka_prevod_redni_broj_2) protected TextView redniBroj2;
    @BindView(R.id.stavka_prevod_redni_broj_3) protected TextView redniBroj3;
    @BindView(R.id.stavka_prevod_vreme_od) protected TextView vremeOd;
    @BindView(R.id.stavka_prevod_vreme_crtica) protected TextView labelaCrtica;
    @BindView(R.id.stavka_prevod_vreme_do) protected TextView vremeDo;
    @BindView(R.id.stavka_prevod_tekst_actor) protected TextView labelaActor;
    @BindView(R.id.stavka_prevod_actor) protected TextView actor;
    @BindView(R.id.stavka_prevod_tekst_stil) protected TextView labelaStil;
    @BindView(R.id.stavka_prevod_stil) protected TextView style;
    @BindView(R.id.stavka_prevod_tekst) protected TextView tekst;

    @Nullable private Callback listener;
    @Nullable private SubtitleLineVso subtitleLineVso;
    @NonNull private final DateTimeFormatter subtitleTimeFormatter;

    public SubtitleLineLayoutItem(Context context, @NonNull DateTimeFormatter dateTimeFormatter) {
        super(context);
        this.subtitleTimeFormatter = dateTimeFormatter;
        init();
    }

    public SubtitleLineLayoutItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new UnsupportedOperationException();
    }

    public SubtitleLineLayoutItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new UnsupportedOperationException();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SubtitleLineLayoutItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        throw new UnsupportedOperationException();
    }

    protected void init() {
        inflate(getContext(), R.layout.item_subtitle_line, this);
        ButterKnife.bind(this);
    }


    // --------------------------------------------------------------------------------- USER EVENTS

    @OnClick(R.id.item_subtitle_line)
    protected void onItemClicked() {
        if (subtitleLineVso != null && listener != null)
            listener.onSubtitleLineClicked(subtitleLineVso, this);
    }


    // ---------------------------------------------------------------------------- PUBLIC INTERFACE

    public void showItem(@NonNull SubtitleLineVso vso) {
        this.subtitleLineVso = vso;

        handleTimingLineDisplay(subtitleLineVso);
        handleStyleAndActorLineDisplay(subtitleLineVso);
        handleSubtitleLineDisplay(subtitleLineVso);

        setFontSizes(subtitleLineVso.getSubtitleLineSettings());
        setBackgroundResource(vso.getBackgroundDrawable());
    }

    public void setListener(@Nullable Callback listener) {
        this.listener = listener;
    }


    // ----------------------------------------------------------------------------------------- GUI

    private void handleTimingLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        if(subtitleLineVso.getSubtitleLineSettings().isShowTimings()) {
            prviRed.setVisibility(View.VISIBLE);
            redniBroj1.setVisibility(View.VISIBLE);
            // TODO: implementirati bar bazican, nekonfigurabilan prikaz
//            redniBroj1.setText("#" + subtitleLineVso.getSubtitleLine().getLineNumber());
//            vremeOd.setText(subtitleLineVso.getSubtitleLine().getStart().format(subtitleTimeFormatter));
//            vremeDo.setText(subtitleLineVso.getSubtitleLine().getEnd().format(subtitleTimeFormatter));
        }  else {
            prviRed.setVisibility(View.GONE);
            redniBroj1.setVisibility(View.GONE);
        }
    }

    /** Shows or hides the second of the three lines with subtitle data (style/actor in particular). */
    private void handleStyleAndActorLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        if(subtitleLineVso.getSubtitleLineSettings().isShowStyleAndActor()) {
            drugiRed.setVisibility(View.VISIBLE);
//            actor.setText(subtitleLineVso.getSubtitleLine().getActorName());
//            style.setText(subtitleLineVso.getSubtitleLine().getStyle());
            if(!subtitleLineVso.getSubtitleLineSettings().isShowTimings()) {
//                redniBroj2.setText("#" + subtitleLineVso.getSubtitleLine().getLineNumber());
                redniBroj2.setVisibility(View.VISIBLE);
            } else {
                redniBroj2.setVisibility(View.GONE);
            }
        } else {
            drugiRed.setVisibility(View.GONE);
        }
    }

    /** Sredjuje prikaz reda sa tekstom - da li se prikazuju tagovi, da li se hajlajtuje neki trazeni izraz
     *  i da li se prikazuje redni broj u trecem redu (ili je vec prikazan u prvom/drugom) */
    private void handleSubtitleLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        String tekstZaPrikaz = subtitleLineVso.getText();
        if(!subtitleLineVso.getSubtitleLineSettings().isShowTagContents())
            tekstZaPrikaz = ParserHelper.izbaciTagove(tekstZaPrikaz,
                    subtitleLineVso.getSubtitleLineSettings().getTagReplacement());

        // TODO highligh/pretraga fazon
//        if(trazeniTekst != null && !trazeniTekst.equals(""))
//            holder.tekst.setText(hajlajtujTekst(tekstZaPrikaz));
//        else
            tekst.setText(tekstZaPrikaz);

        if(!subtitleLineVso.getSubtitleLineSettings().isShowTimings()
                && !subtitleLineVso.getSubtitleLineSettings().isShowStyleAndActor()) {
            redniBroj3.setVisibility(View.VISIBLE);
//            redniBroj3.setText("#" + subtitleLineVso.getSubtitleLine().getLineNumber());
        } else {
            redniBroj3.setVisibility(View.GONE);
        }
    }

    private void setFontSizes(@NonNull SubtitleLineSettings settings) {
        TextView tv[] = { redniBroj1, redniBroj2, redniBroj3, vremeOd, vremeDo, actor, style,
                labelaCrtica, labelaStil, labelaActor };
        for(TextView view : tv)
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getSubtitleTextSizeDp());

        tekst.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getOtherTextSizeDp());
    }


    // -------------------------------------------------------------------------- CALLBACK INTERFACE

    public interface Callback {
        void onSubtitleLineClicked(
                @NonNull SubtitleLineVso itemVso, @NonNull SubtitleLineLayoutItem layoutItem);
    }

}
