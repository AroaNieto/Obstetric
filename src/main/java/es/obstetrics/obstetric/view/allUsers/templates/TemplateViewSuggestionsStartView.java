package es.obstetrics.obstetric.view.allUsers.templates;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.Icon;

public class TemplateViewSuggestionsStartView extends BaseTemplateSuggestionsView{
    int counter = 1;
    public TemplateViewSuggestionsStartView(String backgroundImage){
        resetBackground("","",
                "",
                "",
                "","",
                backgroundImage);
        addClassName("template-view-suggestions-start");
        setSizeFull();
    }
    @Override
    protected void setBackground() {
        if(counter == 0){
            counter = 3;
        }else if(counter == 4){
            counter = 1;
        }
        circlesIconsView.resetCircles(counter); //Se establece el color correcto del circulo

        /*
            Se cambia el fondo con su respectivo color, imágen y texto
         */
        switch (counter){
            case 1:
                resetBackground("","",
                        "",
                        "",
                        "Mother breastfeeding her baby","",
                        "url('themes/my-theme/photos/mother.png')"
                        );
                break;

            case 2:
                resetBackground("","",
                        "",
                        "","Doctor looking at a pregnant woman's belly during an ultrasound.","",
                        "url('themes/my-theme/photos/motherAndDoctor.png')");
                break;

            case 3:
                resetBackground("","",
                        "",
                        "",
                        "Baby sleeping in her mother's belly","",
                        "url('themes/my-theme/photos/baby.png')");
                break;
    }
    }
    @Override
    protected void handleCircle1Icon(ClickEvent<Icon> e) {
        counter = 1;
        setBackground();
    }

    @Override
    protected void handleCircle2Icon(ClickEvent<Icon> e) {
        counter= 2;
        setBackground();
    }

    @Override
    protected void handleCircle3Icon(ClickEvent<Icon> e) {
        counter = 3;
        setBackground();
    }

    @Override
    protected void handleAngleRightIcon(ClickEvent<Icon> e) {
        counter++;
        setBackground();
    }

    @Override
    protected void handleAngleLeftIcon(ClickEvent<Icon> e) {
        counter--;
        setBackground();
    }


}
