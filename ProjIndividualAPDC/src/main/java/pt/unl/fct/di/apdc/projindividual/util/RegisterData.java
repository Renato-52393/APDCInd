package pt.unl.fct.di.apdc.projindividual.util;
public class RegisterData {

	//required
	public String username;
	public String email;
	public String pwd;
	public String pwd_conf;
	public String role;
	public String status;

	//optional
	public String profile;
	public String phone;
	public String mobile;
	public String address;
	public String comp_address;
	public String local;

	public RegisterData() {}

	public RegisterData(String username, String email, String pwd, String pwd_conf, String profile, String phone, String mobile, String address, String comp_address, String local,String role, String status) {
		this.username = username;
		this.pwd = pwd;
		this.pwd_conf = pwd_conf;
		this.email = email;
		this.profile = profile;
		this.phone = phone;
		this.mobile = mobile;
		this.address = address;
		this.comp_address = comp_address;
		this.local = local;
		this.role = role;
		this.status = status;
	}
}
