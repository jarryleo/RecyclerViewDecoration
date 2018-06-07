package cn.leo.recyclerviewdecoration.holder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.leo.recyclerviewdecoration.R;

/**
 * Created by Leo on 2018/6/4.
 */

public class TitleRVHolder extends BaseRVHolder<String> implements View.OnClickListener {
    private TextView tvTitle;
    private final Button mBtn;

    public TitleRVHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tvTitle);
        mBtn = itemView.findViewById(R.id.btnTest);
    }

    @Override
    public void bindView(String title) {
        tvTitle.setText(title);
        tvTitle.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), tvTitle.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
