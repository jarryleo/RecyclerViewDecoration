package cn.leo.recyclerviewdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Leo on 2018/6/4.
 */

public class FloatDecoration extends RecyclerView.ItemDecoration {
    private ArrayMap<Integer, Integer> mHeightCache = new ArrayMap<>();
    private ArrayMap<Integer, Integer> mTypeCache = new ArrayMap<>();
    private ArrayMap<Integer, RecyclerView.ViewHolder> mHolderCache = new ArrayMap<>();
    private int[] mViewTypes;
    private View mFloatView;
    private int mFloatPosition = -1;
    private int mFloatBottom;
    private int lastLayoutCount;
    private boolean hasInit = false;
    private int mRecyclerViewPaddingLeft;
    private int mRecyclerViewPaddingRight;
    private int mRecyclerViewPaddingTop;
    private int mRecyclerViewPaddingBottom;
    private int mHeaderLeftMargin;
    private int mHeaderTopMargin;
    private int mHeaderRightMargin;
    private Rect mClipBounds = new Rect();

    public FloatDecoration(int... viewType) {
        mViewTypes = viewType;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        View firstView = parent.findChildViewUnder(mClipBounds.left, mRecyclerViewPaddingTop + mHeaderTopMargin);
        if (firstView == null) firstView = layoutManager.getChildAt(0);
        View secondView = parent.findChildViewUnder(mClipBounds.left, mFloatBottom);
        if (firstView == null) return;
        if (secondView == null) return;
        RecyclerView.Adapter adapter = parent.getAdapter();
        int firstViewPosition = parent.getChildAdapterPosition(firstView);
        int secondViewPosition = parent.getChildAdapterPosition(secondView);
        for (int i = secondViewPosition - 1; i > firstViewPosition; i--) {
            int itemViewType = adapter.getItemViewType(i);
            if (isFloatHolder(itemViewType)) {
                View view = layoutManager.findViewByPosition(i);
                if (view.getLeft() == firstView.getLeft()) {
                    secondView = view;
                    secondViewPosition = i;
                }
                break;
            }
        }
        int firstItemType = adapter.getItemViewType(firstViewPosition);
        int secondItemType = adapter.getItemViewType(secondViewPosition);
        if (!hasInit) {
            touch(parent);
        }
        if (isFloatHolder(firstItemType)) {
            if (firstViewPosition != mFloatPosition) {
                mFloatPosition = firstViewPosition;
                mFloatView = getFloatView(parent, firstView);
            }
            int top = 0;
            if (isFloatHolder(secondItemType)) {
                if (mFloatView == null || secondView == null) return;
                top = secondView.getTop() - mFloatView.getHeight() - mRecyclerViewPaddingTop;
            }
            drawFloatView(mFloatView, c, top);
            return;
        }
        if (isFloatHolder(secondItemType)) {
            if (mFloatPosition > firstViewPosition) {
                mFloatPosition = findPreFloatPosition(parent);
                mFloatView = getFloatView(parent, null);
            }
            if (mFloatView == null || secondView == null) return;
            int top = secondView.getTop() - mFloatView.getHeight() - mRecyclerViewPaddingTop;
            drawFloatView(mFloatView, c, top);
            return;
        }
        if (mFloatView == null || lastLayoutCount != layoutManager.getChildCount()) {
            mFloatPosition = findPreFloatPosition(parent);
            mFloatView = getFloatView(parent, null);
        }
        lastLayoutCount = layoutManager.getChildCount();
        drawFloatView(mFloatView, c, 0);
    }

    /**
     * 绘制悬浮条目
     */
    private void drawFloatView(View v, Canvas c, int top) {
        if (v == null) return;
        mClipBounds.top = mRecyclerViewPaddingTop + mHeaderTopMargin;
        mClipBounds.bottom = top + mClipBounds.top + v.getHeight();
        c.save();
        c.clipRect(mClipBounds, Region.Op.REPLACE);
        c.translate(mRecyclerViewPaddingLeft + mHeaderLeftMargin,
                top + mRecyclerViewPaddingTop + mHeaderTopMargin);
        v.draw(c);
        c.restore();
    }

