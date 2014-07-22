package net.kulak.psy.statistics;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Degree distribution statistics. Will generate TSV output with three columns:
 * user name, in degree, out degree. TSV output has no header, only raw data.
 */
public class StatDegree extends AbstractStatistics {

	public StatDegree(String[] args) throws Exception {
		super(args);
	}

	public void run() throws SQLException {
		Map<String, Integer> inDegree = getDatabase().statInDegree();
		Map<String, Integer> outDegree = getDatabase().statOutDegree();
		
		Set<String> users = new TreeSet<String>(outDegree.keySet());
		users.addAll(inDegree.keySet());
		
		for (String user: users) {
			Integer in = inDegree.get(user);
			Integer out = outDegree.get(user);
			System.out.println(user + "\t" + (in == null ? 0 : in) + "\t" + (out == null ? 0 : out));
		}
	}
	
	public static void main(String[] args) throws Exception {
		new StatDegree(args).run();
	}
	
}
