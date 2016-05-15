package shmurphy.tacoma.uw.edu.simplyfitter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Gehennom on 5/15/2016.
 */
public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
//    private final int mVerticalSpaceHeight;
//
//    public VerticalSpaceItemDecoration(int mVerticalSpaceHeight) {
//        this.mVerticalSpaceHeight = mVerticalSpaceHeight;
//    }
//
//    @Override
//    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
//                               RecyclerView.State state) {
//        //if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
//            outRect.bottom = mVerticalSpaceHeight;
//        //}
//        //outRect.left/right/top can also be modified for further effects
//    }
    private Drawable mDivider;

    public VerticalSpaceItemDecoration(Context context) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
