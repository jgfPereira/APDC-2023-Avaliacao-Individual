package pt.unl.fct.di.apdc.adcdemo.util;

public class RegisterData {

    public String username;
    public String password;
    public String confirmation;
    public String email;
    public String name;

    public RegisterData() {
    }

    public RegisterData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean validateRegisterDataV2() {
        if (this.username == null || this.password == null || this.confirmation == null || this.email == null
                || this.name == null) {
            return false;
        }
        return this.password.equals(this.confirmation);
    }
}
