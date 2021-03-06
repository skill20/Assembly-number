package com.zql.app_ji.View.Fragments;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zql.app_ji.Adapter.MovieTopRecyclerAdapter;
import com.zql.app_ji.Bean.DoubanMovie;
import com.zql.app_ji.Bean.InterfaceState;
import com.zql.app_ji.Bean.MessageEvent;
import com.zql.app_ji.Listener.OnVerticalScrollListener;
import com.zql.app_ji.Prestener.PrestenerMovieFragment;
import com.zql.app_ji.Prestener.PrestenerMovieFragmentImp;
import com.zql.app_ji.R;
import com.zql.app_ji.Bean.MessageEventType;
import com.zql.app_ji.View.Viewabout.StaggeredDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class Movie_topFragment extends BaseFragment implements Movie_topFragmentImp {
    private PrestenerMovieFragmentImp prestenerMovieFragmentImp;
    private RecyclerView recyclerView_movie_top;
    private MovieTopRecyclerAdapter movie_top_RecyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DoubanMovie doubanMovie;
    private List<DoubanMovie.SubjectsBean> subjectsBeans;
    private Handler mhander,dhander;
    private SmartRefreshLayout smartRefreshLayout_top;
    private View movieview;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       this.movieview=inflater.inflate(R.layout.fragment_movie_top,container,false);
        initPrester();
        initView(movieview);
        prestenerMovieFragmentImp.setNightStateBackgroundtoTop();
        smartRefreshLayout_top.autoRefresh();
        return movieview;
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId()==11){
            DoubanMovie.SubjectsBean subjectsBean=movie_top_RecyclerAdapter.getLongClickResult();
            switch (item.getItemId()){
                case 0:
                    prestenerMovieFragmentImp.addFavoritetoDataBase(subjectsBean);
                    Toast.makeText(getContext(), "已收藏："+subjectsBean.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    shareDialog(subjectsBean.getId());
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }
    private void shareDialog(String url){//创建分享菜单界面
        Intent mintent=new Intent(Intent.ACTION_SEND);
        mintent.setType("text/plain");
        mintent.putExtra(Intent.EXTRA_TEXT,"http://m.douban.com/movie/subject/"+url+"/mobile");
        startActivity(mintent);
    }

    private void initView(View view){
        subjectsBeans=new ArrayList<>();
        doubanMovie=new DoubanMovie();
        recyclerView_movie_top=(RecyclerView)view.findViewById(R.id.recyclerview_movie_top);
        layoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        ((StaggeredGridLayoutManager) layoutManager).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        movie_top_RecyclerAdapter=new MovieTopRecyclerAdapter(getContext(),subjectsBeans,prestenerMovieFragmentImp);
        recyclerView_movie_top.setLayoutManager(layoutManager);
        recyclerView_movie_top.setAdapter(movie_top_RecyclerAdapter);
        recyclerView_movie_top.setOnScrollListener(new OnVerticalScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ((StaggeredGridLayoutManager) layoutManager).invalidateSpanAssignments();
            }
        });
        smartRefreshLayout_top=(SmartRefreshLayout)view.findViewById(R.id.smartfresh_movie_top);
        smartRefreshLayout_top.setRefreshHeader(new MaterialHeader(getContext()).setColorSchemeColors(getResources().getColor(R.color.colorAccent),getResources().getColor(R.color.color_song)));
        smartRefreshLayout_top.setOnRefreshListener(new OnRefreshListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                prestenerMovieFragmentImp.getMoreDoubantopMoviefromDoubanAPI(0,25,0);
                dhander=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what){
                            case MessageEventType.MOVIE_UP_REFECH_DATE:
                                refreshLayout.finishRefresh();
                                subjectsBeans.clear();
                                subjectsBeans.addAll(doubanMovie.getSubjects());
                                movie_top_RecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                };
            }
        });
        smartRefreshLayout_top.setOnLoadMoreListener(new OnLoadMoreListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                prestenerMovieFragmentImp.getMoreDoubantopMoviefromDoubanAPI(movie_top_RecyclerAdapter.getItemCount(),25,1);
                mhander=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what){
                            case MessageEventType.MOVIE_DOWN_REFECH_DATE:
                                refreshLayout.finishLoadMore();
                                subjectsBeans.addAll(movie_top_RecyclerAdapter.getItemCount(),doubanMovie.getSubjects());
                                movie_top_RecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                };
            }
        });

    }
    private void initPrester(){
        prestenerMovieFragmentImp=new PrestenerMovieFragment(this);
    }

    /**
     * 夜间模式
     * @param interfaceState
     */
    @Override
    public void setTheNightstateonFragment(InterfaceState interfaceState) {
        ConstraintLayout constraintLayout=(ConstraintLayout)movieview.findViewById(R.id.fragment_movie_top);
        constraintLayout.setBackgroundColor(interfaceState.getBackgroundcolor());
    }
    /**
     * 切换到夜间
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handEvent(MessageEvent messageEvent){
        switch (messageEvent.getMessagetype()){
            case MessageEvent.NIGHTCHAGE:
                prestenerMovieFragmentImp.setNightStateBackgroundtoTop();
                movie_top_RecyclerAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void setRecyclerItemonPagefromDouban(DoubanMovie mdoubanMovie,int type) {
        this.doubanMovie=mdoubanMovie;
        switch (type){
            case 0:
                dhander.sendEmptyMessage(MessageEventType.MOVIE_UP_REFECH_DATE);
                break;
            case 1:
                mhander.sendEmptyMessage(MessageEventType.MOVIE_DOWN_REFECH_DATE);
                break;
                default:
                    break;
        }
    }

    @Override
    public void setErrorMessageonFragment(String errorMessageonFragment) {
        Toast.makeText(getContext(), errorMessageonFragment, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getBaseContext() {
        return getContext();
    }

    @Override
    public Application getbaseapplication() {
        return getActivity().getApplication();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
