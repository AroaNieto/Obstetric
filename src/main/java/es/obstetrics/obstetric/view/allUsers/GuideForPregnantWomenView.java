package es.obstetrics.obstetric.view.allUsers;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.service.CategoryService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.SubcategoryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.resources.templates.ImgTemplate;
import es.obstetrics.obstetric.view.allUsers.dialog.PdfDialog;
import es.obstetrics.obstetric.view.login.LoginView;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encarga de mostrar los artículos subidos por los diferentes médicos
 *   separados en categogorías/subcategorías y dentro de esta contenidos.
 *   Extiende de {@link HomeDiv}, pudiendo navegar
 *   a la clase  {@link GuideForPregnantWomenView} o {@link LoginView}
 *
 * Puede acceder cualquier tipo de usuario, tanto registrados como no registrados.
 */
@Route(value = "public/articles")
@AnonymousAllowed
public class GuideForPregnantWomenView extends HomeDiv {

    //-----------DECLARACIÓN DE VARIABLES---------------------------
    private final Tabs subCategoryTabs = new Tabs();
    private final Tabs categoryTabs = new Tabs();
    private  List<SubcategoryEntity> allSubcategories;
    private final FormLayout contentFl = new FormLayout();
    private final ArrayList<String> pastelColor = new ArrayList<>();
    private  VerticalLayout containerVl;
    private final CategoryService categoryService;
    private final SubcategoryService subcategoryService;
    private final NewsletterService newsletterService;
    //----------------------------------------------------------------

    /**
     * Constructor de la clase, instancia mediante el Autowired los tres servicios
     *      necesarios para recoger las categorias, subcategorias y contenidos.
     *      Crea el arraylist de colores y llama a los métodos correspondientes para
     *      crear el tab de categorias, subcategorias y contenidos correspondentes.
     */
    @Autowired
    public GuideForPregnantWomenView(CategoryService categoryService, SubcategoryService subcategoryService, NewsletterService newsletterService){
        this.categoryService = categoryService;
        this.subcategoryService = subcategoryService;
        this.newsletterService = newsletterService;

        pastelColor.add("#FFB6C1");
        pastelColor.add("#D8BFD8");
        pastelColor.add("#FFFFE0");
        pastelColor.add("#98FB98");
        pastelColor.add("#ADD8E6");


        addCategories(categoryService.findByState(ConstantUtilities.STATE_ACTIVE));
        addSubcategories();

        createCategoriesTab();
        createSubcategoriesTab();

        add(categoryTabs, subCategoryTabs);
        addNewsletters();
        addClassName("background-gray-color");
        setSizeFull();
    }

    /**
     * Crea los estilos del tab de subcategorias y añade el evento de cuando
     *      el tab esté seleccionado.
     */
    private void createSubcategoriesTab() {
        subCategoryTabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        subCategoryTabs.getStyle().set("background", "var(--light-gray-color)");
        subCategoryTabs.addSelectedChangeListener(event -> addNewsletters());

    }

    /**
     * Crea los estilos del tab de categorias y añade el evento de cuando
     *      el tab esté seleccionado.
     */
    private void createCategoriesTab() {
        categoryTabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        categoryTabs.getStyle().set("background", "var(--medium-gray-color)");
        categoryTabs.addSelectedChangeListener(event -> addSubcategories());
    }

