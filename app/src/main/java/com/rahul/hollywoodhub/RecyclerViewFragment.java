package com.rahul.hollywoodhub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 8/3/16.
 */
public class RecyclerViewFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private String link;
    private boolean fromSearch;
    private RecyclerViewAdapter adapter;
    private View layout;
    private LinearLayout progressBar, swipeMessage;
    private List<Information> list;
    private GridLayoutManager mGridLayoutManager;
    private boolean loading = true;
    private SwipeRefreshLayout swipeContainer;
    private int previousListCount = 0, pageCount=1;

    public RecyclerViewFragment(){
        list = new ArrayList<>();
    }

    public static RecyclerViewFragment newInstance(String link) {
        RecyclerViewFragment recyclerViewFragment= new RecyclerViewFragment();
        Bundle arg = new Bundle();
        arg.putString("link", link);
        recyclerViewFragment.setArguments(arg);
        return recyclerViewFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        link = getArguments().getString("link");
        progressBar = (LinearLayout) view.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeMessage = (LinearLayout) layout.findViewById(R.id.swipe_message);
        swipeMessage.setVisibility(View.GONE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        initializeRecyclerView();
        isNetworkAvailable();
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                pageCount = 1;
                previousListCount = 1;
                swipeMessage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                mRecyclerView.removeAllViews();
                isNetworkAvailable();
            }
        });

    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            if (!swipeContainer.isRefreshing()) {
//                progressBar.setAnimation(CustomAnimation.fadeIn(getContext()));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
            }
//            mRecyclerView.setVisibility(View.GONE);
//            progressBar.setVisibility(View.VISIBLE);
            previousListCount = list.size();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                Log.d("LINK ", params[0]);
                Document document = Jsoup.connect(params[0])
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .followRedirects(true)
                        .get();
                Elements elements = document.getElementsByClass("item");
                for (org.jsoup.nodes.Element e : elements){
                    Information information = new Information();
                    information.title = e.select("a").text();
                    information.image = "http:"+e.select("img").attr("src");
                    information.link = "http://www.watchfree.to"+e.select("a").attr("href");
                    list.add(information);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            try {
                swipeContainer.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    swipeMessage.setVisibility(View.VISIBLE);
                    swipeMessage.bringToFront();
                }
                if (list.isEmpty() || list.size() == previousListCount) {
                    Toast.makeText(getContext(), "Content not found please try again", Toast.LENGTH_SHORT).show();
                    if (pageCount > 1)
                        pageCount--;
                }
                progressBar.setVisibility(View.GONE);
                if (previousListCount != 0)
                    mRecyclerView.smoothScrollToPosition(previousListCount);
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (loading && dy > 0 && !fromSearch) {
                            if (mGridLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() - 1) {
                                loading = false;
                                pageCount++;
                                isNetworkAvailable();
                            }
                        }
                    }
                });
                loading = true;
            }catch (Exception  e){
                e.printStackTrace();
            }
        }
    }

    private void initializeRecyclerView() {
        adapter = new RecyclerViewAdapter(getContext(), list, true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setVisibility(View.VISIBLE);
        int numberOfColumns = 3;
        mGridLayoutManager = new VarColumnGridLayoutManager(getContext(), 240);
//        mGridLayoutManager = new GridLayoutManager(getContext(), numberOfColumns, GridLayoutManager.VERTICAL, false);
//        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return 4;
//            }
//        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);
    }

    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            Log.d("network", "async  called");
            if (fromSearch) {
                new ParserAsyncTask().execute(link);
            }
            else {new ParserAsyncTask().execute(link + "&page=" + pageCount);}
        }
        else showDialogBox();
    }

    public boolean showDialogBox(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setTitle("Network Connectivity");
        dialog.setMessage("No internet connection detected please try again");
        dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isNetworkAvailable();
            }
        });
        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        dialog.setCancelable(false);
        dialog.show();
        return true;
    }
}