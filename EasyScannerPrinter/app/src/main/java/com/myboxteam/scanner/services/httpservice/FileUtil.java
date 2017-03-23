package com.myboxteam.scanner.services.httpservice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for helping parsing file systems.
 */
public abstract class FileUtil {


    public static OutputStream getOutputStream(final File target, Context context) throws Exception {
        return getOutputStream(target, context, 0);
    }

    public static OutputStream getOutputStream(final File target, Context context, long s) throws Exception {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false,context);
                    outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {
                    // Workaround for Kitkat ext SD card
                    return getOutputStream(context,target.getPath());
                }
            }
        } catch (Exception e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from " +  target.getAbsolutePath(), e);
            throw new Exception();
        }
        return outStream;
    }

    public static OutputStream getOutputStream(Context context,String str) {
        OutputStream outputStream = null;
        Uri fileUri = getUriFromFile(str,context);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable th) {
            }
        }
        return outputStream;
    }

    public static Uri getUriFromFile(final String path,Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[] { BaseColumns._ID }, MediaStore.MediaColumns.DATA + " = ?",
                new String[] { path }, MediaStore.MediaColumns.DATE_ADDED + " desc");
        filecursor.moveToFirst();

        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        }
        else {
            int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }



    /**
     * Get a temp file.
     *
     * @param file
     *            The base file for which to create a temp file.
     * @return The temp file.
     */
    public static final File getTempFile(final File file,Context context) {
        File extDir = context.getExternalFilesDir(null);
        File tempFile = new File(extDir, file.getName());
        return tempFile;
    }




    /**
     * Delete all files in a folder.
     *
     * @param folder
     *            the folder
     * @return true if successful.
     */
    public static final boolean deleteFilesInFolder(final File folder,Context context) {
        boolean totalSuccess = true;
        if(folder==null)
            return false;
        if (folder.isDirectory()) {
            for (File child : folder.listFiles()) {
                deleteFilesInFolder(child, context);
            }

            if (!folder.delete())
                totalSuccess = false;
        } else {

            if (!folder.delete())
                totalSuccess = false;
        }
        return totalSuccess;
    }


    /**
     * Check if a file is readable.
     *
     * @param file
     *            The file
     * @return true if the file is reabable.
     */
    public static final boolean isReadable(final File file) {
        if (file == null)
            return false;
        if (!file.exists()) return false;

        boolean result;
        try {
            result = file.canRead();
        } catch (SecurityException e) {
            return false;
        }

        return result;
    }

    /**
     * Check if a file is writable. Detects write issues on external SD card.
     *
     * @param file
     *            The file
     * @return true if the file is writable.
     */
    public static final boolean isWritable(final File file) {
        if(file==null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }


    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if(paths.isEmpty())paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if(paths.isEmpty())paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }
    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file
     *            the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     *         null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file,Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file
     *            The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file,Context c) {
        return getExtSdCardFolder(file,c) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file
     *            The file.
     * @param isDirectory
     *            flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file,context);
        boolean originalDirectory=false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if(!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory=true;
        }
        catch (IOException e) {
            return null;
        }
        catch (Exception f){
            originalDirectory=true;
            //continue
        }
        String as= PreferenceManager.getDefaultSharedPreferences(context).getString("URI",null);

        Uri treeUri =null;
        if(as!=null)treeUri=Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if(originalDirectory)return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                }
                else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    // Utility methods for Kitkat

    /**
     * Copy a resource file into a private target directory, if the target does not yet exist. Required for the Kitkat
     * workaround.
     *
     * @param resource
     *            The resource file.
     * @param folderName
     *            The folder below app folder where the file is copied to.
     * @param targetName
     *            The name of the target file.
     * @return the dummy file.
     * @throws IOException
     */
    private static File copyDummyFile(final int resource, final String folderName, final String targetName,Context context)
            throws IOException {
        File externalFilesDir = context.getExternalFilesDir(folderName);
        if (externalFilesDir == null) {
            return null;
        }
        File targetFile = new File(externalFilesDir, targetName);

        if (!targetFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getResources().openRawResource(resource);
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ex) {
                        // do nothing
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException ex) {
                        // do nothing
                    }
                }
            }
        }
        return targetFile;
    }

    static class MediaFile {

        private static final String NO_MEDIA = ".nomedia";
        private static final String ALBUM_ART_URI = "content://media/external/audio/albumart";
        private static final String[] ALBUM_PROJECTION = { BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM_ID, "media_type" };

        private static File getExternalFilesDir(Context context) {


            try {
                Method method = Context.class.getMethod("getExternalFilesDir", String.class);
                return (File) method.invoke(context, (String) null);
            } catch (SecurityException ex) {
                //   Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (NoSuchMethodException ex) {
                //     Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (IllegalArgumentException ex) {
                // Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (IllegalAccessException ex) {
                //Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            } catch (InvocationTargetException ex) {
                //Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
                return null;
            }
        }


        private final File file;
        private final Context context;
        private final ContentResolver contentResolver;
        Uri filesUri;
        public MediaFile(Context context, File file) {
            this.file = file;
            this.context = context;
            contentResolver = context.getContentResolver();
            filesUri = MediaStore.Files.getContentUri("external");
        }

        /**
         * Deletes the file. Returns true if the file has been successfully deleted or otherwise does not exist. This operation is not
         * recursive.
         */
        public boolean delete()
                throws IOException {

            if (!file.exists()) {
                return true;
            }

            boolean directory = file.isDirectory();
            if (directory) {
                // Verify directory does not contain any files/directories within it.
                String[] files = file.list();
                if (files != null && files.length > 0) {
                    return false;
                }
            }

            String where = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[] { file.getAbsolutePath() };

            // Delete the entry from the media database. This will actually delete media files (images, audio, and video).
            contentResolver.delete(filesUri, where, selectionArgs);

            if (file.exists()) {
                // If the file is not a media file, create a new entry suggesting that this location is an image, even
                // though it is not.
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                // Delete the created entry, such that content provider will delete the file.
                contentResolver.delete(filesUri, where, selectionArgs);
            }

            return !file.exists();
        }

        public File getFile() {
            return file;
        }


        /**
         * Returns an OutputStream to write to the file. The file will be truncated immediately.
         */
        public OutputStream write(long size)
                throws IOException {

            if (NO_MEDIA.equals(file.getName().trim())) {
                throw new IOException("Unable to create .nomedia file via media content provider API.");
            }

            if (file.exists() && file.isDirectory()) {
                throw new IOException("File exists and is a directory.");
            }

            // Delete any existing entry from the media database.
            // This may also delete the file (for media types), but that is irrelevant as it will be truncated momentarily in any case.
            String where = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[] { file.getAbsolutePath() };
            contentResolver.delete(filesUri, where, selectionArgs);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, size);
            Uri uri = contentResolver.insert(filesUri, values);

            if (uri == null) {
                // Should not occur.
                throw new IOException("Internal error.");
            }

            return contentResolver.openOutputStream(uri);
        }
    }
}
