package jp.eq_inc.testapplication.manager;

import android.content.Context;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.io.InputStream;

import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.data.PresetContentCategoryList;

public class PresetDataLoader extends DataLoader {
    private PresetContentCategoryList mPresetContentList;

    public PresetDataLoader(Context context) {
        super(context);
    }

    @Override
    protected boolean loadData() {
        boolean ret = true;
        InputStream contentListInputStream = null;
        try {
            contentListInputStream = mContext.getAssets().open("content_list.json");
            mPresetContentList = LoganSquare.parse(contentListInputStream, PresetContentCategoryList.class);
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if(contentListInputStream != null){
                try {
                    contentListInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    @Override
    public ContentCategoryList getContentList() {
        return mPresetContentList;
    }
}
