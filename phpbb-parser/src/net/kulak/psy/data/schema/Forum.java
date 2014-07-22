package net.kulak.psy.data.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

// Just a JAXB wrapper
@XmlRootElement
public class Forum {

	private List<Topic> topics;

	public Forum(List<Topic> topics) {
		this.topics = topics;
	}

	public Forum() {
		this.topics = new ArrayList<Topic>();
	}
	
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public List<Topic> getTopics() {
		return topics;
	}

}
