package es.obstetrics.obstetric.view.priv.views.users;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.dialog.users.EditUserProfileDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.EditUserProfilePasswordDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.UploadProfilePhotoDialog;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Route(value = "protected/profile/user", layout = PrincipalView.class)
@PageTitle("MotherBloom - Mi perfil")
@PermitAll
public class ProfileUserView extends  Div{
    private UserEntity userEntity;
    private final UserService userService;
    private Avatar avatar;
    private Div avatarOverlay;
    private final VerticalLayout verticalLayout;
    private final EditUserProfileDialog editUserProfileDialog;
    private final EditUserProfilePasswordDialog editUserProfilePasswordDialog;
    @Autowired
    public ProfileUserView(UserCurrent userCurrent,
                           UserService userService,
                           EditUserProfileDialog editUserProfileDialog,
                           EditUserProfilePasswordDialog editUserProfilePasswordDialog) {
        this.userService = userService;
        this.editUserProfileDialog = editUserProfileDialog;
        this.editUserProfilePasswordDialog = editUserProfilePasswordDialog;
        userEntity = userCurrent.getCurrentUser();

        UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.HOME.create(),new UserEntity(), new H3("Mis datos"));
        header.getButton().addClickListener(buttonClickEvent ->
                UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
        );
        verticalLayout = new VerticalLayout(createPersonalInformation(),
                createButtonsHl());
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        verticalLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(header,
                createPhotoProfile(),
                verticalLayout);
    }

    private Component createButtonsHl() {
        Button editProfileBtn = new Button("Editar perfil", e -> {
            editUserProfileDialog.setUser(userEntity);
            editUserProfileDialog.setHeaderTitle("Modificar mis datos");
            editUserProfileDialog.open();
            editUserProfileDialog.addListener(EditUserProfileDialog.SaveEvent.class, this::updateUser);
        });
        editProfileBtn.setIcon(VaadinIcon.USER_CARD.create());
        editProfileBtn.addClassNames("primary-color-button");

        Button editPasswordProfileBtn = new Button("Modificar contraseña", e -> {
            editUserProfilePasswordDialog.setUser(userEntity);
            editUserProfilePasswordDialog.setHeaderTitle("Modificar contraseña");
            editUserProfilePasswordDialog.open();
            editUserProfilePasswordDialog.addListener(EditUserProfileDialog.SaveEvent.class, this::updatePasswordUser);
        });
        editPasswordProfileBtn.setIcon(VaadinIcon.KEY.create());
        editPasswordProfileBtn.addClassNames("dark-green-button");

        return new HorizontalLayout(editProfileBtn, editPasswordProfileBtn);
    }

    /**
     * Guarda la foto de perfil y la actualiza de la vista.
     */
    private void savePhotoProfile(UploadProfilePhotoDialog.SaveEvent saveEvent) {
        userEntity.setProfilePhoto(saveEvent.getContentByte());
        userService.save(userEntity);
        setPhotoProfile(saveEvent.getContentByte());
        //Actualización de la foto de perfil en el applayout
        if(getParent().isPresent()){
            PrincipalView principalView = (PrincipalView) getParent().get();
            principalView.setPhotoProfile(saveEvent.getContentByte());
        }
    }
    private void setPhotoProfile(byte[] photo) {
        avatarOverlay.setText("Cambiar foto");
        StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(photo));
        avatar.setImageResource(resource);
    }

    private void updatePasswordUser(EditUserProfileDialog.SaveEvent saveEvent) {
        userEntity = saveEvent.getSanitary();
        verticalLayout.removeAll();
        verticalLayout.add(createPersonalInformation(),createButtonsHl());
    }

    private void updateUser(EditUserProfileDialog.SaveEvent saveEvent) {
        userEntity = saveEvent.getSanitary();
        verticalLayout.removeAll();
        verticalLayout.add(createPersonalInformation(),createButtonsHl());

    }

    private VerticalLayout createPhotoProfile() {

        H4 nameAndUsername = new H4(userEntity.getName() + " "
                + userEntity.getLastName());

        H6 role = new H6("Rol: " + userEntity.getRole());

        VerticalLayout photoProfileVl = new VerticalLayout(createAvatar(),
                nameAndUsername,
                role);
        photoProfileVl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        photoProfileVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        return photoProfileVl;
    }

    private Div createAvatar() {
        avatar = new Avatar(userEntity.getName());
        avatar.setHeight("100px");
        avatar.setWidth("100px");

        avatarOverlay = new Div();
        avatarOverlay.addClassName("avatar-overlay");

        Div avatarDiv = new Div(avatar, avatarOverlay);
        avatarDiv.addClassName("avatar-container");
        avatarDiv.addClickListener(event -> changeProfilePhoto());

        if(userEntity.getProfilePhoto() == null){
            avatarOverlay.setText("Añadir foto");
            if(userEntity.getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)){
                avatar.setImage("themes/my-theme/icon/pregnance-icon.png");
            }else{
                if(userEntity.getSex().equals(ConstantUtilities.SEX_FEMALE)){
                    avatar.setImage("themes/my-theme/icon/female-doctor-icon.png");
                }else{
                    avatar.setImage("themes/my-theme/icon/male-doctor-icon.png");
                }
            }
        }else{
            setPhotoProfile(userEntity.getProfilePhoto());
        }
        return avatarDiv;
    }

    /**
     * Abre el cuadro de dialogo para que el usuario pueda cambiar de foto de perfil y registra
     *  el evento donde se llevará a cabo la actualizacion del usuario con su nueva foto.
     */
    private void changeProfilePhoto() {
        UploadProfilePhotoDialog uploadProfilePhotoDialog = new UploadProfilePhotoDialog();
        uploadProfilePhotoDialog.addListener(UploadProfilePhotoDialog.SaveEvent.class, this::savePhotoProfile);
        uploadProfilePhotoDialog.open();
    }


    public HorizontalLayout createPersonalInformation() {
        FormLayout allDates = new FormLayout(createBoxPersonalInformation(createPersonalInformationBox()), createBoxPersonalInformation(createAddressInformationBox()));
        allDates.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));
        allDates.setSizeFull();
        HorizontalLayout allDatesHl = new HorizontalLayout(allDates);
        allDatesHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        allDatesHl.addClassName("grid-container");
        allDatesHl.setSizeFull();
        allDatesHl.setPadding(true);
        return allDatesHl;
    }

    private FormLayout createAddressInformationBox() {
        Icon icon = createIcon(VaadinIcon.LOCATION_ARROW);
        HorizontalLayout titleAddress  = createTitleH4("Ubicación", icon);

        FormLayout box = createFormLayout();
        box.add(titleAddress,
                createDataDiv("Dirección",userEntity.getAddress()),
                createDataDiv("Código postal",userEntity.getPostalCode()));

        box.setColspan(titleAddress, 2);
        box.addClassName("square-box");
        box.setSizeFull();
        return box;
    }

    private FormLayout createPersonalInformationBox() {
        Icon iconHearth = createIcon(VaadinIcon.USER_HEART);
        HorizontalLayout titlePersonalInformation = createTitleH4("Datos personales", iconHearth);
        FormLayout box = new FormLayout();
        box.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2),
                new FormLayout.ResponsiveStep("600px", 3),
                new FormLayout.ResponsiveStep("800px", 4));

        box.add(titlePersonalInformation,
                createDataDiv("Nombre",userEntity.getName()),
                createDataDiv("Apellidos",userEntity.getLastName()),
                createDataDiv("Nombre de usuario",userEntity.getUsername()),
                createDataDiv("Edad",userEntity.getAge()),
                createDataDiv("DNI",userEntity.getDni()),
                createDataDiv("Teléfono",userEntity.getDni()),
                createDataDiv("Email",userEntity.getEmail()));

        box.setColspan(titlePersonalInformation, 4);
        box.addClassName("square-box");
        box.setSizeFull();
        return box;
    }

    private Icon createIcon(VaadinIcon vaadinIcon){
        Icon icon = new Icon(vaadinIcon);
        icon.setColor("var(--dark-green-color)");
        return icon;
    }

    private Div createDataDiv(String title, String data){
        Div dataDiv = new Div(createTitle(title),createPersonalDateH5(data));
        dataDiv.addClassName("data-container");
        return dataDiv ;
    }

    private HorizontalLayout createBoxPersonalInformation(FormLayout fl){
        return new HorizontalLayout(fl);
    }

    private H5 createPersonalDateH5(String title) {
        H5 titleH5 = new H5(title);
        titleH5.addClassName("color-title");
        return titleH5;
    }

    private Span createTitle(String title) {
        Span titleSpan = new Span(title);
        titleSpan.addClassName("data-personal-info");
        return titleSpan;
    }

    private HorizontalLayout createTitleH4(String titleH4, Icon icon) {
        H4 title = new H4(titleH4);
        title.addClassNames("title-dark-green","margin-title-dark-green");
        HorizontalLayout horizontalLayoutTitleAndIcon = new HorizontalLayout(title,icon);
        horizontalLayoutTitleAndIcon.setSizeFull();
        horizontalLayoutTitleAndIcon.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        return horizontalLayoutTitleAndIcon;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));
        return formLayout;
    }

}