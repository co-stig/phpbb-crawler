package net.kulak.psy.statistics;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kulak.psy.data.converters.MatrixConverter;
import net.kulak.psy.data.schema.Interaction;

/**
 * Distance distribution statistics. Outputs an adjacency matrix for the
 * relationship graph in TSV format. The actual distance calculation is
 * performed later in Mathematica.
 */
public class StatDistance extends AbstractStatistics {

	public StatDistance(String[] args) throws Exception {
		super(args);
	}

	public static void main(String[] args) throws Exception {
		new StatDistance(args).run();
	}

	public void run() throws SQLException {
		List<Interaction> inters = getDatabase().getAllInteractionsForPeriod(new Date(0), new Date(), 0);
		Set<String> users = getDatabase().getAllUsers();
		Map<String, Map<String, Integer>> rec = calculateReciprocity(inters, users);
		boolean[][] adj = MatrixConverter.toAdjacencyMatrix(rec, users, 0);
		
		int sz = adj.length;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sz; ++i) {
			for(int j = 0; j < sz; ++j) {
				sb.append((adj[i][j] ? 1 : 0) + (j == sz - 1 ? "" : "\t"));
			}
			sb.append((i == sz - 1 ? "" : "\n"));
		}
		
		System.out.println(sb);
	}
}
