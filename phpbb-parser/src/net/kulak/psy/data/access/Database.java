package net.kulak.psy.data.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.kulak.psy.data.schema.Interaction;
import net.kulak.psy.data.schema.Payload;
import net.kulak.psy.data.schema.Topic;
import net.kulak.psy.data.schema.UserStatistic;

public class Database {

	private static Database inst = null;

	public void reconnect(ConnectionProperties properties) throws SQLException, ClassNotFoundException {
		if (inst != null) {
			inst.conn.close();
		}
		inst = new Database(properties);
	}
	
	private static final String GET_INTERACTIONS_FOR_TOPIC = "SELECT when, topicid, fromuser, touser, txt, seq, quotes FROM interactions WHERE topicid = ? ORDER BY seq";
	private static final String GET_INTERACTIONS_FOR_USER = "SELECT when, topicid, fromuser, touser, txt, seq, quotes FROM interactions WHERE fromuser = ? ORDER BY seq";
	private static final String GET_PAYLOADS_FOR_TOPIC = "SELECT topicid, seq, type, value, when FROM payloads WHERE topicid = ? ORDER BY seq";
	private static final String GET_ALL_INTERACTIONS_FOR_PERIOD = 
		"SELECT iii.when, iii.topicid, iii.fromuser, iii.touser, iii.txt, iii.seq, iii.quotes\n" +
		"FROM interactions iii\n" + 
		"JOIN (\n" +
		"   SELECT fromuser FROM (\n" +
		"      SELECT fromuser, COUNT(*) AS cnt FROM interactions GROUP BY fromuser\n" +
		"   ) WHERE cnt >= ?\n" +
		") ccc ON iii.fromuser = ccc.fromuser\n" +
		"WHERE when > ?\n" + 
		"AND when < ?\n" + 
		"ORDER BY seq";
	private static final String DELETE_INTERACTIONS_FOR_TOPIC = "DELETE FROM interactions WHERE topicid = ?";
	private static final String DELETE_TOPIC = "DELETE FROM topics WHERE id = ?";
	private static final String INSERT_INTERACTION = "INSERT INTO interactions (when, topicid, fromuser, touser, txt, seq, quotes) VALUES(?, ?, ?, ?, ?, ?, ?)";
	private static final String GET_ALL_USERS = "SELECT DISTINCT fromuser FROM interactions";
	private static final String GET_ALL_TOPICS = "SELECT id, when, title, cnt FROM topics JOIN (SELECT topicid, COUNT(*) AS cnt FROM interactions GROUP BY topicid) ON topicid = id";
	private static final String GET_SINGLE_TOPIC = "SELECT id, when, title, cnt FROM topics JOIN (SELECT topicid, COUNT(*) AS cnt FROM interactions GROUP BY topicid) ON topicid = id WHERE id = ?";
	private static final String GET_UNPARSED_TOPICS = "SELECT id, title FROM topics WHERE when IS NULL";
	private static final String IS_TOPIC_PARSED = "SELECT id FROM topics WHERE id = ? AND when IS NOT NULL";

	private static final String INSERT_PAYLOAD = "INSERT INTO payloads (topicid, seq, type, value, when) VALUES(?, ?, ?, ?, ?)";
	private static final String DELETE_PAYLOADS_FOR_INTERACTION = "DELETE FROM payloads WHERE topicid = ? AND seq = ?";
	private static final String DELETE_PAYLOADS_FOR_TOPIC = "DELETE FROM payloads WHERE topicid = ?";
	private static final String INSERT_TOPIC = "INSERT INTO topics (id, when, title) VALUES(?, NULL, ?)";
	private static final String UPDATE_TOPIC = "UPDATE topics SET when = ? WHERE id = ?";
	private static final String UPDATE_INTERACTION_SET_FROMUSER = "UPDATE interactions SET fromuser = ? WHERE topicid = ? AND seq = ?";

