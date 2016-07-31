package jp.eq_inc.testapplication.data;

import android.content.Context;
import android.graphics.Bitmap;

abstract public class ContentCategoryList {
    public static enum ShowLockType{
        Free,
        UnlockByAdClick,
        UnlockByWatchAdVideo,
        UnlockByStealthMarketing,
    }

    public static interface ContentCategoryData {
        abstract public String getTitle(Context context);
        abstract public Bitmap getIcon(Context context);
        abstract public int getContentCount(Context context);
        abstract public Bitmap getContent(Context context, int index);
        abstract public ShowLockType getShowLockType(Context context);
    }

    abstract public int getContentCategoryCount();
    abstract public ContentCategoryData getContentCategoryData(int index);
}
