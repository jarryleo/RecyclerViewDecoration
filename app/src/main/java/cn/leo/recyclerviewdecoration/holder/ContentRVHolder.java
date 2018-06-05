package cn.leo.recyclerviewdecoration.holder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cn.leo.recyclerviewdecoration.R;

/**
 * Created by Leo on 2018/6/4.
 */

public class ContentRVHolder extends BaseRVHolder<String> implements View.OnClickListener {
    private TextView tvTitle;

    public ContentRVHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tvTitle);
    }

    @Override
    public void bindView(String title) {
        tvTitle.setText(title);
        tvTitle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.e("-----", tvTitle.getText().toString());
    }
}
