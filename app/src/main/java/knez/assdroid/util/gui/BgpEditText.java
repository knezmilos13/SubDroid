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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import knez.assdroid.R;

@SuppressWarnings("unused")
public class BgpEditText extends FrameLayout {

    @BindView(R.id.bgp_edit_text_view) protected EditText editText;
    @BindView(R.id.bgp_edit_text_left_icon) protected ImageView leftIcon;

    @BindView(R.id.bgp_edit_text_right_icon) protected ImageView rightIcon;
    @BindView(R.id.bgp_cope_edit_text_right_icon_container) protected View rightIconContainer;
    @BindView(R.id.bgp_edit_text_right_icon_progress) protected View rightIconProgress;

    private Listener listener;
    private boolean hasXIcon = true;
    private String hint;
    private boolean showMessageIcon = false;
    private boolean editable = true;
    private boolean noFullScreen = false;

    public BgpEditText(Context context) {
        super(context);
        init(null);
    }

    public BgpEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BgpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BgpEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.bgp_edit_text, this);

        ButterKnife.bind(this);

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BgpEditTextAttributes);
            hasXIcon = a.getBoolean(R.styleable.BgpEditTextAttributes_hasXIcon, hasXIcon);
            showMessageIcon = a.getBoolean(R.styleable.BgpEditTextAttributes_showMessageIcon, showMessageIcon);
            editable = a.getBoolean(R.styleable.BgpEditTextAttributes_editable, editable);
            hint = a.getString(R.styleable.BgpEditTextAttributes_hint);
            noFullScreen = a.getBoolean(R.styleable.BgpEditTextAttributes_noFullScreen, false);
            a.recycle();
        }

        setUpViews();
    }

    private void setUpViews() {

        if(!isInEditMode()) { // icons don't work in IDE preview
            Icon icon = showMessageIcon? FontAwesomeIcons.fa_envelope_o : MaterialIcons.md_search;

            leftIcon.setImageDrawable(new IconDrawable(getContext(), icon)
                    .colorRes(R.color.gray_4_medium_light).sizeDp(24));

            updateXIconDisplay();
        }

        if(hint != null) editText.setHint(hint);

        editText.setSingleLine();
        editText.setMaxLines(1);

        if(noFullScreen) editText.setImeOptions(
                editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if(!editable) {
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setClickable(false);
            editText.setKeyListener(null);
        }

        if(editable)
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

        if(editable)
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null)
                        listener.onLetterInputted(s.toString());
                }

            });
    }

    private void updateXIconDisplay() {
        if(hasXIcon) {
            rightIconContainer.setVisibility(View.VISIBLE);
            rightIcon.setImageDrawable(new IconDrawable(getContext(), MaterialIcons.md_clear)
                    .colorRes(R.color.gray_4_medium_light).sizeDp(24));
        } else {
            rightIconContainer.setVisibility(View.GONE);
        }
    }

    // --------------------------------------------------------------- JAVNI INTERFEJS

    public void showXIcon(boolean show) {
        hasXIcon = show;
        updateXIconDisplay();
        invalidate();
    }

    public void setHint(String hint) {
        this.hint = hint;
        editText.setHint(this.hint);
        invalidate();
    }

    public void showProgressOnXIcon(boolean show) {
        rightIcon.setVisibility(show? View.GONE : View.VISIBLE);
        rightIconProgress.setVisibility(show? View.VISIBLE : View.GONE);
    }

    public boolean isShowingProgressOnXIcon() {
        return rightIconProgress.getVisibility() == View.VISIBLE;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Sets text and also moves cursor to the end (which normal EditText.setText() doesn't do */
    public void setText(String text) {
        editText.setText("");
        editText.append(text); // this way the cursor will be at the end after setting text
    }

    public String getText() {
        return editText.getText().toString();
    }

    @OnClick(R.id.bgp_edit_text_right_icon)
    protected void onXClicked() {
        editText.setText("");
        if(listener != null) listener.onXClicked();
    }

    @OnClick(R.id.bgp_edit_text_left_icon)
    protected void onSearchIconClicked() {
        if (listener != null) listener.onSearchSubmitted(editText.getText().toString());
    }

    public interface Listener {
        void onXClicked();
        void onSearchSubmitted(@NonNull String text);
        void onLetterInputted(@NonNull String text);
    }

}
