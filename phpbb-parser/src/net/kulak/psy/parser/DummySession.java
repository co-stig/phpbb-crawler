package net.kulak.psy.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummySession extends net.kulak.psy.parser.Session {

	public DummySession(String url, String user, String password, Configuration config) {
		super(url, user, password, config);
	}

	@Override
	public List<String> readTopic(String id) throws IOException {
		return readFile(getConfig().getDummyFolder() + File.pathSeparator + id + ".htm");
	}

	// TODO: This is ugly!
	private List<String> readFile(String resource) throws FileNotFoundException, IOException {
		String res = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(
					getClass().getResource(resource).getFile()
				));
			String line;
			while ((line = br.readLine()) != null) {
				res += line;
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		ArrayList<String> r = new ArrayList<String>();
		r.add(res);
		return r;
	}

	@Override
	public List<String> listTopics(int from, Date since) throws IOException {
		return readFile(getConfig().getDummyFolder() + File.pathSeparator + "list.htm");
	}

	@Override
	public void listTopics(int from, Date since, TopicsPageCallback callback) throws IOException, ParseException, SQLException {
		for (String html: readFile(getConfig().getDummyFolder() + File.pathSeparator + "list.htm")) {
			callback.processTopicsPage(html);
		}
	}
}
