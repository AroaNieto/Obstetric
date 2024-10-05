package es.obstetrics.obstetric.view.priv.dialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.backend.service.CategoryService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.FilesUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase que extiende de la clase {@link MasterDialog}, se usa para
 * crear o editar contenidos.
 */
@Slf4j
public class NewsletterDialog extends MasterDialog {

    private final Binder<NewsletterEntity> contentEntityBinder;
    private final TextField name;
    private final TextArea summary;
    private final TextField url;
    private final ComboBox<String> quarter;
    private final ComboBox<String> state;
    private final ComboBox<SubcategoryEntity> subcategoryEntity;
    private final ComboBox<CategoryEntity> categoryEntity;
    private final RadioButtonGroup<String> typeContent;
    private final RadioButtonGroup<String> duration;
    private final DatePicker date;
    private final DatePicker startDate;
    private final DatePicker endingDate;
    private final H5 errorMessage;
    private byte[] contentBytePdf;
    private byte[] contentByteUrl;
    private byte[] contentMiniature;
    private NewsletterEntity newsletterEntity;
    private int dateIsOpen;
    private HorizontalLayout contentUrlHl;
    private HorizontalLayout contentPdfHl;
    private final CategoryService categoryService;

    /**
     * Constructor de la clase, se encarga de inicializar los componentes que aparecerán en el
     * cuadro de dialogo y se estableceran en el Binder, configurar el enlace de datos
     * mediante el BeanValidationBinder y establecer los valores.
     */
    public NewsletterDialog(NewsletterEntity content, SubcategoryEntity subcategoryEntityText,
                            List<CategoryEntity> categoryEntities, List<SubcategoryEntity> subcategoryEntities,
                            CategoryService categoryService) {

        name = new TextField("Nombre");
        state = new ComboBox<>("Estado");
        summary = new TextArea("Resumen");
        subcategoryEntity = new ComboBox<>("Subcategoria");
        categoryEntity = new ComboBox<>("Categoría");
        quarter = new ComboBox<>("Trimestre");
        typeContent = new RadioButtonGroup<>("Contenido especifico");
        duration = new RadioButtonGroup<>("Duración");
        date = new DatePicker();
        endingDate = new DatePickerTemplate("Fecha de finalización");

        startDate = new DatePickerTemplate("Fecha de inicio");

        url = new TextField("URL");
        errorMessage = new H5("");
        contentUrlHl = new HorizontalLayout();
        contentPdfHl = new HorizontalLayout();
        contentEntityBinder = new BeanValidationBinder<>(NewsletterEntity.class);
        dateIsOpen = 0;
        this.categoryService = categoryService;
        createHeaderDialog();
        createDialogLayout();
        contentEntityBinder.forField(duration)
                .asRequired("Este campo es obligatorio. ")
                .bind(NewsletterEntity::getDuration, NewsletterEntity::setDuration);

        contentEntityBinder.forField(subcategoryEntity)
                .asRequired("Este campo es obligatorio. ")
                .bind(NewsletterEntity::getSubcategoryEntity, NewsletterEntity::setSubcategoryEntity);

        contentEntityBinder.forField(categoryEntity)
                .asRequired("Este campo es obligatorio. ")
                .bind(
                        contentEntity -> contentEntity.getSubcategoryEntity() != null ?
                                contentEntity.getSubcategoryEntity().getCategoryEntity() : null,
                        (contentEntity, category) -> {
                            // Asignar la categoría al subcategoría si no está establecida
                            if (contentEntity.getSubcategoryEntity() == null) {
                                SubcategoryEntity subcategoryEntity = new SubcategoryEntity();
                                subcategoryEntity.setCategoryEntity(category);
                                contentEntity.setSubcategoryEntity(subcategoryEntity);
                            } else {
                                // Actualizar la categoría de la subcategoría existente
                                contentEntity.getSubcategoryEntity().setCategoryEntity(category);
                            }
                        }
                );
        contentEntityBinder.bindInstanceFields(this);
        setContent(content, subcategoryEntityText, categoryEntities, subcategoryEntities);
    }

