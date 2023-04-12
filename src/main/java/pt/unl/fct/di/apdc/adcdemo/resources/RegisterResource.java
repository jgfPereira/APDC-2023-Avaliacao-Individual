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

    private String hashPass(String pass) {
        return DigestUtils.sha3_512Hex(pass);
    }

    private void setWithNulls(Entity.Builder eb, String name, String value) {
        if (value == null) {
            eb.setNull(name);
        } else {
            eb.set(name, value);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegister(RegisterData data) {
        LOG.fine("User attempt to register");
        if (data == null || !data.validateData()) {
            LOG.fine("Invalid data: at least one field is null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Invalid data").build();
        } else if (!data.validatePasswords()) {
            LOG.fine("Passwords dont match");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Passwords dont match").build();
        } else if (!data.validatePasswordConstraints()) {
            LOG.fine("Passwords dont meet constraints");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - password dont meet constraints").build();
        } else if (!data.validateZipCode()) {
            LOG.fine("Zip code dont meet constraints");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Zip code dont meet constraints").build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Transaction txn = datastore.newTransaction();
        try {
            Entity userOnDB = txn.get(userKey);
            if (userOnDB != null) {
                LOG.fine("User already exists");
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Conflict - username is already taken").build();
            }
            Entity.Builder eb = Entity.newBuilder(userKey)
                    .set("password", hashPass(data.password))
                    .set("passConf", hashPass(data.passConf))
                    .set("email", data.email)
                    .set("name", data.name)
                    .set("creationDate", Timestamp.now())
                    .set("role", RegisterData.DEFAULT_ROLE)
                    .set("state", RegisterData.DEFAULT_STATE);
            setWithNulls(eb, "visibility", data.visibility);
            setWithNulls(eb, "homePhoneNum", data.homePhoneNum);
            setWithNulls(eb, "phoneNum", data.phoneNum);
            setWithNulls(eb, "occupation", data.occupation);
            setWithNulls(eb, "placeOfWork", data.placeOfWork);
            setWithNulls(eb, "nif", data.nif);
            setWithNulls(eb, "street", data.street);
            setWithNulls(eb, "locale", data.locale);
            setWithNulls(eb, "zipCode", data.zipCode);
            Entity user = eb.build();
            txn.put(user);
            LOG.fine("Register done: " + data.username);
            txn.commit();
            return Response.ok("Register done").build();
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