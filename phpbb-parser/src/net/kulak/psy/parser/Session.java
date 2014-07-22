package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public abstract class Session {

	public static interface TopicsPageCallback {
		void processTopicsPage(String html) throws IOException, ParseException, SQLException;
	}
	
	private final String url;
	private final String user;
	private final String password;
	private final Configuration config;

	public Session(String url, String user, String password, Configuration config) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.config = config;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public Configuration getConfig() {
		return config;
	}

	public abstract List<String> readTopic(String id) throws IOException; 
	public abstract List<String> listTopics(int from, Date since) throws IOException, ParseException;
	public abstract void listTopics(int from, Date since, TopicsPageCallback callback) throws IOException, ParseException, SQLException; 
}
