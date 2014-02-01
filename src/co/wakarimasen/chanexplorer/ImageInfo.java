package co.wakarimasen.chanexplorer;

import co.wakarimasen.chanexplorer.imageboard.Board;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {
	public String url;
	public String filename;
	public String thumb;
	public Board board;
	public int threadId;
	
	public ImageInfo(Board b, int threadId, String url, String filename, String thumb) {
		this.url = url;
		this.filename = filename;
		this.board = b;
		this.threadId = threadId;
		this.thumb = thumb;
		if (this.url.startsWith("//")) {
			this.url = String.format("http:%s", url);
		}
	}
	
	public boolean isGif() {
		return this.url.endsWith("gif");
	}
	
	public ImageInfo(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(board.getId());
		dest.writeInt(threadId);
		dest.writeString(url);
		dest.writeString(filename);
		dest.writeString(thumb);
		
	}
	
	public void readFromParcel(Parcel in) {
		board = Board.getBoardById(in.readString());
		threadId = in.readInt();
		url = in.readString();
		filename = in.readString();
		thumb = in.readString();
	}
	
	public static final Parcelable.Creator<ImageInfo> CREATOR = new Parcelable.Creator<ImageInfo>() {
        @Override
		public ImageInfo createFromParcel(Parcel in) {
            return new ImageInfo(in);
        }
 
        @Override
		public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };
}
