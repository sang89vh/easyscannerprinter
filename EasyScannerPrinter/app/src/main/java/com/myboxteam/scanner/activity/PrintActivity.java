package com.myboxteam.scanner.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FileUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.codec.Base64;
import com.myboxteam.scanner.R;
import com.myboxteam.scanner.adapter.ViewPagerAdapter;
import com.myboxteam.scanner.fragment.PrintHelpFragment;
import com.myboxteam.scanner.fragment.PrintLayoutFragment;
import com.myboxteam.scanner.fragment.PrintPdfFragment;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.myboxteam.scanner.utils.PDFMultipleImages;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public class PrintActivity extends AppCompatActivity {
    private String TAG = "PrintActivity";
    private String bookId;
    private String pdfPath;
    private ParseObject book;
    private File mPdfFile;
    private Toolbar toolbar;
    private Context mContext;
    PrintLayoutFragment printLayoutFragment;
    PrintHelpFragment printHelpFragment;
    PrintPdfFragment printPdfFragment;

    private GetCallback getCallback = new GetCallback<ParseObject>() {

        @Override
        public void done(ParseObject object, ParseException e) {
            if (e == null) {
                // object will be your game score
                book = object;
                toolbar.setTitle(book.getString("title"));
                bookId = object.getObjectId();
                pdfPath = book.getString("pdfPath");

                if (pdfPath == null) {

                    List<String> list = book.getList("imgPaths");
                    try {
                        mPdfFile = createPdfFile(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                        PDFMultipleImages.createPdf(list, mPdfFile);
                        book.put("pdfPath", pdfPath);
                        book.pinInBackground();
                    } catch (IOException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    } catch (DocumentException e1) {
                        Log.e(TAG, e1.getMessage(), e1);
                    }


                } else {

                    mPdfFile = new File(pdfPath);
                }

                Log.d(TAG, pdfPath);
                printPdfFragment.pdfView.fromFile(mPdfFile)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        //.onPageChange(PrintActivity.this)
                        .enableAnnotationRendering(true)
                        //      .onLoad(this)
                        .scrollHandle(new DefaultScrollHandle(PrintActivity.this))
                        .load();

                printLayoutFragment.showFilePicked(mPdfFile);

            } else {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    };

    private File createPdfFile(String fileName) {
        File storageDir = getExternalFilesDir("pdfs");
        if (storageDir == null) {
            throw new RuntimeException("Not able to get to External storage");
        }
        File image = new File(storageDir, fileName + ".pdf");
        pdfPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        mContext = this;
        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        ViewPager viewPager = (ViewPager) findViewById(R.id.sample_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sample_tablayout);
        tabLayout.setupWithViewPager(viewPager);

        //print
        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        final com.getbase.floatingactionbutton.FloatingActionButton actionPrint = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_print);
        actionPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "action print clicked");
                printLayoutFragment.continueButtonClicked(view);

                menuMultipleActions.collapse();
            }
        });

        final View actionCloudPrint = findViewById(R.id.action_cloud_print);
        actionCloudPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cloud print clicked");

                Intent printIntent = new Intent(PrintActivity.this, PrintDialogActivity.class);
                Uri furi = Uri.fromFile(mPdfFile);
                printIntent.setDataAndType(furi, printLayoutFragment.getMimeType(furi));
                printIntent.putExtra("title", "easy scanner");
                startActivity(printIntent);

                menuMultipleActions.collapse();
            }
        });

        final View actionSavePrint = findViewById(R.id.action_save_pdf);
        actionSavePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " actionSavePrint clicked");
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File dest = new File(downloadDir, book.getString("title") + ".pdf");

                try {
                    FileUtils.copy(new FileInputStream(pdfPath), dest);

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);

                    intent.setDataAndType(Uri.fromFile(dest), "application/pdf");

                    PendingIntent pIntent = PendingIntent.getActivity(PrintActivity.this, 0, intent, 0);
                    android.support.v4.app.NotificationCompat.Builder mBilder =
                    new NotificationCompat.Builder(PrintActivity.this)
                            .setSmallIcon(R.drawable.ic_download)
                            .setContentIntent(pIntent)
                            .setContentTitle("Easy scanner")
                            .setContentText("Save PDF finished!");
                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, mBilder.build());

                    Toast.makeText(mContext,"Save PDF finished!",Toast.LENGTH_LONG).show();
                    menuMultipleActions.collapse();

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });




        DatabaseUtils.getBookById(bookId, getCallback);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        printLayoutFragment = new PrintLayoutFragment();
        printHelpFragment = new PrintHelpFragment();
        printPdfFragment = new PrintPdfFragment();
        adapter.addFrag(printPdfFragment, getResources().getString(R.string.printing_pdf_preview));
        adapter.addFrag(printLayoutFragment, getResources().getString(R.string.print_settings));
        adapter.addFrag(printHelpFragment, getResources().getString(R.string.printing_help));


        viewPager.setAdapter(adapter);
    }
}
