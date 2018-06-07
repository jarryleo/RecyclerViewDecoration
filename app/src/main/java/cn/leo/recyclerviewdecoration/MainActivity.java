package cn.leo.recyclerviewdecoration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rvList);

        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new MyRVAdapter());

        mRecyclerView.addItemDecoration(new FloatDecoration(0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
