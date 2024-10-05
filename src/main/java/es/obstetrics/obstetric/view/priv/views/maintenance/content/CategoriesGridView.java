package es.obstetrics.obstetric.view.priv.views.maintenance.content;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.service.CategoryService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.SubcategoryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.CategoriesGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.DeleteCategoryConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.DeleteContentConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.DeleteSubcategoryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.content.CategoryDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.content.NewsletterDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.content.SubcategoryDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * Vista dónde se mostrarán la tabla con los categorias.
 *  Solo podrán acceder los trabajadores, los pacientes no.
 */
//@PermitAll
//@Route(value = "", layout = PrincipalView.class)
@Route(value = "sanitaries/categories-subcategories-newsletters", layout = PrincipalView.class)
@PageTitle("patients")
@PermitAll
public class CategoriesGridView extends MasterGrid<CategoryEntity> {

    private final CategoryService categoryService;
    private final NewsletterService newsletterService;
    private Button deleteBtnNewsletter;
    private Button reactivateBtnNewsletter;
    private Button deleteBtnCategory;
    private Button reactivateBtnCategory;
    private Button deleteBtnSubcategory;
    private Button reactivateSubcategory;
    private final SubcategoryService subcategoryService;
    private Grid<SubcategoryEntity> subcategoryEntityGrid;
    private Grid<NewsletterEntity> contentEntityGrid;

    @Autowired
    public CategoriesGridView(CategoryService categoryService,
                              SubcategoryService subcategoryService,
                              NewsletterService newsletterService) {

        this.categoryService = categoryService;
        this.subcategoryService = subcategoryService;
        this.newsletterService = newsletterService;

        setHeader(new H2("CATEGORÍAS"));

        setGrid();
        setSubcategoryGrid();
        setContentGrid();
        setFilterContainer();
        updateGrid();
    }

    /**
     * Crea el grid de contenidos con sus columnas correspondientes, este se establece cuando se selecciona una
     *  subcategoria.
     */
    private void setContentGrid() {

        contentEntityGrid = new Grid<>();
        contentEntityGrid.addColumn(NewsletterEntity::getName).setAutoWidth(true);
        contentEntityGrid.addColumn(newsletterEntity -> "Estado: "+ newsletterEntity.getState()).setAutoWidth(true);
        contentEntityGrid.addColumn(new ComponentRenderer<>(contentEntity -> {
            deleteBtnNewsletter = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Añadir");
            Button editBtn = createButton(new Icon(VaadinIcon.EDIT),"dark-green-background-button","Editar");
            editBtn.addClickListener(event -> openEditContentDialog(contentEntity));

            reactivateBtnNewsletter = createButton(new Icon(VaadinIcon.REFRESH),"yellow-color-button","Reactivar newsletter");
            reactivateBtnNewsletter.addClickListener(event -> openReactivateNewsletter(contentEntity));

            if(contentEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                deleteBtnNewsletter = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-disable-background-button","Dar de baja newsletter");
                deleteBtnNewsletter.setVisible(false);
                deleteBtnNewsletter.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteBtnNewsletter,reactivateBtnNewsletter);
            }

            reactivateBtnNewsletter.setVisible(false);
            deleteBtnNewsletter = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Dar de baja newsletter");
            deleteBtnNewsletter.addClickListener(event -> openDeleteContentDialog(contentEntity));

            return new HorizontalLayout(editBtn, deleteBtnNewsletter,reactivateBtnNewsletter);

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);

        contentEntityGrid.setAllRowsVisible(true); //Ajuste del grid dependiendo de las filas
        contentEntityGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER); //Quitar el borde del grid
    }

