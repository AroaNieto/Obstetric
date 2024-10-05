package es.obstetrics.obstetric.view.priv.views.maintenance.appointment;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.AppointmentTypeService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.EmailUtility;
import es.obstetrics.obstetric.listings.pdf.AppointmentGridViewPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.appointment.UnsubscribeAppointmentConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.appointment.AppointmentForInsuranceDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.reflections.Reflections.log;

/**
 * Clase encargada de mostrar las citas en un grid con sus respectivos filtros,
 * con el objetivo de que el administrador pueda consultarlas
 */
@Route(value = "secretary/appointments", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class AppointmentGridView extends MasterGrid<AppointmentEntity> {
    private final AppointmentService appointmentService;
    private final AppointmentTypeService appointmentTypeService;
    private Button deleteBtn;
    private Button reactivateBtn;
    private final EmailUtility emailUtility;

    @Autowired
    public AppointmentGridView(AppointmentService appointmentService,
                               EmailUtility emailUtility,
                               AppointmentTypeService appointmentTypeService) {
        this.appointmentService = appointmentService;
        this.appointmentTypeService = appointmentTypeService;
        this.emailUtility = emailUtility;
        setHeader(new H2("CITAS"));

        setGrid();
        setFilterContainer();
        updateGrid();
    }

    @Override
    public void openDialog() {

    }

    private boolean isIdentical(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    @Override
    public void setFilterContainer() {
        gridListDataView = masterGrid.setItems(appointmentService.findAll()); //Configuración del DataView
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchTextField.setPlaceholder("Buscar paciente o sanitario");
        searchTextField.setTooltipText("Escriba el nombre o apellido del sanitario o paciente que desea buscar");
        searchTextField.setPrefixComponent(new Icon(VaadinIcon.USER));
        searchTextField.addValueChangeListener(event -> {
            gridListDataView.addFilter(appointmentEntity -> {
                String search = searchTextField.getValue().trim();
                if (search.isEmpty()) return true;
                boolean patientName = false;
                boolean patientLastname = false;
                boolean sanitaryLastname = false;
                boolean sanitaryName = false;
                if (appointmentEntity.getPatientEntity() != null) {
                    patientName = isIdentical(appointmentEntity.getPatientEntity().getName(), search);
                    patientLastname = isIdentical(appointmentEntity.getPatientEntity().getLastName(), search);
                }
                if (appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() != null) {
                    sanitaryName = isIdentical(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName(), search);
                    sanitaryLastname = isIdentical(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName(), search);
                }
                return patientName || patientLastname || sanitaryLastname || sanitaryName;
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE), "help-button");
        helpButton.setTooltipText("Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTE_HELP_APPOINTMENT,
                    "Guía gestión de citas");
            windowHelp.open();
        });

        Button printButton = createButton(new Icon(VaadinIcon.PRINT), "help-button");
        printButton.setTooltipText("Impimir listado");
        printButton.addClickListener(event -> printButton());

        Button downloadButton = createButton(new Icon(VaadinIcon.DOWNLOAD), "primary-color-button");
        downloadButton.setTooltipText("Descargar citas por aseguradora");
        downloadButton.setText("Citas por aseguradora");
        downloadButton.addClickListener(event -> {
            AppointmentForInsuranceDialog appointment = new AppointmentForInsuranceDialog(appointmentService);
            appointment.open();
        });
        addBtn.setVisible(false);
        filterContainerHl.add(searchTextField,
                createStartTime(),
                createAppointmentTypeCombo(),
                addBtn, downloadButton, printButton, helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Las citas es pasan mediante carga diferencia, solo cuando el usuario solicita
     * la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("citas.pdf", () -> {
            List<AppointmentEntity> appointmentEntities = gridListDataView.getItems().collect(Collectors.toList());
            return new AppointmentGridViewPdf((ArrayList<AppointmentEntity>) appointmentEntities).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de citas");
        dialog.open();
    }

    private Component createAppointmentTypeCombo() {
        ComboBox<AppointmentTypeEntity> appointmentTypeEntityComboBox = new ComboBox<>("Tipo de cita");
        appointmentTypeEntityComboBox.setItems(appointmentTypeService.findAll());
        appointmentTypeEntityComboBox.addClassName("text-field-1300");
        appointmentTypeEntityComboBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                updateGrid();
            } else {
                gridListDataView.addFilter(appointmentEntity -> {
                    String search = appointmentTypeEntityComboBox.getValue().getDescription().trim();
                    return isIdentical(appointmentEntity.getAppointmentTypeEntity().getDescription(), search);
                }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
            }

        });
        return appointmentTypeEntityComboBox;
    }

    private Component createStartTime() {
        DatePicker datePicker = new DatePicker("Día de la cita");
        datePicker.addClassName("text-field-1300");
        datePicker.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                updateGrid();
            } else {
                gridListDataView.addFilter(appointmentEntity -> {
                    String search = String.valueOf(datePicker.getValue());
                    return isIdentical(String.valueOf(appointmentEntity.getDate()), search);
                }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
            }


        });
        return datePicker;
    }

    private Button createButton(Icon icon, String className) {
        Button button = new Button(icon);
        button.addClassName(className);
        return button;
    }

    @Override
    public void setGrid() {
        masterGrid.addColumn(createSanitaryRenderer()).setHeader("Sanitario").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getPatientEntity().getName() + " " + appointmentEntity.getPatientEntity().getLastName()).setHeader("Paciente").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getDate().getDayOfMonth() + "/" + appointmentEntity.getDate().getMonthValue() + "/" + appointmentEntity.getDate().getYear()).setHeader("Día").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getStartTime().getHour() + ":" + appointmentEntity.getStartTime().getMinute()).setHeader("Hora de inicio").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getEndTime().getHour() + ":" + appointmentEntity.getEndTime().getMinute()).setHeader("Hora de finalización").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getNotice).setHeader("Notificar").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getReminder).setHeader("Recordar").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getHasAttended).setHeader("Atendido").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getInsurancePolice).setHeader("Póliza del seguro").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getInsuranceEntity).setHeader("Aseguradora").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getAppointmentTypeEntity).setHeader("Tipo de cita").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(appointmentEntity -> {//Si la fecha ya se ha realizado o se está realizando no se puede eliminar ni dar de baja
            if (appointmentEntity.getDate().isBefore(LocalDate.now()) ||
                    (appointmentEntity.getDate().isEqual(LocalDate.now())
                            &&
                            (appointmentEntity.getStartTime().isBefore(LocalTime.now()) || appointmentEntity.getStartTime().equals(LocalTime.now())))) {
                return null;
            }
            reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button");
            reactivateBtn.setTooltipText("Reactivar");
            reactivateBtn.addClickListener(event -> openReactivateAppointment(appointmentEntity));
            if (appointmentEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button");
                deleteBtn.setVisible(false);
                deleteBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(deleteBtn, reactivateBtn);
            }
            reactivateBtn.setVisible(false);
            deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button");
            deleteBtn.addClickListener(event -> openUnsubscribeAppointmentDialog(appointmentEntity));
            deleteBtn.setTooltipText("Dar de baja cita");
            reactivateBtn.setTooltipText("Reactivar cita");
            return new HorizontalLayout(deleteBtn);

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private static Renderer<AppointmentEntity> createSanitaryRenderer() {
        return LitRenderer.<AppointmentEntity>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", appointmentEntity -> {
                    byte[] profilePhoto = appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName())
                .withProperty("email", appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getDni());
    }

    /**
     * Únicamente podrá reactivarse si no existe una cita activa con solapada.
     * @param appointmentEntity Cita sobred la que se quiere operar
     */
    private void openReactivateAppointment(AppointmentEntity appointmentEntity) {
        List<AppointmentEntity> appointmentEntities = appointmentService.findByStartTimeAndStateAndScheduleEntityDiaryEntitySanitaryEntity(appointmentEntity.getStartTime(),ConstantUtilities.STATE_ACTIVE, appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity());
        if(appointmentEntities.isEmpty()){
            appointmentEntities = appointmentService.findByStartTimeAndStateAndPatientEntity(appointmentEntity.getStartTime(),ConstantUtilities.STATE_ACTIVE,appointmentEntity.getPatientEntity());
           if(appointmentEntities.isEmpty()){
               appointmentEntity.setState(ConstantUtilities.STATE_ACTIVE);
               appointmentService.save(appointmentEntity);
               reactivateBtn.setVisible(false);
               deleteBtn.setVisible(true);
               sendMail(appointmentEntity);
               updateGrid();
           }

        }

    }

    private void sendMail(AppointmentEntity appointmentEntity) {
        try {
            //Se envía el correo al sanitario y al paciente que la cita ha sido cancelada.
            String calendarEvent1 = createICalendarEvent("Reactivación de cita", "Citación con la paciente" + appointmentEntity.getPatientEntity(), appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName(), appointmentEntity.getDate(), appointmentEntity.getStartTime(), appointmentEntity.getDate(), appointmentEntity.getEndTime());
            emailUtility.sendEmail(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getEmail(), "Cita", "El día " + appointmentEntity.getDate() +
                    " tiene una cita a las " + appointmentEntity.getStartTime().getHour() + ":" + appointmentEntity.getStartTime().getMinute() +
                    " con " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() + ", puede agregarla al calendario. ", calendarEvent1);

            String calendarEvent = createICalendarEvent("Reactivación de cita", "Citación con el médico" + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity(), appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName(), appointmentEntity.getDate(), appointmentEntity.getStartTime(), appointmentEntity.getDate(), appointmentEntity.getEndTime());
            emailUtility.sendEmail(appointmentEntity.getPatientEntity().getEmail(), "Cita", "El día " + appointmentEntity.getDate() +
                    " tiene una cita a las " + appointmentEntity.getStartTime().getHour() + ":" + appointmentEntity.getStartTime().getMinute() +
                    " con " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() + ", puede agregarla al calendario. ", calendarEvent);
        } catch (Exception e) {
            log.error("Error al enviar correo electrónico: {}", e.getMessage());
        }
    }

    public String createICalendarEvent(String summary, String description, String location,
                                       LocalDate startDate, LocalTime startTime,
                                       LocalDate endDate, LocalTime endTime) {

        ZonedDateTime startDateTime = ZonedDateTime.of(startDate, startTime, ZoneId.of("Europe/Madrid"));
        ZonedDateTime endDateTime = ZonedDateTime.of(endDate, endTime, ZoneId.of("Europe/Madrid"));

        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String startDateTimeFormatted = startDateTime.format(icsFormatter);
        String endDateTimeFormatted = endDateTime.format(icsFormatter);

        return "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID:-//hacksw/handcal//NONSGML v1.0//EN\n" +
                "BEGIN:VEVENT\n" +
                "UID:uid1@example.com\n" +
                "DTSTAMP:" + ZonedDateTime.now(ZoneId.of("UTC")).format(icsFormatter) + "\n" +
                "ORGANIZER;CN=Organizer Name:MAILTO:organizer@example.com\n" +
                "DTSTART:" + startDateTimeFormatted + "\n" +
                "DTEND:" + endDateTimeFormatted + "\n" +
                "SUMMARY:" + summary + "\n" +
                "DESCRIPTION:" + description + "\n" +
                "LOCATION:" + location + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";
    }

    private void openUnsubscribeAppointmentDialog(AppointmentEntity appointmentEntity) {
        UnsubscribeAppointmentConfirmDialog unsubscribeAppointmentConfirmDialog = new UnsubscribeAppointmentConfirmDialog(appointmentEntity, emailUtility);
        unsubscribeAppointmentConfirmDialog.addListener(UnsubscribeAppointmentConfirmDialog.UnsubscribeEvent.class, this::unsubscribeAppointment);
        unsubscribeAppointmentConfirmDialog.open();
    }

    private void unsubscribeAppointment(UnsubscribeAppointmentConfirmDialog.UnsubscribeEvent unsubscribeEvent) {
        unsubscribeEvent.getAppointmentEntity().setState(ConstantUtilities.STATE_INACTIVE);
        appointmentService.save(unsubscribeEvent.getAppointmentEntity());
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        updateGrid();
    }

    @Override
    public void updateGrid() {
        masterGrid.setItems(appointmentService.findAll());
    }
}