    private void saveContentBytePdf(UploadPdfDialog.SaveEvent saveEvent) {
        contentBytePdf = saveEvent.getContentBytePdf();
        contentMiniature = saveEvent.getContentMiniature();
    }

    private void saveContentByteUrl(UploadSrcDialog.SaveEvent saveEvent) {
        contentByteUrl = saveEvent.getContentByteUrl();
    }

    /**
     * Método utilizado para establecer los valores de los campos de diálogo dependiendo de si
     * se está editando un contenido (subcategory != null) o añadiendo (subcategoryEntityText != null).
     *
     * @param content               Contenido
     * @param subcategoryEntityText Subcategoira asociada al nuevo contenido que se acabe de añadir.
     */
    public void setContent(NewsletterEntity content, SubcategoryEntity subcategoryEntityText,
                           List<CategoryEntity> categoryEntities, List<SubcategoryEntity> subcategoryEntities) {
        if (content != null && subcategoryEntityText == null) { //Editando un contenido desde el apartado de Categorias
            state.setItems(content.getState());
            typeContent.setValue(content.getTypeContent());
            duration.setValue(content.getDuration());
            createItemsCategoryAndSubcategoryEntities(subcategoryEntities, categoryEntities);
            createValuesCategoryAndSubcategoryEntities(content.getSubcategoryEntity(), content.getSubcategoryEntity().getCategoryEntity());
            setReadOnlyCategoryAndSubcategoryEntities(true);
            updateContent(content);
            updatePdfOrUrl(content);
        } else if (content == null && subcategoryEntityText != null) { //Esta añadiendo desde la ventana de categorias
            createVisibilityPdfOrUrl();
            newsletterEntity = new NewsletterEntity();
            newsletterEntity.setSubcategoryEntity(subcategoryEntityText);
            endingDate.setReadOnly(true);
            createItemsCategoryAndSubcategoryEntities(subcategoryEntities, categoryEntities);
            createValuesCategoryAndSubcategoryEntities(newsletterEntity.getSubcategoryEntity(), newsletterEntity.getSubcategoryEntity().getCategoryEntity());
            setReadOnlyCategoryAndSubcategoryEntities(true);
            updateContent(newsletterEntity);
        } else if (content != null) { //Esta editando desde la ventana de contenidos
            state.setItems(content.getState());
            typeContent.setValue(content.getTypeContent());
            duration.setValue(content.getDuration());
            createItemsCategoryAndSubcategoryEntities(subcategoryEntities, categoryEntities);
            createValuesCategoryAndSubcategoryEntities(content.getSubcategoryEntity(), content.getSubcategoryEntity().getCategoryEntity());
            setReadOnlyCategoryAndSubcategoryEntities(false);
            updateContent(content);
            updatePdfOrUrl(content);

        } else { //Añadiendo un contenido dentro del apartado de contenidos

            createVisibilityPdfOrUrl();
            newsletterEntity = new NewsletterEntity();
            endingDate.setReadOnly(true);
            setReadOnlyCategoryAndSubcategoryEntities(false);
            createItemsCategoryAndSubcategoryEntities(subcategoryEntities, categoryEntities);
            updateContent(null);
        }
    }

    private void createVisibilityPdfOrUrl() {
        contentPdfHl.setVisible(false);
        contentUrlHl.setVisible(false);
    }

