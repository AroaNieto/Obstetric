package es.obstetrics.obstetric.view.allUsers.templates;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.Icon;

public class TemplateViewSuggestionsWhoWeAreView extends BaseTemplateSuggestionsView {

    private int counter = 1;
    public TemplateViewSuggestionsWhoWeAreView(){
        resetBackground("var(--lumo-primary-color)","Mantén charlas con tu sanitario favorito",
                "¿Una urgencia leve?, nada que no se resuelva con un par de consejos por mensaje.",
                "themes/my-theme/photos/medicalChat.png",
                "A doctor chatting with her patient", "200px",
                "");
    }
    @Override
    protected void setBackground(){
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
                resetBackground("var(--lumo-primary-color)","Mantén charlas con tu sanitario favorito",
                        "¿Una urgencia leve?, nada que no se resuelva con un par de consejos por mensaje.",
                        "themes/my-theme/photos/medicalChat.png",
                        "A doctor chatting with her patient", "200px",
                        "");
                break;

            case 2:
                resetBackground("var(--light-green-color)","Recomendaciones semanales validadas por expertos profesionales",
                        "Cada semana recibirás recomendaciones para seguir aprendiendo sobre tu ciclo.",
                        "themes/my-theme/photos/recommendationsProfessionals.png",
                        "Recommendations validated by professionals", "200px",
                        "");
                break;

            case 3:
                resetBackground("var(--pink-color)","Reserva de citas desde la aplicación",
                        "No es necesario acudir al centro médico para pedir una cita, puedes hacerlo tu mismo cuando y dónde quieras.",
                        "themes/my-theme/photos/AppointmentPatient.png",
                        "A patient books an appointment","200px",
                        "");
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
