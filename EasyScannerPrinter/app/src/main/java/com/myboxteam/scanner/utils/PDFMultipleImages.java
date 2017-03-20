package com.myboxteam.scanner.utils;

/**
 * Created by Admin on 3/10/2017.
 */

import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFMultipleImages {

    public static File createPdf(List<String> images, File file) throws IOException, DocumentException {
        float margin = 30;

        Image img = Image.getInstance(images.get(0));
        Document document = new Document(img);
        document.setMargins(1,1,1,1);
        PdfWriter.getInstance(document, new FileOutputStream(file.getAbsolutePath()));
        document.open();
        for (String image : images) {
            img = Image.getInstance(image);
            document.setPageSize(img);

            document.newPage();
            img.setAbsolutePosition(0, 0);
            document.add(img);
        }
        document.close();

        Log.d("PDFMultipleImages","Finished create pdf:"+file.getAbsolutePath());
        return file;
    }


}
