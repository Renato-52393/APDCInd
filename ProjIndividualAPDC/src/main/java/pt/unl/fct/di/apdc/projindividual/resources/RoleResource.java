package pt.unl.fct.di.apdc.projindividual.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

@Path("/role")
public class RoleResource {

	private final Datastore datastore =	DatastoreOptions.getDefaultInstance().getService();
	private final KeyFactory userKind = datastore.newKeyFactory().setKind("User");

	private final Gson g = new Gson();

	public RoleResource() {}

	@GET
	@Path("/{username}/{role}")
	public Response doRole(@PathParam ("username") String username, @PathParam("role") String role) {

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = userKind.newKey(username);
			Entity user = datastore.get(userKey);
			
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}
			if(user.getString("role").equals("GBO") || user.getString("role").equals("GA") || user.getString("role").equals("SU")) {
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("User")
						.setFilter(PropertyFilter.eq("role", role))
						.build();

				QueryResults<Entity> attributes = datastore.run(query);

				List<String> users = new ArrayList();
				
				attributes.forEachRemaining(userlog -> {users.add(userlog.getString("username"));
				});
				txn.commit();
				return Response.ok(g.toJson(users)).build();
			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Operation not allowed!").build();
			}
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
