package pt.unl.fct.di.apdc.adcdemo.resources;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.adcdemo.util.RegisterData;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    //    private final Gson g = new Gson();
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public RegisterResource() {
    }

    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegister(RegisterData data) {
        LOG.fine("User attempt to register");
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity person = Entity.newBuilder(userKey).set("password", DigestUtils.sha3_512Hex(data.password))
                .set("timestamp", (Timestamp.now())).build();
        datastore.put(person);
        return Response.ok("Register done.").build();
    }

    @POST
    @Path("/v2")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegisterV2(RegisterData data) {
        LOG.fine("User attempt to register");
        if (data == null || !data.validateRegisterDataV2()) {
            LOG.fine("Invalid data: Null values or pass and confirmation dont match.");
            return Response.status(400, "Bad Request - Invalid data.").build();
        }

        Transaction t = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity personOnDB = t.get(userKey);

            if (personOnDB != null) {
                t.rollback();
                LOG.fine("User already exists!");
                return Response.status(409, "Conflict - username is already taken").build();
            }

            Entity person = Entity.newBuilder(userKey).set("password", DigestUtils.sha3_512Hex(data.password))
                    .set("confirmation", DigestUtils.sha3_512Hex(data.confirmation)).set("email", data.email).set("name", data.name)
                    .set("timestamp", Timestamp.now()).build();
            t.put(person);
            LOG.fine("User was registered: " + data.username);
            t.commit();
            return Response.ok("Register done.").build();

        } finally {
            if (t.isActive()) {
                t.rollback();
                return Response.status(500, "Server Error").build();
            }
        }
    }
}
