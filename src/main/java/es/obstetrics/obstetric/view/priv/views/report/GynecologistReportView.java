package es.obstetrics.obstetric.view.priv.views.report;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.GynecologistReportPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.report.HasComeAppointmentConfirmDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.appointment.MyAppointmentsDayGridView;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Route(value = "sanitary/report", layout = PrincipalView.class)
@PermitAll
public class GynecologistReportView extends Div implements HasUrlParameter<String> {
    private final TextArea gynecologicalExamination;
    private final TextArea anamnesis;
    private final TextArea cytology;
    private final TextArea ultrasound;
    private final TextArea amniocentesis;
    private final TextArea analytics;
    private final TextArea riskFactors;
    private final TextArea diagnosis;
    private final TextArea familyBackground;
    private final TextArea treatment;
    private final TextArea personalHistory;
    private final NumberField menarche;
    private final NumberField numberOfPregnancies;
    private final NumberField numberOfAbortions;
    private final TextArea allergies;
    private final DatePickerTemplate fur;
    private final DatePickerTemplate dateAppointment;
    private final DatePickerTemplate date;
    private final ComboBox<String> rh;
    private final ComboBox<String> bloodType;
    private final TextArea reasonForConsultation;
    private final TextArea subjectiveEvaluations;
    private final TextArea observations;
    private final TextArea fm;
    private final H5 errorMessage;
    private final NumberField age;
    private final Binder<GynecologistReportEntity> reportEntityBinder;
    private PatientEntity patient;
    private final PatientService patientService;
    private GynecologistReportEntity reportEntity = null;
    private final GynecologistReportService reportService;
    private final UserCurrent current;
    private final AppointmentService appointmentService;
    private AppointmentEntity appointmentEntity;
    private final PatientsLogService patientsLogService;
    private final NewsletterService newsletterService;

    @Autowired
    public GynecologistReportView(PatientService patientService,
                                  NewsletterService newsletterService,
                                  GynecologistReportService reportService,
                                  UserCurrent current,
                                  AppointmentService appointmentService,
                                  PatientsLogService patientsLogService) {
        this.current = current;
        this.newsletterService = newsletterService;
        this.patientsLogService = patientsLogService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.reportService = reportService;
        date = new DatePickerTemplate("Día del informe");
        dateAppointment = new DatePickerTemplate("Fecha de consulta");
        dateAppointment.setReadOnly(true);

        dateAppointment.setWidth("300px");
        dateAppointment.setWidth("300px");
        age = createNumberField("Edad", "300px");
        age.setReadOnly(true);

        reasonForConsultation = createTextField("Motivo de consulta", "800px");
        familyBackground = createTextField("Antecedentes familiares", "800px");
        personalHistory = createTextField("Antecedentes personales", "800px");
        allergies = createTextField("Alergias", "300px");
        bloodType = new ComboBox<>("Grupo sanguineo");
        rh = new ComboBox<>();
        rh.addClassName("text-margin");
        rh.setWidth("150px");
        menarche = createNumberField("Menarquia", "300px");
        menarche.addClassName("text-margin");
        menarche.setWidth("150px");
        fm = createTextField("FM", "150px");
        fur = new DatePickerTemplate("FUR");
        fur.addClassName("text-margin");
        fur.setWidth("300px");
        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        numberOfPregnancies = createNumberField("Número de embarazos", "150px");
        numberOfAbortions = createNumberField("Número de abortos", "150px");
        anamnesis = createTextField("Anamnesis", "600px");
        gynecologicalExamination = createTextField("Exploración ginecológica", "800px");
        cytology = createTextField("Citología", "300px");
        ultrasound = createTextField("Ecografía", "800px");
        amniocentesis = createTextField("Amniocentesis", "800px");
        analytics = createTextField("Analítica", "300px");
        riskFactors = createTextField("Factores de riesgo", "800px");
        diagnosis = createTextField("Diagnóstico", "800px");
        treatment = createTextField("Tratamiento", "800px");
        observations = createTextField("Observaciones", "800px");
        subjectiveEvaluations = createTextField("Evaluaciones subjetivas", "600px");

        reportEntityBinder = new Binder<>(GynecologistReportEntity.class);
        reportEntityBinder.bindInstanceFields(this);

    }

    private TextArea createTextField(String title, String maxWidth) {
        TextArea textField = new TextArea(title);
        textField.addClassName("text-margin");
        textField.setWidth(maxWidth);
        return textField;
    }

    private NumberField createNumberField(String title, String maxWidht) {
        NumberField numberField = new NumberField(title);
        numberField.addClassName("text-margin");
        numberField.setWidth(maxWidht);
        return numberField;
    }

