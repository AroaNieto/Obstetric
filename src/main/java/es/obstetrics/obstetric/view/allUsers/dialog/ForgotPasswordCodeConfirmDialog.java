package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.view.login.LoginView;
import lombok.Getter;

public class ForgotPasswordCodeConfirmDialog extends MasterPublicConfirmDialog {
    private final PatientEntity user;

    public ForgotPasswordCodeConfirmDialog(PatientEntity user){
        createHeaderAndTextDialog();
        this.user = user;
    }

    /**
     * Creación de la cabecera y el texto que aparecerá en el cuadro de
     *  diálogo
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("¡Código correcto!");
        setText("El código introduccido es correcto. El siguiente paso es crear su nueva contraseña.\n" +
                "Tenga en cuenta que la contraseña que escoja debe ser segura");
    }

    /**
     * Evento que notifica a la clase {@link LoginView} que se está cerrando
     *  el cuadro de diálogo.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new SaveEvent(this, user));
    }

    /**
     * Clase abstracta que extiene de {@link ForgotPasswordCodeConfirmDialog}, evento ocurrido en dicha clase
     */
    @Getter
    public static  abstract  class ForgotPasswordCodeDialogFormEvent extends ComponentEvent<ForgotPasswordCodeConfirmDialog> {
        private final PatientEntity userEntity;

        protected  ForgotPasswordCodeDialogFormEvent(ForgotPasswordCodeConfirmDialog source, PatientEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }
    }

    /**
     * Clase heredada de ForgotPasswordCodeConfirmDialog, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends ForgotPasswordCodeDialogFormEvent {

        SaveEvent(ForgotPasswordCodeConfirmDialog source, PatientEntity userEntity){
            super(source, userEntity);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T>
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
