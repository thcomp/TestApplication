package jp.eq_inc.testapplication.manager;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class PresetDataLoader extends DataLoader {
    public PresetDataLoader(Context context) {
        super(context);
    }

    @Override
    protected boolean loadData() {
        InputStream contentListInputStream = null;
        try {
            contentListInputStream = mContext.getAssets().open("content_list.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
