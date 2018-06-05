package cn.leo.recyclerviewdecoration.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Leo on 2018/6/4.
 */

public abstract class BaseRVHolder<T> extends RecyclerView.ViewHolder {
    public BaseRVHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindView(T t);
}
