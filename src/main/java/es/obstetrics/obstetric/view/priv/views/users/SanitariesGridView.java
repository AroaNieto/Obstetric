package es.obstetrics.obstetric.view.priv.views.users;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.service.CenterService;
import es.obstetrics.obstetric.backend.service.SanitaryService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.SanitariesGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.SanitaryDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista dónde se mostrarán la tabla con los usuarios y dependiendo del rol,
 * cada sanitario podrá realizar un tipo de acción.
 * Solo podrán acceder los trabajadores, los pacientes no.
 */
@Route(value = "secretary/sanitaries", layout = PrincipalView.class)
@PageTitle("sanitaries")
@PermitAll
/*
@Route(value = "public/users")
@AnonymousAllowed
 */
public class SanitariesGridView extends MasterGrid<SanitaryEntity> {

    private final ConstantValues constantValues;
    private final SanitaryService sanitaryService;
    private final CenterService centerService;
    private final PasswordEncoder passwordEncoder;
    private Button deleteBtn;
    private Button reactivateBtn;
    private final UserCurrent currentUser;
    private final UserService userService;
    /**
     * Constructor, se llaman a los métodos de cración del grid, le HL con los filtros, los eventos de los cuadros de diálogos y
     * la actualización del grid.
     */
    public SanitariesGridView(
            ConstantValues constantValues,
            UserCurrent currentUser,
            SanitaryService sanitaryService,
            CenterService centerService, UserService userService,
            PasswordEncoder passwordEncoder) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.centerService = centerService;
        this.sanitaryService = sanitaryService;
        this.constantValues = constantValues;
        this.passwordEncoder = passwordEncoder;

        setHeader(new H2("TRABAJADORES"));

