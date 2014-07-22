package net.kulak.psy.semantic;

import java.util.Date;

import net.kulak.psy.data.schema.Payload;

public class PlainTextAnalyzer extends InteractionAnalyzer {

	private final InteractionAnalyzer analyzer;
	
	public PlainTextAnalyzer(InteractionAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	public Payload analyze(String text) {
		Payload res = new Payload();
		res.setType("A");

		text = text.toLowerCase();
		text = text.replaceAll("[^\\p{L}]", " ");
		text = text.replaceAll(" +", " ").trim();
		
		res.setValue(analyzer.analyze(text).getValue());
		res.setTimestamp(new Date());
		return res;
	}

}
