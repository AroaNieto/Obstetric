package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.DeleteSubcategoryConfirmDialog;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si desea eliminar la agenda.
 */
public class DeleteDiaryConfirmDialog extends MasterConfirmDialog{
    private final Binder<DiaryEntity> diaryEntityBinder;
    private final DiaryEntity diaryEntity;
    public DeleteDiaryConfirmDialog(DiaryEntity diaryEntity){
        this.diaryEntity = diaryEntity;
        diaryEntityBinder = new Binder<>(DiaryEntity.class);
        diaryEntityBinder.setBean(diaryEntity);  //Recojo la aseguradora
        diaryEntityBinder.readBean(diaryEntity);
        if(!checkAppointments()){
            setErrorMessage();
        }else{
            createHeaderAndTextDialog(); //Establecer los valores
        }

    }
    private void setErrorMessage() {
        setHeader("No puede dar de baja la agenda.");
        if(diaryEntity.getEndTime() != null){
            setText("La agenda: "+diaryEntity.getName()+" de " +
                    diaryEntity.getStartTime() +
                    "-" + diaryEntity.getEndTime()+
                    ", tiene citas pendientes para alguno de sus horarios activos. Debe darlas de baja para poder dar de baja este horario.");
        }else{
            setText("La agenda: "+diaryEntity.getName()+" de " +
                    diaryEntity.getStartTime() +
                    ", tiene citas pendientes para alguno de sus horarios activos. Debe darlas de baja para poder dar de baja este horario.");
        }


        setConfirmText("De acuerdo");
        setCancelable(false);
    }

    /**
     * Comprueba si existen citas pendientas para alguna de los horarios de esa agenda, si es así el sanitario no podrá darlas
     *      de baja.
     * @return Verdadero si no existen citas, falso si sí.
     */
    private boolean checkAppointments() {
        for(ScheduleEntity oneSchedule: diaryEntity.getSchedules()){
            if((oneSchedule.getEndingDate() == null ||
                    (oneSchedule.getEndingDate().isAfter(LocalDate.now()) || oneSchedule.getEndingDate().isEqual(LocalDate.now())))
                && oneSchedule.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                for(AppointmentEntity oneAppointment: oneSchedule.getAppointmentEntities()){
                    if((oneAppointment.getDate().isAfter(LocalDate.now()) || oneAppointment.getDate().isEqual(LocalDate.now())) && oneAppointment.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja agenda");
        setText("Se va a proceder a dar de baja la agenda de " +
                diaryEntityBinder.getBean().getSanitaryEntity().getName() +
                " " + diaryEntityBinder.getBean().getSanitaryEntity().getLastName() +
                " con centro en" + diaryEntityBinder.getBean().getCenterEntity().getCenterName().toUpperCase() +", ¿Está seguro?");
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
     que debe borrar la agenda.
     */
    @Override
    public void clickButton() {
        closeDialog();
        if(checkAppointments()){
            fireEvent(new DeleteEvent(this,diaryEntityBinder.getBean()));
        }
    }

    /**
     * Clase abstracta que extiene de {@link DeleteDiaryConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la agenda asociada al evento.
     */
    @Getter
    public static  abstract  class DeleteDiaryDialogFormEvent extends ComponentEvent<DeleteDiaryConfirmDialog> {
        private final DiaryEntity diaryEntity; //Comunidad con la que trabajamos

        protected  DeleteDiaryDialogFormEvent(DeleteDiaryConfirmDialog source,  DiaryEntity diaryEntity){
            super(source, false);
            this.diaryEntity = diaryEntity;
        }

    }
    /**
     * Clase heredada de DeleteDiaryConfirmDialog, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static  class DeleteEvent extends DeleteDiaryDialogFormEvent {
        DeleteEvent(DeleteDiaryConfirmDialog source,  DiaryEntity diaryEntity){
            super(source, diaryEntity);
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
