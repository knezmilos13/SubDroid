package knez.assdroid.util.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MeasurableLinearLayout extends LinearLayout {

	private OnIzmeren listener;

	public MeasurableLinearLayout(Context context) {
		super(context);
	}
	public MeasurableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(listener != null) listener.onIzmeren(getMeasuredHeight(), getMeasuredWidth());
	}
	
	public interface OnIzmeren {
		void onIzmeren(int sirina, int visina);
	}
	
	public void setListener(OnIzmeren listener) {
		this.listener = listener;
	}
	

}
