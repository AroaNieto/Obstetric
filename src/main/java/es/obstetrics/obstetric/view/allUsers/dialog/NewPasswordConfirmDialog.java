package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.view.login.LoginView;

public class NewPasswordConfirmDialog extends MasterPublicConfirmDialog {

    public NewPasswordConfirmDialog(){
        createHeaderAndTextDialog();
    }

    /**
     * Creación de la cabecera y el texto que aparecerá en el cuadro de
     *  diálogo
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Contraseña cambiada exitosamente");
        setText("La contraseña se ha cambiado de manera correcta, ya esta listo para iniciar sesión con esta nueva contraseña.");
    }

    /**
     * Evento que notifica a la clase {@link LoginView} que se está cerrando
     *  el cuadro de diálogo.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new SaveEvent(this));
    }

    /**
     * Clase abstracta que extiene de {@link NewPasswordConfirmDialog}, evento ocurrido en dicha clase
     */
    public static  abstract  class NewPasswordDialogFormEvent extends ComponentEvent<NewPasswordConfirmDialog> {

        protected  NewPasswordDialogFormEvent(NewPasswordConfirmDialog source){
            super(source, false);
        }
    }
    /**
     * Clase heredada de NewPasswordConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase.
     */
    public static  class SaveEvent extends NewPasswordDialogFormEvent {
        SaveEvent(NewPasswordConfirmDialog source){
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
