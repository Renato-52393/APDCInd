package pt.unl.fct.di.apdc.projindividual.util;

public class PasswordUpdateData {
	public String username;
	public String pwd;
	public String new_pwd;
	public String new_pwd_conf;

	public PasswordUpdateData() {}

	public PasswordUpdateData(String username, String pwd, String new_pwd, String new_pwd_conf) {
		this.username = username;
		this.pwd = pwd;
		this.new_pwd = new_pwd;
		this.new_pwd_conf = new_pwd_conf;
	}
}