    private void fillInPastData() {
        bloodType.setItems(ConstantUtilities.BLOOD_A, ConstantUtilities.BLOOD_O, ConstantUtilities.BLOOD_AB, ConstantUtilities.BLOOD_B);
        rh.setItems(ConstantUtilities.RHMA, ConstantUtilities.RHMI);
        dateAppointment.setValue(appointmentEntity.getDate());
        date.setValue(LocalDate.now());
        age.setValue(Double.valueOf(patient.getAge()));
        if (patient != null) {
            if (patient.getFamilyBackground() != null) {
                familyBackground.setValue(patient.getFamilyBackground());
            }
            if (patient.getPersonalHistory() != null) {
                personalHistory.setValue(patient.getPersonalHistory());
            }
            if (patient.getAllergies() != null) {
                allergies.setValue(patient.getAllergies());
            }
            if (patient.getMenarche() != null) {
                menarche.setValue((double) patient.getMenarche());
            }
            if (patient.getFur() != null) {
                fur.setValue(patient.getFur());
            }
            if (patient.getFm() != null) {
                fm.setValue(patient.getFm());
            }
            if (patient.getNumberOfPregnancies() != null) {
                numberOfPregnancies.setValue(Double.valueOf(patient.getNumberOfPregnancies()));
            }
            if (patient.getNumberOfAbortions() != null) {
                numberOfAbortions.setValue((double) patient.getNumberOfAbortions());
            }
            if (patient.getBloodType() != null) {
                bloodType.setValue(patient.getBloodType());
            }
            if (patient.getRh() != null) {
                rh.setValue(patient.getRh());
            }
        }
    }

    private void createLayout(UserHeaderTemplate header) {

        H4 titleGynecologicalHistory = new H4("Antecedentes ginecológicos");
        H4 complementaryStudies = new H4("Estudios complementarios");

        FormLayout formLayout = getFormLayout(titleGynecologicalHistory, complementaryStudies);

        formLayout.setColspan(titleGynecologicalHistory, 2);
        formLayout.setColspan(complementaryStudies, 2);
        formLayout.setColspan(subjectiveEvaluations, 2);
        formLayout.setColspan(errorMessage, 2);

        VerticalLayout verticalLayout = new VerticalLayout(new H3("INFORME CLÍNICO"), formLayout);
        verticalLayout.setPadding(true);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, formLayout);
        verticalLayout.addClassName("container-report");
        Button acceptButton = new Button("Aceptar");
        acceptButton.addClickListener(event -> acceptButton());
        acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button printButton = new Button("Descargar", VaadinIcon.DOWNLOAD.create());
        printButton.setTooltipText("Descargar");
        printButton.addClassName("dark-gray-color-button");
        printButton.addClickListener(event -> {
            downloadReport();
        });
        Button closeButton = new Button("Consolidar", VaadinIcon.CHECK.create());
        closeButton.addClickListener(event -> consolidateReport());
        closeButton.setTooltipText("Consolidar");

