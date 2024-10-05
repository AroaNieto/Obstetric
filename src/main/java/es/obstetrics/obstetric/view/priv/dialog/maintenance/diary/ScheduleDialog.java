package es.obstetrics.obstetric.view.priv.dialog.maintenance.diary;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleDialog extends MasterDialog {
    private final Binder<ScheduleEntity> scheduleEntityBinder;
    private final TextField maxPatients;
    private final TimePicker startTime;
    private final TimePicker endTime;
    private final DatePicker startDate;
    private final DatePicker endingDate;
    private final H5 errorMessage;
    private final ComboBox<SanitaryEntity> sanitaryEntityComboBox;
    private final ComboBox<CenterEntity> centerEntityComboBox;
    private final DiaryEntity diaryEntity;

    public ScheduleDialog(DiaryEntity diaryEntityText, ScheduleEntity scheduleEntity) {

        this.diaryEntity = diaryEntityText;

        maxPatients = new TextField("Maximo de pacientes");
        sanitaryEntityComboBox = new ComboBox<>("Paciente");
        centerEntityComboBox = new ComboBox<>("Centro");

        endTime = new TimePicker("Hora de finalización");
        startTime = new TimePicker("Hora de inicio");
        startDate = new DatePickerTemplate("Fecha de inicio");
        endingDate =  new DatePickerTemplate("Fecha de finalización");
        // Configurar para permitir solo ciertos días de la semana (por ejemplo, solo lunes y miércoles)

        errorMessage = new H5("");
        scheduleEntityBinder = new BeanValidationBinder<>(ScheduleEntity.class);
        scheduleEntityBinder.bindInstanceFields(this);
        createHeaderDialog();
        createDialogLayout();
        setSchedule(diaryEntityText, scheduleEntity);
    }

    private void setSchedule(DiaryEntity diaryEntityText, ScheduleEntity scheduleEntity) {
        if(scheduleEntity == null){
            startDate.setMin(LocalDate.now());
            endingDate.setMin(LocalDate.now());
        }
        sanitaryEntityComboBox.setItems(diaryEntityText.getSanitaryEntity());
        sanitaryEntityComboBox.setValue(diaryEntityText.getSanitaryEntity());
        sanitaryEntityComboBox.setReadOnly(true);
        centerEntityComboBox.setItems(diaryEntityText.getCenterEntity());
        centerEntityComboBox.setValue(diaryEntityText.getCenterEntity());
        centerEntityComboBox.setReadOnly(true);
        scheduleEntityBinder.readBean(scheduleEntity);

        scheduleEntityBinder.setBean(scheduleEntity);  //Recojo la categoria
    }

    @Override
    public void createDialogLayout() {
        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");

        errorMessage.addClassName("label-error");

        HorizontalLayout diaryAndMaxPatientsHl = new HorizontalLayout(centerEntityComboBox, sanitaryEntityComboBox, maxPatients);
        diaryAndMaxPatientsHl.setSizeFull();
        diaryAndMaxPatientsHl.expand(centerEntityComboBox, sanitaryEntityComboBox, maxPatients);


        startTime.addValueChangeListener(event -> {
            if (endTime.getValue() != null && startTime.getValue() != null) {
                endTime.setMin(startTime.getValue().plusHours(2));
                clearEndTime();
            }
        });

        endTime.addValueChangeListener(event -> {
            if (startTime.getValue() != null && endTime.getValue() != null) {
                clearEndTime();
            }
        });
        startTime.setStep(Duration.ofMinutes(30));
        startTime.setValue(LocalTime.of(12, 30));
        endTime.setStep(Duration.ofMinutes(30));
        endTime.setValue(LocalTime.of(12, 30));

        startDate.addValueChangeListener(event -> {
            if (startDate.getValue() != null && endingDate.getValue() != null) {
                clearEndDate();
            }
        });

        endingDate.addValueChangeListener(event -> {
            if (startDate.getValue() != null && endingDate.getValue() != null) {
                clearEndDate();
            }
        });

        HorizontalLayout dateHl = new HorizontalLayout(startDate, endingDate);
        dateHl.setSizeFull();
        dateHl.expand(startDate, endingDate);
        HorizontalLayout timeHl = new HorizontalLayout(startTime, endTime);
        timeHl.setSizeFull();
        timeHl.expand(endTime, startTime);

        errorMessage.setText("");

        dialogVl.add(diaryAndMaxPatientsHl, dateHl, timeHl, errorMessage);
    }


    private void clearEndDate() {
        if (endingDate.getValue().isBefore(startDate.getValue())) {
            endingDate.clear();
        }
    }

    private void clearEndTime() {
        if (endTime.getValue().isBefore(startTime.getValue())) {
            endTime.clear();
        }
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     * al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.maintenance.appointment.DiaryGridView }
     * que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
    }

    @Override
    public void clickButton() {
        if (endTime.isEmpty() || startDate.isEmpty() || maxPatients.isEmpty() || startTime.isEmpty()) {
            setErrorMessage("No ha rellenado todos los campos.");
            return;
        } else if (!endingDate.isEmpty() && startDate.isEmpty()) {
            setErrorMessage("No ha rellenado todos los campos.");
            return;
        }
        try {
            if (scheduleEntityBinder.getBean() == null) { //Si se está añadiendo un nuevo horario
                ScheduleEntity scheduleEntity = new ScheduleEntity();

                scheduleEntityBinder.writeBean(scheduleEntity);
                scheduleEntity.setEndTime(endTime.getValue());
                scheduleEntity.setDiaryEntity(diaryEntity);
                scheduleEntity.setState(ConstantUtilities.STATE_ACTIVE);
                for (ScheduleEntity oneSchedule : diaryEntity.getSchedules()) {
                    if (makeCheckDates(oneSchedule)) { //Si hay superposición se muestra un mensaje de error y se sale
                        return;
                    }
                }
                fireNewEvent(scheduleEntity);

            } else {
                scheduleEntityBinder.writeBean(scheduleEntityBinder.getBean());

                for (ScheduleEntity oneSchedule : diaryEntity.getSchedules()) {
                    if (!scheduleEntityBinder.getBean().getId().equals(oneSchedule.getId())) { //Se comprueba que no sea el diario que se está editando
                        if(makeCheckDates(oneSchedule)){
                            return;
                        }
                    }
                }
                scheduleEntityBinder.getBean().setEndTime(endTime.getValue());
                fireNewEvent(scheduleEntityBinder.getBean());
            }
        } catch (ValidationException e) {
            setErrorMessage("Campos incorrectos, revíselos.");
        }

    }

    /**
     * Comprobación de que ya exista ese horario para esa agenda.
     *
     * @param oneSchedule Horario existente a comprobar.
     * @return true si hay solapamiento, false en caso contrario.
     */
    private boolean makeCheckDates(ScheduleEntity oneSchedule) {
        if(oneSchedule.getState().equals(ConstantUtilities.STATE_ACTIVE)){
            if (checkDateOverlap(oneSchedule)) {
                // Verificar el solapamiento de horas solo si las fechas se solapan
                if (checkTimeOverlap(oneSchedule)) {
                    setErrorMessage("Ya existe un horario en el centro " + oneSchedule.getDiaryEntity().getCenterEntity().getCenterName()
                            + " con el sanitario " + oneSchedule.getDiaryEntity().getSanitaryEntity().getName() + " " +
                            oneSchedule.getDiaryEntity().getSanitaryEntity().getLastName());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica si hay solapamiento de horas entre el horario existente y el nuevo.
     *
     * @param existingSchedule Horario existente a comprobar.
     * @return true si hay solapamiento de horas, false en caso contrario.
    */
    private boolean checkTimeOverlap(ScheduleEntity existingSchedule) {
        // Verificar si el rango de horas se solapa
        return (existingSchedule.getStartTime().isBefore(endTime.getValue()) || endTime.getValue() == null) &&
                (existingSchedule.getEndTime() == null || existingSchedule.getEndTime().isAfter(startTime.getValue()));
    }

    /**
     * Verifica si hay solapamiento de fechas entre el horario existente y el nuevo.
     *
     * @param existingSchedule Horario existente a comprobar.
     * @return true si hay solapamiento de fechas, false en caso contrario.
     */
    private boolean checkDateOverlap(ScheduleEntity existingSchedule) {

        // Verificar si el rango de fechas se solapa
        if ((endingDate.getValue() == null || existingSchedule.getStartDate().isBefore(endingDate.getValue())) &&
                (existingSchedule.getEndingDate() == null || existingSchedule.getEndingDate().isAfter(startDate.getValue()))) {
            return true;
        }else return (existingSchedule.getEndingDate() == null || startDate.getValue().isBefore(existingSchedule.getEndingDate())) &&
                (endingDate.getValue() == null || endingDate.getValue().isAfter(existingSchedule.getStartDate()));
    }


    private void fireNewEvent(ScheduleEntity scheduleEntity) {
        fireEvent(new SaveEvent(this, scheduleEntity));
        close();
        setErrorMessage("");
        clearTextField();
    }

    @Override
    public void clearTextField() {
        startDate.clear();
        endingDate.clear();
        startTime.clear();
        endTime.clear();
        maxPatients.clear();
        centerEntityComboBox.clear();
        sanitaryEntityComboBox.clear();
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
     * Clase abstracta que extiene de {@link ScheduleDialog}, evento ocurrido en dicha clase.
     * Almacena el horario asociada al evento.
     */
    @Getter
    public static abstract class ScheduleFormEvent extends ComponentEvent<ScheduleDialog> {
        private final ScheduleEntity scheduleEntity; //Horario con ej que se trabaja

        protected ScheduleFormEvent(ScheduleDialog source,
                                    ScheduleEntity scheduleEntity) {
            super(source, false);
            this.scheduleEntity = scheduleEntity;
        }
    }

    /**
     * Clase heredada de ScheduleFormEvent, representa un evento de guardado que ocurre en el diálogo del horario.
     */
    public static class SaveEvent extends ScheduleFormEvent {
        SaveEvent(ScheduleDialog source, ScheduleEntity scheduleEntity) {
            super(source, scheduleEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que maneajrá el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
