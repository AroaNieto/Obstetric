package es.obstetrics.obstetric.view.priv.views.maintenance.insurance;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import es.obstetrics.obstetric.backend.service.CenterService;
import es.obstetrics.obstetric.backend.service.InsuranceService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.InsuranceGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.insurance.DeleteInsuranceConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.insurance.InsuranceDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "secretary/insurances", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class InsuranceGridView extends MasterGrid<InsuranceEntity> {

    private final InsuranceService insuranceService;
    private final CenterService centerService;
    private Button deleteBtn;
    private Button reactivateBtn;

    @Autowired
    public InsuranceGridView(InsuranceService insuranceService,
                             CenterService centerService) {
        this.insuranceService = insuranceService;
        this.centerService = centerService;
        setHeader(new H2("ASEGURADORAS"));
        setFilterContainer();
        setGrid();
        updateGrid();
    }

    /**
     * Abre el cuadro de diálogo dónde se añadirá la aseguradora.
     */
    @Override
    public void openDialog() {
        InsuranceDialog insuranceDialog = new InsuranceDialog(null, insuranceService, centerService);
        insuranceDialog.setHeaderTitle("AÑADIR ASEGURADORA");
        insuranceDialog.addListener(InsuranceDialog.SaveEvent.class, this::saveInsurance);
        insuranceDialog.open();
    }

    @Transactional
    private void saveInsurance(InsuranceDialog.SaveEvent saveEvent) {
        // Guardar la entidad de aseguradora y obtener la versión persistida
        InsuranceEntity savedInsuranceEntity = insuranceService.save(saveEvent.getInsuranceEntity());
        // Eliminar todas las asociaciones actuales entre la aseguradora y los centros
        savedInsuranceEntity.getCenters().clear();
        insuranceService.save(savedInsuranceEntity);
        saveRelation(saveEvent.getCenters(), savedInsuranceEntity);
        updateGrid();
    }

    @Transactional
    private void saveRelation(List<CenterEntity> centers, InsuranceEntity insuranceEntity) {
        if (!centers.isEmpty()) {
            insuranceEntity.getCenters().addAll(centers);
            insuranceService.save(insuranceEntity);
        }
    }

    @Override
    public void setFilterContainer() {
        masterGrid.setDataProvider(createDataProvider());

        searchTextField.setTooltipText("Escriba el nombre de la aseguradora que desea buscar.");
        searchTextField.setValueChangeMode(ValueChangeMode.LAZY); //El evento se dispara inmediatamente después de cada cambio de texto

        searchTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> insuranceService.findByNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
        });

        Button helpButton = createButton(VaadinIcon.QUESTION_CIRCLE.create(), "help-button");
        helpButton.setTooltipText("Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE
                            + ConstantUtilities.ROUTE_HELP_MAINTENANCE_INSURANCES,
                    "Guía gestión de aseguradoras");
            windowHelp.open();
        });

        addBtn.setTooltipText("Añadir aseguradora");

        Button printButton = createButton(new Icon(VaadinIcon.PRINT), "help-button");
        printButton.setTooltipText("Imprimir listado");
        printButton.addClickListener(event -> printButton());
        filterContainerHl.add(searchTextField,
                createSearchAddressTextField(),
                createSearchPhoneTextField(),
                createSearchEmailTextField(),
                createCodPostalTextField(), addBtn, printButton, helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField, createSearchEmailTextField());

        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Las aseguradoras es pasan mediante carga diferencia, solo cuando el usuario solicita
     * la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("aseguradoras.pdf", () -> {
            List<InsuranceEntity> insuranceEntities = getInsurancesData();
            return new InsuranceGridPdf((ArrayList<InsuranceEntity>) insuranceEntities).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de aseguradoras");
        dialog.open();
    }

    /**
     * Obtiene los datos de la aseguradora a través del DataProvider (carga diferida)
     *
     * @return La lista de las aseguradoras.
     */
    private List<InsuranceEntity> getInsurancesData() {
        DataProvider<InsuranceEntity, String> dataProvider = createDataProvider();
        Query<InsuranceEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }

    private Button createButton(Icon icon, String className) {
        Button button = new Button(icon);
        button.addClassName(className);
        return button;
    }

    /**
     * Creación del textfield de buscar por el código postal.
     *
     * @return El texfield de buscar.
     */
    private TextField createCodPostalTextField() {

        TextField searchPostalCodeTextField = createTextField("Código postal",
                "text-field-1300", "Escriba el código postal que desea buscar.",
                new Icon(VaadinIcon.ENVELOPE));
        searchPostalCodeTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> insuranceService.findByPostalCodeContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchPostalCodeTextField;
    }


    private TextField createTextField(String title, String className, String tooltip, Icon icon) {
        TextField textField = new TextField(title);
        textField.addClassName(className);
        textField.setTooltipText(tooltip);
        textField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        textField.setPrefixComponent(icon);
        return textField;
    }

    /**
     * Creación del textfield de buscar por la dirección.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchAddressTextField() {
        TextField searchPostalCodeTextField = createTextField("Dirección",
                "text-field-1100", "Escriba la dirección que desea buscar.",
                new Icon(VaadinIcon.HOME));
        new TextField();
        searchPostalCodeTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> insuranceService.findByAddressContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchPostalCodeTextField;
    }

    /**
     * Creación del textfield de buscar por el email.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchEmailTextField() {

        TextField searchEmailTextField = createTextField("Email",
                "text-field-1100", "Escriba el email que desea buscar.",
                new Icon(VaadinIcon.MAILBOX));

        searchEmailTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> insuranceService.findByEmailContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchEmailTextField;
    }

    /**
     * Creación del textfield de buscar por el teléfono.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchPhoneTextField() {
        TextField searchPhoneTextField = createTextField("Teléfono",
                "text-field-1300", "Escriba el teléfono que desea buscar.",
                new Icon(VaadinIcon.PHONE));
        searchPhoneTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> insuranceService.findByPhoneContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchPhoneTextField;
    }

    /**
     * Creación del grid con sus respectivas columnas.
     */
    @Override
    public void setGrid() {
        masterGrid.addColumn(InsuranceEntity::getName).setHeader("Nombre").setAutoWidth(true).setSortable(true).setFrozen(true);
        masterGrid.addColumn(InsuranceEntity::getPhone).setHeader("Teléfono").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(InsuranceEntity::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(new ComponentRenderer<>(insuranceEntity -> {
            List<String> centers = insuranceEntity.getCenters().stream()
                    .map(CenterEntity::getCenterName)
                    .toList();
            FlexLayout centerList = new FlexLayout();
            centerList.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            centers.forEach(centerName -> centerList.add(new Span(centerName)));

            Details details = new Details("Centros Asociados", centerList);
            details.setOpened(false);
            return new Div(details);
        })).setWidth("300px");

        masterGrid.addColumn(InsuranceEntity::getAddress).setHeader("Dirección").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(InsuranceEntity::getPostalCode).setHeader("Código postal").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(InsuranceEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(new ComponentRenderer<>(insuranceEntity -> {

            Button editBtn = createButton(new Icon(VaadinIcon.EDIT), "dark-green-background-button");
            editBtn.addClickListener(event -> openEditDialog(insuranceEntity));
            editBtn.setTooltipText("Editar");

            reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button");
            reactivateBtn.addClickListener(event -> openReactivateInsurance(insuranceEntity));
            reactivateBtn.setTooltipText("Reactivar");
            if (insuranceEntity.getState() != null && insuranceEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button");
                deleteBtn.setVisible(false);
                deleteBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteBtn, reactivateBtn);
            }
            deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button");
            deleteBtn.addClickListener(event -> openUnsubscribeDialog(insuranceEntity));
            deleteBtn.setTooltipText("Dar de baja");
            reactivateBtn.setVisible(false);
            return new HorizontalLayout(editBtn, deleteBtn, reactivateBtn);
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }


    private void openReactivateInsurance(InsuranceEntity insuranceEntity) {
        insuranceEntity.setState(ConstantUtilities.STATE_ACTIVE);
        insuranceService.save(insuranceEntity);
        deleteBtn.setVisible(true);
        reactivateBtn.setVisible(false);
        updateGrid();
    }

    private void openEditDialog(InsuranceEntity insuranceEntity) {
        InsuranceDialog insuranceDialog = new InsuranceDialog(insuranceEntity, insuranceService, centerService);
        insuranceDialog.setHeaderTitle("MODIFICAR ASEGURADORA");
        insuranceDialog.addListener(InsuranceDialog.SaveEvent.class, this::saveInsurance);
        insuranceDialog.open();
    }

    private void openUnsubscribeDialog(InsuranceEntity insuranceEntity) {
        DeleteInsuranceConfirmDialog deleteInsuranceConfirmDialog = new DeleteInsuranceConfirmDialog(insuranceEntity);
        deleteInsuranceConfirmDialog.addListener(DeleteInsuranceConfirmDialog.DeleteEvent.class, this::unsubscribeInsurance);
        deleteInsuranceConfirmDialog.open();
    }

    private void unsubscribeInsurance(DeleteInsuranceConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getInsurance().setState(ConstantUtilities.STATE_INACTIVE);
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        insuranceService.save(deleteEvent.getInsurance());
        updateGrid();
    }

    /**
     * Actualización del grid.
     */
    @Transactional
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }


    /**
     * Configura un DataProvider para cargar datos de aseguradoras de manera diferida y eficiente,
     * optimizando el rendiiento de la aplicación al minimizar la carga anticipada de datos.
     */
    private DataProvider<InsuranceEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset(); //Indice de inicio
                    int limit = query.getLimit(); //Cantidad de elementos a recuprar
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return insuranceService.findAll(offset / limit, limit).get().toList().stream(); //Devuelve la aseguradoras segun el offset y el límite y la convierte a stream.
                    } else {
                        return insuranceService.findByNameContaining(filter, offset / limit, limit).get().toList().stream(); //Devuelve una página de aseguradoras que coinciden con el filtro y la convierte a stream.
                    }
                },
                query -> { //Obtiene el tamaño total de los datos después de aplicar el filtro.
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) insuranceService.findAll(0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de aseguradoras en el sistema.
                    } else {
                        return (int) insuranceService.findByNameContaining(filter, 0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de aseguradoras que coinciden con el filtro.
                    }
                }
        );
    }
}
