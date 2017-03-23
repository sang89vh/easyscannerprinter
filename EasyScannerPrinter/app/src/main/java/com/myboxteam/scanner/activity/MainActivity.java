package com.myboxteam.scanner.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.adapter.EndlessRecyclerViewScrollListener;
import com.myboxteam.scanner.adapter.GridSpacingItemDecoration;
import com.myboxteam.scanner.adapter.BookRecyclerAdapter;
import com.myboxteam.scanner.application.MBApplication;
import com.myboxteam.scanner.dto.Video;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.myboxteam.scanner.utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MBApplication mApp;
    private Context mContext;
    protected BookRecyclerAdapter mVideoAdapter;
    protected RecyclerView mListVideo;
    public static final int REQUEST_CODE_SCAN = 47;
    private String bookId;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.content_main)
    LinearLayout contentMain;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    private static String WRITE_PERMISSIONS[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final static int REQUEST_WRITEPERMISSION_CODE = 125;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mContext = this;

        mApp = (MBApplication) getApplication();
        /* List audio adapter */
        mVideoAdapter = new BookRecyclerAdapter(mContext, new BookRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Video item) {

                Intent intent = new Intent(mContext,BookActivity.class);
                intent.putExtra(ScanActivity.BOOK_ID, item.getObjectId());
                startActivity(intent);
            }
        }, mApp.getBitmapOptions(), Utils.getWidthScreen(this),Utils.getHeightScreen(this));

        /* List audio from server */
        mListVideo = (RecyclerView) findViewById(R.id.list_video);

        int spanCount = 2; // 3 columns
        int spacing = 20; // 50px
        boolean includeEdge = true;
        mListVideo.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, spanCount);
        mListVideo.setLayoutManager(layoutManager);

        mListVideo.setAdapter(mVideoAdapter);
        mListVideo.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                updateData();
            }
        });

        updateData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_wifi_transfer:
                //go to wifi transfer setting

                Intent intent2 = new Intent(mContext,HTTPServerActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(mContext, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_WRITEPERMISSION_CODE) {
            if (PermissionUtils.verifyAllPermissions(grantResults)) {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                selectImage_dialog(this);
            } else {
                Toast.makeText(this, "Need Permission to add this type of message", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        // NOTE: Perform action that requires the permission. If this is run by PermissionsDispatcher, the permission will have been granted
        openScanActivity();
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_camera_rationale, request);
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
    }


    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @OnClick(R.id.fab)
    public void onClick() {

        MainActivityPermissionsDispatcher.showCameraWithCheck(this);


    }


    void openScanActivity() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        intent.putExtra(ScanActivity.EXTRA_BRAND_IMG_RES, R.drawable.ic_crop_white_24dp); // Set image for title icon - optional
        intent.putExtra(ScanActivity.EXTRA_TITLE, "Crop Document"); // Set title in action Bar - optional
        intent.putExtra(ScanActivity.EXTRA_ACTION_BAR_COLOR, R.color.colorPrimary); // Set title color - optional
        intent.putExtra(ScanActivity.EXTRA_LANGUAGE, "en"); // Set language - optional
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }


    protected void updateData() {

        //Log.i(TAG, "run update data");

        if (mVideoAdapter.isLoaded()) {

            mVideoAdapter.setIsLoaded(false);

            ParseQuery<ParseObject> query = ParseQuery.getQuery(DatabaseUtils.BOOK_COLLECTION);
            //query.whereEqualTo(AppConfig.AUDIO_ISDELETE, 0);
            //query.orderByDescending(AppConfig.AUDIO_GRAVITY);
            query.fromLocalDatastore();
            query.setLimit(100);
            query.setSkip(mVideoAdapter.getPage() * 100);

            //Log.i(TAG, "skip:" + mVideoAdapter.getPage() * 10);

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> videos, ParseException e) {
                    if (e == null) {
                        //Log.d(TAG, "Retrieved " + videos.size() + " scores");
                        //int i = 1;
                        for (ParseObject parseObject : videos) {
                            // i++;
                            try {
                                Video video = new Video();
                                video.setObjectId(parseObject.getObjectId());
                                video.setTitle(parseObject.getString("title"));
                                String path = (String) parseObject.getList("imgPaths").get(0);
                                Log.d(TAG,path);
                                video.setBanner(path);
                                mVideoAdapter.addVideo(video);
                            }catch (ClassCastException cex){

                            }



                        }

                        if (0 != videos.size()) {
                            mVideoAdapter.notifyDataSetChanged();

                            mVideoAdapter.nextPage();

                            mVideoAdapter.setIsLoaded(true);
                        }

                    } else {
                        Log.e(TAG, "Error: " + e.getMessage(), e);
                    }
                }
            });
        }


    }
}