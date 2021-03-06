package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

@Path("/logout")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");
	private final KeyFactory logKind = datastore.newKeyFactory().setKind("Log");

	public LogoutResource() {}

	@DELETE
	@Path("/{username}")
	public Response doLogout(@PathParam ("username") String username) {

		LOG.fine("Login attempt by user: " + username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(username);
			Entity user = datastore.get(userKey);

			Key logKey = logKind.newKey(username);
			Entity log = datastore.get(logKey);

			if(user == null) {
				txn.rollback();
				LOG.warning("Failed login attempt for username " + username);
				return Response.status(Status.FORBIDDEN).entity("Failed login attempt for username" + username).build();
			}
			if(log == null) {
				txn.rollback();
				LOG.warning("User is not logged in!" + username);
				return Response.status(Status.FORBIDDEN).entity("User is already logged in!" + username).build();
			} else {
				datastore.delete(logKey);
				txn.commit();

				LOG.info("User '" + username + "' logged in successfully.");
				return Response.ok().build();
			}
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
