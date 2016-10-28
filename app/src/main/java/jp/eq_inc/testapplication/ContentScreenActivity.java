package jp.eq_inc.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.thcomp.util.PreferenceUtil;
import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.fragment.ContentFragment;

public class ContentScreenActivity extends AppCompatActivity {
    private static final int ShowToolbarIntervalMS = (int) (3 * DateUtils.SECOND_IN_MILLIS);
    private ContentCategoryList.ContentCategoryData mContentData = null;
    private View mTutorialView = null;
    private View mAppBarLayout;
    private ViewPager mContentPager;
    private TextView mTitleView;
    private Toolbar mToolbar;
    private AlertDialog.Builder mDialogBuilder;
    private Handler mMainLooperHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mContentData = intent.getParcelableExtra(Common.IntentParcelableExtraContentCategoryData);
        if(mContentData == null){
            // データが渡されていないため、継続不能
            finish();
        }else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_content_screen);
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mAppBarLayout = findViewById(R.id.appBarLayout);

            // 起動時は非表示にする
            mAppBarLayout.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);

            if(!PreferenceUtil.readPrefBoolean(this, Common.PrefKeyShownTutorial, false)){
                PreferenceUtil.writePref(this, Common.PrefKeyShownTutorial, true);
                LayoutInflater inflater = getLayoutInflater();
                mTutorialView = inflater.inflate(R.layout.activity_content_screen_tutorial, null, false);
                mTutorialView.setOnClickListener(mClickOnTutorialListener);
                mTutorialView.findViewById(R.id.ivClose).setOnClickListener(mClickOnTutorialListener);

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addContentView(mTutorialView, params);
            }

            mTitleView = (TextView)findViewById(R.id.tvTitle);
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
            setCategoryTitle(initPageIndex);

            mDialogBuilder = new AlertDialog.Builder(this);
            mDialogBuilder.setPositiveButton(android.R.string.ok, null);

            mMainLooperHandler = new Handler(getMainLooper());
        }
    }

    @Override
    public void onBackPressed() {
        if(mTutorialView != null && mTutorialView.getVisibility() == View.VISIBLE){
            mTutorialView.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_screen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        int itemId = item.getItemId();

        if(itemId == R.id.menuShare && mToolbar.getVisibility() == View.VISIBLE) {
            Toast.makeText(ContentScreenActivity.this, R.string.under_construction, Toast.LENGTH_SHORT).show();
            ret = true;
        }else if(itemId == android.R.id.home){
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            NavUtils.navigateUpTo(this, upIntent);
            ret = true;
        }

        return ret;
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(Common.IntentIntExtraReadIndexInCategory, changeCurrentItemToContentIndex(mContentPager.getCurrentItem(), mContentData.getContentCount(this)));
        setResult(Activity.RESULT_OK, intent);

        super.finish();
    }

    private void setCategoryTitle(int initPageIndex){
        StringBuilder title = new StringBuilder(mContentData.getTitle(this));
        title.append(" ( ").append(initPageIndex + 1).append(" / ").append(mContentData.getContentCount(this)).append(" ) ");
        mTitleView.setText(title.toString());
    }

    private View.OnClickListener mClickOnTutorialListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // このlistenerが発動したら、無条件でチュートリアルを非表示化
            mTutorialView.setVisibility(View.GONE);
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
                        setCategoryTitle(maxPageIndex - pageIndex - 1 + 1);
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
                        setCategoryTitle(maxPageIndex - pageIndex - 1 - 1);
                    }else{
                        AlertDialog dialog = mDialogBuilder.create();
                        dialog.setTitle(R.string.first_content_in_category);
                        dialog.setMessage(getString(R.string.cannot_move_to_prev_page));
                        dialog.show();
                    }
                    break;
                case R.id.vShowMenuArea:
                    if(mAppBarLayout.getVisibility() == View.GONE){
                        // 表示用のアニメーションを実施
                        mAppBarLayout.setVisibility(View.VISIBLE);
                        mToolbar.setVisibility(View.VISIBLE);
                        Animation showAnimation = AnimationUtils.loadAnimation(ContentScreenActivity.this, R.anim.show_toolbar);
                        showAnimation.setAnimationListener(mShowAnimationListener);
                        mAppBarLayout.startAnimation(showAnimation);
                    }
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

    private Animation.AnimationListener mShowAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            // 処理なし
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // 非表示用のアニメーションに対するタイマを設定
            mMainLooperHandler.postDelayed(mHideToolbarRunnable, ShowToolbarIntervalMS);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // 処理なし
        }
    };

    private Animation.AnimationListener mHideAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            // 処理なし
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mAppBarLayout.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // 処理なし
        }
    };

    private Runnable mHideToolbarRunnable = new Runnable() {
        @Override
        public void run() {
            Animation hideAnimation = AnimationUtils.loadAnimation(ContentScreenActivity.this, R.anim.hide_toolbar);
            hideAnimation.setAnimationListener(mHideAnimationListener);
            mAppBarLayout.startAnimation(hideAnimation);
        }
    };

    private static int changeCurrentItemToContentIndex(int currentItemIndex, int itemCount){
        return itemCount - 1 - currentItemIndex;
    }

    private static int changeContentIndexToCurrentItem(int contentIndex, int itemCount){
        return itemCount - 1 - contentIndex;
    }
}
