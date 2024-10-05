package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import lombok.Getter;

import java.time.LocalDate;

public class DeleteScheduleConfirmDialog extends MasterConfirmDialog {
    private final Binder<ScheduleEntity> scheduleEntityBinder;
    private final ScheduleEntity scheduleEntity;

    public DeleteScheduleConfirmDialog(ScheduleEntity scheduleEntity){
        this.scheduleEntity = scheduleEntity;
        scheduleEntityBinder = new Binder<>(ScheduleEntity.class);
        scheduleEntityBinder.setBean(scheduleEntity);  //Recojo la aseguradora
        scheduleEntityBinder.readBean(scheduleEntity);
        if(!checkAppointments()){
            setErrorMessage();
        }else{
            createHeaderAndTextDialog(); //Establecer los valores
        }
    }

    /**
     * Comprueba si existen citas pendientas para  ese horario, si es así el sanitario no podrá darlas
     *      de baja.
     * @return Verdadero si no existen citas, falso si sí.
     */
    private boolean checkAppointments(){

        for(AppointmentEntity oneAppointment: scheduleEntity.getAppointmentEntities()){
            if((oneAppointment.getDate().isAfter(LocalDate.now()) || oneAppointment.getDate().isEqual(LocalDate.now())) && oneAppointment.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                return false;
            }
        }

        return true;
    }

    private void setErrorMessage() {
        setHeader("No puede dar de baja el horario.");
        if(scheduleEntityBinder.getBean().getEndingDate() != null){
            setText("El horario de " +
                    scheduleEntityBinder.getBean().getStartDate() +
                    "-" + scheduleEntityBinder.getBean().getEndingDate()+
                    ", con horas en " + scheduleEntityBinder.getBean().getStartTime() +
                    "-" + scheduleEntityBinder.getBean().getEndTime() +
                    ", tiene citas pendientes. Debe darlas de baja para poder dar de baja este horario.");
        }else{
            setText("El horario que pertenece a la agenda: " +scheduleEntityBinder.getBean().getDiaryEntity().getName()+" de "+
                    scheduleEntityBinder.getBean().getStartDate() +
                    ", con horas en " + scheduleEntityBinder.getBean().getStartTime() +
                    "-" + scheduleEntityBinder.getBean().getEndTime() +
                    ", tiene citas pendientes. Debe darlas de baja para poder dar de baja este horario.");
        }

        setConfirmText("De acuerdo");
        setCancelable(false);
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja horario");
        setText("Se va a proceder a dar de baja el horario de " +
                scheduleEntityBinder.getBean().getStartDate() +
                "-" + scheduleEntityBinder.getBean().getEndingDate()+
                ", con horas en" + scheduleEntityBinder.getBean().getStartTime() +
                "-" + scheduleEntityBinder.getBean().getEndTime()+
                " ¿Está seguro de ello?");
    }

    /**
     * Cierre del cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.maintenance.appointment.DiaryGridView }
     que debe borrar el horario.
     */
    @Override
    public void clickButton() {
        closeDialog();
        if(checkAppointments()){
            fireEvent(new DeleteEvent(this,scheduleEntityBinder.getBean()));
        }
    }

    /**
     * Clase abstracta que extiene de {@link DeleteScheduleConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la agenda asociada al evento.
     */
    @Getter
    public static  abstract  class DeleteScheduleDialogFormEvent extends ComponentEvent<DeleteScheduleConfirmDialog> {
        private final ScheduleEntity scheduleEntity; //Horario con el que trabajamos

        protected  DeleteScheduleDialogFormEvent(DeleteScheduleConfirmDialog source,  ScheduleEntity scheduleEntity){
            super(source, false);
            this.scheduleEntity = scheduleEntity;
        }

    }
    /**
     * Clase heredada de DeleteScheduleConfirmDialog, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static  class DeleteEvent extends DeleteScheduleDialogFormEvent {
        DeleteEvent(DeleteScheduleConfirmDialog source, ScheduleEntity scheduleEntity){
            super(source, scheduleEntity);
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
