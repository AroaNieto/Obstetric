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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.MatronReportService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.report.HasComeAppointmentConfirmDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.appointment.MyAppointmentsDayGridView;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Clase encargada de mostrar las citas del día actual a la matrona.
 */
@Route(value = "sanitary/appointments-day-matrone", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class MatronReportView extends Div implements HasUrlParameter<String> {
    private final TextArea familyBackground;
    private final TextArea personalHistory;
    private final TextArea currentSituation; //Situaición actual
    private final TextArea fetalHeartbeat; //Latido del feto
    private final TextArea painControl; //Control del dolor
    private final TextArea recommendations;
    private final NumberField menarche;
    private final NumberField numberOfPregnancies;
    private final NumberField numberOfAbortions;
    private final TextArea allergies;
    private final DatePickerTemplate fur;
    private final DatePickerTemplate date;
    private final ComboBox<String> rh;
    private final ComboBox<String> bloodType;
    private final TextArea reasonForConsultation;
    private final TextArea subjectiveEvaluations;
    private final TextArea observations;
    private final TextArea fm;
    private final NumberField age;
    private final Binder<MatronReportEntity> reportEntityBinder;
    private PatientEntity patient;
    private final PatientService patientService;
    private MatronReportEntity reportEntity = null;
    private final MatronReportService reportService;
    private final AppointmentService appointmentService;
    private AppointmentEntity appointmentEntity;
    private final H5 errorMessage;
    private final NewsletterService newsletterService;

    @Autowired
    public MatronReportView(PatientService patientService,
                            NewsletterService newsletterService,
                            MatronReportService reportService,
                            UserCurrent current,
                            AppointmentService appointmentService) {
        this.newsletterService = newsletterService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.reportService = reportService;

        date = new DatePickerTemplate("Fecha de consulta");
        date.setReadOnly(true);
        date.setWidth("300px");
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
        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        menarche = createNumberField("Menarquia", "300px");
        menarche.addClassName("text-margin");
        menarche.setWidth("150px");
        fm = createTextField("FM", "150px");
        fur = new DatePickerTemplate("FUR");
        fur.addClassName("text-margin");
        fur.setWidth("300px");
        numberOfPregnancies = createNumberField("Número de embarazos", "150px");
        numberOfAbortions = createNumberField("Número de abortos", "150px");
        currentSituation = createTextField("Situación actual", "600px");
        fetalHeartbeat = createTextField("Latido del feto", "150px");
        painControl = createTextField("Control del dolor", "150");
        recommendations = createTextField("Recomendaciones", "800px");
        observations = createTextField("Observaciones", "800px");
        subjectiveEvaluations = createTextField("Evaluaciones subjetivas", "600px");

        reportEntityBinder = new Binder<>(MatronReportEntity.class);
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

    /**
     * Establece el mensaje de error y lo muestra en la pantalla debajo.
     */
    private void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    private void fillInPastData() {
        bloodType.setItems(ConstantUtilities.BLOOD_A, ConstantUtilities.BLOOD_O, ConstantUtilities.BLOOD_AB, ConstantUtilities.BLOOD_B);
        rh.setItems(ConstantUtilities.RHMA, ConstantUtilities.RHMI);
        date.setValue(appointmentEntity.getDate());
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

        formLayout.setColspan(reasonForConsultation, 2);
        formLayout.setColspan(familyBackground, 2);
        formLayout.setColspan(personalHistory, 2);
        formLayout.setColspan(allergies, 2);
        formLayout.setColspan(titleGynecologicalHistory, 2);
        formLayout.setColspan(complementaryStudies, 2);
        formLayout.setColspan(recommendations, 2);
        formLayout.setColspan(observations, 2);
        formLayout.setColspan(subjectiveEvaluations, 2);

        VerticalLayout verticalLayout = new VerticalLayout(new H3("INFORME CLÍNICO"), formLayout);
        verticalLayout.setPadding(true);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, formLayout);
        verticalLayout.addClassName("container-report");
        Button acceptButton = new Button("Guardar");
        acceptButton.setTooltipText("Guardar");
        acceptButton.addClickListener(event -> acceptButton());
        acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button printButton = new Button("Descargar", VaadinIcon.PRINT.create());
        printButton.setTooltipText("Descargar");
        printButton.addClassName("dark-gray-color-button");
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
        } else if (currentSituation.isEmpty()) {
            setErrorMessage("La anamnesis es obligatoria, añadelo.");
            return;
        } else if (painControl.isEmpty()) {
            setErrorMessage("Los factores de riesgo son obligatorios, añadelos.");
            return;
        }
        HasComeAppointmentConfirmDialog hasComeAppointmentConfirmDialog = new HasComeAppointmentConfirmDialog(newsletterService, appointmentService, patient, appointmentEntity);
        hasComeAppointmentConfirmDialog.open();
        hasComeAppointmentConfirmDialog.addListener(HasComeAppointmentConfirmDialog.HasCome.class, this::hasComeAndConsolidateReport);

    }

    private void hasComeAndConsolidateReport(HasComeAppointmentConfirmDialog.HasCome hasCome) {
        try {
            if (reportEntity == null) {
                reportEntity = new MatronReportEntity();
            }
            appointmentEntity.setHasAttended(hasCome.getHasCome());
            if(hasCome.getNewsletterEntities() != null){
                patient.getNewsletters().addAll(hasCome.getNewsletterEntities());
            }
            appointmentService.save(appointmentEntity);

            reportEntityBinder.writeBean(reportEntity);
            updatePatient();
            reportEntity.setState(ConstantUtilities.STATE_INACTIVE);
            reportEntity.setAppointmentEntity(appointmentEntity);
            reportService.save(reportEntity);
            patientService.save(patient);
            UI.getCurrent().navigate(MyAppointmentsDayGridView.class); //Se dirige a la ventana anterior
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptButton() {
        try {
            if (reportEntity == null) {
                reportEntity = new MatronReportEntity();
            }
            reportEntityBinder.writeBean(reportEntity);
            updatePatient();
            reportEntity.setState(ConstantUtilities.STATE_ACTIVE);
            reportEntity.setAppointmentEntity(appointmentEntity);
            reportService.save(reportEntity);

            appointmentEntity.setReportEntity(reportEntity);
            appointmentService.save(appointmentEntity);

            patientService.save(patient);
            UI.getCurrent().navigate(MyAppointmentsDayGridView.class); //Se dirige a la ventana anterior
        } catch (
                ValidationException e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void updatePatient() {
        patient.setFamilyBackground(familyBackground.getValue());
        patient.setPersonalHistory(personalHistory.getValue());
        patient.setAllergies(allergies.getValue());
        if (menarche.getValue() == null) {
            patient.setMenarche(null);
        } else {
            patient.setMenarche(menarche.getValue().intValue());
        }
        patient.setFur(fur.getValue());
        patient.setFm(fm.getValue());
        if (numberOfPregnancies.getValue() == null) {
            patient.setNumberOfPregnancies(null);
        } else {
            patient.setNumberOfPregnancies(numberOfPregnancies.getValue().intValue());
        }
        if (numberOfAbortions.getValue() == null) {
            patient.setNumberOfAbortions(null);
        } else {
            patient.setNumberOfAbortions(numberOfAbortions.getValue().intValue());
        }
        patient.setBloodType(bloodType.getValue());
        patient.setRh(rh.getValue());
    }

    private FormLayout getFormLayout(H4 titleGynecologicalHistory, H4 complementaryStudies) {

        FormLayout formLayout = new FormLayout(date, age, reasonForConsultation,
                familyBackground, personalHistory, allergies, bloodType, rh, titleGynecologicalHistory,
                menarche, fm, fur, numberOfAbortions, numberOfPregnancies,
                complementaryStudies, currentSituation, fetalHeartbeat, painControl,
                recommendations, observations, subjectiveEvaluations, errorMessage);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        return formLayout;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String params) {
        if (params == null) {
            // forgotPassFl.add("error");
        }else {
            String[] paramsString = params.split(",");
            if (paramsString.length == 3) {
                patient = patientService.findByDni(paramsString[0]);
                UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(), patient, new H3("Rellenar informe"));
                header.getButton().addClickListener(buttonClickEvent ->
                        UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
                );
                if (!paramsString[1].equalsIgnoreCase("0")) {
                    reportEntity = reportService.findById(Long.parseLong(paramsString[1])).get();
                    reportEntityBinder.readBean(reportEntity);
                    reportEntityBinder.setBean(reportEntity);
                }
                appointmentEntity = appointmentService.findById(Long.parseLong(paramsString[2])).get();
                fillInPastData();
                createLayout(header);
            }
        }
    }
}
