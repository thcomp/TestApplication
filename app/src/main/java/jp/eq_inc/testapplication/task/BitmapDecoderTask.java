package jp.eq_inc.testapplication.task;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.eq_inc.testapplication.data.ContentCategoryList;

public class BitmapDecoderTask extends AsyncTask<BitmapDecoderTask.Parameter, Void, BitmapDecoderTask.BitmapResult[]> {
    public static class BitmapResult{
        public Parameter param;
        public Bitmap decodedBitmap;
    }

    public static interface DecoderTaskListener{
        public void onPreExecute(BitmapDecoderTask task);
        public void onPostExecute(BitmapDecoderTask task, BitmapResult[] decodedBitmaps);
    }

    abstract public static class Parameter{
        public BitmapFactory.Options options;
    }

    public static class Asset extends Parameter{
        public String path;
        public Rect outPadding;
    }

    public static class LocalFile extends Parameter{
        public String path;
        public Rect outPadding;
    }

    public static class RemoteFile extends Parameter{
        public String path;
        public Rect outPadding;
    }

    public static class ResourceFile extends Parameter{
        public int resId;
    }

    public static class ContentCategory extends Parameter{
        public static enum TargetType{
            Icon,
            Content
        }

        public TargetType targetType = TargetType.Icon;
        public ContentCategoryList.ContentCategoryData categoryData;
        public Integer targetContentPosition = null;
    }

    protected Context mContext;
    protected DecoderTaskListener mListener;

    public BitmapDecoderTask(Context context, DecoderTaskListener listener){
        if(context == null){
            throw new NullPointerException("context == null");
        }

        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if(mListener != null){
            mListener.onPreExecute(this);
        }
    }

    @Override
    protected BitmapResult[] doInBackground(Parameter... params) {
        ArrayList<BitmapResult> retBitmapList = new ArrayList<BitmapResult>();

        if(params != null && params.length > 0){
            for (Parameter param : params) {
                try{
                    Bitmap tempBitmap = null;

                    if (param instanceof Asset) {
                        tempBitmap = decodeAsset((Asset) param);
                    } else if (param instanceof LocalFile) {
                        tempBitmap = decodeLocalFile((LocalFile) param);
                    } else if (param instanceof RemoteFile) {
                        tempBitmap = decodeRemoteFile((RemoteFile) param);
                    } else if (param instanceof ResourceFile) {
                        tempBitmap = decodeResourceFile((ResourceFile) param);
                    } else if (param instanceof ContentCategory) {
                        tempBitmap = decodeContentCategory((ContentCategory) param);
                    }

                    if (tempBitmap != null) {
                        BitmapResult tempResult = new BitmapResult();
                        tempResult.param = param;
                        tempResult.decodedBitmap = tempBitmap;
                        retBitmapList.add(tempResult);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }catch (OutOfMemoryError e){
                    e.printStackTrace();
                }
            }
        }

        return retBitmapList.toArray(new BitmapResult[0]);
    }

    @Override
    protected void onPostExecute(BitmapResult[] bitmapResults) {
        if(mListener != null){
            mListener.onPostExecute(this, bitmapResults);
        }
    }

    private Bitmap decodeAsset(Asset param){
        Bitmap ret = null;
        AssetManager assetManager = mContext.getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open(param.path);
            ret = BitmapFactory.decodeStream(inputStream, param.outPadding, param.options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    private Bitmap decodeLocalFile(LocalFile param){
        Bitmap ret = null;
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(param.path);
            ret = BitmapFactory.decodeStream(inputStream, param.outPadding, param.options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    private Bitmap decodeRemoteFile(RemoteFile param){
        throw new UnsupportedOperationException();
    }

    private Bitmap decodeResourceFile(ResourceFile param){
        return BitmapFactory.decodeResource(mContext.getResources(), param.resId, param.options);
    }

    private Bitmap decodeContentCategory(ContentCategory param){
        Bitmap ret = null;

        switch (param.targetType){
            case Icon:
                ret = param.categoryData.getIcon(mContext);
                break;
            case Content:
                ret = param.categoryData.getContent(mContext, param.targetContentPosition);
                break;
        }

        return ret;
    }
}
