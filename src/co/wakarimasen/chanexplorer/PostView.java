package co.wakarimasen.chanexplorer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import co.wakarimasen.chanexplorer.ImageCache.ImageWaiter;
import co.wakarimasen.chanexplorer.imageboard.Board;
import co.wakarimasen.chanexplorer.imageboard.Post;

import co.wakarimasen.chanexplorer.R;

@SuppressWarnings("deprecation")
public class PostView extends LinearLayout implements ImageWaiter {

	private Post mPost;
	private PostMainView mMainPost;
	private RelativeLayout mOptions;
	private RelativeLayout mInfo;
	//private LinearLayout mMain;
	private LinearLayout mPosterImgCont;
	private TextView mPosterName;
	private TextView mPosterTrip;
	private TextView mPosterId;
	private TextView mPosterSubject;
	private TextView mPosterTime;
	private TextView mPosterNo;
	private ImageView mPosterFlag;
	private TextView mPosterFileInfo;
	private PostImage mPosterImage;
	private TextView mPosterTextA;
	private TextView mPosterTextB;
	private TextView mPosterTooLong;
	private TextView mPosterOmitted;
	private ImageView mSticky;
	private ImageView mLocked;
	private ImageView mIdenIcon;
	private LinearLayout mStickyLock;
	private ImageButton mPosterMore;
	private TextView mPosterBanned;
	private TextView mPosterReplies;
	private LinearLayout mBtns;
	private Button mThreadBtn;
	private Button mImageBtn;
	private Button mQuoteBtn;
	private Button mCopyBtn;
	public Button mLinksBtn;
	private Button mEmailBtn;
	public Button mRepliesBtn;

	private PostSlideAnimation mAnimSlideIn;
	private PostSlideOutAnimation mAnimSlideOut;
	private boolean mOptionsShown;
	private Spanned mComment;
	protected String mImageKey;
	private String mFlagKey;
	private String mIdenKey;
	private boolean mLoadingImage;
	private FlagWaiter mFlagWaiter = new FlagWaiter();
	private IdenWaiter mIdenWaiter = new IdenWaiter();
	private EnsureView mEv = null;
	private boolean mThread = false;
	private SharedPreferences mSettings;
	private int mFontSize = -1;
	private boolean mTablet = false;
	private int m16dp;
	//private boolean mHeaderInvalidated = false;
	//private boolean mCommentInvalidated = false;
	
	
	private WindowManager mWinMgr = null;
	//int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
	
	private Theme mTheme = null;

	public final static int MODE_LINKS = 1;
	public final static int MODE_REPLY = 2;
	private int mode = MODE_LINKS;

	private ThreadNavigator mNavigator = null;

	private Resources mRes;
	private OnQuoteClickListener mQuoteClick = null;
	

	private final static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy(EEE)HH:mm");

	public final static Pattern num_match = Pattern.compile("([0-9]+)");
	

	private class FlagWaiter implements ImageWaiter {

		@Override
		public void onLoadedImage(String key, Bitmap image) {

			if (key.equals(mFlagKey)) {
				setFlag(image);
			}

		}
	}

	private class IdenWaiter implements ImageWaiter {

		@Override
		public void onLoadedImage(String key, Bitmap image) {

			if (key.equals(mIdenKey)) {
				setIden(image);
			}

		}
	}

