package es.obstetrics.obstetric.view.allUsers;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import es.obstetrics.obstetric.resources.templates.HeaderTemplate;
import es.obstetrics.obstetric.view.login.LoginView;

/**
 * Clase encarga de navegar a las distintas clases públicas:
 *   - {@link WhoWeAreView}
 *   - {@link GuideForPregnantWomenView}
 *   - {@link LoginView}
 *
 * Puede acceder cualquier tipo de usuario, tanto registrados como no registrados.
 *
 */
public class HomeDiv extends Div {
    /**
     * Constructor de la clase HomeDiv
     *  Crea el Hl dónde iran los enlaces.
     */
    public HomeDiv() {
        HorizontalLayout homeHl = new HorizontalLayout(
                new HeaderTemplate("header-template"),
                getCenterNav(), getStartNav());
        homeHl.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(homeHl); //Se añaden todos los elementos necesarios a la barra de navegación
    }

    /**
     * El método añade los enlaces dónde se insertarán los enlaces a las clases
     *      *  {@link WhoWeAreView} y {@link GuideForPregnantWomenView}
     *
     * @return el Hl con los enlaces.
     */
    public HorizontalLayout getCenterNav(){

        RouterLink whoWeAreLink = createLink("QUIÉNES SOMOS");
        whoWeAreLink.setRoute(WhoWeAreView.class);

        RouterLink articlesLink = createLink("GUÍA PARA LA EMBARAZADA");
        articlesLink.setRoute(GuideForPregnantWomenView.class);

        HorizontalLayout centerNavHl = new HorizontalLayout(whoWeAreLink, articlesLink);
        centerNavHl.setWidthFull();
        centerNavHl.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        return centerNavHl;
    }

    /**
     * El método añade el enlace a la clase {@link LoginView}
     *
     * @return el Hl con el enlace.
     */
    public HorizontalLayout getStartNav(){

        RouterLink startLink = createLink("COMENZAR");
        startLink.setRoute(LoginView.class);
        startLink.addClassName("start-link");
        HorizontalLayout startNavHL = new HorizontalLayout(startLink);
        startNavHL.setWidthFull();
        startNavHL.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        startNavHL.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        startNavHL.setSpacing(true);
        return  startNavHL;
    }

    /**
     * El método crea todos los enlaces
     * @return el RouterLink de cada uno de los enlaces.
     */
    private RouterLink createLink(String linkName) {
        RouterLink link = new RouterLink();
        link.add(linkName);
        link.addClassName("create-link");
        return link;
    }
}



