package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

import pt.unl.fct.di.apdc.projindividual.util.RegisterData;

@Path("/update")
public class UpdateResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public UpdateResource() {}

	@PUT
	@Path("/attributes/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdate(RegisterData data, @PathParam ("username") String username) {

		LOG.fine("Update attempt by user: " + username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(username);
			Entity user = datastore.get(userKey);

			Key logKey = logKind.newKey(username);
			Entity log = datastore.get(logKey);
			
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			} if(log == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged in!").build();
			} 	
			checkInput(data, user);
			if(data.username.equals("")) {
				data.username = user.getString("username");
				
				user = Entity.newBuilder(userKey)
						.set("username", data.username)
						.set("email", data.email)
						.set("pwd", DigestUtils.sha512Hex(data.pwd))
						.set("pwd_conf", data.pwd_conf)
						.set("profile", data.profile)
						.set("phone", data.phone)
						.set("mobile", data.mobile)
						.set("address", data.address)
						.set("comp_address", data.comp_address)
						.set("local", data.local)
						.build();
				
				log = Entity.newBuilder(logKey)
						.set("username", data.username)
						.set("pwd", DigestUtils.sha512Hex(data.pwd))
						.set("role", data.role)
						.build();

				txn.put(user, log);
				txn.commit();
				
			} else {
				Key newUserKey = userKind.newKey(data.username);
				Entity newUser = datastore.get(newUserKey);

				Key newLogKey = logKind.newKey(data.username);
				Entity newLog = datastore.get(newLogKey);
				
				if(newUser != null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Username already exists.").build();
				} if(newLog != null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User is already logged in!").build();
				} 
				
				newUser = Entity.newBuilder(newUserKey)
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
						.build();
				
				newLog = Entity.newBuilder(newLogKey)
						.set("username", data.username)
						.set("pwd", DigestUtils.sha512Hex(data.pwd))
						.set("role", data.role)
						.build();
				
				datastore.delete(userKey);
				datastore.delete(logKey);
				txn.put(newUser, newLog);
				txn.commit();
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
		return Response.ok().build();
	}

	public void checkInput(RegisterData data, Entity user) {
		if(data.email.equals("")) 
			data.email = user.getString("email");
		if(data.pwd.equals("")) 
			data.pwd = user.getString("pwd");
		if(data.pwd_conf.equals("")) 
			data.pwd_conf = user.getString("pwd_conf");
		if(data.profile.equals(""))
			data.profile = user.getString("profile");
		if(data.phone.equals(""))
			data.phone = user.getString("phone");
		if(data.mobile.equals(""))
			data.mobile = user.getString("mobile");
		if(data.address.equals(""))
			data.address = user.getString("address");
		if(data.comp_address.equals(""))
			data.comp_address = user.getString("comp_address");
		if(data.local.equals(""))
			data.local = user.getString("local");
	}
}
