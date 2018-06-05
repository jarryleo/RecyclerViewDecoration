package cn.leo.recyclerviewdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Leo on 2018/6/4.
 */

public class FloatDecoration extends RecyclerView.ItemDecoration {
    private int[] mViewTypes;
    private View mFloatView;
    private int mFloatPosition = -1;
    private ArrayMap<Integer, Integer> mHeightCache = new ArrayMap<>();
    private ArrayMap<Integer, RecyclerView.ViewHolder> mHolderCache = new ArrayMap<>();
    private boolean hasInit = false;

    public FloatDecoration(int... viewType) {
        mViewTypes = viewType;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        View firstView = layoutManager.getChildAt(0);
        View secondView = layoutManager.getChildAt(1);
        if (firstView == null || secondView == null) return;
        RecyclerView.Adapter adapter = parent.getAdapter();
        int firstViewPosition = parent.getChildAdapterPosition(firstView);
        int SecondViewPosition = parent.getChildAdapterPosition(secondView);
        int firstItemType = adapter.getItemViewType(firstViewPosition);
        int secondItemType = adapter.getItemViewType(SecondViewPosition);
        //第一个条目是悬浮类型
        if (isFloatHolder(firstItemType)) {
            if (firstViewPosition != mFloatPosition) {
                mFloatPosition = firstViewPosition;
                mFloatView = getFloatView(parent, firstView);
                touch(parent);
            }
            drawFloatView(mFloatView, c, 0, 0);
            return;
        }
        //第二个条目是悬浮类型
        if (isFloatHolder(secondItemType)) {
            if (mFloatPosition > firstViewPosition) {
                mFloatPosition = findPreFloatPosition(parent);
                mFloatView = getFloatView(parent, null);
            }
            int top = secondView.getTop() - mFloatView.getHeight();
            if (top > 0) top = 0;
            drawFloatView(mFloatView, c, 0, top);
        } else if (mFloatView != null) {
            drawFloatView(mFloatView, c, 0, 0);
        }
    }

    /**
     * 处理悬浮条目触摸事件
     */
    private void touch(final RecyclerView parent) {
        if (hasInit) return;
        hasInit = true;
        parent.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private GestureDetectorCompat mGestureDetectorCompat =
                    new GestureDetectorCompat(parent.getContext(), new MyGestureListener(parent));

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                mGestureDetectorCompat.onTouchEvent(e);
                final int floatHeight = mHeightCache.get(mFloatPosition);
                return e.getY() < floatHeight;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                mGestureDetectorCompat.onTouchEvent(e);
            }
        });
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private RecyclerView mRecyclerView;

        MyGestureListener(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final int floatHeight = mHeightCache.get(mFloatPosition);
            boolean b = e.getY() < floatHeight;
            if (b) {
                childClick(getHolder(mRecyclerView).itemView, e.getX(), e.getY());
            }
            return true;
        }

        /**
         * 遍历容器和它的子view，传递点击事件
         */
        private void childClick(View v, float x, float y) {
            Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            if (rect.contains((int) x, (int) y)) {
                v.performClick();
            }
            if (v instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) v;
                int childCount = ((ViewGroup) v).getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = viewGroup.getChildAt(i);
                    childClick(view, x, y);
                }
            }
        }
    }

    /**
     * 绘制悬浮条目
     */
    private void drawFloatView(View v, Canvas c, int left, int top) {
        if (v == null) return;
        c.save();
        c.translate(left, top);
        v.draw(c);
        c.restore();
    }

    /**
     * 判断条目类型是否需要悬浮
     */
    private boolean isFloatHolder(int type) {
        for (int viewType : mViewTypes) {
            if (type == viewType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找之前的悬浮标题position
     */
    private int findPreFloatPosition(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        View firstVisibleView = recyclerView.getLayoutManager().getChildAt(0);
        int childAdapterPosition = recyclerView.getChildAdapterPosition(firstVisibleView);
        for (int i = childAdapterPosition; i >= 0; i--) {
            if (isFloatHolder(adapter.getItemViewType(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取要悬浮的item的bitmap
     */
    private View getFloatView(RecyclerView parent, View view) {
        if (view != null) {
            mHeightCache.put(mFloatPosition, view.getHeight());
        }
        return getHolder(parent).itemView;
    }


    /**
     * 获取之前要悬浮的holder
     */
    private RecyclerView.ViewHolder getHolder(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        int viewType = adapter.getItemViewType(mFloatPosition);
        RecyclerView.ViewHolder holder = mHolderCache.get(viewType);
        if (holder == null) {
            holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(mFloatPosition));
            mHolderCache.put(viewType, holder);
            int height = mHeightCache.get(mFloatPosition);
            int width = recyclerView.getWidth();
            layoutView(holder.itemView, width, height);
        }
        adapter.bindViewHolder(holder, mFloatPosition);
        return holder;
    }

    /**
     * 测量悬浮布局
     */
    private void layoutView(View v, int width, int height) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
    }
}
