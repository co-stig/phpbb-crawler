package net.kulak.psy.data.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kulak.psy.data.schema.Interaction;

public class ListConverter {

	public static Map<String, List<Interaction>> groupInteractionsByUser(List<Interaction> inters) {
		Map<String, List<Interaction>> res = new HashMap<String, List<Interaction>>();
		for(Interaction i: inters) {
			String from = i.getFromUserId();
			List<Interaction> userInts = res.get(from);
			if (userInts == null) {
				userInts = new ArrayList<Interaction>();
				res.put(from, userInts);
			}
			userInts.add(i);
		}
		return res;
	}

}
