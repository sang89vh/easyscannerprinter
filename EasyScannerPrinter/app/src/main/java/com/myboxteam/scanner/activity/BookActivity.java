package com.myboxteam.scanner.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.fragment.ScanFragment;

/**
 * add news image, export to pdf, print, edit image
 */
public class BookActivity extends AppCompatActivity {
    private String bookId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        Bundle bundle = getIntent().getExtras();
        //if book id is null=> start from main activity else start from book for adding new image
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        String imgPath = bundle.getString(ScanFragment.RESULT_IMAGE_PATH);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        //imageView.setImageBitmap(bitmap);
    }


}
