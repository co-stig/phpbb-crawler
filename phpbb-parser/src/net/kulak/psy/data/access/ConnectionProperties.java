package net.kulak.psy.data.access;

public class ConnectionProperties {

	private final String username;
	private final String password;
	private final String url;
	private final String driver;

	public ConnectionProperties(String username, String password, String url, String driver) {
		this.username = username;
		this.password = password;
		this.url = url;
		this.driver = driver;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getDriver() {
		return driver;
	}

	@Override
	public String toString() {
		return "ConnectionProperties [username=" + username + ", password="
				+ password + ", url=" + url + ", driver=" + driver + "]";
	}
}