    private void openReactivateNewsletter(NewsletterEntity contentEntity) {
        contentEntity.setState(ConstantUtilities.STATE_ACTIVE);
        newsletterService.save(contentEntity);
        deleteBtnCategory.setVisible(true);
        reactivateBtnNewsletter.setVisible(false);
        subcategoryService.save(contentEntity.getSubcategoryEntity());
        updateGrid();
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     *  si está seguro de eliminar la categoria.
     * @param categoryEntity Categoria que se va a eliminar.
     */
    private void openUnsubscribeDialog(CategoryEntity categoryEntity) {
        DeleteCategoryConfirmDialog deleteCategoryConfirmDialog = new DeleteCategoryConfirmDialog(categoryEntity);
        deleteCategoryConfirmDialog.open();
        deleteCategoryConfirmDialog.addListener(DeleteCategoryConfirmDialog.DeleteEvent.class, this::deleteCategory);
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     *  si está seguro de eliminar la subcategoria.
     * @param subcategoryEntity subcategoria que se va a eliminar.
     */
    private void openDeleteSubcategoryDialog(SubcategoryEntity subcategoryEntity) {
        DeleteSubcategoryConfirmDialog deleteSubcategoryConfirmDialog = new DeleteSubcategoryConfirmDialog(subcategoryEntity);
        deleteSubcategoryConfirmDialog.addListener(DeleteSubcategoryConfirmDialog.DeleteEvent.class, this::UnsubscribeSubcategory);
        deleteSubcategoryConfirmDialog.open();
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     *  si está seguro de eliminar el contenido.
     * @param newsletterEntity contenido que se va a eliminar.
     */
    private void openDeleteContentDialog(NewsletterEntity newsletterEntity) {
        DeleteContentConfirmDialog deleteContentConfirmDialog = new DeleteContentConfirmDialog(newsletterEntity);
        deleteContentConfirmDialog.addListener(DeleteContentConfirmDialog.DeleteEvent.class, this::deleteContent);
        deleteContentConfirmDialog.open();
    }

    /**
     * Abre el cuadro de diálogo de añadir la categoria, como está añadiendo
     *  pone la categoria a nulo.
     */
    @Override
    public void openDialog() {
        CategoryDialog categoryDialog = new CategoryDialog(null, categoryService);
        categoryDialog.addListener(CategoryDialog.SaveEvent.class, this::saveCategory);
        categoryDialog.setHeaderTitle("AÑADIR CATEGORÍA");
        categoryDialog.open();
    }


    /**
     * Abre el cuadro de diálogo dónde se editará la categoria
     */
    private void openEditCategoryDialog(CategoryEntity categoryEntity) {
        CategoryDialog categoryDialog = new CategoryDialog(categoryEntity, categoryService);
        categoryDialog.addListener(CategoryDialog.SaveEvent.class, this::saveCategory);
        categoryDialog.setHeaderTitle("MODIFICAR CATEGORÍA");
        categoryDialog.open();
    }

    /**
     * Abre el cuadro de diálogo dónde se editará la subcategoria
     */
    private void openEditSubcategoryDialog(SubcategoryEntity subcategoryEntity) {
        SubcategoryDialog subcategoryDialog = new SubcategoryDialog(subcategoryEntity, null, subcategoryService);
        subcategoryDialog.addListener(SubcategoryDialog.SaveEvent.class, this::saveSubcategory);
        subcategoryDialog.setHeaderTitle("MODIFICAR SUBCATEGORIA");
        subcategoryDialog.open();
    }

    /**
     * Abre el cuadro de diálogo dónde se editará el contenido
     */
    private void openEditContentDialog(NewsletterEntity newsletterEntity) {
        NewsletterDialog newsletterDialog = new NewsletterDialog(newsletterEntity, null,
                categoryService.findAll(), subcategoryService.findAll(), categoryService);
        newsletterDialog.setHeaderTitle("MODIFICAR NEWSLETTER");
        newsletterDialog.addListener(NewsletterDialog.SaveEvent.class, this::saveContent);

        newsletterDialog.open();
    }


    /**
     * Abre el cuadro de diálogo dónde se añadir la subcategoria,
     *  Se le pasa la categoria asociada.
     */
    private void openSubcategoryDialog(CategoryEntity categoryEntity) {
        SubcategoryDialog subcategoryDialog = new SubcategoryDialog(null, categoryEntity, subcategoryService);
        subcategoryDialog.setHeaderTitle("AÑADIR SUBCATEGORIA");
        subcategoryDialog.addListener(SubcategoryDialog.SaveEvent.class, this::saveSubcategory);
        subcategoryDialog.open();
    }

    /**
     * Abre el cuadro de diálogo dónde se editará el contenido, se le
     *  pasa la subcategoria asociada y todas las categorias y sucategorias.
     */
    private void openContentDialog(SubcategoryEntity subcategoryEntity) {
        NewsletterDialog newsletterDialog = new NewsletterDialog(null, subcategoryEntity, categoryService.findAll(), subcategoryService.findAll(),
                categoryService);
        newsletterDialog.setHeaderTitle("AÑADIR NEWSLETTER");
        newsletterDialog.addListener(NewsletterDialog.SaveEvent.class, this::saveContent);
        newsletterDialog.open();
    }

    /**
     * Actualización del grid de categorias.
     */
    @Override
    public void updateGrid() {
        masterGrid.setItems(categoryService.findAll());
    }
    /**
     * Actualización del grid de subcategorias.
     */
    private void updateSubCategoriesGrid(CategoryEntity category) {
        subcategoryEntityGrid.setItems(subcategoryService.findByCategoryEntity(category));
    }

    /**
     * Actualización de contenidos.
     */
    private void updateContentGrid(SubcategoryEntity subcategoryEntity) {
        contentEntityGrid.setItems(newsletterService.findBySubcategory(subcategoryEntity));
    }

    private Button createButton (Icon icon, String className, String tooltip){
        Button button = new Button();
        button.setIcon(icon);
        button.addClassName(className);
        button.setTooltipText(tooltip);
        return button;
    }

    /**
     * Creación del grid de subcategorias con las columnas correspondientes.
     */
    private void setSubcategoryGrid() {

        subcategoryEntityGrid = new Grid<>();
        subcategoryEntityGrid.addColumn(new ComponentRenderer<>(subcategoryEntity -> {
            Button angleButton = createButton(new Icon(VaadinIcon.ANGLE_RIGHT),"angle-right-button","");
            return new HorizontalLayout(angleButton);
        })).setAutoWidth(true).setFrozen(true).setFlexGrow(0);

        subcategoryEntityGrid.addColumn(SubcategoryEntity::getName).setAutoWidth(true);
        subcategoryEntityGrid.addColumn(SubcategoryEntity::getDescription).setAutoWidth(true);
        subcategoryEntityGrid.addColumn(SubcategoryEntity::getState).setAutoWidth(true);
        subcategoryEntityGrid.addColumn(new ComponentRenderer<>(subcategoryEntity -> {
            Button addBtn =createButton(new Icon(VaadinIcon.PLUS),"lumo-primary-color-background-button","Añadir newsletter" );
            addBtn.addClickListener(event -> openContentDialog(subcategoryEntity));

            Button editBtn = createButton(new Icon(VaadinIcon.EDIT),"dark-green-background-button","Editar subcategoría" );
            editBtn.addClickListener(event -> openEditSubcategoryDialog(subcategoryEntity));

            reactivateSubcategory = createButton(new Icon(VaadinIcon.REFRESH),"yellow-color-button","Reactivar subcategoría" );
            reactivateSubcategory.setTooltipText("Reactivar subcategoría");
            reactivateSubcategory.addClickListener(event -> openReactivateSubcategory(subcategoryEntity));

            if(subcategoryEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                deleteBtnSubcategory = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-disable-background-button","Dar de baja subcategoría");
                deleteBtnSubcategory.setVisible(false);
                deleteBtnSubcategory.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteBtnSubcategory,reactivateSubcategory);
            }

            reactivateSubcategory.setVisible(false);
            deleteBtnSubcategory = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Dar de baja subcategoría");
            deleteBtnSubcategory.setVisible(false);
            deleteBtnSubcategory.addClickListener(event -> openDeleteSubcategoryDialog(subcategoryEntity));

            return new HorizontalLayout(addBtn, editBtn, deleteBtnSubcategory);
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);

        subcategoryEntityGrid.setAllRowsVisible(true); //Ajuste del grid dependiendo de las filas
        subcategoryEntityGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER); //Quitar el borde del grid
        subcategoryEntityGrid.setItemDetailsRenderer(createContentEntityGrid()); //Creación del panel de detalles al pulsar el grid
        /*
            Evento encargado de ocultar el panel de detalles en el caso que no haya
                detalles que mostrar.
         */
        subcategoryEntityGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                if (event.getItem().getContentEntities().isEmpty()) {
                    subcategoryEntityGrid.setDetailsVisible(event.getItem(), false);
                }
            }
        });
    }

