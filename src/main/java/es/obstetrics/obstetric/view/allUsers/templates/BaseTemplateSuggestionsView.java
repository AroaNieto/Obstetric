package es.obstetrics.obstetric.view.allUsers.templates;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class BaseTemplateSuggestionsView extends Div {
    protected HorizontalLayout suggestionsHl;
    protected H3 titleSuggestions;
    protected H6 bodySuggestions ;
    protected Image img;
    protected CirclesIconsView circlesIconsView;

    public BaseTemplateSuggestionsView(){

        titleSuggestions = new H3();
        suggestionsHl = new HorizontalLayout();
        bodySuggestions = new H6();
        img =new Image();

        circlesIconsView = new CirclesIconsView(
                this::handleCircle1Icon, this::handleCircle2Icon, this::handleCircle3Icon);
        createTemplateStructure();
    }
    protected abstract void  handleCircle1Icon(ClickEvent<Icon> e);

    protected abstract void handleCircle2Icon(ClickEvent<Icon> e);

    protected abstract void handleCircle3Icon(ClickEvent<Icon> e);

    protected abstract void handleAngleRightIcon(ClickEvent<Icon> e);

    protected abstract void handleAngleLeftIcon(ClickEvent<Icon> e);
    protected abstract void setBackground();
    private void createTemplateStructure(){

        Icon angleLeftIcon = VaadinIcon.ANGLE_LEFT.create();
        Icon angleRightIcon = VaadinIcon.ANGLE_RIGHT.create();

        AnglesIconsView anglesIconsView = new AnglesIconsView(this::handleAngleRightIcon, this::handleAngleLeftIcon,
                angleLeftIcon, angleRightIcon);

        VerticalLayout textSuggestionsHl = new VerticalLayout(titleSuggestions, bodySuggestions);
        HorizontalLayout photoSuggestionsHl = new HorizontalLayout(textSuggestionsHl, img);

        VerticalLayout containerSuggestionsVl = new VerticalLayout(photoSuggestionsHl, circlesIconsView);
        suggestionsHl.add(angleLeftIcon, containerSuggestionsVl, angleRightIcon);
        add(suggestionsHl);

        containerSuggestionsVl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        circlesIconsView.setSizeFull();
        circlesIconsView.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        circlesIconsView.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        textSuggestionsHl.setWidthFull();
        textSuggestionsHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        textSuggestionsHl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        suggestionsHl.setHeightFull();
        suggestionsHl.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, angleLeftIcon);
        suggestionsHl.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, angleRightIcon);
    }
    protected void resetBackground(String backgroundColor, String title, String body, String src, String alt, String imgHeight, String backgroundImage){
        titleSuggestions.setText(title);
        bodySuggestions.setText(body);

        img.setSrc(src);
        img.setAlt(alt);
        img.setHeight(imgHeight);
        suggestionsHl.getStyle().set("background-color",backgroundColor)
                .set("background-image", backgroundImage)
                .set("background-repeat", "no-repeat")
                .set("background-size", "cover");
    }


}

