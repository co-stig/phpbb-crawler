package net.kulak.psy.data.converters;

import java.util.Map;
import java.util.Set;

public class MatrixConverter {

	public static boolean[][] toAdjacencyMatrix(Map<String, Map<String, Integer>> reciprocity, Set<String> users, int minMessages) {
		int sz = users.size();
		boolean[][] res = new boolean[sz][sz];
		for (int i = 0; i < sz; ++i) {
			for (int j = 0; j < sz; ++j) {
				res[i][j] = false;
			}
		}

		int i = 0;
		int j = 0;
		for (String u: users) {
			j = 0;
			for (String v: users) {
				Map<String, Integer> t = reciprocity.get(u);
				if (t != null) {
					Integer n = t.get(v);
					if (n != null && n >= minMessages) {
						res[i][j] = true;
					}
				}
				++j;
			}
			++i;
		}
		
		return res;
	}

}
