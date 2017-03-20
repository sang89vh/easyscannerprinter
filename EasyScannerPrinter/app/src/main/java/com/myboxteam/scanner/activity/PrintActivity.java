package com.myboxteam.scanner.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.itextpdf.text.DocumentException;
import com.myboxteam.scanner.R;
import com.myboxteam.scanner.adapter.ViewPagerAdapter;
import com.myboxteam.scanner.fragment.PrintHelpFragment;
import com.myboxteam.scanner.fragment.PrintLayoutFragment;
import com.myboxteam.scanner.fragment.PrintPdfFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.myboxteam.scanner.utils.PDFMultipleImages;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class PrintActivity extends AppCompatActivity {
    private String TAG = "PrintActivity";
    private String bookId;
    private String pdfPath;
    private ParseObject book;
    private File mPdfFile;


    PrintLayoutFragment printLayoutFragment;
    PrintHelpFragment printHelpFragment;
    PrintPdfFragment printPdfFragment;

    private GetCallback getCallback = new GetCallback<ParseObject>() {

        @Override
        public void done(ParseObject object, ParseException e) {
            if (e == null) {
                // object will be your game score
                book = object;

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

        Bundle bundle = getIntent().getExtras();
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        DatabaseUtils.getBookById(bookId, getCallback);


        ViewPager viewPager = (ViewPager) findViewById(R.id.sample_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sample_tablayout);
        tabLayout.setupWithViewPager(viewPager);

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
