package pt.unl.fct.di.apdc.adcdemo.util;

public class RolePermissions {

    private static final String USER_ROLE = "USER";
    private static final String GBO_ROLE = "GBO";
    private static final String GA_ROLE = "GA";
    private static final String GS_ROLE = "GS";
    private static final String SU_ROLE = "SU";

    public static final String[] USER_PERMS = {};
    public static final String[] GBO_PERMS = {USER_ROLE};
    public static final String[] GA_PERMS = {USER_ROLE, GBO_ROLE};
    public static final String[] GS_PERMS = {USER_ROLE, GBO_ROLE, GA_ROLE};
    public static final String[] SU_PERMS = {USER_ROLE, GBO_ROLE, GA_ROLE, GS_ROLE, SU_ROLE};

    public static boolean canRemove(String removerRole, String removedRole) {
        return contains(removerRole, removedRole);
    }

    private static boolean contains(String removerRole, String removedRole) {
        String[] tmp = null;
        switch (removerRole) {
            case USER_ROLE:
                return false;
            case GBO_ROLE:
                tmp = GBO_PERMS;
                break;
            case GA_ROLE:
                tmp = GA_PERMS;
                break;
            case GS_ROLE:
                tmp = GS_PERMS;
                break;
            case SU_ROLE:
                tmp = SU_PERMS;
                break;
        }
        if (tmp == null) {
            return false;
        }
        for (String s : tmp) {
            if (s.equals(removedRole)) {
                return true;
            }
        }
        return false;
    }
}
