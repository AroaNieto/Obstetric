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
import es.obstetrics.obstetric.backend.entity.LoginLogOutLogEntity;
import es.obstetrics.obstetric.backend.service.LoginLogOutLogService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.AccessGridViewPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/access-user-access", layout = PrincipalView.class)
@PermitAll
public class UserAccessGridView extends MasterGrid<LoginLogOutLogEntity> {

    private  final  LoginLogOutLogService loginLogOutLogService;

    @Autowired
    public UserAccessGridView(LoginLogOutLogService loginLogOutLogService) {
        setHeader(new H2("ACCESOS"));
        this.loginLogOutLogService = loginLogOutLogService;
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
        searchTextField.setTooltipText("Buscar nombre del usuario");
        searchTextField.setPlaceholder("Buscar usuario");
        searchTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query ->
                    loginLogOutLogService.findByUserEntityNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
        });
        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE));
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTE_HELP_ACCESS,
                    "Guía gestión de cambios");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT));
        printButton.addClickListener(event-> printButton());
        filterContainerHl.add(searchTextField,helpButton,printButton);
        filterContainerHl.setFlexGrow(1,searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("modificaciones_pacientes.pdf", () -> {
            List<LoginLogOutLogEntity> userAccess = getUserAccessGridViewData();
            return new AccessGridViewPdf((ArrayList<LoginLogOutLogEntity>) userAccess).generatePdf();
        });
        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de accesos");
        dialog.open();
    }

    /**
     * Obtiene los datos a través del DataProvider (carga diferida)
     * @return La lista de los registros.
     */
    private List<LoginLogOutLogEntity> getUserAccessGridViewData() {
        DataProvider<LoginLogOutLogEntity, String> dataProvider = createDataProvider();
        Query<LoginLogOutLogEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }

    private Button createButton(Icon icon){
        Button button = new Button(icon);
        button.addClassName("help-button");
        return button;
    }

    private DataProvider<LoginLogOutLogEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return loginLogOutLogService.findAll(offset / limit, limit).get().toList().stream();
                    } else {
                        return loginLogOutLogService.findByUserEntityNameContaining(filter, offset / limit, limit).get().toList().stream();
                    }
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) loginLogOutLogService.findAll(0, Integer.MAX_VALUE).getTotalElements();
                    } else {
                        return (int) loginLogOutLogService.findByUserEntityNameContaining(filter, 0, Integer.MAX_VALUE).getTotalElements();
                    }
                }
        );
    }

    private static Renderer<LoginLogOutLogEntity> createUserRenderer() {
        return LitRenderer.<LoginLogOutLogEntity> of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", loginLogOutLogEntity -> {
                    if (loginLogOutLogEntity.getUserEntity() != null && loginLogOutLogEntity.getUserEntity().getProfilePhoto() != null) {
                        byte[] profilePhoto = loginLogOutLogEntity.getUserEntity().getProfilePhoto();

                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", loginLogOutLogEntity ->{
                    if(loginLogOutLogEntity.getUserEntity() == null){
                        return null;
                    }
                         return loginLogOutLogEntity.getUserEntity().getName() + " " + loginLogOutLogEntity.getUserEntity().getLastName();

                        })
                .withProperty("email", loginLogOutLogEntity -> {
                    if(loginLogOutLogEntity.getUserEntity() == null){
                        return null;
                    }
                    return  loginLogOutLogEntity.getUserEntity().getAge() + " años.";
                });
    }
    @Override
    public void setGrid() {
        masterGrid.addColumn(createUserRenderer()).setHeader("Usuarios").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(loginLogOutLogEntity -> loginLogOutLogEntity.getDate().getDayOfMonth()+"/"+loginLogOutLogEntity.getDate().getMonthValue()+"/"+loginLogOutLogEntity.getDate().getYear()).setHeader("Día").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(loginLogOutLogEntity -> loginLogOutLogEntity.getTime().getHour()+":"+loginLogOutLogEntity.getTime().getMinute() + " "+  loginLogOutLogEntity.getTime().getSecond()+"s").setHeader("Tiempo").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(LoginLogOutLogEntity::getMessage).setHeader("Tipo de acceso").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(LoginLogOutLogEntity::getIp).setHeader("Dirección IP").setAutoWidth(true).setSortable(true);
    }

    /**
     * Actualización del grid de mensajes.
     */
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }
}
