package jp.eq_inc.testapplication.manager;

import android.content.Context;

import java.util.ArrayList;

public class ContentDataManager {
    private static final int DataLoaderPreset = 0;
    private static ContentDataManager sInstance = null;

    public static synchronized ContentDataManager getInstance(Context context) {
        if(sInstance == null){
            sInstance = new ContentDataManager(context);
        }
        return sInstance;
    }

    private Context mContext;
    private DataLoader[] mDataLoaderArray = null;
    private ArrayList<DataLoadListener> mPresetListenerList = new ArrayList<DataLoadListener>();

    private ContentDataManager(Context context) {
        if(context == null){
            throw new NullPointerException("context == null");
        }

        mContext = context;
        mDataLoaderArray = new DataLoader[]{new PresetDataLoader(context)};
        mDataLoaderArray[DataLoaderPreset].setDataLoadListener(mPresetDataLoadListener);
    }

    public void registerPresetDataLoadListener(DataLoadListener listener){
        mPresetListenerList.add(listener);
    }

    public void unregisterPresetDataLoadListener(DataLoadListener listener){
        mPresetListenerList.remove(listener);
    }

    public DataLoader.DataStatus getPresetCurrentDataLoadStatus(){
        return mDataLoaderArray[DataLoaderPreset].getCurrentDataLoadStatus();
    }

    public void loadPresetContents(){
        mDataLoaderArray[DataLoaderPreset].loadDataAsync();
    }

    private DataLoadListener mPresetDataLoadListener = new DataLoadListener() {
        @Override
        public void onStartLoading() {
            DataLoadListener[] listenerArray = mPresetListenerList.toArray(new DataLoadListener[mPresetListenerList.size()]);
            for(DataLoadListener listener : listenerArray){
                listener.onStartLoading();
            }
        }

        @Override
        public void onEndLoading(boolean result) {
            DataLoadListener[] listenerArray = mPresetListenerList.toArray(new DataLoadListener[mPresetListenerList.size()]);
            for(DataLoadListener listener : listenerArray){
                listener.onEndLoading(result);
            }
        }
    };
}
