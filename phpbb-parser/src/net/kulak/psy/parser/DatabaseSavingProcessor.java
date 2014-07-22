package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Interaction;
import net.kulak.psy.data.schema.Topic;

public class DatabaseSavingProcessor extends Processor {

	private final Database database;
	private final Processor processor;
	private final boolean deleteUnparsedTopicsOlny;
	
	DatabaseSavingProcessor(Configuration config, Processor processor, Database database, boolean deleteUnparsedTopicsOlny) {
		super(config, null, null);
		this.database = database;
		this.processor = processor;
		this.deleteUnparsedTopicsOlny = deleteUnparsedTopicsOlny;
	}

	@Override
	public Topic parseTopic(String id) throws IOException, ParseException, SQLException {
		Topic topic = processor.parseTopic(id);
		database.deleteInteractionsForTopic(id);
		database.deleteTopicOnly(id);
		database.saveTopicOnly(topic);
		for (Interaction inter: topic.getInteractions()) {
			database.saveInteraction(inter);
		}
		topic.setTimestamp(new Date());
		database.finalizeTopic(topic);
		return topic;
	}

	@Override
	public List<Topic> listTopics(int from, Date since) throws IOException, ParseException, SQLException {
		List<Topic> topics = processor.listTopics(from, since);
		for (Topic topic: topics) {
			if ((deleteUnparsedTopicsOlny && !database.isTopicParsed(topic.getId())) || !deleteUnparsedTopicsOlny) {
				database.deleteInteractionsForTopic(topic.getId());
				database.deleteTopicOnly(topic.getId());
				database.saveTopicOnly(topic);
			}
		}
		return topics;
	}

	@Override
	public int listTopics(int from, Date since, final TopicCallback callback) throws IOException, ParseException, SQLException {
		return processor.listTopics(from, since, new TopicCallback() {
			@Override
			public void processTopic(Topic t, boolean processed) throws IOException, ParseException, SQLException {
				if ((deleteUnparsedTopicsOlny && !database.isTopicParsed(t.getId())) || !deleteUnparsedTopicsOlny) {
					database.deleteInteractionsForTopic(t.getId());
					database.deleteTopicOnly(t.getId());
					database.saveTopicOnly(t);
					callback.processTopic(t, true);
				} else {
					callback.processTopic(t, false);
				}
			}
		});
	}
	
}
