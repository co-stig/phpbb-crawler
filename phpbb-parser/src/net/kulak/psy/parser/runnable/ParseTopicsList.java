package net.kulak.psy.parser.runnable;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.kulak.psy.data.access.ConnectionProperties;
import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.parser.Configuration;
import net.kulak.psy.parser.PhpbbSession;
import net.kulak.psy.parser.Processor;
import net.kulak.psy.parser.Processor.TopicCallback;

public class ParseTopicsList {

	public static void main(String[] args) throws IOException, ParseException, SQLException, ClassNotFoundException {
		if (args.length < 9) {
			System.out.println("Insufficient number of arguments. Usage:");
			System.out.println("java net.kulak.psy.parser.runnable.ParseTopicsList FORUM_URL FORUM_LANG FORUM_USER FORUM_PASSWORD SINCE");
			System.out.println("                                                   JDBC_URL JDBC_DRIVER JDBC_USER JDBC_PASSWORD");
			System.out.println("Where FORUM_URL is forum's URL, e.g. http://forums.neons.org");
			System.out.println("      FORUM_LANG is forum's language, e.g. en");
			System.out.println("      FORUM_USER and FORUM_PASSWORD are login credentials");
			System.out.println("      SINCE is the date since which to parse new topics, e.g. 2011-03-20");
			System.out.println("      JDBC_URL is database connection URL, e.g. jdbc:oracle:thin:@127.0.0.1:1521:xe");
			System.out.println("      JDBC_DRIVER is database driver, e.g. oracle.jdbc.driver.OracleDriver");
			System.out.println("      JDBC_USER and JDBC_PASSWORD are database user credentials");
		} else {
			Configuration config = new Configuration(args[1]);
			parseTopicsList(
					Processor.getInstance(
						config,
						new PhpbbSession(args[0], args[2], args[3], config), 
						Database.getDatabase(new ConnectionProperties(args[7], args[8], args[5], args[6]))
					),
					new SimpleDateFormat("yyyy-MM-dd").parse(args[4])
				);
		}
	}

	private static void parseTopicsList(Processor processor, Date since) throws IOException, ParseException, SQLException {
		processor.listTopics(0, since, new TopicCallback() {
			@Override
			public void processTopic(Topic t, boolean processed) throws IOException, ParseException, SQLException {
				System.out.println((processed ? "Adding: " : "Exists: ") + t.getId() + ": " + t.getTitle());
			}
		});
	}
}