	private static final String STAT_VOLUME_BY_DATE = "SELECT when, COUNT(*) FROM (SELECT TO_CHAR(when, 'YYYY-MM-DD') AS when FROM interactions) GROUP BY when";
	// TODO: Fix to 24 hour + minute!
	private static final String STAT_VOLUME_BY_TIME = "SELECT when, COUNT(*) FROM (SELECT TO_CHAR(when, 'HH') AS when FROM interactions) GROUP BY when";
	private static final String STAT_TOPICS_PARSED = "SELECT DISTINCT TO_CHAR(MAX(when), 'YYYY/MM/DD') FROM interactions GROUP BY topicid";
	private static final String STAT_USERS = "SELECT fromuser, MIN(when), MAX(when), COUNT(*) FROM interactions GROUP BY fromuser";
	private static final String STAT_USERS_BY_DATE_MONTH = "SELECT TO_CHAR(when, 'YYYY/MM'), fromuser, COUNT(*) FROM interactions GROUP BY TO_CHAR(when, 'YYYY/MM'), fromuser";
	private static final String STAT_USERS_BY_DATE_DAY = "SELECT TO_CHAR(when, 'YYYY/MM/DD'), fromuser, COUNT(*) FROM interactions GROUP BY TO_CHAR(when, 'YYYY/MM/DD'), fromuser";
	private static final String STAT_OUT_DEGREE = "SELECT fromuser, COUNT(*) FROM (SELECT DISTINCT fromuser, touser FROM interactions) GROUP BY fromuser";
	private static final String STAT_IN_DEGREE = "SELECT touser, COUNT(*) as cnt FROM (SELECT DISTINCT fromuser, touser FROM interactions) WHERE touser IN (SELECT fromuser FROM interactions) GROUP BY touser";
	private static final String STAT_REPLIES_HISTOGRAM = 
		"( \n" +
		"  SELECT usr, cnt, COUNT(*) AS hist \n" +
		"  FROM ( \n" +
		"    SELECT i1.fromuser AS usr, COUNT(*) AS cnt \n" +
		"    FROM interactions i1 \n" +
		"    JOIN interactions i2 \n" +
		"    ON i2.topicid = i1.topicid AND i2.quotes = i1.seq \n" +
		"    GROUP BY i1.fromuser, i1.topicid, i1.seq \n" +
		"  ) \n" +
		"  GROUP BY usr, cnt \n" +
		"UNION ALL \n" +
		"  SELECT i1.fromuser AS usr, 0 AS cnt, COUNT(*) AS hist \n" +
		"  FROM interactions i1 \n" +
		"  WHERE NOT EXISTS ( \n" +
		"    SELECT seq FROM interactions i2 WHERE i2.quotes = i1.seq AND i2.topicid = i1.topicid \n" +
		"  ) \n" +
		"  GROUP BY i1.fromuser \n" +
		") \n" +
		"ORDER BY usr, cnt";
	
	private final Connection conn;
	
	private final PreparedStatement stmtGetInteractionsForUser;
	private final PreparedStatement stmtGetInteractionsForTopic;
	private final PreparedStatement stmtGetPayloadsForTopic;
	private final PreparedStatement stmtGetAllInteractionsForPeriod;
	private final PreparedStatement stmtDeleteInteractionsForTopic;
	private final PreparedStatement stmtDeletePayloadsForTopic;
	private final PreparedStatement stmtDeleteTopic;
	private final PreparedStatement stmtIsTopicParsed;
	private final PreparedStatement stmtInsertInteraction;
	private final PreparedStatement stmtGetAllUsers;
	private final PreparedStatement stmtGetAllTopics;
	private final PreparedStatement stmtGetSingleTopic;
	private final PreparedStatement stmtGetUnparsedTopics;
	private final PreparedStatement stmtInsertPayloads;
	private final PreparedStatement stmtDeletePayloadsForInteraction;
	private final PreparedStatement stmtInsertTopic;
	private final PreparedStatement stmtUpdateTopic;
	private final PreparedStatement stmtUpdateInteractionSetFromUser;
	
