package es.obstetrics.obstetric.view.priv.views.myFolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.ReportService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.listings.pdf.GynecologistReportPdf;
import es.obstetrics.obstetric.listings.pdf.MatronReportPdf;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.NewsletterPatientView;
import es.obstetrics.obstetric.view.priv.views.users.ProfileUserView;

import java.util.ArrayList;
import java.util.List;

public class MyFolder extends Div {

    private final PatientEntity patientEntity;
    private final ReportService reportService;
    private final NewsletterService newsletterService;
    private final UserCurrent userCurrent;
    private final Grid<AppointmentEntity> appointmentGrid;
    private final Grid<NewsletterEntity> newslettersGrid;
    private final  Grid<ReportEntity> reportsGrid;
    private final AppointmentService appointmentService;
    private final UserService userService;

    public MyFolder(NewsletterService newsletterService,
                    ReportService reportService,
                    AppointmentService appointmentService,
                    PatientEntity patientEntity,
                    UserCurrent userCurrent,
                    UserService userService) {

        this.userService= userService;
        this.newsletterService = newsletterService;
        this.reportService = reportService;
        this.patientEntity = patientEntity;
        this.userCurrent = userCurrent;
        this.appointmentService = appointmentService;

        reportsGrid = new Grid<>(ReportEntity.class, false);
        appointmentGrid = new Grid<>(AppointmentEntity.class, false);
        newslettersGrid = new Grid<>(NewsletterEntity.class, false);

        if(userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)){
            UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.HOME.create(),new UserEntity(), new H3("Mi carpeta"));

