package jp.eq_inc.testapplication.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@JsonObject
public class PresetContentCategoryList extends ContentCategoryList {
    @JsonField
    public ArrayList<PresetContentData> contents;

    @Override
    public int getContentCategoryCount() {
        return contents.size();
    }

    @Override
    public ContentCategoryData getContentCategoryData(int index) {
        return ((contents != null) && (index >= 0 && index < contents.size())) ? contents.get(index) : null;
    }

    @JsonObject
    public static class PresetContentData implements ContentCategoryData, Parcelable {
        @JsonField
        public String id;
        @JsonField
        public String title;
        @JsonField
        public String icon_name;
        @JsonField
        public String content_range;
        @JsonField
        public String show_lock;

        private String[] mContents = null;
        private ShowLockType mShowLockType = null;

        public PresetContentData(){
            // no work
        }

        protected PresetContentData(Parcel in) {
            id = in.readString();
            title = in.readString();
            icon_name = in.readString();
            content_range = in.readString();
            show_lock = in.readString();
            mContents = in.createStringArray();
        }

        public static final Creator<PresetContentData> CREATOR = new Creator<PresetContentData>() {
            @Override
            public PresetContentData createFromParcel(Parcel in) {
                return new PresetContentData(in);
            }

            @Override
            public PresetContentData[] newArray(int size) {
                return new PresetContentData[size];
            }
        };

        @Override
        public String getId(Context context) {
            return id;
        }

        @Override
        public String getTitle(Context context) {
            return title;
        }

        @Override
        public Bitmap getIcon(Context context) {
            return decodeBitmap(context, "icon/" + icon_name + ".png");
        }

        @Override
        public int getContentCount(Context context) {
            expandContentNames();
            return mContents == null ? 0 : mContents.length;
        }

        @Override
        public Bitmap getContent(Context context, int index) {
            return decodeBitmap(context, mContents != null ? mContents[index] : null);
        }

        @Override
        public ShowLockType getShowLockType(Context context) {
            if(mShowLockType == null){
                try {
                    int code = Integer.parseInt(show_lock);
                    mShowLockType = ShowLockType.values()[code];
                }catch(NumberFormatException e){
                    mShowLockType = ShowLockType.Free;
                }catch(Exception e){
                    mShowLockType = ShowLockType.Free;
                }
            }

            return mShowLockType;
        }

        private void expandContentNames() {
            if(mContents == null && content_range != null){
                String contentRange[] = content_range.split("_");
                if(contentRange != null && contentRange.length >= 2){
                    Integer contentRangeStart = null;
                    Integer contentRangeEnd = null;

                    try{
                        contentRangeStart = Integer.parseInt(contentRange[0]);
                        contentRangeEnd = Integer.parseInt(contentRange[1]);
                    }catch(NumberFormatException e){
                        // no work
                    }

                    if(contentRangeStart != null && contentRangeEnd != null){
                        ArrayList<String> tempContentList = new ArrayList<String>();
                        int min = Math.min(contentRangeStart, contentRangeEnd);
                        int max = Math.max(contentRangeStart, contentRangeEnd);
                        for(int i=min; i<=max; i++){
                            tempContentList.add(String.format("contents/%02d.png", i));
                        }
                        mContents = tempContentList.toArray(new String[0]);
                    }
                }
            }
        }

        private Bitmap decodeBitmap(Context context, String assetPath){
            Bitmap ret = null;
            AssetManager assetManager = context.getAssets();
            InputStream bitmapInptStream = null;

            try {
                bitmapInptStream = assetManager.open(assetPath);
                ret = BitmapFactory.decodeStream(bitmapInptStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(bitmapInptStream != null){
                    try {
                        bitmapInptStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return ret;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(title);
            dest.writeString(icon_name);
            dest.writeString(content_range);
            dest.writeString(show_lock);
            dest.writeStringArray(mContents);
        }
    }
}
