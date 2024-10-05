package es.obstetrics.obstetric.view.priv.confirmDialog.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.Getter;

public class UnsubscribeAppointmentTypeConfirmDialog extends MasterConfirmDialog {

    private final AppointmentTypeEntity appointmentType;

    public UnsubscribeAppointmentTypeConfirmDialog(AppointmentTypeEntity appointmentType){
        this.appointmentType = appointmentType;
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja tipo de cita");
        setText("Se va a dar de baja el tipo de cita: " + appointmentType.getDescription() + "¿Está seguro?");
    }

    /**
     * Cierre del cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe borrar la categoría.
     */
    @Override
    public void clickButton() {
        closeDialog();
        fireEvent(new UnsubscribeEvent(this, appointmentType));
    }

    /**
     * Clase abstracta que extiene de {@link UnsubscribeAppointmentTypeConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el tipo de cita asociada al evento.
     */
    @Getter
    public static  abstract  class AppointmentConfirmDialogFormEvent extends ComponentEvent<UnsubscribeAppointmentTypeConfirmDialog> {

        private final AppointmentTypeEntity appointmentTypeEntity;
        protected  AppointmentConfirmDialogFormEvent(UnsubscribeAppointmentTypeConfirmDialog source,AppointmentTypeEntity appointmentTypeEntity){
            super(source, false);
            this.appointmentTypeEntity = appointmentTypeEntity;
        }

    }
    /**
     * Clase heredada de UnsubscribeAppointmentTypeConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el tipo de  cita asociada al evento.
     */
    public static  class UnsubscribeEvent extends AppointmentConfirmDialogFormEvent {
        UnsubscribeEvent(UnsubscribeAppointmentTypeConfirmDialog source, AppointmentTypeEntity appointmentType){
            super(source,appointmentType);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}

