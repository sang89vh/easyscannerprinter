package com.myboxteam.scanner.utils;

/**
 * Created by Admin on 3/10/2017.
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class PDFMultipleImages {

    public static File createPdf(String[] images, String dest) throws IOException, DocumentException {
        File file = new File(dest);
        file.getParentFile().mkdirs();
        Image img = Image.getInstance(images[0]);
        Document document = new Document(img);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        for (String image : images) {
            img = Image.getInstance(image);
            document.setPageSize(img);
            document.newPage();
            img.setAbsolutePosition(0, 0);
            document.add(img);
        }
        document.close();

        return file;
    }
}
