package knez.assdroid.editor.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class SubtitleLineSettings implements Parcelable {

    private final boolean showTimings;
    private final boolean showStyle;
    private final boolean showTagContents;
    @NonNull private final String tagReplacement;
    private final int subtitleTextSizeDp;
    private final int otherTextSizeDp;

    public SubtitleLineSettings(boolean showTimings,
                                boolean showStyle,
                                boolean showTagContents,
                                @NonNull String tagReplacement,
                                int subtitleTextSizeDp,
                                int otherTextSizeDp) {
        this.showTimings = showTimings;
        this.showStyle = showStyle;
        this.showTagContents = showTagContents;
        this.tagReplacement = tagReplacement;
        this.subtitleTextSizeDp = subtitleTextSizeDp;
        this.otherTextSizeDp = otherTextSizeDp;
    }

    public boolean isShowStyleAndActor() { return showStyle; }
    public boolean isShowTagContents() { return showTagContents; }
    public boolean isShowTimings() { return showTimings; }
    @NonNull public String getTagReplacement() { return tagReplacement; }
    public int getSubtitleTextSizeDp() { return subtitleTextSizeDp; }
    public int getOtherTextSizeDp() { return otherTextSizeDp; }


    // ---------------------------------------------------------------------------------- PARCELABLE

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.showTimings ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showStyle ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showTagContents ? (byte) 1 : (byte) 0);
        dest.writeString(this.tagReplacement);
        dest.writeInt(this.subtitleTextSizeDp);
        dest.writeInt(this.otherTextSizeDp);
    }

    protected SubtitleLineSettings(Parcel in) {
        this.showTimings = in.readByte() != 0;
        this.showStyle = in.readByte() != 0;
        this.showTagContents = in.readByte() != 0;
        this.tagReplacement = in.readString();
        this.subtitleTextSizeDp = in.readInt();
        this.otherTextSizeDp = in.readInt();
    }

    public static final Creator<SubtitleLineSettings> CREATOR = new Creator<SubtitleLineSettings>() {
        @Override
        public SubtitleLineSettings createFromParcel(Parcel source) {
            return new SubtitleLineSettings(source);
        }

        @Override
        public SubtitleLineSettings[] newArray(int size) {
            return new SubtitleLineSettings[size];
        }
    };

}
