package es.obstetrics.obstetric.view.priv.dialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.service.SubcategoryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Clase que extiende de la clase {@link MasterDialog}, se usa para
 *  crear o editar subcategorías.
 */
@UIScope
@Component
public class SubcategoryDialog extends MasterDialog {

    private final Binder<SubcategoryEntity> subcategoriesBinder;
    private final TextField name;
    private final TextArea description;
    private final ComboBox<String> state;
    private final ComboBox<CategoryEntity> categoryEntity;
    private final DatePicker date;
    private final H5 errorMessage;
    private SubcategoryEntity subcategoryEntity;
    private SubcategoryService subcategoryService;

    /**
     * Constructor de la clase, se encarga de inicializar los componentes que aparecerán en el
     *      cuadro de dialogo y se estableceran en el Binder, configurar el enlace de datos
     *      mediante el BeanValidationBinder y establecer los valores.
     */
    public SubcategoryDialog(SubcategoryEntity subcategory, CategoryEntity category, SubcategoryService subcategoryService){

        this.subcategoryService = subcategoryService;
        name = new TextField("Nombre");
        categoryEntity = new ComboBox<>("Categoría");
        description= new TextArea("Descripción");
        date = new DatePicker();
        state = new ComboBox<>("Estado");
        errorMessage = new H5("");

        createHeaderDialog();
        createDialogLayout();

        subcategoriesBinder = new BeanValidationBinder<>(SubcategoryEntity.class);
        subcategoriesBinder.bindInstanceFields(this);
        setSubcategory(subcategory, category);
    }

    /**
     * Método utilizado para establecer los valores de los campos de diálogo dependiendo de si
     *  se está editando una categoria (subcategory != null) o añadiendo (subcategory == null).
     *
     * @param subcategory Subcategoria
     * @param category Categoria asociada a la nueva subcategoria que se acabe de añadir.
     */
    private void setSubcategory(SubcategoryEntity subcategory, CategoryEntity category){
        if(subcategory != null) {
            state.setItems(subcategory.getState());
            updateSubcategory(subcategory);
        }else{
            subcategoryEntity = new SubcategoryEntity();
            subcategoryEntity.setCategoryEntity(category);
            updateSubcategory(subcategoryEntity);
        }
    }

    /**
     * Añade los items a la categoria asociada a la subcategoria y recoge la subcategoria del binder.
     * @param subcategory Subcategoria sobre la que se van a realizar las operaciones.
     *
     */
    public void updateSubcategory(SubcategoryEntity subcategory){
        categoryEntity.setItems(subcategory.getCategoryEntity());
        categoryEntity.setValue(subcategory.getCategoryEntity());
        categoryEntity.setReadOnly(true);
        subcategoriesBinder.setBean(subcategory);  //Recojo la categoria
        subcategoriesBinder.readBean(subcategory);
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
        description.clear();
        date.clear();
        state.clear();
        subcategoriesBinder.readBean(null);
    }

    /**
     * Establece los valores de estado y fecha antes
     *  de guardar una nueva subcategoria.
     */
    public  void setValues() {
        date.setValue(LocalDate.now());
        state.setItems(ConstantUtilities.STATE_ACTIVE);
        state.setValue(ConstantUtilities.STATE_ACTIVE);
    }


    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public  void setErrorMessage(String message){
        errorMessage.setText(message);
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        setHeaderTitle("AÑADIR SUBCATEGORÍA");
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
        setSubcategory(null, null);
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo una nueva subcategoría o editando.
     *      - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     *      - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {
        if(subcategoriesBinder.getBean().getId() == null){ //Si se está añadiendo una nueva subcategoría;
            setValues();

            try {
                subcategoriesBinder.writeBean(subcategoryEntity);
                if (subcategoryService.findByName(subcategoriesBinder.getBean()) != null) {
                    setErrorMessage("La subcategoria ya existe.");
                    return;
                }
                fireEvent(new SaveEvent(this, subcategoryEntity));
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }else{
            try {
                subcategoriesBinder.writeBean(subcategoriesBinder.getBean());
                SubcategoryEntity oldCategory = subcategoryService.findByName(subcategoriesBinder.getBean());
                //Se comprueba si ya existe una categoría con ese nombre
                if (oldCategory != null &&
                        !oldCategory.getId().equals(subcategoriesBinder.getBean().getId())
                        &&  oldCategory.getName().equals(subcategoriesBinder.getBean().getName())) {
                    setErrorMessage("La categoría ya existe.");
                    return;
                }
                fireEvent(new SaveEvent(this, subcategoriesBinder.getBean()));
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
        close();
        setErrorMessage("");

    }

    /**
     * Método que se encarga de configurar el diseño del diálogo de subcategoría
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {
        name.setErrorMessage("Este campo es obligatorio.");

        description.setMinHeight("120px");
        description.setMaxHeight("120px");
        description.setErrorMessage("Este campo es obligatorio.");

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el anccho
        dialogVl.getStyle().set("width", "30rem").set("max-width", "100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(categoryEntity,name, description, errorMessage);
    }

    /**
     * Clase abstracta que extiene de {@link SubcategoryDialog}, evento ocurrido en dicha clase.
     *  Almacena la subcategoria asociada al evento.
     */
    public static abstract class SubcategoriesFormEvent extends ComponentEvent<SubcategoryDialog> {
        private final SubcategoryEntity subcategoryEntity; //Categoría con la que se trabaja

        protected SubcategoriesFormEvent(SubcategoryDialog source, SubcategoryEntity subcategoryEntity) {
            super(source, false);
            this.subcategoryEntity = subcategoryEntity;
        }

        public SubcategoryEntity getSubcategory() {
            return subcategoryEntity;
        }
    }

    /**
     * Clase heredada de SubcategoriesFormEvent, representa un evento de guardado que ocurre en el diálogo de subcategoria,
     *      Tiene un constructor que llama al constructor de la super clase y establece la subcategoria asociada al evento.
     */
    public static class SaveEvent extends SubcategoriesFormEvent {
        SaveEvent(SubcategoryDialog source, SubcategoryEntity subcategoryEntity) {
            super(source, subcategoryEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}