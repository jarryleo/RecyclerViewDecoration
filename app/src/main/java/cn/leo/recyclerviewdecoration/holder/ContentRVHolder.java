package cn.leo.recyclerviewdecoration.holder;

import android.view.View;
import android.widget.TextView;

import cn.leo.recyclerviewdecoration.R;

/**
 * Created by Leo on 2018/6/4.
 */

public class ContentRVHolder extends BaseRVHolder<String> {
    private TextView tvTitle;

    public ContentRVHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tvTitle);
    }

    @Override
    public void bindView(String title) {
        tvTitle.setText(title);
    }
}
