package net.kulak.psy.statistics;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import net.kulak.psy.data.converters.MatrixConverter;
import net.kulak.psy.data.schema.Interaction;

/**
 * Triad significance profile (TSP) statistics. Will output a TSV with 11 rows
 * and 13 columns in each. Each column corresponds to one of the triads. The
 * first row is calculated for the original network, while the rest 10 are
 * calculated against the random graphs with the same degree distribution.
 * Calculation can take considerable time, depending on the network size. No TSV
 * header is included in output.
 */
public class StatTriads extends AbstractStatistics {

	public StatTriads(String[] args) throws Exception {
		super(args);
	}

	public static void main(String[] args) throws Exception {
		new StatTriads(args).run(0);
	}

	private static final Random RAND = new Random();

	public void run(int minMessages) throws SQLException, ClassNotFoundException, IOException {
		List<Interaction> inters = getDatabase().getAllInteractionsForPeriod(new Date(0), new Date(), 0);
		Set<String> usersAll = getDatabase().getAllUsers();
		Map<String, Map<String, Integer>> rec = calculateReciprocity(inters, usersAll);
		
		// Limit number of iterated users
		Set<String> users = new TreeSet<String>();
		for (Entry<String, Map<String, Integer>> e: rec.entrySet()) {
			for (Entry<String, Integer> f: e.getValue().entrySet()) {
				if (f.getValue() >= minMessages) {
					users.add(f.getKey());
					users.add(e.getKey());
				}
			}
		}

		boolean[][] adj = MatrixConverter.toAdjacencyMatrix(rec, users, 0);
		outputRow(calculateTriadsAdj(adj));

		for(int k = 0; k < 10; ++k) {
			boolean[][] adjrand = createRandomGraph(adj, adj.length * adj.length);
			outputRow(calculateTriadsAdj(adjrand));
		}
	}

	private static int findRandom1InRow(boolean[][] adj, int row) {
		int n = 0;
		for (int j = 0; j < adj.length; ++j) {
			if (adj[row][j]) {
				++n;
			}
		}
		n = n > 0 ? RAND.nextInt(n) : 0;
		for (int j = 0; j < adj.length; ++j) {
			if (adj[row][j]) {
				if(n-- == 0) {
					return j;
				}
			}
		}
		return -1;
	}
	
	private static int findRandom0InCol(boolean[][] adj, int col) {
		int n = 0;
		for (int i = 0; i < adj.length; ++i) {
			if (!adj[i][col]) {
				++n;
			}
		}
		n = RAND.nextInt(n);
		for (int i = 0; i < adj.length; ++i) {
			if (!adj[i][col]) {
				if(n-- == 0) {
					return i;
				}
			}
		}
		return -1;
	}

	private static boolean[][] createRandomGraph(boolean[][] adj, int iters) {
		int sz = adj.length;
		boolean[][] res = new boolean[sz][sz];
		
		for (int i = 0; i < sz; ++i) {
			for (int j = 0; j < sz; ++j) {
				res[i][j] = adj[i][j];
			}
		}

		int col = 0, row = 0, rowOld = 0;
		for (int k = 0; k < iters; ++k) {
			rowOld = row;
			while ((col = findRandom1InRow(res, row)) == -1) {
				row = RAND.nextInt(sz);
			}
			while ((row = findRandom0InCol(res, col)) == -1) {
				col = RAND.nextInt(sz);
			}
			res[rowOld][col] = false;
			res[row][col] = true;
		}
		
		return res;
	}
	
	private static int[] calculateTriadsAdj(boolean[][] adj) {
		int sz = adj.length;
		
		int[] triads = new int[13];
		for (int i = 0; i < 13; ++i) {
			triads[i] = 0;
		}

		boolean u1u2, u1u3, u2u1, u2u3, u3u1, u3u2;
		
		for (int i = 0; i < sz; ++i) {
			for (int j = 0; j < sz; ++j) {
				for (int k = 0; k < sz; ++k) {
					if (i != j && i != k && j != k) {
						u1u2 = adj[i][j];
						u2u1 = adj[j][i];
						u1u3 = adj[i][k];
						u3u1 = adj[k][i];
						u2u3 = adj[j][k];
						u3u2 = adj[k][j];
		
						triads[0]  += ( u1u2 &&  u1u3 && !u2u1 && !u2u3 && !u3u1 && !u3u2 ? 1 : 0);
						triads[1]  += (!u1u2 && !u1u3 &&  u2u1 && !u2u3 &&  u3u1 && !u3u2 ? 1 : 0);
						triads[2]  += ( u1u2 && !u1u3 && !u2u1 &&  u2u3 && !u3u1 && !u3u2 ? 1 : 0);
						triads[3]  += ( u1u2 && !u1u3 && !u2u1 &&  u2u3 && !u3u1 &&  u3u2 ? 1 : 0);
						triads[4]  += ( u1u2 && !u1u3 &&  u2u1 &&  u2u3 && !u3u1 && !u3u2 ? 1 : 0);
						triads[5]  += ( u1u2 && !u1u3 &&  u2u1 &&  u2u3 && !u3u1 &&  u3u2 ? 1 : 0);
						triads[6]  += ( u1u2 &&  u1u3 && !u2u1 &&  u2u3 && !u3u1 && !u3u2 ? 1 : 0);
						triads[7]  += ( u1u2 && !u1u3 && !u2u1 &&  u2u3 &&  u3u1 && !u3u2 ? 1 : 0);
						triads[8]  += ( u1u2 &&  u1u3 && !u2u1 &&  u2u3 && !u3u1 &&  u3u2 ? 1 : 0);
						triads[9]  += ( u1u2 &&  u1u3 && !u2u1 && !u2u3 &&  u3u1 &&  u3u2 ? 1 : 0);
						triads[10] += ( u1u2 &&  u1u3 && !u2u1 &&  u2u3 &&  u3u1 && !u3u2 ? 1 : 0);
						triads[11] += ( u1u2 &&  u1u3 &&  u2u1 &&  u2u3 && !u3u1 &&  u3u2 ? 1 : 0);
						triads[12] += ( u1u2 &&  u1u3 &&  u2u1 &&  u2u3 &&  u3u1 &&  u3u2 ? 1 : 0);
					}
				}
			}
		}
		
		return triads;
	}

	private static void outputRow(int[] triads) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("" + triads[0]);
		for (int i = 1; i < triads.length; ++i) {
			sb.append("\t" + triads[i]);
		}
		System.out.println(sb);
	}
}
