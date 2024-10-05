package es.obstetrics.obstetric.backend.entity;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;

@Getter
public class UserPrincipal extends User {
    private UserEntity user;

    public UserPrincipal(UserEntity user) {
        super(user.getUsername(), user.getPasswordHash(), Arrays.asList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())));
        this.user = user;
    }
}
