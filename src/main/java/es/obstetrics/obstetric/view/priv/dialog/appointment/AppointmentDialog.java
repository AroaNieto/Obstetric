package es.obstetrics.obstetric.view.priv.dialog.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.InsuranceService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 * que el usuario añada los datos de la nueva cita.
 */
public class AppointmentDialog extends MasterDialog {

    private Binder<AppointmentEntity> appointmentEntityBinder;
    private RadioButtonGroup<String> notice;
    private RadioButtonGroup<String> reminder;
    private TextField insurancePolice;
    private ComboBox<InsuranceEntity> insuranceEntity;
    private ComboBox<AppointmentTypeEntity> appointmentTypeEntity;
    private H5 errorMessage;
    private LocalTime endTime;
    private final PatientEntity patientEntity;
    private int timeDate;
    private final LocalDate date;
    private final ScheduleEntity scheduleEntity;
    private final InsuranceService insuranceService;
    private final List<AppointmentTypeEntity> appointmentTypeEntities;
    private final  AppointmentService appointmentService;
    private boolean isError = false;
    public AppointmentDialog(ScheduleEntity schedule, InsuranceService insuranceService,
                             PatientEntity patientEntity, LocalDate date,
                             List<AppointmentTypeEntity> appointmentTypeEntities,
                             AppointmentService appointmentService) {
        this.appointmentTypeEntities = appointmentTypeEntities;
        this.appointmentService = appointmentService;
        this.scheduleEntity = schedule;
        this.insuranceService = insuranceService;
        this.patientEntity = patientEntity;
        this.date = date;
        if(patientEntity == null){
            Grid<AppointmentEntity> grid = createGrid();
            //Busco las citas asociadas al sanitario el día de hoy.
            List<AppointmentEntity> appointmentEntities = appointmentService.findByDateAndState(date, ConstantUtilities.STATE_ACTIVE);
            List<AppointmentEntity> sanitaryAppointments = new ArrayList<>();
            for(AppointmentEntity oneAppointment: appointmentEntities){
                if(schedule.getDiaryEntity().getSanitaryEntity().getId().equals(oneAppointment.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getId())
                && oneAppointment.getScheduleEntity().getStartTime().equals(schedule.getStartTime())){
                    sanitaryAppointments.add(oneAppointment);
                }
            }
            grid.setItems(sanitaryAppointments);
            isError=true;
            dialogVl.add(grid);
            button.setText("De acuerdo");
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el ancho completo
            dialogVl.getStyle().set("width", "60rem")
                    .set("max-width", "100%");
        }else{
            endTime = setEndTimeDate(schedule, patientEntity,date);

            if (endTime != null) {
                //Calculo del tiempo por cada cita, dividiendo el número máximo de pacientes entre la fecha de inicio y de fin del horario
                timeDate = setTimeDate(setDuration(schedule.getStartTime(), schedule.getEndTime()), schedule.getMaxPatients());
                insurancePolice = new TextField("Poliza de seguro");
                insurancePolice.setReadOnly(true);
                errorMessage = new H5("");
                notice = new RadioButtonGroup<>("Notificar la cita por correo");
                notice.setItems(ConstantUtilities.RESPONSE_YES, ConstantUtilities.RESPONSE_NO);
                reminder = new RadioButtonGroup<>("Recordar la cita");
                reminder.setItems(ConstantUtilities.RESPONSE_YES, ConstantUtilities.RESPONSE_NO);
                appointmentTypeEntity = new ComboBox<>("Tipo de cita");
                insuranceEntity = new ComboBox<>("Aseguradora");
                appointmentEntityBinder = new BeanValidationBinder<>(AppointmentEntity.class);
                appointmentEntityBinder.bindInstanceFields(this);
                setAppointment();
                createHeaderDialog();
                createDialogLayout();

            } else {
                setHeaderAndTextError(patientEntity, date, schedule);
            }
        }
    }

