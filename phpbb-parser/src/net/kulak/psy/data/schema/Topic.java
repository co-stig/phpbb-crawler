package net.kulak.psy.data.schema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Topic {

	private final static DateFormat TIMESTAMP_FORMAT = SimpleDateFormat
			.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

	private String id;
	private String title;
	private Date timestamp;
	private Integer count;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Topic [id=" + id + ", title=" + title + ", timestamp="
				+ (timestamp == null ? "???" : TIMESTAMP_FORMAT.format(timestamp)) + ", interactions=" + interactions + "]";
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	private List<Interaction> interactions = new LinkedList<Interaction>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Interaction> getInteractions() {
		return interactions;
	}

	public void setInteractions(List<Interaction> interactions) {
		this.interactions = interactions;
	}

}
