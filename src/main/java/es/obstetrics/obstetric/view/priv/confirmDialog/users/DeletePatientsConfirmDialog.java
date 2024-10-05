package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.users.PatientsGridView;
import lombok.Getter;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si desea eliminar usuarios.
 */
public class DeletePatientsConfirmDialog extends MasterConfirmDialog {

    private final Binder<PatientEntity> userBinder;
    public DeletePatientsConfirmDialog(PatientEntity user){
        userBinder = new Binder<>(PatientEntity.class);
        userBinder.setBean(user);  //Recojo el usuario
        userBinder.readBean(user);
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja paciente");
        setText("Se va a proceder a dar de baja al paciente: " + userBinder.getBean().getName().toUpperCase() +", ¿Está seguro?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link PatientsGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link PatientsGridView }
     que debe borrar el usuario.
     */
    @Override
    public void clickButton() {
        fireEvent(new DeleteEvent(this, userBinder.getBean()));
        closeDialog();
    }

    /**
     * Clase abstracta que extiene de {@link DeletePatientsConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el usuario asociada al evento.
     */
    @Getter
    public static  abstract  class DeleteUserConfirmDialogFormEvent extends ComponentEvent<DeletePatientsConfirmDialog> {
        private final PatientEntity userEntity; //Comunidad con la que trabajamos

        protected  DeleteUserConfirmDialogFormEvent(DeletePatientsConfirmDialog source, PatientEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }
    }
    /**
     * Clase heredada de DeleteUsersConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el usuario asociada al evento.
     */
    public static  class DeleteEvent extends DeleteUserConfirmDialogFormEvent {
        DeleteEvent(DeletePatientsConfirmDialog source, PatientEntity userEntity){
            super(source, userEntity);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T> CLase
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
