package com.myboxteam.scanner.utils;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
}
