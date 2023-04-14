package pt.unl.fct.di.apdc.adcdemo.resources;

import com.google.cloud.datastore.*;
import pt.unl.fct.di.apdc.adcdemo.util.UpdateData;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateResource {

    private static final Logger LOG = Logger.getLogger(UpdateResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public UpdateResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doList(UpdateData data) {
        LOG.fine("User attempt to update attribute");
        if (data == null || !data.validateData()) {
            LOG.fine("Invalid data: at least one field is null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Invalid data").build();
        }
        Key updaterUserKey = datastore.newKeyFactory().setKind("User").newKey(data.updaterUsername);
        Key updatedUserKey = datastore.newKeyFactory().setKind("User").newKey(data.updatedUsername);
        Transaction txn = datastore.newTransaction();
        try {
            Entity updaterOnDB = txn.get(updaterUserKey);
            Entity updatedOnDB = txn.get(updatedUserKey);
            if (updaterOnDB == null || updatedOnDB == null) {
                LOG.fine("At least one of the users dont exist");
                txn.rollback();
                return Response.status(Response.Status.NOT_FOUND).entity("Not Found - At least one of the users dont exist").build();
            }
            final String updaterRole = updaterOnDB.getString("role");
            final String updatedRole = updatedOnDB.getString("role");
            final List<String> forbiddenUpdates = new ArrayList<>();
            Entity.Builder userChangedBuilder = Entity.newBuilder(txn.get(updatedUserKey));
            for (int i = 0; i < data.attributesNames.length; i++) {
                if (!data.validateUpdatePermissions(updaterRole, updatedRole, data.attributesNames[i], data.attributesValues[i])) {
                    LOG.fine("Dont have permission to update attribute " + data.attributesNames[i] + " of user role " + updatedRole);
                    forbiddenUpdates.add(data.attributesNames[i]);
                } else {
                    userChangedBuilder.set(data.attributesNames[i], data.attributesValues[i]);
                    LOG.fine("Updated attribute " + data.attributesNames[i]);
                }
            }
            Entity userChanged = userChangedBuilder.build();
            txn.put(userChanged);
            if (forbiddenUpdates.isEmpty()) {
                LOG.fine("Updated all attributes");
                txn.commit();
                return Response.ok("Updated all attributes").build();
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : forbiddenUpdates) {
                    sb.append(" " + s);
                }
                LOG.fine("Some properties were not updated because of lack of permissions");
                txn.commit();
                return Response.ok("Some properties were not updated because of lack of permissions: " + sb).build();
            }
        } catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server Error").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server Error").build();
            }
        }

    }
}
