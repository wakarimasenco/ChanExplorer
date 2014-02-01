package co.wakarimasen.chanexplorer;

import co.wakarimasen.chanexplorer.R;

public class Theme {
	public int action_icon;
	public int action_bar_bg;
	public int action_bar_text;
	public int ic_action_reply;
	public int ic_action_refresh;
	public int ic_action_close;
	public int ic_action_jump;
	public int pager_title_bg;
	public int pager_title_text;
	public int bg_color;
	public int title_color;
	public int text_color;
	public int fav_on;
	public int fav_off;
	public int pinned_header_bg;
	public int pinned_header_text;
	public int name_color;
	public int subject_color;
	public int quote_color;
	public int green_text;
	public int progress_large;
	public int reply_style;
	public int list_style;
	public int post_more;

	public final static Theme Holo = (new Theme() {

		public Theme define() {
			action_icon = R.drawable.ic_action_bar_holo;
			action_bar_bg = 0xFF000000;
			action_bar_text = 0xFFFFFFFF;
			ic_action_reply = R.drawable.ic_action_reply_holo;
			ic_action_refresh = R.drawable.ic_action_refresh_holo;
			ic_action_close = R.drawable.ic_action_close_holo;
			ic_action_jump = R.drawable.ic_action_jump_holo;
			pager_title_bg = 0xFF33b5e5;
			pager_title_text = 0xFFFFFFFF;
			bg_color = 0xFF000000;
			title_color = 0xFFFFFFFF;
			text_color = 0xFFFFFFFF;
			fav_on = R.drawable.ic_favorite_holo;
			fav_off = R.drawable.ic_favorite_empty_holo;
			pinned_header_bg = 0xff202020;
			pinned_header_text = 0xFFFFFFFF;
			name_color = 0xFF669900;
			subject_color = 0xFF0099CC;
			quote_color = 0xFFFF4444;
			green_text = 0x99CC00;
			progress_large = R.drawable.progress_large_holo;
			reply_style = R.drawable.post_style_holo;
			list_style = R.drawable.board_list_selector_holo;
			post_more = R.drawable.ic_post_more_holo;
			return this;
		}
	}).define();

