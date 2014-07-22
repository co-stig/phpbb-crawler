package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.kulak.psy.data.access.Database;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.semantic.InteractionAnalyzer;

public abstract class Processor {

	public static interface TopicCallback {
		void processTopic(Topic t, boolean processed) throws IOException, ParseException, SQLException;
	}
	
	private final Session session;
	private final Configuration config;
	private final List<InteractionAnalyzer> interactionAnalyzers;
	private static final boolean DELETE_UNPARSED_TOPICS_ONLY = true;
	
	Processor(Configuration config, Session session, List<InteractionAnalyzer> interactionAnalyzers) {
		this.session = session;
		this.config = config;
		this.interactionAnalyzers = 
				(interactionAnalyzers == null ? new ArrayList<InteractionAnalyzer>() : interactionAnalyzers);
	}
	
	public List<InteractionAnalyzer> getInteractionAnalyzers() {
		return interactionAnalyzers;
	}

	public static Processor getInstance(Configuration config, Session session, List<InteractionAnalyzer> interactionAnalyzers, Database database) {
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.PhpbbProcessor(config, session, interactionAnalyzers),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}
	
	public static Processor getInstance(Configuration config, Session session, InteractionAnalyzer interactionAnalyzer, Database database) {
		List<InteractionAnalyzer> ia = new ArrayList<InteractionAnalyzer>();
		ia.add(interactionAnalyzer);
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.PhpbbProcessor(config, session, ia),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}

	public static Processor getInstance(Configuration config, Session session, Database database) {
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.PhpbbProcessor(config, session, new ArrayList<InteractionAnalyzer>()),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}

	public static Processor getReprocessorInstance(Configuration config, InteractionAnalyzer interactionAnalyzer, Database database) {
		List<InteractionAnalyzer> ia = new ArrayList<InteractionAnalyzer>();
		ia.add(interactionAnalyzer);
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.ReProcessor(config, null, database, ia),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}

	public static Processor getReprocessorInstance(Configuration config, Database database) {
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.ReProcessor(config, null, database, new ArrayList<InteractionAnalyzer>()),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}

	public static Processor getReprocessorInstance(Configuration config, List<InteractionAnalyzer> interactionAnalyzers, Database database) {
		return new DatabaseSavingProcessor(
				config,
				new net.kulak.psy.parser.ReProcessor(config, null, database, interactionAnalyzers),
				database,
				DELETE_UNPARSED_TOPICS_ONLY
			);
	}

	public Session getSession() {
		return session;
	}

	public Configuration getConfig() {
		return config;
	}

	public abstract Topic parseTopic(String id) throws IOException, ParseException, SQLException;
	public abstract List<Topic> listTopics(int from, Date since) throws IOException, ParseException, SQLException;
	public abstract int listTopics(int from, Date since, TopicCallback callback) throws IOException, ParseException, SQLException;
	
}
