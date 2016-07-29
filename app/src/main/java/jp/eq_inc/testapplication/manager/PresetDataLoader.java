package jp.eq_inc.testapplication.manager;

import android.content.Context;

public class PresetDataLoader extends DataLoader {
    public PresetDataLoader(Context context) {
        super(context);
    }

    @Override
    protected boolean loadData() {
        return false;
    }
}
