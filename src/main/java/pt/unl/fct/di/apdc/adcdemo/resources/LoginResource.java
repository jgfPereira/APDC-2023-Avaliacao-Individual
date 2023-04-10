package pt.unl.fct.di.apdc.adcdemo.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.adcdemo.util.AuthToken;
import pt.unl.fct.di.apdc.adcdemo.util.LoginData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    public LoginResource() {
    }

    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response doLogin(LoginData data) {
        LOG.fine("Login attempt by user: " + data.username);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity personOnDB = datastore.get(userKey);
        if (personOnDB != null) {
            final String givenPasswordHash = DigestUtils.sha3_512Hex(data.password);
            if (personOnDB.getString("password").equals(givenPasswordHash)) {
                LOG.fine("Password is corret. Generating token...");
                AuthToken at = new AuthToken(data.username);
                return Response.ok(g.toJson(at)).build();
            } else {
                LOG.fine("Wrong password");
                return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
            }
        } else {
            LOG.fine("User does not exist");
            return Response.status(Status.FORBIDDEN).entity("Incorrect username.").build();
        }
    }

    @POST
    @Path("/v2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response doLoginV2(LoginData data, @Context HttpHeaders headers, @Context HttpServletRequest request) {
        LOG.fine("Login attempt by user: " + data.username);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Key loginRegistryKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username))
                .setKind("LoginRegistry").newKey("loginReg");
        Key loginLogKey = datastore.allocateId(datastore.newKeyFactory()
                .addAncestors(PathElement.of("User", data.username)).setKind("LoginLog").newKey());

        Transaction t = datastore.newTransaction();
        try {
            Entity personOnDB = t.get(userKey);
            if (personOnDB != null) {
                Entity loginRegistry = t.get(loginRegistryKey);
                if (loginRegistry == null) {
                    // criar pela primeira vez
                    loginRegistry = Entity.newBuilder(loginRegistryKey).set("sucess_logins", 0).set("fail_logins", 0)
                            .set("first_login", Timestamp.now()).set("last_login", Timestamp.now()).build();
                }

                final String givenPasswordHash = DigestUtils.sha3_512Hex(data.password);
                if (personOnDB.getString("password").equals(givenPasswordHash)) {
                    LOG.fine("Password is corret. Generating token...");
                    AuthToken at = new AuthToken(data.username);

                    Entity loginLog = Entity.newBuilder(loginLogKey).set("login_ip", request.getRemoteAddr())
                            .set("login_host", request.getRemoteHost())
                            .set("login_country", headers.getHeaderString("X-AppEngine-Country"))
                            .set("login_city", headers.getHeaderString("X-AppEngine-City"))
                            .set("login_time", Timestamp.now())
                            .set("login_coordinates",
                                    StringValue.newBuilder(headers.getHeaderString("X-Appengine-CityLatLong"))
                                            .setExcludeFromIndexes(true).build())
                            .build();

                    Entity loginRegistryNew = Entity.newBuilder(loginRegistryKey)
                            .set("sucess_logins", 1 + loginRegistry.getLong("sucess_logins"))
                            .set("fail_logins", loginRegistry.getLong("fail_logins"))
                            .set("first_login", loginRegistry.getTimestamp("first_login"))
                            .set("last_login", Timestamp.now()).build();

                    t.put(loginLog, loginRegistryNew);
                    t.commit();
                    return Response.ok(g.toJson(at)).build();
                } else {
                    LOG.fine("Wrong password");
                    Entity loginRegistryNew = Entity.newBuilder(loginRegistryKey)
                            .set("sucess_logins", loginRegistry.getLong("sucess_logins"))
                            .set("fail_logins", 1 + loginRegistry.getLong("fail_logins"))
                            .set("first_login", loginRegistry.getTimestamp("first_login"))
                            .set("last_login", loginRegistry.getTimestamp("last_login")).build();

                    t.put(loginRegistryNew);
                    t.commit();
                    return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
                }
            } else {
                LOG.fine("User does not exist");
                return Response.status(Status.FORBIDDEN).entity("Incorrect username.").build();
            }

        } catch (Exception e) {
            t.rollback();
            LOG.fine(e.getLocalizedMessage());
            return Response.status(500, "Server Error").build();
        } finally {
            if (t.isActive()) {
                t.rollback();
                return Response.status(500, "Server Error").build();
            }
        }

    }

    @GET
    @Path("/{username}")
    public Response isUsernameAvailable(@PathParam("username") String username) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        if (userKey != null) {
            return Response.ok().entity(g.toJson(false)).build();
        } else {
            return Response.ok().entity(g.toJson(true)).build();
        }
    }
}
