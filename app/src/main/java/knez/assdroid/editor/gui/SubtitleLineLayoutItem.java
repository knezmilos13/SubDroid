package knez.assdroid.editor.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import knez.assdroid.R;
import knez.assdroid.editor.vso.SubtitleLineVso;

public class SubtitleLineLayoutItem extends FrameLayout {

    @BindView(R.id.subtitle_line_line_number) protected TextView lineNumberView;
    @BindView(R.id.subtitle_line_timings) protected TextView timingsView;
    @BindView(R.id.subtitle_line_actor) protected TextView actorView;
    @BindView(R.id.subtitle_line_style) protected TextView styleView;
    @BindView(R.id.subtitle_line_text) protected TextView subtitleTextView;

    @Nullable private Callback listener;
    @Nullable private SubtitleLineVso subtitleLineVso;

    public SubtitleLineLayoutItem(Context context) {
        super(context);
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
        View view = inflate(getContext(), R.layout.item_subtitle_line, this);
        ButterKnife.bind(this);

        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    // --------------------------------------------------------------------------------- USER EVENTS

    @OnClick(R.id.subtitle_line_item)
    protected void onItemClicked() {
        if (subtitleLineVso != null && listener != null)
            listener.onSubtitleLineClicked(subtitleLineVso, this);
    }


    // ---------------------------------------------------------------------------- PUBLIC INTERFACE

    public void showItem(@NonNull SubtitleLineVso vso) {
        this.subtitleLineVso = vso;

        lineNumberView.setText(getResources().getString(
                R.string.subtitle_line_number, subtitleLineVso.getLineNumber()));
        handleTimingLineDisplay(subtitleLineVso);
        handleStyleAndActorLineDisplay(subtitleLineVso);
        handleSubtitleLineDisplay(subtitleLineVso);

        setFontSizes(subtitleLineVso.getSharedSettings().getTextSize(),
                subtitleLineVso.getSharedSettings().getOtherSize());
        setBackgroundResource(vso.getBackgroundDrawable());
    }

    public void setListener(@Nullable Callback listener) {
        this.listener = listener;
    }


    // ----------------------------------------------------------------------------------------- GUI

    private void handleTimingLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        if(subtitleLineVso.getSharedSettings().isShowTimings()) {
            timingsView.setVisibility(View.VISIBLE);
            timingsView.setText(getResources().getString(R.string.subtitle_line_timings,
                    subtitleLineVso.getStart(), subtitleLineVso.getEnd()));
        }  else {
            timingsView.setVisibility(View.GONE);
        }
    }

    /** Shows or hides the second of the three lines with subtitle data (style/actor in particular). */
    private void handleStyleAndActorLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        if(subtitleLineVso.getSharedSettings().isShowActorAndStyle()) {
            styleView.setVisibility(View.VISIBLE);
            actorView.setVisibility(View.VISIBLE);
            styleView.setText(getResources().getString(
                    R.string.subtitle_line_style, subtitleLineVso.getStyle()));
            actorView.setText(getResources().getString(
                    R.string.subtitle_line_actor, subtitleLineVso.getActorName()));
        } else {
            styleView.setVisibility(View.GONE);
            actorView.setVisibility(View.GONE);
        }
    }

    private void handleSubtitleLineDisplay(@NonNull SubtitleLineVso subtitleLineVso) {
        subtitleTextView.setText(subtitleLineVso.getText());
    }

    private void setFontSizes(int textSizeDp, int otherSizeDp) {
        TextView tv[] = {lineNumberView, timingsView, actorView, styleView};
        for(TextView view : tv) view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, otherSizeDp);
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp);
    }


    // -------------------------------------------------------------------------- CALLBACK INTERFACE

    public interface Callback {
        void onSubtitleLineClicked(
                @NonNull SubtitleLineVso itemVso, @NonNull SubtitleLineLayoutItem layoutItem);
    }

}
