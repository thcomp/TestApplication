package jp.eq_inc.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import jp.co.thcomp.util.PreferenceUtil;
import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.task.BitmapDecoderTask;

public class ContentActivity extends AppCompatActivity {

    private static final int RequestCodeShowContent = "RequestCodeShowContent".hashCode() & 0x0000FFFF;
    private ContentCategoryList.ContentCategoryData mContentData = null;
    private int mReadIndexFromContinued = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mContentData = intent.getParcelableExtra(Common.IntentParcelableExtraContentCategoryData);
        if(mContentData == null){
            // データが渡されていないため、継続不能
            finish();
        }else{
            setContentView(R.layout.activity_content);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            View tvReadFromContinued = findViewById(R.id.tvReadFromContinued);
            tvReadFromContinued.setOnClickListener(mClickListener);

            mReadIndexFromContinued = PreferenceUtil.readPrefInt(this, Common.PrefKeyLastReadIndexInCategory + mContentData.getId(this), 0);
            if((mReadIndexFromContinued == 0) || (mReadIndexFromContinued < 0) || (mReadIndexFromContinued >= mContentData.getContentCount(this))){
                // 値が未設定または不正な値なので、「つづきから読む」ボタンを無効化
                tvReadFromContinued.setEnabled(false);
            }
            findViewById(R.id.tvReadFromTheBeginning).setOnClickListener(mClickListener);

            BitmapDecoderTask.ContentCategory param = new BitmapDecoderTask.ContentCategory();
            param.categoryData = mContentData;
            BitmapDecoderTask task = new BitmapDecoderTask(this, mDecodeTaskListener);
            task.execute(param);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RequestCodeShowContent){
            if(resultCode == Activity.RESULT_OK && data != null){
                mReadIndexFromContinued = data.getIntExtra(Common.IntentIntExtraReadIndexInCategory, 0);

                PreferenceUtil.writePref(this, Common.PrefKeyLastReadIndexInCategory + mContentData.getId(this), mReadIndexFromContinued);

                View tvReadFromContinued = findViewById(R.id.tvReadFromContinued);
                if((mReadIndexFromContinued == 0) || (mReadIndexFromContinued < 0) || (mReadIndexFromContinued >= mContentData.getContentCount(this))){
                    // 値が未設定または不正な値なので、「つづきから読む」ボタンを無効化
                    tvReadFromContinued.setEnabled(false);
                }else{
                    // 途中まで読んだので、「つづきから読む」ボタンを有効化
                    tvReadFromContinued.setEnabled(true);
                }
            }
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            Intent intent = new Intent();
            intent.setClass(ContentActivity.this, ContentScreenActivity.class);
            intent.putExtra(Common.IntentParcelableExtraContentCategoryData, mContentData);

            if(id == R.id.tvReadFromTheBeginning){
                intent.putExtra(Common.IntentIntExtraReadIndexInCategory, 0);
            }else if(id == R.id.tvReadFromContinued){
                intent.putExtra(Common.IntentIntExtraReadIndexInCategory, mReadIndexFromContinued);
            }

            startActivityForResult(intent, RequestCodeShowContent);
        }
    };

    private BitmapDecoderTask.DecoderTaskListener mDecodeTaskListener = new BitmapDecoderTask.DecoderTaskListener() {
        @Override
        public void onPreExecute(BitmapDecoderTask task) {
            // 処理なし
        }

        @Override
        public void onPostExecute(BitmapDecoderTask task, BitmapDecoderTask.BitmapResult[] decodedBitmaps) {
            if(decodedBitmaps != null && decodedBitmaps.length > 0){
                if(ContentActivity.this.isFinishing()){
                    // 全てを解放
                    for(BitmapDecoderTask.BitmapResult result : decodedBitmaps){
                        if(result.decodedBitmap != null){
                            result.decodedBitmap.recycle();
                        }
                    }
                }else{
                    // １つしかデコードしていないはずだが、念のためそれ以外が存在したときは、他は全て解放
                    for(int i=0, size=decodedBitmaps.length; i<size; i++){
                        if(i == 0){
                            ImageView iconImageView = (ImageView)findViewById(R.id.ivIcon);
                            iconImageView.setImageBitmap(decodedBitmaps[i].decodedBitmap);
                            iconImageView.setTag(Common.ViewTagIconBitmap, decodedBitmaps[i].decodedBitmap);
                        }else{
                            if(decodedBitmaps[i].decodedBitmap != null){
                                decodedBitmaps[i].decodedBitmap.recycle();
                            }
                        }
                    }
                }
            }
        }
    };
}
