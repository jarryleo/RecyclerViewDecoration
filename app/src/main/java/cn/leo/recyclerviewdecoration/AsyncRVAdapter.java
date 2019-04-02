package cn.leo.recyclerviewdecoration;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/3/19 16:09
 */
public abstract class AsyncRVAdapter<T> extends RecyclerView.Adapter {
    private AsyncListDiffer<T> mDiffer;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;
    private DiffUtil.ItemCallback<T> diffCallback = new DiffUtil.ItemCallback<T>() {

        @Override
        public boolean areItemsTheSame(T oldItem, T newItem) {
            //是不是同一个item，不是则加到列表末尾
            return AsyncRVAdapter.this.areItemsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            //如果是同一个item，判断内容是不是相同，不相同则替换成新的
            return AsyncRVAdapter.this.areContentsTheSame(oldItem, newItem);
        }


    };

    public AsyncRVAdapter() {
        mDiffer = new AsyncListDiffer<>(this, diffCallback);
    }

    private AsyncDifferConfig<T> mConfig = new AsyncDifferConfig.Builder<>(diffCallback).build();

    /**
     * 异步比对去重，areItemsTheSame相同areContentsTheSame不同的则替换位置
     *
     * @param oldList 原列表
     * @param newList 新列表
     */
    private void asyncAddData(final List<T> oldList, final List<T> newList) {
        mConfig.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean change = false;
                for (T newItem : newList) {
                    boolean flag = false;
                    for (int i = 0; i < oldList.size(); i++) {
                        T oldItem = oldList.get(i);
                        if (!diffCallback.areItemsTheSame(oldItem, newItem)) {
                            continue;
                        }
                        flag = diffCallback.areContentsTheSame(oldItem, newItem);
                        if (!flag) {
                            oldList.set(i, newItem);
                            change = true;
                            flag = true;
                        }
                        break;
                    }
                    if (!flag) {
                        oldList.add(newItem);
                        change = true;
                    }
                }
                if (change) {
                    mDiffer.submitList(oldList);
                }
            }
        });
    }

    private void asyncAddData(final List<T> data) {
        final List<T> oldList = getData();
        asyncAddData(oldList, data);
    }

    private void asyncAddData(final T data) {
        final List<T> oldList = getData();
        List<T> newList = new ArrayList<>();
        newList.add(data);
        asyncAddData(oldList, newList);
    }

    /**
     * 设置新的数据集
     *
     * @param data 数据
     */
    public void setData(List<T> data) {
        mDiffer.submitList(data);
    }

    /**
     * 新增数据集
     *
     * @param data 数据集
     */
    public void addData(List<T> data) {
        asyncAddData(data);
    }

    /**
     * 新增单条数据
     *
     * @param data 数据
     */
    public void addData(T data) {
        asyncAddData(data);
    }

    /**
     * 根据索引移除条目
     *
     * @param position 条目索引
     */
    public void removeData(int position) {
        if (position < 0 ||
                position >= mDiffer.getCurrentList().size()) {
            return;
        }
        List<T> list = getData();
        list.remove(position);
        mDiffer.submitList(list);
    }

    /**
     * 根据对象移除条目
     * 对象必须重写equals
     *
     * @param data 条目对象
     */
    public void removeData(T data) {
        List<T> list = getData();
        list.remove(data);
        mDiffer.submitList(list);
    }

    /**
     * 清空列表
     */
    public void removeAll() {
        mDiffer.submitList(null);
    }

    /**
     * 获取当前数据集
     *
     * @return 返回一个可修改的数据集，修改数据后通过
     * @see AsyncRVAdapter#setData(List)
     * 可以刷新列表
     */

    public List<T> getData() {
        return new ArrayList<>(mDiffer.getCurrentList());
    }

    /**
     * 获取索引位置对应的条目
     *
     * @param position 索引
     * @return 条目
     */
    public T getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    /**
     * 判断条目是不是相同
     *
     * @param oldItem 旧条目
     * @param newItem 新条目
     * @return 是否相同
     */
    protected boolean areItemsTheSame(T oldItem, T newItem) {
        return oldItem.equals(newItem);
    }

    /**
     * 判断内容是不是相同，
     * 如果条目相同，内容不同则替换条目
     *
     * @param oldItem 旧条目
     * @param newItem 新条目
     * @return 是否相同
     */
    protected boolean areContentsTheSame(T oldItem, T newItem) {
        return oldItem == newItem;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayout(position);
    }

    /**
     * 获取条目类型的布局
     *
     * @param position 索引
     * @return 布局id
     */
    protected abstract @LayoutRes
    int getItemLayout(int position);

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    /**
     * 给条目绑定数据
     *
     * @param helper 条目帮助类
     * @param data   对应数据
     */
    protected abstract void bindData(ItemHelper helper, final T data);


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        /**
         * 点击条目
         *
         * @param adapter  当前适配器
         * @param v        点击的view
         * @param position 条目索引
         */
        void onItemClick(AsyncRVAdapter adapter, View v, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public interface OnItemLongClickListener {
        /**
         * 长按点击条目
         *
         * @param adapter  当前适配器
         * @param v        点击的view
         * @param position 条目索引
         */
        void onItemLongClick(AsyncRVAdapter adapter, View v, int position);
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public interface OnItemChildClickListener {
        /**
         * 点击条目内部的view
         *
         * @param adapter  当前适配器
         * @param v        点击的view
         * @param position 条目索引
         */
        void onItemChildClick(AsyncRVAdapter adapter, View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {
        private final ItemHelper mItemHelper;

        private ViewHolder(ViewGroup parent, int layout) {
            super(LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false));
            mItemHelper = new ItemHelper(itemView);
            mItemHelper.setLayoutResId(layout);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void setData(int position) {
            mItemHelper.setPosition(position);
            bindData(mItemHelper, getItem(position));
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(AsyncRVAdapter.this, v, mItemHelper.getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(AsyncRVAdapter.this, v, mItemHelper.getPosition());
                return true;
            }
            return false;
        }
    }

    public class ItemHelper implements View.OnClickListener {
        private SparseArray<View> viewCache = new SparseArray<>();
        private List<Integer> clickListenerCache = new ArrayList<>();
        private View itemView;
        private @LayoutRes
        int layoutResId;
        private int mPosition;
        /**
         * 携带额外绑定数据便于复用
         */
        private Object tag;

        public ItemHelper(View itemView) {
            this.itemView = itemView;
        }

        public @LayoutRes
        int getItemLayout() {
            return layoutResId;
        }

        private void setLayoutResId(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
        }

        public int getPosition() {
            return mPosition;
        }

        private void setPosition(int position) {
            mPosition = position;
        }

        public View getItemView() {
            return itemView;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public final <V extends View> V getViewById(@IdRes int viewId) {
            View v = viewCache.get(viewId);
            V view;
            if (v == null) {
                view = itemView.findViewById(viewId);
                if (view == null) {
                    String entryName = itemView.getResources().getResourceEntryName(viewId);
                    throw new NullPointerException("id: R.id." + entryName + " can not find in this item!");
                }
                viewCache.put(viewId, view);
            } else {
                view = (V) v;
            }
            return view;
        }

        /**
         * 给按钮或文本框设置文字
         *
         * @param viewId 控件id
         * @param text   设置的文字
         */
        public ItemHelper setText(@IdRes int viewId, CharSequence text) {
            View view = getViewById(viewId);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            } else {
                String entryName = view.getResources().getResourceEntryName(viewId);
                throw new ClassCastException("id: R.id." + entryName + " are not TextView");
            }
            return this;
        }

        /**
         * 给按钮或文本框设置文字
         *
         * @param viewId 控件id
         * @param resId  设置的文字资源
         */
        public ItemHelper setText(@IdRes int viewId, @StringRes int resId) {
            View view = getViewById(viewId);
            if (view instanceof TextView) {
                ((TextView) view).setText(resId);
            } else {
                String entryName = view.getResources().getResourceEntryName(viewId);
                throw new ClassCastException("id: R.id." + entryName + " are not TextView");
            }
            return this;
        }

        /**
         * 给图片控件设置资源图片
         *
         * @param viewId 图片控件id
         * @param resId  资源id
         */
        public ItemHelper setImageResource(@IdRes int viewId, @DrawableRes int resId) {
            View view = getViewById(viewId);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            } else {
                String entryName = view.getResources().getResourceEntryName(viewId);
                throw new ClassCastException("id: R.id." + entryName + " are not ImageView");
            }
            return this;
        }

        /**
         * 设置view的背景
         *
         * @param viewId 控件id
         * @param resId  资源id
         */
        public ItemHelper setBackgroundResource(@IdRes int viewId, @DrawableRes int resId) {
            View view = getViewById(viewId);
            view.setBackgroundResource(resId);
            return this;
        }


        public ItemHelper setVisibility(@IdRes int viewId, int visibility) {
            View view = getViewById(viewId);
            view.setVisibility(visibility);
            return this;
        }

        public ItemHelper setViewVisble(@IdRes int viewId) {
            View view = getViewById(viewId);
            view.setVisibility(View.VISIBLE);
            return this;
        }

        public ItemHelper setViewInvisble(@IdRes int viewId) {
            View view = getViewById(viewId);
            view.setVisibility(View.INVISIBLE);
            return this;
        }

        public ItemHelper setViewGone(@IdRes int viewId) {
            View view = getViewById(viewId);
            view.setVisibility(View.GONE);
            return this;
        }

        /**
         * 给条目中的view添加点击事件
         *
         * @param viewId 控件id
         */
        public ItemHelper addOnClickListener(@IdRes int viewId) {
            boolean contains = clickListenerCache.contains(viewId);
            if (!contains) {
                getViewById(viewId).setOnClickListener(this);
                clickListenerCache.add(viewId);
            }
            return this;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemChildClickListener != null) {
                mOnItemChildClickListener.onItemChildClick(AsyncRVAdapter.this, v, mPosition);
            }
        }
    }
}
