package es.obstetrics.obstetric.view.priv.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Clase abstracta que sirve extiende de {@link Dialog} y se crearán de todos
 *      los Dialog que se registren en la aplicación.
 */
public abstract class MasterDialog extends Dialog {

    protected Button cancelBtn;
    protected Button  button;
    protected VerticalLayout dialogVl ;

    /**
     * Constructor de la clase, proporciona una base genérica para la creación de un Dialog
     *  Crea el botón de cancelación y de guardar y establece el cuaddro de dialogo en modo
     *   no modal.
     */
    public MasterDialog() {
        dialogVl = new VerticalLayout();
        button = new Button();
        button.addClickListener(event -> clickButton());
        getFooter().add(button);

        cancelBtn = new Button(VaadinIcon.CLOSE.create(), e -> closeDialog());
        cancelBtn.addClassName("lumo-error-color-background-button");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        getHeader().add(cancelBtn);

        //    setDraggable(true); //El cuadro de diálogo permite arrastrar.
        setModal(true);
        setResizable(true);

        add(dialogVl);
    }

    /**
     * Método abstracto que configura el contenido de la cabecera del cuadro de diálogo.
     */
    public abstract void createHeaderDialog();

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

    /**
     * Método abstracto implementado que configura el contenido del cuadro de diálogo.
     */
    public abstract void createDialogLayout();

    public abstract void clearTextField();
    public abstract void setErrorMessage(String message);
}