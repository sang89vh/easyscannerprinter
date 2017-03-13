package com.myboxteam.scanner.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * add news image, export to pdf, print, edit image
 */
public class BookActivity extends AppCompatActivity {
    private String bookId;
    private ParseObject book;
    private String imgPath;

    private GetCallback getCallback=  new GetCallback<ParseObject>() {
        @Override
        public void done(ParseObject object, ParseException e) {
            if (e == null) {
                // object will be your game score
                book = object;
                DatabaseUtils.addImageToBook(book,imgPath);
            } else {
                // something went wrong
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        Bundle bundle = getIntent().getExtras();
        //if book id is null=> start from main activity else start from book for adding new image
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        imgPath = bundle.getString(ScanFragment.RESULT_IMAGE_PATH);

        if(null != bookId){
            book = DatabaseUtils.createBook(imgPath);
        }else{
            DatabaseUtils.getBookById(bookId,getCallback);
        }

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        //imageView.setImageBitmap(bitmap);
    }


}
