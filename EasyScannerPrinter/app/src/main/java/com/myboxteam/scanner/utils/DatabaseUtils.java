package com.myboxteam.scanner.utils;

import android.graphics.Bitmap;

import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Admin on 3/13/2017.
 */

public  class DatabaseUtils {
    public static String BOOK_COLLECTION = "SCANNER_BOOK";
    public static void getBookById(String bookId,GetCallback<ParseObject> callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(BOOK_COLLECTION);
        query.getInBackground("bookId",callback);
    }

    public static void deleteBookById() {

    }

    public static ParseObject updateBook(ParseObject book) {
        return null;
    }

    public static ParseObject createBook(String imgPath) {
        ParseObject book = new ParseObject(BOOK_COLLECTION);
        book.addAllUnique("imgPaths", Arrays.asList(imgPath));
        book.pinInBackground();

        return book;

    }

    public static void addImageToBook(ParseObject book, String imgPath) {
        if(imgPath != null) {
            List<String> imgPaths = book.getList("imgPaths");
            imgPaths.add(imgPath);
            book.addAllUnique("imgPaths", imgPaths);
            book.pinInBackground();
        }
    }

    public static boolean saveBitmapToFile(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static boolean saveBitmapToFile(String path, Bitmap bitmap) {
        File file = new File(path);
        return  saveBitmapToFile(file,bitmap);
    }
}
