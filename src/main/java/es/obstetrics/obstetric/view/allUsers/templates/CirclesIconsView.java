package es.obstetrics.obstetric.view.allUsers.templates;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;

public class CirclesIconsView extends HorizontalLayout{

    private Icon circle1;
    private  Icon circle2;
    private  Icon circle3;
    /*
        Se crean los 3 Consumers para cada evento de cada circulo,
            Cada consumer puede aceptar un evento de click sobre
            un circulo y hacer operaciones sobre él.
     */
    private Consumer<ClickEvent<Icon>> circle1IconConsumer;
    private Consumer<ClickEvent<Icon>> circle2IconConsumer;
    private Consumer<ClickEvent<Icon>> circle3IconConsumer;
    /*
    Constructor, inicializa los consumers para que las clases qeu implementen
      esta clase puedan realizar las operaciones correspondientes sobre cada
      uno de ellos.
     */
    public CirclesIconsView(
            Consumer<ClickEvent<Icon>> circle1IconConsumer,
            Consumer<ClickEvent<Icon>> circle2IconConsumer,
            Consumer<ClickEvent<Icon>> circle3IconConsumer
    ) {
        this.circle1IconConsumer = circle1IconConsumer;
        this.circle2IconConsumer = circle2IconConsumer;
        this.circle3IconConsumer = circle3IconConsumer;
        createCirclesAndListeners();
    }

    /*
        Creación y asignación de los manejadores de los 3 circulos con sus estilos y tamaños
          correspondientes.
     */
    public void createCirclesAndListeners() {

        circle1 = VaadinIcon.CIRCLE.create();
        circle2 = VaadinIcon.CIRCLE.create();
        circle3 = VaadinIcon.CIRCLE.create();

        //Colocará verticalmente los 3 circulos con photoSuggestions
        circle1.setSize("var(--lumo-space-m)");
        circle2.setSize("var(--lumo-space-m)");
        circle3.setSize("var(--lumo-space-m)");
        circle1.setColor("var(--lumo-contrast-90pct)");
        circle2.setColor("var(--lumo-contrast-60pct)");
        circle3.setColor("var(--lumo-contrast-60pct)");

        /*
            Cuando se produzca un evento click en el circulo se activará
                el consumer con sus operaciones correspondientes.
         */
        circle1.addClickListener( e-> circle1IconConsumer.accept(e));
        circle2.addClickListener( e-> circle2IconConsumer.accept(e));
        circle3.addClickListener( e-> circle3IconConsumer.accept(e));

        add(circle1, circle2, circle3);
    }

    public void setColorsCircle1_90pc(){
        //Colocará verticalmente los 3 circulos con photoSuggestions
        circle1.setColor("var(--lumo-contrast-90pct)");
        circle2.setColor("var(--lumo-contrast-60pct)");
        circle3.setColor("var(--lumo-contrast-60pct)");
    }

    public void setColorsCircle2_90pc(){
        circle1.setColor("var(--lumo-contrast-60pct)");
        circle2.setColor("var(--lumo-contrast-90pct)");
        circle3.setColor("var(--lumo-contrast-60pct)");
    }

    public void setColorsCircle3_90pc(){
        circle1.setColor("var(--lumo-contrast-60pct)");
        circle2.setColor("var(--lumo-contrast-60pct)");
        circle3.setColor("var(--lumo-contrast-90pct)");
    }

    public void resetCircles(int counter){
        switch (counter){
            case 1:
                setColorsCircle1_90pc();
                break;
            case 2:
                setColorsCircle2_90pc();
                break;
            case 3:
                setColorsCircle3_90pc();
                break;
        }
    }
}
