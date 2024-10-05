package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;

public class CodeConfirmDialog extends MasterPublicConfirmDialog {
    private final PatientEntity patient;
    public CodeConfirmDialog(PatientEntity user){
        this.patient = user;
        createHeaderAndTextDialog();
    }


    @Override
    public void createHeaderAndTextDialog() {
        setHeader("¡Código correcto!");
        setText("El código introduccido es correcto. El siguiente paso es crear su nombre de usuario y contraseña para que pueda iniciar sesión.\n" +
                "Tenga en cuenta que la contraseña que escoja debe ser segura.");
    }

    @Override
    public void clickButton() {
        close();
        fireEvent(new SaveEvent(this,patient));
    }

    /**
     * Clase abstracta que extiene de {@link CodeConfirmDialog}, evento ocurrido en dicha clase
     */
    public static  abstract  class CodeDialogFormEvent extends ComponentEvent<CodeConfirmDialog> {
        private final PatientEntity userEntity;

        protected  CodeDialogFormEvent(CodeConfirmDialog source,PatientEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }

        public PatientEntity getUser(){
            return userEntity;
        }
    }
    /**
     * Clase heredada de CodeConfirmDialog, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends CodeDialogFormEvent{
        SaveEvent(CodeConfirmDialog source, PatientEntity userEntity){
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
