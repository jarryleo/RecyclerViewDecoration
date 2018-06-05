package cn.leo.recyclerviewdecoration;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Leo on 2018/6/4.
 */

public class FloatDecoration extends RecyclerView.ItemDecoration {
    private int[] mViewTypes;
    private Bitmap mDrawingBitmap; //当前悬浮的条目bitmap
    private Bitmap mDrawingCache; //前一个bitmap缓存
    private int mFloatPosition = -1; //点前悬浮的条目position
    private int mLastFloatPosition = -1; //前一个悬浮的position
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ArrayMap<Integer, RecyclerView.ViewHolder> mHolderCache = new ArrayMap<>();
    private ArrayMap<Integer, Integer> mHeightCache = new ArrayMap<>();

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
                final int lastPosition = mFloatPosition;
                mFloatPosition = firstViewPosition;
                mDrawingBitmap = getBitmap(firstView, parent);
                mLastFloatPosition = lastPosition;
            }
            drawBitmap(c, 0, 0);
            return;
        }
        //第二个条目是悬浮类型
        if (isFloatHolder(secondItemType)) {
            if (mFloatPosition > firstViewPosition) {
                final int lastPosition = mFloatPosition;
                mFloatPosition = findPreFloatPosition(parent);
                mDrawingBitmap = getBitmap(null, parent);
                mLastFloatPosition = lastPosition;
            }
            int top = secondView.getTop() - mDrawingBitmap.getHeight();
            if (top > 0) top = 0;
            drawBitmap(c, 0, top);
        } else if (mDrawingBitmap != null) {
            drawBitmap(c, 0, 0);
        }
    }

    /**
     * 绘制bitmap
     */
    private void drawBitmap(Canvas c, int left, int top) {
        if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
            c.drawBitmap(mDrawingBitmap, left, top, mPaint);
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
     * 获取要悬浮的item的bitmap
     */
    private Bitmap getBitmap(View view, RecyclerView parent) {
        Bitmap bitmap;
        if (mFloatPosition == mLastFloatPosition) {
            bitmap = mDrawingCache;
            mDrawingCache = mDrawingBitmap;
            return bitmap;
        }
        if (view == null) {
            RecyclerView.ViewHolder holder = getHolder(parent);
            int height = mHeightCache.get(mFloatPosition);
            int width = parent.getWidth();
            layoutView(holder.itemView, width, height);
            bitmap = getViewBitmap(holder.itemView);
        } else {
            view.setDrawingCacheEnabled(true);
            Bitmap cacheBitmap = view.getDrawingCache(false);
            bitmap = Bitmap.createBitmap(cacheBitmap);
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(false);
            mHeightCache.put(mFloatPosition, bitmap.getHeight());
        }
        if (mDrawingCache != null && !mDrawingCache.isRecycled()) {
            mDrawingCache.recycle();
        }
        mDrawingCache = mDrawingBitmap;
        return bitmap;
    }


    /**
     * 获取之前要悬浮的holder
     */
    private RecyclerView.ViewHolder getHolder(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        int viewType = adapter.getItemViewType(mFloatPosition);
        RecyclerView.ViewHolder holder = mHolderCache.get(viewType);
        if (holder == null) {
            holder = adapter.
                    createViewHolder(recyclerView, adapter.getItemViewType(mFloatPosition));
            mHolderCache.put(viewType, holder);
        }
        adapter.bindViewHolder(holder, mFloatPosition);
        return holder;
    }

    /**
     * 测量悬浮布局
     */
    private void layoutView(View v, int width, int height) {
        v.layout(0, 0, width, height);
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
    }

    /**
     * 根据view获取bitmap
     */
    private Bitmap getViewBitmap(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        //c.drawColor(Color.WHITE);
        v.layout(0, 0, w, h);
        v.draw(c);
        return bmp;
    }
}