        closeButton.addClassName("dark-green-button");
        HorizontalLayout buttonsHl = new HorizontalLayout(acceptButton, printButton, closeButton);
        buttonsHl.setWidthFull();
        buttonsHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        HorizontalLayout div = new HorizontalLayout(new Div(verticalLayout, buttonsHl));
        div.addClassName("container-div");
        add(header, div);
    }

    private void consolidateReport() {

        if (reasonForConsultation.isEmpty()) {
            setErrorMessage("El motivo de consulta es obligatorio, añadalo.");
            return;
        } else if (familyBackground.isEmpty()) {
            setErrorMessage("Los antecedentes familiares son obligatorios, añadalos.");
            return;
        } else if (personalHistory.isEmpty()) {
            setErrorMessage("Los antecedentes personales son obligatorios, añadalos.");
            return;
        } else if (allergies.isEmpty()) {
            setErrorMessage("Las alergias son obligatorias, añadelo.");
            return;
        } else if (menarche.isEmpty()) {
            setErrorMessage("La menarquia es obligatoria, añadelo.");
            return;
        } else if (fur.isEmpty()) {
            setErrorMessage("FUR obligatoria, añadelo.");
            return;
        } else if (numberOfAbortions.isEmpty()) {
            setErrorMessage("Numero de abortos obligatorio, añadelo.");
            return;
        } else if (numberOfPregnancies.isEmpty()) {
            setErrorMessage("Numero de abortos embarazos, añadelo.");
            return;
        } else if (anamnesis.isEmpty()) {
            setErrorMessage("La anamnesis es obligatoria, añadelo.");
            return;
        } else if (gynecologicalExamination.isEmpty()) {
            setErrorMessage("La exploración ginecológica es obligatoria, añadelo.");
            return;
        } else if (riskFactors.isEmpty()) {
            setErrorMessage("Los factores de riesgo son obligatorios, añadelos.");
            return;
        } else if (diagnosis.isEmpty()) {
            setErrorMessage("El diágnostico es obligatorio, añadelo.");
            return;
        }
        HasComeAppointmentConfirmDialog hasComeAppointmentConfirmDialog = new HasComeAppointmentConfirmDialog(newsletterService,appointmentService, patient, appointmentEntity);
        hasComeAppointmentConfirmDialog.open();
        hasComeAppointmentConfirmDialog.addListener(HasComeAppointmentConfirmDialog.HasCome.class, this::hasComeAndConsolidateReport);

    }
    @Transactional
    private void hasComeAndConsolidateReport(HasComeAppointmentConfirmDialog.HasCome hasCome) {
        try {
            if (reportEntity == null) {
                reportEntity = new GynecologistReportEntity();
            }
            appointmentEntity.setHasAttended(hasCome.getHasCome());
            appointmentService.save(appointmentEntity);
            updatePatient();
            reportEntityBinder.writeBean(reportEntity);
            reportEntity.setState(ConstantUtilities.STATE_INACTIVE);
            reportEntity.setAppointmentEntity(appointmentEntity);
            reportService.save(reportEntity);
            PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
            patientsLogEntity.setDate(LocalDate.now());
            patientsLogEntity.setTime(LocalTime.now());
            patientsLogEntity.setMessage("Cita grabada");
            patientsLogEntity.setPatientEntity(patient);
            patientsLogEntity.setSanitaryEntity((SanitaryEntity) current.getCurrentUser());
            patientsLogEntity.setIp(getServerIp());
            patientsLogService.save(patientsLogEntity);
            UI.getCurrent().navigate(MyAppointmentsDayGridView.class); //Se dirige a la ventana anterior
            if(hasCome.getNewsletterEntities() != null){
             //   patient.getNewsletters().clear();
                patient.getNewsletters().addAll(hasCome.getNewsletterEntities());
                patientService.save(patient);
                patientService.merge(patient);
            }
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getLocalAddr();
    }

    /**
     * Establece el mensaje de error y lo muestra en la pantalla debajo.
     */
    private void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    private void downloadReport() {
        try {
            if (reportEntity == null) {
                reportEntity = new GynecologistReportEntity();
            }
            reportEntityBinder.writeBean(reportEntity);
            updatePatient();
            reportEntity.setState(ConstantUtilities.STATE_ACTIVE);
            reportEntity.setAppointmentEntity(appointmentEntity);
            reportService.save(reportEntity);
            patientService.merge(patient);
            StreamResource resource = new StreamResource("informe.pdf", () -> new GynecologistReportPdf(reportEntity).generatePdf());

            Anchor downloadLink = new Anchor(resource, "");
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
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }
    private void acceptButton() {
        try {
            if (reportEntity == null) {
                reportEntity = new GynecologistReportEntity();
            }
            reportEntityBinder.writeBean(reportEntity);
            updatePatient();
            reportEntity.setState(ConstantUtilities.STATE_ACTIVE);
            reportEntity.setAppointmentEntity(appointmentEntity);
            reportService.save(reportEntity);
            patientService.merge(patient);
            UI.getCurrent().navigate(MyAppointmentsDayGridView.class); //Se dirige a la ventana anterior
        } catch (ValidationException e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void updatePatient() {
        patient.setFamilyBackground(familyBackground.getValue());
        patient.setPersonalHistory(personalHistory.getValue());
        patient.setAllergies(allergies.getValue());
        if(menarche.getValue() == null){
            patient.setMenarche(null);
        }else{
            patient.setMenarche(menarche.getValue().intValue());
        }
        patient.setFur(fur.getValue());
        patient.setFm(fm.getValue());
        if(numberOfPregnancies.getValue() == null){
            patient.setNumberOfPregnancies(null);
        }else{
            patient.setNumberOfPregnancies(numberOfPregnancies.getValue().intValue());
        }
        if(numberOfAbortions.getValue() == null){
            patient.setNumberOfAbortions(null);
        }else{
            patient.setNumberOfAbortions(numberOfAbortions.getValue().intValue());
        }
        patient.setBloodType(bloodType.getValue());
        patient.setRh(rh.getValue());
    }

    private FormLayout getFormLayout(H4 titleGynecologicalHistory, H4 complementaryStudies) {

        FormLayout formLayout = new FormLayout(dateAppointment, age, reasonForConsultation,
                familyBackground, personalHistory, allergies, bloodType, rh, titleGynecologicalHistory,
                menarche, fm, fur, numberOfAbortions, numberOfPregnancies,
                anamnesis, gynecologicalExamination, complementaryStudies,
                cytology, ultrasound, amniocentesis, analytics, riskFactors, diagnosis,
                treatment, observations, subjectiveEvaluations, errorMessage);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        return formLayout;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String params) {
        if (params != null) {
            String[] paramsString = params.split(",");
            if (paramsString.length == 3) {
                patient = patientService.findByDni(paramsString[0]);
                UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(), patient, new H3("Rellenar informe"));
                header.getButton().addClickListener(buttonClickEvent ->
                        UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
                );
                if (!paramsString[1].equalsIgnoreCase("0")) {
                    if(reportService.findById(Long.parseLong(paramsString[1])).isPresent()){
                        reportEntity = reportService.findById(Long.parseLong(paramsString[1])).get();
                        reportEntityBinder.readBean(reportEntity);
                        reportEntityBinder.setBean(reportEntity);
                    }

                }
                if(appointmentService.findById(Long.parseLong(paramsString[2])).isPresent()){
                    appointmentEntity = appointmentService.findById(Long.parseLong(paramsString[2])).get();
                }
                fillInPastData();
                createLayout(header);
            }
        }
    }
}
