package net.kulak.psy.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class Configuration {

	private final Pattern topicTitle;
	private final Pattern messageSplitter;
	private final Pattern messageParser;
	private final Pattern bigQuote;
	private final Pattern smallQuote;
	private final Pattern topicSplitter;
	private final Pattern dateStringFormat;
	private final Pattern pageCount;
	private final Pattern topicDate;
	
	private final SimpleDateFormat timestampFormat;
	
	private final String loginText;
	private final String dummyFolder;
	
	private final Map<String, String> months = new HashMap<String, String>();
	
	public Configuration(String lang) throws IOException {
		Properties config = new Properties();
		InputStream is = getClass().getResourceAsStream("/net/kulak/psy/parser/parser-" + lang + ".properties");
		try {
			config.load(is);

			topicTitle = Pattern.compile(config.getProperty("regex.topicTitle"));
			messageSplitter = Pattern.compile(config.getProperty("regex.messageSplitter"));
			messageParser = Pattern.compile(config.getProperty("regex.messageParser"));
			bigQuote = Pattern.compile(config.getProperty("regex.bigQuote"));
			smallQuote = Pattern.compile(config.getProperty("regex.smallQuote"));
			topicSplitter = Pattern.compile(config.getProperty("regex.topicSplitter"));
			dateStringFormat = Pattern.compile(config.getProperty("regex.dateStringFormat"));
			pageCount = Pattern.compile(config.getProperty("regex.pageCount"));
			topicDate = Pattern.compile(config.getProperty("regex.topicDate"));

			timestampFormat = new SimpleDateFormat(config.getProperty("format.timestamp"));
			
			loginText = config.getProperty("text.login");
			dummyFolder = config.getProperty("folder.dummy");
			
			months.put(config.getProperty("month.jan"), "Jan");
			months.put(config.getProperty("month.feb"), "Feb");
			months.put(config.getProperty("month.mar"), "Mar");
			months.put(config.getProperty("month.apr"), "Apr");
			months.put(config.getProperty("month.may"), "May");
			months.put(config.getProperty("month.jun"), "Jun");
			months.put(config.getProperty("month.jul"), "Jul");
			months.put(config.getProperty("month.aug"), "Aug");
			months.put(config.getProperty("month.sep"), "Sep");
			months.put(config.getProperty("month.oct"), "Oct");
			months.put(config.getProperty("month.nov"), "Nov");
			months.put(config.getProperty("month.dec"), "Dec");
			
		} finally {
			is.close();
		}
	}

	public Pattern getTopicTitle() {
		return topicTitle;
	}

	public Pattern getMessageSplitter() {
		return messageSplitter;
	}

	public Pattern getMessageParser() {
		return messageParser;
	}

	public Pattern getBigQuote() {
		return bigQuote;
	}

	public Pattern getSmallQuote() {
		return smallQuote;
	}

	public Pattern getTopicSplitter() {
		return topicSplitter;
	}

	public Pattern getDateStringFormat() {
		return dateStringFormat;
	}

	public Pattern getPageCount() {
		return pageCount;
	}

	public Pattern getTopicDate() {
		return topicDate;
	}

	public SimpleDateFormat getTimestampFormat() {
		return timestampFormat;
	}

	public String getLoginText() {
		return loginText;
	}

	public Map<String, String> getMonths() {
		return months;
	}

	public String getDummyFolder() {
		return dummyFolder;
	}

}
