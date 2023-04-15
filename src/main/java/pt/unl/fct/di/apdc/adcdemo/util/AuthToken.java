package pt.unl.fct.di.apdc.adcdemo.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2h
    private static final String AUTH_TYPE = "Bearer";
    public String username;
    public String tokenID;
    public long creationDate;
    public long expirationDate;

    public AuthToken() {
    }

    public AuthToken(String username) {
        this.username = username;
        this.tokenID = UUID.randomUUID().toString();
        this.creationDate = System.currentTimeMillis();
        this.expirationDate = this.creationDate + AuthToken.EXPIRATION_TIME;
    }

    public AuthToken(String username, String tokenID, long creationDate, long expirationDate) {
        this.username = username;
        this.tokenID = tokenID;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static boolean isValid(long expDate) {
        return System.currentTimeMillis() <= expDate;
    }

    public static String getAuthHeader(HttpServletRequest request) {
        final String[] split = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ");
        return split[0].equals(AUTH_TYPE) ? split[1] : null;
    }
}