            add(header,createPersonalDataAndAppointmentBox(), createReportAndNewsletterBox());
        }else if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_SECRETARY)){
            add(createPersonalDataAndAppointmentBox());
        }else{
            add(createPersonalDataAndAppointmentBox(), createReportAndNewsletterBox());
        }

        refreshNewsletterGrid();
        refreshReportsGrid();
        refreshAppointmentGrid();
    }

    /**
     * Añade los FL responsivos de datos de newsletters e informes a otro FL
     * @return El HL con los FL responsivos.
     */
    private HorizontalLayout createReportAndNewsletterBox() {
        FormLayout allDates = new FormLayout(createBox(createReportsDataGrid()), createBox(createNewsletterBox()));
        allDates.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 2));
        allDates.setSizeFull();
        HorizontalLayout allDatesHl = new HorizontalLayout(allDates);
        allDatesHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        allDatesHl.setSizeFull();
        allDatesHl.setPadding(true);
        return allDatesHl;
    }

    /**
     * Añade los FL responsivos de datos de usuarios y citas a otro FL
     * @return El HL con los FL responsivos.
     */
    public HorizontalLayout createPersonalDataAndAppointmentBox() {

        FormLayout allDates = new FormLayout(createBox(createPersonalDataBox()), createBox(createAppointmentsBox()));
        allDates.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("800px", 2));
        allDates.setSizeFull();
        HorizontalLayout allDatesHl = new HorizontalLayout(allDates);
        allDatesHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        allDatesHl.setSizeFull();
        allDatesHl.setPadding(true);
        return allDatesHl;
    }

    private Button createButton(Icon icon) {
        Button button = new Button(icon);
        button.setTooltipText("Descargar");
        button.addClassName("dark-green-background-button");
        return button;
    }

    /**
     * Crea el formlayout con los informes del paciente.
     * @return El formlayout con los informes.
     */
    private FormLayout createReportsDataGrid() {

        reportsGrid.addColumn(reportEntity -> {
            if(reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                return  reportEntity.getDate().getDayOfMonth() + "-"+reportEntity.getDate().getDayOfMonth() + "-" + reportEntity.getDate().getYear();
            }
            return null;
        }).setHeader("Día").setAutoWidth(true);

        reportsGrid.addColumn(reportEntity -> {
            if(reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                return  reportEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity();
            }
            return null;
        }).setHeader("Sanitario").setAutoWidth(true);

        reportsGrid.addColumn(reportEntity -> {
            if(reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                return  reportEntity.getAppointmentEntity().getAppointmentTypeEntity().getDescription();
            }
            return null;
        }).setHeader("Tipo de cita").setAutoWidth(true);

        reportsGrid.addColumn(new ComponentRenderer<>(reportEntity -> {
            if (reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {

                Button printButton = createButton(VaadinIcon.DOWNLOAD.create());
                printButton.setTooltipText("Descargar informe");
                printButton.addClickListener(event -> {
                    Anchor downloadLink = getAnchor(reportEntity);
                    downloadLink.getElement().setAttribute("download", true);
                    downloadLink.getElement().setAttribute("hidden", true);
                    add(downloadLink);
                    downloadLink.getElement().callJsFunction("click");

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Esperar 1 segundo antes de eliminar el Anchor
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        getUI().ifPresent(ui -> ui.access(() -> {
                            remove(downloadLink);
                        }));
                    }).start();
                });
                return new HorizontalLayout(printButton);
            }
            return null;
        })).setHeader("PDF").setAutoWidth(true).setFrozenToEnd(true);
        reportsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        refreshReportsGrid();

        Button locationIcon = createIconButton(VaadinIcon.LOCATION_ARROW_CIRCLE);
        locationIcon.addClickListener(event -> UI.getCurrent().navigate(MyReportsView.class));
        locationIcon.setTooltipText("Ver todos mis informes");
        Button refreshIcon = createIconButton(VaadinIcon.REFRESH);
        refreshIcon.addClickListener(event -> refreshReportsGrid()); //Carga de nuevo las citas

        HorizontalLayout title;
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            title = createTitleH4("Informes",
                    createIcon(VaadinIcon.CLIPBOARD_HEART),
                    refreshIcon,
                    locationIcon);
        }else{
            title = createTitleH4("Informes",
                    createIcon(VaadinIcon.CLIPBOARD_HEART),
                    refreshIcon,
                    null);
        }


        FormLayout box = createFormLayout();
        box.addClassName("square-box");
        box.add(title,reportsGrid);
        box.setColspan(title, 2);
        box.setColspan(reportsGrid, 2);
        box.setSizeFull();
        return box;
    }

    private Anchor getAnchor(ReportEntity reportEntity) {
        StreamResource resource;
        if(reportEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)){
            resource = new StreamResource("informe.pdf", () -> new GynecologistReportPdf((GynecologistReportEntity) reportEntity).generatePdf());
        }else{
            resource = new StreamResource("informe.pdf", () -> new MatronReportPdf((MatronReportEntity) reportEntity).generatePdf());
        }
        return new Anchor(resource, "");
    }

    private void refreshReportsGrid() {
        reportsGrid.setItems(reportService.findByPatientEntityAndState(patientEntity, ConstantUtilities.STATE_INACTIVE));
    }

    /**
     * Crea el formlayout con las citas pendientes del paciente.
     * @return El formlayout con las citas.
     */
    private FormLayout createAppointmentsBox() {

        appointmentGrid.addColumn(AppointmentEntity::getDate).setHeader("Fecha").setAutoWidth(true).setAutoWidth(true);
        appointmentGrid.addColumn(AppointmentEntity::getStartTime).setHeader("Hora").setAutoWidth(true);
        appointmentGrid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName()).setHeader("Médico").setAutoWidth(true);
        appointmentGrid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName()).setHeader("Centro").setAutoWidth(true);
        appointmentGrid.addColumn(appointmentEntity -> appointmentEntity.getAppointmentTypeEntity().getDescription()).setHeader("Tipo de cita").setAutoWidth(true);
        appointmentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        refreshAppointmentGrid();

        Button locationIcon = createIconButton(VaadinIcon.LOCATION_ARROW_CIRCLE);
        locationIcon.addClickListener(event -> UI.getCurrent().navigate(MyAppointments.class));
        locationIcon.setTooltipText("Ver todas mis citas");
        Button refreshIcon = createIconButton(VaadinIcon.REFRESH);
        refreshIcon.addClickListener(event -> refreshAppointmentGrid()); //Carga de nuevo las citas
        HorizontalLayout title;
        if(userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)){
            title = createTitleH4("Citas pendientes",
                    createIcon(VaadinIcon.CALENDAR_USER),
                    refreshIcon,
                    locationIcon);
        }else{
            title = createTitleH4("Citas pendientes",
                    createIcon(VaadinIcon.CALENDAR_USER),
                    refreshIcon,
                    null);
        }


        FormLayout box = createFormLayout();
        box.addClassName("square-box");
        box.add(title,appointmentGrid);
        box.setColspan(title, 2);
        box.setColspan(appointmentGrid, 2);
        box.setSizeFull();
        return box;
    }

    private void refreshAppointmentGrid() {
        appointmentGrid.setItems(appointmentService.findByPatientEntityAndState(patientEntity, ConstantUtilities.STATE_ACTIVE));
    }


    /**
     * Crea el formlayout con los datos personales del paciente.
     * @return El formlayout.
     */
    private FormLayout createPersonalDataBox() {
        HorizontalLayout title;
        if(userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)){
            Button locationIcon = createIconButton(VaadinIcon.LOCATION_ARROW_CIRCLE);
            locationIcon.addClickListener(event -> UI.getCurrent().navigate(ProfileUserView.class));
            locationIcon.setTooltipText("Ver mis datos");
            title = createTitleH4("Mis datos",
                    createIcon(VaadinIcon.USER_HEART),
                    null,
                    locationIcon);
        }else{
            title = createTitleH4("Datos personales y médicos",
                    createIcon(VaadinIcon.USER_HEART),
                    null,
                    null);
        }


        FormLayout personalInformationFL = new FormLayout();
        personalInformationFL.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2),       // Menor o igual a 0px - 2 columnas
                new FormLayout.ResponsiveStep("600px", 2),   // Entre 0 y 600px - 2 columnas
                new FormLayout.ResponsiveStep("900px", 3));
        PregnanceEntity pregnance = Utilities.isPregnantActive(new ArrayList<>(patientEntity.getPregnancies()));
        String week="No registrado";
        String quarter;

        if (pregnance != null) {
            week = String.valueOf(Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()));
            if (Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()) >= 0 && Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()) <= 12) { //Primer trimestre
                quarter = ConstantUtilities.FIRST_QUARTER;
            } else if (Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()) >= 13 && Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()) <= 24) { //Segundo trimestre
                quarter = ConstantUtilities.SECOND_QUARTER;
            } else { //Tercer trimestre
                quarter = ConstantUtilities.THIRD_QUARTER;
            }
        }else{
            quarter = "No registrado";
        }

        personalInformationFL.add(title,
                createDataDiv("Nombre", patientEntity.getName()),
                createDataDiv("Apellidos", patientEntity.getLastName()),
                createDataDiv("DNI", patientEntity.getDni()),
                createDataDiv("Edad", patientEntity.getAge()),
                createDataDiv("Sexo", patientEntity.getSex()),
                createDataDiv("Email", patientEntity.getEmail()),
                createDataDiv("Dirección", patientEntity.getAddress()),
                createDataDiv("Código postal", patientEntity.getPostalCode()),
                createDataDiv("Nombre de usuario", patientEntity.getUsername()),
                createDataDiv("Teléfono", patientEntity.getPhone()),
                createDataDiv("Alergias", patientEntity.getAllergies()),
                createDataDiv("Número de embarazos", String.valueOf(patientEntity.getPregnancies().size())),
                createDataDiv("Tipo de sangre", patientEntity.getBloodType()),
                createDataDiv("Rh", patientEntity.getRh()),
                createDataDiv("Semana de embarazo",week),
                createDataDiv("Trimestre de embarazo", quarter));

        personalInformationFL.setColspan(title, 4);
        personalInformationFL.addClassName("square-box");
        personalInformationFL.setSizeFull();
        return personalInformationFL;
    }

    /**
     * Crea un div que contiene el título y el dato personal del paciente y le da su estilo correspondiente.
     * @param title Titulo del dato.
     * @param data Dato personal
     * @return El div con los datos.
     */
    private Div createDataDiv(String title, String data){
        Div dataDiv = new Div(createTitle(title),createPersonalDateH5(data));
        dataDiv.addClassName("data-container");
        return dataDiv ;
    }

    /**
     * Crea un HL para añadirselo al FL.
     * @param fl El FL que va a formal el HL.
     * @return El HL.
     */
    private HorizontalLayout createBox(FormLayout fl){
        HorizontalLayout personalInformationHl = new HorizontalLayout();
        personalInformationHl.add(fl);
        return personalInformationHl;
    }

    /**
     * Crea un titulo y le da un estilo.
     * @param title El titulo que se quiere crear.
     * @return El titulo creado
     */
    private H5 createPersonalDateH5(String title) {
        H5 titleH5 = new H5(title);
        titleH5.addClassName("color-title");
        return titleH5;
    }
    /**
     * Crea un span y le da un estilo.
     * @param title El titulo delspan que se quiere crear.
     * @return El span creado
     */
    private Span createTitle(String title) {
        Span titleSpan = new Span(title);
        titleSpan.addClassName("data-personal-info");
        return titleSpan;
    }

    /**
     * Crea el titulo de cabecera de cada una de las box.
     * @param titleH4 El titulo.
     * @param icon El icono relacionado con el título.
     * @param iconRefresh El icono de refrescar (no será nulo cuando el box contenga un grid)
     * @param iconNavigate El icono de navegar a la pagina correspondiente al box.
     * @return El HL con todos los datos.
     */
    private HorizontalLayout createTitleH4(String titleH4, Icon icon, Button iconRefresh, Button iconNavigate) {
        H4 title = new H4(titleH4);
        title.addClassNames("title-dark-green","margin-title-dark-green");
        HorizontalLayout horizontalLayoutTitleAndIcon = new HorizontalLayout(title,icon);
        horizontalLayoutTitleAndIcon.setSizeFull();
        horizontalLayoutTitleAndIcon.setJustifyContentMode(FlexComponent.JustifyContentMode.START);


        HorizontalLayout horizontalLayoutIconRight;
        if(iconRefresh == null && iconNavigate == null){
            return horizontalLayoutTitleAndIcon;
        }
        if(iconRefresh != null && iconNavigate != null){
            horizontalLayoutIconRight = new HorizontalLayout(iconRefresh,iconNavigate);
        }else if(iconNavigate != null){
            horizontalLayoutIconRight = new HorizontalLayout(iconNavigate);
        }else{
        horizontalLayoutIconRight = new HorizontalLayout(iconRefresh);
    }


        horizontalLayoutIconRight.setSizeFull();
        horizontalLayoutIconRight.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return new HorizontalLayout(horizontalLayoutTitleAndIcon, horizontalLayoutIconRight);
    }

    /**
     * Crea un FL.
     * @return El FL resposivo.
     */
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 2));
        return formLayout;
    }

    /**
     * Crea el formlayout con las newsleters del paciente.
     * @return El formlayout con las nesletters.
     */
    private FormLayout createNewsletterBox() {
        newslettersGrid.addColumn(NewsletterEntity::getName).setHeader("Nombre").setAutoWidth(true);
        newslettersGrid.addColumn(NewsletterEntity::getSummary).setHeader("Descripción").setAutoWidth(true);

        newslettersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        refreshNewsletterGrid();
        Button locationIcon = createIconButton(VaadinIcon.LOCATION_ARROW_CIRCLE);
        locationIcon.addClickListener(event -> UI.getCurrent().navigate(NewsletterPatientView.class));
        locationIcon.setTooltipText("Ver mis newsletters");
        Button refreshIcon = createIconButton(VaadinIcon.REFRESH);
        refreshIcon.addClickListener(event -> refreshNewsletterGrid()); //Carga de nuevo las citas
        HorizontalLayout title;
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            title = createTitleH4("Newsletters",
                    createIcon(VaadinIcon.NEWSPAPER),
                    refreshIcon,
                    locationIcon);
        }else{
            title = createTitleH4("Newsletters",
                    createIcon(VaadinIcon.NEWSPAPER),
                    refreshIcon,
                    null);
        }

        FormLayout box = createFormLayout();
        box.addClassName("square-box");
        box.add(title,newslettersGrid);
        box.setColspan(title, 2);
        box.setColspan(newslettersGrid, 2);
        box.setSizeFull();
        return box;

    }

    private void refreshNewsletterGrid() {
        newslettersGrid.setItems(getPatientNewsletters());
    }

    /**
     * Busca las newsletter asociadas al paciente
     * @return La lista de newslteres.
     */
    private List<NewsletterEntity> getPatientNewsletters() {

        List<NewsletterEntity> newsletterEntities = new ArrayList<>();
        PatientEntity user = (PatientEntity) userService.findOneByDniAndRole(patientEntity.getDni(), ConstantUtilities.ROLE_PATIENT);
        if (user.getPregnancies() != null) {
            PregnanceEntity pregnancy = Utilities.isPregnantActive(user.getPregnancies());
            if (pregnancy != null) {
                int quarter = Utilities.quarterCalculator(pregnancy.getLastPeriodDate().toEpochDay());
                List<NewsletterEntity> newsletters;
                if (quarter >= 0 && quarter <= 12) { //Primer trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.FIRST_QUARTER, ConstantUtilities.STATE_ACTIVE); //Lista de contenidos

                } else if (quarter >= 13 && quarter <= 24) { //Segundo trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.SECOND_QUARTER, ConstantUtilities.STATE_ACTIVE);//Lista de contenidos

                } else { //Tercer trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.THIRD_QUARTER, ConstantUtilities.STATE_ACTIVE);//Lista de contenidos
                }
                newsletterEntities.addAll(newsletters);
                newsletterEntities.addAll(patientEntity.getNewsletters());
            }
        }
        return newsletterEntities;
    }

    /**
     * Crea un botón y le da el estilo de color verde.
     * @param vaadinIcon Icono que se va a mostrar em el botón
     * @return El botón.
     */
    private Button createIconButton(VaadinIcon vaadinIcon) {
        Button button = new Button(new Icon(vaadinIcon));
        button.addClassName("interactive-button");
        return button;
    }
    /**
     * Crea un icono con mediante un VaadinIcon y le da el estilo de color verde.
     * @param vaadinIcon Icono que se va a mostrar
     * @return El icono.
     */
    private Icon createIcon(VaadinIcon vaadinIcon) {
        Icon icon = new Icon(vaadinIcon);
        icon.setColor("var(--dark-green-color)");
        return icon;
    }
}
