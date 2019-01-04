package main;

public class WDBCCommandData {

	private String queryString;
	private String userName;
	private String passWord;
	private String procString;
	private String connString;
	private String dbName;
	private String dbType;
	
	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
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

	public String getProcString() {
		return procString;
	}

	public void setProcString(String procString) {
		this.procString = procString;
	}

	public WDBCCommandData() {
		// TODO Auto-generated constructor stub
	}
	
	public WDBCConnectionParams ExtractConnectionParams() {
		return new WDBCConnectionParams(EConnectionType.valueOf(dbType), userName, passWord, dbName, connString);
	}

	public String getConnString() {
		return connString;
	}

	public void setConnString(String connString) {
		this.connString = connString;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

}
