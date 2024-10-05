package es.obstetrics.obstetric.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import es.obstetrics.obstetric.view.login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(antMatchers("/admin/**")).hasAnyRole("ADMINISTRADOR")
                .requestMatchers(antMatchers("/patients/**")).hasAnyRole("PACIENTE", "ADMINISTRADOR")
                .requestMatchers(antMatchers("/ginecologo/**")).hasAnyRole("GINECOLOGO", "ADMINISTRADOR")
                .requestMatchers(antMatchers("/matrona/**")).hasAnyRole("MATRONA", "ADMINISTRADOR")
                .requestMatchers(antMatchers("/secretary/**")).hasAnyRole("SECRETARIO", "ADMINISTRADOR")
                .requestMatchers(antMatchers("/protected/**")).hasAnyRole("PACIENTE","MATRONA", "GINECOLOGO", "ADMINISTRADOR")
                .requestMatchers("/sanitaries/**").hasAnyRole("SECRETARIO","MATRONA", "GINECOLOGO", "ADMINISTRADOR")
                .requestMatchers("/workers/**").hasAnyRole("MATRONA", "GINECOLOGO", "SECRETARIO", "ADMINISTRADOR")
                .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                );//Cualquier usuario autenticado podrá acceder a esta ruta

        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Override
    protected void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SimpleUrlAuthenticationSuccessHandler successHandler() {
        return new SimpleUrlAuthenticationSuccessHandler("/protected/profile/user"); // Redirigir siempre a /home después del login
    }
    /*
    @Bean
    public UserDetailsManager userDetailsService() {
        UserDetails user =
                User.withUsername("user")
                        .password("{noop}user")
                        .roles("USER")
                        .build();
        UserDetails admin =
                User.withUsername("admin")
                        .password("{noop}admin")
                        .roles("ADMIN")
                        .build();
        return new InMemoryUserDetailsManager(user, admin);
    }*/
}