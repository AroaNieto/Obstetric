package es.obstetrics.obstetric.view.priv.dialog.maintenance.diary;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.service.CenterService;
import es.obstetrics.obstetric.backend.service.DiaryService;
import es.obstetrics.obstetric.backend.service.SanitaryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import es.obstetrics.obstetric.view.priv.views.maintenance.appointment.DiaryGridView;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DiaryDialog extends MasterDialog {
    private final Binder<DiaryEntity> diaryEntityBinder;
    private final MultiSelectComboBox<String> days;
    private final DatePicker startTime;
    private final DatePicker endTime;
    private final H5 errorMessage;
    private final ComboBox<CenterEntity> centerEntity;
    private final TextField name;
    private final ComboBox<SanitaryEntity> sanitaryEntity;
    private final DiaryService diaryService;
    private final SanitaryService sanitaryService;
    private final CenterService centerService;
    private final List<SanitaryEntity> allSanitaries;
    private final List<CenterEntity> allCenters;

    public DiaryDialog(DiaryEntity diaryEntity, DiaryService diaryService,
                       List<SanitaryEntity> sanitaries, List<CenterEntity> centers,
                       CenterService centerService, SanitaryService sanitaryService) {
        this.allSanitaries = sanitaries;
        this.allCenters = centers;
        this.sanitaryService = sanitaryService;
        this.centerService = centerService;
        this.diaryService = diaryService;

        name = new TextField("Nombre descriptivo");
        days = new MultiSelectComboBox<>("Días en la agenda");
        centerEntity = new ComboBox<>("Centro asociado");
        sanitaryEntity = new ComboBox<>("Sanitario");
        endTime = new DatePickerTemplate("Fecha de finalización");
        startTime = new DatePickerTemplate("Fecha de inicio");
        errorMessage = new H5("");
        diaryEntityBinder = new BeanValidationBinder<>(DiaryEntity.class);
        diaryEntityBinder.bindInstanceFields(this);
        createHeaderDialog();
        createDialogLayout();
        setDiary(diaryEntity, sanitaries, centers);
    }

    private void setDiary(DiaryEntity diaryEntity, List<SanitaryEntity> sanitaries,
                          List<CenterEntity> centers) {
        days.setItems(ConstantUtilities.MONDAY, ConstantUtilities.TUESDAY,
                ConstantUtilities.WEDNESDAY, ConstantUtilities.THURSDAY,
                ConstantUtilities.FRIDAY, ConstantUtilities.SATURDAY,
                ConstantUtilities.SUNDAY);
        if (diaryEntity != null) {
            days.setValue(createDays(diaryEntity));
        } else {
            startTime.setMin(LocalDate.now());
        }
        sanitaryEntity.setItems(sanitaries);
        centerEntity.setItems(centers);

        diaryEntityBinder.setBean(diaryEntity);  //Recojo la categoria
        diaryEntityBinder.readBean(diaryEntity);
    }

    private List<String> createDays(DiaryEntity diaryEntity) {
        List<String> daysDiary = new ArrayList<>();
        if (diaryEntity.isMonday()) {
            daysDiary.add(ConstantUtilities.MONDAY);
        }
        if (diaryEntity.isTuesday()) {
            daysDiary.add(ConstantUtilities.TUESDAY);
        }
        if (diaryEntity.isWednesday()) {
            daysDiary.add(ConstantUtilities.WEDNESDAY);
        }
        if (diaryEntity.isThursday()) {
            daysDiary.add(ConstantUtilities.THURSDAY);
        }
        if (diaryEntity.isFriday()) {
            daysDiary.add(ConstantUtilities.FRIDAY);
        }
        if (diaryEntity.isSaturday()) {
            daysDiary.add(ConstantUtilities.SATURDAY);
        }
        if (diaryEntity.isSunday()) {
            daysDiary.add(ConstantUtilities.SUNDAY);
        }
        return daysDiary;
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
     * Dispara el evento para notificar a la clase {@link DiaryGridView }
     * que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
    }

    @Override
    public void clickButton() {
        if (sanitaryEntity.isEmpty() || centerEntity.isEmpty() || days.isEmpty() || startTime.isEmpty()) {
            setErrorMessage("No ha rellenado todos los campos obligatorios.");
            return;
        } else if (startTime.isEmpty() && !endTime.isEmpty()) {
            setErrorMessage("Debe rellenar al fecha de inicio de la agenda.");
            return;
        }

        List<DiaryEntity> diaryEntities = diaryService.findBySanitaryEntityAndCenterEntity(sanitaryEntity.getValue(), centerEntity.getValue());

        try {
            if (diaryEntityBinder.getBean() == null) { //Si se está añadiendo una nueva agenda
                DiaryEntity diaryEntity = getDiaryEntity();
                diaryEntityBinder.writeBean(diaryEntity);
                for (DiaryEntity oneDiary : diaryEntities) {
                    if (oneDiary.getState().equals(ConstantUtilities.STATE_ACTIVE)) {
                        if(makeCheck(oneDiary)){ //Si hay superposición se muestra un mensaje de error y se sale
                            return;
                        }
                    }

                }
                diaryEntity.setState(ConstantUtilities.STATE_ACTIVE);
                fireNewEvent(diaryEntity);

            } else {
                diaryEntityBinder.writeBean(diaryEntityBinder.getBean());

                for (DiaryEntity oneDiary : diaryEntities) {
                    if (oneDiary.getState().equals(ConstantUtilities.STATE_ACTIVE)) {
                        if(!diaryEntityBinder.getBean().getId().equals(oneDiary.getId())){ //Se comprueba que no sea el diario que se está editando
                            if(makeCheck(oneDiary)){ //Si hay superposición se muestra un mensaje de error y se sale
                                return;
                            }
                        }
                    }

                }
                diaryEntityBinder.getBean().setMonday(false);
                diaryEntityBinder.getBean().setTuesday(false);
                diaryEntityBinder.getBean().setWednesday(false);
                diaryEntityBinder.getBean().setThursday(false);
                diaryEntityBinder.getBean().setFriday(false);
                diaryEntityBinder.getBean().setSaturday(false);
                diaryEntityBinder.getBean().setSunday(false);
                for (String oneDay : days.getSelectedItems()) {
                    checkDays(oneDay);
                }
                diaryEntityBinder.getBean().setState(ConstantUtilities.STATE_ACTIVE);
                fireNewEvent(diaryEntityBinder.getBean());
            }
        } catch (ValidationException e) {
            setErrorMessage("Campos incorrectos, revíselos.");
        }
    }

    private void checkDays(String oneDay) {
        if (oneDay.equals(ConstantUtilities.MONDAY)) {
            diaryEntityBinder.getBean().setMonday(true);
        }
        if (oneDay.equals(ConstantUtilities.TUESDAY)) {
            diaryEntityBinder.getBean().setTuesday(true);
        }
        if (oneDay.equals(ConstantUtilities.WEDNESDAY)) {
            diaryEntityBinder.getBean().setWednesday(true);
        }
        if (oneDay.equals(ConstantUtilities.THURSDAY)) {
            diaryEntityBinder.getBean().setThursday(true);
        }
        if (oneDay.equals(ConstantUtilities.FRIDAY)) {
            diaryEntityBinder.getBean().setFriday(true);
        }
        if (oneDay.equals(ConstantUtilities.SATURDAY)) {
            diaryEntityBinder.getBean().setSaturday(true);
        }
        if (oneDay.equals(ConstantUtilities.SUNDAY)) {
            diaryEntityBinder.getBean().setSunday(true);
        }
    }

    /**
     * Verificación de superposición de agendas, se verifica:
     *  1. La agenda existente empieza antes o al mismo tiempo que el inicio de la nueva agenda y termina después de que el inicio de la nueva agenda
     *  2. La agenda existente empieza antes o al mismo tiempo que el final de la nueva agenda y termina después o al mismo tiempo que el final de la nueva agenda
     *  3. El inicio de la nueva agenda empieza antes o al mismo tiempo que la agenda existente y termina después de que la agenda existente haya terminado
     *  4. El inicio de la nueva agenda empieza antes o al mismo tiempo que la agenda existente y la agenda existente termina después de el final de la nueva agenda
     *  5. La fecha de finalización de la agenda existente es nula y la agenda existente empieza antes o al mismo tiempo que el inicio de la nueva agenda
     * @param oneDiary La agenda existente
     * @return Verdadero si no existe solapamiento, falso si sí.
     */
    private boolean makeCheck(DiaryEntity oneDiary) {
        LocalDate oneStart = oneDiary.getStartTime();
        LocalDate oneEnd = oneDiary.getEndTime();
        LocalDate startTimeValue = startTime.getValue();
        LocalDate endTimeValue = endTime.getValue();

        // Caso 1: La agenda existente empieza antes o al mismo tiempo que el inicio de la nueva agenda y termina después de que el inicio de la nueva agenda
        boolean case1 = (oneStart.isBefore(startTimeValue) || oneStart.isEqual(startTimeValue))
                && (oneEnd == null || startTimeValue.isBefore(oneEnd) || startTimeValue.isEqual(oneEnd));

        // Caso 2: La agenda existente empieza antes o al mismo tiempo que el final de la nueva agenda y termina después o al mismo tiempo que el final de la nueva agenda
        boolean case2 = (oneStart.isBefore(endTimeValue) || oneStart.isEqual(endTimeValue))
                && (endTimeValue.isBefore(oneEnd) || endTimeValue.isEqual(Objects.requireNonNull(oneEnd)));

        // Caso 3: El inicio de la nueva agenda empieza antes o al mismo tiempo que la agenda existente y termina después de que la agenda existente haya terminado
        boolean case3 = (startTimeValue.isBefore(oneStart) || startTimeValue.isEqual(oneStart)) && (endTimeValue.isAfter(oneEnd) || endTimeValue.isEqual(Objects.requireNonNull(oneEnd)));

        // Caso 4: El inicio de la nueva agenda empieza antes o al mismo tiempo que la agenda existente y la agenda existente termina después de el final de la nueva agenda
        boolean case4 = (startTimeValue.isBefore(oneStart) || startTimeValue.isEqual(oneStart))
                && (oneEnd == null || oneEnd.isAfter(endTimeValue) || oneEnd.isEqual(endTimeValue));

        // Caso 5: La fecha de finalización de la agenda existente es nula y la agenda existente empieza antes o al mismo tiempo que el inicio de la nueva agenda
        boolean case5 = oneEnd == null && (oneStart.isBefore(startTimeValue) || oneStart.isEqual(startTimeValue));

        if (case1 || case2 || case3 || case4 || case5) {
            setErrorMessage("Ya existe una agenda en el centro " + centerEntity.getValue() + " con el sanitario " + sanitaryEntity.getValue());
            return true;
        }
        return false;
    }


    private DiaryEntity getDiaryEntity() {
        DiaryEntity diaryEntity = new DiaryEntity();
        for (String oneDay : days.getValue()) {
            if (oneDay.equals(ConstantUtilities.MONDAY)) {
                diaryEntity.setMonday(true);
            }
            if (oneDay.equals(ConstantUtilities.TUESDAY)) {
                diaryEntity.setTuesday(true);
            }
            if (oneDay.equals(ConstantUtilities.WEDNESDAY)) {
                diaryEntity.setWednesday(true);
            }
            if (oneDay.equals(ConstantUtilities.THURSDAY)) {
                diaryEntity.setThursday(true);
            }
            if (oneDay.equals(ConstantUtilities.FRIDAY)) {
                diaryEntity.setFriday(true);
            }
            if (oneDay.equals(ConstantUtilities.SATURDAY)) {
                diaryEntity.setSaturday(true);
            }
            if (oneDay.equals(ConstantUtilities.SUNDAY)) {
                diaryEntity.setSunday(true);
            }
        }
        return diaryEntity;
    }

    private void fireNewEvent(DiaryEntity diaryEntity) {
        fireEvent(new SaveEvent(this, diaryEntity));
        close();
        setErrorMessage("");
        clearTextField();
    }


    @Override
    public void createDialogLayout() {

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el anccho
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");

        errorMessage.addClassName("label-error");

        HorizontalLayout sanitaryAndCenterHl = new HorizontalLayout(sanitaryEntity, centerEntity);
        sanitaryEntity.addValueChangeListener(event ->updateWithSanitaryEntity(event.getValue()));
        centerEntity.addValueChangeListener(event -> updateWithCenterEntity(event.getValue()));
        sanitaryAndCenterHl.setSizeFull();
        sanitaryAndCenterHl.expand(sanitaryEntity, centerEntity);

        HorizontalLayout daysAndName = new HorizontalLayout(days, name);
        daysAndName.setSizeFull();
        daysAndName.expand(days, name);

        HorizontalLayout timeHl = new HorizontalLayout(startTime, endTime);
        startTime.addValueChangeListener(event -> {
            if (startTime.getValue() != null && endTime.getValue() != null) {
                clearEndTime();
                endTime.setMin(startTime.getValue().plusDays(60)); //La agenda debe tener una duración de al menos dos meses
            }
        });
        endTime.addValueChangeListener(event -> {
            if (startTime.getValue() != null && endTime.getValue() != null) {
                clearEndTime();
            }
        });
        timeHl.setSizeFull();
        timeHl.expand(endTime, startTime);

        errorMessage.setText("");

        dialogVl.add(sanitaryAndCenterHl, daysAndName, timeHl, errorMessage);
    }

    private void clearEndTime() {
        if(endTime.getValue().isBefore(startTime.getValue())){
            endTime.clear();
        }
    }

    private void updateWithSanitaryEntity(SanitaryEntity value) {
       if(sanitaryEntity.isEmpty()){
            resetCombos();
        }else if(centerEntity.isEmpty()){
           centerEntity.setItems(centerService.findBySanitary(value));
       }else if(!centerService.existsBySanitaryEntityAndCenterEntity(sanitaryEntity.getValue(),centerEntity.getValue())){
            resetCombos();
        }

    }

    private void updateWithCenterEntity(CenterEntity value) {
        if(value != null){
            if(sanitaryEntity.isEmpty()){
                // Limpiar ComboBox sólo si no hay selección actual en centro o sanitario
                sanitaryEntity.setItems(sanitaryService.findByCenterEntity(value));
            }else if(!centerService.existsBySanitaryEntityAndCenterEntity(sanitaryEntity.getValue(),centerEntity.getValue())){
                resetCombos();
            }
        }
    }



    private void resetCombos() {
        centerEntity.setItems(allCenters);
        sanitaryEntity.setItems(allSanitaries);
    }

    @Override
    public void clearTextField() {
        name.clear();
        startTime.clear();
        endTime.clear();
        sanitaryEntity.clear();
        days.clear();
        centerEntity.clear();
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
     * Clase abstracta que extiene de {@link DiaryDialog}, evento ocurrido en dicha clase.
     * Almacena la agenda asociada al evento.
     */
    @Getter
    public static abstract class DiaryFormEvent extends ComponentEvent<DiaryDialog> {
        private final DiaryEntity diaryEntity; //Agenda con la que se trabaja

        protected DiaryFormEvent(DiaryDialog source,
                                 DiaryEntity diaryEntity) {
            super(source, false);
            this.diaryEntity = diaryEntity;
        }
    }

    /**
     * Clase heredada de DiaryFormEvent, representa un evento de guardado que ocurre en el diálogo de agenda.
     */
    public static class SaveEvent extends DiaryFormEvent {
        SaveEvent(DiaryDialog source, DiaryEntity diaryEntity) {
            super(source, diaryEntity);
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
