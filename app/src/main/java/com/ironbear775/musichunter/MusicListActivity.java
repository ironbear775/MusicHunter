package com.ironbear775.musichunter;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.footer.FalsifyFooter;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by ironbear775 on 2017/12/30.
 */

public class MusicListActivity extends BaseActivity {

    private ArrayList<Music> musicArrayList = new ArrayList<>();
    private String search;
    private MusicListAdapter adapter;
    private ArrayListReceiver receiver;
    private int count  = 1;
    private SmartRefreshLayout refreshLayout;
    private boolean isRefreshing,isLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musiclist_layout);
        musicArrayList.clear();
        musicArrayList = (ArrayList<Music>) getIntent().getSerializableExtra("List");
        search = getIntent().getStringExtra("search");

        receiver = new ArrayListReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ArrayList Is Ready");
        filter.addAction("Search Failed");

        registerReceiver(receiver, filter);

        RecyclerView listView = findViewById(R.id.music_list);
        refreshLayout = findViewById(R.id.refresj_layout);

        adapter = new MusicListAdapter(
                MusicListActivity.this, musicArrayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false);

        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Intent intent = new Intent(MusicListActivity.this,MusicDetailActivity.class);
                intent.putExtra("Music",musicArrayList.get(position));
                startActivity(intent);
            }
        });

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                musicArrayList.clear();
                MainActivity.searchMusic(getApplicationContext(), search,
                        String.valueOf(1), false, musicArrayList);
                isRefreshing = true;
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                MainActivity.searchMusic(getApplicationContext(), search,
                        String.valueOf(count++), false, musicArrayList);
                isLoading = true;
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private class ArrayListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "ArrayList Is Ready")) {
                adapter.notifyDataSetChanged();
                if (isRefreshing){
                    refreshLayout.finishRefresh();
                    isRefreshing = false;
                }

                if (isLoading){
                    refreshLayout.finishLoadMore();
                    isLoading = false;
                }
            }else if (Objects.equals(intent.getAction(), "Search Failed")){
                if (isRefreshing){
                    refreshLayout.finishRefresh(false);
                    isRefreshing = false;
                }

                if (isLoading){
                    refreshLayout.finishLoadMore(false);
                    isLoading = false;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