    private void openReactivateSubcategory(SubcategoryEntity subcategoryEntity) {
        subcategoryEntity.setState(ConstantUtilities.STATE_ACTIVE);
        deleteBtnSubcategory.setVisible(true);
        reactivateSubcategory.setVisible(false);
        subcategoryService.save(subcategoryEntity);
        updateSubCategoriesGrid(subcategoryEntity.getCategoryEntity());
    }

    /**
     * Crea un renderer que actualiza y muestra el grid de contenidos dentro de un HL.
     * @return El renderer con el grid.
     */
    private ComponentRenderer<Component, SubcategoryEntity> createContentEntityGrid() {
        return new ComponentRenderer<>(subcategoryEntity -> {
            if (subcategoryEntity.getContentEntities().isEmpty()) { //SI aún no existen subcategorías no se muestra nada
                return null;
            } else {
                updateContentGrid(subcategoryEntity);
                return new HorizontalLayout(contentEntityGrid);
            }
        });
    }

    /**
     * Crea un renderer que actualiza y muestra el grid de subcategorias dentro de un HL.
     * @return El renderer con el grid.
     */
    private ComponentRenderer<Component, CategoryEntity> createSubcategoryGrid() {
        return new ComponentRenderer<>(categoryEntity -> {
            updateSubCategoriesGrid(categoryEntity);
            return new HorizontalLayout(subcategoryEntityGrid);
        });
    }