        setGrid();
        setFilterContainer();
        updateGrid();
        setSizeFull();
    }

    /**
     * Creación del grid de usuario con las columnas correspondientes,
     * - El administrador podra añadir, editar y eliminar los usuarios.
     * - El ginecologo, matrona o secretario podrá añadir y editarlos (antes de que se hayan registrado).
     * Si el código de acceso de registro ha caducado, sale un botón para volver a generarlo.
     */
    @Override
    public void setGrid() {
        masterGrid.addColumn(createSanitaryRenderer()).setHeader("Trabajador").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getDni).setHeader("DNI").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getSex).setHeader("Sexo").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getAge).setHeader("Edad").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getRole).setHeader("Rol").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getAddress).setHeader("Dirección").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getPostalCode).setHeader("Código postal").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getPhone).setHeader("Teléfono").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(SanitaryEntity::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(sanitaryEntity -> {
            List<String> centers = sanitaryEntity.getCenters().stream()
                    .map(CenterEntity::getCenterName)
                    .toList();
            FlexLayout centerList = new FlexLayout();
            centerList.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            centers.forEach(centerName -> centerList.add(new Span(centerName)));

            Details details = new Details("Centros Asociados", centerList);
            details.setOpened(false);
            return new Div(details);
        })).setWidth("300px");
        masterGrid.addColumn(SanitaryEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(userEntity -> {
            if (currentUser.getCurrentUser() != null) {
                if ((currentUser.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_ADMIN))) { //Si el usuario es el administrador
                    Button editBtn = createButton(new Icon(VaadinIcon.EDIT), "dark-green-background-button", "Editar");
                    editBtn.addClickListener(event -> openEditUserDialog(userEntity));

                    reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button", "Reactivar");
                    reactivateBtn.setTooltipText("Reactivar");
                    reactivateBtn.addClickListener(event -> openReactivateSanitary(userEntity));

                    if (userEntity.getState() != null && userEntity.getState().equals(ConstantUtilities.STATE_DISCHARGED)) {
                        deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button", "Dar de baja");
                        deleteBtn.setVisible(false);
                        deleteBtn.getElement().setAttribute("disabled", true);
                        return new HorizontalLayout(editBtn, deleteBtn, reactivateBtn);
                    }

                    reactivateBtn.setVisible(false);
                    deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button", "Dar de baja");
                    deleteBtn.addClickListener(event -> openUnsubscribeUserDialog(userEntity));
                    return new HorizontalLayout(editBtn, deleteBtn);
                }
            }
            return null;

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);
    }

    private static Renderer<SanitaryEntity> createSanitaryRenderer() {
        return LitRenderer.<SanitaryEntity>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", sanitaryEntity -> {
                    byte[] profilePhoto = sanitaryEntity.getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", sanitaryEntity -> sanitaryEntity.getName() + " " + sanitaryEntity.getLastName())
                .withProperty("email", SanitaryEntity::getDni);
    }


    private void openReactivateSanitary(SanitaryEntity userEntity) {
        userEntity.setState(ConstantUtilities.STATE_ACTIVE);
        sanitaryService.save(userEntity);
        deleteBtn.setVisible(true);
        reactivateBtn.setVisible(false);
        updateGrid();
    }


    private Button createButton(Icon icon, String s, String help) {
        Button button = new Button(icon);
        button.addClassName(s);
        button.setTooltipText(help);
        return button;
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta al usuario si está seguro de eliminar el usuario.
     *
     * @param user Usuario que se va a eliminar.
     */
    private void openUnsubscribeUserDialog(SanitaryEntity user) {
        DeleteSanitaryConfirmDialog deleteSanitaryConfirmDialog = new DeleteSanitaryConfirmDialog(user);
        deleteSanitaryConfirmDialog.addListener(DeleteSanitaryConfirmDialog.DeleteEvent.class, this::openUnsubscribeUserDialog);
        deleteSanitaryConfirmDialog.open();
    }

    /**
     * Se abre el cuadro de dialogo para editar al usuario.
     *
     * @param sanitaryEntity Usuario que se va a editar.
     */
    private void openEditUserDialog(SanitaryEntity sanitaryEntity) {
        SanitaryDialog sanitaryDialog = new SanitaryDialog(centerService, sanitaryService, sanitaryEntity, passwordEncoder, userService);

        sanitaryDialog.addListener(SanitaryDialog.SaveEvent.class, this::saveUser);

        sanitaryDialog.setHeaderTitle("MODIFICAR TRABAJADOR");
        sanitaryDialog.open();
    }

    /**
     * Actualización del grid.
     */
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }


    /**
     * Guarda el usuario nuevo o editado.
     * Si se trata de un usuario nuevo, comprueba que no exista ni el email, ni el número de telefono ni el dni
     * -Si es así, muestra un mensaje de error.
     * Si se está editando el usuario, se comprueba si alguno de los campos unicos han sido modificados y si ya existe en la base de datos
     * otro usuario con ese campo.
     * -Si es así, muestra un mensaje de error.
     * Si no han ocurrido errores:
     * - Añade el nuevo sanitario en base de datos
     * - Elimina de la base de datos todas las relacciones sanitario centro que hayan sido eliminadas por el usuario.
     * - Añade las nuevas en la base de datos.
     * -
     *
     * @param saveEvent Evento que salta cuando el usuario pulsa en el botón de guardar en el cuadro de diálogo.
     */
    @Transactional
    private void  saveUser(SanitaryDialog.SaveEvent saveEvent) {
        SanitaryEntity savedSanitaryEntity = sanitaryService.save(saveEvent.getUserEntity());

        // Eliminar todas las relaciones actuales entre el sanitario y los centros.
        savedSanitaryEntity.getCenters().clear();
        sanitaryService.save(savedSanitaryEntity); // Persistir los cambios.

        // Añadir las nuevas relaciones entre el sanitario y los centros.
        saveRelation(saveEvent.getCenters(), savedSanitaryEntity);

        updateGrid();
    }

    private void saveRelation(List<CenterEntity> centers, SanitaryEntity sanitaryEntity) {
        if (centers != null && !centers.isEmpty()) {
            sanitaryEntity.getCenters().addAll(centers); // Asociar los nuevos centros.
            sanitaryService.save(sanitaryEntity); // Persistir los cambios.
        }
    }

    /**
     * Abre el cuadro de diálogo de añadir usuario, como está añadiendo
     * pone el usuario a nulo.
     */
    @Override
    public void openDialog() {
        SanitaryDialog sanitaryDialog = new SanitaryDialog(centerService, sanitaryService, null, passwordEncoder, userService);
        sanitaryDialog.addListener(SanitaryDialog.SaveEvent.class, this::saveUser);
        sanitaryDialog.setHeaderTitle("AÑADIR TRABAJADOR");
        sanitaryDialog.open();
    }

    /**
     * Elimina el usuario.
     *
     * @param deleteEvent Usuario a eliminar
     */
    private void openUnsubscribeUserDialog(DeleteSanitaryConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getUserEntity().setState(ConstantUtilities.STATE_DISCHARGED);
        sanitaryService.save(deleteEvent.getUserEntity());
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        updateGrid();
    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsqeudas en el grid
     */
    @Override
    public void setFilterContainer() {
        searchTextField.setTooltipText("Escriba el nombre del trabajador que desea buscar.");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto

        searchTextField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> sanitaryService.findByNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });

        masterGrid.setDataProvider(createDataProvider());
        searchTextField.addKeyPressListener(Key.ESCAPE, keyPressEvent -> searchTextField.setValue(""));


        addBtn.setTooltipText("Añadir trabajador");
        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE), "help-button","Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTER_HELP_USERS,
                    "Guía gestión de trabajadores");
            windowHelp.open();
        });

        Button printButton = createButton(new Icon(VaadinIcon.PRINT), "help-button", "Imprimir listado");
        printButton.addClickListener(event -> printButton());

        filterContainerHl.add(searchTextField,
                createSearchDniTextField(),
                createSearchEmailTextField(),
                createSearchPhoneTextField(),
                createRole(),
                addBtn, printButton, helpButton);
        filterContainerHl.setFlexGrow(1, searchTextField);
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }


    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Los pacietnes es pasan mediante carga diferencia, solo cuando el usuario solicita
     * la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("trabajador.pdf", () -> {
            List<SanitaryEntity> sanitaries = getSanitaryData();
            return new SanitariesGridPdf((ArrayList<SanitaryEntity>) sanitaries).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de trabajadores");
        dialog.open();
    }

    /**
     * Obtiene los datos del paciente a través del DataProvider (carga diferida)
     *
     * @return La lista del paciente.
     */
    private List<SanitaryEntity> getSanitaryData() {
        DataProvider<SanitaryEntity, String> dataProvider = createDataProvider();
        Query<SanitaryEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }

    private DataProvider<SanitaryEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),offset / limit, limit).get().toList().stream();
                    } else {
                        return sanitaryService.findByNameContainingAndRoleIn(filter, Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),offset / limit, limit).get().toList().stream();
                    }
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) sanitaryService.countByRoles(
                                Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY)
                        );
                    } else {
                        return (int) sanitaryService.countByNameContainingAndRoles(filter, Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY));
                    }
                }
        );
    }


    /**
     * Creación del textfield de buscar por el DNI del usuario.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchDniTextField() {

        TextField searchDniTextField = new TextField("DNI");
        searchDniTextField.setTooltipText("Escriba el DNI que desea buscar.");
        searchDniTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchDniTextField.setPrefixComponent(new Icon(VaadinIcon.USER_CARD));
        searchDniTextField.addClassName("text-field-1300");

        searchDniTextField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> sanitaryService.findByDniContainingAndRoleIn(filter, Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset(), query.getLimit()).stream());

            } else {
                masterGrid.setItems(query -> sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });
        return searchDniTextField;
    }

    /**
     * Creación del textfield de buscar por el DNI del usuario.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchEmailTextField() {

        TextField searchEmailTextField = createTextField("Email",
                "text-field-1100", "Escriba el email que desea buscar.",
                new Icon(VaadinIcon.MAILBOX));

        searchEmailTextField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> sanitaryService.findByEmailContainingAndRoleIn(filter, Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });
        return searchEmailTextField;
    }


    /**
     * Creación del textfield de buscar por el teléfono del usuario.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchPhoneTextField() {

        TextField searchPhoneTextField = createTextField("Teléfono",
                "text-field-1300", "Escriba el teléfono que desea buscar.",
                new Icon(VaadinIcon.PHONE));
        searchPhoneTextField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> sanitaryService.findByPhoneContainingAndRoleIn(filter,Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY), query.getOffset(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });
        return searchPhoneTextField;
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
     * Crea el combobox dónde se aplicarán los filtros del rol
     * en el grid.
     *
     * @return El combobox correspondiente.
     */
    private ComboBox<String> createRole() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setLabel("Rol");
        comboBox.addClassName("text-field-1100");
        comboBox.setPrefixComponent(new Icon(VaadinIcon.USER));
        comboBox.setItems(constantValues.getRole());
        comboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String filter = event.getValue();
                masterGrid.setItems(query -> sanitaryService.findByRoleContaining(filter, query.getOffset(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> sanitaryService.findByRoleIn(Arrays.asList(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY),query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream());
            }
        });
        return comboBox;
    }
}
