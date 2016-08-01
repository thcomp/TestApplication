package jp.eq_inc.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import jp.eq_inc.testapplication.data.ContentCategoryList;

public class ContentActivity extends Activity {
    private ContentCategoryList.ContentCategoryData mContentData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mContentData = intent.getParcelableExtra(Common.IntentParcelableExtraPresetContentCategoryData);
        if(mContentData == null){
            // データが渡されていないため、継続不能
            finish();
        }else{

        }
    }
}
