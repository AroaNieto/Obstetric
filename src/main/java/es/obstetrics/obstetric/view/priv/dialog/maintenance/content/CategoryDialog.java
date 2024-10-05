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
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.service.CategoryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

/**
 * Clase que extiende de la clase {@link MasterDialog}, se usa para
 * crear o editar categorias.
 */
public class CategoryDialog extends MasterDialog {

    private final Binder<CategoryEntity> categoriesBinder;
    private final TextField name;
    private final TextArea description;
    private final ComboBox<String> state;
    private final DatePicker date;
    private final H5 errorMessage;
    CategoryService categoryService;

    @Autowired
    public CategoryDialog(CategoryEntity category, CategoryService categoryService) {
        this.categoryService = categoryService;
        name = new TextField("Nombre");
        description = new TextArea("Descripción");
        date = new DatePicker();

        state = new ComboBox<>();
        errorMessage = new H5("");

        createHeaderDialog();
        createDialogLayout();

        categoriesBinder = new BeanValidationBinder<>(CategoryEntity.class);
        categoriesBinder.bindInstanceFields(this);
        setCategory(category);
    }

    /**
     * Método utilizado para establecer los valores de los campos de diálogo dependiendo de si
     * se está editando una categoria (category != null) o añadiendo (category == null).
     *
     * @param category categoria
     */
    private void setCategory(CategoryEntity category) {
        clearTextField();

        if (category != null) {
            state.setItems(category.getState());
        }
        categoriesBinder.setBean(category);  //Recojo la categoria
        categoriesBinder.readBean(category);
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     * al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     * que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        clearTextField();
        setErrorMessage("");
        setCategory(null);
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo una nueva categoria o editando.
     * - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     * - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {
        if (categoriesBinder.getBean() == null) { //Si se está añadiendo una nueva categoría
            CategoryEntity categoryEntity = new CategoryEntity();
            setValues();
            try {
                categoriesBinder.writeBean(categoryEntity);

                if (categoryService.findOneByName(categoryEntity.getName()) != null) {
                    setErrorMessage("La categoría ya existe.");
                    return;
                }
                fireEvent(new SaveEvent(this, categoryEntity));
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                categoriesBinder.writeBean(categoriesBinder.getBean());
                CategoryEntity oldCategory = categoryService.findOneByName(categoriesBinder.getBean().getName());
                //Se comprueba si ya existe una categoría con ese nombre
                if (oldCategory != null &&
                        !oldCategory.getId().equals(categoriesBinder.getBean().getId())
                        && oldCategory.getName().equals(categoriesBinder.getBean().getName())) {
                    setErrorMessage("La categoría ya existe.");
                    return;
                }
                fireEvent(new SaveEvent(this, categoriesBinder.getBean()));

            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
        close();
        setErrorMessage("");
        clearTextField();
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
        categoriesBinder.readBean(null);
    }

    /**
     * Establece los valores de estado y fecha antes
     * de guardar una nueva categoria.
     */
    public void setValues() {
        date.setValue(LocalDate.now());
        state.setItems(ConstantUtilities.STATE_ACTIVE);
        state.setValue(ConstantUtilities.STATE_ACTIVE);
    }


    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo de categoria
     * con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {

        name.setErrorMessage("Este campo es obligatorio.");

        description.setMinHeight("120px");
        description.setMaxHeight("120px");
        description.setErrorMessage("Este campo es obligatorio.");

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el anccho
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(name, description, errorMessage);
    }

    /**
     * Clase abstracta que extiene de {@link CategoryDialog}, evento ocurrido en dicha clase.
     * Almacena la categoría asociado al evento.
     */
    @Getter
    public static abstract class CategoriesFormEvent extends ComponentEvent<CategoryDialog> {
        private final CategoryEntity categoryEntity; //Categoría con la que se trabaja

        protected CategoriesFormEvent(CategoryDialog source, CategoryEntity categoryEntity) {
            super(source, false);
            this.categoryEntity = categoryEntity;
        }
    }

    /**
     * Clase heredada de CategoriesFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     * Tiene un constructor que llama al constructor de la super clase y establece la categoria asociado al evento.
     */
    public static class SaveEvent extends CategoriesFormEvent {
        SaveEvent(CategoryDialog source, CategoryEntity categoryEntity) {
            super(source, categoryEntity);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que maneajrá el evento.
     * @param <T> Clase
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}