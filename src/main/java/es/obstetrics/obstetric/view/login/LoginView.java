package es.obstetrics.obstetric.view.login;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.LegalWarningPdf;
import es.obstetrics.obstetric.resources.templates.ImgTemplate;
import es.obstetrics.obstetric.security.SecurityConfig;
import es.obstetrics.obstetric.view.allUsers.GuideForPregnantWomenView;
import es.obstetrics.obstetric.view.allUsers.HomeDiv;
import es.obstetrics.obstetric.view.allUsers.WhoWeAreView;
import es.obstetrics.obstetric.view.allUsers.templates.TemplateViewSuggestionsStartView;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase encarga de gestionar el login y el registro de los
 * usuarios. Extiende de {@link HomeDiv}, pudiendo navegar
 * a la clase  {@link GuideForPregnantWomenView} o  {@link WhoWeAreView}
 * <p>
 * Puede acceder cualquier tipo de usuario, tanto registrados como no registrados.
 *
 */
@Route(value = "login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends HomeDiv implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final ConstantValues constantValues;
    private final PatientsLogService patientsLogService;
    private final  UserService userService;

    /**
     * Constructor de la clase, mediante un hl crea:
     * - Las diferentes fotografías que se añadiran a la derecha mediante la clase {@link TemplateViewSuggestionsStartView}
     * - A la izquierda se añadirá un TabSheet dónde se mostrará la interfaz de login o de registro.
     */
    @Autowired
    public LoginView(SecurityConfig securityConfig,
                     PasswordEncoder passwordEncoder,
                     PatientsLogService patientsLogService,
                     UserService userService,
                     PatientService patientService,
                     ConstantValues constantValues) {

        this.userService = userService;
        this.patientsLogService = patientsLogService;
        this.constantValues = constantValues;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;


        createLoginForm();
        TemplateViewSuggestionsStartView template = new TemplateViewSuggestionsStartView("url('themes/my-theme/photos/mother.png')");
        HorizontalLayout mainLayout = new HorizontalLayout(template, createStartView());
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        add(mainLayout);
        setSizeFull();
    }

    private void createLoginForm() {
        LoginI18n i18nLogin = LoginI18n.createDefault(); //Internacionalizacion para personalizar los campos del login
        LoginI18n.Form i18nLoginForm = i18nLogin.getForm();
        i18nLoginForm.setTitle("");
        i18nLoginForm.setUsername("Nombre de usuario ");
        i18nLoginForm.setPassword("Contraseña");
        i18nLoginForm.setSubmit("Iniciar sesión");
        i18nLoginForm.setForgotPassword("Olvidé mi contraseña");
        i18nLogin.setForm(i18nLoginForm);

        loginForm.setI18n(i18nLogin);
        loginForm.setAction("login");
        loginForm.addForgotPasswordListener(forgotPasswordEvent ->
            UI.getCurrent().navigate(ForgotPasswordView.class)
        );
    }

    public VerticalLayout createStartView() {
        Tab loginTab = new Tab("Iniciar sesión");
        Tab registerTab = new Tab("Registrarse");
        loginTab.addClassName("white-tab");
        registerTab.addClassName("white-tab");

        TabSheet startTabSheet = new TabSheet();
        startTabSheet.add(loginTab, loginForm);
        startTabSheet.add(registerTab, createRegister());

        startTabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED); //Centro los tabs
        ImgTemplate imgHl = new ImgTemplate(ConstantUtilities.ROUTE_LOGO_MOTHERBLOOM,
                "Logo mother bloom",
                "150px");
        Button legalWarning = createLegalWarning();
        VerticalLayout startVl = new VerticalLayout(legalWarning,imgHl, startTabSheet);
        startVl.setSizeFull();
        startVl.setHorizontalComponentAlignment(FlexComponent.Alignment.END, legalWarning);
        startVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, imgHl);
        startVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, startTabSheet);
        startVl.setAlignItems(FlexComponent.Alignment.CENTER);
        return startVl;
    }

    private Button createLegalWarning() {
        Button legalWarning = new Button(VaadinIcon.CLIPBOARD_TEXT.create());
        legalWarning.setTooltipText("Aviso legal");
        legalWarning.setClassName("dark-green-color-button");
        legalWarning.addClickListener(event -> {
            StreamResource resource = new StreamResource("aviso_legal.pdf", () -> {
                return new LegalWarningPdf(constantValues).generatePdf();
            });
            PdfViewer pdfViewer = new PdfViewer();
            pdfViewer.setSrc(resource);

            MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
            dialog.setHeaderTitle("Aviso legal");
            dialog.open();
        });
        return legalWarning;
    }

    private VerticalLayout createRegister() {
        RegisterView registerView = new RegisterView(passwordEncoder, patientService, patientsLogService,userService,constantValues);
        VerticalLayout registerVl = new VerticalLayout(registerView);
        registerVl.setSizeFull();
        registerVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        return registerVl;
    }

    /**
     * Método que se ejecuta antes de que se produzca la navegación a la vista.
     * Verifica si la URL de la solicitud contiene un parámetro de consulta "error"
     * Si es así, establece un indicador de error en el formulario.
     *
     */
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

}