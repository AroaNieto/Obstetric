package es.obstetrics.obstetric.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@Scope("session") //Alcance de sesión, cada usuario tendrá su propia instancia de usuario
public class UserCurrent {
    private UserEntity currentUser;
}
