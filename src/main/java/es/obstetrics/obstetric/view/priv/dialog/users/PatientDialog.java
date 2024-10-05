package es.obstetrics.obstetric.view.priv.dialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.UserCodeConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.views.users.PatientsGridView;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;

public class PatientDialog extends MasterDialog {

    private final Binder<PatientEntity> userEntityBinder;
    private final TextField name;
    private final TextField lastName;
    private final TextField dni;
    private final TextField address;
    private final TextField phone;
    private final EmailField email;
    private final TextField postalCode;
    private final TextField age;
    private  DatePicker accessCodeDate;
    private  TextField accessCode;
    private final ComboBox<String> sex;
    private final ComboBox<String> state;
    private final ComboBox<String> role;
    private final H5 errorMessage;
    private final PatientService patientService;
    private final ConstantValues constantValues;
    /**
     * Constructor de la clase, se encarga de inicializar los componentes que aparecerán en el
     *      cuadro de dialogo y se estableceran en el Binder, configurar el enlace de datos
     *      mediante el BeanValidationBinder y establecer los valores.
     */
    public PatientDialog(PatientEntity user, PatientService patientService, ConstantValues constantValues){
        this.constantValues = constantValues;
        this.patientService  = patientService;
        name = new TextField("Nombre");
        lastName= new TextField("Apellidos");
        dni= new TextField("DNI");
        address= new TextField("Dirección");
        phone= new TextField("Teléfono");
        age= new TextField("Edad");
        email= new EmailField("Email");
        postalCode= new TextField("Código postal");
        state = new ComboBox<>("Estado");
        sex = new ComboBox<>("Sexo");
        role = new ComboBox<>("Rol");
        errorMessage = new H5("");

        createHeaderDialog();
        createDialogLayout();

        userEntityBinder = new BeanValidationBinder<>(PatientEntity.class);
        userEntityBinder.bindInstanceFields(this);
        setUser(user);
    }

    private void closeConfirmDialog(UserCodeConfirmDialog.SaveCode saveCode) {
        PatientEntity userEntity = new PatientEntity();
        accessCode.setValue(String.valueOf(saveCode.getCode()));
        setValues();
            try {
                userEntityBinder.writeBean(userEntity);
                userEntity.setAccessCodeDate(LocalDate.now().plusDays(10));
            } catch (ValidationException e) {
                setErrorMessage(e.getMessage());
            }
        fireEvent(new SaveEvent(this, userEntity));
        close();
        setErrorMessage("");

    }

    /**
     * Método utilizado para establecer los valores de los campos de diálogo dependiendo de si
     *  se está editando una usuario (user != null) o añadiendo (user == null).
     *
     * @param user usuario
     */
    private void setUser(PatientEntity user){
        sex.setItems(ConstantUtilities.SEX_MALE, ConstantUtilities.SEX_FEMALE);
        state.setItems(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE);
        role.setItems(ConstantUtilities.ROLE_PATIENT);

        userEntityBinder.setBean(user);  //Recojo el usuario
        userEntityBinder.readBean(user);
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.getFooter().add(button);
    }

