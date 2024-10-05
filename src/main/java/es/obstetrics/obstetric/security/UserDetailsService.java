package es.obstetrics.obstetric.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.entity.UserPrincipal;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserService userService;

    @Autowired
    public UserDetailsService(AuthenticationContext authenticationContext,
                              UserService userService) {

        this.userService = userService;
    }

    public static void logout() {

        UI.getCurrent().getPage().setLocation("/login"); // Redirigir a /login después del logout
        VaadinSession.getCurrent().getSession().invalidate();
    }

    public UserPrincipal getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findOneByUsername(username);
        if (user == null || user.getState().equalsIgnoreCase(ConstantUtilities.STATE_INACTIVE)) {
            throw new UsernameNotFoundException(username);
        }

        return new UserPrincipal(user);
    }
}