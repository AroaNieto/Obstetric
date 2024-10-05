package es.obstetrics.obstetric.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.backend.utilities.EmailUtility;
import es.obstetrics.obstetric.resources.templates.ImgTemplate;
import es.obstetrics.obstetric.view.allUsers.HomeDiv;
import es.obstetrics.obstetric.view.allUsers.dialog.ForgotPasswordConfirmDialog;
import es.obstetrics.obstetric.view.allUsers.templates.TemplateViewSuggestionsStartView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;

@Route(value = "ForgotMyPassword")
@PageTitle("ForgotPassword")
@AnonymousAllowed
public class ForgotPasswordView extends HomeDiv {
    private final TextField phone;
    private final EmailField email;
    private final PatientService patientService;
    private final EmailUtility emailUtility;
    private final FormLayout registerFl;
    private Binder<PatientEntity> userEntityBinder;
    private H5 errorMessage;
    private final ConstantValues constantValues;
    private final PatientsLogService patientsLogService;

    @Autowired
    public ForgotPasswordView(PatientService patientService,
                              EmailUtility emailUtility, PatientsLogService patientsLogService,
                              ConstantValues constantValues){
        this.patientsLogService = patientsLogService;
        this.patientService = patientService;
        this.emailUtility = emailUtility;
        this.constantValues = constantValues;

        registerFl = new FormLayout();
        phone = new TextField("Teléfono");
        phone.setPrefixComponent(new Icon(VaadinIcon.PHONE));
        email = new EmailField("Email");
        email.setPrefixComponent(new Icon(VaadinIcon.MAILBOX));

        createBinder();
        TemplateViewSuggestionsStartView template = new TemplateViewSuggestionsStartView("url('themes/my-theme/photos/mother.png')");
        template.setSizeFull();
        HorizontalLayout templates = new HorizontalLayout(template, createForgotPasswordView());
        templates.setSizeFull();

        add(templates);
        setSizeFull();
    }
    /**
     * Vista donde se carga el tab de olvidé mi contraseña. Contiene los texfields correspondientes
     *      para que el usuario rellene los datos.
     *
     * @return El verticalLayout con todos los campos.
     */
    private VerticalLayout createRecoverPasswordTemplate() {
        registerFl.removeAll();

        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        registerFl.setWidth("28em");
        registerFl.getStyle().set("margin-top", "20px");

        Button btn = new Button("Reestablecer contraseña");
        btn.getStyle().set("margin-top", "30px");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> validateDatesRecoverPassword());

        RouterLink linkLogin = new RouterLink("Volver al inicio de sesión", LoginView.class);
        linkLogin.getStyle().set("color", "var(--dark-green-color)")
                .set("margin-top", "10px")
                .set("text-align", "center")
                .set("font-weight", "600");

        registerFl.add(new H4("Introduzca el email y el número de teléfono con el que tenga vinculada la cuenta."),
                phone, email,errorMessage, btn, linkLogin);
        return new VerticalLayout(registerFl);
    }

    private void validateDatesRecoverPassword() {

        PatientEntity userMail = patientService.findOneByEmail(email.getValue());
        PatientEntity userPhone = patientService.findOneByPhone(phone.getValue());
        if(userMail == null || userPhone == null || !userMail.getId().equals(userPhone.getId())){
            setErrorMessage("Los datos son incorrectos, reviselos.");
            return;
        }
        setErrorMessage("");
        String codeURL = generateCode();
        String codeForgotPass = generateCode();
        userPhone.setAccessCodeForgotPassword(codeForgotPass);
        userPhone.setAccessCodeUrlForgotPassword(codeURL);
        userPhone.setAccessCodeForgotPasswordDate(LocalDate.now());

        patientService.save(userPhone);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("El paciente ha pedido recuperar su contraseña y se le ha enviado un correo.");
        patientsLogEntity.setPatientEntity(userPhone);
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);

        String url = "http://" + constantValues.getUrl()+"/forgotPass/" +codeURL;

        emailUtility.sendEmail(email.getValue(),
                "CAMBIO DE CONTRASEÑA MOTHER BLOOM",
                " ¡Hola!, " + userMail.getName() +
                constantValues.getMessageForgotPassword()+ url +  " CÓDIGO: " + codeForgotPass);
        ForgotPasswordConfirmDialog forgotPasswordConfirmDialog = new ForgotPasswordConfirmDialog(userPhone);
        forgotPasswordConfirmDialog.addListener(ForgotPasswordConfirmDialog.SaveEvent.class, this::closeForgotPasswordConfirmDialog);
        forgotPasswordConfirmDialog.open();
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getLocalAddr();
    }

    private String generateCode(){
        byte[] bufferByte = new byte[6]; //Creación de un array de bytes de longitud 9
        SecureRandom secureRandom = new SecureRandom();

        secureRandom.nextBytes(bufferByte);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bufferByte);
    }

    /**
     * Vista que se crea cuando el usuario pulsa en el botón del login "olvidé mi contraseña".
     *
     * @return Devuelve el verticalayout en el que contiene el tab para indicar que se encuentra
     *  en la vista de campo de contraseña, los texfields correspondientes para rellenar el
     *  procedimiento de nuevo cambio de contraseña y el logo de la aplicación de la misma manera
     *  que en el registro y login.
     */
    private VerticalLayout createForgotPasswordView(){

        Tab recoverPasswordTab = new Tab("Recuperar contraseña");
        recoverPasswordTab.addClassName("white-tab");

        TabSheet recoverPasswordTabSheet = new TabSheet();
        recoverPasswordTabSheet.add(recoverPasswordTab, createRecoverPasswordTemplate());

        recoverPasswordTabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
        HorizontalLayout comeBackToLoginAndTab = new HorizontalLayout(recoverPasswordTabSheet);
        comeBackToLoginAndTab.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        HorizontalLayout imgHl = new ImgTemplate(ConstantUtilities.ROUTE_LOGO_MOTHERBLOOM,
                "Logo mother bloom",
                "150px");
        VerticalLayout recoverPasswordVl = new VerticalLayout(imgHl, comeBackToLoginAndTab);
        recoverPasswordVl.setSizeFull();
        recoverPasswordVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, imgHl);
        recoverPasswordVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, comeBackToLoginAndTab);

        return recoverPasswordVl;
    }

    private void createBinder() {
        userEntityBinder = new BeanValidationBinder<>(PatientEntity.class);
        userEntityBinder.bindInstanceFields(this);
    }

    private void closeForgotPasswordConfirmDialog(ForgotPasswordConfirmDialog.SaveEvent saveEvent) {
        clearValues();
        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }

    /**
     * Limpia los valores de todos los texfields.
     */
    private void clearValues() {
        email.clear();
        phone.clear();
        userEntityBinder.setBean(null);
    }

    /**
     * Establece el mensaje de error y lo muestra en la pantalla debajo de los campos DNI y codigo.
     */
    private void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

}
