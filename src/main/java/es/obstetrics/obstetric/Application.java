package es.obstetrics.obstetric;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableScheduling
@Push
@Theme("my-theme") //Aplicación de cambios de temas personalizados de Vaadin
@PWA(name = "Proyecto de fin de grado de Obstetricia", shortName = "TFG Obstetricia", offlineResources = {})
public class Application implements AppShellConfigurator {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
