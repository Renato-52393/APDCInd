package pt.unl.fct.di.apdc.projindividual.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {

	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public RemoveResource() {}

	@DELETE
	@Path("/{username}/{user_to_remove}")
	public Response doRemove(@PathParam ("username") String username, @PathParam("user_to_remove") String user_to_remove) {

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
			} if(!username.equals(user_to_remove) && (user.getString("role").equals("User") || user.getString("role").equals("SuperUser"))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You don't have permission to remove this user!").build();
			} if(checkToken(log, username)){
				datastore.delete(userKey);
				datastore.delete(logKey);
				txn.commit();
			}
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
		return Response.ok().build();
	}
	
	public boolean checkToken(Entity log, String username) {
		if(!log.getString("username").equals(username) || log.getLong("expiration_data") < System.currentTimeMillis()){
			return false;
		}
		return true;
	}
	
}
