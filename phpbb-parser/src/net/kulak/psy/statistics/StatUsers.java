package net.kulak.psy.statistics;

import java.io.IOException;
import java.sql.SQLException;

import net.kulak.psy.data.schema.UserStatistic;

/**
 * Users statistics. Will output a TSV with 4 columns: user name, first message
 * date in format yyyy-MM-dd, last message in format yyyy-MM-dd, total number of
 * messages wrote by this user. No header is included in output.
 */
public class StatUsers extends AbstractStatistics {

	public StatUsers(String[] args) throws Exception {
		super(args);
	}

	public static void main(String[] args) throws Exception {
		new StatUsers(args).run();
	}
	
	public void run() throws SQLException, ClassNotFoundException, IOException {
		for (UserStatistic us: getDatabase().statUsers()) {
			System.out.println(new StringBuilder().
					append(us.getName()).append("\t").
					append(us.getFirstMessageAsString()).append("\t").
					append(us.getLastMessageAsString()).append("\t").
					append(us.getCount())
				);
		}
	}
}
