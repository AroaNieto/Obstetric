package es.obstetrics.obstetric.resources.templates;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TitleHeaderTemplate extends VerticalLayout {

    public TitleHeaderTemplate(H1 title){

        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, title);
        add(title);
    }
}
