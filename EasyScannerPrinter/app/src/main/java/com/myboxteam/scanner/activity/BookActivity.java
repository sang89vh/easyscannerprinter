package com.myboxteam.scanner.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.application.MBApplication;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import java.util.List;

/**
 * add news image, export to pdf, print, edit image
 */
public class BookActivity extends AppCompatActivity {
    private String bookId;
    private ParseObject book;
    private String imgPath;
    private MBApplication mApp;
    private Context mContext;


    private SaveCallback saveCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            if (e == null) {
                bookId = book.getObjectId();

                setupListRecyclerView();
            } else {

            }
        }
    };
    private GetCallback getCallback = new GetCallback<ParseObject>() {

        @Override
        public void done(ParseObject object, ParseException e) {
            if (e == null) {
                // object will be your game score
                book = object;

                bookId = object.getObjectId();


                List<String> list = book.getList("imgPaths");
                for (int i = 0; i < list.size(); i++) {

                }

                setupListRecyclerView();


            } else {
                // something went wrong
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        mContext = this;

        mApp = (MBApplication) getApplication();
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Bundle bundle = getIntent().getExtras();
        //if book id is null=> start from main activity else start from book for adding new image
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        imgPath = bundle.getString(ScanFragment.RESULT_IMAGE_PATH);


        if (null == bookId) {
            book = DatabaseUtils.createBook(imgPath, saveCallback);

        } else {
            DatabaseUtils.getBookById(bookId, getCallback);

        }


    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(Activity.RESULT_OK, intent);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
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

    private void setupListRecyclerView() {

    }

}
