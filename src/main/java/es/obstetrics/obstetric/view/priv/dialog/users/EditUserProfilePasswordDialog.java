package es.obstetrics.obstetric.view.priv.dialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.ChangePasswordConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UIScope
@Component
public class EditUserProfilePasswordDialog extends MasterDialog {
    private final PasswordEncoder passwordEncoder;
    private final PasswordField password;
    private final PasswordField passwordRepeat;
    private final UserService userService;
    private final H5 errorMessage;
    private final Binder<UserEntity> userEntityBinder;

    @Autowired
    public EditUserProfilePasswordDialog(UserService userService,
                                         PasswordEncoder passwordEncoder){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        password = new PasswordField("Contraseña");
        passwordRepeat = new PasswordField("Repita la contraseña");
        errorMessage = new H5("");
        userEntityBinder = new Binder<>();
        createHeaderDialog();
        createDialogLayout();
    }
    /**
     * Método utilizado para establecer los valores de los campos de diálogo.
     *
     * @param user usuario
     */
    public void setUser(UserEntity user){
        clearTextField();
        userEntityBinder.setBean(user);  //Recojo el usuario
        userEntityBinder.readBean(user);
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Modificar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.users.ProfileUserView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
        setUser(null);
        clearTextField();
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo una nuevo usuario o editando.
     *      - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     *      - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {
        writeBean(userEntityBinder.getBean());
    }

    /**
     * Escribe el valor del contenido en el binder y dispara el evento SaveEvent
     *  para que la clase {@link  SanitaryEntity}
     *  lo recoja y pueda guardar el usuario en la base de datos.
     *
     * @param userEntity Instancia dónde se escribirán los valores
     */
    public void writeBean(UserEntity userEntity){

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password.getValue());

        if (!matcher.matches()) {
            setErrorMessage("La contraseña debe contenter al menos 8 caracteres, de los cuales debe contener una mayúsucla, una minúscula y un dígito.");
            return;
        }else if(password.getValue().isEmpty() || passwordRepeat.getValue().isEmpty()){
            setErrorMessage("Debe rellenar los campos obligatorios.");
            return;
        }else if(!password.getValue().equals(passwordRepeat.getValue())){
            setErrorMessage("Las contraseñas no coinciden.");
            return;
        }
        try {
            userEntityBinder.writeBean(userEntity);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }setErrorMessage("");
        userEntity.setPasswordHash(passwordEncoder.encode(password.getValue()));

        userService.save(userEntity);
        ChangePasswordConfirmDialog editUserProfileConfirmDialog = new ChangePasswordConfirmDialog(userEntity);
        editUserProfileConfirmDialog.open();
        editUserProfileConfirmDialog.addListener(ChangePasswordConfirmDialog.UpdateEvent.class, this::editUserFireEvent);

    }

    private void editUserFireEvent(ChangePasswordConfirmDialog.UpdateEvent updateEvent) {
        fireEvent(new SaveEvent(this, updateEvent.getUserEntity()));
        close();
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo del usuario
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(password, passwordRepeat, errorMessage);
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        password.clear();
        passwordRepeat.clear();
        userEntityBinder.readBean(null);
    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }


    /**
     *  Clase abstracta que extiene de {@link EditUserProfilePasswordDialog}, evento ocurrido en dicha clase.
     *      Almacena el contenido asociado al evento.
     */
    public static  abstract  class ProfileUserFormEvent extends ComponentEvent<EditUserProfilePasswordDialog> {
        private final UserEntity userEntity; //Usuario con el que se trabaja

        protected ProfileUserFormEvent(EditUserProfilePasswordDialog source, UserEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }

        public UserEntity getUser(){
            return userEntity;
        }
    }

    /**
     * Clase heredada de EditUserProfilePasswordDialog, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends ProfileUserFormEvent {
        SaveEvent(EditUserProfilePasswordDialog source, UserEntity userEntity){
            super(source, userEntity);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que maneajrá el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
