package es.obstetrics.obstetric.view.priv.dialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.EditUserProfileConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@UIScope
@Component
public class EditUserProfileDialog extends MasterDialog {

    private final Binder<UserEntity> userEntityBinder;
    private final TextField name;
    private final TextField lastName;
    private final TextField dni;
    private final TextField address;
    private final TextField phone;
    private final EmailField email;
    private final TextField postalCode;
    private final TextField age;
    private final TextField username;
    private final H5 errorMessage;
    private final UserService userService;

    @Autowired
    public EditUserProfileDialog(UserService userService){
        this.userService = userService;
        name = new TextField("Nombre");
        username = new TextField("Nombre de usuario");
        lastName= new TextField("Apellidos");
        dni= new TextField("DNI");
        address= new TextField("Dirección");
        phone= new TextField("Teléfono");
        age= new TextField("Edad");
        email= new EmailField("Email");
        postalCode= new TextField("Código postal");
        errorMessage = new H5("");

        createHeaderDialog();
        createDialogLayout();
        userEntityBinder = new BeanValidationBinder<>(UserEntity.class);

        userEntityBinder.bindInstanceFields(this);
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

        try {
            userEntityBinder.writeBean(userEntity);
            Optional<UserEntity> originalUser = userService.findById(userEntity.getId());

            if (!userEntity.getDni().equals(originalUser.get().getDni()) && userService.findOneByDni(userEntity.getDni()) != null) {
                setErrorMessage("El DNI ya está en uso.");
                return;
            }

            // Verificar si se ha modificado el correo electrónico
            if (!userEntity.getEmail().equals(originalUser.get().getEmail()) && userService.findOneByEmail(userEntity.getEmail()) != null) {
                setErrorMessage("El correo electrónico ya está en uso.");
                return;
            }

            // Verificar si se ha modificado el telefono
            if (!userEntity.getPhone().equals(originalUser.get().getPhone()) && userService.findOneByPhone(userEntity.getPhone()) != null) {
                setErrorMessage("El teléfono móvil ya esta en uso.");
                return;
            }
            setErrorMessage("");
            userService.save(userEntity);
            EditUserProfileConfirmDialog editUserProfileConfirmDialog = new EditUserProfileConfirmDialog(userEntity);
            editUserProfileConfirmDialog.open();
            editUserProfileConfirmDialog.addListener(EditUserProfileConfirmDialog.UpdateEvent.class, this::editUserFireEvent);

        } catch (ValidationException e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void editUserFireEvent(EditUserProfileConfirmDialog.UpdateEvent updateEvent) {
        fireEvent(new SaveEvent(this, updateEvent.getUser()));
        close();
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo del usuario
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {

        HorizontalLayout nameAndLastnameHl = new HorizontalLayout(name, lastName);
        nameAndLastnameHl.setSizeFull();
        nameAndLastnameHl.expand(name,lastName);

        HorizontalLayout dniAndAgeHl = new HorizontalLayout(age, dni);
        dniAndAgeHl.setSizeFull();
        dniAndAgeHl.expand(age,dni);

        HorizontalLayout phoneEmailHl = new HorizontalLayout(phone, email);
        phoneEmailHl.setSizeFull();
        phoneEmailHl.expand(phone,email);

        HorizontalLayout postalCodeAndSexHl = new HorizontalLayout(address,postalCode);
        postalCodeAndSexHl.setSizeFull();
        postalCodeAndSexHl.expand(address,postalCode);

        HorizontalLayout passwordAndUsernameHl = new HorizontalLayout(username);
        passwordAndUsernameHl.setSizeFull();
        passwordAndUsernameHl.expand(username);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(nameAndLastnameHl,dniAndAgeHl,phoneEmailHl, postalCodeAndSexHl, passwordAndUsernameHl, errorMessage);
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
        username.clear();
        lastName.clear();
        dni.clear();
        address.clear();
        phone.clear();
        email.clear();
        postalCode.clear();
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
     *  Clase abstracta que extiene de {@link EditUserProfileDialog}, evento ocurrido en dicha clase.
     *      Almacena el contenido asociado al evento.
     */
    public static  abstract  class ProfileUserFormEvent extends ComponentEvent<EditUserProfileDialog> {
        private UserEntity userEntity; //Usuario con el que se trabaja

        protected ProfileUserFormEvent(EditUserProfileDialog source, UserEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }

        public UserEntity getSanitary(){
            return userEntity;
        }
    }

    /**
     * Clase heredada de UserFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends ProfileUserFormEvent {
        SaveEvent(EditUserProfileDialog source, UserEntity userEntity){
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
