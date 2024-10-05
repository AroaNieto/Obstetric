package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;

public class DeleteAllDatesConfirmDialog extends MasterConfirmDialog {

    public DeleteAllDatesConfirmDialog(){
        createHeaderAndTextDialog();
    }

    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Eliminar todos mis datos");
        setText("Ha seleccionado la opción eliminar todos mis datos y se va a proceder a notificar al administrador para realice el procedimiento. Tenga en cuenta que esta acción es IRREVERSIBLE e implicará el cierre de su cuenta. ¿Está seguro de ello? ");
    }

    /**
     * Cierra el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.PrincipalView }
         que debe proceden a la eliminaición de datos del usuario.
     */
    @Override
    public void clickButton() {
        fireEvent(new Delete(this));
        closeDialog();
    }
    /**
     * Clase abstracta que extiene de {@link DeleteDiaryConfirmDialog}, evento ocurrido en dicha clase.
     */
    public static  abstract  class DeleteAllDatesConfirmDialogFormEvent extends ComponentEvent<DeleteAllDatesConfirmDialog> {

        protected  DeleteAllDatesConfirmDialogFormEvent(DeleteAllDatesConfirmDialog source){
            super(source, false);
        }

    }
    /**
     * Clase heredada de EditUserProfileConfirmDialogFormEvent, representa un evento de actualizar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el usuario asociada al evento.
     */
    public static  class Delete extends DeleteAllDatesConfirmDialogFormEvent {
        Delete(DeleteAllDatesConfirmDialog source){
            super(source);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T> Clase
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }

}
