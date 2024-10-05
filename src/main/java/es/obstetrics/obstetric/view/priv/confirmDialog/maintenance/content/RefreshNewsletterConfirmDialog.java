package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.NotificationGridView;
import lombok.Getter;

public class RefreshNewsletterConfirmDialog extends MasterConfirmDialog {
    private final NotificationEntity notificationEntity;

    /**
     * Constructor
     * @param notificationEntity Mensaje sobre el que se va a operar
     */
    public RefreshNewsletterConfirmDialog(NotificationEntity notificationEntity){
        this.notificationEntity = notificationEntity;
        createHeaderAndTextDialog();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Reenviar mensaje");
        if(notificationEntity.getNewsletterEntity() != null){
            setText("Se va a proceder a reenviar el mensaje: "
                    + notificationEntity.getNewsletterEntity().getName()
                    +" que pertenece al paciente "
                    +notificationEntity.getUserEntity().getUsername().toUpperCase() +", ¿Está seguro de ello?");
        }else if(notificationEntity.getAppointmentEntity() != null){
            setText("Se va a proceder a reenviar el recordatorio de cita del día: "
                    + notificationEntity.getAppointmentEntity().getDate() + "a las "+notificationEntity.getAppointmentEntity().getStartTime()
                            +  notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " "
                            + notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName()
                    + ", ¿Está seguro de ello?");
        }

    }

    /**
     * Dispara el evento para notificar a la clase {@link NotificationGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link NotificationGridView }
     que debe actualizar el mensaje.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new UpdateEvent(this,notificationEntity));
    }

    /**
     * Clase abstracta que extiene de {@link RefreshNewsletterConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el mensaje asociado al evento.
     */
    @Getter
    public static  abstract  class RefreshNewsletterFormEvent extends ComponentEvent<RefreshNewsletterConfirmDialog> {
        private final NotificationEntity notificationEntity; //Comunidad con la que trabajamos

        protected  RefreshNewsletterFormEvent(RefreshNewsletterConfirmDialog source, NotificationEntity notificationEntity){
            super(source, false);
            this.notificationEntity = notificationEntity;
        }
    }

    /**
     * Clase heredada de RefreshNewsletterFormEvent, representa un evento de cerrar que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece el mensaje asociado al evento.
     */
    public static  class UpdateEvent extends RefreshNewsletterFormEvent {
        UpdateEvent(RefreshNewsletterConfirmDialog source, NotificationEntity notificationEntity){
            super(source, notificationEntity);
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
