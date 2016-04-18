package com.rahul.hollywoodhub_old_source;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,android.support.v7.widget.SearchView.OnQueryTextListener {
    private Toolbar toolbar;
    private static ImageView header;
    private CollapsingToolbarLayout collapsingToolbar;
    private TabPagerAdapter mTabPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    public String[] genre;
    public Spinner spinner;
    TextView genreIVButton;
    private static int RESULT_LOAD_IMAGE = 1;
    public static final String SHAREDPREFRENCES_STRING = "SHAREDPREFRENCES_DATA";
    private String searchQuery="", MOVIE_HEADER_IMAGE="movie_header",TVSERIES_HEADER_IMAGE="movie_tvseries";
    static boolean fromMoviePage = true;
    String tvSeriesText = "", imgDecodableString;
    Snackbar snack;
    ImageView imageView;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedPreferencesEditor;
    private android.support.v7.widget.SearchView searchView;
    boolean doubleBackToExitPressedOnce = false;
    static String GENRE_LINK="http://www.watchfree.to/?genre=", SEARCH_SECTION_TV="&search_section=2", SEARCH_SECTION_MOVIE="&search_section=1";
    static String DEFAULT_LINK="http://www.watchfree.to/?sort=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        genre = getResources().getStringArray(R.array.genre);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        genreIVButton = (TextView) findViewById(R.id.genre_iv);

        sharedPreferences = this.getSharedPreferences(MainActivity.SHAREDPREFRENCES_STRING, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        header = (ImageView) findViewById(R.id.backdrop);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        header.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sharedPreferencesEditor.clear();
                sharedPreferencesEditor.commit();
                setHeaderImage(fromMoviePage);
                return true;
            }
        });

        genreIVButton.setTranslationY(-20f);

        spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.genre, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (genre[position].equalsIgnoreCase("all")) {
                    collapsingToolbar.setTitle("Hollywood");
                    getSupportActionBar().setTitle("Hollywood");
                } else {
                    collapsingToolbar.setTitle(genre[position]);
                    getSupportActionBar().setTitle(genre[position]);
                }
                searchQuery = "";
                if (position == 0)
                    setViewPagerAdapter(DEFAULT_LINK);
                else
                    setViewPagerAdapter(GENRE_LINK + spinner.getSelectedItem().toString() + "&sort=");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        genreIVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initializeAd();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setHeaderImage(fromMoviePage);
    }

    private void setViewPagerAdapter(String link) {
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), link);
        mTabLayout.setTabsFromPagerAdapter(mTabPagerAdapter);
        mViewPager.setAdapter(mTabPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initializeAd() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            snack = Snackbar.make(drawer, "Press back again to exit", Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(android.R.color.white));
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snack.show();
//            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fromMoviePage = true;
            setHeaderImage(fromMoviePage);
            spinner.setSelection(0);
            collapsingToolbar.setTitle("Hollywood");
            searchQuery = "";
            tvSeriesText = "";
            setViewPagerAdapter(DEFAULT_LINK);
        } else if (id == R.id.nav_gallery) {
            fromMoviePage = false;
            setHeaderImage(fromMoviePage);
            collapsingToolbar.setTitle("TV Series");
            spinner.setSelected(false);
            spinner.setSelection(0);
            searchQuery = "";
            tvSeriesText = "&tv=";
            setViewPagerAdapter(DEFAULT_LINK);
        }else if (id == R.id.nav_share) {
            try {
                PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
                File srcFile = new File(ai.publicSourceDir);
                Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                share.setType("application/vnd.android.package-archive");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(srcFile));
                startActivity(Intent.createChooser(share, "Share App .."));
            } catch (Exception e) {
                Log.e("ShareApp", e.getMessage());
            }
        } else if (id == R.id.nav_send) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","rahulpandey1501@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback -Hollywood Hub");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        tvSeriesText = "";
        if (fromMoviePage)
            searchQuery = query.replace(" ", "+")+SEARCH_SECTION_MOVIE;
        else
            searchQuery = query.replace(" ", "+")+SEARCH_SECTION_TV;
        collapsingToolbar.setTitle(query);
        getSupportActionBar().setTitle(query);
        setViewPagerAdapter(DEFAULT_LINK);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchView.onActionViewCollapsed();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void setHeaderImage(boolean fromMoviePage) {
        if (fromMoviePage) {
            String header_image = sharedPreferences.getString(MOVIE_HEADER_IMAGE, "false");
            if (!header_image.equalsIgnoreCase("false") && new File(header_image).exists()) {
                Picasso.with(MainActivity.this).load(new File(header_image)).into(header);
            } else {
                Picasso.with(MainActivity.this).load(R.drawable.header_movie).into(header);
            }
        }else{
            String header_image = sharedPreferences.getString(TVSERIES_HEADER_IMAGE, "false");
            if (!header_image.equalsIgnoreCase("false") && new File(header_image).exists()) {
                Picasso.with(MainActivity.this).load(new File(header_image)).into(header);
            } else {
                Picasso.with(MainActivity.this).load(R.drawable.header_tvseries).into(header);
            }
        }
    }

    class TabPagerAdapter extends FragmentStatePagerAdapter{

        String link;
        public TabPagerAdapter(FragmentManager fm, String link) {
            super(fm);
            this.link = link;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return RecyclerViewFragment.newInstance(link+"&keyword="+searchQuery+tvSeriesText);
                case 1:
                    return RecyclerViewFragment.newInstance(link+"release&keyword="+searchQuery+tvSeriesText);
                case 2:
                    return RecyclerViewFragment.newInstance(link+"views&keyword="+searchQuery+tvSeriesText);
                case 3:
                    return RecyclerViewFragment.newInstance(link+"alphabet&keyword="+searchQuery+tvSeriesText);
                default:
                    return RecyclerViewFragment.newInstance(DEFAULT_LINK);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Top";
                case 1:
                    return "Release";
                case 2:
                    return "Popular";
                case 3:
                    return "A-Z";
                default:
                    return "All";
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                Log.d("filepath", imgDecodableString);
                cursor.close();

                if (fromMoviePage)
                    sharedPreferencesEditor.putString(MOVIE_HEADER_IMAGE, imgDecodableString);
                else
                    sharedPreferencesEditor.putString(TVSERIES_HEADER_IMAGE, imgDecodableString);

                sharedPreferencesEditor.commit();

                header.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
            }else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
