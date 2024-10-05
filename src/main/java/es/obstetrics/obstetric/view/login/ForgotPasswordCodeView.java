package es.obstetrics.obstetric.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.resources.templates.ImgTemplate;
import es.obstetrics.obstetric.view.allUsers.dialog.ForgotPasswordCodeConfirmDialog;
import es.obstetrics.obstetric.view.allUsers.dialog.NewPasswordConfirmDialog;
import es.obstetrics.obstetric.view.allUsers.templates.TemplateViewSuggestionsStartView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Route(value = "forgotPass")
@PageTitle("MotherBloom-ForgotPassword")
@AnonymousAllowed
public class ForgotPasswordCodeView extends Div implements HasUrlParameter<String> {

    private final TextField accessCodeForgotPasswordUser;
    private String code;
    private final PasswordField passwordHash;
    private final PasswordField password_repeat;
    private final PasswordEncoder passwordEncoder;
    private H5 errorMessage;
    private final PatientService patientService;
    private final FormLayout forgotPassFl;
    private final Binder<UserEntity> userEntityBinder;
    private final PatientsLogService patientsLogService;

    @Autowired
    public ForgotPasswordCodeView(PatientService patientService, PatientsLogService patientsLogService,
                                 PasswordEncoder passwordEncoder) {

        this.patientsLogService = patientsLogService;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;
        forgotPassFl = new FormLayout();
        errorMessage = new H5("");
        passwordHash = new PasswordField("Contraseña");
        password_repeat = new PasswordField("Repita la contraseña");
        accessCodeForgotPasswordUser = new TextField("Código de acceso");
        accessCodeForgotPasswordUser.setPlaceholder("Introduzca el código de acceso");
        userEntityBinder = new BeanValidationBinder<>(UserEntity.class);
        userEntityBinder.bindInstanceFields(this);


        TemplateViewSuggestionsStartView template = new TemplateViewSuggestionsStartView("url('themes/my-theme/photos/mother.png')");
        template.setSizeFull();
        HorizontalLayout templates = new HorizontalLayout(template, createForgotPasswordCodeView());
        templates.setSizeFull();

        add(templates);
        setSizeFull();
    }

