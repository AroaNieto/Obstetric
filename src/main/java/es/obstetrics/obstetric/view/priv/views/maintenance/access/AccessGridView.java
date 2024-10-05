package es.obstetrics.obstetric.view.priv.views.maintenance.access;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.UserAccessGridViewPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/access-patients-change-access", layout = PrincipalView.class)
@PermitAll
public class AccessGridView extends MasterGrid<PatientsLogEntity> {

    private final PatientsLogService patientsLogService;

    @Autowired
    public AccessGridView(PatientsLogService patientsLogService) {
        setHeader(new H2("CONTROL DE CAMBIOS EN PACIENTES"));
        this.patientsLogService = patientsLogService;
        setGrid();
        setFilterContainer();
        updateGrid();
    }

    @Override
    public void openDialog() {

    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsquedas en el grid
     */
    @Override
    public void setFilterContainer() {
        masterGrid.setDataProvider(createDataProvider());
        searchTextField.addKeyPressListener(Key.ESCAPE, keyPressEvent -> searchTextField.setValue(""));
        searchTextField.setPlaceholder("Buscar paciente");
        searchTextField.setTooltipText("Buscar nombre del paciente");
        searchTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query ->
                    patientsLogService.findByPatientEntityNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
        });
        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE));
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTE_HELP_ACCESS,
                    "Guía gestión de accesos");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT));
        printButton.addClickListener(event-> printButton());
        filterContainerHl.add(searchTextField,helpButton,printButton);
        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("accesos.pdf", () -> {
            List<PatientsLogEntity> userAccess = getUserAccessGridViewData();
            return new UserAccessGridViewPdf((ArrayList<PatientsLogEntity>) userAccess).generatePdf();
        });
        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de cambios");
        dialog.open();
    }

    private Button createButton(Icon icon){
        Button button = new Button(icon);
        button.addClassName("help-button");
        return button;
    }

    /**
     * Obtiene los datos del paciente a través del DataProvider (carga diferida)
     * @return La lista de los registros.
     */
    private List<PatientsLogEntity> getUserAccessGridViewData() {
        DataProvider<PatientsLogEntity, String> dataProvider = createDataProvider();
        Query<PatientsLogEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }


    private DataProvider<PatientsLogEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return patientsLogService.findAll(offset / limit, limit).get().toList().stream();
                    } else {
                        return patientsLogService.findByPatientEntityNameContaining(filter, offset / limit, limit).get().toList().stream();
                    }
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) patientsLogService.findAll(0, Integer.MAX_VALUE).getTotalElements();
                    } else {
                        return (int) patientsLogService.findByPatientEntityNameContaining(filter, 0, Integer.MAX_VALUE).getTotalElements();
                    }
                }
        );
    }

    private static Renderer<PatientsLogEntity> createUserRenderer() {
        return LitRenderer.<PatientsLogEntity>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", patientsLogEntity -> {
                    byte[] profilePhoto = patientsLogEntity.getPatientEntity().getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", patientsLogEntity -> patientsLogEntity.getPatientEntity().getName() + " " + patientsLogEntity.getPatientEntity().getLastName())
                .withProperty("email", patientsLogEntity -> patientsLogEntity.getPatientEntity().getAge() + " años.");
    }

    @Override
    public void setGrid() {
        masterGrid.addColumn(createUserRenderer()).setHeader("Paciente").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(patientsLogEntity -> {
                    if (patientsLogEntity.getSanitaryEntity() != null) {
                        return patientsLogEntity.getSanitaryEntity();
                    }
                    return null;
                }
        ).setHeader("Trabajador").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(patientsLogEntity -> {
                    if (patientsLogEntity.getSanitaryEntity() != null) {
                        return patientsLogEntity.getSanitaryEntity().getDni();
                    }
                    return null;
                }
        ).setHeader("DNI del trabajador").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(patientsLogEntity -> patientsLogEntity.getDate().getDayOfMonth() + "/" + patientsLogEntity.getDate().getMonthValue() + "/" + patientsLogEntity.getDate().getYear()).setHeader("Día").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(patientsLogEntity -> patientsLogEntity.getTime().getHour() + ":" + patientsLogEntity.getTime().getMinute() + " " + patientsLogEntity.getTime().getSecond() + "s").setHeader("Tiempo").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientsLogEntity::getMessage).setHeader("Tipo de cambio").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientsLogEntity::getIp).setHeader("Dirección IP").setAutoWidth(true).setSortable(true);
    }

    /**
     * Actualización del grid de mensajes.
     */
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }
}
