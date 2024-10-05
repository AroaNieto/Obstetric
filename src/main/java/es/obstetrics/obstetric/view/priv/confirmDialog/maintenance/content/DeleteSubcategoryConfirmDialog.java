package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.Getter;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si desea eliminar subcategorías.
 */
public class DeleteSubcategoryConfirmDialog extends MasterConfirmDialog {

    private final Binder<SubcategoryEntity> subcategoryEntityBinder;

    /**
     * Constructor
     * @param subcategory Subcategoría sobre la que se va a operar
     */
    public DeleteSubcategoryConfirmDialog(SubcategoryEntity subcategory){
        subcategoryEntityBinder = new Binder<>(SubcategoryEntity.class);
        subcategoryEntityBinder.setBean(subcategory);
        subcategoryEntityBinder.readBean(subcategory);
        createHeaderAndTextDialog();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja subcategoria");
        setText("Se va a proceder a dar de baja la subcategoria: "
                + subcategoryEntityBinder.getBean().getName()
                +" que forma parte de la categoria: " +subcategoryEntityBinder.getBean().getCategoryEntity().getName().toUpperCase()+", ¿Está seguro?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe borrar la subcategoria.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new DeleteEvent(this,subcategoryEntityBinder.getBean()));
    }

    /**
     * Clase abstracta que extiene de {@link DeleteSubcategoryConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la subcategoria asociada al evento.
     */
    @Getter
    public static  abstract  class DeleteSubcategoryDialogFormEvent extends ComponentEvent<DeleteSubcategoryConfirmDialog> {
        private final SubcategoryEntity subcategoryEntity; //Comunidad con la que trabajamos

        protected  DeleteSubcategoryDialogFormEvent(DeleteSubcategoryConfirmDialog source, SubcategoryEntity subcategoryEntity){
            super(source, false);
            this.subcategoryEntity = subcategoryEntity;
        }
    }

    /**
     * Clase heredada de DeleteSubcategoryConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece la subcategoria asociada al evento.
     */
    public static  class DeleteEvent extends DeleteSubcategoryDialogFormEvent {
        DeleteEvent(DeleteSubcategoryConfirmDialog source, SubcategoryEntity subcategoryEntity){
            super(source, subcategoryEntity);
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
