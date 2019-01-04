package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.JDBC;

public class WDBCConnector {

	private Connection conn = null;
	
	public WDBCConnector() {
		try {
			DriverManager.registerDriver(new JDBC());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public WDBCResponse Establish(WDBCConnectionParams connParams) {
		if (conn!=null) {
			return new WDBCResponse(0, "Connection already established");
		}
        try {
        	if (connParams.getConnType() != EConnectionType.sqlite) {
        		return new WDBCResponse(100, "Connection type " + connParams.getConnType().toString() + " not supported");
        	}
            // db parameters
        	//"jdbc:sqlite:~/databases/testDB.db";
            String url = "jdbc:sqlite:" + connParams.getConnString();
            // create a connection to the database
            this.conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");
            
            return new WDBCResponse(0, "Connection to SQLite has been established");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new WDBCResponse(e.getErrorCode(), e.getMessage());
        }
	}
	public boolean IsConnected() {
		if (conn == null) {
			return false;
		}
		try {
			return !conn.isClosed();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	public WDBCResponse Close( ) {
        try {
            if (conn != null) {
                conn.close();
                return new WDBCResponse(0, "Connection closed");
            }else {
            	return new WDBCResponse(0, "Connection already closed");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new WDBCResponse(e.getErrorCode(), e.getMessage());
        }
	}
}
