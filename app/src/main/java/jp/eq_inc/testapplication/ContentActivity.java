package jp.eq_inc.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import jp.co.thcomp.util.PreferenceUtil;
import jp.eq_inc.testapplication.data.ContentCategoryList;

public class ContentActivity extends AppCompatActivity {
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

            mReadIndexFromContinued = PreferenceUtil.readPrefInt(this, mContentData.getId(this) + Common.PrefKeyLastReadIndexInCategory, 0);
            if((mReadIndexFromContinued == 0) || (mReadIndexFromContinued < 0) || (mReadIndexFromContinued >= mContentData.getContentCount(this))){
                // 値が未設定または不正な値なので、「つづきから読む」ボタンは非表示
                tvReadFromContinued.setVisibility(View.GONE);
            }

            findViewById(R.id.tvReadFromTheBeginning).setOnClickListener(mClickListener);
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

            startActivity(intent);
        }
    };
}
