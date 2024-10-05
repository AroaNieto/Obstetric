package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.users.SanitariesGridView;
import lombok.Getter;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el adminsitrador confirme si desea eliminar sanitarios.
 */
public class DeleteSanitaryConfirmDialog extends MasterConfirmDialog {
    private Binder<SanitaryEntity> userBinder;
    private boolean isDischarged;
    public DeleteSanitaryConfirmDialog(SanitaryEntity user){
        userBinder = new Binder<>(SanitaryEntity.class);
        userBinder.setBean(user);  //Recojo el sanitario
        userBinder.readBean(user);
        isDischarged = false;

        if(user.getState().equals(ConstantUtilities.STATE_DISCHARGED)){
           createHeaderAndTextErrorDialog();
        }else{
            createHeaderAndTextDialog(); //Establecer los valores
        }
    }

    private void createHeaderAndTextErrorDialog() {
        isDischarged = true;
        setHeader("Dar de baja sanitario");
        setText("El sanitario " + userBinder.getBean().getName().toUpperCase() +" ya está dado de baja.");
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja sanitario");
        setText("Se va a proceder a dar de baja el sanitario: " + userBinder.getBean().getName().toUpperCase() +", ¿Está seguro?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link SanitariesGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link SanitariesGridView }
     que debe borrar el sanitario.
     */
    @Override
    public void clickButton() {
        if(!isDischarged){
            fireEvent(new DeleteEvent(this, userBinder.getBean()));
        }
        closeDialog();
    }

    /**
     * Clase abstracta que extiene de {@link DeleteSanitaryConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el sanitario asociada al evento.
     */
    @Getter
    public static  abstract  class DeleteSanitaryConfirmDialogFormEvent extends ComponentEvent<DeleteSanitaryConfirmDialog> {
       private final SanitaryEntity userEntity; //Comunidad con la que trabajamos

        protected  DeleteSanitaryConfirmDialogFormEvent(DeleteSanitaryConfirmDialog source, SanitaryEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }
    }

    /**
     * Clase heredada de DeleteSanitaryConfirmDialogFormEvent, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el usuario asociada al evento.
     */
    public static  class DeleteEvent extends DeleteSanitaryConfirmDialogFormEvent {
        DeleteEvent(DeleteSanitaryConfirmDialog source, SanitaryEntity userEntity){
            super(source, userEntity);
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
