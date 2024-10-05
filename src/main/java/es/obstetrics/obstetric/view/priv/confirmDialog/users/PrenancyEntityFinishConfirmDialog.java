package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;

public class PrenancyEntityFinishConfirmDialog extends MasterConfirmDialog {
    private final PatientEntity patientEntity;

    public PrenancyEntityFinishConfirmDialog(PatientEntity patientEntity){
        this.patientEntity = patientEntity;
        createHeaderAndTextDialog();
    }
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Finalizar embarazo");
        setText("Se va a finalizar el embarazo de la paciente "+patientEntity+" ¿Está seguro de ello?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.users.PatientDetailsView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    @Override
    public void clickButton() {
        fireEvent(new FinishEvent(this));
    closeDialog();
    }

    /**
     * Clase abstracta que extiene de {@link PrenancyEntityFinishConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el usuario asociada al evento.
     */
    public static  abstract  class PregnancyFinishConfirmDialogFormEvent extends ComponentEvent<PrenancyEntityFinishConfirmDialog> {

        protected  PregnancyFinishConfirmDialogFormEvent(PrenancyEntityFinishConfirmDialog source){
            super(source, false);
        }
    }
    /**
     * Clase heredada de PrenancyEntityFinishConfirmDialog, representa un evento de cerrar que ocurre en el diálogo
     */
    public static  class FinishEvent extends PregnancyFinishConfirmDialogFormEvent {
        FinishEvent(PrenancyEntityFinishConfirmDialog source){
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
