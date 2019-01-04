package main;

public class WDBCQueryResponse extends WDBCResponse {

	private String columns, data, report;
	
	public WDBCQueryResponse(String columns, String data, String report) {
		super();
		this.setColumns(columns);
		this.setData(data);
		this.setReport(report);
	}

	public void Set(String columns, String data, String report) {
		this.setColumns(columns);
		this.setData(data);
		this.setReport(report);
	}
	
	public WDBCQueryResponse() {
		// TODO Auto-generated constructor stub
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

}
