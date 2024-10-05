package es.obstetrics.obstetric.view.priv.dialog.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.service.AppointmentTypeService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 * que el usuario añada o edite los tipos de citas..
 */
public class AppointmentTypeDialog extends MasterDialog {

    private final Binder<AppointmentTypeEntity> appointmentTypeEntityBinder;
    private final H5 errorMessage;
    private final TextField description;
    private final AppointmentTypeService appointmentTypeService;

    public AppointmentTypeDialog(AppointmentTypeEntity appointmentType,AppointmentTypeService appointmentTypeService) {
        this.appointmentTypeService = appointmentTypeService;

        description = new TextField("Descripción");
        errorMessage = new H5("");
        appointmentTypeEntityBinder = new BeanValidationBinder<>(AppointmentTypeEntity.class);
        appointmentTypeEntityBinder.bindInstanceFields(this);
        setAppointment();
        createHeaderDialog();
        createDialogLayout();
        appointmentTypeEntityBinder.setBean(appointmentType);  //Recojo la categoria
        appointmentTypeEntityBinder.readBean(appointmentType);
    }

    private void setAppointment() {
        clearTextField();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Cierre del cuadro de diálogo,.
     */
    @Override
    public void closeDialog() {
        close();
        clearTextField();
        setErrorMessage("");
    }


    /**
     * Dispara el evento para notificar a la clase {@link  }
     * que quiere continuar con el proceso de cita.
     */
    @Override
    public void clickButton() {

        if (appointmentTypeEntityBinder.getBean() == null) { //Si se está añadiendo un nuevo tipo de cita
            AppointmentTypeEntity appointmentTypeEntity = new AppointmentTypeEntity();

            try {
                appointmentTypeEntityBinder.writeBean(appointmentTypeEntity);
                appointmentTypeEntity.setState(ConstantUtilities.STATE_ACTIVE);
                if (appointmentTypeService.findOneByDescription(appointmentTypeEntity.getDescription()) != null) {
                    setErrorMessage("La cita ya existe.");
                    return;
                }
                fireEvent(new SaveEvent(this, appointmentTypeEntity));
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                appointmentTypeEntityBinder.writeBean(appointmentTypeEntityBinder.getBean());
                AppointmentTypeEntity oldAppointmentType = appointmentTypeService.findOneByDescription(appointmentTypeEntityBinder.getBean().getDescription());
                //Se comprueba si ya existe un tipo de cita con esa descripción
                if (oldAppointmentType != null &&
                        !oldAppointmentType.getId().equals(appointmentTypeEntityBinder.getBean().getId())
                        && oldAppointmentType.getDescription().equals(appointmentTypeEntityBinder.getBean().getDescription())) {
                    setErrorMessage("La categoría ya existe.");
                    return;
                }
                appointmentTypeEntityBinder.getBean().setState(ConstantUtilities.STATE_ACTIVE);
                fireEvent(new SaveEvent(this, appointmentTypeEntityBinder.getBean()));

            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
        closeDialog();

    }

    @Override
    public void createDialogLayout() {

        HorizontalLayout descriptionHl = new HorizontalLayout(description);
        descriptionHl.setSizeFull();
        descriptionHl.expand(description);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(descriptionHl, errorMessage);
    }

    @Override
    public void clearTextField() {
        description.clear();
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
     * Clase abstracta que extiene de {@link AppointmentTypeDialog}, evento ocurrido en dicha clase.
     */
    public static abstract class AppointmentTypePatientDialogFormEvent extends ComponentEvent<AppointmentTypeDialog> {
        private final AppointmentTypeEntity appointmentTypeEntity; //Comunidad con la que trabajamos

        protected AppointmentTypePatientDialogFormEvent(AppointmentTypeDialog source, AppointmentTypeEntity appointmentTypeEntity) {
            super(source, false);
            this.appointmentTypeEntity = appointmentTypeEntity;
        }

        public AppointmentTypeEntity getAppointment() {
            return appointmentTypeEntity;
        }
    }

    /**
     * Clase heredada de AppointmentConfirmDialog, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static class SaveEvent extends AppointmentTypePatientDialogFormEvent {
        SaveEvent(AppointmentTypeDialog source, AppointmentTypeEntity appointmentTypeEntity) {
            super(source, appointmentTypeEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}