	public final static Theme Photon = (new Theme() {

		public Theme define() {
			action_icon = R.drawable.ic_action_bar_photon;
			action_bar_bg = 0xFFEEEEEE;
			action_bar_text = 0xFF004A99;
			ic_action_reply = R.drawable.ic_action_reply_photon;
			ic_action_refresh = R.drawable.ic_action_refresh_photon;
			ic_action_close = R.drawable.ic_action_close_photon;
			ic_action_jump = R.drawable.ic_action_jump_photon;
			pager_title_bg = 0xFFFF6600;
			pager_title_text = 0xFFFFFFFF;
			bg_color = 0xFFEEEEEE;
			title_color = 0xFF004A99;
			text_color = 0xFF333333;
			fav_on = R.drawable.ic_favorite_photon;
			fav_off = R.drawable.ic_favorite_empty_photon;
			pinned_header_bg = 0xff004A99;
			pinned_header_text = 0xFFFFFFFF;
			name_color = 0xFF004A99;
			subject_color = 0xFF111111;
			quote_color = 0xFFFF6600;
			green_text = 0x789922;
			progress_large = R.drawable.progress_large_photon;
			reply_style = R.drawable.post_style_photon;
			list_style = R.drawable.board_list_selector_photon;
			post_more = R.drawable.ic_post_more_photon;
			return this;
		}
	}).define();
	public final static Theme YotsubaBlue = (new Theme() {

		public Theme define() {
			action_icon = R.drawable.ic_action_bar_yotsuba_blue;
			action_bar_bg = 0xFFD1D5EE;
			action_bar_text = 0xFFAF0A0F;
			ic_action_reply = R.drawable.ic_action_reply_yotsuba_blue;
			ic_action_refresh = R.drawable.ic_action_refresh_yotsuba_blue;
			ic_action_close = R.drawable.ic_action_close_yotsuba_blue;
			ic_action_jump = R.drawable.ic_action_jump_yotsuba_blue;
			pager_title_bg = 0xFFAF0A0F;
			pager_title_text = 0xFFFFFFFF;
			bg_color = 0xFFEEF2FF;
			title_color = 0xFFAF0A0F;
			text_color = 0xFF000000;
			fav_on = R.drawable.ic_favorite_yotsuba_blue;
			fav_off = R.drawable.ic_favorite_empty_yotsuba_blue;
			pinned_header_bg = 0xffD6DAF0;
			pinned_header_text = 0xFFAF0A0F;
			name_color = 0xFF117743;
			subject_color = 0xFF0F0C5D;
			quote_color = 0xFFDD0000;
			green_text = 0x789922;
			progress_large = R.drawable.progress_large_yotsuba_blue;
			reply_style = R.drawable.post_style_yotsuba_blue;
			list_style = R.drawable.board_list_selector_yotsuba_blue;
			post_more = R.drawable.ic_post_more_yotsuba_blue;
			return this;
		}
	}).define();
	public final static Theme Yotsuba = (new Theme() {

		public Theme define() {
			action_icon = R.drawable.ic_action_bar_yotsuba;
			action_bar_bg = 0xFFFED6B0;
			action_bar_text = 0xFF800000;
			ic_action_reply = R.drawable.ic_action_reply_yotsuba;
			ic_action_refresh = R.drawable.ic_action_refresh_yotsuba;
			ic_action_close = R.drawable.ic_action_close_yotsuba;
			ic_action_jump = R.drawable.ic_action_jump_yotsuba;
			pager_title_bg = 0xFF800000;
			pager_title_text = 0xFFFFFFFF;
			bg_color = 0xFFFFFFEE;
			title_color = 0xFF800000;
			text_color = 0xFF800000;
			fav_on = R.drawable.ic_favorite_yotsuba;
			fav_off = R.drawable.ic_favorite_empty_yotsuba;
			pinned_header_bg = 0xffF0E0D6;
			pinned_header_text = 0xFF800000;
			name_color = 0xFF117743;
			subject_color = 0xFFCC1105;
			quote_color = 0xFF000080;
			green_text = 0x789922;
			progress_large = R.drawable.progress_large_yotsuba;
			reply_style = R.drawable.post_style_yotsuba;
			list_style = R.drawable.board_list_selector_yotsuba;
			post_more = R.drawable.ic_post_more_yotsuba;
			return this;
		}
	}).define();
	public final static Theme Tomorrow = (new Theme() {

		public Theme define() {
			action_icon = R.drawable.ic_action_bar_tomorrow;
			action_bar_bg = 0xFF1D1F21;
			action_bar_text = 0xFFc5c8c6;
			ic_action_reply = R.drawable.ic_action_reply_tomorrow;
			ic_action_refresh = R.drawable.ic_action_refresh_tomorrow;
			ic_action_close = R.drawable.ic_action_close_tomorrow;
			ic_action_jump = R.drawable.ic_action_jump_tomorrow;
			pager_title_bg = 0xFF81a2be;
			pager_title_text = 0xFFe3e6e4;
			bg_color = 0xFF1D1F21;
			title_color = 0xFFc5c8c6;
			text_color = 0xFFc5c8c6;
			fav_on = R.drawable.ic_favorite_tomorrow;
			fav_off = R.drawable.ic_favorite_empty_tomorrow;
			pinned_header_bg = 0xff282a2e;
			pinned_header_text = 0xFFc5c8c6;
			name_color = 0xFFc5c8c6;
			subject_color = 0xFFb294bb;
			quote_color = 0xFF5F89AC;
			green_text = 0xb5bd68;
			progress_large = R.drawable.progress_large_tomorrow;
			reply_style = R.drawable.post_style_tomorrow;
			list_style = R.drawable.board_list_selector_tomorrow;
			post_more = R.drawable.ic_post_more_tomorrow;
			return this;
		}
	}).define();
	
	public static Theme getTheme (String theme, boolean worksafe) {
		if (theme == null) {
			theme = "";
		}
		if (theme.equals("Holo")) {
			return Holo;
		} else if (theme.equals("Yotsuba")) {
			return Yotsuba;
		} else if (theme.equals("Yotsuba B")) {
			return YotsubaBlue;
		} else if (theme.equals("Photon")) {
			return Photon;
		} else if (theme.equals("Tomorrow")) {
			return Tomorrow;
		} else {
			return (worksafe) ? YotsubaBlue : Yotsuba;
		}
	}
	
	public static Theme getTheme (String theme) {
		return getTheme(theme, false);
	}
	
	public static Theme validTheme (String theme) {
		if (theme == null) {
			return null;
		}
		if (theme.equals("Holo")) {
			return Holo;
		} else if (theme.equals("Yotsuba")) {
			return Yotsuba;
		} else if (theme.equals("Yotsuba B")) {
			return YotsubaBlue;
		} else if (theme.equals("Photon")) {
			return Photon;
		} else if (theme.equals("Tomorrow")) {
			return Tomorrow;
		} else {
			return null;
		}
	}

}
