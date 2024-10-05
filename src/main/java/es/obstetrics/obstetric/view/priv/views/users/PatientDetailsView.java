package es.obstetrics.obstetric.view.priv.views.users;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.PrenancyEntityFinishConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.PregnancyDialog;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.appointment.ChooseAppointmentCharacteristicsView;
import es.obstetrics.obstetric.view.priv.views.myFolder.MyFolder;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Route(value = "workers/patients/patient", layout = PrincipalView.class)
@PageTitle("MotherBloom-patient")
@PermitAll
public class PatientDetailsView extends Div implements HasUrlParameter<String> {

    private PatientEntity user;
    private final PatientService patientService;
    private final PregnanceService pregnanceService;
    private Button addPregnantBtn;
    private Button deletePregnantBtn;
    private final ReportService reportService;
    private final UserCurrent userCurrent;
    private final NewsletterService newsletterService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PatientsLogService patientsLogService;

    @Autowired
    public PatientDetailsView(UserCurrent userCurrent,
                              PatientService patientService,
                              PatientsLogService patientsLogService,
                              PregnanceService pregnanceService,
                              ReportService reportService,
                              AppointmentService appointmentService,
                              NewsletterService newsletterService,
                              UserService userService) {

        this.appointmentService = appointmentService;
        this.patientsLogService = patientsLogService;
        this.userCurrent = userCurrent;
        this.pregnanceService = pregnanceService;
        this.patientService = patientService;
        this.userService = userService;
        this.reportService = reportService;
        this.newsletterService = newsletterService;
    }

    private void savePregnant(PregnancyDialog.SaveEvent saveEvent) {
        pregnanceService.save(saveEvent.getPregnance());
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setPatientEntity(saveEvent.getPregnance().getPatientEntity());
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) userCurrent.getCurrentUser());
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Embarazo registrado");
        // Obtener la IP del servidor
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        addPregnantBtn.setVisible(false);
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getLocalAddr();
    }

    private void createButtonPregnancy() {
        addPregnantBtn = createButton("Registrar embarazo","dark-green-button");
        addPregnantBtn.addClickListener(buttonClickEvent -> {
            PregnanceEntity pregnancy = new PregnanceEntity();
            pregnancy.setPatientEntity(user);
            PregnancyDialog pregnancyDialog = new PregnancyDialog(pregnancy);
            pregnancyDialog.addListener(PregnancyDialog.SaveEvent.class, this::savePregnant);
            pregnancyDialog.setHeaderTitle("Registrar embarazo");
            pregnancyDialog.open();
        });
    }

    private void createButtonFinishPregnancy() {
        deletePregnantBtn = createButton("Finalizar embarazo","dark-green-button");
        deletePregnantBtn.addClickListener(buttonClickEvent -> {
            PrenancyEntityFinishConfirmDialog pregnancy = new PrenancyEntityFinishConfirmDialog(user);
            pregnancy.addListener(PrenancyEntityFinishConfirmDialog.FinishEvent.class, this::finishPregnancy);
            pregnancy.open();
        });
    }

    private void finishPregnancy(PrenancyEntityFinishConfirmDialog.FinishEvent finishEvent) {
        PregnanceEntity pregnanceEntity = new PregnanceEntity();
        for (PregnanceEntity pregnance : user.getPregnancies()) {
            if (pregnance.getEndingDate() == null) { //Buscar el embarazo activo
                pregnanceEntity = pregnance;
                break;
            }
        }
        pregnanceEntity.setEndingDate(LocalDate.now());
        pregnanceEntity.setState(ConstantUtilities.STATE_INACTIVE);
        pregnanceService.save(pregnanceEntity);
        deletePregnantBtn.setVisible(false);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setPatientEntity(user);
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) userCurrent.getCurrentUser());
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Embarazo registrado");
        patientsLogService.save(patientsLogEntity);

    }

    private Button createButton(String label, String className){
        Button button = new Button(label);
        if(className != null){
            button.addClassName(className);
        }
        return button;
    }

    private int isPregnancyBoolean(){
        //Se comprueba que aunque no tenga embarazos activos, han pasado 18 semanas para volver a quedarse embarazada
        if(!userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_SECRETARY)){
            for (PregnanceEntity pregnancy : user.getPregnancies()) {
                if (pregnancy.getEndingDate() != null && pregnancy.getEndingDate().plusWeeks(18).isBefore(pregnancy.getEndingDate())) {
                    return  0;
                } else if (pregnancy.getEndingDate() == null) { //Aún está embarazada
                    return 1;
                }
            }
        }

        return 3;
    }
    private HorizontalLayout createActionButtons() {
        int isPregnance = 3;
        if(!user.getState().equals(ConstantUtilities.STATE_DISCHARGED)){ //Solo se podrá realizar acciones si el usario no está dado de baja
            Button requestAppointmentButton = createButton("Pedir cita",null);
            requestAppointmentButton.addClickListener(event ->  UI.getCurrent().navigate(ChooseAppointmentCharacteristicsView.class, user.getDni()));
            requestAppointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            if (Utilities.isPregnantActive(user.getPregnancies()) != null) { //Ha tenido al menos un embarazo
                isPregnance = isPregnancyBoolean();
            }

            HorizontalLayout buttonsLayout;
            if(isPregnance == 0|| isPregnance==3){
                createButtonPregnancy();
                buttonsLayout = new HorizontalLayout(requestAppointmentButton,addPregnantBtn);
            }else if(isPregnance == 1) {
                createButtonFinishPregnancy();
                buttonsLayout = new HorizontalLayout(requestAppointmentButton,deletePregnantBtn);
            }else{
                buttonsLayout = new HorizontalLayout(requestAppointmentButton);
            }
            buttonsLayout.setSizeFull();
            buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            buttonsLayout.setPadding(true);
            return buttonsLayout;
        }
      return new HorizontalLayout();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String dni) {
        if (dni != null) {
            user = patientService.findByDniAndRole(dni);
            UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(),user, new H3("Consultar usuario"));
            header.getButton().addClickListener(buttonClickEvent ->
                    UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
            );

            add(header,
                    createActionButtons(),
                    new MyFolder(newsletterService,
                            reportService,
                            appointmentService,
                            user,
                            userCurrent,
                            userService));

        }
    }
}