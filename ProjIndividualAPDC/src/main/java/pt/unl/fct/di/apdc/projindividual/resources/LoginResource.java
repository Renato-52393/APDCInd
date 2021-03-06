package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import com.google.gson.Gson;

import pt.unl.fct.di.apdc.projindividual.util.AuthToken;
import pt.unl.fct.di.apdc.projindividual.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	private final Gson g = new Gson();

	public LoginResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data,
			@Context HttpServletRequest request, 
			@Context HttpHeaders headers) {

		LOG.fine("Login attempt by user: " + data.username);

		Key userKey = userKind.newKey(data.username);
		
		Key logKey = logKind.newKey(data.username);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			
			if(user == null) {
				txn.rollback();
				LOG.warning("Failed login attempt for username " + data.username);
				return Response.status(Status.FORBIDDEN).entity("Failed login attempt for username" + data.username).build();
			}
			
			Entity log = txn.get(logKey);
			
			if(log != null) {
				txn.rollback();
				LOG.warning("User is already logged in!" + data.username);
				return Response.status(Status.FORBIDDEN).entity("User is already logged in!" + data.username).build();
			}
			
			String hashedPwd = (String) user.getString("pwd");

			if(hashedPwd.equals(DigestUtils.sha512Hex(data.pwd))) {
				
				AuthToken at = new AuthToken(data.username, user.getString("role"));

				log = Entity.newBuilder(logKey)
						.set("tokenID",  DigestUtils.sha512Hex(at.tokenID))
						.set("username", at.username)
						.set("expiration_data", at.expirationData)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_time",Timestamp.now())
						.set("role", at.role)
						.build();

				txn.put(log);
				txn.commit();
				
				LOG.info("User '" + data.username + "' logged in successfully.");
				return Response.ok(g.toJson(at)).build();
			} else {
				txn.rollback();
				LOG.info("Something went wrong!");
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}