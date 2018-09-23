package knez.assdroid.util.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private final boolean showOnFirstItemTop;
    private final boolean showOnLastItemBottom;

    private Drawable mDivider;

    /**
     * Default divider will be used
     */
    public DividerItemDecoration(Context context,
                                 boolean showOnFirstItemTop,
                                 boolean showOnLastItemBottom) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        this.showOnFirstItemTop = showOnFirstItemTop;
        this.showOnLastItemBottom = showOnLastItemBottom;
        styledAttributes.recycle();
    }

    /**
     * Custom divider will be used
     */
    public DividerItemDecoration(Context context,
                                 int resId,
                                 boolean showOnFirstItemTop,
                                 boolean showOnLastItemBottom) {
        mDivider = ContextCompat.getDrawable(context, resId);
        this.showOnFirstItemTop = showOnFirstItemTop;
        this.showOnLastItemBottom = showOnLastItemBottom;
    }

    @Override
    public void onDrawOver(
            @NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        if(childCount == 0) return;

        // Skipujes prvu poziciju SAMO AKO je to STVARNO prva pozicija. Posto ti ovde iteriras samo
        // po vidljivim stavkama sto je mozda pet stavki od pedeset i neke... Pa vidi koja je prva
        // vidljiva pozicija i primeni skipFirstItem tek ako je prva vidljiva ona globalno prva
        LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();

        int firstPosition = llm.findFirstVisibleItemPosition();
        int lastPosition = llm.findLastVisibleItemPosition();
        boolean firstItemVisible = firstPosition == 0;
        boolean lastItemVisible = parent.getAdapter().getItemCount()-1 == lastPosition;


        // Ako treba i na vrhu prve stavke da se crta jedna kerefeka
        if(firstItemVisible && showOnFirstItemTop) {
            View child = parent.getChildAt(0);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getTop() - params.topMargin - mDivider.getIntrinsicHeight()/2;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

        for (int i = 0; i < childCount; i++) {
            // ako je ovo zadnja stavka sad, i to je STVARNO ZADNJA od svih stavki, a receno je da
            // ne crtas kerefeke na zadnju stavku, onda preskoci je (kraj fakticki)
            if(i == childCount-1 && lastItemVisible && !showOnLastItemBottom) continue;//break isto

            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin - mDivider.getIntrinsicHeight()/2;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}