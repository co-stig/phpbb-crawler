package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Interaction;
import net.kulak.psy.data.schema.Payload;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.semantic.InteractionAnalyzer;

public class ReProcessor extends net.kulak.psy.parser.Processor {

	private final Database db;
	
	ReProcessor(Configuration config, Session session, Database db, List<InteractionAnalyzer> interactionAnalyzers) {
		super(config, session, interactionAnalyzers);
		this.db = db;
	}

	@Override
	public Topic parseTopic(String id) throws IOException, ParseException {
		try {
			List<Interaction> inters = db.getInteractionsForTopic(id).getInteractions();
			Topic t = new Topic();
			t.setId(id);
			for (Interaction i: inters) {
				for (InteractionAnalyzer ia: getInteractionAnalyzers()) {
					Payload r = ia.analyze(i.getText());
					r.setTopicId(i.getTopicId());
					r.setSequence(i.getSequence());
					i.getPayloads().add(r);
				}
			}
			t.setInteractions(inters);
			return t;
		} catch (SQLException e) {
			throw new IOException("Unable to read topic " + id + " from the database", e);
		}
	}
	
	@Override
	public List<Topic> listTopics(int from, Date since) throws IOException, ParseException, SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int listTopics(int from, Date since, TopicCallback callback) throws IOException, ParseException, SQLException {
		throw new UnsupportedOperationException();
	}
}
