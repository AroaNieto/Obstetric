package es.obstetrics.obstetric.view.priv.views.maintenance.content;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import es.obstetrics.obstetric.listings.pdf.NewslettesGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.DeleteContentConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.content.NewsletterDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * Vista dónde se mostrarán la tabla con los contenidos.
 *  Solo podrán acceder los trabajadores, los pacientes no.
 */
@Route(value = "sanitaries/newsletters", layout = PrincipalView.class)
@PageTitle("patients")
@PermitAll
public class NewsletterGridView extends MasterGrid<NewsletterEntity> {

    private final NewsletterService newsletterService;
    private final CategoryService categoryService;
    private final SubcategoryService subcategoryService;
    private Button deleteBtn;
    private Button reactivateBtn;

    @Autowired
    public NewsletterGridView(NewsletterService newsletterService,
                              CategoryService categoryService,
                              SubcategoryService subcategoryService){
        this.newsletterService = newsletterService;
        this.categoryService = categoryService;
        this.subcategoryService = subcategoryService;

        setHeader(new H2("NEWSLETTERS"));
        setFilterContainer();
        setGrid();
        updateGrid();
    }


    /**
     * Elimina el contenido.
     *
     * @param deleteEvent contenido a eliminar
     */
    private void unsubscribeNewsletter(DeleteContentConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getContent().setState(ConstantUtilities.STATE_INACTIVE);
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        newsletterService.save(deleteEvent.getContent());
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
        updateGrid();
    }

    /**
     * Actualización del grid de contenidos.
     */
    @Override
    public void updateGrid() {
        masterGrid.setItems(newsletterService.findAll());
    }

    /**
     * Abre el cuadro de diálogo dónde se editará el contenido, se le
     *  pasa todas las categorias y sucategorias.
     */
    @Override
    public void openDialog() {
        NewsletterDialog newsletterDialog = new NewsletterDialog(null, null, categoryService.findAll(), subcategoryService.findAll(), categoryService);
        newsletterDialog.setHeaderTitle("AÑADIR NEWSLETTER");
        newsletterDialog.addListener(NewsletterDialog.SaveEvent.class, this::saveContent);
        newsletterDialog.open();
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     *  si está seguro de eliminar el contenido.
     * @param newsletterEntity contenido que se va a eliminar.
     */
    private void openUnsubscribeDialog(NewsletterEntity newsletterEntity) {
        DeleteContentConfirmDialog deleteContentConfirmDialog = new DeleteContentConfirmDialog(newsletterEntity);
        deleteContentConfirmDialog.addListener(DeleteContentConfirmDialog.DeleteEvent.class, this::unsubscribeNewsletter);
        deleteContentConfirmDialog.open();
    }

    /**
     * Llama al evento que abre el cuadro de diálogo dónde se editará el contenido
     *      Establece la cabecera y el contenido.
     */
    private void openEditContentDialog(NewsletterEntity newsletterEntity) {
        NewsletterDialog newsletterDialog = new NewsletterDialog(newsletterEntity, null, categoryService.findAll(), subcategoryService.findAll(), categoryService);
        newsletterDialog.setHeaderTitle("MODIFICAR NEWSLETTER");
        newsletterDialog.addListener(NewsletterDialog.SaveEvent.class, this::saveContent);
        newsletterDialog.open();
    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsquedas en el grid
     */
    @Override
    public void setFilterContainer() {
        gridListDataView = masterGrid.setItems(newsletterService.findAll()); //Configuración del DataView

        searchTextField.setTooltipText("Escriba la newsletter que desea buscar.");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto

        searchTextField.addValueChangeListener(event -> {
            gridListDataView.addFilter(contentEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                String search = searchTextField.getValue().trim();
                if (search.isEmpty()) return true;

                boolean name = isIdentical(contentEntity.getName(), search);
                boolean typeContent = isIdentical(contentEntity.getTypeContent(), search);

                return name || typeContent;
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });

        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE),"help-button","Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTER_HELP_CONTENT,
                    "Guía mantenimiento de newsletters");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT),"help-button","Imprimir listado");
        printButton.addClickListener(event-> printButton());

        addBtn.setTooltipText("Añadir newsletter");

