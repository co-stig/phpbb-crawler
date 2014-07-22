package net.kulak.psy.data.schema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Payload {

	@Override
	public String toString() {
		return "Payload [topicId=" + topicId + ", sequence="
				+ sequence + ", timestamp="
				+ TIMESTAMP_FORMAT.format(timestamp) + ", type=" + type
				+ ", value=" + value + "]";
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	private final static DateFormat TIMESTAMP_FORMAT = SimpleDateFormat
			.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

	private String topicId;
	private Integer sequence;
	private Date timestamp;
	private String type;
	private String value;
}
