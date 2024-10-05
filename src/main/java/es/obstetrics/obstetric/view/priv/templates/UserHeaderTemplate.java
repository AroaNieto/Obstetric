package es.obstetrics.obstetric.view.priv.templates;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import lombok.Getter;

import java.io.ByteArrayInputStream;

@Getter
public class UserHeaderTemplate extends Div {

    private final Button button;

    public UserHeaderTemplate(Icon icon, UserEntity user, H3 secondaryTitle){
        button = createButton(icon);
        HorizontalLayout horizontalLayout;
        if(user.getName() != null){
            Avatar avatarName = new Avatar(user.getName());
            avatarName.setWidth("60px");
            avatarName.setHeight("60px");
            if(user.getProfilePhoto() == null){
                avatarName.setImage("themes/my-theme/icon/pregnance-icon.png");
            }else{
                StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(user.getProfilePhoto()));
                avatarName.setImageResource(resource);
            }
            Div userDatesDiv = new Div(new H4(user.getName() + " " + user.getLastName()), new H5(user.getRole().toUpperCase()));
            horizontalLayout = new HorizontalLayout(button, avatarName, userDatesDiv);
            add(horizontalLayout);
        }else{
            secondaryTitle.addClassName("color-dark-green");
            if(secondaryTitle.equals("Mi carpeta")){
                horizontalLayout = new HorizontalLayout(button, secondaryTitle);
            }else{
                Icon angleRight = new Icon(VaadinIcon.ANGLE_RIGHT);
                angleRight.setColor("var(--dark-green-color)");
                HorizontalLayout iconAndButton = new HorizontalLayout(button,angleRight);
                iconAndButton.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                iconAndButton.setSpacing(false);
                horizontalLayout = new HorizontalLayout(iconAndButton, secondaryTitle);
            }
            add(horizontalLayout);
        }

        horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        horizontalLayout.setPadding(true);
        addClassName("user-header-template");
    }

    private Button createButton(Icon icon){
        Button button = new Button(icon);
        button.addClassName("dark-green-color-button");
        return button;
    }
}
