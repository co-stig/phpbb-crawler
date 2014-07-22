package net.kulak.psy.semantic;

import java.util.Date;

import net.kulak.psy.data.schema.Payload;

public class DummyRandomInteractionAnalyzer extends InteractionAnalyzer {

	public Payload analyze(String text) {
		Payload res = new Payload();
		res.setType("A");
		res.setTimestamp(new Date());
		
		int r = (int)(Math.random() * 4.0);
		switch (r) {
			case 0: res.setValue("U"); break;
			case 1: res.setValue("P"); break;
			case 2: res.setValue("N"); break;
			default: res.setValue("E"); break;
		}
		
		return res;
	}

}
