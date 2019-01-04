package main;

public class WDBCConnectionParams {

	private EConnectionType connType;
	
	private String userName;
	
	private String passWord;
	
	private String dbName;
	
	private String connString;

	public WDBCConnectionParams(EConnectionType connType, String userName, String passWord, String dbName,
			String connString) {
		super();
		this.connType = connType;
		this.userName = userName;
		this.passWord = passWord;
		this.dbName = dbName;
		this.connString = connString;
	}
	
	public EConnectionType getConnType() {
		return connType;
	}

	public void setConnType(EConnectionType connType) {
		this.connType = connType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getConnString() {
		return connString;
	}

	public void setConnString(String connString) {
		this.connString = connString;
	}

}
