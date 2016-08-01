package jp.eq_inc.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.thcomp.util.PreferenceUtil;
import jp.eq_inc.testapplication.data.ContentCategoryList;

public class ContentScreenActivity extends AppCompatActivity {
    private ContentCategoryList.ContentCategoryData mContentData = null;
    private View mTutorialView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mContentData = intent.getParcelableExtra(Common.IntentParcelableExtraContentCategoryData);
        if(mContentData == null){
            // データが渡されていないため、継続不能
            finish();
        }else{
            setContentView(R.layout.activity_content_screen);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if(!PreferenceUtil.readPrefBoolean(this, Common.PrefKeyShownTutorial, false)){
                LayoutInflater inflater = getLayoutInflater();
                mTutorialView = inflater.inflate(R.layout.activity_content_screen_tutorial, null, false);
                mTutorialView.findViewById(R.id.ivClose).setOnClickListener(mClickOnTutorialListener);

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addContentView(mTutorialView, params);
            }
        }
    }

    private View.OnClickListener mClickOnTutorialListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();

            if(id == R.id.ivClose){
                mTutorialView.setVisibility(View.GONE);
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
