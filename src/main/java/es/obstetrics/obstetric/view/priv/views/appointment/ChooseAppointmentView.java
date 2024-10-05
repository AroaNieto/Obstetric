package es.obstetrics.obstetric.view.priv.views.appointment;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.appointment.AppointmentConfirmDialog;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import es.obstetrics.obstetric.view.priv.views.myFolder.MyAppointments;
import es.obstetrics.obstetric.view.priv.views.users.PatientsGridView;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

@Route(value = "workers/patientsAppointment/choose-appointment/choose-day", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class ChooseAppointmentView extends Div implements HasUrlParameter<String> {

    private final PatientService patientService;
    private final DiaryService diaryService;
    private final AppointmentService appointmentService;
    private final InsuranceService insuranceService;
    private final AppointmentTypeService appointmentTypeService;
    private final UserCurrent current;

    @Autowired
    public ChooseAppointmentView(AppointmentTypeService appointmentTypeService,
                                 InsuranceService insuranceService, UserCurrent current,
                                 PatientService patientService, AppointmentService appointmentService,
                                 DiaryService diaryService) {

        this.current = current;
        this.appointmentTypeService = appointmentTypeService;
        this.insuranceService = insuranceService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.diaryService = diaryService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        String[] params = s.split(",");
        if (params.length == 3) {
            Optional<DiaryEntity> diaryEntity = diaryService.findById(Long.parseLong(params[2]));
            UserHeaderTemplate header;
            CalendarAppointment calendarAppointment;
            if(current.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)){
                if(diaryEntity.isPresent()){
                    header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(), new UserEntity(),new H3("Elegir día"));
                    header.getButton().addClickListener(buttonClickEvent ->
                            UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
                    );
                    calendarAppointment = new CalendarAppointment(LocalDate.parse(params[1]),
                            diaryEntity.get(), (PatientEntity) current.getCurrentUser(), appointmentService,
                            insuranceService,
                            appointmentTypeService.findAll());
                    calendarAppointment.addListener(CalendarAppointment.ConfirmEvent.class, this::saveAppointment);
                    add(header, calendarAppointment);
                }
            }else if(Long.parseLong(params[0]) != 0){

                if(diaryEntity.isPresent()){
                    Optional<PatientEntity> patientEntity = patientService.findById(Long.parseLong(params[0]));
                    if(patientEntity.isPresent()){
                        header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(),patientEntity.get(),new H3("Elegir cita"));
                        header.getButton().addClickListener(buttonClickEvent ->
                                UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
                        );
                        calendarAppointment = new CalendarAppointment(LocalDate.parse(params[1]),
                                diaryEntity.get(),patientEntity.get(), appointmentService,
                                insuranceService,
                                appointmentTypeService.findAll());
                        calendarAppointment.addListener(CalendarAppointment.ConfirmEvent.class, this::saveAppointment);
                        add(header, calendarAppointment);
                    }
                }

            }else{
                if(diaryEntity.isPresent()){
                    calendarAppointment = new CalendarAppointment(LocalDate.parse(params[1]), diaryEntity.get(),
                            null,appointmentService,
                            insuranceService, appointmentTypeService.findAll());
                    calendarAppointment.addListener(CalendarAppointment.ConfirmEvent.class, this::saveAppointment);
                    header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(),new PatientEntity(), new H3("Mis citas"));
                    header.getButton().addClickListener(buttonClickEvent ->
                            UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
                    );
                    add(header, calendarAppointment);
                }
            }
        }
    }

    private void saveAppointment(CalendarAppointment.ConfirmEvent confirmEvent) {
        appointmentService.save(confirmEvent.getAppointmentEntity());
        AppointmentConfirmDialog appointmentConfirmDialog = new AppointmentConfirmDialog(confirmEvent.getAppointmentEntity());
        appointmentConfirmDialog.open();
        appointmentConfirmDialog.addListener(AppointmentConfirmDialog.ConfirmEvent.class, this::navigateToPatientGridView);
    }

    private void navigateToPatientGridView(AppointmentConfirmDialog.ConfirmEvent confirmEvent) {
        if(current.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)){
            UI.getCurrent().navigate(MyAppointments.class);
        }else{
            UI.getCurrent().navigate(PatientsGridView.class);
        }
    }
}
