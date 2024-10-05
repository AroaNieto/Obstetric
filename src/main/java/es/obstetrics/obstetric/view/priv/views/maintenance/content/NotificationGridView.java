package es.obstetrics.obstetric.view.priv.views.maintenance.content;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.backend.service.NotificationService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.MessagesGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content.RefreshNewsletterConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "secretary/messages", layout = PrincipalView.class)
@PageTitle("MotherBloom-Newsletter")
@PermitAll
public class NotificationGridView extends MasterGrid<NotificationEntity> {

    private final NotificationService notificationService;
    private final ConstantValues constantValues;

    @Autowired
    public NotificationGridView(NotificationService notificationService,
                                ConstantValues constantValues) {
        this.notificationService = notificationService;
        this.constantValues = constantValues;

        setHeader(new H2("MENSAJES"));
        setFilterContainer();
        setGrid();
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
        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE),"help-button","Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE +
                            ConstantUtilities.ROUTER_HELP_CONTENT,
                    "Guía mantenimiento de mensajes");
            windowHelp.open();
        });
        Button printButton = createButton(new Icon(VaadinIcon.PRINT),"help-button","Imprimir listado");
        printButton.addClickListener(event-> printButton());
        searchTextField.setTooltipText("Escriba el nombre o apellidos del destinatario.");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchTextField.setPlaceholder("Buscar al destinatario");
        searchTextField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> notificationService.findByUserEntityNameOrUserEntityLastNameContaining(filter, filter,query.getOffset() / query.getLimit(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> notificationService.findAll(query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });
        searchTextField.addKeyPressListener(Key.ESCAPE, keyPressEvent -> searchTextField.setValue(""));
        masterGrid.setDataProvider(createDataProvider());

        addBtn.setVisible(false);
        filterContainerHl.add(searchTextField,createChanel(),createState(), addBtn,printButton,helpButton);
        filterContainerHl.setFlexGrow(1,searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Los mensajes es pasan mediante carga diferencia, solo cuando el usuario solicita
     *  la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("newsletters.pdf", () -> {
            List<NotificationEntity> messageEntities = getMessagesData();
            return new MessagesGridPdf((ArrayList<NotificationEntity>) messageEntities).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de mensajes");
        dialog.open();
    }

    private List<NotificationEntity> getMessagesData() {
        DataProvider<NotificationEntity, String> dataProvider = createDataProvider();
        Query<NotificationEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }

    /**
     * Crea un DatePicker en la fila de filtros del grid de mensajes, corresponde con el estado de los mensajes.
     * Se agrega el evento de cambio de estado, que actualizará el grid en modo LAZY (cargando los datos de manera diferida):
     * - query.getLimit() obtiene el número máximo de elementos que deben recuperarse.
     * - query.getOffset() obtiene el índice del primer elemento que debe recuperarse.
     * - stream() convierte la lista de mensajes en un Stream para su procesamiento más eficiente.
     *
     * @return El RadioButtonGroup configurado para filtrar los mensajes por estado.
     */
    private RadioButtonGroup<String> createState() {
        RadioButtonGroup<String> radioGroup = createRadioButton("Estado", constantValues.getState());
        radioGroup.addClassName("text-field-1100");
        radioGroup.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> notificationService.findByMessageStateContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return radioGroup;
    }

    /**
     * Crea un DatePicker en la fila de filtros del grid de mensajes, corresponde con el canal de los mensajes.
     * Se agrega el evento de cambio de canal, que actualizará el grid en modo LAZY (cargando los datos de manera diferida):
     * - query.getLimit() obtiene el número máximo de elementos que deben recuperarse.
     * - query.getOffset() obtiene el índice del primer elemento que debe recuperarse.
     * - stream() convierte la lista de mensajes en un Stream para su procesamiento más eficiente.
     *
     * @return El RadioButtonGroup configurado para filtrar los mensajes por canal.
     */
    private RadioButtonGroup<String> createChanel() {
        RadioButtonGroup<String> radioGroup = createRadioButton("Canal", constantValues.getChanel());
        radioGroup.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> notificationService.findByChanelContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        radioGroup.addClassName("text-field-1100");
        return radioGroup;
    }

    private RadioButtonGroup<String> createRadioButton(String label, String[] values) {
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel(label);
        radioGroup.setItems(values);
        radioGroup.getStyle().set("margin-left", "10px");

        return radioGroup;
    }

    /**
     * Creación del grid de mensajes con sus columnas correspondientes.
     */
    @Override
    public void setGrid() {
        masterGrid.addColumn(createAddresseeRenderer()).setHeader("Destinatario").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NotificationEntity::getChanel).setHeader("Canal").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(NotificationEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(notificationEntity -> {
            if(notificationEntity.getShippingDate() != null){
                return notificationEntity.getShippingDate().getDayOfMonth()+"/"+notificationEntity.getShippingDate().getMonthValue()+"/"+notificationEntity.getShippingDate().getYear();
            }
            return null;
        }).setHeader("Fecha de envío").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(notificationEntity -> {
                    if (notificationEntity.getNewsletterEntity() != null) {
                        return notificationEntity.getNewsletterEntity().getName();
                    }else if(notificationEntity.getAppointmentEntity() != null){
                        return " Cita el día "+  notificationEntity.getAppointmentEntity().getDate() + " a las "+notificationEntity.getAppointmentEntity().getStartTime()
                                + " con "+ notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " "
                                + notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName();
                    }
                    return null;
                }
        ).setHeader("Contenido").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(new ComponentRenderer<>(notificationEntity -> {
            if (notificationEntity.getState().equals("Entregado")) {
                Button editBtn = createButton(new Icon(VaadinIcon.REFRESH), "dark-green-background-button","Volver a enviar");

                editBtn.addClickListener(event -> refreshNewsletterDialog(notificationEntity));

                return new HorizontalLayout(editBtn);
            }
            return null;
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private static Renderer<NotificationEntity> createAddresseeRenderer() {
        return LitRenderer.<NotificationEntity>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", notificationEntity -> {
                    byte[] profilePhoto = notificationEntity.getUserEntity().getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", notificationEntity -> notificationEntity.getUserEntity().getName() + " " + notificationEntity.getUserEntity().getLastName())
                .withProperty("email", notificationEntity -> notificationEntity.getUserEntity().getRole());
    }

    private Button createButton(Icon icon, String className, String tooltip) {
        Button button = new Button(icon);
        button.setTooltipText(tooltip);
        button.addClassName(className);
        return button;
    }

    /**
     * Se ejecuta cuando el usuario selecciona el botón de volver a enviar el contenido
     *
     * @param notificationEntity Sobre el que se va a operar.
     */
    private void refreshNewsletterDialog(NotificationEntity notificationEntity) {
        RefreshNewsletterConfirmDialog refreshNewsletterConfirmDialog = new RefreshNewsletterConfirmDialog(notificationEntity);
        refreshNewsletterConfirmDialog.addListener(RefreshNewsletterConfirmDialog.UpdateEvent.class, this::updateNewsletter);
        refreshNewsletterConfirmDialog.open();
    }

    /**
     * Actualiza la noticia con estado no entregado para que se vuelva a enviar.
     *
     * @param updateEvent Evento que salta cuando se cierra el cuaddro de diálogo de actualización.
     */
    private void updateNewsletter(RefreshNewsletterConfirmDialog.UpdateEvent updateEvent) {
        NotificationEntity messageUpdate = updateEvent.getNotificationEntity();
        messageUpdate.setShippingDate(LocalDate.now());
        messageUpdate.setState(ConstantUtilities.MESSAGE_NOT_DELIVERED);
        notificationService.save(messageUpdate);
        updateGrid();
    }

    /**
     * Actualización del grid de mensajes.
     */
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }

    private DataProvider<NotificationEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return notificationService.findAll(offset / limit, limit).get().toList().stream();
                    } else {
                        return notificationService.findByUserEntityNameOrUserEntityLastNameContaining(filter, filter,offset / limit, limit).get().toList().stream();
                    }
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) notificationService.findAll(0, Integer.MAX_VALUE).getTotalElements();
                    } else {
                        return (int) notificationService.findByUserEntityNameOrUserEntityLastNameContaining(filter, filter, 0, Integer.MAX_VALUE).getTotalElements();
                    }
                }
        );
    }
}
