package com.rahul.hollywoodhub;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MovieInfo extends Activity {

    Information movieData;
    TextView title, release, duration, genre, story, rating, director, writer, cast;
    ImageView image;
    private String link;
    private RecyclerViewAdapter adapter;
    LinearLayout movieLayout, tvSeriesLayout;
    View rootLayout;
    List<Information> downloadList;
    RecyclerView recyclerView;
    RatingBar ratingBar;
    Spinner selectSeason, selectEpisode;
    List<LinkedHashMap<String, String>> downloadListTVSeason;
    ProgressBar recyclerProgressbar;
    View viewSpanDwnlnk;
    private ProgressDialog dialog;
    private Button submitButtonTVSeries;
    boolean isTVSeries = false, changeFromSeasonAsyncTask = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);
        movieData = new Information();
        intializeView();
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        link = (String) getIntent().getExtras().get("link");
        movieData.title = (String) getIntent().getExtras().get("title");
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        Picasso.with(getApplicationContext()).load((String) getIntent().getExtras().get("image")).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                rootLayout.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(getApplicationContext(), bitmap)));
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });

        initializeAd();

        dialog = new ProgressDialog(MovieInfo.this);
        dialog.setTitle((String) getIntent().getExtras().get("title"));
        dialog.setMessage("Please wait while fetching movie data");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        ParserAsyncTask parserAsyncTask = new ParserAsyncTask();
        parserAsyncTask.execute(link);
    }

    private void initializeAd() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            downloadList = new ArrayList<>();
            recyclerView.setVisibility(View.INVISIBLE);
            recyclerProgressbar.setVisibility(View.VISIBLE);
            if (!changeFromSeasonAsyncTask) {
                movieLayout.setVisibility(View.INVISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0])
                        .timeout(0)
                        .followRedirects(true)
                        .get();

//                Log.v("link ->> ", params[0]+document.hasClass("tv_container"));
                Elements elements = document.getElementsByClass("tv_container");
                //CHECK FOR TV-SERIES PAGE
                if (elements.size() != 0) {
                    isTVSeries = true;
                    downloadListTVSeason = new ArrayList<>();
                    Log.v("TV ", "called");
                    for (org.jsoup.nodes.Element e : elements.first().getElementsByClass("show_season")){
                        LinkedHashMap<String, String> dwnlnk = new LinkedHashMap<>();
                        for (org.jsoup.nodes.Element seasonLink : e.select("a")){
                            dwnlnk.put(seasonLink.ownText()+" : "+seasonLink.select("span").get(0).text(), "http://www.watchfree.to" + seasonLink.attr("href"));
                        }
                        downloadListTVSeason.add(dwnlnk);
                    }
                }
                else {
                    Log.v("MOVIE ", "called");
                    elements = document.select("div.list_links").select("table");
                    for (Element e : elements) {
                        Information downloadLinkInfo = new Information();
                        downloadLinkInfo.downloadLinkInfo = new DownloadLinkInfo();
                        downloadLinkInfo.downloadLinkInfo.link = "http://www.watchfree.to" + e.select("a").attr("href");
                        downloadLinkInfo.downloadLinkInfo.title = e.getElementsByAttributeValue("align", "left").first().text();
                        downloadLinkInfo.downloadLinkInfo.quality = e.getElementsByClass("quality").text();
                        downloadList.add(downloadLinkInfo);
                    }
                }
                elements = document.getElementsByClass("movie_data");
                movieData.rating = document.getElementsByClass("movie_info_header").first().select("strong").eq(0).text();
                movieData.story = elements.first().getElementsByClass("synopsis").first().text();

                for (Element e:elements.first().select("tr")){
                    String query = e.select("th").text()+" : "+e.select("td").text();
                    if (query.contains("Genre"))
                        movieData.genre = query;
                    else if (query.contains("Release"))
                        movieData.release = query;
                    else if (query.contains("Duration"))
                        movieData.duration = query;
                    else if (query.contains("Produce"))
                        movieData.producer = query;
                    else if (query.contains("Direct"))
                        movieData.director = query;
                    else if (query.contains("Star"))
                        movieData.stars = query;
                }
            }catch (Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MovieInfo.this, "Network error please try again ", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Picasso.with(getApplicationContext()).load((String)getIntent().getExtras().get("image")).into(image);
            if (isTVSeries){
                tvSeriesLayout.setVisibility(View.VISIBLE);
                final ArrayAdapter<String> seasonAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
                final ArrayAdapter<String> episodeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
                seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                episodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (int i=1; i<=downloadListTVSeason.size(); ++i){
                    seasonAdapter.add("Season "+i);
                }
                selectSeason.setAdapter(seasonAdapter);
//                selectSeason.setSelection(selectSeason.getSelectedItemPosition(), false);
                selectSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        episodeAdapter.clear();
                        for (String episode : downloadListTVSeason.get(position).keySet()) {
                            episodeAdapter.add(episode);
                        }
                        episodeAdapter.notifyDataSetChanged();
                        selectEpisode.setAdapter(episodeAdapter);
                        selectEpisode.setSelection(selectEpisode.getSelectedItemPosition(), false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                submitButtonTVSeries.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isTVSeries = false;
                        changeFromSeasonAsyncTask = true;
                        new ParserAsyncTask().execute(
                                downloadListTVSeason
                                        .get(selectSeason.getSelectedItemPosition())
                                        .get(selectEpisode.getSelectedItem().toString())
                        );
                        selectEpisode.requestFocus();
                        submitButtonTVSeries.setVisibility(View.GONE);
                        recyclerProgressbar.requestFocus();
                    }
                });

