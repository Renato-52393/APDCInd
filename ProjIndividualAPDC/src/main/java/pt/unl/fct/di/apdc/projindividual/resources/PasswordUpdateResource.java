package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.projindividual.util.PasswordUpdateData;

@Path("/update")
public class PasswordUpdateResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public PasswordUpdateResource() {}

	@PUT
	@Path("/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPassword(PasswordUpdateData data) {

		LOG.fine("Password update attempt by user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(data.username);
			Entity user = datastore.get(userKey);

			Key logKey = logKind.newKey(data.username);
			Entity log = datastore.get(logKey);

			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			} if(log == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged in!").build();
			}
			
			String hashedPwd = (String) user.getString("pwd");

			if(hashedPwd.equals(DigestUtils.sha512Hex(data.pwd))) {
				if(!checkPwd(data.new_pwd, data.new_pwd_conf)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Invalid password. A minimum of 6 characters and a special character required!").build();
				}

				Entity person = Entity.newBuilder(userKey)
						.set("username", data.username)
						.set("email", user.getString("email"))
						.set("pwd", DigestUtils.sha512Hex(data.new_pwd))
						.set("pwd_conf", DigestUtils.sha512Hex(data.new_pwd_conf))
						.set("profile", user.getString("profile"))
						.set("phone", user.getString("phone"))
						.set("mobile", user.getString("mobile"))
						.set("address", user.getString("address"))
						.set("comp_address", user.getString("comp_address"))
						.set("local", user.getString("local"))
						.set("role", user.getString("role"))
						.set("status", "ENABLED")
						.build();

				txn.put(person);
				txn.commit();
			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password is incorrect!").build();
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
		return Response.ok().build();
	}
	
	public boolean checkPwd(String pwd, String pwd_conf) {
		if(!(pwd.contains("@") ||
				pwd.contains("#") ||
				pwd.contains("!") ||
				pwd.contains("&") ||
				pwd.contains("-") ||
				pwd.contains("+") ||
				pwd.contains("^") ||
				pwd.contains("~") ||
				pwd.contains("*") ||
				pwd.contains("$") ||
				pwd.contains(":") ||
				pwd.contains(",") ||
				pwd.contains("<") ||
				pwd.contains(">") ||
				pwd.contains("?") ||
				pwd.contains(".") ||
				pwd.contains("_") ||
				pwd.contains("/") ||
				pwd.contains("%") ||
				pwd.contains("|") ||
				pwd.contains("(") ||
				pwd.contains(")") ||
				pwd.contains("=")))
			return false;

		if(pwd.length() < 6)
			return false;

		if(!pwd_conf.equals(pwd))
			return false;

		return true;
	}
}