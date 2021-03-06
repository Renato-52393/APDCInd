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

import pt.unl.fct.di.apdc.projindividual.util.StatusData;

@Path("/update")
public class StatusResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public StatusResource() {}

	@PUT
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doStatus(StatusData data) {

		LOG.fine("Promote attempt by user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(data.username);
			Entity user = datastore.get(userKey);

			Key updateKey = userKind.newKey(data.user_to_update);
			Entity update = datastore.get(updateKey);

			Key logKey = logKind.newKey(data.username);
			Entity log = datastore.get(logKey);

			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			} if(log == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged in!").build();
			}  if(update == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist!").build();
			}	if(!(user.getString("role").equals("SU") || user.getString("role").equals("GA") || user.getString("role").equals("GBO"))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
			} else {
				switch(update.getString("role")) {
				case "USER":
					update = Entity.newBuilder(updateKey)
					.set("username", update.getString("username"))
					.set("email", update.getString("email"))
					.set("pwd", DigestUtils.sha512Hex(update.getString("pwd")))
					.set("pwd_conf", update.getString("pwd_conf"))
					.set("profile", update.getString("profile"))
					.set("phone", update.getString("phone"))
					.set("mobile", update.getString("mobile"))
					.set("address", update.getString("address"))
					.set("comp_address", update.getString("comp_address"))
					.set("local", update.getString("local"))
					.set("role", update.getString("role"))
					.set("status",data.status)
					.build();

					txn.update(update);
					txn.commit();
					break;
				case "GBO":
					if(user.getString("role").equals("SU") || user.getString("role").equals("GA")){
						update = Entity.newBuilder(updateKey)
								.set("username", update.getString("username"))
								.set("email", update.getString("email"))
								.set("pwd", DigestUtils.sha512Hex(update.getString("pwd")))
								.set("pwd_conf", update.getString("pwd_conf"))
								.set("profile", update.getString("profile"))
								.set("phone", update.getString("phone"))
								.set("mobile", update.getString("mobile"))
								.set("address", update.getString("address"))
								.set("comp_address", update.getString("comp_address"))
								.set("local", update.getString("local"))
								.set("role", update.getString("role"))
								.set("status",data.status)
								.build();

						txn.update(update);
						txn.commit();
					}else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
					}
					break;
				case "GA":
					if(user.getString("role").equals("SU")){
						update = Entity.newBuilder(updateKey)
								.set("username", update.getString("username"))
								.set("email", update.getString("email"))
								.set("pwd", DigestUtils.sha512Hex(update.getString("pwd")))
								.set("pwd_conf", update.getString("pwd_conf"))
								.set("profile", update.getString("profile"))
								.set("phone", update.getString("phone"))
								.set("mobile", update.getString("mobile"))
								.set("address", update.getString("address"))
								.set("comp_address", update.getString("comp_address"))
								.set("local", update.getString("local"))
								.set("role", update.getString("role"))
								.set("status",data.status)
								.build();

						txn.update(update);
						txn.commit();
					} else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
					}
					break;
				default:
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
				}
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
		return Response.ok().build();
	}
}