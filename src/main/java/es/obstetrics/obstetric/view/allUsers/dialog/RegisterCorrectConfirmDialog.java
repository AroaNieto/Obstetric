package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;

public class RegisterCorrectConfirmDialog extends MasterPublicConfirmDialog{

    public RegisterCorrectConfirmDialog(){
        createHeaderAndTextDialog();
    }
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("¡Registro realizado correctamente!");
        setText("Ya puede iniciar sesión para registrar de nuestros servicios");
    }

    @Override
    public void clickButton() {
        fireEvent(new CloseEvent(this));
    }

    /**
     * Clase abstracta que extiene de {@link RegisterCorrectConfirmDialog}, evento ocurrido en dicha clase
     */
    public static  abstract  class RegisterCorrectDialogFormEvent extends ComponentEvent<RegisterCorrectConfirmDialog> {

        protected RegisterCorrectDialogFormEvent(RegisterCorrectConfirmDialog source){
            super(source, false);
        }
    }
    /**
     * Clase heredada de CodeConfirmDialog, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class CloseEvent extends RegisterCorrectDialogFormEvent{
        CloseEvent(RegisterCorrectConfirmDialog source){
            super(source);
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
