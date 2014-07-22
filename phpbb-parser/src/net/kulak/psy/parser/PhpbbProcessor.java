package net.kulak.psy.parser;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import net.kulak.psy.data.schema.Interaction;
import net.kulak.psy.data.schema.Payload;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.semantic.InteractionAnalyzer;

import org.jsoup.Jsoup;

public class PhpbbProcessor extends net.kulak.psy.parser.Processor {
	
	PhpbbProcessor(Configuration config, Session session, List<InteractionAnalyzer> interactionAnalyzers) {
		super(config, session, interactionAnalyzers);
	}

	private String extractTopicTitle(String html) {
		Matcher m = getConfig().getTopicTitle().matcher(html);
		return m.find() ? m.group(1) : null;
	}
	
	private List<String> splitToMessages(String html) {
		Matcher m = getConfig().getMessageSplitter().matcher(html.replace('\n', ' '));
		List<String> res = new ArrayList<String>();
		while (m.find()) {
			res.add(m.group(1));
		}
		// The last element is not a message actually
		if (res.size() > 0) {
			res.remove(res.size() - 1);
		}
		return res;
	}

	private List<Topic> splitToTopics(String html) {
		Matcher m = getConfig().getTopicSplitter().matcher(html.replace('\n', ' '));
		List<Topic> res = new ArrayList<Topic>();
		while (m.find()) {
			Topic t = new Topic();
			t.setId(m.group(1) + "-" + m.group(2));
			t.setTitle(m.group(3));
			res.add(t);
		}
		return res;
	}

	// Strips HTML tags and excessive whitespace.
	private String filterText(String html) {
		return Jsoup.parse(html).text().trim();
	}

	// Here we identify a reply target, if any
	private Integer findQuote(Interaction i, List<Interaction> prev) {
		if (i.getQuoteText() != null) {
//			System.out.println(i.getSequence() + " Big quote: " + i.getQuoteText());
			for (Interaction inter: prev) {
				if (inter.getFromUserId().equals(i.getToUserId())) {
//					System.out.println("?" + inter.getRawText());
					if (inter.getRawText().contains(i.getQuoteText())) {
//						System.out.println("!");
						return inter.getSequence();
					}
				}
			}
		} 
		// Use this logic even for big quotes, not only for the short ones, because
		// sometimes (buggy auto censorship) the quote is not exactly the same as the 
		// original text
		if (i.getToUserId() != null) {
			// Either reply to the last to-user post, or to the last to-user post to from-user
			ListIterator<Interaction> li = prev.listIterator(prev.size());
			int pos = 0;
			int lastSeq = 0;
			while (li.hasPrevious() && pos < 50) {
				Interaction inter = li.previous();
				++pos;
				if (inter.getFromUserId().equals(i.getToUserId())) {
					if (lastSeq == 0) {
						lastSeq = inter.getSequence();
					}
					if (inter.getToUserId() != null && inter.getToUserId().equals(i.getFromUserId())) {
						lastSeq = inter.getSequence();
						break;
					}
				}
			}
			return lastSeq;
		}
		return 0;
	}
	
	// Here we only parse the message body into a series of strings, without trying to relate the messages to each other
	private Interaction parseMessageText(String html) {
		Interaction res = new Interaction();
		res.setRawText(html);

		// Search for big quote (nested)
		Matcher m = getConfig().getBigQuote().matcher(html);
		if (m.find()) {
			String preText = m.group(1);
			res.setToUserId(m.group(2));
			String quoteText = m.group(3);
			String postText = m.group(4);
			String text = filterText(preText + " " + postText);
			for (InteractionAnalyzer ia: getInteractionAnalyzers()) {
				res.getPayloads().add(ia.analyze(text));
			}
			res.setQuoteText(quoteText);
			res.setText(text);
			return res;
		} else {
			// Search for simple quote (bold text)
			m = getConfig().getSmallQuote().matcher(html);
			if (m.find()) {
				String preText = m.group(1);
				res.setToUserId(m.group(2));
				String postText = m.group(3);
				String text = filterText(preText + " " + postText);
				for (InteractionAnalyzer ia: getInteractionAnalyzers()) {
					res.getPayloads().add(ia.analyze(text));
				}
				res.setQuoteText(null);
				res.setText(text);
				return res;
			} else {
				// Simple message with no quote - not a real interaction, but we still save it
				res.setToUserId(null);
				String text = filterText(html);
				for (InteractionAnalyzer ia: getInteractionAnalyzers()) {
					res.getPayloads().add(ia.analyze(text));
				}
				res.setQuoteText(null);
				res.setText(text);
				return res;
			}
		}
	}
	
	private Interaction parseMessage(String html) throws ParseException {
		Matcher m = getConfig().getMessageParser().matcher(html);
		if (m.find()) {
			String author = m.group(1);
			String timestamp = m.group(2);
			String text = m.group(3);
			Interaction res = parseMessageText(text);
			if (res != null) {
				res.setFromUserId(author);
				res.setTimestamp(((PhpbbSession)getSession()).parseTimestamp(timestamp));
				return res;
			}
		}
		return null;
	}

	@Override
	public Topic parseTopic(String id) throws IOException, ParseException {
		List<String> pagesHtml = getSession().readTopic(id);
		
		Topic res = new Topic();
		res.setId(id);
		res.setTitle(extractTopicTitle(pagesHtml.get(0)));

		int i = 0;
		for (String html: pagesHtml) {
			for (String message: splitToMessages(html)) {
				Interaction inter = parseMessage(message);
				if (inter != null) {
					inter.setTopicId(id);
					inter.setSequence(++i);
					inter.setQuotes(findQuote(inter, res.getInteractions()));
					for (Payload ra: inter.getPayloads()) {
						ra.setTopicId(id);
						ra.setSequence(i);
					}
					res.getInteractions().add(inter);
				}
			}
		}
		
		return res;
	}

	@Override
	public List<Topic> listTopics(int from, Date since) throws IOException, ParseException, SQLException {
		List<Topic> res = new ArrayList<Topic>();
		List<String> pagesHtml = getSession().listTopics(from, since);
		for (String html: pagesHtml) {
			res.addAll(splitToTopics(html));
		}
		return res;
	}

	@Override
	public int listTopics(int from, Date since, final TopicCallback callback) throws IOException, ParseException, SQLException {
		final int[] topics = {0};	// Ugly hack
		getSession().listTopics(from, since, new Session.TopicsPageCallback() {
			@Override
			public void processTopicsPage(String html) throws IOException, ParseException, SQLException {
				for (Topic t: splitToTopics(html)) {
					callback.processTopic(t, true);
					++topics[0];
				}
			}
		});
		return topics[0];
	}
}
