package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;

import com.google.gson.Gson;

import pt.unl.fct.di.apdc.projindividual.util.RegisterData;

import org.apache.commons.codec.digest.DigestUtils;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final String U = "USER";
	private static final String E = "ENABLED";

	private final Gson g = new Gson();	

	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");

	public RegisterResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegister(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(data.username);
			Entity user = datastore.get(userKey);

			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} if(!data.email.contains("@")) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Invalid email.").build();
			} if(!checkPwd(data.pwd, data.pwd_conf)) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Invalid password. A minimum of 6 characters and a special character required!").build();
			} else {
				if(data.role.equals(""))
					data.role = U;
				Entity person = Entity.newBuilder(userKey)
						.set("username", data.username)
						.set("email", data.email)
						.set("pwd", DigestUtils.sha512Hex(data.pwd))
						.set("pwd_conf", DigestUtils.sha512Hex(data.pwd_conf))
						.set("profile", data.profile)
						.set("phone", data.phone)
						.set("mobile", data.mobile)
						.set("address", data.address)
						.set("comp_address", data.comp_address)
						.set("local", data.local)
						.set("role", data.role)
						.set("status", E)
						.build();

				txn.add(person);
				LOG.fine("User registered: " + data.username);
				txn.commit();
				return Response.ok(g.toJson(userKey)).build();
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
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