    private VerticalLayout createForgotPasswordCodeView() {
        Tab recoverPasswordTab = new Tab("Recuperar contraseña");
        recoverPasswordTab.addClassName("white-tab");

        TabSheet recoverPasswordTabSheet = new TabSheet();
        recoverPasswordTabSheet.add(recoverPasswordTab, createForgotPasswordCodeTemplate());

        recoverPasswordTabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
        HorizontalLayout comeBackToLoginAndTab = new HorizontalLayout(recoverPasswordTabSheet);
        comeBackToLoginAndTab.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        HorizontalLayout imgHl = new ImgTemplate(ConstantUtilities.ROUTE_LOGO_MOTHERBLOOM, "Logo mother bloom","150px");

        VerticalLayout recoverPasswordVl = new VerticalLayout(imgHl, comeBackToLoginAndTab);
        recoverPasswordVl.setSizeFull();
        recoverPasswordVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, imgHl);
        recoverPasswordVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, comeBackToLoginAndTab);

        return recoverPasswordVl;
    }

    /**
     * Validación del código, COMPROBACIONES
     *  - Exista el código en la base de datos de algún usuario.
     *  - Que el código del usuario almacenado sea el mismo que el código introducido
     *  - Que el código de la url sea el mismo que el código de la url almacenado en el usuario.
     *  - Que el códgio NO esté caducado --> validez de 10 horas.
     */
    private void validateAccessCodeForgotPassword() {
        PatientEntity userAccessCode = patientService.findByAccessCodeForgotPassword(accessCodeForgotPasswordUser.getValue());
        if (accessCodeForgotPasswordUser.isEmpty()) {
            setErrorMessage("Debe introducir el código para continuar con la validación");
            return;
        } else if (userAccessCode == null ||
                !userAccessCode.getAccessCodeForgotPassword().equals(accessCodeForgotPasswordUser.getValue())||
                !userAccessCode.getAccessCodeUrlForgotPassword().equals(code)) {
            setErrorMessage("El codigo introducido es incorrecto");
            return;
        }else if(!isCodeValid(userAccessCode.getAccessCodeForgotPasswordDate())){
            setErrorMessage("Codigo caducado, debe volver a generarlo.");
            return;
        }
        ForgotPasswordCodeConfirmDialog forgotPasswordCodeConfirmDialog = new ForgotPasswordCodeConfirmDialog(userAccessCode);
        forgotPasswordCodeConfirmDialog.addListener(ForgotPasswordCodeConfirmDialog.SaveEvent.class, this::closeForgotPasswordCodeConfirmDialog);
        forgotPasswordCodeConfirmDialog.open();
    }

    /**
     * Comprobación de que el codigo de acceso asignado al usuairo no ha caducado
     * @param accessCodeDate Fecha y hora en la que se le dió el código de acceso, con validez de 10 horas.
     * @return Devuelve verdadero si el código es válido y falso si no.
     */
    private boolean isCodeValid(LocalDate accessCodeDate) {
        LocalDate now = LocalDate.now(); //Se verifica si la diferencia entre la fecha actual y la fecha del código de acceso es menor a 1 día
        return ChronoUnit.DAYS.between(accessCodeDate, now) < 1; //Devuelve verdadero si el código es válido y falso si no.
    }

    private VerticalLayout createForgotPasswordCodeTemplate() {
        forgotPassFl.removeAll();

        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        forgotPassFl.setWidth("28em");
        forgotPassFl.getStyle().set("margin-top", "20px");

        Button btn = new Button("Continuar");
        btn.getStyle().set("margin-top", "30px");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> validateAccessCodeForgotPassword());

        H2 titleH2 = new H2("Introduzca el código de acceso proporcionado");
        titleH2.getStyle().set("margin-bottom", "25px");



        forgotPassFl.add(titleH2, accessCodeForgotPasswordUser, errorMessage, btn);
        return new VerticalLayout(forgotPassFl);
    }

    private void closeForgotPasswordCodeConfirmDialog(ForgotPasswordCodeConfirmDialog.SaveEvent closeEvent) {
        setErrorMessage("");
        forgotPassFl.removeAll();

        Button btn = new Button("Continuar");
        btn.getStyle().set("margin-top", "30px");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> validatePasswordCodeForgotPassword(closeEvent.getUserEntity()));

        H2 titleH2 = new H2("Introduzca su nueva contraseña.");
        titleH2.getStyle().set("margin-bottom", "20px");

        H4 summaryH4  = new H4("Recuerde que es importante que escoja una contraseña segura.");

        forgotPassFl.add(titleH2,summaryH4, passwordHash, password_repeat, errorMessage, btn);
    }

    private void closeNewPasswordConfirmDialog(NewPasswordConfirmDialog.SaveEvent closeEvent) {
        clearValues();
        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }

    private void validatePasswordCodeForgotPassword(PatientEntity user) {
        if(!passwordHash.getValue().equals(password_repeat.getValue())){
            setErrorMessage("Las contraseñas no coinciden");
            return;
        }

        try {
            userEntityBinder.writeBean(user);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        if(user.getPasswordHash() != null && user.getPasswordHash().equals(passwordEncoder.encode(passwordHash.getValue()))){ //Se comprueba que el usuario este registrado y además que las contraseñas no coincidan
            setErrorMessage("Debe poner una contraseña distinta a la que ya tenía");
            return;
        }
        user.setPasswordHash(passwordEncoder.encode(passwordHash.getValue()));
        user.setAccessCodeForgotPassword("");

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(passwordHash.getValue());

        if (!matcher.matches()){
            setErrorMessage("La contraseña debe contenter al menos una mayúsucla, una minúscula y un dígito.");
            return;
        }

        setErrorMessage("");
        patientService.save(user);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("El paciente ha cambiado su contraseña desde fuera de la aplicación");
        patientsLogEntity.setPatientEntity(user);
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        NewPasswordConfirmDialog newPasswordConfirmDialog = new NewPasswordConfirmDialog();
        newPasswordConfirmDialog.addListener(NewPasswordConfirmDialog.SaveEvent.class, this::closeNewPasswordConfirmDialog);
        newPasswordConfirmDialog.open();
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getLocalAddr();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        if (s == null) {
            forgotPassFl.add("error");
        } else {
            code = s;
        }
    }

    /**
     * Limpia los valores de todos los texfields.
     */
    private void clearValues() {
        passwordHash.clear();
        password_repeat.clear();
        userEntityBinder.setBean(null);
    }

    /**
     * Establece el mensaje de error y lo muestra en la pantalla debajo de los campos DNI y codigo.
     */
    private void setErrorMessage(String message) {
        errorMessage.setText(message);
    }
}
