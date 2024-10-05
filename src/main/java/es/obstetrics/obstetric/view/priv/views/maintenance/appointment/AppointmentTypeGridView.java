package es.obstetrics.obstetric.view.priv.views.maintenance.appointment;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.AppointmentTypeEntity;
import es.obstetrics.obstetric.backend.service.AppointmentTypeService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.AppointmentTypeGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.appointment.UnsubscribeAppointmentTypeConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.appointment.AppointmentTypeDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * Clase encargada de mostrar los tipos de citas en un grid con sus respectivos filtros,
 *  con el objetivo de que el secretario o el administrador pueda añadir un tipo de cita.
 */
@Route(value = "secretary/appointment-type", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class AppointmentTypeGridView extends MasterGrid<AppointmentTypeEntity> {


    private final AppointmentTypeService appointmentTypeService;
    private Button deleteBtn;
    private Button reactivateBtn;

    @Autowired
    public AppointmentTypeGridView(AppointmentTypeService appointmentTypeService) {

        this.appointmentTypeService = appointmentTypeService;

        setHeader(new H2("TIPOS DE CITAS"));

        setGrid();
        setFilterContainer();
        updateGrid();
    }
    @Override
    public void openDialog() {
        AppointmentTypeDialog appointmentTypeDialog = new AppointmentTypeDialog(null, appointmentTypeService);
        appointmentTypeDialog.open();
        appointmentTypeDialog.setHeaderTitle("AÑADIR TIPO DE CITA");
        appointmentTypeDialog.addListener(AppointmentTypeDialog.SaveEvent.class, this::saveAppointment);
    }

    private void saveAppointment(AppointmentTypeDialog.SaveEvent saveEvent) {
        appointmentTypeService.save(saveEvent.getAppointment());
        updateGrid();
    }

    @Override
    public void setFilterContainer() {
        gridListDataView = masterGrid.setItems(appointmentTypeService.findAll()); //Configuración del DataView

        searchTextField.setTooltipText("Escriba la descripción del tipo de cita que desea buscar.");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchTextField.setPlaceholder("Buscar descripción de tipo de cita");
        searchTextField.addValueChangeListener(event -> {
            gridListDataView.addFilter(appointmentType -> { //Filtro. compara cada categoria con el texto que escribe el usuario
                String search = searchTextField.getValue().trim();
                if (search.isEmpty()) return true;
                return isIdentical(appointmentType.getDescription(), search);
            }); // Actualizar el grid después de cambiar el valor del campo de búsqueda
        });

        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE),"help-button","Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTE_HELP_APPOINTMENT,
                    "Guía gestión de tipos citas");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT),"help-button","Imprimir listado");
        printButton.addClickListener(event-> printButton());
        addBtn.setTooltipText("Añadir tipo de cita");

        filterContainerHl.add(searchTextField,addBtn, printButton,helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("tipos_citas.pdf", () -> new AppointmentTypeGridPdf((ArrayList<AppointmentTypeEntity>) appointmentTypeService.findAll()).generatePdf());

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de tipos de citas");
        dialog.open();
    }

    /**
     * Verifica si la cadena que está escribiendo el usuario mediante el textfield está contenida
     *  dentro del nombre de la propiedad.
     */
    private boolean isIdentical(String text, String search) {
        return text.toLowerCase().contains(search.toLowerCase());
    }

    @Override
    public void setGrid() {
        masterGrid.addColumn(AppointmentTypeEntity::getDescription).setHeader("Descripción").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(AppointmentTypeEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(appointmentTypeEntity -> {
            Button editBtn =  createButton(new Icon(VaadinIcon.EDIT),"dark-green-background-button","Editar");
            editBtn.addClickListener(event -> openEditAppointmentTypeDialog(appointmentTypeEntity));

            reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH),"yellow-color-button","Reactivar");
            reactivateBtn.setTooltipText("Reactivar");
            reactivateBtn.addClickListener(event -> openReactivateAppointmentType(appointmentTypeEntity));
            //Se comprueba si está dado de baja, si es así se deshabilita el botón de dar de baja y se añade el botón de reload
            if(appointmentTypeEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-disable-background-button","Dar de baja");
                deleteBtn.setVisible(false);
                deleteBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout( editBtn, deleteBtn,reactivateBtn);
            }
            reactivateBtn.setVisible(false);
            deleteBtn = createButton(new Icon(VaadinIcon.TRASH),"lumo-error-color-background-button","Dar de baja");
            deleteBtn.addClickListener(event -> openDeleteAppointmentTypeDialog(appointmentTypeEntity));
            return new HorizontalLayout( editBtn, deleteBtn);
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private void openReactivateAppointmentType(AppointmentTypeEntity appointmentTypeEntity) {
        appointmentTypeEntity.setState(ConstantUtilities.STATE_ACTIVE);
        appointmentTypeService.save(appointmentTypeEntity);
        deleteBtn.setVisible(true);
        reactivateBtn.setVisible(false);
        updateGrid();
    }

    private Button createButton(Icon icon, String className, String tooltip){
        Button button = new Button(icon);
        button.addClassName(className);
        button.setTooltipText(tooltip);
        return button;
    }

    private void openDeleteAppointmentTypeDialog(AppointmentTypeEntity appointmentTypeEntity) {
        UnsubscribeAppointmentTypeConfirmDialog unsubscribeAppointmentTypeConfirmDialog = new UnsubscribeAppointmentTypeConfirmDialog(appointmentTypeEntity);
        unsubscribeAppointmentTypeConfirmDialog.open();
        unsubscribeAppointmentTypeConfirmDialog.addListener(UnsubscribeAppointmentTypeConfirmDialog.UnsubscribeEvent.class, this::unsubscribeAppointment);
    }

    private void unsubscribeAppointment(UnsubscribeAppointmentTypeConfirmDialog.UnsubscribeEvent unsubscribeEvent) {
        unsubscribeEvent.getAppointmentTypeEntity().setState(ConstantUtilities.STATE_INACTIVE);
        appointmentTypeService.save(unsubscribeEvent.getAppointmentTypeEntity());
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        updateGrid();
    }

    private void openEditAppointmentTypeDialog(AppointmentTypeEntity appointmentTypeEntity) {
        AppointmentTypeDialog appointmentTypeDialog = new AppointmentTypeDialog(appointmentTypeEntity, appointmentTypeService);
        appointmentTypeDialog.setHeaderTitle("MODIFICAR TIPO DE CITA");
        appointmentTypeDialog.open();
        appointmentTypeDialog.addListener(AppointmentTypeDialog.SaveEvent.class, this::saveAppointment);
    }

    @Override
    public void updateGrid() {
        masterGrid.setItems(appointmentTypeService.findAll());
    }
}