        filterContainerHl.add(searchTextField,
                createStartDate(),
                createCategoryCombobox(),
                createSubcategoryCombobox(),
                addBtn,printButton, helpButton);

        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }
    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("newsletters.pdf", () -> new NewslettesGridPdf((ArrayList<NewsletterEntity>) newsletterService.findAll()).generatePdf());

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de newsletters");
        dialog.open();
    }


    private Button createButton(Icon icon, String className, String tooltip){
        Button button = new Button(icon);
        button.setTooltipText(tooltip);
        button.addClassName(className);
        return button;
    }

    /**
     * Creación de los dos comboBox: categorías y subcategorias para
     *  la aplicación de filtros ne le grid.
     */
    private ComboBox<CategoryEntity> createCategoryCombobox() {
        ComboBox<CategoryEntity> categories = new ComboBox<>();
        categories.setLabel("Categoría");
        categories.addClassName("text-field-1300");
        categories.setItems(categoryService.findAll());
        categories.addValueChangeListener(event -> {
            gridListDataView.addFilter(newsletterEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                if (categories.getValue() == null|| categories.getValue().getName().trim().isEmpty()  ) return true;
                return isIdentical(newsletterEntity.getSubcategoryEntity().getCategoryEntity().getName(),  categories.getValue().getName().trim());
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        return categories;
    }

    /**
     * Creación de los dos comboBox: categorías y subcategorias para
     *  la aplicación de filtros ne le grid.
     */

    private ComboBox<SubcategoryEntity> createSubcategoryCombobox() {
        ComboBox<SubcategoryEntity> subcategories = new ComboBox<>();
        subcategories.setLabel("Subcategoría");
        subcategories.addClassName("text-field-1300");

        subcategories.addValueChangeListener(event -> {
            gridListDataView.addFilter(newsletterEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                if (subcategories.getValue()==null ||subcategories.getValue().getName().trim().isEmpty() || subcategories.getValue().getName() == null) return true;
                return isIdentical(newsletterEntity.getSubcategoryEntity().getName(), subcategories.getValue().getName().trim());
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        subcategories.setItems(subcategoryService.findAll());

        return subcategories;
    }

    /**
     * Creción de la fecha de inicio y la de fin.
     * @return El hl dónde se encuentran ambas fechas.
     */
    private DatePicker createStartDate() {
        DatePicker startDate = new DatePickerTemplate("Fecha de inicio");
        startDate.addClassName("text-field-1300");
        startDate.addValueChangeListener(event -> {
            gridListDataView.addFilter(newsletterEntity -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                if (startDate.getValue() == null || startDate.getValue().toString().trim().isEmpty()|| newsletterEntity.getStartDate()==null) return true;
                String search = startDate.getValue().toString().trim();

                return isIdentical(newsletterEntity.getStartDate().toString(), search);
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });
        return startDate;
    }

    /**
     * Verifica si la cadena que está escribiendo el usuario mediante el textfield está contenida
     *  dentro del nombre de la propiedad.
     */
    private boolean isIdentical(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    /**
     * Creación del grid de contenidos con sus columnas correspondientes.
     */
    @Override
    public void setGrid() {

        masterGrid.addColumn(NewsletterEntity::getName).setHeader("Titulo").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NewsletterEntity::getTypeContent).setHeader("Contenido").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NewsletterEntity::getQuarter).setHeader("Trimestre").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(contentEntity ->
                contentEntity.getSubcategoryEntity().getCategoryEntity()
        ).setHeader("Categoría").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(NewsletterEntity::getSubcategoryEntity).setHeader("Subcategoria").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NewsletterEntity::getDuration).setHeader("Duración").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(newsletterEntity -> newsletterEntity.getDate().getDayOfMonth()+"/"+newsletterEntity.getDate().getMonthValue()+"/"+newsletterEntity.getDate().getYear()).setHeader("Fecha de inicio").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(newsletterEntity -> newsletterEntity.getDate().getDayOfMonth()+"/"+newsletterEntity.getDate().getMonthValue()+"/"+newsletterEntity.getDate().getYear()).setHeader("Fecha de fin").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NewsletterEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(new ComponentRenderer<>(contentEntity -> {
            Button editBtn = createButton(new Icon(VaadinIcon.EDIT),"dark-green-background-button","Editar");
            editBtn.addClickListener(event -> openEditContentDialog(contentEntity));

            reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH),"yellow-color-button","Refrescar");
            reactivateBtn.setTooltipText("Reactivar");
            reactivateBtn.addClickListener(event -> openReactivateNewsletter(contentEntity));

            if(contentEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)){
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-disable-background-button","Dar de baja");
                deleteBtn.setVisible(false);
                deleteBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteBtn,reactivateBtn);
            }

            reactivateBtn.setVisible(false);
            deleteBtn = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Dar de baja");
            deleteBtn.addClickListener(event -> openUnsubscribeDialog(contentEntity));

            return new HorizontalLayout(editBtn, deleteBtn,reactivateBtn);

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private void openReactivateNewsletter(NewsletterEntity contentEntity) {
        contentEntity.setState(ConstantUtilities.STATE_ACTIVE);
        newsletterService.save(contentEntity);
        deleteBtn.setVisible(true);
        reactivateBtn.setVisible(false);
        updateGrid();
    }
}