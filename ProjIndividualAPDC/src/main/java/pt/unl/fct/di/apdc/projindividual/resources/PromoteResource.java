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

import pt.unl.fct.di.apdc.projindividual.util.PromotionData;

@Path("/promote")
public class PromoteResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public PromoteResource() {}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doUpdate(PromotionData data) {

		LOG.fine("Promote attempt by user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(data.username);
			Entity user = datastore.get(userKey);

			Key promoteKey = userKind.newKey(data.user_to_promote);
			Entity promote = datastore.get(promoteKey);

			Key logKey = logKind.newKey(data.username);
			Entity log = datastore.get(logKey);

			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			} if(log == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged in!").build();
			}  if(promote == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist!").build();
			}	if(!(user.getString("role").equals("SU") || user.getString("role").equals("GA"))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
			} else {
				switch(data.role) {
				case "GA":
					if((user.getString("role").equals("SU"))){
						
						promote = Entity.newBuilder(promoteKey)
								.set("username", promote.getString("username"))
								.set("email", promote.getString("email"))
								.set("pwd", DigestUtils.sha512Hex(promote.getString("pwd")))
								.set("pwd_conf", promote.getString("pwd_conf"))
								.set("profile", promote.getString("profile"))
								.set("phone", promote.getString("phone"))
								.set("mobile", promote.getString("mobile"))
								.set("address", promote.getString("address"))
								.set("comp_address", promote.getString("comp_address"))
								.set("local", promote.getString("local"))
								.set("role", data.role)
								.set("status",promote.getString("status"))
								.build();
						
						txn.update(promote);
						txn.commit();
					}
					break;
				case"GBO":
					promote = Entity.newBuilder(promoteKey)
					.set("username", promote.getString("username"))
					.set("email", promote.getString("email"))
					.set("pwd", DigestUtils.sha512Hex(promote.getString("pwd")))
					.set("pwd_conf", promote.getString("pwd_conf"))
					.set("profile", promote.getString("profile"))
					.set("phone", promote.getString("phone"))
					.set("mobile", promote.getString("mobile"))
					.set("address", promote.getString("address"))
					.set("comp_address", promote.getString("comp_address"))
					.set("local", promote.getString("local"))
					.set("role", data.role)
					.set("status",promote.getString("status"))
					.build();

					txn.update(promote);
					txn.commit();
					break;
				default:
					break;
				}
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
		return Response.ok().build();
	}
}