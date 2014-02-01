package co.wakarimasen.chanexplorer.imageboard;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.wakarimasen.chanexplorer.HTMLEntities;
import co.wakarimasen.chanexplorer.Theme;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;

public class Post implements Parcelable {

	private String name;
	private String tripcode;
	private String email;
	private String subject;
	private String comment;
	private Spannable spannedComment;
	private long timestamp = -1;
	private Timestamp timestamp_obj = null;
	private int id = -1;
	private String thumbnail;
	private String image;
	private String filename;
	private String filesize;
	private boolean hasFile = false;
	private boolean fileDeleted = false;
	private int width = 0;
	private int height = 0;
	private int th_width = 0;
	private int th_height = 0;
	private int thread_id = 0;
	private String poster_id;
	private int omitted_posts = 0;
	private int omitted_images = 0;
	private boolean isMod = false;
	private boolean isAdmin = false;
	private boolean isTooLong = false;
	private boolean isLocked = false;
	private boolean isSticky = false;
	private boolean isSpoiler = false;
	private boolean isBanned = false;
	private String idenIcon;
	private List<Integer> replies;
	private int mGreenTextColor = Theme.Holo.green_text;

	private final static Pattern link_match = Pattern
			.compile("<a href=\"(.+?)\" class=\"quotelink\".*?>(.+?)</a>");
	private final static Pattern green_match = Pattern
			.compile("<span class=\"quote\">(.+?)</span><br>");
	private final static Pattern url_match = Pattern
			.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private final static Pattern spoiler_match = Pattern
			.compile("<span class=\"spoiler\">(.+?)</span>");
	protected final static Pattern exif_td = Pattern.compile("<td>(.+?)</td>");
	
	private List<String> mHyperLinks = new ArrayList<String>();
	private List<String> mLinks = new ArrayList<String>();

	/* Board Specific Features */
	/* sp */
	private String flag;
	/* p */
	private String[] exif_camera = null;
	private String[] exif_image = null;

	public Post(int greenTextColor) {
		mGreenTextColor = greenTextColor;
	}

