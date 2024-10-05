package es.obstetrics.obstetric.view.priv.views.appointment;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
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
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.DiaryService;
import es.obstetrics.obstetric.backend.service.ReportService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.listings.pdf.GynecologistReportPdf;
import es.obstetrics.obstetric.listings.pdf.MatronReportPdf;
import es.obstetrics.obstetric.listings.pdf.MyAppointmentsDayGridViewPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import es.obstetrics.obstetric.view.priv.views.report.GynecologistReportView;
import es.obstetrics.obstetric.view.priv.views.report.MatronReportView;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase encargada de mostrar las citas del día actual a la médica ginecóloga.
 */
@Route(value = "sanitaries/appointments-day", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class MyAppointmentsDayGridView extends MasterGrid<AppointmentEntity> {
    private final AppointmentService appointmentService;
    private final DiaryService diaryService;
    private final UserCurrent currentUser;
    private DatePicker datePicker;
    private ComboBox<CenterEntity> centerEntityComboBox;
    private List<AppointmentEntity> sanitaryAppointments;
    private ComboBox<DiaryEntity> diaryEntityCombo;
    private final ReportService reportService;

    public MyAppointmentsDayGridView(AppointmentService appointmentService, UserCurrent currentUser, DiaryService diaryService, ReportService reportService) {
        this.reportService = reportService;
        this.appointmentService = appointmentService;
        this.diaryService = diaryService;
        this.currentUser = currentUser;
        sanitaryAppointments = new ArrayList<>();
        setHeader(new H2("MI AGENDA"));

        setGrid();
        setFilterContainer();
        updateGrid();
    }

    @Override
    public void openDialog() {
        UI.getCurrent().navigate(ChooseAppointmentView.class,
                0 + "," +
                        datePicker.getValue() + ","
                        + diaryEntityCombo.getValue().getId());
    }

    @Override
    public void setFilterContainer() {
        gridListDataView = masterGrid.setItems(appointmentService.findAll()); //Configuración del DataView

        searchTextField.setTooltipText("Escriba el nombre o apellidos del paciente que desea buscar.");
        searchTextField.setPlaceholder("Buscar paciente");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto

        searchTextField.addValueChangeListener(event -> {
            gridListDataView.addFilter(appointmentEntity -> {
                String search = searchTextField.getValue().trim();
                if (search.isEmpty()) return true;
                boolean patientName = false;
                boolean patientLastname = false;
                if (appointmentEntity.getPatientEntity() != null) {
                    patientName = isIdentical(appointmentEntity.getPatientEntity().getName(), search);
                    patientLastname = isIdentical(appointmentEntity.getPatientEntity().getLastName(), search);
                }

                return patientName || patientLastname;
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });

        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE), "help-button");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_APPOINTMENT,
                    "Guía mi agenda");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT), "help-button");
        printButton.addClickListener(event -> printButton());
        addBtn.setVisible(false);
        addBtn.setTooltipText("Consultar agendas");
        addBtn.setIcon(VaadinIcon.CALENDAR.create());
        addBtn.setClassName("dark-green-background-button");
        filterContainerHl.add(searchTextField, createDateTextField(),
                createCenterTextField(), createDiaryTextField()
                , addBtn, printButton, helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("citas_" + currentUser.getCurrentUser().getName() + "_" + currentUser.getCurrentUser().getLastName() + ".pdf", () -> {
            List<AppointmentEntity> appointmentEntities = gridListDataView.getItems().collect(Collectors.toList());
            return new MyAppointmentsDayGridViewPdf((ArrayList<AppointmentEntity>) appointmentEntities).generatePdf();
        });
        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de citas");
        dialog.open();
    }

    private Component createDiaryTextField() {
        diaryEntityCombo = new ComboBox<>("Nombre de la agenda");
        diaryEntityCombo.setEnabled(false);
        diaryEntityCombo.addClassName("text-field-1100");
        List<DiaryEntity> diariesEntities = diaryService.findBySanitaryEntityAndCenterEntity((SanitaryEntity) currentUser.getCurrentUser(), centerEntityComboBox.getValue());
        diaryEntityCombo.setItems(diariesEntities);

        diaryEntityCombo.setPrefixComponent(VaadinIcon.CALENDAR_USER.create());
        if (!sanitaryAppointments.isEmpty()) {
            diaryEntityCombo.setValue(sanitaryAppointments.get(0).getScheduleEntity().getDiaryEntity());
        }
        diaryEntityCombo.addValueChangeListener(event -> {
            if (centerEntityComboBox.getValue() == null || datePicker.getValue() == null) {
                addBtn.setVisible(false);
            } else {
                updateGridWithDiary(event.getValue());
                gridListDataView.addFilter(appointmentEntity -> {
                    String search = diaryEntityCombo.getValue().getName().trim();
                    return isIdentical(appointmentEntity.getScheduleEntity().getDiaryEntity().getName(), search);
                }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
                addBtn.setVisible(true);
            }

        });
        return diaryEntityCombo;
    }

    private Component createDateTextField() {
        datePicker = new DatePicker("Día");
        datePicker.addClassName("text-field-1100");
        datePicker.addValueChangeListener(event -> {
            updateGrid();
            gridListDataView.addFilter(appointmentEntity -> {
                String search = String.valueOf(datePicker.getValue());
                return isIdentical(String.valueOf(appointmentEntity.getDate()), search);
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        return datePicker;
    }

    private Component createCenterTextField() {
        centerEntityComboBox = new ComboBox<>("Centro");
        centerEntityComboBox.setEnabled(false);
        centerEntityComboBox.addClassName("text-field-1100");
        centerEntityComboBox.setPrefixComponent(VaadinIcon.HOSPITAL.create());
        centerEntityComboBox.addValueChangeListener(event -> {
            if (centerEntityComboBox.getValue() == null) {
                diaryEntityCombo.setEnabled(false);
                addBtn.setVisible(false);
            } else {
                diaryEntityCombo.setEnabled(true);
            }
            gridListDataView.addFilter(appointmentEntity -> {
                String search = centerEntityComboBox.getValue().getCenterName().trim();
                return isIdentical(appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName(), search);
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        return centerEntityComboBox;
    }

    private Button createButton(Icon icon, String className) {
        Button button = new Button(icon);
        button.addClassName(className);
        return button;
    }

    /**
     * Verifica si la cadena que está escribiendo el usuario mediante el textfield está contenida
     * dentro del nombre de la propiedad.
     */
    private boolean isIdentical(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    @Override
    public void setGrid() {
        masterGrid.addColumn(createPatientRenderer()).setHeader("Paciente").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> {
            PregnanceEntity pregnance = null;
            if (appointmentEntity.getPatientEntity().getPregnancies() != null) {
                for (int i = 0; i < appointmentEntity.getPatientEntity().getPregnancies().size(); i++) {
                    if (appointmentEntity.getPatientEntity().getPregnancies().get(i).getEndingDate() == null) {
                        pregnance = appointmentEntity.getPatientEntity().getPregnancies().get(i);
                        break;
                    }
                }
                if (pregnance != null) {
                    return Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()); //Calculo de la semana en la que se encuentra el embara
                }
            }
            return null;
        }).setHeader("Semana de embarazo").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getDate().getDayOfMonth() + "/" + appointmentEntity.getDate().getMonthValue() + "/" + appointmentEntity.getDate().getYear()).setHeader("Día").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getStartTime() + "-" + appointmentEntity.getEndTime()).setHeader("Hora").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getAppointmentTypeEntity).setHeader("Tipo de cita").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getName()).setHeader("Agenda").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getHasAttended).setHeader("Atendido").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getInsuranceEntity).setHeader("Aseguradora").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentEntity::getInsurancePolice).setHeader("Póliza del seguro").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity()).setHeader("Centro").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(appointmentEntity -> {
            if (LocalDate.now().isAfter(appointmentEntity.getDate().plusDays(30))) { //Si han pasado 30 días y aún no ha hecho el informe, no le permite hacerlo
                return null;
            }
            ReportEntity reportEntity = reportService.findByAppointmentEntity(appointmentEntity);
            if (reportEntity == null || (reportEntity.getState() != null && reportEntity.getState().equals(ConstantUtilities.STATE_ACTIVE))) { //Comprobación de que el informe no está consolidado
                Button appointmentButton = createButton(new Icon(VaadinIcon.CLIPBOARD_USER), "lumo-primary-color-background-button");
                appointmentButton.setTooltipText("Comenzar atención");
                appointmentButton.addClickListener(event -> {
                    if (reportEntity != null) {
                        if (currentUser.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                            UI.getCurrent().navigate(GynecologistReportView.class, reportEntity.getAppointmentEntity().getPatientEntity().getDni() + "," + reportEntity.getId() + "," + reportEntity.getAppointmentEntity().getId());
                        } else {
                            UI.getCurrent().navigate(MatronReportView.class, reportEntity.getAppointmentEntity().getPatientEntity().getDni() + "," + reportEntity.getId() + "," + reportEntity.getAppointmentEntity().getId());
                        }

                    } else {
                        if (currentUser.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                            UI.getCurrent().navigate(GynecologistReportView.class, appointmentEntity.getPatientEntity().getDni() + "," + 0 + "," + appointmentEntity.getId());
                        } else {
                            UI.getCurrent().navigate(MatronReportView.class, appointmentEntity.getPatientEntity().getDni() + "," + 0 + "," + appointmentEntity.getId());
                        }
                    }
                });
                return new HorizontalLayout(appointmentButton);
            } else {
                if (reportEntity.getState() != null && reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                    Button printButton = createButton(VaadinIcon.DOWNLOAD.create(), "dark-green-background-button");
                    printButton.setTooltipText("Descargar informe");
                    printButton.addClickListener(event -> getDownloadLink(reportEntity));
                    return new HorizontalLayout(printButton);
                }
            }
            return null;
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private void getDownloadLink(ReportEntity reportEntity) {
        StreamResource resource;
        if (currentUser.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
            resource = new StreamResource("informe.pdf", () -> new GynecologistReportPdf((GynecologistReportEntity) reportEntity).generatePdf());
        } else {
            resource = new StreamResource("informe.pdf", () -> new MatronReportPdf((MatronReportEntity) reportEntity).generatePdf());
        }
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.getElement().setAttribute("hidden", true);
        add(downloadLink);
        downloadLink.getElement().callJsFunction("click");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Esperar 1 segundo antes de eliminar el Anchor
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getUI().ifPresent(ui -> ui.access(() -> {
                remove(downloadLink);
                //  UI.getCurrent().navigate(MyAppointmentsDayGridView.class); // Navegar después de la descarga
            }));
        }).start();
    }

    private static Renderer<AppointmentEntity> createPatientRenderer() {
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
                    byte[] profilePhoto = appointmentEntity.getPatientEntity().getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", appointmentEntity -> appointmentEntity.getPatientEntity().getName() + " " + appointmentEntity.getPatientEntity().getLastName())
                .withProperty("email", appointmentEntity -> appointmentEntity.getPatientEntity().getAge() + " años.");
    }

    @Override
    public void updateGrid() {
        if (datePicker.getValue() != null) {
            //Busco las citas asociadas al sanitario el día de hoy.
            Set<CenterEntity> centerEntities = new HashSet<>();
            DiaryEntity diaryEntity = null;
            sanitaryAppointments.clear();
            sanitaryAppointments = appointmentService.findByDateAndStateAndScheduleEntityDiaryEntitySanitaryEntity(datePicker.getValue(), ConstantUtilities.STATE_ACTIVE, (SanitaryEntity) currentUser.getCurrentUser());
            for (AppointmentEntity oneAppointment : sanitaryAppointments) {
                centerEntities.add(oneAppointment.getScheduleEntity().getDiaryEntity().getCenterEntity());
                diaryEntity = oneAppointment.getScheduleEntity().getDiaryEntity();
            }
            centerEntityComboBox.setEnabled(true);
            centerEntityComboBox.setItems(new ArrayList<>(centerEntities));
            diaryEntityCombo.setItems(diaryEntity);
            masterGrid.setItems(sanitaryAppointments);

        } else {
            //Busco las citas asociadas al sanitario el día de hoy.
            sanitaryAppointments.clear();
            sanitaryAppointments = appointmentService.findByDateAndStateAndScheduleEntityDiaryEntitySanitaryEntity(LocalDate.now(), ConstantUtilities.STATE_ACTIVE, (SanitaryEntity) currentUser.getCurrentUser());
            datePicker.setValue(LocalDate.now());
            centerEntityComboBox.setEnabled(false);
            addBtn.setVisible(false);
            diaryEntityCombo.setEnabled(false);
            masterGrid.setItems(sanitaryAppointments);
        }
    }

    private void updateGridWithDiary(DiaryEntity value) {
        sanitaryAppointments.clear();
        for (ScheduleEntity oneSchedule : value.getSchedules()) {
            for (AppointmentEntity oneAppointment : oneSchedule.getAppointmentEntities()) {
                if (oneAppointment.getState().equals(ConstantUtilities.STATE_ACTIVE) && oneAppointment.getDate().equals(datePicker.getValue()) &&
                        oneAppointment.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getId().equals(currentUser.getCurrentUser().getId())) {
                    sanitaryAppointments.add(oneAppointment);
                }
            }
        }
        masterGrid.setItems(sanitaryAppointments);
        centerEntityComboBox.setEnabled(false);
        diaryEntityCombo.setEnabled(false);
        addBtn.setVisible(false);
    }
}