    private void updatePdfOrUrl(NewsletterEntity content) {
        if (content.getContentByteUrl() != null) {
            contentPdfHl.setVisible(false);
            contentUrlHl.setVisible(true);
            contentByteUrl = content.getContentByteUrl(); //Convertir el contenido
        } else if (content.getContentBytePdf() != null) {
            contentPdfHl.setVisible(true);
            contentUrlHl.setVisible(false);
            contentBytePdf = content.getContentBytePdf();
            String filenameJPG = FilesUtilities.setThumbnailOfPdfInByte("contenido", contentBytePdf);
            try {
                contentMiniature = FilesUtilities.fileToByte(filenameJPG);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createItemsCategoryAndSubcategoryEntities(List<SubcategoryEntity> subcategoryEntities,
                                                           List<CategoryEntity> categoryEntities) {
        subcategoryEntity.setItems(subcategoryEntities);
        categoryEntity.setItems(categoryEntities);
    }

    private void createValuesCategoryAndSubcategoryEntities(SubcategoryEntity subcategoryEnt,
                                                            CategoryEntity categoryEnt) {
        subcategoryEntity.setValue(subcategoryEnt);
        categoryEntity.setValue(categoryEnt);
    }

    private void setReadOnlyCategoryAndSubcategoryEntities(boolean value) {
        subcategoryEntity.setReadOnly(value);
        categoryEntity.setReadOnly(value);

    }

    public void updateContent(NewsletterEntity content) {
        quarter.setItems(ConstantUtilities.FIRST_QUARTERER,
                ConstantUtilities.SECOND_QUARTERER,
                ConstantUtilities.THIRD_QUARTERER,
                ConstantUtilities.NONE_QUARTERER);
        typeContent.setItems(ConstantUtilities.TYPE_CONTENT_PDF, ConstantUtilities.TYPE_CONTENT_WEB);
        duration.setItems(ConstantUtilities.ANNUAL, ConstantUtilities.JUST_ONCE, ConstantUtilities.TIMELESS);

        contentEntityBinder.setBean(content);
        contentEntityBinder.readBean(content);
    }

    /**
     * Establece los valores de estado y fecha antes
     * de guardar un nuevo contenido.
     */
    public void setValues() {
        date.setValue(LocalDate.now());
        state.setItems(ConstantUtilities.STATE_INACTIVE, ConstantUtilities.STATE_ACTIVE);
        state.setValue(ConstantUtilities.STATE_ACTIVE);
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
        setErrorMessage("");
        clearTextField();
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
        state.clear();
        summary.clear();
        quarter.clear();
        typeContent.clear();
        duration.clear();
        date.clear();
        endingDate.clear();
        startDate.clear();
        url.clear();
        categoryEntity.clear();
        contentByteUrl = null;
        contentBytePdf = null;
        contentMiniature = null;
        subcategoryEntity.clear();
        contentEntityBinder.readBean(null);
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo un nuevo contenido o editando.
     * - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     * - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {

        if (contentEntityBinder.getBean() == null || contentEntityBinder.getBean().getId() == null) { //Si se está añadiendo una nueva newsletter
            try {
                setValues();
                contentEntityBinder.writeBean(newsletterEntity);
                newsletterEntity.setContentByteUrl(contentByteUrl);
                newsletterEntity.setContentBytePdf(contentBytePdf);
                newsletterEntity.setContentMiniature(contentMiniature);
                newsletterEntity.setState(ConstantUtilities.STATE_ACTIVE);
                if (categoryService.findOneByName(newsletterEntity.getName()) != null) {
                    setErrorMessage("El contenido ya existe para esa subcategoría.");
                }
                if (newsletterEntity.getTypeContent().equals(ConstantUtilities.TYPE_CONTENT_PDF) &&
                        (newsletterEntity.getContentMiniature() == null || newsletterEntity.getContentBytePdf() == null)) {
                    setErrorMessage("Rellene correctamente los campos PDF.");
                    return;
                } else if (newsletterEntity.getTypeContent().equals(ConstantUtilities.TYPE_CONTENT_WEB)
                        && (newsletterEntity.getContentByteUrl() == null || newsletterEntity.getUrl().isEmpty())) {
                    setErrorMessage("Rellene correctamente los campos de la web.");
                    return;
                }
                if (validateURl(newsletterEntity.getUrl())) {
                    setErrorMessage("URL no válida.");
                    return;
                }
            } catch (ValidationException e) {
                setErrorMessage("Debe rellenar todos los campos correctamente");
            }
            fireEvent(new SaveEvent(this, newsletterEntity));

        } else {
            try {
                contentEntityBinder.writeBean(contentEntityBinder.getBean());
                contentEntityBinder.getBean().setContentByteUrl(contentByteUrl);
                contentEntityBinder.getBean().setContentBytePdf(contentBytePdf);
                contentEntityBinder.getBean().setContentMiniature(contentMiniature);
                contentEntityBinder.getBean().setState(ConstantUtilities.STATE_ACTIVE);
                if (categoryService.findOneByName(contentEntityBinder.getBean().getName()) != null) {
                    setErrorMessage("El contenido ya existe para esa subcategoría.");

                } else if (contentEntityBinder.getBean().getTypeContent().equals(ConstantUtilities.TYPE_CONTENT_PDF)
                        && (contentEntityBinder.getBean().getContentMiniature() == null || contentEntityBinder.getBean().getContentBytePdf() == null)) {
                    setErrorMessage("Rellene correctamente los campos PDF.");
                    return;
                } else if (contentEntityBinder.getBean().getTypeContent().equals(ConstantUtilities.TYPE_CONTENT_WEB)
                        && contentEntityBinder.getBean().getContentByteUrl() == null) {
                    setErrorMessage("Rellene correctamente los campos de la web.");
                    return;
                }
                if (validateURl(contentEntityBinder.getBean().getUrl())) {
                    setErrorMessage("URL no válida.");
                    return;
                }

            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
            fireEvent(new SaveEvent(this, contentEntityBinder.getBean()));
        }
        setErrorMessage("");
        close();
    }

    /**
     * Validación de que la URL adjuntada es correcta.
     * @param url Url adjuntada
     * @return Verdadero is es correcta, falso si no.
     */
    private boolean validateURl(String url) {
        String regex = "^(https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?$";
        Pattern pattern = Pattern.compile(regex);
        if(!url.isEmpty()){
            Matcher matcher = pattern.matcher(url);
            if(!matcher.matches()){
                setErrorMessage("URL invalida");
                return true;
            }
        }
        return false;
    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo de contenido
     * con sus campos correspondientes.
     */
    public void createDialogLayout() {
        categoryEntity.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        subcategoryEntity.setItems(event.getValue().getSubcategories());
                    }
                }
        );

        errorMessage.addClassName("label-error");

        typeContent.addValueChangeListener(event ->
                getTypeContent(event.getValue())
        );

        duration.addValueChangeListener(event ->
                getDuration(event.getValue())
        );

        setDates();

        H5 prefix = new H5("http://");
        prefix.getStyle().set("color", "#999999");
        url.setPrefixComponent(prefix);
        HorizontalLayout categoryAndSubHl = new HorizontalLayout(categoryEntity, subcategoryEntity);
        categoryAndSubHl.setSizeFull();
        categoryAndSubHl.expand(categoryEntity, subcategoryEntity);

        HorizontalLayout datesHl = new HorizontalLayout(startDate, endingDate);
        datesHl.setSizeFull();
        datesHl.expand(startDate, endingDate);

        HorizontalLayout contentAndDurationHl = new HorizontalLayout(typeContent, duration);
        contentAndDurationHl.setSizeFull();
        contentAndDurationHl.expand(typeContent, duration);

        HorizontalLayout titleAndQuartererHl = new HorizontalLayout(name, quarter);
        titleAndQuartererHl.setSizeFull();
        titleAndQuartererHl.expand(name, quarter);

        summary.setMinHeight("120px");
        summary.setMaxHeight("120px");
        Button buttonUploadPdf = createButtonAndListenerPdf();
        Button buttonUploadUrl = createButtonAndListenerUrl();
        contentUrlHl = new HorizontalLayout(url, buttonUploadUrl);
        contentUrlHl.setSizeFull();
        contentUrlHl.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        contentUrlHl.expand(url);

        contentPdfHl = new HorizontalLayout(buttonUploadPdf);
        contentPdfHl.setSizeFull();

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.add(categoryAndSubHl, titleAndQuartererHl, summary, contentAndDurationHl, contentUrlHl, contentPdfHl, datesHl, errorMessage);
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
    }

    private Button createButtonAndListenerPdf() {

        Button openUpload = createButton("Añadir PDF...");

        openUpload.addClickListener(buttonClickEvent -> {
            UploadPdfDialog uploadPdfDialog;
            if (contentBytePdf != null) {
                uploadPdfDialog = new UploadPdfDialog(contentBytePdf, contentMiniature);
            } else {
                uploadPdfDialog = new UploadPdfDialog(null, null);
            }
            uploadPdfDialog.addListener(UploadPdfDialog.SaveEvent.class, this::saveContentBytePdf);
            uploadPdfDialog.setHeaderTitle("AÑADIR PDF");
            uploadPdfDialog.open();
        });
        openUpload.getStyle().set("margin-top","40px");
        return openUpload;
    }

    private Button createButtonAndListenerUrl() {
        Button openUpload = createButton("Añadir fotografía...");
        openUpload.addClickListener(buttonClickEvent -> {
            UploadSrcDialog uploadSrcDialog;
            if (contentByteUrl != null) {
                uploadSrcDialog = new UploadSrcDialog(contentByteUrl);
            } else {
                uploadSrcDialog = new UploadSrcDialog(null);
            }
            uploadSrcDialog.addListener(UploadSrcDialog.SaveEvent.class, this::saveContentByteUrl);
            uploadSrcDialog.setHeaderTitle("AÑADIR FOTOGRAFÍA");
            uploadSrcDialog.open();
        });
        openUpload.getStyle().set("margin-top","40px");
        return openUpload;
    }


    private Button createButton(String title){
        Button button = new Button(title);
        button.addClassName("dark-green-button");
        return button;
    }

    private void setDates() {
        startDate.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dateIsOpen = 1;
            }
        });

        startDate.addValueChangeListener(event -> {
            if (dateIsOpen == 1) {
                LocalDate date = startDate.getValue();
                if (endingDate.getValue() != null) {
                    updateEndingDate(endingDate.getValue());
                }
                if (date != null && date.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
                    setErrorMessage("La fecha debe ser igual o posterior al día de hoy.");
                    startDate.clear();
                }
            }
        });

        endingDate.addValueChangeListener(event ->
                updateEndingDate(event.getValue()));
    }

