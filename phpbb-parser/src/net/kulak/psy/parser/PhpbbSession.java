package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class PhpbbSession extends net.kulak.psy.parser.Session {

	private static final int PARSE_TIME = 5000;

	private DefaultHttpClient client = new DefaultHttpClient();
	
	private String getSid() {
		for (Cookie c: client.getCookieStore().getCookies()) {
			if (c.getName().equals("phpbb3_sp14j_sid")) {
				return c.getValue();
			}
		}
		return null;
	}

	private void login(String url, String user, String password) throws IllegalStateException, ClientProtocolException, IOException {
		client.execute(new HttpGet(url + "/ucp.php?mode=login")).getEntity().getContent().close();
		String sid = getSid();
		HttpPost req = new HttpPost(url + "/ucp.php?mode=login&sid=" + sid);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("login", getConfig().getLoginText()));
		formparams.add(new BasicNameValuePair("password", password));
		formparams.add(new BasicNameValuePair("redirect", "./ucp.php?mode=login&sid=" + sid));
		formparams.add(new BasicNameValuePair("redirect", "index.php"));
		formparams.add(new BasicNameValuePair("sid", sid));
		formparams.add(new BasicNameValuePair("username", user));
		req.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
		// Assume successful authentication - no checks
		client.execute(req).getEntity().getContent().close();
	}
	
	public PhpbbSession(String url, String user, String password, Configuration config) throws ClientProtocolException, IOException {
		super(url, user, password, config);
		login(url, user, password);
	}

	private String getTopicUrl(String id, int start) {
		String[] realId = id.split("-");
		return getUrl() + "/viewtopic.php?f=" + realId[0] + "&t=" + realId[1] + "&start=" + start + "&view=print";
	}
	
	private String getTopicsListUrl(int start) {
		return getUrl() + "/search.php?st=0&sk=t&sd=d&sr=topics&search_id=active_topics&start=" + start;
	}
	
	@Override
	public List<String> readTopic(String id) throws IOException {
		System.out.print("Reading topic " + id + ": ");
		List<String> res = new ArrayList<String>();
		String html = EntityUtils.toString(client.execute(new HttpGet(getTopicUrl(id, 0))).getEntity());
		res.add(html);
		Matcher m = getConfig().getPageCount().matcher(html);
		if (m.find()) {
			int pages = Integer.parseInt(m.group(1));
			System.out.print("found " + pages + " pages. Parsing: .");
			for (int i = 1; i < pages; ++i) {
				try { Thread.sleep(PARSE_TIME); } catch (InterruptedException e) { }
				html = EntityUtils.toString(client.execute(new HttpGet(getTopicUrl(id, i * 25))).getEntity());
				res.add(html);
				System.out.print(".");
			}
			System.out.println(" done.");
		} else {
			System.out.println("fail.");
		}
		return res;
	}

	@Override
	public List<String> listTopics(int from, Date since) throws IOException, ParseException {
		System.out.print("Reading topics list: ");
		List<String> res = new ArrayList<String>();
		int i = from;
		while (true) {
			try { Thread.sleep(PARSE_TIME); } catch (InterruptedException e) { }
			String html = EntityUtils.toString(client.execute(new HttpGet(getTopicsListUrl((i++) * 50))).getEntity());
			Matcher m = getConfig().getTopicDate().matcher(html);
			String date = null;
			while (m.find()) {
				date = m.group(1);
			}
			if (date != null) {
				res.add(html);
				System.out.print(".");
				Date last = parseTimestamp(date);
				if (last.before(since)) {
					break;
				}
			} else {
				System.out.println("fail.");
				break;
			}
		}
		System.out.println(" done. Next page: " + i);
		return res;
	}

	@Override
	public void listTopics(int from, Date since, TopicsPageCallback callback) throws IOException, ParseException, SQLException {
		System.out.print("Reading topics list: ");
		int i = from;
		while (true) {
			try { Thread.sleep(PARSE_TIME); } catch (InterruptedException e) { }
			String html = EntityUtils.toString(client.execute(new HttpGet(getTopicsListUrl((i++) * 50))).getEntity());
			Matcher m = getConfig().getTopicDate().matcher(html);
			String date = null;
			while (m.find()) {
				date = m.group(1);
			}
			if (date != null) {
				callback.processTopicsPage(html);
				System.out.print(".:" + date);
				Date last = parseTimestamp(date);
				if (last.before(since)) {
					break;
				}
			} else {
				System.out.println("fail.");
				break;
			}
		}
		System.out.println(" done. Next page: " + i);
	}
	
	public Date parseTimestamp(String date) throws ParseException {
		Matcher m = getConfig().getDateStringFormat().matcher(date);
		m.matches();
		String month = getConfig().getMonths().get(m.group(1));
		return getConfig().getTimestampFormat().parse(month + m.group(2));
	}

}
