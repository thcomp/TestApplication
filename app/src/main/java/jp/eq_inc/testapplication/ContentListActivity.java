package jp.eq_inc.testapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.manager.ContentDataManager;
import jp.eq_inc.testapplication.manager.DataLoadListener;
import jp.eq_inc.testapplication.task.BitmapDecoderTask;

public class ContentListActivity extends AppCompatActivity {
    private int mFirstVisibleItemIndex = 0;
    private ContentListAdapter mContentListAdapter;
    private HashMap<Bitmap, Object> mDecodedBitmapMap = new HashMap<Bitmap, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        ListView lvContentListView = (ListView) findViewById(R.id.lvContentList);
        lvContentListView.setOnScrollListener(mContentListScrollListener);
        lvContentListView.setAdapter(mContentListAdapter = new ContentListAdapter(this, mDecodedBitmapMap));
        lvContentListView.setOnItemClickListener(mItemClickListener);

        if(savedInstanceState != null){
            mFirstVisibleItemIndex = savedInstanceState.getInt("mFirstVisibleItemIndex", 0);
        }
        lvContentListView.setSelection(mFirstVisibleItemIndex);
        lvContentListView.setDividerHeight(0);  // ListViewで標準で表示される境界線を除去

        ContentDataManager contentManager = ContentDataManager.getInstance(this);
        contentManager.registerPresetDataLoadListener(mPresetDataLoadListener);
        contentManager.loadPresetContents();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("mFirstVisibleItemIndex", mFirstVisibleItemIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ContentDataManager contentManager = ContentDataManager.getInstance(this);
        contentManager.unregisterPresetDataLoadListener(mPresetDataLoadListener);

        Set<Bitmap> bitmapSet = mDecodedBitmapMap.keySet();
        Iterator<Bitmap> iterator = bitmapSet.iterator();
        while (iterator.hasNext()) {
            iterator.next().recycle();
        }
        mDecodedBitmapMap.clear();
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ContentCategoryList.ContentCategoryData item = (ContentCategoryList.ContentCategoryData) parent.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.setClass(ContentListActivity.this, ContentActivity.class);
            intent.putExtra(Common.IntentParcelableExtraContentCategoryData, item);
            startActivity(intent);
        }
    };

    private AbsListView.OnScrollListener mContentListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // no work
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mFirstVisibleItemIndex = firstVisibleItem;
        }
    };

    private DataLoadListener mPresetDataLoadListener = new DataLoadListener() {
        @Override
        public void onStartLoading() {
            // no work
        }

        @Override
        public void onEndLoading(boolean result) {
            int errorMessageResId = 0;

            if(result){
                ContentDataManager contentManager = ContentDataManager.getInstance(ContentListActivity.this);
                ContentCategoryList contentCategoryList = contentManager.getPresetContentList();
                if(contentCategoryList != null){
                    mContentListAdapter.setContentList(contentCategoryList);
                }else{
                    errorMessageResId = R.string.fail_to_load_content;
                }
            }else{
                errorMessageResId = R.string.fail_to_load_content;
            }

            if(errorMessageResId != 0){
                Toast.makeText(ContentListActivity.this, errorMessageResId, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private static class ContentListAdapter extends BaseAdapter{
        private Context mContext;
        private HashMap<Bitmap, Object> mDecodedBitmapMap;
        private ContentCategoryList mContentCategoryList;

        public ContentListAdapter(Context context, HashMap<Bitmap, Object> decodedBitmapMap){
            mContext = context;
            mDecodedBitmapMap = decodedBitmapMap;
        }

        public void setContentList(ContentCategoryList contentCategoryList){
            mContentCategoryList = contentCategoryList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mContentCategoryList != null ? mContentCategoryList.getContentCategoryCount() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mContentCategoryList != null ? mContentCategoryList.getContentCategoryData(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = convertView;
            ContentCategoryList.ContentCategoryData item = (ContentCategoryList.ContentCategoryData) getItem(position);

            if(ret == null){
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ret = inflater.inflate(R.layout.item_content_category, parent, false);
            }else{
                // release old resource
                ImageView iconImageView = (ImageView) ret.findViewById(R.id.ivIcon);
                Bitmap oldIconBitmap = (Bitmap) iconImageView.getTag(Common.ViewTagIconBitmap);
                if(oldIconBitmap != null){
                    mDecodedBitmapMap.remove(oldIconBitmap);
                    oldIconBitmap.recycle();
                    iconImageView.setTag(Common.ViewTagIconBitmap, null);
                }
            }

            final ImageView fTargetImageView = (ImageView) ret.findViewById(R.id.ivIcon);
            BitmapDecoderTask task = new BitmapDecoderTask(mContext, new BitmapDecoderTask.DecoderTaskListener() {
                @Override
                public void onPreExecute(BitmapDecoderTask task) {
                    // no work
                }

                @Override
                public void onPostExecute(BitmapDecoderTask task, BitmapDecoderTask.BitmapResult[] decodedBitmaps) {
                    if(decodedBitmaps != null && decodedBitmaps.length > 0){
                        BitmapDecoderTask.ContentCategory param = (BitmapDecoderTask.ContentCategory) fTargetImageView.getTag(Common.ViewTagDecodeParam);
                        if(param.equals(decodedBitmaps[0].param)){
                            // まだリサイクルされていないViewなので、デコードが完了した画像を設定
                            mDecodedBitmapMap.put(decodedBitmaps[0].decodedBitmap, new Object());
                            fTargetImageView.setTag(Common.ViewTagIconBitmap, decodedBitmaps[0].decodedBitmap);
                            fTargetImageView.setImageBitmap(decodedBitmaps[0].decodedBitmap);
                        }else{
                            // Viewがすでにリサイクルされて、別の画像を割りあてるようになっているので、設定せずにそのまま解放
                            decodedBitmaps[0].decodedBitmap.recycle();
                        }
                    }
                }
            });
            BitmapDecoderTask.ContentCategory param = new BitmapDecoderTask.ContentCategory();
            param.categoryData = item;
            task.execute(param);
            fTargetImageView.setTag(Common.ViewTagDecodeParam, param);

            ((TextView)ret.findViewById(R.id.tvTitle)).setText(item.getTitle(mContext));

            return ret;
        }
    }
}