    /**
     * 处理悬浮条目触摸事件
     */
    private void touch(final RecyclerView parent) {
        if (hasInit) return;
        hasInit = true;
        parent.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private GestureDetectorCompat mGestureDetectorCompat =
                    new GestureDetectorCompat(parent.getContext(), new MyGestureListener());

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return isContains(e);
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                isContains(e);
            }

            private boolean isContains(MotionEvent e) {
                boolean contains = mClipBounds.contains((int) e.getX(), (int) e.getY());
                if (contains) {
                    mGestureDetectorCompat.onTouchEvent(e);
                    mFloatView.onTouchEvent(e);
                }
                Rect drawRect = new Rect();
                parent.getDrawingRect(drawRect);
                drawRect.top = mClipBounds.bottom;
                drawRect.left = mRecyclerViewPaddingLeft;
                drawRect.right -= mRecyclerViewPaddingRight;
                drawRect.bottom -= mRecyclerViewPaddingBottom;
                contains = !drawRect.contains((int) e.getX(), (int) e.getY());
                return contains;
            }

        });
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mClipBounds.contains((int) e.getX(), (int) e.getY())) {
                childClick(mFloatView,
                        e.getX() - mRecyclerViewPaddingLeft,
                        e.getY() - mRecyclerViewPaddingTop);
            }
            return true;
        }

        /**
         * 遍历容器和它的子view，传递点击事件
         */
        private void childClick(View v, float x, float y) {
            Rect rect = new Rect();
            v.getGlobalVisibleRect(rect);
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
     * 获取要悬浮的itemView
     */
    private View getFloatView(RecyclerView parent, View view) {
        if (mFloatPosition < 0) return null;
        if (view != null && view.getHeight() > 0) {
            mHeightCache.put(mFloatPosition, view.getHeight());
            mTypeCache.put(parent.getAdapter().getItemViewType(mFloatPosition), view.getHeight());
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
        }
        adapter.bindViewHolder(holder, mFloatPosition);
        layoutView(holder.itemView, recyclerView);
        return holder;
    }

    /**
     * 测量悬浮布局
     */
    private void layoutView(View v, RecyclerView parent) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            // 标签默认宽度占满parent
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            v.setLayoutParams(lp);
        }

        // 对高度进行处理
        int heightMode = View.MeasureSpec.EXACTLY;
        Integer height = mHeightCache.get(mFloatPosition);
        if (height == null) {
            height = mTypeCache.get(parent.getAdapter().getItemViewType(mFloatPosition));
        }
        int heightSize = height == null ? mClipBounds.height() : height;

        mRecyclerViewPaddingLeft = parent.getPaddingLeft();
        mRecyclerViewPaddingRight = parent.getPaddingRight();
        mRecyclerViewPaddingTop = parent.getPaddingTop();
        mRecyclerViewPaddingBottom = parent.getPaddingBottom();

        if (lp instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mHeaderLeftMargin = mlp.leftMargin;
            mHeaderTopMargin = mlp.topMargin;
            mHeaderRightMargin = mlp.rightMargin;
        }

        // 最大高度为RecyclerView的高度减去padding
        final int maxHeight = parent.getHeight() - mRecyclerViewPaddingTop - mRecyclerViewPaddingBottom;
        // 不能超过maxHeight
        heightSize = Math.min(heightSize, maxHeight);

        // 因为标签默认宽度占满parent，所以宽度强制为RecyclerView的宽度减去padding
        int widthSize = parent.getWidth() - mRecyclerViewPaddingLeft -
                mRecyclerViewPaddingRight - mHeaderLeftMargin - mHeaderRightMargin;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            widthSize /= spanCount;
        }
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        // 强制测量
        v.measure(widthSpec, heightSpec);

        int left = mRecyclerViewPaddingLeft + mHeaderLeftMargin;
        int right = v.getMeasuredWidth() + left;
        int top = mRecyclerViewPaddingTop + mHeaderTopMargin;
        int bottom = v.getMeasuredHeight() + top;

        // 位置强制布局在顶部
        v.layout(left, top, right, bottom);

        mClipBounds.top = top;
        mClipBounds.bottom = bottom;
        mClipBounds.left = left;
        mClipBounds.right = right;
        mFloatBottom = bottom;
    }
}
