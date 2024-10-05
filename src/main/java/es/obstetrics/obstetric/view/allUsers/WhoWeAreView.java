package es.obstetrics.obstetric.view.allUsers;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.view.allUsers.templates.TemplateViewSuggestionsWhoWeAreView;

/**
 * Clase encarga de mostrar información sobre la aplicación.
 *   Extiende de {@link HomeDiv}, pudiendo navegar
 *   a la clase  {@link GuideForPregnantWomenView} o  {@link es.obstetrics.obstetric.view.login.LoginView}
 *
 * Puede acceder cualquier tipo de usuario, tanto registrados como no registrados.
 *
 */
@Route(value = "public/whoWeAre")
@AnonymousAllowed
public class WhoWeAreView extends HomeDiv {

    /**
     * Constructor, añade la plantilla con las fotografías y VL de quienes somos.
     */
    public WhoWeAreView(){
        add(new TemplateViewSuggestionsWhoWeAreView(), getWhoWeAreVl());
        addClassNames("background-gray-color");
        setSizeFull();
    }

    /*
      Creación del vertical layout dónde aparecerá el contenido de quienes somos.
    */
    private VerticalLayout getWhoWeAreVl(){
        VerticalLayout whoWeAreVl = new VerticalLayout();

        H2 titleWhoWeAre = new H2("Cuidando tu embarazo, conectando tu salud");
        titleWhoWeAre.getStyle().set("padding", "var(--lumo-size-s)"); //Se establece una separación

        // Secciones adicionales con imágenes
        HorizontalLayout missionLayout = new HorizontalLayout();
        Image missionImage = new Image("themes/my-theme/photos/ultrasoundAndMother.png", "Nuestra misión");
        missionImage.setHeight("240px");
        missionImage.addClassName("responsive-image");
        missionLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        missionLayout.add(new H4("Bienvenid@ a MotherBloom, un recurso confiable para la gestión integral de procesos obstétricos. Nuestra misión es proporcionar una plataforma innovadora " +
                "y fácil de usar que facilite la comunicación y el manejo de la información entre médicos y pacientes durante el embarazo y el parto."));
        missionLayout.setPadding(true);
        missionLayout.addClassName("responsive-layout");

        HorizontalLayout visionLayout = new HorizontalLayout();
        visionLayout.setPadding(true);
        visionLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        Image visionImage = new Image("themes/my-theme/photos/doctorAndMotherImg.png", "Nuestra visión");
        visionImage.setHeight("240px");
        visionImage.addClassName("responsive-image");
        Image teamImage = new Image("themes/my-theme/photos/motherAndTeddy.png", "Nuestro equipo");
        teamImage.setHeight("240px");
        teamImage.addClassName("responsive-image");
        visionLayout.add(missionImage,visionImage, teamImage);
        visionLayout.addClassName("responsive-layout");

        HorizontalLayout teamHL = new HorizontalLayout();
        teamHL.setPadding(true);
        teamHL.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        teamHL.add(new H4("En MotherBloom, creemos que la atención obstétrica debe ser accesible, eficiente y centrada en el paciente. Nuestro objetivo es mejorar la experiencia " +
                "tanto de los profesionales de la salud como de las futuras madres a través de una tecnología avanzada y segura."));
        teamHL.addClassName("responsive-layout");

        whoWeAreVl.add(titleWhoWeAre, missionLayout, visionLayout, teamHL);
        whoWeAreVl.setSpacing(true);
        whoWeAreVl.setPadding(true);
        whoWeAreVl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        whoWeAreVl.addClassName("whoWeAre-Vl");
        whoWeAreVl.setHeightFull();
        return whoWeAreVl;
    }

}