    /**
     * Guarda o actualiza la nueva categoría
     */
    private void saveCategory(CategoryDialog.SaveEvent saveEvent) {
        categoryService.save(saveEvent.getCategoryEntity());
        updateGrid();
    }

    /**
     * Añade o edita la subcategoria.
     * Antes de hacerlo, comprueba si existe ya una subcategoría con el mismo
     *  nombre.
     */
    private void saveSubcategory(SubcategoryDialog.SaveEvent saveEvent) {
        subcategoryService.save(saveEvent.getSubcategory());

        updateSubCategoriesGrid(saveEvent.getSubcategory().getCategoryEntity());
        updateGrid();

    }

    /**
     * Guarda el contenido nuevo o editado
     *  - Verifica si la URL es válida.
     *  - Verifica si está editando
     *      - Si es así, verifica que se han introducido los datos correctamente y si es así, actualiza el contenido.
     *      - Si no es así, verifica que se han introducido los datos correctamente y si es así, añade el contenido.
     */
    private void saveContent(NewsletterDialog.SaveEvent saveEvent) {
        newsletterService.save(saveEvent.getContent());
        subcategoryService.save(saveEvent.getContent().getSubcategoryEntity());
        updateGrid();
    }

    /**
     * Elimina la categoria.
     *
     * @param deleteEvent categoria a eliminar
     */
    private void deleteCategory(DeleteCategoryConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getCategory().setState(ConstantUtilities.STATE_INACTIVE);
        deleteBtnCategory.setVisible(false);
        reactivateBtnCategory.setVisible(true);
        categoryService.save(deleteEvent.getCategory());
        updateGrid();
    }

    /**
     * Elimina la subcategoria.
     *
     * @param deleteEvent subcategoria a eliminar
     */
    private void UnsubscribeSubcategory(DeleteSubcategoryConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getSubcategoryEntity().setState(ConstantUtilities.STATE_INACTIVE);
        deleteBtnSubcategory.setVisible(false);
        reactivateSubcategory.setVisible(true);
        subcategoryService.save(deleteEvent.getSubcategoryEntity());
        updateSubCategoriesGrid(deleteEvent.getSubcategoryEntity().getCategoryEntity());
        updateGrid();
    }

    /**
     * Elimina el contenido.
     *
     * @param deleteEvent contenido a eliminar
     */
    private void deleteContent(DeleteContentConfirmDialog.DeleteEvent deleteEvent) {
        //subcategoryService.deleteContent(deleteEvent.getContent().getSubcategoryEntity(), deleteEvent.getContent());
        deleteEvent.getContent().setState(ConstantUtilities.STATE_INACTIVE);
        deleteBtnNewsletter.setVisible(false);
        reactivateBtnNewsletter.setVisible(true);
        newsletterService.save(deleteEvent.getContent());

        subcategoryService.save(deleteEvent.getContent().getSubcategoryEntity());
        updateGrid();
    }

