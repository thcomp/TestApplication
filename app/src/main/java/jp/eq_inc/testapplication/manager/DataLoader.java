package jp.eq_inc.testapplication.manager;

import android.content.Context;

public abstract class DataLoader {
    public enum DataStatus{
        none,
        loading,
        loaded,
    }

    protected Context mContext;
    private DataStatus mDataStatus = DataStatus.none;
    private DataLoadListener mDataLoadListener;

    abstract protected boolean loadData();

    public DataLoader(Context context){
        mContext = context;
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
                    if(mDataLoadListener != null){
                        mDataLoadListener.onStartLoading();
                    }
                    break;
                case loaded:
                    if(mDataLoadListener != null){
                        mDataLoadListener.onStartLoading();
                        mDataLoadListener.onEndLoading(true);
                    }
                    break;
            }
        }
    }

    private Runnable mLoadDataRunnable = new Runnable() {
        @Override
        public void run() {
            if(mDataLoadListener != null){
                mDataLoadListener.onStartLoading();
            }

            if(loadData()){
                mDataStatus = DataStatus.loaded;
            }else{
                mDataStatus = DataStatus.none;
            }

            if(mDataLoadListener != null){
                mDataLoadListener.onEndLoading(mDataStatus == DataStatus.loaded);
            }
        }
    };
}
