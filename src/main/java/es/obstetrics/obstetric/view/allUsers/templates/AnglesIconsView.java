package es.obstetrics.obstetric.view.allUsers.templates;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.Icon;

import java.util.function.Consumer;

public class AnglesIconsView {

    /*
        Se crean los 3 Consumers para cada evento de cada circulo,
            Cada consumer puede aceptar un evento de click sobre
            un circulo y hacer operaciones sobre él.
     */
    private Consumer<ClickEvent<Icon>> angleRightIconConsumer;
    private Consumer<ClickEvent<Icon>> angleLeftIconConsumer;
    private Icon angleRightIcon;
    private  Icon angleLeftIcon;

    /*
    Constructor, inicializa los consumers para que las clases qeu implementen
      esta clase puedan realizar las operaciones correspondientes sobre cada
      uno de ellos.
     */
    public AnglesIconsView(
            Consumer<ClickEvent<Icon>> angleRightIconConsumer,
            Consumer<ClickEvent<Icon>> angleLeftIconConsumer,
            Icon angleLeftIcon, Icon angleRightIcon
    ) {
        this.angleRightIcon = angleRightIcon;
        this.angleLeftIcon = angleLeftIcon;
        this.angleRightIconConsumer = angleRightIconConsumer;
        this.angleLeftIconConsumer = angleLeftIconConsumer;

        createAnglesAndListeners();
    }

    /*
        Creación y asignación de los manejadores de los 3 circulos con sus estilos y tamaños
          correspondientes.
     */
    public void createAnglesAndListeners() {

        angleLeftIcon.setSize("var(--lumo-space-xl)");
        angleRightIcon.setSize("var(--lumo-space-xl)");

        angleRightIcon.getStyle().set("margin", "var(--lumo-space-l)");
        angleLeftIcon.getStyle().set("margin", "var(--lumo-space-l)");

        angleLeftIcon.addClickListener( e-> angleLeftIconConsumer.accept(e));
        angleRightIcon.addClickListener( e-> angleRightIconConsumer.accept(e));
    }

}
