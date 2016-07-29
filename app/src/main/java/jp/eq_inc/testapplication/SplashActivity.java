package jp.eq_inc.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    private Handler mMainLooperHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        // スプラッシュをクリックしたときに、即時にコンテンツ一覧画面に遷移
        findViewById(R.id.flSplashRoot).setOnClickListener(mSplashClickListener);

        mMainLooperHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 3秒後にスプラッシュ画面の消灯
        mMainLooperHandler.removeCallbacks(mLaunchMainScreenRunnable);
        mMainLooperHandler.postDelayed(mLaunchMainScreenRunnable, 3 * DateUtils.SECOND_IN_MILLIS);
    }

    private Runnable mLaunchMainScreenRunnable = new Runnable() {
        @Override
        public void run() {
            if(!SplashActivity.this.isFinishing()) {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, ContentListActivity.class);
                startActivity(intent);

                // スプラッシュ画面自体は不要なので終了
                finish();
            }
        }
    };

    private View.OnClickListener mSplashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLaunchMainScreenRunnable.run();

            // タイマで設定されている方を無効化
            mMainLooperHandler.removeCallbacks(mLaunchMainScreenRunnable);
        }
    };
}