    /**
     * Dispara el evento para notificar a la clase {@link PatientsGridView }
         que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        clearTextField();
        setErrorMessage("");
        setUser(null);
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo una nuevo usuario o editando.
     *      - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     *      - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {
        if(userEntityBinder.getBean() == null){ //Si se está añadiendo un nuevo paciente
            PatientEntity userEntity = new PatientEntity();
            try {
                userEntityBinder.writeBean(userEntity);

            } catch (ValidationException e) {
                setErrorMessage(e.getMessage());
            }
            if (patientService.findOneByDni(userEntity.getDni()) != null) {
                setErrorMessage("El DNI ya existe.");
                return;
            } else if (patientService.findOneByEmail(userEntity.getEmail()) != null) {
                setErrorMessage("El email ya existe.");
                return;
            } else if (patientService.findOneByPhone(userEntity.getPhone()) != null) {
                setErrorMessage("El  teléfono móvil  ya existe.");
                return;
            }
            UserCodeConfirmDialog userCodeConfirmDialog = new UserCodeConfirmDialog(generateCode(), name.getValue(), lastName.getValue(), null,constantValues);
            userCodeConfirmDialog.addListener(UserCodeConfirmDialog.SaveCode.class, this::closeConfirmDialog);
            userCodeConfirmDialog.open();
        }else{
            try { //Comprobación de que no este añadiendo valores repetidos
                userEntityBinder.writeBean(userEntityBinder.getBean());
                if (patientService.findByDni(userEntityBinder.getBean().getDni()) != null
                        && !userEntityBinder.getBean().getId().equals(patientService.findByDni(userEntityBinder.getBean().getDni()).getId())) {
                    setErrorMessage("El DNI ya está en uso.");
                    return;
                }
                // Verificar si se ha modificado el correo electrónico
                if (patientService.findByEmail(userEntityBinder.getBean().getEmail()) != null &&
                        !userEntityBinder.getBean().getId().equals(patientService.findByEmail(userEntityBinder.getBean().getEmail()).getId())) {
                    setErrorMessage("El correo electrónico ya está en uso.");
                    return;
                }

                // Verificar si se ha modificado el telefono
                if (patientService.findByPhone(userEntityBinder.getBean().getPhone()) != null  &&
                        !userEntityBinder.getBean().getId().equals(patientService.findByDni(userEntityBinder.getBean().getDni()).getId())) {
                    setErrorMessage("El teléfono móvil ya esta en uso.");
                    return;
                }
                fireEvent(new SaveEvent(this, userEntityBinder.getBean()));
                close();
                setErrorMessage("");
            } catch (ValidationException e) {
                setErrorMessage(e.getMessage());
            }

        }
    }

    private String generateCode(){
        byte[] bufferByte = new byte[6]; //Creación de un array de bytes de longitud 9
        SecureRandom secureRandom = new SecureRandom();

        secureRandom.nextBytes(bufferByte);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bufferByte);
    }


    /**
     * Método que se encarga de configurar el diseño del diálogo del usuario
     *  con sus campos correspondientes.
     */ @Override
    public void createDialogLayout() {

        HorizontalLayout nameAndLastnameHl = new HorizontalLayout(name, lastName);
        nameAndLastnameHl.setSizeFull();
        nameAndLastnameHl.expand(name,lastName);

        HorizontalLayout sexAndAgeHl = new HorizontalLayout(age, dni);
        sexAndAgeHl.setSizeFull();
        sexAndAgeHl.expand(age, dni);

        HorizontalLayout phoneAndDniHl = new HorizontalLayout(phone,email );
        phoneAndDniHl.setSizeFull();
        phoneAndDniHl.expand(phone,email);

        HorizontalLayout postalCodeAndEmailHl = new HorizontalLayout(address,postalCode);
        postalCodeAndEmailHl.setSizeFull();
        postalCodeAndEmailHl.expand(address,postalCode);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(nameAndLastnameHl,sexAndAgeHl,phoneAndDniHl, postalCodeAndEmailHl, errorMessage);
    }

    /**
     * Establece los valores de estado antes
     *  de guardar un nuevo usuario.
     */
    public void setValues() {
        state.setValue(ConstantUtilities.STATE_INACTIVE);
        sex.setValue(ConstantUtilities.SEX_FEMALE);
        role.setValue(ConstantUtilities.ROLE_PATIENT);
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
        sex.clear();
        lastName.clear();
        state.clear();
        dni.clear();
        address.clear();
        phone.clear();
        email.clear();
        postalCode.clear();
        accessCode.clear();
        accessCodeDate.clear();
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
     *  Clase abstracta que extiene de {@link PatientDialog}, evento ocurrido en dicha clase.
     *      Almacena el contenido asociado al evento.
     */
    @Getter
    public static  abstract  class UserFormEvent extends ComponentEvent<PatientDialog> {

        private final PatientEntity userEntity; //Usuario con el que se trabaja

        protected  UserFormEvent(PatientDialog source, PatientEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }
    }

    /**
     * Clase heredada de UserFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends UserFormEvent {
        SaveEvent(PatientDialog source, PatientEntity userEntity){
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