    private Grid<AppointmentEntity> createGrid() {
        Grid<AppointmentEntity> grid = new Grid<>(AppointmentEntity.class,false);
        grid.addColumn(createPatientRenderer()).setHeader("Paciente").setAutoWidth(true).setSortable(true);
        grid.addColumn(appointmentEntity -> {
            PregnanceEntity pregnance = null;
            if (appointmentEntity.getPatientEntity().getPregnancies() != null) {
                for (int i = 0; i< appointmentEntity.getPatientEntity().getPregnancies().size(); i++) {
                    if (appointmentEntity.getPatientEntity().getPregnancies().get(i).getEndingDate() == null) {
                        pregnance = appointmentEntity.getPatientEntity().getPregnancies().get(i);
                        break;
                    }
                }
                if(pregnance != null){
                    return Utilities.quarterCalculator(pregnance.getLastPeriodDate().toEpochDay()); //Calculo de la semana en la que se encuentra el embara
                }
            }
            return null;
        }).setHeader("Semana de embarazo").setAutoWidth(true).setSortable(true);
        grid.addColumn(AppointmentEntity::getStartTime).setHeader("Hora de inicio").setAutoWidth(true).setSortable(true);
        grid.addColumn(AppointmentEntity::getEndTime).setHeader("Hora de finalización").setAutoWidth(true).setSortable(true);
        grid.addColumn(AppointmentEntity::getAppointmentTypeEntity).setHeader("Tipo de cita").setAutoWidth(true).setSortable(true);
        grid.addColumn(AppointmentEntity::getHasAttended).setHeader("Atendido").setAutoWidth(true).setSortable(true);
        grid.addColumn(AppointmentEntity::getInsuranceEntity).setHeader("Aseguradora").setAutoWidth(true).setSortable(true);
        grid.addColumn(appointmentEntity -> appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity()).setHeader("Centro").setAutoWidth(true).setSortable(true);
        return grid;
    }
    private static Renderer<AppointmentEntity> createPatientRenderer() {
        return LitRenderer.<AppointmentEntity> of(
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
    private void setAppointment() {
        clearTextField();
    }

    private void setHeaderAndTextError(PatientEntity patientEntity, LocalDate date, ScheduleEntity schedule) {
        H3 titleError = new H3("No se puede crear la cita");
        H5 descriptionError = new H5("El paciente "
                + patientEntity.getName() + " " + patientEntity.getLastName() +
                " ya tíene una cita el día " + date + " con el sanitario "
                + schedule.getDiaryEntity().getSanitaryEntity().getName() + " " +
                schedule.getDiaryEntity().getSanitaryEntity().getLastName() +".");
        dialogVl.add(titleError, descriptionError);
        button.setText("De acuerdo");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        isError = true;

    }


    private int setTimeDate(long durationInMinutes, String maxPatients) {
        return Integer.parseInt(String.valueOf(durationInMinutes)) / Integer.parseInt(maxPatients);
    }


    private long setDuration(LocalTime startTime, LocalTime endTime) {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Método para escoger la hora de la cita.
     * @param schedule Horario asociado.
     * @param patientEntity Paciente al que se va a dar la cita
     * @param date Día de la cita
     * @return El miniuto de comienzo de la cita (que coincide con el de fin de la última o con el de inicio de la inactiva)
     */
    private LocalTime setEndTimeDate(ScheduleEntity schedule, PatientEntity patientEntity, LocalDate date) {
        LocalTime endTime = schedule.getStartTime();
        List<LocalTime> inactiveStartTimes = new ArrayList<>();

        for (AppointmentEntity appointment : schedule.getAppointmentEntities()) {
            //Si ya existe una cita activa para el paciente en la fecha, no puede asignarse otra
            if (appointment.getPatientEntity().getId().equals(patientEntity.getId()) && date.equals(appointment.getDate()) && appointment.getState().equals(ConstantUtilities.STATE_ACTIVE)) {
                return null;
            }
            //Se recoge la última cita activa y se va añadiendo todas las citas inactivas
            if (appointment.getState().equals(ConstantUtilities.STATE_ACTIVE) && appointment.getEndTime() != null) {
                if (endTime.isBefore(appointment.getEndTime())) {
                    endTime = appointment.getEndTime();
                }
            } else if (appointment.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                inactiveStartTimes.add(appointment.getStartTime());
            }
        }
        //Se comprueba si se puede asignar una nueva cita a una que estaba inactiva para completar las horas del horario
        if (!inactiveStartTimes.isEmpty()) {
            for (LocalTime inactiveStartTime : inactiveStartTimes) {
                boolean isOverlapping = false;
                for (AppointmentEntity appointment : schedule.getAppointmentEntities()) {
                    //Si existe una cita activa dada para ese horario que completa las horas de la cita inactiva, finaliza el bucle
                    if (appointment.getState().equals(ConstantUtilities.STATE_ACTIVE) && appointment.getStartTime() != null && inactiveStartTime.equals(appointment.getStartTime())) {
                        isOverlapping = true;
                        break;
                    }
                }
                if (!isOverlapping) {
                    return inactiveStartTime;
                }
            }
        }

        return endTime;
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Confirmar cita");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Cierre del cuadro de diálogo,.
     */
    @Override
    public void closeDialog() {
        close();
        if(patientEntity != null){
            clearTextField();
            setErrorMessage("");
        }

    }

    /**
     * Dispara el evento para notificar a la clase {@link  }
     * que quiere continuar con el proceso de cita.
     */
    @Override
    public void clickButton() {
        if(!isError){
            if (notice.isEmpty() || reminder.isEmpty() || appointmentTypeEntity.isEmpty()) {
                setErrorMessage("Debe rellenar todos los campos obligatorios.");
                return;
            }
            if(insuranceEntity.isEmpty() && !insurancePolice.isEmpty()){
                setErrorMessage("Debe rellenar todos los campos obligatorios.");
                return;
            }
            if(!insuranceEntity.isEmpty() && insurancePolice.isEmpty()){
                setErrorMessage("Debe rellenar todos los campos obligatorios.");
                return;
            }
            AppointmentEntity appointmentEntity = new AppointmentEntity();

            try {
                appointmentEntityBinder.writeBean(appointmentEntity);
                appointmentEntity.setStartTime(endTime);
                appointmentEntity.setEndTime(endTime.plusMinutes(timeDate));
                appointmentEntity.setTime(timeDate);
                appointmentEntity.setDate(date);
                appointmentEntity.setState(ConstantUtilities.STATE_ACTIVE);
                appointmentEntity.setPatientEntity(patientEntity);
                appointmentEntity.setScheduleEntity(scheduleEntity);
                if (notice.getValue().equals(ConstantUtilities.RESPONSE_YES)) {
                    appointmentEntity.setNotice(ConstantUtilities.RESPONSE_YES);
                } else if (reminder.getValue().equals(ConstantUtilities.RESPONSE_NO)) {
                    appointmentEntity.setNotice(ConstantUtilities.RESPONSE_NO);
                }
                if (reminder.getValue().equals(ConstantUtilities.RESPONSE_YES)) {
                    appointmentEntity.setReminder(ConstantUtilities.RESPONSE_YES);
                } else if (reminder.getValue().equals(ConstantUtilities.RESPONSE_NO)) {
                    appointmentEntity.setReminder(ConstantUtilities.RESPONSE_NO);
                }
            } catch (ValidationException e) {
                setErrorMessage("Campos incorrectos, revíselos.");
                return;
            }

            fireEvent(new ConfirmEvent(this, appointmentEntity));
        }
        closeDialog();

    }

    @Override
    public void createDialogLayout() {
        insuranceEntity.setItems(setInsuranceItems());
        appointmentTypeEntity.setItems(appointmentTypeEntities);

        HorizontalLayout insuranceHl = new HorizontalLayout(insuranceEntity, insurancePolice);
        insuranceHl.setSizeFull();
        insuranceHl.expand(insuranceEntity, insurancePolice);
        insuranceEntity.addValueChangeListener(event -> {
            if(event.getValue() == null){
                insurancePolice.clear();
                insurancePolice.setReadOnly(true);
            }else{
                LocalDate localDate = LocalDate.MIN;
                String insurancePoliceString = null; //Se comprueba la ultima poliza de seguro y se muestra por defecto en la pantalla
                List<AppointmentEntity> appointmentPatient = appointmentService.findByPatientEntity(patientEntity);
                for(AppointmentEntity appointment: appointmentPatient){
                    if(appointment.getPatientEntity().getId().equals(patientEntity.getId()) && localDate.isBefore(appointment.getDate())){
                        localDate = appointment.getDate();
                        if(appointment.getInsurancePolice() != null && appointment.getInsuranceEntity() != null && appointment.getInsuranceEntity().getName().equals(insuranceEntity.getValue().getName())){
                            insurancePoliceString = appointment.getInsurancePolice();
                        }
                    }
                }
                if(insurancePoliceString != null){
                    insurancePolice.setValue(insurancePoliceString);
                }
                insurancePolice.setReadOnly(false);
            }
        });
        HorizontalLayout noticeAndReminderHl = new HorizontalLayout(notice, reminder);
        noticeAndReminderHl.setSizeFull();
        noticeAndReminderHl.expand(notice, reminder);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el ancho completo
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(insuranceHl, appointmentTypeEntity,noticeAndReminderHl, errorMessage);
    }

    public List<InsuranceEntity> setInsuranceItems(){

        return insuranceService.findByCenterEntity(scheduleEntity.getDiaryEntity().getCenterEntity());
    }

    @Override
    public void clearTextField() {
        if(notice.getValue()  != null){
            notice.clear();
        }
        reminder.clear();
        appointmentTypeEntity.clear();
        insuranceEntity.clear();
        insurancePolice.clear();
        appointmentTypeEntity.clear();
        errorMessage.setText("");
    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Clase abstracta que extiene de {@link AppointmentDialog}, evento ocurrido en dicha clase.
     */
    public static abstract class AppointmentPatientDialogFormEvent extends ComponentEvent<AppointmentDialog> {
        private final AppointmentEntity appointmentEntity; //Comunidad con la que trabajamos

        protected AppointmentPatientDialogFormEvent(AppointmentDialog source, AppointmentEntity appointmentEntity) {
            super(source, false);
            this.appointmentEntity = appointmentEntity;
        }

        public AppointmentEntity getAppointment() {
            return appointmentEntity;
        }
    }

    /**
     * Clase heredada de AppointmentConfirmDialog, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static class ConfirmEvent extends AppointmentPatientDialogFormEvent {
        ConfirmEvent(AppointmentDialog source, AppointmentEntity appointmentEntity) {
            super(source, appointmentEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     *  @param listener  El listener que manejará. el evento.
     *  @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T> Clase
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
