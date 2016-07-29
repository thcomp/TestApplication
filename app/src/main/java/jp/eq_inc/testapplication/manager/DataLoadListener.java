package jp.eq_inc.testapplication.manager;

public interface DataLoadListener {
    public void onStartLoading();
    public void onEndLoading(boolean result);
}
