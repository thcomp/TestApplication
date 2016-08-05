package jp.eq_inc.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.co.thcomp.util.PreferenceUtil;
import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.fragment.ContentFragment;

public class ContentScreenActivity extends AppCompatActivity {
    private ContentCategoryList.ContentCategoryData mContentData = null;
    private View mTutorialView = null;
    private ViewPager mContentPager;
    AlertDialog.Builder mDialogBuilder;

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

            if(!PreferenceUtil.readPrefBoolean(this, Common.PrefKeyShownTutorial, false)){
                PreferenceUtil.writePref(this, Common.PrefKeyShownTutorial, true);
                LayoutInflater inflater = getLayoutInflater();
                mTutorialView = inflater.inflate(R.layout.activity_content_screen_tutorial, null, false);
                mTutorialView.findViewById(R.id.ivClose).setOnClickListener(mClickOnTutorialListener);

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addContentView(mTutorialView, params);
            }

            ((TextView)findViewById(R.id.tvTitle)).setText(mContentData.getTitle(this));
            mContentPager = (ViewPager)findViewById(R.id.vpContentPager);
            mContentPager.setOnTouchListener(mConsumeViewPagerTouchListener);
            mContentPager.setAdapter(new ContentFragmentPagerAdapter(getSupportFragmentManager()));

            findViewById(R.id.vToNextArea).setOnClickListener(mClickListener);
            findViewById(R.id.vShowMenuArea).setOnClickListener(mClickListener);
            findViewById(R.id.vToPrevArea).setOnClickListener(mClickListener);

            int initPageIndex = intent.getIntExtra(Common.IntentIntExtraReadIndexInCategory, 0);
            /**
             * ViewPagerは通常は左->右にページングする部品に対して、読み物アプリは右->左にページングすることを想定している。
             * そのためViewPagerは使用する際に、反対方向に動作する必要がある。
             */
            mContentPager.setCurrentItem(changeContentIndexToCurrentItem(initPageIndex, mContentData.getContentCount(this)), false);

            mDialogBuilder = new AlertDialog.Builder(this);
            mDialogBuilder.setPositiveButton(android.R.string.ok, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(Common.IntentIntExtraReadIndexInCategory, changeCurrentItemToContentIndex(mContentPager.getCurrentItem(), mContentData.getContentCount(this)));
        setResult(Activity.RESULT_OK, intent);

        super.finish();
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
            int id = v.getId();
            int pageIndex = mContentPager.getCurrentItem();
            int maxPageIndex = mContentData.getContentCount(ContentScreenActivity.this);

            /**
             * ViewPagerは画面左端に向けてフリックすると次ページへの遷移に対して、本アプリは画面端をタッチすると次のページに遷移となる（方向が逆）
             */
            switch (id){
                case R.id.vToNextArea:
                    if(pageIndex - 1 >= 0){
                        mContentPager.setCurrentItem(pageIndex - 1, true);
                    }else{
                        AlertDialog dialog = mDialogBuilder.create();
                        dialog.setTitle(R.string.last_content_in_category);
                        dialog.setMessage(getString(R.string.cannot_move_to_next_page));
                        dialog.show();
                    }
                    break;
                case R.id.vToPrevArea:
                    if(pageIndex + 1 <= (maxPageIndex - 1)){
                        mContentPager.setCurrentItem(pageIndex + 1, true);
                    }else{
                        AlertDialog dialog = mDialogBuilder.create();
                        dialog.setTitle(R.string.first_content_in_category);
                        dialog.setMessage(getString(R.string.cannot_move_to_prev_page));
                        dialog.show();
                    }
                    break;
                case R.id.vShowMenuArea:
                    break;
            }
        }
    };

    private View.OnTouchListener mConsumeViewPagerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // ViewPagerによるページ送りを無効化するため、常にtrueを返却
            return true;
        }
    };

    private class ContentFragmentPagerAdapter extends FragmentStatePagerAdapter {
        public ContentFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ContentFragment.newInstance(mContentData, position);
        }

        @Override
        public int getCount() {
            return mContentData.getContentCount(ContentScreenActivity.this);
        }
    };

    private static int changeCurrentItemToContentIndex(int currentItemIndex, int itemCount){
        return itemCount - 1 - currentItemIndex;
    }

    private static int changeContentIndexToCurrentItem(int contentIndex, int itemCount){
        return itemCount - 1 - contentIndex;
    }
}
