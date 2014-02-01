package co.wakarimasen.chanexplorer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class NewPost implements Parcelable {
	private String mName = "";
	private String mSubject = "";
	private String mEmail = "";
	private String mComment = "";
	private boolean mSpoiler;
	private String mCaptcha = "";
	private String mCaptchaToken = "";
	private String mFile = "";
	private String mPassword = "";
	private boolean mThreadClosed;
	private boolean mCaptchaUrlLoaded;
	private String mRefCaptchaUrl = "";
	private String mMaxSize = "0";
	private String mCaptchaFinal;
	
	private final String max_start = "name=\"MAX_FILE_SIZE\" value=\"";
	private final String max_end = "\"";
	
	private final String form_start = "<iframe src=\"";
	private final String form_end = "\"";
	
	private final String cap_start = "src=\"image?c=";
	private final String cap_end = "\"";
	
	public NewPost(String name, String email, String password) {
		mName = name;
		mEmail = email;
		mPassword = password;
	}
	
	public NewPost(Parcel in) {
		readFromParcel(in);
	}
	
	public NewPost(NewPost np) {
		mName = np.getName();
		mSubject = np.getSubject();
		mEmail = np.getEmail();
		mComment = np.getComment();
		mSpoiler = np.isSpoiler();
		mCaptcha = np.getCaptcha();
		mCaptchaToken = np.getCaptchaToken();
		mFile = np.getFile();
		mPassword = np.getPassword();
		mThreadClosed = np.isThreadClosed();
		mCaptchaUrlLoaded = np.mCaptchaUrlLoaded;
		mRefCaptchaUrl = np.getCapchaRef();
		mMaxSize = np.getMaxFileSize();
		mCaptchaFinal = np.mCaptchaFinal;
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(String subject) {
		this.mSubject = subject;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		this.mComment = comment;
	}

	public boolean isSpoiler() {
		return mSpoiler;
	}

	public void setSpoiler(boolean spoiler) {
		this.mSpoiler = spoiler;
	}

	public String getCaptcha() {
		return mCaptcha;
	}

	public void setCaptcha(String mCaptcha) {
		this.mCaptcha = mCaptcha;
	}

	public String getCaptchaToken() {
		return mCaptchaToken;
	}
	
	public String getCaptchaChallenge() {
		return mCaptchaFinal;
	}
	
	public void setCaptchaChallenge(String cf) {
		mCaptchaFinal = cf;
	}
	
	public String getCapchaRef() {
		return mRefCaptchaUrl;
	}

	public String getFile() {
		return mFile;
	}

	public void setFile(String file) {
		this.mFile = file;
	}
	
	public boolean hasFile() {
		return mFile != null && mFile.length() != 0;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}
	
	public void reset() {
		mComment = "";
		mSpoiler = false;
		mCaptcha = "";
		mCaptchaToken = null;
		mFile = "";
	}
	
	public String getMaxFileSize() {
		return mMaxSize;
	}
	
	// MAKES A HTTP REQUEST
	// NOT ASYNC
	public void getCaptchaInfo(String boardHtml, String ref) {
		int start_from = boardHtml.indexOf("<form name=\"post\"");
		mMaxSize = boardHtml.substring((start_from = boardHtml.indexOf(max_start, start_from)+max_start.length()), boardHtml.indexOf(max_end, start_from));
		start_from = boardHtml.indexOf("<form name=\"post\"");
		//android.util.Log.d("Captcha", "-->"+start_from);
		mThreadClosed = start_from == -1;
		if (!mThreadClosed) {
			mRefCaptchaUrl = boardHtml.substring((start_from = boardHtml.indexOf(form_start, start_from)+form_start.length()), boardHtml.indexOf(form_end, start_from));
			if (mRefCaptchaUrl.startsWith("//")) {
				mRefCaptchaUrl = String.format("http:%s", mRefCaptchaUrl);
			}
			
			try {
				//android.util.Log.d("Captcha", mRefCaptchaUrl);
				String captchaData = Http.getRequestAsString(mRefCaptchaUrl, ref);
				
				mCaptchaToken =  captchaData.substring((start_from = captchaData.indexOf(cap_start, 0)+cap_start.length()), captchaData.indexOf(cap_end, start_from));
				return;
			} catch (MalformedURLException e) {
				mThreadClosed = true;
				return;
			} catch (IOException e) {
				mThreadClosed = true;
				return;
			}
			
		}		
	}
	
	public boolean reloadCaptcha() {
		int start_from = 0;
		try {
			String captchaData = Http.getRequestAsString(mRefCaptchaUrl, "");
			mCaptchaToken =  captchaData.substring((start_from = captchaData.indexOf(cap_start, start_from)+cap_start.length()), captchaData.indexOf(cap_end, start_from));
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isThreadClosed() {
		return mThreadClosed;
	}
	
	public boolean isCaptchaReady() {
		return mCaptchaUrlLoaded;
	}
	
	public abstract class CaptchaWaiter {
		public abstract void onCaptcha(String url);
	}
	
	public static String getPassword(SharedPreferences settings) {
		String pass = settings.getString(PrefsActivity.KEY_DEFAULT_PASS, null);
		if (pass == null) {
			pass = generatePass();
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PrefsActivity.KEY_DEFAULT_PASS, pass);
			editor.commit();
		}
		return pass;
	}
	
	protected static String generatePass() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11);
        for (int i = 0; i < count; i++) {
        	buffer.append(Http.MULTIPART_CHARS[rand.nextInt(Http.MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
   }
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mEmail);
		dest.writeString(mComment);
		dest.writeInt(mSpoiler ? 1:0);
		dest.writeString(mCaptcha);
		dest.writeString(mCaptchaToken);
		dest.writeString(mFile);
		dest.writeString(mPassword);
		dest.writeInt(mThreadClosed ?1:0);
		dest.writeString(mRefCaptchaUrl);
	}
	
	public void readFromParcel(Parcel in) {
		mName = in.readString();
		mEmail = in.readString();
		mComment = in.readString();
		mSpoiler = in.readInt() == 1;
		mCaptcha = in.readString();
		mCaptchaToken = in.readString();
		mFile = in.readString();
		mPassword = in.readString();
		mThreadClosed = in.readInt() == 1;
		mRefCaptchaUrl = in.readString();
	}
	
	public static final Parcelable.Creator<NewPost> CREATOR = new Parcelable.Creator<NewPost>() {
        @Override
		public NewPost createFromParcel(Parcel in) {
            return new NewPost(in);
        }
 
        @Override
		public NewPost[] newArray(int size) {
            return new NewPost[size];
        }
    };

}
