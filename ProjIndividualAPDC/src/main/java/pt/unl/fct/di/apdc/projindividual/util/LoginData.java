package pt.unl.fct.di.apdc.projindividual.util;

public class LoginData {
	
	public String username;
	public String pwd;
	
	//validation variables
	public String tokenID;
	public String expiration_data;
	
	public String role;
	
	public LoginData() {}
	
	public LoginData(String username, String pwd, String tokenID, String expiration_data, String role) {
		this.username = username;
		this.pwd = pwd;
		this.tokenID = tokenID;
		this.expiration_data = expiration_data;
		this.role = role;
	}
}
