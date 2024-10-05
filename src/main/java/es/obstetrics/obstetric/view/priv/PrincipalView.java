package es.obstetrics.obstetric.view.priv;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.backend.utilities.EmailUtility;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.listings.pdf.DownloadAllDatesPdf;
import es.obstetrics.obstetric.resources.templates.HeaderTemplate;
import es.obstetrics.obstetric.security.UserDetailsService;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteAllDatesConfirmDialog;
import es.obstetrics.obstetric.view.priv.home.HomeView;
import es.obstetrics.obstetric.view.priv.views.appointment.MyAppointmentsDayGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.access.AccessGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.access.UserAccessGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.appointment.AppointmentGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.appointment.AppointmentTypeGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.appointment.DiaryGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.NotificationGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.NewsletterGridView;
import es.obstetrics.obstetric.view.priv.views.maintenance.insurance.InsuranceGridView;
import es.obstetrics.obstetric.view.priv.views.messengerService.SelectedMessengerServiceView;
import es.obstetrics.obstetric.view.priv.views.messengerService.SelectedSanitaryMessengerServiceView;
import es.obstetrics.obstetric.view.priv.views.myFolder.MyFolder;
import es.obstetrics.obstetric.view.priv.views.users.PatientsGridView;
import es.obstetrics.obstetric.view.priv.views.users.ProfileUserView;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Clase principal que extiende de appLayout, define la vista principal de la
 * aplicación.
 *
 */
@Slf4j
public class PrincipalView extends AppLayout implements BeforeEnterObserver {
    private final AuthenticationContext authenticationContext;
    private final HorizontalLayout userHl;
    private final UserCurrent userCurrent;
    private final PatientService patientService;
    private final NotificationService notificationService;
    private final ConstantValues constantValues;
    private Avatar avatar;
    private final LoginLogOutLogService loginLogOutLogService;
    private final AppointmentService appointmentService;
    private final PregnanceService pregnanceService;
    private final PatientsLogService patientsLogService;
    private final DiaryService diaryService;
    private final CenterService centerService;
    private final UserService userService;
    private final ReportService reportService;
    private final EmailUtility emailUtility;

    @Autowired
    public PrincipalView(AuthenticationContext authenticationContext,
                         PatientService patientService,
                         UserCurrent userCurrent,
                         UserService userService,
                         EmailUtility emailUtility,
                         AppointmentService appointmentService,
                         PregnanceService pregnanceService,
                         PatientsLogService patientsLogService,
                         DiaryService diaryService,
                         CenterService centerService,
                         LoginLogOutLogService loginLogOutLogService,
                         NotificationService notificationService,
                         ReportService reportService,
                         ConstantValues constantValues) {

        this.emailUtility = emailUtility;
        this.reportService = reportService;
        this.appointmentService = appointmentService;
        this.pregnanceService = pregnanceService;
        this.patientsLogService = patientsLogService;
        this.diaryService = diaryService;
        this.centerService = centerService;
        this.loginLogOutLogService = loginLogOutLogService;
        this.authenticationContext = authenticationContext;
        this.patientService = patientService;
        this.notificationService = notificationService;
        this.userCurrent = userCurrent;
        this.constantValues = constantValues;
        this.userService = userService;

        userHl = new HorizontalLayout();

        createHeader();
        addClassName("principal-view");
    }