	public boolean isThread() {
		return id == thread_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getComment() {
		return comment;
	}
	
	public Spannable getSpannedComment() {
		return spannedComment;
	}
	
	public List<String> getLinks() {
		return mLinks;
	}
	
	public List<String> getHyperLinks() {
		return mHyperLinks;
	}

	public void setComment(String comment) {
		this.comment = comment.replace("<br/>", "<br>");
		if (this.comment.indexOf("<span class=\"abbr\">Comment") != -1) {
			setTooLong(true);
			this.comment = comment.substring(0,
					this.comment.indexOf("<span class=\"abbr\">Comment"));
		}
		if (this.comment.indexOf("<span class=\"abbr\">[EXIF data") != -1) {
			String campSpec = getBetween("Camera-Specific Properties",
					"Image-Specific", this.comment, 0);
			String imageSpec = this.comment.substring(this.comment
					.indexOf("Image-Specific"));
			Matcher m1 = exif_td.matcher(campSpec);
			Matcher m2 = exif_td.matcher(imageSpec);
			ArrayList<String> t_exif_camera = new ArrayList<String>();
			ArrayList<String> t_exif_image = new ArrayList<String>();
			while (m1.find()) {
				t_exif_camera.add(m1.group(1));
			}
			while (m2.find()) {
				t_exif_image.add(m2.group(1));
			}
			exif_camera = new String[t_exif_camera.size()];
			exif_image = new String[t_exif_image.size()];
			t_exif_camera.toArray(exif_camera);
			t_exif_image.toArray(exif_image);

			this.comment = comment.substring(0,
					this.comment.indexOf("<span class=\"abbr\">[EXIF data"));
		}
		if (this.comment
				.indexOf("<b style=\"color:red;\">(USER WAS BANNED FOR THIS POST)</b>") != -1) {
			setBanned(true);
			this.comment = comment
					.substring(
							0,
							this.comment
									.indexOf("<br><br><b style=\"color:red;\">(USER WAS BANNED"));
		} else if (this.comment
				.indexOf("<strong style=\"color: red;\">(USER WAS BANNED FOR THIS POST)</strong>") != -1) {
			setBanned(true);
			this.comment = comment
					.substring(
							0,
							this.comment
									.indexOf("<br><br><strong style=\"color: red;\">(USER WAS BANNED FOR THIS POST)</strong>"));
		}
		parseComment();
	}
	
	private void parseComment() {
		String comment = this.comment + "<br>";
		mLinks.clear();
		mHyperLinks.clear();
		// <span class="quote"><a href="143752#p143993" class="quotelink"
		// onClick="replyhl('143993');">&gt;&gt;143993</a></span><br><span
		// class="quote"><a href="/v/"
		// class="quotelink">&gt;&gt;&gt;/v/</a></span><br><span
		// class="quote"><a href="/v/res/152900882#p152900882"
		// class="quotelink">&gt;&gt;&gt;/v/152900882</a></span><br><span
		// class="quote"><a href="/v/res/152900882#p152901379"
		// class="quotelink">&gt;&gt;&gt;/v/152901379</a></span>
		Matcher m1 = green_match.matcher(comment);
		String s1 = String
				.format("</font><a href=\"$1\" class=\"quotelink\">$2</a><font color=\"#%X\">",
						mGreenTextColor & 0xFFFFFF);
		String s2 = String.format(
				"</font><span class=\"spoiler\">$1</span><font color=\"#%X\">",
				mGreenTextColor & 0xFFFFFF);
		while (m1.find()) {
			String green_text = m1.group(1);
			green_text = green_text.replaceAll(
					"<a href=\"(.+?)\" class=\"quotelink\".*?>(.+?)</a>", s1);
			green_text = green_text.replaceAll(
					"<span class=\"spoiler\">(.*?)</span>", s2);
			comment = comment.replace(m1.group(), "<span class=\"quote\">"
					+ green_text + "</span><br>");
		}
		comment = comment.replaceAll("<span class=\"quote\">(.+?)</span><br>",
				String.format("<font color=\"#%X\">$1</font><br>",
						mGreenTextColor & 0xFFFFFF));

		// comment =
		// comment.replaceAll("<a href=\"(.+?)\" class=\"quotelink\".*?>(.+?)</a>",
		// "<font color=\"#DD0000\"><a href=\"$1\">$2</a></font>");
		m1 = link_match.matcher(comment);
		while (m1.find()) {
			mLinks.add(m1.group(1));
			String replacement = String.format(
					"<font color=\"#DD0000\"><u>%s</u></font>", m1.group(2));
			comment = comment.replace(m1.group(), replacement);
		}
		int end = comment.length();
		while (comment.substring(Math.max(0, end - 4), end).equals("<br>")) {
			end -= 4;
		}
		comment = new String(comment.substring(0, end));

		spannedComment = (Spannable) Html.fromHtml(comment);

		
		m1 = url_match.matcher(spannedComment);
		while (m1.find()) {
			mHyperLinks.add(m1.group());
		}
	}

	public boolean hasExif() {
		return exif_camera != null;
	}

	public String[] getCameraExif() {
		return exif_camera;
	}

	public String[] getImageExif() {
		return exif_image;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public Timestamp getSqlTimestamp() {
		return timestamp_obj;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		timestamp_obj = new Timestamp(timestamp * 1000);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = HTMLEntities.unhtmlentities(filename);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getThreadId() {
		return thread_id;
	}

	public void setThreadId(int thread_id) {
		this.thread_id = thread_id;
	}

	public String getPosterId() {
		return poster_id;
	}

	public void setPosterId(String poster_id) {
		this.poster_id = poster_id;
	}

	public boolean isMod() {
		return isMod;
	}

	public void setMod(boolean isMod) {
		this.isMod = isMod;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getTripcode() {
		return tripcode;
	}

	public void setTripcode(String tripcode) {
		this.tripcode = tripcode;
	}

	public int getThWidth() {
		return th_width;
	}

	public void setThWidth(int th_width) {
		this.th_width = th_width;
	}

	public int getThHeight() {
		return th_height;
	}

	public void setThHeight(int th_height) {
		this.th_height = th_height;
	}

	public int getOmittedPosts() {
		return omitted_posts;
	}

	public int getOmittedImages() {
		return omitted_images;
	}

	public void setOmitted(int omitted_posts, int omitted_images) {
		this.omitted_posts = omitted_posts;
		this.omitted_images = omitted_images;
	}

	public boolean hasOmitted() {
		return (omitted_images + omitted_posts > 0);
	}

	public boolean isTooLong() {
		return isTooLong;
	}

	public void setTooLong(boolean isTooLong) {
		this.isTooLong = isTooLong;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean isSticky() {
		return isSticky;
	}

	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}

	private static final String getBetween(String start, String end,
			String haystack, int start_from) {
		return haystack.substring(
				(start_from = haystack.indexOf(start, start_from)
						+ start.length()), haystack.indexOf(end, start_from));
	}

	public String getFilesize() {
		return filesize;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public boolean isSpoiler() {
		return isSpoiler;
	}

	public void setSpoiler(boolean isSpoiler) {
		this.isSpoiler = isSpoiler;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public void setFile(boolean hasFile) {
		this.hasFile = hasFile;
	}

	public boolean isFileDeleted() {
		return fileDeleted;
	}

	public void setFileDeleted(boolean fileDeleted) {
		this.fileDeleted = fileDeleted;
	}

	public boolean hasImage() {
		return hasFile && !fileDeleted;
	}

	public boolean isBanned() {
		return isBanned;
	}

	public void setBanned(boolean isBanned) {
		this.isBanned = isBanned;
	}

	public String getIdenIcon() {
		return idenIcon;
	}

	public void setIdenIcon(String idenIcon) {
		this.idenIcon = idenIcon;
	}

	public void addReply(int id) {
		if (replies == null) {
			replies = new ArrayList<Integer>();
		}
		replies.add(id);
	}
	
	public List<Integer> getReplies() {
		return replies;
	}
	
	public boolean hasReplies() {
		return replies != null && replies.size() != 0;
	}
	
	// Android Parcelable
	
	public Post(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(tripcode);
		dest.writeString(email);
		dest.writeString(subject);
		dest.writeInt(mGreenTextColor);
		dest.writeString(comment);
		dest.writeLong(timestamp);
		dest.writeInt(id);
		dest.writeString(thumbnail);
		dest.writeString(image);
		dest.writeString(filename);
		dest.writeString(filesize);
		dest.writeInt(hasFile ? 1 : 0);
		dest.writeInt(fileDeleted ? 1 : 0);
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeInt(th_width);
		dest.writeInt(th_height);
		dest.writeInt(thread_id);
		dest.writeString(poster_id);
		dest.writeInt(omitted_posts);
		dest.writeInt(omitted_images);
		dest.writeInt(isMod ? 1 : 0);
		dest.writeInt(isAdmin ? 1 : 0);
		dest.writeInt(isTooLong ? 1 : 0);
		dest.writeInt(isLocked ? 1 : 0);
		dest.writeInt(isSticky ? 1 : 0);
		dest.writeInt(isSpoiler ? 1 : 0);
		dest.writeInt(isBanned ? 1 : 0);
		dest.writeString(flag);
		if (replies != null && replies.size() != 0) {
			dest.writeInt(replies.size());
			int[] repliesArr = new int[replies.size()];
			for (int i = 0; i < repliesArr.length; i++)
				repliesArr[i] = replies.get(i);
			dest.writeIntArray(repliesArr);
		} else {
			dest.writeInt(0);
			// dest.writeIntArray(null);
		}
		dest.writeInt(exif_camera == null ? 0 : exif_camera.length);
		if (exif_camera != null && exif_camera.length != 0)
			dest.writeStringArray(exif_camera);
		dest.writeInt(exif_image == null ? 0 : exif_image.length);
		if (exif_image != null && exif_image.length != 0)
			dest.writeStringArray(exif_image);
	}

	public void readFromParcel(Parcel in) {
		name = in.readString();
		tripcode = in.readString();
		email = in.readString();
		subject = in.readString();
		mGreenTextColor = in.readInt();
		comment = in.readString();
		parseComment();
		timestamp = in.readLong();
		id = in.readInt();
		thumbnail = in.readString();
		image = in.readString();
		filename = in.readString();
		filesize = in.readString();
		hasFile = in.readInt() == 1;
		fileDeleted = in.readInt() == 1;
		width = in.readInt();
		height = in.readInt();
		th_width = in.readInt();
		th_height = in.readInt();
		thread_id = in.readInt();
		poster_id = in.readString();
		omitted_posts = in.readInt();
		omitted_images = in.readInt();
		isMod = in.readInt() == 1;
		isAdmin = in.readInt() == 1;
		isTooLong = in.readInt() == 1;
		isLocked = in.readInt() == 1;
		isSticky = in.readInt() == 1;
		isSpoiler = in.readInt() == 1;
		isBanned = in.readInt() == 1;
		flag = in.readString();
		int sz = in.readInt();
		if (sz > 0) {
			replies = new ArrayList<Integer>();
			int[] r = new int[sz];
			in.readIntArray(r);
			for (int i = 0; i < sz; i++)
				replies.add(r[i]);
		}

		int exif_camera_length = in.readInt();
		if (exif_camera_length != 0)
			in.readStringArray(exif_camera);

		int exif_image_length = in.readInt();
		if (exif_image_length != 0)
			in.readStringArray(exif_image);
		
		timestamp_obj = new Timestamp(timestamp * 1000);
	}
	

	public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
		@Override
		public Post createFromParcel(Parcel in) {
			return new Post(in);
		}

		@Override
		public Post[] newArray(int size) {
			return new Post[size];
		}
	};

}
