package main;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;

public class WDBCQueryExec {

	public WDBCQueryExec() {
		// TODO Auto-generated constructor stub
	}
	/*
	 * ResultSetHandler<Object[]> h = new ResultSetHandler<Object[]>() { public
	 * Object[] handle(ResultSet rs) throws SQLException { if (!rs.next()) { return
	 * null; }
	 * 
	 * ResultSetMetaData meta = rs.getMetaData(); int cols = meta.getColumnCount();
	 * Object[] result = new Object[cols];
	 * 
	 * for (int i = 0; i < cols; i++) { result[i] = rs.getObject(i + 1); }
	 * 
	 * return result; } };
	 */

	public WDBCQueryResponse Execute(String query, WDBCConnector connector) {
		// WDBCConnector conn = WDBC
		ResultSet rs = null;
		try {
			Statement st = connector.getConn().createStatement();
			// QueryRunner run = new QueryRunner();
			// Object[] result = run.query(connector.getConn(), query, h);
			rs = st.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return new WDBCQueryResponse("", "", e.getMessage());
		}

		try {
			return new WDBCQueryResponse("columns", formatData(rs), "report");
		} catch (SQLException e) {
			e.printStackTrace();
			return new WDBCQueryResponse("", "", e.getMessage());
		}
	}

	private String formatData(ResultSet rs) throws SQLException {
		if (rs == null)
			return "";

		ResultSetMetaData metadata;
		ArrayList<String> ls = new ArrayList<>();

		metadata = rs.getMetaData();
		int i = 1;
		while (rs.next()) {
			for (i=1; i <= metadata.getColumnCount(); i++) {
				ls.add(rs.getObject(i).toString());
			}
		}
		Gson go = new Gson();
		return go.toJson(ls);
	}

}
