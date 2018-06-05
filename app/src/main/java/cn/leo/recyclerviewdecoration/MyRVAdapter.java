package cn.leo.recyclerviewdecoration;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.leo.recyclerviewdecoration.holder.BaseRVHolder;
import cn.leo.recyclerviewdecoration.holder.ContentRVHolder;
import cn.leo.recyclerviewdecoration.holder.TitleRVHolder;

/**
 * Created by Leo on 2018/6/4.
 */

public class MyRVAdapter extends RecyclerView.Adapter<BaseRVHolder> {
    private List<String> mData = new ArrayList<>();

    public MyRVAdapter() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int i1 = random.nextInt(100000);
            mData.add(String.valueOf(i1));
        }
    }


    @NonNull
    @Override
    public BaseRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseRVHolder holder;
        if (viewType == 0) {
            View title = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title, parent, false);
            holder = new TitleRVHolder(title);
        } else {
            View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
            holder = new ContentRVHolder(content);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRVHolder holder, int position) {
        String string = mData.get(position);
        holder.bindView(string);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 10 == 0) {
            return 0;
        }
        return 1;
    }
}
