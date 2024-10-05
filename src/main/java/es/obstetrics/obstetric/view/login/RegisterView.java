package es.obstetrics.obstetric.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.view.allUsers.GuideForPregnantWomenView;
import es.obstetrics.obstetric.view.allUsers.dialog.CodeConfirmDialog;
import es.obstetrics.obstetric.view.allUsers.dialog.RegisterCorrectConfirmDialog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class RegisterView extends HorizontalLayout{

    private final TextField dni;
    private final TextField accessCode;
    private final TextField username;
    private final RadioButtonGroup<String> chanel;
    private final RadioButtonGroup<String> subscription;
    private final PasswordField password;
    private final PasswordField password_repeat;
    private final FormLayout registerFl = new FormLayout();
    private H5 errorMessage;
    private final Binder<PatientEntity> userEntityBinder;
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final PatientsLogService patientsLogService;
    private final UserService userService;

    public RegisterView(PasswordEncoder passwordEncoder,
                        PatientService patientService,
                        PatientsLogService patientsLogService,
                        UserService userService,
                        ConstantValues constantValues){

        this.userService = userService;
        this.patientsLogService=patientsLogService;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;

        username = new TextField("Nombre de usuario");
        dni = new TextField("DNI");
        accessCode = new TextField("Código de acceso");
        password = new PasswordField("Contraseña");
        password_repeat = new PasswordField("Repita la contraseña");
        chanel = new RadioButtonGroup<>("Envío de contenido");
        chanel.setTooltipText("Escriba dónde te gustaría que te llegue el contenido");
        chanel.setItems(constantValues.getChanel());

        subscription = new RadioButtonGroup<>("Desea suscribirse a nuestra newsletter");
        subscription.setTooltipText("Escriba dónde te gustaría que te llegue el contenido");
        subscription.setItems("Si", "No");
        userEntityBinder = new BeanValidationBinder<>(PatientEntity.class);
        userEntityBinder.bindInstanceFields(this);
        add(createRegisterTemplate());
    }


    /**
     * Vista que se crea cuando el usuario pulsa en el tab de "registrarse".
     *
     * @return Devuelve el HorizontalLayout en el que contiene  los texfields y botones correspondientes
     *      con sus respectivos estilos para rellenar el procedimiento de registro..
     */
    private HorizontalLayout createRegisterTemplate() {
        registerFl.removeAll();

        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        registerFl.setWidth("28em");
        registerFl.getStyle().set("margin-top", "20px");
        userEntityBinder.readBean(null);

        Button registerButton = new Button( "Registrarse");
        registerButton.getStyle().set("margin-top", "20px");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> secondRegisterTemplate());

        registerFl.add(dni, accessCode, errorMessage, registerButton);

        return new HorizontalLayout(registerFl);
    }

    /**
     * Evento de cierre notificado por el cuadro de diálogo en el que
     *      se cierra el mismo.
     * @param closeEvent Evento que notifica el cierre.
     */
    private void closeConfirmDialog(CodeConfirmDialog.SaveEvent closeEvent) {
        errorMessage = new H5("");
        errorMessage.addClassName("label-error");

        registerFl.removeAll();

       Button registerButton = new Button("Registrarse");
        registerButton.getStyle().set("margin-top", "30px");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> validateDates(closeEvent.getUser()));

        chanel.setEnabled(false);

        subscription.addValueChangeListener(event -> {
            if(event.getValue().equals("Si")){
                chanel.setEnabled(true); //Habilita la suscripción si el usuario lo decide
            }else{
                chanel.clear();
                chanel.setEnabled(false); //Habilita la suscripción si el usuario lo decide
            }
        });
        registerFl.add(username, password, password_repeat,errorMessage, subscription,chanel, errorMessage, registerButton);
    }

    /**
     * Método que se lanza antes de que un usuario se registre para validar que los datos son correctos:
     *  - Se pone el estado a activo
     *  - Se elimina el codigo de acceso y la fecha del codigo.
     *  - Se añade el usuario y la contraseña codificada.
     *  - Se establece y escribe la entidad en el binder.
     *  - Se guarda la entidad en el repositorio.
     *  EXCEPCIÓN: antes de guardar la entidad en el repositorio se comprueba:
     *      - Que le nombre de usuario no este en uso
     *      - Que las contraseñas se hayan escrito correctamente.
     *      - Que la contraseña sea suficientemente segura.
     *   Si da alguna de estas excepciones, se lo indica al usuario para que vuelva a rellenar los campos.
     *   Si no, se abre un cuadro de diálogo indicandole que está listo para inicicar sesión.
     */
    private void validateDates(PatientEntity user) {
        try {
            userEntityBinder.writeBean(user);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        user.setState(ConstantUtilities.STATE_ACTIVE);
        user.setAccessCode("");
        user.setAccessCodeDate(null);
        user.setUsername(username.getValue());
        user.setPasswordHash(passwordEncoder.encode(password.getValue()));

        if(chanel == null){
            user.setChanel(ConstantUtilities.MESSAGE_CHANEL_NOTHING);
        }else {
            user.setChanel(chanel.getValue());
        }

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password.getValue());

        if (!matcher.matches()){
            setErrorMessage("La contraseña debe contenter al menos 8 caracteres, de los cuales debe contener una mayúsucla, una minúscula y un dígito.");
            return;
        }
        else if(userService.findOneByUsername(user.getUsername()) != null) {
            setErrorMessage("El nombre de usuario ya está en uso, debe poner otro.");
            return;
        } else if (!password.getValue().equals(password_repeat.getValue())) {
            setErrorMessage("Las contraseñas no coinciden, revíselas..");
            return;
        }else if(subscription.getValue() == null){
            setErrorMessage("Debe seleccionar la opción que desea de la suscripción");
            return;
        }

        setErrorMessage("");

        patientService.save(user);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("El paciente ha completado su registro");
        patientsLogEntity.setPatientEntity(user);
        // Obtener la IP del servidor
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        goToLogin();
    }
    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getLocalAddr();
    }


    private void goToLogin() {
        clearValues();
        RegisterCorrectConfirmDialog registerCorrectConfirmDialog = new RegisterCorrectConfirmDialog();
        registerCorrectConfirmDialog.addListener(RegisterCorrectConfirmDialog.CloseEvent.class, this::forgotPasswordClose);
        registerCorrectConfirmDialog.open();
    }

    private void forgotPasswordClose(RegisterCorrectConfirmDialog.CloseEvent closeEvent) {
        getUI().ifPresent(ui -> ui.navigate(GuideForPregnantWomenView.class));
    }

    /**
     * Método llamado cuando el usuario se está registrando y ha introducido  su código de acceso
     *      y su DNI, se comprueba que los valores son correctos.
     *      - Si es así, se abre el cuadro de diálogo donde se lo indica.
     *      - Si no, se le muestra un mensaje de error.
     */
    private void secondRegisterTemplate() {
        PatientEntity patientEntity = patientService.findByAccessCodeAndDNI(accessCode.getValue(), dni.getValue());
        if (patientEntity!= null) {
            CodeConfirmDialog codeConfirmDialog = new CodeConfirmDialog(patientEntity);
            codeConfirmDialog.addListener(CodeConfirmDialog.SaveEvent.class, this::closeConfirmDialog);
            codeConfirmDialog.open();
        } else {
            setErrorMessage("Los campos introducidos son incorrectos, revíselos.");
        }
    }

    /**
     * Establece el mensaje de error y lo muestra en la pantalla debajo de los campos DNI y codigo.
     */
    private void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Limpia los valores de todos los texfields.
     */
    public void clearValues() {
        password.clear();
        password_repeat.clear();
        username.clear();
        dni.clear();
        accessCode.clear();
        userEntityBinder.setBean(null);

        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        registerFl.setWidth("38em");
        registerFl.getStyle().set("margin-top", "30px");
        userEntityBinder.readBean(null);
    }
}
