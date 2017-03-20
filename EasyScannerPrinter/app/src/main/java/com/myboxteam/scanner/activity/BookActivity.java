package com.myboxteam.scanner.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.myboxteam.scanner.R;
import com.myboxteam.scanner.adapter.ItemAdapter;
import com.myboxteam.scanner.application.MBApplication;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.myboxteam.scanner.utils.Utils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * add news image, export to pdf, print, edit image
 */
public class BookActivity extends AppCompatActivity {
    private static final String TAG = "BookActivity";
    private String bookId;
    private ParseObject book;
    private String imgPath;
    private MBApplication mApp;
    private Context mContext;

    private ArrayList<Pair<Long, String>> mItemArray = new ArrayList<Pair<Long, String>>();;
    private DragListView mDragListView;
    private ItemAdapter mListAdapter;
    private ListSwipeHelper mSwipeHelper;
    private SaveCallback saveCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            if (e == null) {
                bookId = book.getObjectId();

                mItemArray.add(new Pair(1L, imgPath));
                mListAdapter.notifyDataSetChanged();
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
                    String path = list.get(i);
                    if(path != null && path != "") {
                        mItemArray.add(new Pair(new Long(i + 1), path));
                        //mListAdapter.addItem(mItemArray.size(),new Pair(new Long(i + 1), path));
                    }
                }
                mListAdapter.notifyDataSetChanged();

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
        //bookId = null;
        imgPath = bundle.getString(ScanFragment.RESULT_IMAGE_PATH);
        //imgPath = "/storage/emulated/0/DCIM/easycamera/03.jpg";

        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                Toast.makeText(mContext, "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    Toast.makeText(mContext, "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Pair<Long, String> adapterItem = (Pair<Long, String>) item.getTag();
                    int pos = mDragListView.getAdapter().getPositionForItem(adapterItem);
                    mDragListView.getAdapter().removeItem(pos);
                }
            }
        });


        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Pair<Long, String> adapterItem = (Pair<Long, String>) item.getTag();
                    int pos = mDragListView.getAdapter().getPositionForItem(adapterItem);
                    mDragListView.getAdapter().removeItem(pos);
                }
            }
        });


        setupListRecyclerView();

        if (null == bookId) {
            book = DatabaseUtils.createBook(imgPath, saveCallback);

        } else {
            DatabaseUtils.getBookById(bookId, getCallback);

        }

        final FloatingActionButton actionPrint = (FloatingActionButton) findViewById(R.id.action_print);
        actionPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"action print clicked");

                Intent intent = new Intent(mContext,PrintActivity.class);
                intent.putExtra(ScanActivity.BOOK_ID, bookId);
                startActivity(intent);
            }
        });

        final View actionAddMore = findViewById(R.id.action_add_more);
        actionAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"action add more clicked");
            }
        });


        final View actionShare = findViewById(R.id.action_share);
        actionShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG," action Share clicked");
            }
        });

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);




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
        menu.findItem(R.id.action_disable_drag).setVisible(mDragListView.isDragEnabled());
        menu.findItem(R.id.action_enable_drag).setVisible(!mDragListView.isDragEnabled());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_disable_drag:
                mDragListView.setDragEnabled(false);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_enable_drag:
                mDragListView.setDragEnabled(true);
                supportInvalidateOptionsMenu();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(mContext));
        mListAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.image, true, mApp.getBitmapOptions(), Utils.getWidthScreen(this),Utils.getHeightScreen(this));
        mDragListView.setAdapter(mListAdapter, false);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MyDragItem(mContext, R.layout.list_item));
    }


    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
//            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
//            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            dragView.findViewById(R.id.item_layout).setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }

    }
}