    /**
     * Creación del grid de categorias con sus columnas correspondientes.
     */
    @Override
    public void setGrid() {
        masterGrid.addColumn(new ComponentRenderer<>(categoryEntity -> {
            Button angleButton = createButton(new Icon(VaadinIcon.ANGLE_RIGHT),"angle-right-button","");
            return new HorizontalLayout(angleButton);
        })).setAutoWidth(true).setFlexGrow(0);
        masterGrid.addColumn(CategoryEntity::getName).setHeader("Nombre").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(CategoryEntity::getDescription).setHeader("Descripción").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(CategoryEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(new ComponentRenderer<>(categoryEntity -> {
            Button addBtn = createButton(new Icon(VaadinIcon.PLUS),"lumo-primary-color-background-button","Añadir subcategoría");
            addBtn.addClickListener(event -> openSubcategoryDialog(categoryEntity));

            Button editBtn =  createButton(new Icon(VaadinIcon.EDIT),"dark-green-background-button","Editar categoría");
            editBtn.addClickListener(event -> openEditCategoryDialog(categoryEntity));
            reactivateBtnCategory = createButton(new Icon(VaadinIcon.REFRESH),"yellow-color-button","Reactivar categoría");
            reactivateBtnCategory.setTooltipText("Reactivar categoria");
            reactivateBtnCategory.addClickListener(event -> openReactivateCategory(categoryEntity));

            if(categoryEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                deleteBtnCategory = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-disable-background-button","Dar de baja categoría");
                deleteBtnCategory.setVisible(false);
                deleteBtnCategory.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteBtnCategory,reactivateBtnCategory);
            }

            reactivateBtnCategory.setVisible(false);
            deleteBtnCategory =  createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Dar de baja categoría");
            deleteBtnCategory.addClickListener(event -> openUnsubscribeDialog(categoryEntity));

            return new HorizontalLayout(addBtn, editBtn, deleteBtnCategory);

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
        masterGrid.setItemDetailsRenderer(createSubcategoryGrid()); //Creación del panel de detalles al pulsar el grid

        /*
            Evento encargado de ocultar el panel de detalles en el caso que no haya
                detalles que mostrar.
         */
        masterGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                if (event.getItem().getSubcategories().isEmpty()) {
                    masterGrid.setDetailsVisible(event.getItem(), false);
                }
            }
        });
    }

    private void openReactivateCategory(CategoryEntity categoryEntity) {
        categoryEntity.setState(ConstantUtilities.STATE_ACTIVE);
        categoryService.save(categoryEntity);
        deleteBtnCategory.setVisible(true);
        reactivateBtnCategory.setVisible(false);
        updateGrid();
    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsqeudas en el grid
     */
    @Override
    public void setFilterContainer() {

        gridListDataView = masterGrid.setItems(categoryService.findAll()); //Configuración del DataView

        searchTextField.setTooltipText("Escriba la categoría que desea buscar.");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto

        searchTextField.addValueChangeListener(event -> {
            gridListDataView.addFilter(categoryEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                String search = searchTextField.getValue().trim();
                if (search.isEmpty()) return true;

                boolean name = isIdentical(categoryEntity.getName(), search);
                boolean description = isIdentical(categoryEntity.getDescription(), search);

                return name || description;
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });

        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE),"help-button","Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTER_HELP_CONTENT,
                    "Guía mantenimiento de categorías");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT),"help-button","Imprimir listado");
        printButton.addClickListener(event-> printButton());

        addBtn.setTooltipText("Añadir categoría");
        searchTextField.setPlaceholder("Buscar categoría");
        filterContainerHl.add(searchTextField, createRadioButton(),addBtn,printButton, helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField, createRadioButton());

        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("categorias.pdf", () -> new CategoriesGridPdf((ArrayList<CategoryEntity>) categoryService.findAll()).generatePdf());

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de categorias");
        dialog.open();
    }

    /**
     * Verifica si la cadena que está escribiendo el usuario mediante el textfield está contenida
     *  dentro del nombre de la propiedad.
     */
    private boolean isIdentical(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    /**
     * Crea el combobox dónde se aplicarán los filtros del estado
     *  en el grid.
     * @return El radioButton correspondiente.
     */
    private RadioButtonGroup<String> createRadioButton() {
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Estado");
        radioGroup.addClassName("text-field-1100");
        radioGroup.setItems(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE);
        radioGroup.addValueChangeListener(event -> {
            gridListDataView.addFilter(categoryEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                String search = radioGroup.getValue().trim();
                if (search.isEmpty()) return true;
                return isIdentical(categoryEntity.getState(), search);
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        return radioGroup;
    }

}