package es.obstetrics.obstetric.view.priv.views.myFolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.home.HomeView;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.appointment.ChooseAppointmentCharacteristicsView;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(value="patients/my-appointments", layout = PrincipalView.class)
public class MyAppointments extends Div {

    private final UserCurrent userCurrent;
    private final AppointmentService appointmentService;

    public MyAppointments(UserCurrent userCurrent, AppointmentService appointmentService){
        this.userCurrent = userCurrent;
        this.appointmentService = appointmentService;

        UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.HOME.create(), new UserEntity(), new H3("Mis citas"));
        header.getButton().addClickListener(buttonClickEvent ->
                UI.getCurrent().navigate(HomeView.class)//Se dirige a la ventana anterior
        );
        Button requestAppointmentButton = new Button("Pedir cita");
        requestAppointmentButton.addClickListener(event ->  UI.getCurrent().navigate(ChooseAppointmentCharacteristicsView.class, userCurrent.getCurrentUser().getDni()));
        requestAppointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout requestAppointmentHl = new HorizontalLayout(requestAppointmentButton);
        requestAppointmentHl.setMargin(true);
        add(header,requestAppointmentHl, createPersonalDataAndAppointmentBox());
    }
    /**
     * Añade los FL responsivos de datos de usuarios y citas a otro FL
     * @return El HL con los FL responsivos.
     */
    public HorizontalLayout createPersonalDataAndAppointmentBox() {

        FormLayout allDates = new FormLayout(createBox(createFutureAppointmentsBox()), createBox(createPastAppointmentsBox()));
        allDates.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("1000px", 2));
        allDates.setSizeFull();
        HorizontalLayout allDatesHl = new HorizontalLayout(allDates);
        allDatesHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        allDatesHl.addClassName("grid-container");
        allDatesHl.setSizeFull();
        allDatesHl.setPadding(true);
        return allDatesHl;
    }

    /**
     * Crea el formlayout con las citas pendientes del paciente.
     * @return El formlayout con las citas.
     */
    private FormLayout createFutureAppointmentsBox() {
        Grid<AppointmentEntity> grid = new Grid<>(AppointmentEntity.class, false);
        grid.addColumn(AppointmentEntity::getDate).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(AppointmentEntity::getStartTime).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity()).setHeader("Médico").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName()).setHeader("Centro").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getAppointmentTypeEntity().getDescription()).setHeader("Tipo de cita").setAutoWidth(true);

        grid.setItems(setPatientAppointments(2));
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        Button refreshIcon = createIconButton();
        refreshIcon.addClickListener(event -> grid.setItems(setPatientAppointments(2)));
        HorizontalLayout title = createTitleH4("Citas pendientes",
                createIcon(),
                refreshIcon);
        FormLayout box = createFormLayout();
        box.add(title,grid);
        box.setColspan(title, 2);
        box.setColspan(grid, 2);
        box.addClassName("square-box");
        box.setSizeFull();
        return box;
    }
    /**
     * Crea el formlayout con las citas pasadas del paciente.
     * @return El formlayout con las citas.
     */
    private FormLayout createPastAppointmentsBox() {
        Grid<AppointmentEntity> grid = new Grid<>(AppointmentEntity.class, false);
        grid.addColumn(AppointmentEntity::getDate).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(AppointmentEntity::getStartTime).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity()).setHeader("Médico").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName()).setHeader("Centro").setAutoWidth(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getAppointmentTypeEntity().getDescription()).setHeader("Tipo de cita").setAutoWidth(true);

        List<AppointmentEntity> appointmentEntities = setPatientAppointments(1);
        grid.setItems(appointmentEntities);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        Button refreshIcon = createIconButton();
        refreshIcon.addClickListener(event -> grid.setItems(setPatientAppointments(1))); //Carga de nuevo las citas

        HorizontalLayout title = createTitleH4("Citas pasadas",
                createIcon(),
                refreshIcon);

        FormLayout box = createFormLayout();
        box.add(title,grid);
        box.setColspan(title, 2);
        box.setColspan(grid, 2);
        box.addClassName("square-box");
        box.setSizeFull();
        return box;
    }


    /**
     * Crea un FL.
     * @return El FL resposivo.
     */
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("1000px", 2));
        return formLayout;
    }

    /**
     * Crea el titulo de cabecera de cada una de las box.
     * @param titleH4 El titulo.
     * @param icon El icono relacionado con el título.
     * @param iconRefresh El icono de refrescar.
     * @return El HL con todos los datos.
     */
    private HorizontalLayout createTitleH4(String titleH4, Icon icon, Button iconRefresh) {
        H4 title = new H4(titleH4);
        title.addClassNames("title-dark-green","margin-title-dark-green");
        HorizontalLayout horizontalLayoutTitleAndIcon = new HorizontalLayout(title,icon);
        horizontalLayoutTitleAndIcon.setWidthFull();
        horizontalLayoutTitleAndIcon.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        HorizontalLayout horizontalLayout = new HorizontalLayout(horizontalLayoutTitleAndIcon, iconRefresh);
        horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.START, iconRefresh);
        horizontalLayout.setWidthFull();
        return new HorizontalLayout(horizontalLayout);
    }


    /**
     * Crea un botón y le da el estilo de color verde.
     *
     * @return El botón.
     */
    private Button createIconButton() {
        Button button = new Button(new Icon(VaadinIcon.REFRESH));
        button.setTooltipText("Refrescar");
        button.addClassName("interactive-button");
        return button;
    }
    /**
     * Crea un icono con mediante un VaadinIcon y le da el estilo de color verde.
     *
     * @return El icono.
     */
    private Icon createIcon() {
        Icon icon = new Icon(VaadinIcon.CALENDAR_USER);
        icon.setColor("var(--dark-green-color)");
        return icon;
    }


    /**
     * Crea la lista futura o pasada del paciente,
     * @param value 1 si se desea la lista futura, 2 si se desa la lista pasada.
     * @return La lista de citas correspondiente.
     */
    private List<AppointmentEntity> setPatientAppointments(int value) {
        if(value == 2){
            return appointmentService.findByPatientEntityAndState((PatientEntity) userCurrent.getCurrentUser(), ConstantUtilities.STATE_ACTIVE);

        }else{
            return appointmentService.findByPatientEntityAndState((PatientEntity) userCurrent.getCurrentUser(), ConstantUtilities.STATE_INACTIVE);
        }
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
}