	private static final Interpolator sInterpolator = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};

	public PostView(Context context) {
		super(context);
	}

	public PostView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		
		mTablet = getTag() != null && getTag().equals("tablet");
		m16dp = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5f);
		mWinMgr = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		
		mRes = getResources();
		mOptionsShown = false;
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());
		//mFontSize = mSettings.getInt(PrefsActivity.KEY_TEXT_SZ, 0);

		mMainPost = (PostMainView) findViewById(R.id.main_post);
		//mMain = (LinearLayout) findViewById(R.id.poster_top_main);
		mOptions = (RelativeLayout) findViewById(R.id.post_options);
		mInfo = (RelativeLayout) findViewById(R.id.poster_info);
		mPosterName = (TextView) findViewById(R.id.poster_name);
		mPosterTrip = (TextView) findViewById(R.id.poster_trip);
		mPosterId = (TextView) findViewById(R.id.poster_id);
		mPosterSubject = (TextView) findViewById(R.id.poster_subject);
		mPosterTime = (TextView) findViewById(R.id.poster_time);
		mPosterNo = (TextView) findViewById(R.id.poster_no);
		mPosterFlag = (ImageView) findViewById(R.id.poster_flag);
		mPosterFileInfo = (TextView) findViewById(R.id.poster_file_info);
		mPosterImgCont = (LinearLayout) findViewById(R.id.poster_img_container);
		mPosterImage = (PostImage) findViewById(R.id.poster_image);
		mPosterTextA = (TextView) findViewById(R.id.poster_text_a);
		mPosterTextB = (TextView) findViewById(R.id.poster_text_b);
		mPosterTooLong = (TextView) findViewById(R.id.poster_too_long);
		mPosterOmitted = (TextView) findViewById(R.id.poster_omitted);
		mPosterMore = (ImageButton) findViewById(R.id.poster_more);
		mPosterBanned = (TextView) findViewById(R.id.poster_banned);
		mPosterReplies = (TextView) findViewById(R.id.poster_replies);

		mBtns = (LinearLayout) findViewById(R.id.btn_layout);
		mThreadBtn = (Button) findViewById(R.id.btn_thread);
		mImageBtn = (Button) findViewById(R.id.btn_image);
		mQuoteBtn = (Button) findViewById(R.id.btn_quote);
		mCopyBtn = (Button) findViewById(R.id.btn_copy);
		mLinksBtn = (Button) findViewById(R.id.btn_links);
		mEmailBtn = (Button) findViewById(R.id.btn_email);
		mRepliesBtn = (Button) findViewById(R.id.btn_replies);

		mStickyLock = (LinearLayout) findViewById(R.id.poster_stickylock);
		mSticky = (ImageView) findViewById(R.id.poster_sticky);
		mLocked = (ImageView) findViewById(R.id.poster_locked);
		mIdenIcon = (ImageView) findViewById(R.id.poster_iden_icon);


		mAnimSlideIn = new PostSlideAnimation(mOptions, 500);
		mAnimSlideIn.setInterpolator(sInterpolator);
		// mAnimSlideIn.setFillAfter(true);

		mAnimSlideOut = new PostSlideOutAnimation(mOptions, 500);
		mAnimSlideOut.setInterpolator(sInterpolator);
		// mAnimSlideOut.setFillAfter(true);

		mPosterMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOptionsShown) {
					mOptions.startAnimation(mAnimSlideOut.restart());
					mOptionsShown = false;
					//mMainPost.enableTouch();

				} else {
					mOptions.setVisibility(View.VISIBLE);
					mOptions.startAnimation(mAnimSlideIn.restart());
					mOptionsShown = true;
					//mMainPost.disableTouch(this);
				}
			}

		});
		
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mNavigator.isThread() && mOptions.getVisibility() != View.VISIBLE) {
					mNavigator.gotoPost(mNavigator.getBoard(), mPost.getThreadId(), mPost.getId(), false, true);
				}
				
			}
		});

		mPosterTextA.setMovementMethod(LinkMovementMethod.getInstance());
		mPosterTextB.setMovementMethod(LinkMovementMethod.getInstance());
		mPosterReplies.setMovementMethod(LinkMovementMethod.getInstance());

		setWillNotDraw(false);
		if (!isInEditMode())
			setTheme(Theme.Holo);
		mPosterImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		mPosterImage.setPost(this);

		mThreadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mNavigator != null) {
					mNavigator.gotoPost(mNavigator.getBoard(),
							mPost.getThreadId(), mPost.getId(), false, false);
				}
			}
		});
		mImageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNavigator.viewImage(mPost.getId());

			}
		});
		mPosterImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNavigator.viewImage(mPost.getId());

			}
		});
		mQuoteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNavigator.addQuote(mPost.getId());
				mOptions.startAnimation(mAnimSlideOut.restart());
				mOptionsShown = false;
				mMainPost.enableTouch();

			}
		});
		mCopyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getContext();
				ClipboardManager clipboard = (ClipboardManager) getContext()
						.getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(mComment.toString());
				Toast.makeText(getContext(), "Copied post.", Toast.LENGTH_SHORT)
						.show();

			}
		});
		// mLinksBtn.setOnClickListener();
		mEmailBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { mPost.getEmail() });
				emailIntent.setType("text/plain");
				getContext().startActivity(
						Intent.createChooser(emailIntent,
								"Email " + mPost.getEmail()));

			}
		});
	}

	public void setNavigator(ThreadNavigator navigator) {
		mNavigator = navigator;
	}
	
	public void threadAction() {
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mNavigator != null && mOptions.getVisibility() != View.VISIBLE) {
					mNavigator.gotoPost(mNavigator.getBoard(),
							mPost.getThreadId(), mPost.getId(), false, false);
				}
			}
		});
	}

	public void setPost(Post post, boolean isThread) {
		if (mOptionsShown) {
			mOptions.setVisibility(View.GONE);
			mOptionsShown = false;
			mMainPost.enableTouch();
		}
		mThread = isThread;
		mPost = post;
		// mPosterName.setText(HTMLEntities.unhtmlentities(mPost.getName()));
		if (mPost.isAdmin()) {
			mPosterName.setText(mPost.getName() + " ## Admin");
		} else if (mPost.isMod()) {
			mPosterName.setText(mPost.getName() + " ## Mod");
		} else {
			mPosterName.setText(HTMLEntities.unhtmlentities(mPost.getName()));
		}

		if (mPost.getPosterId() != null) {
			mPosterId.setText("(ID: " + mPost.getPosterId() + ")");
			mPosterId.setVisibility(View.VISIBLE);
		} else {
			mPosterId.setVisibility(View.GONE);
		}

		if (mPost.getTripcode() != null) {
			mPosterTrip.setText(mPost.getTripcode());
			mPosterTrip.setVisibility(View.VISIBLE);
		} else {
			mPosterTrip.setVisibility(View.GONE);
		}
		if (mPost.getSubject().length() == 0) {
			mPosterSubject.setVisibility(View.GONE);
		} else {
			mPosterSubject.setVisibility(View.VISIBLE);
			mPosterSubject.setText(HTMLEntities.unhtmlentities(mPost
					.getSubject()));
		}
		mPosterTime.setText(sdf.format(mPost.getSqlTimestamp()));
		mPosterNo.setText(String.format("No. %d", mPost.getId()));

		if (mPost.hasFile()) {
			mPosterFileInfo.setVisibility(View.VISIBLE);
			if (mPost.isFileDeleted())
				mPosterFileInfo.setText("File deleted.");
			else
				mPosterFileInfo.setText(String.format("File: (%s, %dx%d, %s)",
						mPost.getFilesize(), mPost.getWidth(),
						mPost.getHeight(), mPost.getFilename()));
		} else {
			mPosterFileInfo.setVisibility(View.GONE);
		}
		if (mPost.hasImage()) {
			mImageKey = mPost.getThumbnail();
			if (mImageKey.startsWith("//")) {
				mImageKey = String.format("http:%s", mImageKey);
			}
			setImage(ImageCache.get(mImageKey, this));
			mPosterImage.setVisibility(View.VISIBLE);
			mPosterImage.getLayoutParams().width = mPost.getThWidth();
			mPosterImage.getLayoutParams().height = mPost.getThHeight();
			//mPosterImage.setMinimumWidth(mPost.getThWidth());
			//mPosterImage.setMinimumHeight(mPost.getThHeight());
		} else {
			mPosterImage.setVisibility(View.GONE);
			mPosterImage.setImageDrawable(null);
			mLoadingImage = false;
		}
		if (mPost.getFlag() != null) {
			mFlagKey = mPost.getFlag();
			if (mFlagKey.startsWith("//")) {
				mFlagKey = String.format("http:%s", mFlagKey);
			}
			setFlag(ImageCache.get(mFlagKey, mFlagWaiter));
		} else {
			mFlagKey = null;
			mPosterFlag.setImageDrawable(null);
		}

		if (mPost.getIdenIcon() != null) {
			mIdenKey = mPost.getIdenIcon();
			if (mIdenKey.startsWith("//")) {
				mIdenKey = String.format("http:%s", mIdenKey);
			}
			setIden(ImageCache.get(mIdenKey, mIdenWaiter));
		} else {
			mIdenKey = null;
			mIdenIcon.setImageDrawable(null);
		}

		if (mPost.isSticky() || mPost.isLocked()) {
			mStickyLock.setVisibility(View.VISIBLE);
			mSticky.setVisibility((mPost.isSticky()) ? View.VISIBLE : View.GONE);
			mLocked.setVisibility((mPost.isLocked()) ? View.VISIBLE : View.GONE);
		} else {
			mStickyLock.setVisibility(View.GONE);
		}

		mComment = parseComment(mPost.getComment());

		if (mPost.isThread()) {
			mMainPost.setBackgroundColor(mTheme.bg_color);
			if (mTablet) {
				setPadding(0, getPaddingTop(), m16dp*6, getPaddingBottom());
				mMainPost.setPadding(mMainPost.getPaddingLeft(), mMainPost.getPaddingTop(), mMainPost.getPaddingRight(), mMainPost.getPaddingBottom());
				//setPaddingLeft(0);
			}
		} else {
			mMainPost.setBackgroundResource(mTheme.reply_style);
			if (mTablet) {
				setPadding((int)(m16dp*1.5), getPaddingTop(), m16dp*6, getPaddingBottom());
				mMainPost.setPadding(mMainPost.getPaddingLeft(), mMainPost.getPaddingTop(), m16dp*2, m16dp);
			}
		}
		// mPosterTextB.setText(parseComment(mPost.getComment()));

		if (mPost.isTooLong()) {
			mPosterTooLong.setVisibility(View.VISIBLE);
			mPosterTooLong.setText("Comment too long.");
		} else {
			mPosterTooLong.setVisibility(View.GONE);
		}
		if (mPost.hasOmitted()) {
			mPosterOmitted.setVisibility(View.VISIBLE);
			if (mPost.getOmittedImages() > 0) {
				mPosterOmitted.setText(mRes.getQuantityString(
						R.plurals.imagePostOmitted,
						mPost.getOmittedPosts(),
						mPost.getOmittedPosts(),
						mRes.getQuantityString(R.plurals.imageOmitted,
								mPost.getOmittedImages(),
								mPost.getOmittedImages())));
			} else {
				mPosterOmitted.setText(mRes.getQuantityString(
						R.plurals.postOmitted, mPost.getOmittedPosts(),
						mPost.getOmittedPosts()));
			}
		} else {
			mPosterOmitted.setVisibility(View.GONE);
		}

		if (mPost.hasReplies() && mNavigator.getThreadId() != -1) {
			SpannableStringBuilder sb = new SpannableStringBuilder("Replies : ");
			for (Integer reply : mPost.getReplies()) {
				int start = sb.length();
				sb.append(String.format(">>%d", reply));
				int end = sb.length();
				// sb.setSpan(new ForegroundColorSpan(mTheme.quote_color),
				// start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				// sb.setSpan(new UnderlineSpan(), start, end,
				// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				sb.setSpan(new ReplySpan(reply), start, end,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				sb.append(" ");
			}
			mPosterReplies.setVisibility(View.VISIBLE);
			mPosterReplies.setText(sb);
		} else {
			mPosterReplies.setVisibility(View.GONE);
		}
		mPosterBanned.setVisibility((mPost.isBanned()) ? View.VISIBLE
				: View.GONE);

		mOptionsShown = false;
		//mHeaderInvalidated = true;
		mAnimSlideOut.force();
		setupButtons();
		requestLayout();
		//postInvalidate();
		//forceLayout();
		//renderComment(mPosterImage.getWidth(), mPosterImage.getHeight());

	}

	protected void renderComment(int imgWidth, int imgHeight) {
		int textWidth;
		if (mPost.hasImage() && imgWidth == 0) {
			return;
		}
		if (mPost.hasImage() && mComment.length() > 0) {
			mPosterTextA.setVisibility(View.VISIBLE);
			TextPaint tp = mPosterTextA.getPaint();
			
			if (mTablet) { 
				textWidth = mWinMgr.getDefaultDisplay().getWidth() - imgWidth - mPosterTextA.getPaddingLeft();
			} else {
				textWidth = mPosterImgCont.getWidth() - imgWidth
					- mPosterTextA.getPaddingLeft();
				if (textWidth <= 0 || true) {
				textWidth = mWinMgr.getDefaultDisplay().getWidth() - imgWidth - mPosterTextA.getPaddingLeft();

				}
			}
			Paint.FontMetricsInt metrics = tp.getFontMetricsInt();
			int fontHeight = metrics.bottom - metrics.top;
			int currHeight = 0;
			int chars = 0;
			int currStart = 0;

			while ((currHeight + fontHeight) < imgHeight
					&& chars < mComment.length()) {

				if (mComment.charAt(chars) == '\n') {
					currHeight += fontHeight;
					currStart = ++chars;
					continue;
				}
				chars++;
				float charsWidth = tp.measureText(mComment,
						(mComment.charAt(currStart) == ' ' || mComment
								.charAt(currStart) == '\n') ? currStart + 1
								: currStart, chars - 1);
				if (charsWidth >= textWidth) {
					int word_break;
					for (word_break = chars - 1; word_break > currStart; word_break--) {
						if (mComment.charAt(word_break) == ' '
								|| mComment.charAt(word_break) == '\t') {
							break;
						}
					}
					if (word_break == currStart) {
						word_break = chars - 1;
					}
					currHeight += fontHeight;
					chars = word_break + 1;
					// android.util.Log.d("WordWrap",
					// mComment.subSequence(currStart, chars).toString());
					currStart = word_break;

				}
			}
			if (chars == mComment.length()) {
				if (chars > 0)
					chars += mComment.charAt(chars - 1) == '\n' ? -1 : 0;
				mPosterTextA.setText(mComment.subSequence(0, chars));
				mPosterTextB.setVisibility(View.GONE);
			} else {
				mPosterTextB.setVisibility(View.VISIBLE);
				// while (mComment.charAt(chars) != ' ' &&
				// mComment.charAt(chars) != '\n' && mComment.charAt(chars) !=
				// '\t' && chars > 0) {
				// chars--;
				// }
				mPosterTextA.setText(mComment.subSequence(0, chars));
				// chars += mComment.charAt(chars) == '\n' ? 1 : 0;
				while (mComment.charAt(chars) == '\n' && (chars+1) < mComment.length())
					chars++;
				mPosterTextB.setText(mComment.subSequence(chars,
						mComment.length()));
			}
		} else {
			mPosterTextA.setVisibility(View.GONE);
			mPosterTextB.setVisibility(mComment.length() == 0 ? View.GONE : View.VISIBLE);
			mPosterTextB.setText(mComment);
			
		}
		//mMain.measure();
		//mMain.forceLayout();
		//android.util.Log.d("CHAN", ""+getWidth());
		//mMain.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED));
		//mMain.forceLayout();
		//forceLayout();
		//forceLayout();
		//mMain.requestLayout();
		//requestLayout();
		//mCommentInvalidated = true;
		//measure(0, 0);
		//requestLayout();
	}

	public Spanned parseComment(String comment) {
		Spannable spannedComment = mPost.getSpannedComment();
		// This isn't technically correct, but we shouldn't hit a case that
		// would
		// cause a QuoteSpan an a link thats not a quote;
		Object[] spans = spannedComment.getSpans(0, spannedComment.length(),
				Object.class);
		int qs = 0;
		for (Object o : spans) {
			if (o instanceof ForegroundColorSpan) {
				int color = ((ForegroundColorSpan) o).getForegroundColor() & 0xFFFFFF;
				if (color == 0xDD0000) {
					spannedComment.setSpan(new QuoteSpan(mPost.getLinks().get(qs++)),
							spannedComment.getSpanStart(o),
							spannedComment.getSpanEnd(o),
							spannedComment.getSpanFlags(o));
				}
			}
		}
		//
		return spannedComment;
	}

	public void setOnQuoteClickListener(OnQuoteClickListener listener) {
		mQuoteClick = listener;
	}

	public Post getPost() {
		return mPost;
	}

	public boolean hasLinks() {
		return (getQuotes().size() + getHyperLinks().size()) > 0;
	}

	public boolean hasReplies() {
		return mPost.hasReplies();
	}

	public List<String> getQuotes() {
		return mPost.getLinks();
	}

	public List<String> getHyperLinks() {
		return mPost.getHyperLinks();
	}

	public void setupButtons() {
		enableButton(mThreadBtn, !mThread);
		enableButton(mImageBtn, mPost.hasImage());
		enableButton(mLinksBtn, (getQuotes().size() + getHyperLinks().size()) > 0);
		enableButton(mEmailBtn, mPost.getEmail() != null);
		enableButton(mRepliesBtn, mPost.hasReplies());

		if (mPost.getEmail() != null) {
			// android.util.Log.d("Chan",
			// "Email "+mPost.getEmail()+" for post "+mPost.getId());
			mEmailBtn.setText(mPost.getEmail());
		}
		for (int i = 0; i < mBtns.getChildCount(); i++) {
			if (mBtns.getChildAt(i).getVisibility() == View.VISIBLE) {
				if (mBtns.getChildAt(i).getId() == View.NO_ID) {
					mBtns.getChildAt(i).setVisibility(View.GONE);
				}
				break;
			}
		}
	}

	private void enableButton(Button btn, boolean enabled) {
		if (enabled) {
			btn.setVisibility(View.VISIBLE);

			if (mBtns.getChildAt(mBtns.indexOfChild(btn) - 1) != null)
				mBtns.getChildAt(mBtns.indexOfChild(btn) - 1).setVisibility(
						View.VISIBLE);
		} else {
			btn.setVisibility(View.GONE);
			if (mBtns.getChildAt(mBtns.indexOfChild(btn) - 1) != null)
				mBtns.getChildAt(mBtns.indexOfChild(btn) - 1).setVisibility(
						View.GONE);
		}

	}

	public void setEnsureView(EnsureView ev) {
		mEv = ev;
	}

	@Override
	public void onLoadedImage(String key, Bitmap image) {
		if (key.equals(mImageKey)) {
			setImage(image);
		}
	}

	protected void setImage(Bitmap image) {
		if (ImageCache.empty(image) || image.isRecycled()) {
			mPosterImage.setImageResource(mTheme.progress_large);
			mLoadingImage = true;
		} else {
			mPosterImage.setImageBitmap(image);
			mLoadingImage = false;
		}
		requestLayout();
		//forceLayout();
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		requestLayout();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//final int width = MeasureSpec.getSize(widthMeasureSpec);
		//final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (isInEditMode()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		if (mPost.hasImage())
			mPosterImage.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), (MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)));
		mInfo.measure(widthMeasureSpec, (MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)));
		
		RelativeLayout.LayoutParams postTime = (RelativeLayout.LayoutParams) mPosterTime
				.getLayoutParams();
		RelativeLayout.LayoutParams postNo = (RelativeLayout.LayoutParams) mPosterNo
				.getLayoutParams();
		if (mIdenIcon.getRight() > mPosterTime.getLeft()) {
			postTime.addRule(RelativeLayout.BELOW, R.id.poster_name);
			postNo.addRule(RelativeLayout.BELOW, R.id.poster_name);
		} else {
			postTime.addRule(RelativeLayout.BELOW, 0);
			postNo.addRule(RelativeLayout.BELOW, 0);
		}
		mInfo.measure(widthMeasureSpec, (MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)));
		
		//android.util.Log.d("CHAN", "Width : "+mPosterImage.getMeasuredWidth()+" Heihgt:"+mPosterImage.getMeasuredHeight());
		renderComment(mPosterImage.getMeasuredWidth(), mPosterImage.getMeasuredHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		if (mLoadingImage) {
			LayerDrawable ld = (LayerDrawable) mPosterImage.getDrawable();
			for (int i = 0; i < ld.getNumberOfLayers(); i++) {
				ld.getDrawable(i)
						.setLevel(
								(int) (((int) (System.currentTimeMillis() % 4000) / (float) 4000) * 10000));
			}
		}
		/*if (mHeaderInvalidated) {
			RelativeLayout.LayoutParams postTime = (RelativeLayout.LayoutParams) mPosterTime
					.getLayoutParams();
			RelativeLayout.LayoutParams postNo = (RelativeLayout.LayoutParams) mPosterNo
					.getLayoutParams();

			if (mIdenIcon.getRight() > mPosterTime.getLeft()) {
				postTime.addRule(RelativeLayout.BELOW, R.id.poster_name);
				postNo.addRule(RelativeLayout.BELOW, R.id.poster_name);
			} else {
				postTime.addRule(RelativeLayout.BELOW, 0);
				postNo.addRule(RelativeLayout.BELOW, 0);
			}
			mHeaderInvalidated = mIdenIcon.getRight() == 0;
			//measure(0,0);
			//requestLayout();
			//postInvalidate();
			//mInfo.requestLayout();
			//mInfo.postInvalidate();
			// android.util.Log.d("CHAN",
			// "IdenRight: "+mIdenIcon.getRight()+", PosterLeft:"+mPosterTime.getLeft());
		}
		if (mCommentInvalidated) {
			//mCommentInvalidated = false;
			//requestLayout();
			//postInvalidate();
			//mMain.requestLayout();
			//mMain.postInvalidate();
		}*/
		super.onDraw(canvas);
	}

	protected void setFlag(Bitmap image) {
		if (ImageCache.empty(image)) {
			mPosterFlag.setImageDrawable(null);
		} else {
			mPosterFlag.setImageBitmap(image);
		}
	}

	protected void setIden(Bitmap image) {
		if (ImageCache.empty(image)) {
			mIdenIcon.setImageDrawable(null);
		} else {
			mIdenIcon.setImageBitmap(image);
		}
	}

	public void setTheme(Theme t) {
		
		if (mFontSize != Integer.parseInt(mSettings.getString(PrefsActivity.KEY_TEXT_SZ, "0"))) {
			mFontSize = Integer.parseInt(mSettings.getString(PrefsActivity.KEY_TEXT_SZ, "0"));
			switch (mFontSize) {
			case 1:
				mPosterTextA.setTextSize(14);
				mPosterTextB.setTextSize(14);
				break;
			default:
				mPosterTextA.setTextSize(12);
				mPosterTextB.setTextSize(12);
			}
		}
		
		if (t.equals(mTheme))
			return;
		mTheme = t;
		if (mPost != null && mPost.isThread()) {
			mMainPost.setBackgroundColor(mTheme.bg_color);
		} else {
			mMainPost.setBackgroundResource(mTheme.reply_style);
		}
		mPosterTextA.setTextColor(mTheme.text_color);
		mPosterTextB.setTextColor(mTheme.text_color);
		mPosterName.setTextColor(mTheme.name_color);
		mPosterTrip.setTextColor(mTheme.name_color);
		mPosterSubject.setTextColor(mTheme.subject_color);

		mPosterId.setTextColor(mTheme.text_color);
		mPosterTime.setTextColor(mTheme.text_color);
		mPosterNo.setTextColor(mTheme.text_color);
		mPosterFileInfo.setTextColor(mTheme.text_color);
		mPosterTooLong.setTextColor(mTheme.text_color);
		mPosterOmitted.setTextColor(mTheme.text_color);
		mPosterReplies.setTextColor(mTheme.text_color);
		mPosterMore.setImageResource(mTheme.post_more);
		if (mLoadingImage) {
			mPosterImage.setImageResource(mTheme.progress_large);
		}
		
		

	}

	public int getContextMode() {
		return mode;
	}

	public void setContextMode(int mode) {
		this.mode = mode;
	}

	private class ReplySpan extends ClickableSpan {
		int mTarget;

		public ReplySpan(int target) {
			mTarget = target;
		}

		@Override
		public void onClick(View widget) {
			Board b = mNavigator.getBoard();
			int thread = mNavigator.getThreadId();
			int post = mTarget;
			mNavigator.gotoPost(b, thread, post, true, false);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(true);
			ds.setColor(mTheme.quote_color);
		}
	}

	private class QuoteSpan extends ClickableSpan {
		String mTarget;

		public QuoteSpan(String target) {
			mTarget = target;
		}

		@Override
		public void onClick(View widget) {
			if (mQuoteClick != null) {
				mQuoteClick.onClick(widget, mTarget);
			}
			Board b = mNavigator.getBoard();
			int thread = mNavigator.getThreadId();
			int post = -1;
			if (mTarget.indexOf('#') != -1) {
				post = Integer
						.parseInt(mTarget.substring(mTarget.indexOf('#') + 2));
			}
			if (mTarget.startsWith("/")) {
				b = Board.getBoardById(mTarget.substring(1,
						mTarget.indexOf('/', 1)));
			}
			Matcher m1 = num_match.matcher(mTarget);
			if (m1.find()) {
				thread = Integer.parseInt(m1.group(1));
			}
			mNavigator.gotoPost(b, thread, post, true, false);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(true);
			ds.setColor(mTheme.quote_color);
		}
	}

	public interface OnQuoteClickListener {
		public abstract void onClick(View v, String target);
	}

	protected class PostSlideAnimation extends Animation {
		private View mAnimatedView;
		private LayoutParams mViewLayoutParams;
		private int mMarginStart, mMarginEnd;
		// private boolean mIsVisibleAfter = false;
		private boolean mWasEndedAlready = false;

		/**
		 * Initialize the animation
		 * 
		 * @param view
		 *            The layout we want to animate
		 * @param duration
		 *            The duration of the animation, in ms
		 */
		public PostSlideAnimation(View view, int duration) {

			setDuration(duration);
			mAnimatedView = view;
			mViewLayoutParams = (LayoutParams) view.getLayoutParams();

			// decide to show or hide the view
			// mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

			mMarginStart = mViewLayoutParams.bottomMargin;
			mMarginEnd = (mMarginStart == 0 ? (0 - view.getHeight()) : 0);

			view.setVisibility(View.VISIBLE);
		}

		public PostSlideAnimation restart() {
			if (mEv != null) {
				mEv.ensureStart(PostView.this);
			}
			mWasEndedAlready = false;
			return this;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			int change = (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

			if (interpolatedTime < 1.0f) {

				// Calculating the new bottom margin, and setting it
				mViewLayoutParams.bottomMargin = mMarginStart
						+ (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

				// Invalidating the layout, making us seeing the changes we made
				mAnimatedView.requestLayout();

				// Making sure we didn't run the ending before (it happens!)
			} else if (!mWasEndedAlready) {
				mViewLayoutParams.bottomMargin = mMarginEnd;
				mAnimatedView.requestLayout();

				// if (mIsVisibleAfter) {
				// mAnimatedView.setVisibility(View.GONE);
				// }
				mWasEndedAlready = true;
			}
			if (mEv != null) {
				mEv.ensureView(PostView.this, change);
			}
		}
	}

	protected class PostSlideOutAnimation extends Animation {
		private View mAnimatedView;
		private LayoutParams mViewLayoutParams;
		public int mMarginStart, mMarginEnd;
		private boolean mWasEndedAlready = false;

		/**
		 * Initialize the animation
		 * 
		 * @param view
		 *            The layout we want to animate
		 * @param duration
		 *            The duration of the animation, in ms
		 */
		public PostSlideOutAnimation(View view, int duration) {

			setDuration(duration);
			mAnimatedView = view;
			mViewLayoutParams = (LayoutParams) view.getLayoutParams();

			// decide to show or hide the view
			// mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

			mMarginStart = 0;
			mMarginEnd = mViewLayoutParams.bottomMargin;

		}

		public PostSlideOutAnimation restart() {
			if (mEv != null) {
				mEv.ensureStart(PostView.this);
			}
			mWasEndedAlready = false;
			return this;
		}

		public void force() {
			mViewLayoutParams.bottomMargin = mMarginEnd;
			mAnimatedView.requestLayout();

			mAnimatedView.setVisibility(View.GONE);
			mWasEndedAlready = true;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			int change = (int) ((mMarginEnd - mMarginStart) * interpolatedTime);
			if (interpolatedTime < 1.0f) {

				// Calculating the new bottom margin, and setting it
				mViewLayoutParams.bottomMargin = mMarginStart
						+ (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

				// Invalidating the layout, making us seeing the changes we made
				mAnimatedView.requestLayout();

				// Making sure we didn't run the ending before (it happens!)
			} else if (!mWasEndedAlready) {
				mViewLayoutParams.bottomMargin = mMarginEnd;
				mAnimatedView.requestLayout();

				mAnimatedView.setVisibility(View.GONE);
				mWasEndedAlready = true;
			}
			if (mEv != null) {
				mEv.ensureView(PostView.this, change);
			}
		}
	}

	public abstract static class EnsureView {
		public abstract void ensureStart(View v);

		public abstract void ensureView(View v, int change);
	}

}
