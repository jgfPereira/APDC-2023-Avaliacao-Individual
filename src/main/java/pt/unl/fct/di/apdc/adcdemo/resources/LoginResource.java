package pt.unl.fct.di.apdc.adcdemo.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.adcdemo.util.AuthToken;
import pt.unl.fct.di.apdc.adcdemo.util.LoginData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    public LoginResource() {
    }

    private String hashPass(String pass) {
        return DigestUtils.sha3_512Hex(pass);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data, @Context HttpHeaders headers, @Context HttpServletRequest request) {
        LOG.fine("Login attempt by user " + data.username);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Key loginRegistryKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.username))
                .setKind("LoginRegistry").newKey("loginReg");
        Key loginLogKey = datastore.allocateId(datastore.newKeyFactory()
                .addAncestors(PathElement.of("User", data.username)).setKind("LoginLog").newKey());
        Key loginAuthTokenKey = datastore.newKeyFactory().addAncestors(PathElement.of("LoginLog", loginLogKey.getId()))
                .setKind("AuthToken").newKey("authToken");
        Transaction txn = datastore.newTransaction();
        try {
            Entity userOnDB = txn.get(userKey);
            if (userOnDB != null) {
                Entity loginRegistry = txn.get(loginRegistryKey);
                if (loginRegistry == null) {
                    // creating for the first time
                    loginRegistry = Entity.newBuilder(loginRegistryKey)
                            .set("success_logins", 0L)
                            .set("fail_logins", 0L)
                            .set("first_login", Timestamp.now())
                            .set("last_login", Timestamp.now())
                            .setNull("last_attempt")
                            .build();
                }
                final String givenPasswordHash = hashPass(data.password);
                if (userOnDB.getString("password").equals(givenPasswordHash)) {
                    AuthToken tokenAuth = new AuthToken(data.username);
                    Entity loginLog = Entity.newBuilder(loginLogKey)
                            .set("login_ip", request.getRemoteAddr())
                            .set("login_host", request.getRemoteHost())
                            .set("login_country", headers.getHeaderString("X-AppEngine-Country"))
                            .set("login_city", headers.getHeaderString("X-AppEngine-City"))
                            .set("login_time", Timestamp.now())
                            .set("login_coords",
                                    StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
                                            .setExcludeFromIndexes(true).build())
                            .build();
                    Entity loginAuthToken = Entity.newBuilder(loginAuthTokenKey)
                            .set("tokenID", tokenAuth.tokenID)
                            .set("username", tokenAuth.username)
                            .set("creationDate", tokenAuth.creationDate)
                            .set("expirationDate", tokenAuth.expirationDate)
                            .build();
                    Entity.Builder loginRegistryBuilder = Entity.newBuilder(loginRegistry);
                    loginRegistryBuilder
                            .set("success_logins", 1 + loginRegistry.getLong("success_logins"))
                            .set("last_login", Timestamp.now());
                    Entity loginRegistryNew = loginRegistryBuilder.build();
                    LOG.fine("Password is correct - Generated token and logs");
                    txn.put(loginLog, loginRegistryNew, loginAuthToken);
                    txn.commit();
                    return Response.ok(g.toJson(tokenAuth.tokenID)).build();
                } else {
                    Entity.Builder loginRegistryBuilder = Entity.newBuilder(loginRegistry);
                    loginRegistryBuilder
                            .set("fail_logins", 1 + loginRegistry.getLong("fail_logins"))
                            .set("last_attempt", Timestamp.now());
                    Entity loginRegistryNew = loginRegistryBuilder.build();
                    LOG.fine("Wrong password");
                    txn.put(loginRegistryNew);
                    txn.commit();
                    return Response.status(Status.UNAUTHORIZED).entity("Wrong credentials").build();
                }
            } else {
                LOG.fine("User does not exist");
                return Response.status(Status.UNAUTHORIZED).entity("Wrong credentials").build();
            }
        } catch (Exception e) {
            txn.rollback();
            LOG.fine(e.getLocalizedMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Server Error").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Server Error").build();
            }
        }
    }

    @POST
    @Path("/history")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginTimes(LoginData data) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity userOnDB = datastore.get(userKey);
        if (userOnDB == null) {
            LOG.fine("User dont exist");
            return Response.status(Status.UNAUTHORIZED).entity("Wrong credentials").build();
        }
        // use token instead
        if (userOnDB.getString("password").equals(hashPass(data.password))) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Timestamp yesterday = Timestamp.of(cal.getTime());
            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("LoginLog")
                    .setFilter(CompositeFilter.and(
                            PropertyFilter.hasAncestor(datastore.newKeyFactory().setKind("User").newKey(data.username)),
                            PropertyFilter.ge("login_time", yesterday)))
                    .setOrderBy(OrderBy.desc("login_time"))
                    .setLimit(3)
                    .build();
            QueryResults<Entity> logs = datastore.run(query);
            List<Date> loginTimes = new ArrayList<>();
            logs.forEachRemaining(userLog -> {
                loginTimes.add(userLog.getTimestamp("login_time").toDate());
            });
            return Response.ok(g.toJson(loginTimes)).build();
        } else {
            LOG.fine("Wrong password");
            return Response.status(Status.UNAUTHORIZED).entity("Wrong credentials").build();
        }
    }
}