    private void registerLogin() {
        //Se registra el nuevo inicio de sesión
        LoginLogOutLogEntity loginLogOutLogEntity = new LoginLogOutLogEntity();
        loginLogOutLogEntity.setUserEntity(userCurrent.getCurrentUser());
        loginLogOutLogEntity.setTime(LocalTime.now());
        loginLogOutLogEntity.setDate(LocalDate.now());
        loginLogOutLogEntity.setMessage("LOGIN");
        loginLogOutLogEntity.setIp(getServerIp());
        loginLogOutLogService.save(loginLogOutLogEntity);
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getLocalAddr();
    }
    /**
     * Creación de la cabecera de la aplicación para añadirla al navbar,
     * Si el usuario está autenticado, aparece el botón de cerrar sesión
     */
    private void createHeader() {

        HorizontalLayout header = authenticationContext.getAuthenticatedUser(UserPrincipal.class)
                .map(userPrincipal -> {
                    userCurrent.setCurrentUser(userPrincipal.getUser());
                    if (userPrincipal.getUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_ADMIN)) {
                        createAdminDrawer(); //Se le pasa el nombre de usuario para adjuntarlo en le menu
                    } else if (userPrincipal.getUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)) {
                        createPatientDrawer(); //Se le pasa el nombre de usuario para adjuntarlo en le menu
                    } else if (userPrincipal.getUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                        createSanitaryDrawer(userPrincipal.getUser().getRole()); //Se le pasa el nombre de usuario para adjuntarlo en le menu
                    } else if (userPrincipal.getUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_MATRONE)) {
                        createSanitaryDrawer(userPrincipal.getUser().getRole()); //Se le pasa el nombre de usuario para adjuntarlo en le menu
                    } else if (userPrincipal.getUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_SECRETARY)) {
                        createSanitaryDrawer(userPrincipal.getUser().getRole()); //Se le pasa el nombre de usuario para adjuntarlo en le menu
                    }

                    return new HorizontalLayout(createHeaderHl(), createUserMenu(userPrincipal.getUser())); //Se añade lo que irá en la cabecera

                }).orElseGet(() -> new HorizontalLayout(new HeaderTemplate("header-template")));

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER); //Siempre se alinee verticalmente en el centro
        header.setWidthFull(); //El ancho al máximo
        addToNavbar(header); //Método del app layout
        registerLogin();
    }

    private HorizontalLayout createUserMenu(UserEntity user) {
        avatar = new Avatar(user.getName());
        avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        MenuBar menuBar = new MenuBar();
        menuBar.addClassName("custom-menu-bar");

        MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.CHEVRON_DOWN));
        SubMenu subMenu = menuItem.getSubMenu();

        if (user.getProfilePhoto() != null) {
            setPhotoProfile(user.getProfilePhoto());
        }

        subMenu.addItem("Mi perfil").addClickListener(event -> UI.getCurrent().navigate(ProfileUserView.class));
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            subMenu.addItem("Mi carpeta").addClickListener(event -> UI.getCurrent().navigate(MyFolder.class));
            subMenu.addItem("Mensajería").addClickListener(event -> UI.getCurrent().navigate(SelectedMessengerServiceView.class));

        }
        MenuItem dataProtectionItem = subMenu.addItem("Protección de datos");
        SubMenu dataProtectionSubMenu = dataProtectionItem.getSubMenu();

        dataProtectionSubMenu.addItem("Descargar mis datos").addClickListener(event -> {
            StreamResource resource = new StreamResource("mis_datos.pdf", () -> new DownloadAllDatesPdf(userCurrent.getCurrentUser(),
                    notificationService, loginLogOutLogService, appointmentService, pregnanceService,
                    patientsLogService, diaryService, centerService, reportService).generatePdf());
            /*
                La descarga se hará automáticamente.
             */
            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getElement().setAttribute("hidden", true);
            addToDrawer(downloadLink);
            downloadLink.getElement().callJsFunction("click");
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar 1 segundo antes de eliminar el Anchor
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                getUI().ifPresent(ui -> ui.access(() -> remove(downloadLink)));
            }).start();
        });

        dataProtectionSubMenu.addItem("Eliminar mis datos").addClickListener(event -> {
            DeleteAllDatesConfirmDialog deleteAllDatesConfirmDialog = new DeleteAllDatesConfirmDialog();
            deleteAllDatesConfirmDialog.open();
            deleteAllDatesConfirmDialog.addListener(DeleteAllDatesConfirmDialog.Delete.class, this::deleteMyCount);

        });

        subMenu.addItem("Cerrar sesión").addClickListener(event ->{
            //Registrar el logout
            LoginLogOutLogEntity loginLogOutLogEntity = new LoginLogOutLogEntity();
            loginLogOutLogEntity.setUserEntity(user);
            loginLogOutLogEntity.setTime(LocalTime.now());
            loginLogOutLogEntity.setDate(LocalDate.now());
            loginLogOutLogEntity.setMessage("LOGOUT");
            loginLogOutLogEntity.setIp(getServerIp());
            loginLogOutLogService.save(loginLogOutLogEntity);
            UserDetailsService.logout();
        } );

        HorizontalLayout userMenu = new HorizontalLayout(avatar,
                createTitleUsername(user),
                menuBar);

        userMenu.setSizeFull();
        userMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        userMenu.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return userMenu;
    }


    private void deleteMyCount(DeleteAllDatesConfirmDialog.Delete delete) {
        userCurrent.getCurrentUser().setState(ConstantUtilities.STATE_DISCHARGED);
        userCurrent.getCurrentUser().setStateMessagingSystemPatient(ConstantUtilities.STATE_INACTIVE);
        userCurrent.getCurrentUser().setStateMessagingSystemSanitary(ConstantUtilities.STATE_INACTIVE);
        userService.save(userCurrent.getCurrentUser());
        //Registrar el logout
        LoginLogOutLogEntity loginLogOutLogEntity = new LoginLogOutLogEntity();
        loginLogOutLogEntity.setUserEntity(userCurrent.getCurrentUser());
        loginLogOutLogEntity.setTime(LocalTime.now());
        loginLogOutLogEntity.setDate(LocalDate.now());
        loginLogOutLogEntity.setMessage("LOGOUT");
        loginLogOutLogEntity.setIp(getServerIp());
        loginLogOutLogService.save(loginLogOutLogEntity);

        //Registrar el logout
        LoginLogOutLogEntity loginLogOutLog = new LoginLogOutLogEntity();
        loginLogOutLog.setUserEntity(userCurrent.getCurrentUser());
        loginLogOutLog.setTime(LocalTime.now());
        loginLogOutLog.setDate(LocalDate.now());
        loginLogOutLog.setMessage("Cierre de cuenta, petición de eliminar todos sus datos");
        loginLogOutLog.setIp(getServerIp());
        loginLogOutLogService.save(loginLogOutLog);
        emailUtility .sendEmail(constantValues.getEmailAdmin(), "Petición de eliminación de cuenta",
                "El paciente "+ userCurrent.getCurrentUser() + " ha solicitado la eliminación de su cuenta. Tome las medidas que estime oportuna para ello.");
        //Enviar el correo electrónico al adminstrador
        UserDetailsService.logout();

    }

    private H5 createTitleUsername(UserEntity user) {
        H5 titleH5 = new H5(user.getName() + " " + user.getLastName());
        titleH5.addClassName("color-title");
        return titleH5;
    }

    public void setPhotoProfile(byte[] photo) {
        StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(photo));
        avatar.setImageResource(resource);
    }

    /**
     * Crea la cabecera de la aplicación (el logo y su nombre) y lo añade a un Hl
     *
     * @return La cabecera dentro de un Hl.
     */
    private HorizontalLayout createHeaderHl() {
        return new HorizontalLayout(new HeaderTemplate("header-template"));
    }


    /**
     * Creación del menú lateral de la aplicación, para el paciente
     * define la barra de navegación con los diferentes enlaces.
     */
    private void createPatientDrawer() {
        //Comprobación de si tiene el embarazo activo
        PatientEntity patient = patientService.findOneByUsername(userCurrent.getCurrentUser().getUsername());
        PregnanceEntity pregnance = Utilities.isPregnantActive(patient.getPregnancies());
        if (pregnance != null) {
            checkNotification();
        }

     /*   SideNavItem homeNav = new SideNavItem("Mi carpeta", HomeView.class, VaadinIcon.HOME.create());
        SideNavItem userNav = new SideNavItem("Citas", MyAppointments.class, VaadinIcon.CALENDAR_CLOCK.create());
        SideNavItem shippingSystem = new SideNavItem("Mensajería", SelectedMessengerServiceView.class, VaadinIcon.CHAT.create());
        SideNavItem reportNav = new SideNavItem("Informes", MyReportsView.class, VaadinIcon.CLIPBOARD_USER.create());
        SideNavItem contentNav = new SideNavItem("Newsletter", NewsletterPatientView.class, VaadinIcon.NEWSPAPER.create());

        SideNav nav = new SideNav();
        nav.addItem(homeNav, userNav, reportNav, contentNav, shippingSystem);
        addToDrawer(userHl, nav);*/
    }

    private void checkNotification() {
        //Comprobación de si tienen newsletter nuevas
        List<NotificationEntity> messages = notificationService.findByChanelAndMessageStateAndUserEntity(ConstantUtilities.MESSAGE_CHANEL_APP, ConstantUtilities.MESSAGE_NOT_DELIVERED, userCurrent.getCurrentUser()); //Lista de mensajes
        int notificationIs = 0;
        for (NotificationEntity oneNotificacion : messages) {
            oneNotificacion.setState(ConstantUtilities.MESSAGE_DELIVERED);
            oneNotificacion.setShippingDate(LocalDate.now()); //Se añade la fecha de envío
            notificationService.save(oneNotificacion); //Se actualiza el mensaje en Entregado
            notificationIs = 1;
        }
        if (notificationIs != 0) {

            Notification.show(userCurrent.getCurrentUser().getName() + ", ¡Contenido nuevo!, reviselo en su apartado de newsletter",
                    5000, Notification.Position.TOP_CENTER);
        }
    }

    /**
     * Creación del menú lateral de la aplicación, para el administrador
     * define la barra de navegación con los diferentes enlaces.
     */
    private void createAdminDrawer() {

        SideNav nav = new SideNav();
        SideNavItem usersNav = new SideNavItem("Usuarios");
        SideNavItem patientsNav = new SideNavItem("Pacientes", PatientsGridView.class, VaadinIcon.USER_HEART.create());
        SideNavItem sanitariesNav = new SideNavItem("Trabajadores", HomeView.class, VaadinIcon.USERS.create());
        usersNav.addItem(patientsNav, sanitariesNav);

        SideNavItem adminMaintenanceNav = new SideNavItem("Mantenimiento");

        SideNavItem adminMaintenanceAppointmentNav = new SideNavItem("Citas");
        SideNavItem adminMaintenanceAppointmentTypeNav = new SideNavItem("Tipos de citas", AppointmentTypeGridView.class, VaadinIcon.CALENDAR_BRIEFCASE.create());
        SideNavItem adminMaintenanceAppointmentsNav = new SideNavItem("Citas", AppointmentGridView.class, VaadinIcon.CALENDAR_USER.create());
        SideNavItem adminMaintenanceAppointmentDiaryNav = new SideNavItem("Agenda", DiaryGridView.class, VaadinIcon.CALENDAR_O.create());
        SideNavItem adminMaintenanceRegisterNav = new SideNavItem("Registros");
        SideNavItem adminMaintenanceCategoriesNav = new SideNavItem("Categorías", CategoriesGridView.class, VaadinIcon.CLIPBOARD_USER.create());
        SideNavItem adminMaintenanceContentNav = new SideNavItem("Newsletter", NewsletterGridView.class, VaadinIcon.USER_HEART.create());
        SideNavItem adminMaintenanceNewsletterNav = new SideNavItem("Mensajes", NotificationGridView.class, VaadinIcon.ACADEMY_CAP.create());
        SideNavItem adminMaintenanceInsuranceNav = new SideNavItem("Aseguradoras", InsuranceGridView.class, VaadinIcon.BRIEFCASE.create());
        SideNavItem adminMaintenanceRoleNav = new SideNavItem("Control", AccessGridView.class, VaadinIcon.MALE.create());
        SideNavItem adminMaintenanceAccessNav = new SideNavItem("Accesos", UserAccessGridView.class, VaadinIcon.KEY.create());
        adminMaintenanceRegisterNav.addItem(adminMaintenanceAccessNav,adminMaintenanceRoleNav);

        adminMaintenanceAppointmentNav.addItem(adminMaintenanceAppointmentDiaryNav, adminMaintenanceAppointmentsNav, adminMaintenanceAppointmentTypeNav);

        adminMaintenanceNav.addItem(adminMaintenanceCategoriesNav, adminMaintenanceContentNav,
                adminMaintenanceNewsletterNav, adminMaintenanceInsuranceNav, adminMaintenanceAppointmentNav,adminMaintenanceRegisterNav);

        nav.addItem(usersNav, adminMaintenanceNav);
        addToDrawer(userHl, nav);
    }

    /**
     * Creación del menú lateral de la aplicación, para el sanitario
     * define la barra de navegación con los diferentes enlaces.
     */
    private void createSanitaryDrawer(String role) {
        SideNav nav = new SideNav();

        SideNavItem homeNav;
        SideNavItem userNav = new SideNavItem("Pacientes", PatientsGridView.class, VaadinIcon.USERS.create());
        checkNotification();
        if (role.equalsIgnoreCase(ConstantUtilities.ROLE_SECRETARY)) { //Cada rol tiene una funcionalidad distinta para cada cita
            homeNav = new SideNavItem("Usuarios");
            SideNavItem patientsNav = new SideNavItem("Pacientes", PatientsGridView.class, VaadinIcon.USER_HEART.create());
            SideNavItem sanitariesNav = new SideNavItem("Trabajadores", HomeView.class, VaadinIcon.USERS.create());
            homeNav.addItem(patientsNav, sanitariesNav);

            SideNavItem maintenanceAppointmentNav = new SideNavItem("Citas");
            SideNavItem maintenanceAppointmentTypeNav = new SideNavItem("Tipos de citas", AppointmentTypeGridView.class, VaadinIcon.CALENDAR_BRIEFCASE.create());
            SideNavItem maintenanceAppointmentsNav = new SideNavItem("Citas", AppointmentGridView.class, VaadinIcon.CALENDAR_USER.create());
            SideNavItem maintenanceAppointmentDiaryNav = new SideNavItem("Agenda", DiaryGridView.class, VaadinIcon.CALENDAR_O.create());
            maintenanceAppointmentNav.addItem(maintenanceAppointmentsNav, maintenanceAppointmentDiaryNav, maintenanceAppointmentTypeNav);

            SideNavItem maintenanceNav = new SideNavItem("Mantenimiento");
            SideNavItem maintenanceNewsletterNav = new SideNavItem("Mensajes", NotificationGridView.class, VaadinIcon.ACADEMY_CAP.create());

            SideNavItem insuranceMaintenanceNav = new SideNavItem("Aseguradoras", InsuranceGridView.class, VaadinIcon.BRIEFCASE.create());
            maintenanceNav.addItem(insuranceMaintenanceNav, maintenanceNewsletterNav,maintenanceAppointmentNav);

            nav.addItem(homeNav, maintenanceNav);

        } else { //Si es un sanitario
            homeNav = new SideNavItem("Mi agenda", MyAppointmentsDayGridView.class, VaadinIcon.CALENDAR.create());
            SideNavItem maintenanceNav = new SideNavItem("Mantenimiento");
            SideNavItem maintenanceCategoriesNav = new SideNavItem("Categorías", CategoriesGridView.class, VaadinIcon.CLIPBOARD_USER.create());
            SideNavItem maintenanceContentNav = new SideNavItem("Newsletters", NewsletterGridView.class, VaadinIcon.USER_HEART.create());
            maintenanceNav.addItem(maintenanceCategoriesNav, maintenanceContentNav);

            SideNavItem shippingSystemNav = new SideNavItem("Mensajería");
            SideNavItem chatSanitaryNav = new SideNavItem("Trabajadores", SelectedSanitaryMessengerServiceView.class, VaadinIcon.DOCTOR.create());
            SideNavItem chatPatientNav = new SideNavItem("Pacientes", SelectedMessengerServiceView.class, VaadinIcon.USER.create());

            shippingSystemNav.addItem(chatSanitaryNav, chatPatientNav);
            nav.addItem(homeNav, userNav, shippingSystemNav, maintenanceNav);

        }
        addToDrawer(homeNav, userHl, nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (authenticationContext.getPrincipalName().isEmpty()) {
            beforeEnterEvent.forwardTo("/login");
        }
    }
}