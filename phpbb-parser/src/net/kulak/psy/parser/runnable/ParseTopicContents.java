package net.kulak.psy.parser.runnable;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import net.kulak.psy.data.access.ConnectionProperties;
import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.parser.Configuration;
import net.kulak.psy.parser.PhpbbSession;
import net.kulak.psy.parser.Processor;

public class ParseTopicContents {

	public static void main(String[] args) throws IOException, ParseException, SQLException, ClassNotFoundException {
		if (args.length < 8) {
			System.out.println("Insufficient number of arguments. Usage:");
			System.out.println("java net.kulak.psy.parser.runnable.ParseTopicContents FORUM_URL FORUM_LANG FORUM_USER FORUM_PASSWORD");
			System.out.println("                                                      JDBC_URL JDBC_DRIVER JDBC_USER JDBC_PASSWORD");
			System.out.println("Where FORUM_URL is forum's URL, e.g. http://forums.neons.org");
			System.out.println("      FORUM_LANG is forum's language, e.g. en");
			System.out.println("      FORUM_USER and FORUM_PASSWORD are login credentials");
			System.out.println("      JDBC_URL is database connection URL, e.g. jdbc:oracle:thin:@127.0.0.1:1521:xe");
			System.out.println("      JDBC_DRIVER is database driver, e.g. oracle.jdbc.driver.OracleDriver");
			System.out.println("      JDBC_USER and JDBC_PASSWORD are database user credentials");
		} else {
			Configuration config = new Configuration(args[1]);
			parseUnparsed(
					Processor.getInstance(
						config,
						new PhpbbSession(args[0], args[2], args[3], config), 
						Database.getDatabase(new ConnectionProperties(args[6], args[7], args[4], args[5]))
					)
				);
		}
	}

	private static void parseUnparsed(Processor processor) throws SQLException, ClassNotFoundException, IOException, ParseException {
		List<Topic> unparsed = Database.getDatabase().getUnparsedTopics();
		int i = 0;
		for (Topic t: unparsed) {
			++i;
			System.out.print(t.getTitle() + " (" + i + " / " + unparsed.size() + "): ");
			processor.parseTopic(t.getId());
		}
	}
}
