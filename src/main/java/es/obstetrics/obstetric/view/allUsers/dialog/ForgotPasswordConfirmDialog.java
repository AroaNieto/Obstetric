package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.view.login.LoginView;

public class ForgotPasswordConfirmDialog extends MasterPublicConfirmDialog {
    private final Binder<UserEntity> userEntityBinder;

    public ForgotPasswordConfirmDialog(UserEntity user){
        createHeaderAndTextDialog();
        userEntityBinder = new Binder<>(UserEntity.class);
        userEntityBinder.setBean(user);  //Recojo el usuario
        userEntityBinder.readBean(user);
    }


    /**
     * Creación de la cabecera y el texto que aparecerá en el cuadro de
     *  diálogo
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Datos introducidos correctamente");
        setText("Se ha enviado un email a su correo electrónico. Reviselo y podrá cambiar su contraseña.");
    }

    /**
     * Evento que notifica a la clase {@link LoginView} que se está cerrando
     *  el cuadro de diálogo.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new SaveEvent(this, userEntityBinder.getBean()));
    }

    /**
     * Clase abstracta que extiene de {@link ForgotPasswordConfirmDialog}, evento ocurrido en dicha clase
     */
    public static  abstract  class ForgotPasswordDialogFormEvent extends ComponentEvent<ForgotPasswordConfirmDialog> {
        private  UserEntity userEntity;

        protected  ForgotPasswordDialogFormEvent(ForgotPasswordConfirmDialog source,  UserEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }

        public UserEntity getUser(){
            return userEntity;
        }
    }

    /**
     * Clase heredada de ForgotPasswordConfirmDialog, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends ForgotPasswordDialogFormEvent {
        SaveEvent(ForgotPasswordConfirmDialog source, UserEntity userEntity){
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
