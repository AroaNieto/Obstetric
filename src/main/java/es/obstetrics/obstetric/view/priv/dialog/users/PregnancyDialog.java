package es.obstetrics.obstetric.view.priv.dialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PregnanceEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;

import java.time.LocalDate;

public class PregnancyDialog extends MasterDialog {
    private final Binder<PregnanceEntity> pregnancyEntityBinder;
    private final DatePicker registerDate;
    private final DatePicker lastPeriodDate;
    private final H5 errorMessage;
    private final PregnanceEntity pregnanceEntity;

    public PregnancyDialog(PregnanceEntity pregnance){
        setMinWidth("400px");
        setMaxWidth("400px");
        pregnanceEntity = pregnance;

        registerDate = new DatePickerTemplate("");
        errorMessage = new H5("");
        lastPeriodDate = new DatePickerTemplate("Fecha de última regla");
        pregnancyEntityBinder = new BeanValidationBinder<>(PregnanceEntity.class);
        pregnancyEntityBinder.bindInstanceFields(this);
        createHeaderDialog();
        createDialogLayout();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Establecer embarazo");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.getFooter().add(button);
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.users.PatientDetailsView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Método que se ejecuta cuando el sanitario selecciona que desea
     *      establecer el embarazo.
     */
    @Override
    public void clickButton() {
        if (lastPeriodDate == null) {
            setErrorMessage("Debe completar la fecha correctamente");
            return;
        }
        closeDialog();
        writeBean(pregnanceEntity);
    }

    /**
     * Escribe el valor del contenido en el binder y dispara el evento SaveEvent
     *  para que la clase {@link  es.obstetrics.obstetric.view.priv.views.users.PatientDetailsView}
     *  lo recoja y pueda guardar el embarazo en la base de datos.
     *
     * @param pregnance Instancia dónde se escribirán los valores
     */
    public void writeBean(PregnanceEntity pregnance){

        try {
            pregnancyEntityBinder.writeBean(pregnance);
            registerDate.setValue(LocalDate.now());
            pregnance.setState(ConstantUtilities.STATE_ACTIVE);
            fireEvent(new SaveEvent(this, pregnance));

        } catch (ValidationException e) {
            setErrorMessage(e.getMessage());
        }
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo del embarazo
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {
        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
        errorMessage.addClassName("label-error");
        lastPeriodDate.addValueChangeListener(event ->{
            if(LocalDate.now().isBefore(event.getValue())){
                setErrorMessage("La fecha debe ser anterior a la actual");
                lastPeriodDate.clear();
            }
            if(Utilities.quarterCalculator(lastPeriodDate.getValue().toEpochDay()) > 42){
                setErrorMessage("La fecha indica más de 42 semanas de embarazo.");
                lastPeriodDate.clear();
            }
        });
        dialogVl.add(lastPeriodDate, errorMessage);
    }


    @Override
    public void clearTextField() {
        pregnancyEntityBinder.readBean(null);
        registerDate.clear();
        lastPeriodDate.clear();
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
     *  Clase abstracta que extiene de {@link PregnancyDialog}, evento ocurrido en dicha clase.
     *      Almacena el embarazo asociado al evento.
     */
    public static  abstract  class PregnanceFormEvent extends ComponentEvent<PregnancyDialog> {
        private PregnanceEntity pregnanceEntity; //Usuario con el que se trabaja

        protected  PregnanceFormEvent(PregnancyDialog source, PregnanceEntity pregnanceEntity){
            super(source, false);
            this.pregnanceEntity = pregnanceEntity;
        }

        public PregnanceEntity getPregnance(){
            return pregnanceEntity;
        }
    }

    /**
     * Clase heredada de PregnanceFormEvent, representa un evento de guardado que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece el embarazo asociado al evento.
     */
    public static  class SaveEvent extends PregnanceFormEvent {
        SaveEvent(PregnancyDialog source, PregnanceEntity pregnanceEntity){
            super(source, pregnanceEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que maneajrá el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
