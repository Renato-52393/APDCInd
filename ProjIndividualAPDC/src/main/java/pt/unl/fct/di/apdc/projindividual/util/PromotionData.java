package pt.unl.fct.di.apdc.projindividual.util;

public class PromotionData {
	public String username;
	public String user_to_promote;
	public String role;

	public PromotionData() {}

	public PromotionData(String username, String user_to_promote, String role) {
		this.username = username;
		this.user_to_promote = user_to_promote;
		this.role = role;
	}
}