    private void updateEndingDate(LocalDate date) {
        if ((startDate.getValue() != null) && (date != null)) {
            if (date.isBefore(startDate.getValue())) {
                setErrorMessage("La fecha de fin no puede ser anterior a la fecha de inicio, reviselo.");
                clearEndingDate();
            }
        }
    }

    private void clearEndingDate() {
        endingDate.clear();
    }

    private void getDuration(String value) {
        if (value != null) {
            if (value.equals(ConstantUtilities.TIMELESS)) {
                endingDate.clear();

                endingDate.setReadOnly(true);
            } else {
                endingDate.setReadOnly(false);
            }
        }
    }

    private void getTypeContent(String value) {
        if (value != null) {
            if (value.equals(ConstantUtilities.TYPE_CONTENT_PDF)) {
                contentByteUrl = null;
                url.clear();
                if (newsletterEntity != null) {
                    newsletterEntity.setContentBytePdf(null); //Si el contenido se cambia a formato URL se elimina el campo contentByte de la entidad
                }
                contentPdfHl.setVisible(true);
                contentUrlHl.setVisible(false);
            } else {
                if (newsletterEntity != null) {
                    newsletterEntity.setContentByteUrl(null); //Si el contenido se cambia a formato URL se elimina el campo contentByte de la entidad
                }
                contentByteUrl = null;
                contentPdfHl.setVisible(false);
                contentUrlHl.setVisible(true);
            }
        }
    }

    /**
     * Clase abstracta que extiene de {@link NewsletterDialog}, evento ocurrido en dicha clase.
     * Almacena el contenido asociado al evento.
     */
    public static abstract class ContentFormEvent extends ComponentEvent<NewsletterDialog> {
        private final NewsletterEntity newsletterEntity; //Contenido con la que se trabaja

        protected ContentFormEvent(NewsletterDialog source, NewsletterEntity newsletterEntity) {
            super(source, false);
            this.newsletterEntity = newsletterEntity;
        }

        public NewsletterEntity getContent() {
            return newsletterEntity;
        }
    }

    /**
     * Clase heredada de ContentFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     * Tiene un constructor que llama al constructor de la super clase y establece el contenido asociado al evento.
     */
    public static class SaveEvent extends ContentFormEvent {
        SaveEvent(NewsletterDialog source, NewsletterEntity newsletterEntity) {
            super(source, newsletterEntity);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que maneajrá el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

}