	private final PreparedStatement stmtStatUsersByDateDay;
	private final PreparedStatement stmtStatUsersByDateMonth;
	private final PreparedStatement stmtStatVolumeByDate;
	private final PreparedStatement stmtStatVolumeByTime;
	private final PreparedStatement stmtStatTopicsParsed;
	private final PreparedStatement stmtStatUsers;
	private final PreparedStatement stmtStatOutDegree;
	private final PreparedStatement stmtStatInDegree;
	private final PreparedStatement stmtStatRepliesHistogram;

	protected Database(ConnectionProperties properties) throws SQLException, ClassNotFoundException {
		Class.forName(properties.getDriver());
		conn = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
		
		stmtGetInteractionsForUser = conn.prepareStatement(GET_INTERACTIONS_FOR_USER);
		stmtGetInteractionsForTopic = conn.prepareStatement(GET_INTERACTIONS_FOR_TOPIC);
		stmtGetPayloadsForTopic = conn.prepareStatement(GET_PAYLOADS_FOR_TOPIC);
		stmtGetAllInteractionsForPeriod = conn.prepareStatement(GET_ALL_INTERACTIONS_FOR_PERIOD);
		stmtDeleteInteractionsForTopic = conn.prepareStatement(DELETE_INTERACTIONS_FOR_TOPIC);
		stmtDeletePayloadsForTopic = conn.prepareStatement(DELETE_PAYLOADS_FOR_TOPIC);
		stmtDeleteTopic = conn.prepareStatement(DELETE_TOPIC);
		stmtIsTopicParsed = conn.prepareStatement(IS_TOPIC_PARSED);
		stmtInsertInteraction = conn.prepareStatement(INSERT_INTERACTION);
		stmtGetAllUsers = conn.prepareStatement(GET_ALL_USERS);
		stmtGetAllTopics = conn.prepareStatement(GET_ALL_TOPICS);
		stmtGetSingleTopic = conn.prepareStatement(GET_SINGLE_TOPIC);
		stmtGetUnparsedTopics = conn.prepareStatement(GET_UNPARSED_TOPICS);
		stmtInsertPayloads = conn.prepareStatement(INSERT_PAYLOAD);
		stmtDeletePayloadsForInteraction = conn.prepareStatement(DELETE_PAYLOADS_FOR_INTERACTION);
		stmtInsertTopic = conn.prepareStatement(INSERT_TOPIC);
		stmtUpdateTopic = conn.prepareStatement(UPDATE_TOPIC);
		stmtUpdateInteractionSetFromUser = conn.prepareStatement(UPDATE_INTERACTION_SET_FROMUSER);
		
		stmtStatUsersByDateDay = conn.prepareStatement(STAT_USERS_BY_DATE_DAY);
		stmtStatUsersByDateMonth = conn.prepareStatement(STAT_USERS_BY_DATE_MONTH);
		stmtStatVolumeByDate = conn.prepareStatement(STAT_VOLUME_BY_DATE);
		stmtStatVolumeByTime = conn.prepareStatement(STAT_VOLUME_BY_TIME);
		stmtStatTopicsParsed = conn.prepareStatement(STAT_TOPICS_PARSED);
		stmtStatUsers = conn.prepareStatement(STAT_USERS);
		stmtStatOutDegree = conn.prepareStatement(STAT_OUT_DEGREE);
		stmtStatInDegree = conn.prepareStatement(STAT_IN_DEGREE);
		stmtStatRepliesHistogram = conn.prepareStatement(STAT_REPLIES_HISTOGRAM);
	}

	public static Database getDatabase() {
		return inst;
	}

	public static Database getDatabase(ConnectionProperties properties) throws SQLException, ClassNotFoundException {
		if (inst == null) {
			inst = new Database(properties);
		}
		return inst;
	}

