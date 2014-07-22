package net.kulak.psy.semantic;

import java.io.IOException;

import net.kulak.psy.data.schema.Payload;

public abstract class InteractionAnalyzer {
	
	private static InteractionAnalyzer inst;

	public static InteractionAnalyzer getInstance(boolean dummy) throws IOException {
		if (inst == null) {
			if (dummy) {
				inst = new DummyRandomInteractionAnalyzer();
			} else {
				inst = new PlainTextAnalyzer(new RegexpInteractionAnalyzer());
			}
		}
		return inst;
	}
	
	public abstract Payload analyze (String text);
	
}
