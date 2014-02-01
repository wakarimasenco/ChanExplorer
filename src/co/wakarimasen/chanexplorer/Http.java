package co.wakarimasen.chanexplorer;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


import co.wakarimasen.chanexplorer.imageboard.Board;

import com.google.common.io.CharStreams;

public class Http {
	
	public final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                 .toCharArray();
	private final static String lineEnd = "\r\n";
	private final static String twoHyphens = "--";
	
	static {
	   CookieManager cookieManager = new CookieManager();
	   CookieHandler.setDefault(cookieManager);	   
	}
	
	public static InputStream getRequest(String url, String referer) throws MalformedURLException, IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url))
				.openConnection();
		InputStream in = new HttpStream(new BufferedInputStream(
				urlConnection.getInputStream()), urlConnection);
		return in;
	}

	public static String getRequestAsString(String url, String referer)
			throws MalformedURLException, IOException {
		InputStream in = null;
		try {
			in = getRequest(url, referer);
			return CharStreams.toString(new InputStreamReader(in, "UTF-8"));
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	public static InputStream postRequest(String url, Map<String, String> values, String referer) throws IOException {
		return postRequest(url, MapToString(values), referer);
	}
	
	public static InputStream postRequest(String url, String params, String referer) throws IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url))
				.openConnection();
		urlConnection.setFixedLengthStreamingMode(params.getBytes().length);
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Content-Type", 
		           "application/x-www-form-urlencoded");
		urlConnection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(params.getBytes().length));
		urlConnection.setUseCaches(false);
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		
		
		DataOutputStream wr = new DataOutputStream (
				urlConnection.getOutputStream());
	    wr.writeBytes (params);
	    wr.flush();
	    wr.close();
	    
	    InputStream in = new HttpStream(new BufferedInputStream(
				urlConnection.getInputStream()), urlConnection);
	    return in;
	}
	
	public static String postRequestAsString(String url, Map<String, String> values, String referer) throws IOException {
		return postRequestAsString(url, MapToString(values), referer);
	}
	
	public static String postRequestAsString(String url, String values, String referer) throws IOException {
		InputStream in = null;
		try {
			in = postRequest(url, values, referer);
			return CharStreams.toString(new InputStreamReader(in, "UTF-8"));
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	public static InputStream multipartRequest(String url, List<HttpMultiPart> values, String Referer, List<HttpCookie> cookies) throws MalformedURLException, IOException {
		String boundary = generateBoundary();
		
		HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url))
				.openConnection();
		
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		if (cookies != null) {
			for (HttpCookie cookie : cookies) {
				if (cookie != null) {
					((CookieManager)CookieHandler.getDefault()).getCookieStore().add(null, cookie);
				}
			}
		}
		
		//urlConnection.setRequestProperty("Transfer-Encoding", "chunked");
		
		urlConnection.setUseCaches(false);
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		
		//urlConnection.setChunkedStreamingMode(0);
		long sz = (twoHyphens + boundary + lineEnd).length();
		for (HttpMultiPart value : values) { 
			sz += value.estimateSize();
			sz += (twoHyphens + boundary + lineEnd).length();
		}
		urlConnection.setFixedLengthStreamingMode((int)sz);
		DataOutputStream wr = new DataOutputStream (
				urlConnection.getOutputStream());
		
		wr.writeBytes(twoHyphens + boundary + lineEnd);
		for (HttpMultiPart value : values) {
			value.writeHeader(wr);
			value.writeData(wr);
			wr.writeBytes(twoHyphens + boundary + lineEnd);
			value.closeStream();
		}
		
		wr.flush();
	    wr.close();
	    
	    InputStream in = new HttpStream(new BufferedInputStream(
				urlConnection.getInputStream()), urlConnection);
	    return in;
	}
	
	public static String multipartRequestAsString(String url, List<HttpMultiPart> values, String referer, List<HttpCookie> cookies) throws IOException {
		InputStream in = null;
		try {
			in = multipartRequest(url, values, referer, cookies);
			return CharStreams.toString(new InputStreamReader(in, "UTF-8"));
		} finally {
			if (in != null)
				in.close();
		}
	}
	
	protected static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
        	buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
   }
	
	private static String MapToString(Map<String, String> map) {
		StringBuilder params = new StringBuilder();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (params.length() > 0) {
				params.append('&');
			}
			try {
				params.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {}
		}
		return params.toString();
	}
	
	public static String newPost(NewPost np, String passToken, Board b, int threadId) throws IOException {
		List<HttpMultiPart> postData = new ArrayList<HttpMultiPart>();
		postData.add(new HttpMultiPart("MAX_FILE_SIZE", np.getMaxFileSize()));
		postData.add(new HttpMultiPart("mode", "regist"));
		if (threadId != -1) {
			postData.add(new HttpMultiPart("resto", Integer.valueOf(threadId).toString()));
		}
		postData.add(new HttpMultiPart("email", np.getEmail()));
		postData.add(new HttpMultiPart("name", np.getName()));
		postData.add(new HttpMultiPart("sub", np.getSubject()));
		postData.add(new HttpMultiPart("com", np.getComment()));
		
		if (np.getFile().length() != 0) {
			File upload = new File(np.getFile());
			InputStream is = new FileInputStream(upload);
			postData.add(new HttpMultiPart("upfile", upload.getName(), is, upload.length()));
		}
		postData.add(new HttpMultiPart("pwd", np.getPassword()));
		
		List<HttpCookie> cookies = null;
		
		if (passToken == "" || passToken == null) {
			postData.add(new HttpMultiPart("recaptcha_response_field", "manual_challenge"));
			postData.add(new HttpMultiPart("recaptcha_challenge_field", np.getCaptchaChallenge()));
		} else {
			cookies = new ArrayList<HttpCookie>();
			HttpCookie cookie = new HttpCookie("pass_enabled", "1");
			cookie.setDomain(".4chan.org");
			cookie.setPath("/");
			cookie.setVersion(0);
			cookies.add(cookie);
			
			cookie = new HttpCookie("pass_id", passToken);
			cookie.setDomain(".4chan.org");
			cookie.setPath("/");
			cookie.setVersion(0);
			cookies.add(cookie);
		}
		
		String url = String.format("https://sys.4chan.org/%s/post", b.getId());
		String ref;
		if (threadId == -1) {
			ref = String.format("http://boards.4chan.org/%s/res/%d", b.getId(), threadId);
		} else {
			ref = String.format("http://boards.4chan.org/%s", b.getId());
		}
		
		return multipartRequestAsString(url, postData, ref, cookies);
	}
	
	public static class HttpMultiPart {
		private InputStream in = null;
		private String data = null;
		private String name = null;
		private String filename = null;
		private long filesize = 0;
		
	
		public HttpMultiPart(String name, String filename, InputStream in, long filesize) { 
			this.in = in;
			this.data = null;
			this.name = name;
			this.filename = filename;
			this.filesize = filesize;
		}
		
		public HttpMultiPart(String name, String data) { 
			this.in = null;
			this.data = data;
			this.name = name;
			this.filename = null;
		}
		
		public String getHeader() {
			StringBuilder builder = new StringBuilder();
			
			builder.append("Content-Disposition: form-data; name=\"");
			builder.append(name);
			builder.append("\"");
			if (filename != null) {
				builder.append(";filename=\"");
				builder.append(filename);
				builder.append("\"");
			}
			builder.append(lineEnd);
			if (in == null) {
				builder.append("Content-Type: text/plain;charset=UTF-8" + lineEnd);
				builder.append("Content-Length: " + data.getBytes().length + lineEnd);
			} else {
				builder.append("Content-Type: application/octet-stream" + lineEnd);
				builder.append("Content-Length: " + filesize + lineEnd);
			}
			builder.append(lineEnd);
			return builder.toString();
		}
		
		public void writeHeader(DataOutputStream os) throws IOException {
			os.writeBytes(getHeader());
		}
		
		public void writeData(DataOutputStream os) throws IOException {
			if (in == null) {
				os.writeBytes(data);
			} else{
				byte[] buffer = new byte[8*1024];
				int bytesRead;
				while ((bytesRead = in.read(buffer)) > -1) {
			        os.write(buffer, 0, bytesRead);
			    }
			}
			os.writeBytes(lineEnd);
		}
		
		public void closeStream() throws IOException {
			if (in != null) {
				in.close();
			}
		}
		
		public long estimateSize() {
			if (in == null) {
				int dz = data.length();
				try {
					dz = data.getBytes("UTF-8").length;
				} catch (UnsupportedEncodingException e) {
				}
				return getHeader().length() + dz + lineEnd.length();
			} else {
				return getHeader().length() + filesize + lineEnd.length();
			}
		}
	}
	
	public static class HttpStream extends InputStream {
		private InputStream in;
		private HttpURLConnection urlConnection;
		
		public HttpStream(InputStream in, HttpURLConnection urlConnection) {
			this.in = in;
			this.urlConnection = urlConnection;
		}

		public int length() {
			return urlConnection.getContentLength();
		}
		
		@Override
		public int read() throws IOException {
			return in.read();
		}
		
		@Override
		public int read(byte[] b)  throws IOException {
			return in.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return in.read(b, off, len);
		}
		
		@Override
		public long skip(long n) throws IOException {
			return in.skip(n);
		}
		
		@Override
		public void close() throws IOException {
			in.close();
			urlConnection.disconnect();
		}
		
	}
	
	public static final class Chan {
		public static final String threadURL(Board b) {
			return threadURL(b, -1);
		}
		public static final String threadURL(Board b, int threadId) {
			if (threadId == -1) {
				return String.format("http://boards.4chan.org/%s", b.getId());
			} else {
				return String.format("http://boards.4chan.org/%s/res/%d", b.getId(), threadId);
			}
		}
	}

}