	public void saveInteraction(Interaction inter) throws SQLException {
		stmtInsertInteraction.setTimestamp(1, new Timestamp(inter.getTimestamp().getTime()));
		stmtInsertInteraction.setString(2, inter.getTopicId());
		stmtInsertInteraction.setString(3,
				inter.getFromUserId().length() >= 20 ? 
					inter.getFromUserId().substring(0, 19) : inter.getFromUserId());
		if (inter.getToUserId() == null) {
			stmtInsertInteraction.setNull(4, java.sql.Types.VARCHAR);
		} else {
			stmtInsertInteraction.setString(4,
					inter.getToUserId().length() >= 20 ? 
						inter.getToUserId().substring(0, 19) : inter.getToUserId());
		}
		stmtInsertInteraction.setString(5,
				inter.getText() == null ? "" :
				(inter.getText().length() >= 1000 ? 
					inter.getText().substring(0, 999) : inter.getText()));
		stmtInsertInteraction.setInt(6, inter.getSequence());
		stmtInsertInteraction.setInt(7, inter.getQuotes());
		stmtInsertInteraction.executeUpdate();
		updatePayloads(inter);
	}

	public void updatePayloads(Interaction inter) throws SQLException {
		stmtDeletePayloadsForInteraction.setString(1, inter.getTopicId());
		stmtDeletePayloadsForInteraction.setInt(2, inter.getSequence());
		stmtDeletePayloadsForInteraction.executeUpdate();
		
		for (Payload r : inter.getPayloads()) {
			addPayload(r);
		}
	}

	public void addPayload(Payload r) throws SQLException {
		stmtInsertPayloads.setString(1, r.getTopicId());
		stmtInsertPayloads.setInt(2, r.getSequence());
		stmtInsertPayloads.setString(3, r.getType());
		stmtInsertPayloads.setString(4, r.getValue());
		stmtInsertPayloads.setTimestamp(5, new Timestamp(r.getTimestamp().getTime()));
		stmtInsertPayloads.executeUpdate();
	}
	
	public void deleteInteractionsForTopic(String topicId) throws SQLException {
		stmtDeletePayloadsForTopic.setString(1, topicId);
		stmtDeletePayloadsForTopic.executeUpdate();
		stmtDeleteInteractionsForTopic.setString(1, topicId);
		stmtDeleteInteractionsForTopic.executeUpdate();
	}

	public List<Interaction> getAllInteractionsForPeriod(Date from, Date to, int minimumMessages) throws SQLException {
		long start = System.currentTimeMillis();
		stmtGetAllInteractionsForPeriod.setInt(1, minimumMessages);
		stmtGetAllInteractionsForPeriod.setTimestamp(2, new Timestamp(from.getTime()));
		stmtGetAllInteractionsForPeriod.setTimestamp(3, new Timestamp(to.getTime()));
		ResultSet rs = stmtGetAllInteractionsForPeriod.executeQuery();
		List<Interaction> res = new ArrayList<Interaction>();
		int reads = 0;
		System.out.print("Reading DB: ");
		try {
			while (rs.next()) {
				Interaction i = new Interaction();
				i.setTimestamp(rs.getTimestamp(1));
				i.setTopicId(rs.getString(2));
				i.setFromUserId(rs.getString(3));
				i.setToUserId(rs.getString(4));
				i.setText(rs.getString(5));
				i.setSequence(rs.getInt(6));
				i.setQuotes(rs.getInt(7));
				res.add(i);
				if ((++reads) % 10000 == 0) {
					System.out.print("*");
				}
			}
		} finally {
			long done = (System.currentTimeMillis() - start) / 1000;
			System.out.println(" done in " + done + "s.");
			rs.close();
		}
		return res;
	}

