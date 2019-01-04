package main;

public class WDBCCommand {

	private String verb;
	private WDBCCommandData commandData;
	
	public WDBCCommand() {
		// TODO Auto-generated constructor stub
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public WDBCCommandData getCommandData() {
		return commandData;
	}

	public void setCommand(WDBCCommandData command) {
		this.commandData = command;
	}

}
