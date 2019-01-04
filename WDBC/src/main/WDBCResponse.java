package main;

public class WDBCResponse {

	private Integer code;
	private String message;

	public WDBCResponse(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public WDBCResponse() {
		this.code = 0;
		this.message = "";
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void Set(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
