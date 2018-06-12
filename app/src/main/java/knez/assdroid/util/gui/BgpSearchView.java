package knez.assdroid.util.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import knez.assdroid.R;

@SuppressWarnings("unused")
public class BgpSearchView extends FrameLayout {

    @BindView(R.id.bgp_search_view_text) protected EditText editText;
    @BindView(R.id.bgp_search_view_num_results) protected TextView numResultsView;

    @BindView(R.id.bgp_search_view_prev_icon) protected ImageView prevIcon;
    @BindView(R.id.bgp_search_view_next_icon) protected ImageView nextIcon;
    @BindView(R.id.bgp_search_view_close_icon) protected ImageView closeIcon;

    private Listener listener;
    private String hint;
    private boolean noFullScreen = false;

    public BgpSearchView(Context context) {
        super(context);
        init(null);
    }

    public BgpSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BgpSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BgpSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.bgp_search_view, this);

        ButterKnife.bind(this);

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BgpSearchViewAttributes);
            hint = a.getString(R.styleable.BgpSearchViewAttributes_hint);
            noFullScreen = a.getBoolean(R.styleable.BgpSearchViewAttributes_noFullScreen, false);
            a.recycle();
        }

        setUpViews();
    }

    private void setUpViews() {

        prevIcon.setImageDrawable(new IconDrawable(getContext(), MaterialIcons.md_keyboard_arrow_up)
                .colorRes(R.color.gray_4_medium_light).sizeDp(25));
        nextIcon.setImageDrawable(new IconDrawable(getContext(), MaterialIcons.md_keyboard_arrow_down)
                .colorRes(R.color.gray_4_medium_light).sizeDp(25));
        closeIcon.setImageDrawable(new IconDrawable(getContext(), MaterialIcons.md_clear)
                .colorRes(R.color.gray_4_medium_light).sizeDp(25));

        if(hint != null) editText.setHint(hint);

        editText.setSingleLine();
        editText.setMaxLines(1);

        if(noFullScreen)
            editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        editText.setOnEditorActionListener((v, actionId, event) -> {

            if ((actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                    && (event == null || event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (listener != null)
                    listener.onSearchSubmitted(editText.getText().toString());
                return true;
            }

            return false;
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (listener != null) listener.onLetterInputted(s.toString());
            }
        });
    }


    // ---------------------------------------------------------------------------- PUBLIC INTERFACE

    public void setHint(String hint) {
        this.hint = hint;
        editText.setHint(this.hint);
        invalidate();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Sets text and also moves cursor to the end (which normal EditText.setText() doesn't do */
    public void setText(String text) {
        editText.setText("");
        editText.append(text); // this way the cursor will be at the end after setting text
    }

    public void setNumResults(int current, int total) {
        numResultsView.setText(current + "/" + total);
    }

    public void hideNumResults() {
        numResultsView.setText("");
    }

    public String getText() {
        return editText.getText().toString();
    }

    @OnClick(R.id.bgp_search_view_close_icon)
    protected void onXClicked() {
        editText.setText("");
        if(listener != null) listener.onXClicked();
    }

    @OnClick(R.id.bgp_search_view_prev_icon)
    protected void onPrevIconClicked() {
        if (listener != null) listener.onPrevResultRequested();
    }

    @OnClick(R.id.bgp_search_view_next_icon)
    protected void onNextIconClicked() {
        if (listener != null) listener.onNextResultRequested();
    }

    public void focusToInputField() {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public interface Listener {
        void onXClicked();
        void onSearchSubmitted(@NonNull String text);
        void onLetterInputted(@NonNull String text);
        void onPrevResultRequested();
        void onNextResultRequested();
    }

}
