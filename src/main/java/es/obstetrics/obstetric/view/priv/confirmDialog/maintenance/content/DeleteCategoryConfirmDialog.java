package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si desea eliminar categorias.
 */
public class DeleteCategoryConfirmDialog extends MasterConfirmDialog {

    private final Binder<CategoryEntity> categoriesBinder;

    public DeleteCategoryConfirmDialog(CategoryEntity category){
        categoriesBinder = new Binder<>(CategoryEntity.class);
        categoriesBinder.setBean(category);  //Recojo la categoria
        categoriesBinder.readBean(category);
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja categoria");
        setText("Se va a proceder a dar de baja la categoría: " + categoriesBinder.getBean().getName().toUpperCase() +", ¿Está seguro?");
    }

    /**
     * Cierre del cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe borrar la categoría.
     */
    @Override
    public void clickButton() {
        closeDialog();
        fireEvent(new DeleteEvent(this,categoriesBinder.getBean()));
    }

    /**
     * Clase abstracta que extiene de {@link DeleteCategoryConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena la categoria asociada al evento.
     */
    public static  abstract  class DeleteCategoryDialogFormEvent extends ComponentEvent<DeleteCategoryConfirmDialog> {
        private CategoryEntity categoryEntity; //Comunidad con la que trabajamos

        protected  DeleteCategoryDialogFormEvent(DeleteCategoryConfirmDialog source, CategoryEntity categoryEntity){
            super(source, false);
            this.categoryEntity = categoryEntity;
        }

        public CategoryEntity getCategory(){
            return categoryEntity;
        }
    }
    /**
     * Clase heredada de DeleteCategoryConfirmDialog, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece la categoria asociada al evento.
     */
    public static  class DeleteEvent extends DeleteCategoryDialogFormEvent {
        DeleteEvent(DeleteCategoryConfirmDialog source, CategoryEntity categoryEntity){
            super(source, categoryEntity);
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
