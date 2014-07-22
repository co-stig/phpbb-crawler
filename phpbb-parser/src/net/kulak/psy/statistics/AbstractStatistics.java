package net.kulak.psy.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kulak.psy.data.access.ConnectionProperties;
import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Interaction;

public abstract class AbstractStatistics {

	private final Database database;
	
	public AbstractStatistics(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Insufficient number of arguments. Please provide the following (in the given order):");
			System.out.println(" * JDBC_URL is database connection URL, e.g. jdbc:oracle:thin:@127.0.0.1:1521:xe");
			System.out.println(" * JDBC_DRIVER is database driver, e.g. oracle.jdbc.driver.OracleDriver");
			System.out.println(" * JDBC_USER database user name");
			System.out.println(" * JDBC_PASSWORD database user password");
			
			throw new Exception("Not enough arguments");
		} else {
			ConnectionProperties properties = new ConnectionProperties(args[2], args[3], args[0], args[1]);
			database = Database.getDatabase(properties);
		}
	}

	public Database getDatabase() {
		return database;
	}

	/*
	 * Some of the commonly used "helper" methods
	 */
	
	/**
	 * Transforms plain list of interactions to map of maps. I.e. for each user
	 * (first map key) the value map will contain a map between the interacting
	 * user and the corresponding number of interactions.
	 * 
	 * @param inters
	 * @param users
	 * @return
	 */
	protected static Map<String, Map<String, Integer>> calculateReciprocity(List<Interaction> inters, Set<String> users) {
		Map<String, Map<String, Integer>> rec = new HashMap<String, Map<String,Integer>>();
		for (Interaction i: inters) {
			String from = i.getFromUserId();
			String to = i.getToUserId();
			if (to != null && users.contains(to)) {
				Map<String, Integer> fm = rec.get(from);
				if (fm == null) {
					fm = new HashMap<String, Integer>();
					fm.put(to, 1);
					rec.put(from, fm);
				} else {
					Integer val = fm.get(to);
					if (val == null) {
						val = 0;
					}
					fm.put(to, ++val);
				}
			}
		}
		return rec;
	}
}
