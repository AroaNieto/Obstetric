package es.obstetrics.obstetric.view.priv.confirmDialog;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

/**
 * Clase abstracta que sirve extiende de {@link ConfirmDialog}.
 */
public abstract class MasterConfirmDialog extends ConfirmDialog {

    /**
     * Constructor de la clase, crea el cuadro de dialogo cancelable (para que el usuario pueda cerrarlo),
     *  se agrega el evento de confirmación cuando el usuario confirma el diálogo y un evento de cancelación.
     */
    public  MasterConfirmDialog(){
        setCancelable(true);
        addCancelListener(cancelEvent ->  closeDialog());
        setConfirmText("Continuar");
        setCancelText("Cancelar");
        addConfirmListener(event -> clickButton());
    }

    /**
     * Método abstracto que configura el contenido de la cabecera del cuadro de diálogo.
     */
    public abstract void createHeaderAndTextDialog();

    /**
     * Método abstracto encargado de cerrar el cuadro de diálogo cuando
     *  el usuario cancela la opción de cancelación.
     */
    public abstract void closeDialog();

    /**
     * Método abstracto implementado por las subclases para definir la
     *  acción de guardar.
     */
    public abstract void clickButton();



}
