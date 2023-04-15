package pt.unl.fct.di.apdc.adcdemo.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pt.unl.fct.di.apdc.adcdemo.util.AuthToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/showtoken")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowTokenResource {

    private static final Logger LOG = Logger.getLogger(ShowTokenResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    public ShowTokenResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doShowToken(String usernameJSON, @Context HttpHeaders headers, @Context HttpServletRequest request) {
        JsonObject jsonObj = new Gson().fromJson(usernameJSON, JsonObject.class);
        String username = null;
        if (jsonObj == null) {
            LOG.fine("Invalid data");
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Invalid data").build();
        } else {
            JsonElement jsonElement = jsonObj.get("username");
            if (jsonElement == null) {
                LOG.fine("Invalid data");
                return Response.status(Response.Status.BAD_REQUEST).entity("Bad Request - Invalid data").build();
            }
            username = jsonElement.getAsString();
        }
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        String headerToken = AuthToken.getAuthHeader(request);
        if (headerToken == null) {
            LOG.fine("Wrong credentials/token - no auth header or invalid auth type");
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
        Key loginAuthTokenKey = datastore.newKeyFactory()
                .addAncestors(PathElement.of("User", username)).setKind("AuthToken").newKey(headerToken);
        Transaction txn = datastore.newTransaction();
        try {
            Entity tokenOnDB = txn.get(loginAuthTokenKey);
            if (tokenOnDB == null) {
                LOG.fine("Wrong credentials/token - not found");
                txn.rollback();
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            } else {
                boolean isTokenValid = AuthToken.isValid(tokenOnDB.getLong("expirationDate"), tokenOnDB.getBoolean("isRevoked"));
                if (!isTokenValid) {
                    LOG.fine("Expired token");
                    txn.rollback();
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
                }
                LOG.fine("Valid token - proceeding");
            }
            Entity userOnDB = datastore.get(userKey);
            if (userOnDB == null) {
                LOG.fine("User dont exist");
                txn.rollback();
                return Response.status(Response.Status.UNAUTHORIZED).entity("Wrong credentials").build();
            }
            AuthToken resToken = new AuthToken(tokenOnDB.getString("username"),
                    tokenOnDB.getString("tokenID"),
                    tokenOnDB.getLong("creationDate"),
                    tokenOnDB.getLong("expirationDate"),
                    tokenOnDB.getBoolean("isRevoked"));
            LOG.fine("Token info was sent to client");
            txn.commit();
            return Response.ok(g.toJson(resToken)).build();
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