//                selectEpisode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        isTVSeries = false;
//                        changeFromSeasonAsyncTask = true;
//                        new ParserAsyncTask().execute(
//                                downloadListTVSeason
//                                        .get(selectSeason.getSelectedItemPosition())
//                                        .get(selectEpisode.getSelectedItem().toString())
//                        );
//                        recyclerView.scrollToPosition(0);
//                        selectEpisode.requestFocus();
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//
//                    }
//                });
            }

            if (!downloadList.isEmpty())
                viewSpanDwnlnk.setVisibility(View.GONE);

            adapter = new RecyclerViewAdapter(getApplicationContext(), downloadList, false);
            recyclerView.setAdapter(adapter);
            WrappingLinearLayoutManager layout = new WrappingLinearLayoutManager(getApplicationContext());
            layout.setSmoothScrollbarEnabled(true);
            recyclerView.setLayoutManager(layout);
            movieLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerProgressbar.setVisibility(View.GONE);
            submitButtonTVSeries.setVisibility(View.VISIBLE);

            if (!changeFromSeasonAsyncTask) {
                setData();
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        }
    }


    private void setData() {
        title.setText(movieData.title);
        duration.setText(movieData.duration);
        release.setText(movieData.release);
        rating.setText("Rating : " + movieData.rating);
        if (movieData.rating != null) {
            if (!movieData.rating.contains("N")) {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating((float) (Float.parseFloat((movieData.rating.split("/")[0])) / 2.0));
            }
        }
        else ratingBar.setVisibility(View.GONE);
        genre.setText(movieData.genre);
        story.setText(movieData.story);
        if (movieData.director == null)
            director.setVisibility(View.GONE);
        director.setText(movieData.director);
        writer.setText(movieData.producer);
        cast.setText(movieData.stars);
    }

    private void intializeView() {
        title = (TextView) findViewById(R.id.title);
        image = (ImageView) findViewById(R.id.image);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        release = (TextView) findViewById(R.id.release);
        duration = (TextView) findViewById(R.id.duration);
        genre = (TextView) findViewById(R.id.genre);
        story = (TextView) findViewById(R.id.story);
        rating = (TextView) findViewById(R.id.rating);
        director = (TextView) findViewById(R.id.director);
        writer = (TextView) findViewById(R.id.writer);
        cast = (TextView) findViewById(R.id.cast);
        movieLayout = (LinearLayout) findViewById(R.id.movie_layout);
        rootLayout = (View) findViewById(R.id.movie_root_layout);
        tvSeriesLayout = (LinearLayout) findViewById(R.id.tvseries_layout);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        selectEpisode = (Spinner) findViewById(R.id.select_episode);
        selectSeason = (Spinner) findViewById(R.id.select_season);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_download);
        recyclerProgressbar = (ProgressBar) findViewById(R.id.recycler_progressbar);
//        recyclerProgressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(android.R.color.white),
//                android.graphics.PorterDuff.Mode.SRC_ATOP);
        submitButtonTVSeries = (Button) findViewById(R.id.submitButtonTVSeries);
        viewSpanDwnlnk = findViewById(R.id.view_span_dwnlnk);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.rating), PorterDuff.Mode.SRC_ATOP);
    }
}
