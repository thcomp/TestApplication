package jp.eq_inc.testapplication.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import jp.eq_inc.testapplication.Common;
import jp.eq_inc.testapplication.R;
import jp.eq_inc.testapplication.data.ContentCategoryList;
import jp.eq_inc.testapplication.task.BitmapDecoderTask;

public class ContentFragment extends Fragment {
    private ContentCategoryList.ContentCategoryData mContentData;
    private int mPageIndex = 0;

    public ContentFragment() {
        // Required empty public constructor
    }

    public static ContentFragment newInstance(ContentCategoryList.ContentCategoryData categoryData, int pageIndex) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putParcelable(Common.IntentParcelableExtraContentCategoryData, categoryData);
        args.putInt(Common.IntentIntExtraReadIndexInCategory, pageIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mContentData = arguments.getParcelable(Common.IntentParcelableExtraContentCategoryData);
            mPageIndex = arguments.getInt(Common.IntentIntExtraReadIndexInCategory, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_content, container, false);
        int contentDataCount = mContentData.getContentCount(getActivity());
        final ImageView fImageView = (ImageView) ret.findViewById(R.id.ivContent);

        BitmapDecoderTask task = new BitmapDecoderTask(getActivity(), new BitmapDecoderTask.DecoderTaskListener() {
            @Override
            public void onPreExecute(BitmapDecoderTask task) {
                // no work
            }

            @Override
            public void onPostExecute(BitmapDecoderTask task, BitmapDecoderTask.BitmapResult[] decodedBitmaps) {
                if(decodedBitmaps != null && decodedBitmaps.length > 0){
                    BitmapDecoderTask.ContentCategory param = (BitmapDecoderTask.ContentCategory) fImageView.getTag(Common.ViewTagDecodeParam);
                    if(param.equals(decodedBitmaps[0].param)){
                        // まだリサイクルされていないViewなので、デコードが完了した画像を設定
                        fImageView.setTag(Common.ViewTagIconBitmap, decodedBitmaps[0].decodedBitmap);
                        ((ImageView)fImageView.findViewById(R.id.ivContent)).setImageBitmap(decodedBitmaps[0].decodedBitmap);
                        ((ViewGroup)fImageView.getParent()).invalidate();
                    }else{
                        // Viewがすでにリサイクルされて、別の画像を割りあてるようになっているので、設定せずにそのまま解放
                        decodedBitmaps[0].decodedBitmap.recycle();
                    }
                }
            }
        });
        BitmapDecoderTask.ContentCategory param = new BitmapDecoderTask.ContentCategory();
        param.categoryData = mContentData;
        param.targetType = BitmapDecoderTask.ContentCategory.TargetType.Content;
        param.targetContentPosition = contentDataCount - 1 - mPageIndex;
        task.execute(param);
        fImageView.setTag(Common.ViewTagDecodeParam, param);

        return ret;
    }
}
