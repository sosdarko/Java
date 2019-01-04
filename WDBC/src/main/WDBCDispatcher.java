package main;

public class WDBCDispatcher {

	private WDBCConnector connector = new WDBCConnector();

	public WDBCDispatcher() {
		// TODO Auto-generated constructor stub
	}

	public WDBCResponse Dispatch(WDBCCommand comm) {
		if (comm == null) {
			return new WDBCResponse(100, "Command should not be empty");
		}

		WDBCCommandData cd = comm.getCommandData();

		if (cd == null) {
			return new WDBCResponse(200, "Command data not present");
		}

		WDBCResponse resp;
		switch (comm.getVerb()) {
		case "query":
			if (!connector.IsConnected()) {
				resp = new WDBCResponse(101, "Not connected");
			} else {
				resp = new WDBCQueryExec().Execute(comm.getCommandData().getQueryString(), connector);
			}
			break;
		case "connect":
			if ((cd.getDbType() == null) || (cd.getDbType().equals(""))) {
				cd.setDbType("sqlite");
			}
			resp = connector.Establish(cd.ExtractConnectionParams());
			break;
		case "disconnect":
			resp = connector.Close();
			break;
		default:
			resp = new WDBCResponse(100, "Unsupported verb");
		}

		return resp;
	}

}
