package com.myboxteam.scanner.activity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.adapter.ItemAdapter;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.utils.DatabaseUtils;
import com.myboxteam.scanner.utils.MySwipeRefreshLayout;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.util.ArrayList;

/**
 * add news image, export to pdf, print, edit image
 */
public class BookActivity extends AppCompatActivity {
    private Context mContext;
    private String bookId;
    private ParseObject book;
    private String imgPath;
    private ArrayList<Pair<Long, String>> mItemArray;
    private DragListView mDragListView;
    private ListSwipeHelper mSwipeHelper;
    private MySwipeRefreshLayout mRefreshLayout;

    private GetCallback getCallback = new GetCallback<ParseObject>() {
        @Override
        public void done(ParseObject object, ParseException e) {
            if (e == null) {
                // object will be your game score
                book = object;
                DatabaseUtils.addImageToBook(book, imgPath);
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
        Bundle bundle = getIntent().getExtras();
        //if book id is null=> start from main activity else start from book for adding new image
        bookId = bundle.getString(ScanActivity.BOOK_ID);

        imgPath = bundle.getString(ScanFragment.RESULT_IMAGE_PATH);

        if (null != bookId) {
            book = DatabaseUtils.createBook(imgPath);
        } else {
            DatabaseUtils.getBookById(bookId, getCallback);
        }

        mRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                mRefreshLayout.setEnabled(false);
                Toast.makeText(mContext, "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                mRefreshLayout.setEnabled(true);
                if (fromPosition != toPosition) {
                    Toast.makeText(mContext, "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                mRefreshLayout.setEnabled(false);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                mRefreshLayout.setEnabled(true);

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Pair<Long, String> adapterItem = (Pair<Long, String>) item.getTag();
                    int pos = mDragListView.getAdapter().getPositionForItem(adapterItem);
                    mDragListView.getAdapter().removeItem(pos);
                }
            }
        });

        mItemArray = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            mItemArray.add(new Pair<>((long) i, "Item " + i));
        }

        mRefreshLayout.setScrollingView(mDragListView.getRecyclerView());
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.app_color));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                mRefreshLayout.setEnabled(false);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                mRefreshLayout.setEnabled(true);

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Pair<Long, String> adapterItem = (Pair<Long, String>) item.getTag();
                    int pos = mDragListView.getAdapter().getPositionForItem(adapterItem);
                    mDragListView.getAdapter().removeItem(pos);
                }
            }
        });

        setupListRecyclerView();

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        //imageView.setImageBitmap(bitmap);
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
            case R.id.action_disable_drag:
                mDragListView.setDragEnabled(false);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_enable_drag:
                mDragListView.setDragEnabled(true);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_list:
                setupListRecyclerView();
                return true;
            case R.id.action_grid_vertical:
                setupGridVerticalRecyclerView();
                return true;
            case R.id.action_grid_horizontal:
                setupGridHorizontalRecyclerView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(mContext));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.image, false);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MyDragItem(mContext, R.layout.list_item));
    }

    private void setupGridVerticalRecyclerView() {
        mDragListView.setLayoutManager(new GridLayoutManager(mContext, 4));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);

    }

    private void setupGridHorizontalRecyclerView() {
        mDragListView.setLayoutManager(new GridLayoutManager(mContext, 4, LinearLayoutManager.HORIZONTAL, false));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);
    }

    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            dragView.findViewById(R.id.item_layout).setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }
    }
}
