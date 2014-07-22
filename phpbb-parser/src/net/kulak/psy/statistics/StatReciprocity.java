package net.kulak.psy.statistics;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.kulak.psy.data.schema.Interaction;

/**
 * Reciprocity statistics. Will output a TSV with 4 columns: from (username A),
 * to (username B), number of messages from A to B, number of messages from B to
 * A. Each pair of users will be listed only once. No header is included in
 * output.
 */
public class StatReciprocity extends AbstractStatistics {

	public StatReciprocity(String[] args) throws Exception {
		super(args);
	}

	public static void main(String[] args) throws Exception {
		new StatReciprocity(args).run();
	}
	
	public void run() throws SQLException, ClassNotFoundException, IOException {
		List<Interaction> inters = getDatabase().getAllInteractionsForPeriod(new Date(0), new Date(), 0);
		Set<String> users = getDatabase().getAllUsers();

		Map<String, Map<String, Integer>> rec = calculateReciprocity(inters, users);
		Set<String> observed = new TreeSet<String>();

		for (Entry<String, Map<String, Integer>> f: rec.entrySet()) {
			for (Entry<String, Integer> t: f.getValue().entrySet()) {
				String from = f.getKey();
				String to = t.getKey();
				if (
						!observed.contains(from + to) &&
						!observed.contains(to + from)
					) {
					Map<String, Integer> rev = rec.get(to);
					int forward = t.getValue();
					int reverse = rev == null ? 0 : (rev.get(from) == null ? 0 : rev.get(from));
					
					System.out.println(new StringBuilder().
							append(from).append("\t").
							append(to).append("\t").
							append(forward).append("\t").
							append(reverse)
						);
					
					observed.add(from + to);
				}
			}
		}
	}
}