    /**
     * Evento que salta cuando se selecciona una subcategoria dentro de una categorias
     *      -Se elimina todo le contenido de formlayout para volverlo a crear.
     *      -Se recoge la subcategoria sobre la que se va a operar.
     *      -Se crea una lista de contenidos dentro de dicha subcategoria.
     *      - Se comprueba si el contenido es válido, si es así:
     *          - Se recorre la lista de contenidos añadiendo un vertical layout formado por
     *               el título del contenido, la miniatura (PDF o la que ha subido el sanitario)
     *              y el resumen del contenido. Además se le asigna un color del arraylist de colores.
     */
    private void addNewsletters() {
        contentFl.removeAll();
        if(subCategoryTabs.getSelectedTab() != null){
            SubcategoryEntity subcategory = new SubcategoryEntity();
            for(SubcategoryEntity s : allSubcategories){
                if(subCategoryTabs.getSelectedTab().getLabel().equalsIgnoreCase(s.getName())){
                    subcategory = s; //Se recoge la subcategoria sobre la que se va a operar
                    break;
                }
            }

            List<NewsletterEntity> contentEntities = newsletterService.findBySubcategoryEntityAndState(subcategory, ConstantUtilities.STATE_ACTIVE); //Lista de contenidos

            contentFl.setResponsiveSteps( //Diseño responsivo
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("30em", 2),
                    new FormLayout.ResponsiveStep("50em", 3),
                    new FormLayout.ResponsiveStep("60em", 5)
            );

            int i = 0; //Variable sobre la que se va a interar para crear los distintos colores del contenido
            int next = 0;
            for(NewsletterEntity c : contentEntities){ //Se recorren todos los contenidos
                // Comprueba si la newsletter es de duración "solo una vez" y está fuera del rango de fechas
                if(c.getDuration().equals(ConstantUtilities.JUST_ONCE) &&
                        (LocalDate.now().isBefore(c.getStartDate()) || LocalDate.now().isAfter(c.getEndingDate()))){
                    next = 1;
                }else if(c.getDuration().equals(ConstantUtilities.ANNUAL)){  // Comprueba si la newsletter es de duración "anual" y está fuera del rango de fechas
                    boolean isNotDate = LocalDate.now().isEqual(c.getStartDate()) || (LocalDate.now().isAfter(c.getStartDate())
                                             &&
                            (c.getEndingDate() == null || LocalDate.now().isBefore(c.getEndingDate())) || LocalDate.now().isEqual(c.getEndingDate()));

                    if (!isNotDate && (ChronoUnit.YEARS.between(c.getStartDate(), LocalDate.now()) > 1)) {
                       next = 1;
                    }
                }else if(c.getDuration().equals(ConstantUtilities.TIMELESS) && c.getStartDate().isAfter(LocalDate.now())){
                    next = 1;
                }
                if(next == 0){
                    StreamResource resource = createResource(c);
                    if(resource != null){
                        createStyleContainer(c,new ImgTemplate(resource, "120px") , i);
                    }

                    contentFl.add(containerVl);
                    i++;
                    if(i>= pastelColor.size()){ //Si el arraylist de colores ha acabado vuelve a empezar
                        i=0;
                    }
                    contentFl.getStyle().set("margin-left", "50px")
                            .set("margin-right", "50px");
                    add(contentFl);
                }
               next = 0;
            }

        }
    }

    /**
     * Crea el StreamResource que se usará mpara mostrar por pantalla la miniatura,, dependiendo
     *  del tipo, se recoge un byte[] u otro.
     * @param c Contenido sobre el que se creara el stremResource para
     *         mostrar por pantalla el contenido.
     */
    private StreamResource createResource(NewsletterEntity c) {
        if(c.getContentByteUrl() != null){ //Se establece la fotografía dependiendo de su tipo
            return new StreamResource("foto.jpg", () -> new ByteArrayInputStream(c.getContentByteUrl()));
        }else if(c.getContentMiniature() != null){
            return new StreamResource("foto.jpg", () -> new ByteArrayInputStream(c.getContentMiniature()));
        }
        return null;
    }

    /**
     * Creación de los estilos del contenedor y se añaden sus eventos de click correspondientes
     *  dependiendo de si se trata de una URL o un PDF.
     */
    private void createStyleContainer(NewsletterEntity c, HorizontalLayout image, int i) {
        containerVl = new VerticalLayout();
        containerVl.add(new H6(c.getName()), image, new Span(c.getSummary()));
        containerVl.getStyle().set("background-color", pastelColor.get(i))
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("border-radius", "20px")
                .set("font-family", "'Times New Roman', serif"); //Cuando se posiciona sobre él el cursor se activa
        containerVl.setMargin(true);
        containerVl.setPadding(true);
        containerVl.setMaxHeight("300px");
        containerVl.setMinHeight("300px");
        containerVl.addClickListener(e -> {
            if(c.getContentByteUrl() != null){
                getUI().ifPresent(ui -> ui.getPage().executeJs("window.open($0, '_blank')", c.getUrl())); //Si el usuario pulsa en el contenido con URL, se redirige a el
            }else if(c.getContentMiniature() != null){
                PdfDialog pdfDialog = new PdfDialog(c.getContentBytePdf(), c.getName());
                pdfDialog.open(); //Si el usuario desea ver el pdf, se abre el cuadro de diálogo
            }
        });
    }

    /**
     * Añade las subcategorias dependiendo de la categoria seleciconada.
     */
    private void addSubcategories() {
        contentFl.removeAll();
        subCategoryTabs.removeAll();
        if(categoryTabs.getSelectedTab() != null){
            CategoryEntity categoryName  = categoryService.findOneByNameAndState(categoryTabs.getSelectedTab().getLabel(),ConstantUtilities.STATE_ACTIVE);
            allSubcategories = subcategoryService.findByCategoryEntityAndState(categoryName,ConstantUtilities.STATE_ACTIVE);
            for(SubcategoryEntity s : allSubcategories){
                Tab subcategorytab= new Tab(s.getName());
                subcategorytab.addClassName("articles-tab");
                subCategoryTabs.add(subcategorytab);
            }
        }
    }

    /**
     * Añade todas las categorias que existen.
     */
    private void addCategories(List<CategoryEntity> allCategories){
        for(CategoryEntity c : allCategories){
            Tab categorytab= new Tab(c.getName());
            categorytab.addClassName("articles-tab");
            categoryTabs.add(categorytab);
        }

    }
}