	public Set<String> getAllUsers() throws SQLException {
		ResultSet rs = stmtGetAllUsers.executeQuery();
		Set<String> res = new TreeSet<String>();
		try {
			while (rs.next()) {
				res.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public boolean isTopicParsed(String id) throws SQLException {
		stmtIsTopicParsed.setString(1, id);
		ResultSet rs = stmtIsTopicParsed.executeQuery();
		try {
			return rs.next();
		} finally {
			rs.close();
		}
	}

	public List<Topic> getAllTopics() throws SQLException {
		ResultSet rs = stmtGetAllTopics.executeQuery();
		List<Topic> res = new ArrayList<Topic>();
		try {
			while (rs.next()) {
				Topic t = new Topic();
				t.setId(rs.getString(1));
				t.setTimestamp(rs.getTimestamp(2));
				t.setTitle(rs.getString(3));
				t.setCount(rs.getInt(4));
				res.add(t);
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, Integer> statVolumeByDate() throws SQLException {
		ResultSet rs = stmtStatVolumeByDate.executeQuery();
		Map<String, Integer> res = new HashMap<String, Integer>();
		try {
			while (rs.next()) {
				res.put(rs.getString(1), rs.getInt(2));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, Integer> statVolumeByTime() throws SQLException {
		ResultSet rs = stmtStatVolumeByTime.executeQuery();
		Map<String, Integer> res = new HashMap<String, Integer>();
		try {
			while (rs.next()) {
				res.put(rs.getString(1), rs.getInt(2));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, Set<UserStatistic>> statUsersByDate(boolean monthGranularity) throws SQLException {
		ResultSet rs = 
				monthGranularity ? 
						stmtStatUsersByDateMonth.executeQuery() :
						stmtStatUsersByDateDay.executeQuery();
		Map<String, Set<UserStatistic>> res = new HashMap<String, Set<UserStatistic>>();
		try {
			while (rs.next()) {
				String date = rs.getString(1);
				String user = rs.getString(2);
				Integer count = rs.getInt(3);
				Set<UserStatistic> users = res.get(date);
				if (users == null) {
					users = new TreeSet<UserStatistic>();
					res.put(date, users);
				}
				UserStatistic stat = new UserStatistic();
				stat.setName(user);
				stat.setCount(count);
				users.add(stat);
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Set<String> statTopicsParsed() throws SQLException {
		ResultSet rs = stmtStatTopicsParsed.executeQuery();
		Set<String> res = new TreeSet<String>();
		try {
			while (rs.next()) {
				res.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public List<UserStatistic> statUsers() throws SQLException {
		ResultSet rs = stmtStatUsers.executeQuery();
		List<UserStatistic> res = new ArrayList<UserStatistic>();
		try {
			while (rs.next()) {
				UserStatistic us = new UserStatistic();
				us.setName(rs.getString(1));
				us.setFirstMessage(rs.getTimestamp(2));
				us.setLastMessage(rs.getTimestamp(3));
				us.setCount(rs.getInt(4));
				res.add(us);
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, Integer> statOutDegree() throws SQLException {
		ResultSet rs = stmtStatOutDegree.executeQuery();
		Map<String, Integer> res = new HashMap<String, Integer>();
		try {
			while (rs.next()) {
				res.put(rs.getString(1), rs.getInt(2));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, Integer> statInDegree() throws SQLException {
		ResultSet rs = stmtStatInDegree.executeQuery();
		Map<String, Integer> res = new HashMap<String, Integer>();
		try {
			while (rs.next()) {
				res.put(rs.getString(1), rs.getInt(2));
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Map<String, int[]> statRepliesHistogram() throws SQLException {
		Map<String, int[]> res = new HashMap<String, int[]>();
		ResultSet rs = stmtStatRepliesHistogram.executeQuery();
		String currentUser = "";
		try {
			int[] repArr = null;
			while (rs.next()) {
				String usr = rs.getString(1);
				int replies = rs.getInt(2);
				int count = rs.getInt(3);
				if (!usr.equals(currentUser)) {
					repArr = new int[20];
					res.put(usr, repArr);
					for (int i = 0; i < repArr.length; ++i) {
						repArr[i] = 0;
					}
				}
				repArr[replies] = count;
				currentUser = usr;
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public List<Topic> getSingleTopicAsList(String topicId) throws SQLException {
		stmtGetSingleTopic.setString(1, topicId);
		ResultSet rs = stmtGetSingleTopic.executeQuery();
		List<Topic> res = new ArrayList<Topic>();
		try {
			while (rs.next()) {
				Topic t = new Topic();
				t.setId(rs.getString(1));
				t.setTimestamp(rs.getTimestamp(2));
				t.setTitle(rs.getString(3));
				t.setCount(rs.getInt(4));
				res.add(t);
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public List<Topic> getUnparsedTopics() throws SQLException {
		ResultSet rs = stmtGetUnparsedTopics.executeQuery();
		List<Topic> res = new ArrayList<Topic>();
		try {
			while (rs.next()) {
				Topic t = new Topic();
				t.setId(rs.getString(1));
				t.setTitle(rs.getString(2));
				res.add(t);
			}
		} finally {
			rs.close();
		}
		return res;
	}

	public Topic getInteractionsForTopic(String topicId) throws SQLException {
		Topic res = new Topic();
		stmtGetInteractionsForTopic.setString(1, topicId);
		ResultSet rs = stmtGetInteractionsForTopic.executeQuery();
		try {
			res.setInteractions(parseResultSetWithInteractions(rs));
			res.setId(topicId);
		} finally {
			rs.close();
		}
		
		Map<Integer, List<Payload>> payloads = getPayloadsForTopic(topicId);
		for (Interaction i: res.getInteractions()) {
			i.setPayloads(payloads.get(i.getSequence()));
		}
		
		return res;
	}

	public List<Interaction> getInteractionsForUser(String fromUser) throws SQLException {
		stmtGetInteractionsForUser.setString(1, fromUser);
		ResultSet rs = stmtGetInteractionsForUser.executeQuery();
		try {
			return parseResultSetWithInteractions(rs);
		} finally {
			rs.close();
		}
	}

	private List<Interaction> parseResultSetWithInteractions(ResultSet rs) throws SQLException {
		List<Interaction> ints = new ArrayList<Interaction>();
		while (rs.next()) {
			Interaction i = new Interaction();
			i.setTimestamp(rs.getTimestamp(1));
			i.setTopicId(rs.getString(2));
			i.setFromUserId(rs.getString(3));
			i.setToUserId(rs.getString(4));
			i.setText(rs.getString(5));
			i.setSequence(rs.getInt(6));
			i.setQuotes(rs.getInt(7));
			ints.add(i);
		}
		return ints;
	}

	public Map<Integer, List<Payload>> getPayloadsForTopic(String topicId) throws SQLException {
		stmtGetPayloadsForTopic.setString(1, topicId);
		ResultSet rs = stmtGetPayloadsForTopic.executeQuery();
		Map<Integer, List<Payload>> ras = new HashMap<Integer, List<Payload>>();
		try {
			while (rs.next()) {
				Payload r = new Payload();
				r.setTopicId(rs.getString(1));
				r.setSequence(rs.getInt(2));
				r.setType(rs.getString(3));
				r.setValue(rs.getString(4));
				r.setTimestamp(rs.getTimestamp(5));
				List<Payload> payloads = ras.get(r.getSequence());
				if (payloads == null) {
					payloads = new ArrayList<Payload>();
				}
				payloads.add(r);
				ras.put(r.getSequence(), payloads);
			}
		} finally {
			rs.close();
		}
		return ras;
	}

	public void saveTopicOnly(Topic topic) throws SQLException {
		stmtInsertTopic.setString(1, topic.getId());
		stmtInsertTopic.setString(2,
				topic.getTitle() == null ? "" :
				(topic.getTitle().length() >= 300 ? 
					topic.getTitle().substring(0, 299) : topic.getTitle()));
		stmtInsertTopic.executeUpdate();
	}

	public void finalizeTopic(Topic topic) throws SQLException {
		stmtUpdateTopic.setTimestamp(1, new Timestamp(topic.getTimestamp().getTime()));
		stmtUpdateTopic.setString(2, topic.getId());
		stmtUpdateTopic.executeUpdate();
	}

	public void deleteTopicOnly(String id) throws SQLException {
		stmtDeleteTopic.setString(1, id);
		stmtDeleteTopic.executeUpdate();
	}

	public void updateInteractionSetFromUser(String topicid, int seq, String fromuser) throws SQLException {
		stmtUpdateInteractionSetFromUser.setString(1, fromuser);
		stmtUpdateInteractionSetFromUser.setString(2, topicid);
		stmtUpdateInteractionSetFromUser.setInt(3, seq);
		stmtUpdateInteractionSetFromUser.executeUpdate();
	}
}
