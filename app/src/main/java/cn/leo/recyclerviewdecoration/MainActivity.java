package cn.leo.recyclerviewdecoration;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private TestRvAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rvList);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));
        mAdapter = new TestRvAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new FloatDecoration(R.layout.item_title));
        initData();

        findViewById(R.id.btnRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void initData() {
        List<String> data = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int i1 = random.nextInt(100000);
            data.add(String.valueOf(i1));
        }
        mAdapter.setData(data);
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            System.out.println("app 退 到 后 台");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        appIsTop();
    }

    public boolean appIsTop() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        if (runningTasks != null && runningTasks.size() > 0) {
            ActivityManager.RunningTaskInfo info = runningTasks.get(0);
            String className = info.topActivity.getClassName();
            if (className.contains(getPackageName())) {
                System.out.println("app 在前台");
                return true;
            }
        }
        return false;
    }
}
