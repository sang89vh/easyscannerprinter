package com.myboxteam.scanner.application;

import android.app.Activity;
import android.content.Context;


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.microsoft.onedrivesdk.picker.*;
import com.microsoft.onedrivesdk.saver.ISaver;
import com.microsoft.onedrivesdk.saver.Saver;

import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * https://dev.onedrive.com/sdk/android-picker/android-picker-overview.htm
 * https://developers.google.com/drive/android/intro
 * https://www.dropbox.com/developers/apps/info/5w95yo3ogfplzld
 * Created by Admin on 3/9/2017.
 */

public class MBApplication extends android.app.Application {
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private IPicker mPicker;
    private ISaver mSaver;
    private Context mContext;
    private final String ONEDRIVE_APP_ID = "fa412f6c-9b0f-4351-8275-ec241c393d95";

    private final String DROPBOX_ACCESS_TOKEN = "qOjcdM27HSAAAAAAAAAAzs9-883em2LazVRHSoCqQw_P8iF9ZHCTD58WseN5D8wW";

    /**
     * The onClickListener that will start the OneDrive picker
     * Link Types
     * <p>
     * The open picker can be configured to return a URL for the selected file in one of these formats: * LinkType.DownloadLink
     * - A URL is returned that provides access for 1 hour directly to the contents of the file. You can use this URL to download the file into your application. * LinkType.WebViewLink - A sharing link that provides a web preview of the file is created. The link is valid until the user deletes the shared link through OneDrive. Sharing links are not available for OneDrive for Business files.
     */
    private final OnClickListener mStartPickingListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            mPicker.startPicking((Activity) v.getContext(), LinkType.WebViewLink);
        }
    };


    // The onClickListener that will start the OneDrive picker
    private final OnClickListener mStartSavingListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            // create example file to save to OneDrive
            final String filename = "file.txt";
            final File f = new File(mContext.getFilesDir(), filename);

            // create and launch the saver
            mSaver = Saver.createSaver(ONEDRIVE_APP_ID);
            mSaver.startSaving((Activity) v.getContext(), filename, Uri.fromFile(f));

            // Create Dropbox client
            DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
            DbxClientV2 client = new DbxClientV2(config, DROPBOX_ACCESS_TOKEN);

            // Get current account info
            FullAccount account = null;
            try {
                account = client.users().getCurrentAccount();

                System.out.println(account.getName().getDisplayName());

                // Get files and folder metadata from Dropbox root directory
                ListFolderResult result = client.files().listFolder("");
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        System.out.println(metadata.getPathLower());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());
                }

                // Upload "test.txt" to Dropbox
                try (InputStream in = new FileInputStream("test.txt")) {
                    FileMetadata metadata = client.files().uploadBuilder("/test.txt")
                            .uploadAndFinish(in);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mPicker = Picker.createPicker(ONEDRIVE_APP_ID);
        // create and launch the saver
        mSaver = Saver.createSaver(ONEDRIVE_APP_ID);


    }

    public IPicker getPicker() {
        return mPicker;
    }

    public void setPicker(IPicker mPicker) {
        this.mPicker = mPicker;
    }

    public ISaver getmSaver() {
        return mSaver;
    }

    public void setmSaver(ISaver mSaver) {
        this.mSaver = mSaver;
    }
}