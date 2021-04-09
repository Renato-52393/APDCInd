package pt.unl.fct.di.apdc.projindividual.util;

public class StatusData {
	public String username;
	public String user_to_update;
	public String status;

	public StatusData() {}

	public StatusData(String username, String user_to_update, String role) {
		this.username = username;
		this.user_to_update = user_to_update;
		this.status = role;
	}
}
