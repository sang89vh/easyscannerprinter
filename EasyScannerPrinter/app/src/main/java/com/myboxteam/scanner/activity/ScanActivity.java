package com.myboxteam.scanner.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.filters.MixFilter;
import com.myboxteam.scanner.filters.ThumbnailCallback;
import com.myboxteam.scanner.filters.ThumbnailItem;
import com.myboxteam.scanner.filters.ThumbnailsAdapter;
import com.myboxteam.scanner.filters.ThumbnailsManager;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;

import java.util.List;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity  implements ThumbnailCallback {
    public static final String BOOK_ID = "bookId";
    private String bookId;
    public static final String EXTRA_BRAND_IMG_RES = "title_img_res";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_LANGUAGE = "language";
    public static final String EXTRA_ACTION_BAR_COLOR = "ab_color";
    private ScanFragment f;
    Toolbar toolbar;

    private Activity activity;
    private RecyclerView thumbListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Bundle bundle = getIntent().getExtras();
        //if book id is null=> start from main activity else start from book for adding new image
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        activity = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int titleImgRes = bundle.getInt(EXTRA_BRAND_IMG_RES);
        int abColor = bundle.getInt(EXTRA_ACTION_BAR_COLOR);
        String title = bundle.getString(EXTRA_TITLE);
        String locale = bundle.getString(EXTRA_LANGUAGE);

        if (locale != null) {
            Locale l = new Locale(locale);
            Locale.setDefault(l);
            Configuration config = new Configuration();
            config.locale = l;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        if (title != null) getSupportActionBar().setTitle(title);
        if (titleImgRes != 0) getSupportActionBar().setLogo(titleImgRes);

        if (abColor != 0) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(abColor)));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            if (getIntent().getExtras() != null) {
                args.putAll(getIntent().getExtras());
            }

            FragmentManager fragMan = getSupportFragmentManager();
            f = new ScanFragment();
            f.setArguments(args);
            FragmentTransaction fragTransaction = fragMan.beginTransaction();
            fragTransaction.replace(R.id.contaner, f, "scan_frag").commit();
        }
    }

    @Override
    public void onBackPressed() {
        ScanFragment scanFragment = (ScanFragment) getSupportFragmentManager().findFragmentByTag("scan_frag");
        if (scanFragment != null) {
            boolean exit = scanFragment.onBackPressed();
            if (exit) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
    public void initUIWidgets() {
        thumbListView = (RecyclerView) findViewById(R.id.thumbnails);

            initHorizontalList();
    }

    private void initHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }

    private void bindDataToAdapter() {
        final Context context = this.getApplication();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage = Bitmap.createScaledBitmap(f.getDocumentBitmap(), 640, 640, false);
                ThumbnailItem t1 = new ThumbnailItem();
                ThumbnailItem t2 = new ThumbnailItem();
                ThumbnailItem t4 = new ThumbnailItem();
                ThumbnailItem t5 = new ThumbnailItem();
                ThumbnailItem t6 = new ThumbnailItem();
                ThumbnailItem t7 = new ThumbnailItem();
                ThumbnailItem t8 = new ThumbnailItem();
                ThumbnailItem t9 = new ThumbnailItem();
                ThumbnailItem t10 = new ThumbnailItem();

                t1.image = thumbImage;
                t2.image = thumbImage;
                t4.image = thumbImage;
                t5.image = thumbImage;
                t6.image = thumbImage;
                t7.image = thumbImage;
                t8.image = thumbImage;
                t9.image = thumbImage;
                t10.image = thumbImage;

                ThumbnailsManager.clearThumbs();
                ThumbnailsManager.addThumb(t1); // Original Image

                t7.filter = new MixFilter(ScanFragment.MODE_BLACK_AND_WHITE);
                ThumbnailsManager.addThumb(t7);

                t8.filter = new MixFilter(ScanFragment.MODE_MAGIC);
                ThumbnailsManager.addThumb(t8);

                t2.filter = SampleFilters.getStarLitFilter();
                ThumbnailsManager.addThumb(t2);
                
                t4.filter = SampleFilters.getAweStruckVibeFilter();
                ThumbnailsManager.addThumb(t4);

                t5.filter = SampleFilters.getLimeStutterFilter();
                ThumbnailsManager.addThumb(t5);

                t6.filter = SampleFilters.getNightWhisperFilter();
                ThumbnailsManager.addThumb(t6);

                t9.filter = new MixFilter(ScanFragment.MODE_OLD);
                ThumbnailsManager.addThumb(t9);

                t10.filter = new MixFilter(ScanFragment.MODE_BRIGHTNESS);
                ThumbnailsManager.addThumb(t10);


                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) activity);
                thumbListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @Override
    public void onThumbnailClick(Filter filter) {
        Bitmap bm = f.getDocumentBitmap();
        if(bm!= null) {
            f.setDocumentColoredBitmap(filter.processFilter(bm));
            f.updateViewsWithNewBitmap();
        }
    }
}