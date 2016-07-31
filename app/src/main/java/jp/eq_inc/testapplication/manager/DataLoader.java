package jp.eq_inc.testapplication.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import jp.eq_inc.testapplication.data.ContentCategoryList;

public abstract class DataLoader {
    public enum DataStatus{
        none,
        loading,
        loaded,
    }

    protected Context mContext;
    private DataStatus mDataStatus = DataStatus.none;
    private DataLoadListener mDataLoadListener;
    private Handler mMainLooperHandler = null;

    abstract protected boolean loadData();
    abstract public ContentCategoryList getContentList();

    public DataLoader(Context context){
        mContext = context;
        mMainLooperHandler = new Handler(Looper.getMainLooper());
    }

    public void setDataLoadListener(DataLoadListener listener){
        mDataLoadListener = listener;
    }

    public DataStatus getCurrentDataLoadStatus(){
        return mDataStatus;
    }

    public void loadDataAsync(){
        synchronized (this) {
            switch (mDataStatus){
                case none:
                    mDataStatus = DataStatus.loading;
                    new Thread(mLoadDataRunnable).start();
                    break;
                case loading:
                    callStartLoadingListener();
                    break;
                case loaded:
                    callStartLoadingListener();
                    callEndLoadingListener(true);
                    break;
            }
        }
    }

    private void callStartLoadingListener(){
        if(mDataLoadListener != null) {
            if (Thread.currentThread().equals(mContext.getMainLooper().getThread())) {
                mDataLoadListener.onStartLoading();
            } else {
                mMainLooperHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callStartLoadingListener();
                    }
                });
            }
        }
    }

    private void callEndLoadingListener(final boolean result){
        if(mDataLoadListener != null) {
            if (Thread.currentThread().equals(mContext.getMainLooper().getThread())) {
                mDataLoadListener.onEndLoading(result);
            } else {
                mMainLooperHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callEndLoadingListener(result);
                    }
                });
            }
        }
    }

    private Runnable mLoadDataRunnable = new Runnable() {
        @Override
        public void run() {
            callStartLoadingListener();

            if(loadData()){
                mDataStatus = DataStatus.loaded;
            }else{
                mDataStatus = DataStatus.none;
            }

            callEndLoadingListener(mDataStatus == DataStatus.loaded);
        }
    };
}
