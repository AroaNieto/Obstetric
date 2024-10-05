package es.obstetrics.obstetric.view.priv.confirmDialog.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.utilities.EmailUtility;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.Getter;

public class UnsubscribeAppointmentConfirmDialog extends MasterConfirmDialog {
    private final AppointmentEntity appointmentEntity;
    private final EmailUtility emailUtility;

    public UnsubscribeAppointmentConfirmDialog(AppointmentEntity appointmentEntity, EmailUtility emailUtility){
        this.appointmentEntity = appointmentEntity;
        this.emailUtility =emailUtility;
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja tipo de cita");
        setText("Se va a dar de baja la cita del día " + appointmentEntity.getDate() +
                " " + appointmentEntity.getStartTime() + "-" + appointmentEntity.getEndTime() +
                ". Con el paciente "+ appointmentEntity.getPatientEntity().getName()+ " " + appointmentEntity.getPatientEntity().getLastName() +
                " y el sanitario "+ appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName()+" "+ appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName()+
                " ¿Está seguro?");
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
        //Se envía el correo al sanitario y al paciente que la cita ha sido cancelada.
        emailUtility.sendEmail(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getEmail(),
                "Anulación de cita",
                "Su cita con la paciente " + appointmentEntity.getPatientEntity().getName() + " " + appointmentEntity.getPatientEntity().getLastName()
        + " el día "+ appointmentEntity.getDate() + " de "+appointmentEntity.getStartTime() + "-"+appointmentEntity.getEndTime()+
                        " ha sido ANULADA. Disculpe las molestias. ");

        emailUtility.sendEmail(appointmentEntity.getPatientEntity().getEmail(),
                "Anulación de cita",
                "Su cita con el sanitario " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " " + appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName()
                        + " el día "+ appointmentEntity.getDate() + " de "+appointmentEntity.getStartTime() + "-"+appointmentEntity.getEndTime()+
                        " ha sido ANULADA. Disculpe las molestias. ");
        fireEvent(new UnsubscribeEvent(this, appointmentEntity));
    }

    /**
     * Clase abstracta que extiene de {@link UnsubscribeAppointmentConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la cita asociada al evento.
     */
    @Getter
    public static  abstract  class AppointmentConfirmDialogFormEvent extends ComponentEvent<UnsubscribeAppointmentConfirmDialog> {

        private final AppointmentEntity appointmentEntity;
        protected  AppointmentConfirmDialogFormEvent(UnsubscribeAppointmentConfirmDialog source,AppointmentEntity appointmentEntity){
            super(source, false);
            this.appointmentEntity = appointmentEntity;
        }

    }
    /**
     * Clase heredada de AppointmentConfirmDialogFormEvent, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece la cita asociada al evento.
     */
    public static  class UnsubscribeEvent extends AppointmentConfirmDialogFormEvent {
        UnsubscribeEvent(UnsubscribeAppointmentConfirmDialog source, AppointmentEntity appointmentEntity){
            super(source,appointmentEntity);
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

