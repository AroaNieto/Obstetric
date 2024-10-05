package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.insurance;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si desea eliminar la aseguradora.
 */
public class DeleteInsuranceConfirmDialog extends MasterConfirmDialog {

    private final Binder<InsuranceEntity> insuranceEntityBinder;

    public DeleteInsuranceConfirmDialog(InsuranceEntity insuranceEntity){
        insuranceEntityBinder = new Binder<>(InsuranceEntity.class);
        insuranceEntityBinder.setBean(insuranceEntity);  //Recojo la aseguradora
        insuranceEntityBinder.readBean(insuranceEntity);
        createHeaderAndTextDialog(); //Establecer los valores
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja aseguradora");
        setText("Se va a proceder a dar de baja la aseguradora: " +
                insuranceEntityBinder.getBean().getName() +", ¿Está seguro?");
    }

    /**
     * Cierre del cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.maintenance.insurance.InsuranceGridView }
     que debe borrar la aseguradora.
     */
    @Override
    public void clickButton() {
        closeDialog();
        fireEvent(new DeleteEvent(this,insuranceEntityBinder.getBean()));
    }

    /**
     * Clase abstracta que extiene de {@link DeleteInsuranceConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la aseguradora asociada al evento.
     */
    public static  abstract  class DeleteInsuranceDialogFormEvent extends ComponentEvent<DeleteInsuranceConfirmDialog> {
        private InsuranceEntity insuranceEntity; //Comunidad con la que trabajamos

        protected  DeleteInsuranceDialogFormEvent(DeleteInsuranceConfirmDialog source, InsuranceEntity insuranceEntity){
            super(source, false);
            this.insuranceEntity = insuranceEntity;
        }

        public InsuranceEntity getInsurance(){
            return insuranceEntity;
        }
    }
    /**
     * Clase heredada de DeleteInsuranceConfirmDialog, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static  class DeleteEvent extends DeleteInsuranceDialogFormEvent {
        DeleteEvent(DeleteInsuranceConfirmDialog source,  InsuranceEntity insuranceEntity){
            super(source, insuranceEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
