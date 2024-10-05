package es.obstetrics.obstetric.view.allUsers.dialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
/**
 * Clase abstracta que sirve extiende de {@link ConfirmDialog} y se crearán de todos
 *      usuarios que se registren en la aplicación cuando un usuario
 *      no ha iniciado sesión.
 */
public abstract class MasterPublicConfirmDialog extends ConfirmDialog {
    /**
     * Constructor de la clase, crea el cuadro de dialogo cancelable (para que el usuario pueda cerrarlo),
     *  se agrega el evento de confirmación cuando el usuario confirma el diálogo y un evento de cancelación.
     */
    public  MasterPublicConfirmDialog(){
        setCancelable(false);
        setConfirmText("Continuar");
        addConfirmListener(event -> clickButton());
    }

    /**
     * Método abstracto que configura el contenido de la cabecera del cuadro de diálogo.
     */
    public abstract void createHeaderAndTextDialog();


    /**
     * Método abstracto implementado por las subclases para definir la
     *  acción de guardar.
     */
    public abstract void clickButton();

}
