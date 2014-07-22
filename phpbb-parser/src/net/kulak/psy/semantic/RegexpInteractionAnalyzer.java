package net.kulak.psy.semantic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.kulak.psy.data.schema.Payload;

public class RegexpInteractionAnalyzer extends InteractionAnalyzer {

	private List<Pattern> patternsPositive = new ArrayList<Pattern>();
	private List<Pattern> patternsNegative = new ArrayList<Pattern>();
	private List<Pattern> patternsExtremelyNegative = new ArrayList<Pattern>();

	private void readPatterns(String resource, List<Pattern> target) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(
					getClass().getResource(resource).getFile()
				));
			String pattern;
			while ((pattern = br.readLine()) != null) {
				target.add(Pattern.compile(pattern));
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public RegexpInteractionAnalyzer() throws IOException {
		readPatterns("/net/kulak/psy/semantic/semantic-positive.txt", patternsPositive);
		readPatterns("/net/kulak/psy/semantic/semantic-negative.txt", patternsNegative);
		readPatterns("/net/kulak/psy/semantic/semantic-extremely-negative.txt", patternsExtremelyNegative);
	}

	public Payload analyze(String text) {
		Payload res = new Payload();
		res.setTimestamp(new Date());
		res.setType("A");
		
		for (Pattern p: patternsNegative) {
			if (p.matcher(text).find()) {
				res.setValue("N");
				return res;
			}
		}
		for (Pattern p: patternsExtremelyNegative) {
			if (p.matcher(text).find()) {
				res.setValue("E");
				return res;
			}
		}
		for (Pattern p: patternsPositive) {
			if (p.matcher(text).find()) {
				res.setValue("P");
				return res;
			}
		}
		res.setValue("U");
		return res;
	}

}
