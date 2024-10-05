package es.obstetrics.obstetric.resources.templates;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Clase que crea el hl con el título y el logo principal de la aplicación.
 */
public class HeaderTemplate extends HorizontalLayout {

    /**
     * Constructor de la clase, añade el título y la imagen principal.
     */
    public HeaderTemplate(String titleHeaderTemplate){
        H1 titleH1 = new H1("MotherBloom");
        titleH1.addClassName(titleHeaderTemplate);

        Image logoImg = new Image("themes/my-theme/photos/untitled-logo.png", "logo");
        logoImg.setHeight("60px");

        add(logoImg, titleH1);
        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        addClassName("header-template");
    }

}
