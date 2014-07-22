package net.kulak.psy.data.schema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserStatistic implements Comparable<UserStatistic> {

	private final static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private String name;
	private Date firstMessage;
	private Date lastMessage;
	private Integer count;

	@Override
	public String toString() {
		return "UserStatistic [name=" + name + ", firstMessage="
				+ TIMESTAMP_FORMAT.format(firstMessage) + ", lastMessage="
				+ TIMESTAMP_FORMAT.format(lastMessage) + ", count=" + count
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getFirstMessage() {
		return firstMessage;
	}

	public String getFirstMessageAsString() {
		return TIMESTAMP_FORMAT.format(firstMessage);
	}

	public void setFirstMessage(Date firstMessage) {
		this.firstMessage = firstMessage;
	}

	public Date getLastMessage() {
		return lastMessage;
	}

	public String getLastMessageAsString() {
		return TIMESTAMP_FORMAT.format(lastMessage);
	}

	public void setLastMessage(Date lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public int compareTo(UserStatistic user) {
		return name.compareTo(user.getName());
	}

}
