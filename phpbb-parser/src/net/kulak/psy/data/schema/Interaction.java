package net.kulak.psy.data.schema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Interaction {

	private final static DateFormat TIMESTAMP_FORMAT = 
			SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

	private Integer sequence;
	private Date timestamp;
	private String topicId;
	private String fromUserId;
	private String toUserId;
	private String quoteText;
	private Integer quotes;
	private String rawText;
	private String text;
	private List<Payload> payloads;

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public String getQuoteText() {
		return quoteText;
	}

	public void setQuoteText(String quoteText) {
		this.quoteText = quoteText;
	}

	public Integer getQuotes() {
		return quotes;
	}

	@Override
	public String toString() {
		return "Interaction [timestamp=" + TIMESTAMP_FORMAT.format(timestamp) + ", topicId=" + topicId
				+ ", fromUserId=" + fromUserId + ", toUserId=" + toUserId
				+ ", text=" + text + ", sequence=" + sequence + ", quotes="
				+ quotes + ", payloads=" + payloads + "]";
	}

	public void setQuotes(Integer quotes) {
		this.quotes = quotes;
	}

	public List<Payload> getPayloads() {
		if (payloads == null) {
			payloads = new ArrayList<Payload>();
		}
		return payloads;
	}

	public void setPayloads(List<Payload> payloads) {
		this.payloads = payloads;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String formatTimestamp() {
		return TIMESTAMP_FORMAT.format(timestamp);
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}
}
