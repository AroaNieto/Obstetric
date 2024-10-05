package es.obstetrics.obstetric.view.priv.confirmDialog.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.Getter;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el trabajador tenga constancia dde que la cita se ha reservado
 *  correctamente.
 */
public class AppointmentConfirmDialog extends MasterConfirmDialog{

    private final AppointmentEntity appointment;
    public AppointmentConfirmDialog(AppointmentEntity appointment){
        this.appointment = appointment;
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Nueva cita");
        setCancelable(false);
        setText("Se ha concertado una nueva cita con la paciente " + appointment.getPatientEntity().getName() +" "+ appointment.getPatientEntity().getLastName()
                +" y el sanitario " + appointment.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " "+ appointment.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName()
                +". El día "+ appointment.getDate().getDayOfMonth() + "-"+ appointment.getDate().getMonthValue()+"-"+ appointment.getDate().getYear()
                + " a las "+appointment.getStartTime() +" en el centro "+ appointment.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName());
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
        fireEvent(new ConfirmEvent(this));
    }

    /**
     * Clase abstracta que extiene de {@link AppointmentConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la cita asociada al evento.
     */
    @Getter
    public static  abstract  class AppointmentConfirmDialogFormEvent extends ComponentEvent<AppointmentConfirmDialog> {

        protected  AppointmentConfirmDialogFormEvent(AppointmentConfirmDialog source){
            super(source, false);
        }

    }
    /**
     * Clase heredada de AppointmentConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece la cita asociada al evento.
     */
    public static  class ConfirmEvent extends AppointmentConfirmDialogFormEvent {
        ConfirmEvent(AppointmentConfirmDialog source){
            super(source);
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
