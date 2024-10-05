package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.EditUserProfileDialog;

public class EditUserProfileConfirmDialog extends MasterConfirmDialog {
    private Binder<UserEntity> userBinder;

    public EditUserProfileConfirmDialog(UserEntity user){
        userBinder = new Binder<>(UserEntity.class);
        userBinder.setBean(user);  //Recojo el usuario
        userBinder.readBean(user);
        createHeaderAndTextDialog(); //Establecer los valores
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Modificar datos personales");
        setText(userBinder.getBean().getName() + ", Se va a proceder a modificar tus datos personales, ¿Está seguro de ello?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link EditUserProfileDialog }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link EditUserProfileDialog }
     que debe actualizar el usuario.
     */
    @Override
    public void clickButton() {
        fireEvent(new UpdateEvent(this, userBinder.getBean()));
        closeDialog();
    }

    /**
     * Clase abstracta que extiene de {@link EditUserProfileConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el usuario asociada al evento.
     */
    public static  abstract  class EditUserProfileConfirmDialogFormEvent extends ComponentEvent<EditUserProfileConfirmDialog> {
        private UserEntity userEntity; //Comunidad con la que trabajamos

        protected  EditUserProfileConfirmDialogFormEvent(EditUserProfileConfirmDialog source, UserEntity userEntity){
            super(source, false);
            this.userEntity = userEntity;
        }

        public UserEntity getUser(){
            return userEntity;
        }
    }
    /**
     * Clase heredada de EditUserProfileConfirmDialog, representa un evento de actualizar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el usuario asociada al evento.
     */
    public static  class UpdateEvent extends EditUserProfileConfirmDialogFormEvent {
        UpdateEvent(EditUserProfileConfirmDialog source, UserEntity userEntity){
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
