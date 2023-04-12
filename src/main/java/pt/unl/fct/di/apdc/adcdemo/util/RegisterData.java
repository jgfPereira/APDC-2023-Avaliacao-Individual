package pt.unl.fct.di.apdc.adcdemo.util;

import org.passay.*;

import java.util.ArrayList;
import java.util.List;

public class RegisterData {

    public String username;
    public String password;
    public String passConf;
    public String email;
    public String name;

    public RegisterData() {
    }

    public boolean validateData() {
        return !(this.username == null || this.password == null || this.passConf == null || this.email == null
                || this.name == null);
    }

    public boolean validatePasswords() {
        return this.password.equals(this.passConf);
    }

    public boolean validatePasswordConstraints() {
        List<Rule> passRules = new ArrayList<>();
        passRules.add(new LengthRule(8, 20));
        passRules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        passRules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        passRules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        passRules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        passRules.add(new WhitespaceRule());

        PasswordValidator passValidator = new PasswordValidator(passRules);
        PasswordData passData = new PasswordData(this.password);
        RuleResult res = passValidator.validate(passData);
        return res.isValid();
    }
}
