package db_proj;

import java.util.Properties;

/**
 * Encapsulates all the information necessary to establish a
 * connection with a postgress server.
 * For example:
 * url: "jdbc:postgresql://localhost/imgtest";
 * properties: {user: shumash, password: pwd, ssl: true} 
 *
 */
public class DbConnectionInfo {
	private String url = new String();
	private Properties props = new Properties();
	
	/**
	 * Creates null connection info.
	 */
	DbConnectionInfo() {}
	
	/**
	 * @return internal url
	 */
	String getUrl() {
		return url;
	}
	
	/**
	 * @return internal properties
	 */
	Properties getProps() {
		return props;
	}
	
	/**
	 * Sets url to jdbc:postgresql://localhost/mydb.
	 * @param database created e.g. using bash command "createdb mydb && psql mydb"
	 */
	void setLocalUrl(String database) {
		setUrl("localhost", null, database);
	}
	
	/**
	 * Sets url to jdbc:postgresql://host:port/mydb.
	 * @param host
	 * @param port can be null for localhost
	 * @param database
	 */
	void setUrl(String host, String port, String database) {
		url = "jdbc:postgresql://" + host + 
				(port == null ? "" : (":" + port)) + "/" + database;
	}
	
	void setUserInfo(String user, String psw) {
		props.setProperty("user", user);
		if (psw != null) {
			props.setProperty("password", psw);
			// props.setProperty("ssl","true");  // might need later
		}
	}
}
