package es.obstetrics.obstetric.view.priv.views.users;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import com.vaadin.flow.theme.lumo.LumoIcon;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.service.PatientsLogService;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.PatientsGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.UserCodeConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.UploadConsentDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.users.PatientDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Vista dónde se mostrarán la tabla con los usuarios y dependiendo del rol,
 * cada sanitario podrá realizar un tipo de acción.
 * Solo podrán acceder los trabajadores, los pacientes no.
 */
@Route(value = "workers/patients", layout = PrincipalView.class)
@PageTitle("patients")
@PermitAll
//@RolesAllowed({"ROLE_MATRONA", "ROLE_GINECOLOGO", "ROLE_SECRETARIO", "ROLE_ADMINISTRADOR"})
//@Route(value = "public/users")
//@AnonymousAllowed
public class PatientsGridView extends MasterGrid<PatientEntity> {

    private final PatientService patientService;
    private final UserCurrent currentUser;
    private Button deleteBtn;
    private Button reactivateBtn;
    private Button consentBtn;
    private final ConstantValues constantValues;
    private final PatientsLogService patientsLogService;

    /**
     * Constructor, se llaman a los métodos de cración del grid, le HL con los filtros, los eventos de los cuadros de diálogos y
     * la actualización del grid.
     */
    @Autowired
    public PatientsGridView(PatientService patientService,
                            ConstantValues constantValues,
                            PatientsLogService patientsLogService,
                            UserCurrent currentUser) {

        this.constantValues = constantValues;
        this.patientService = patientService;
        this.currentUser = currentUser;
        this.patientsLogService = patientsLogService;

        setHeader(new H2("PACIENTES"));

        setGrid();
        setFilterContainer();
        updateGrid();
    }

    /**
     * Creación del grid de usuario con las columnas correspondientes,
     * - El administrador podra añadir, editar y eliminar los usuarios.
     * - El ginecologo, matrona o secretario podrá añadir y editarlos (antes de que se hayan registrado).
     * Si el código de acceso de registro ha caducado, sale un botón para volver a generarlo.
     */
    @Override
    public void setGrid() {

        masterGrid.addColumn(createPatientRenderer()).setHeader("Paciente").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getUsername).setHeader("Nombre de usuario").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getChanel).setHeader("Canal").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getAddress).setHeader("Dirección").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getPostalCode).setHeader("Código postal").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getPhone).setHeader("Teléfono").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getAccessCode).setHeader("Codigo de acceso").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(patientEntity -> {
                    if (patientEntity.getAccessCodeDate() != null) {
                        return patientEntity.getAccessCodeDate().getDayOfMonth() + "/" + patientEntity.getAccessCodeDate().getMonthValue() + "/" + patientEntity.getAccessCodeDate().getYear();
                    }
                    return null;
                }
        ).setHeader("Caducidad").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(PatientEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        //Si el usuario es el administrador
        if ((currentUser.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_ADMIN))) {
            masterGrid.addColumn(new ComponentRenderer<>(userEntity -> {
                HorizontalLayout buttonsHl = new HorizontalLayout();
                if (userEntity.getAccessCodeDate() != null && userEntity.getAccessCodeDate().isBefore(LocalDate.now())) {
                    buttonsHl.add(createAddCodeButton(userEntity));
                }
                if (userEntity.getInformedConsent() == null) {
                    createConsentButton(userEntity);
                    buttonsHl.add(consentBtn);
                }
                buttonsHl.add(createDetailsButton(userEntity));
                reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button", "Reactivar");
                reactivateBtn.setTooltipText("Reactivar paciente.");
                reactivateBtn.addClickListener(event -> openReactiveUser(userEntity));

                if (userEntity.getState().equals(ConstantUtilities.STATE_DISCHARGED)) {
                    deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button", "Dar de baja");
                    deleteBtn.setVisible(false);
                    deleteBtn.getElement().setAttribute("disabled", true);
                    buttonsHl.add(createEditButton(userEntity), deleteBtn, reactivateBtn);
                    return buttonsHl;
                }

                reactivateBtn.setVisible(false);
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button", "Dar de baja");
                deleteBtn.addClickListener(event -> openUnsubscribeUserConfirmDialog(userEntity));
                buttonsHl.add(createEditButton(userEntity), deleteBtn, reactivateBtn);
                return buttonsHl;
            })).setFrozenToEnd(true).setFlexGrow(0).setWidth("280px");
        }

        if ((!currentUser.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_ADMIN))) {
            masterGrid.addColumn(new ComponentRenderer<>(userEntity -> {
                HorizontalLayout buttonsHl = new HorizontalLayout();
                if (userEntity.getAccessCodeDate() != null && userEntity.getAccessCodeDate().isBefore(LocalDate.now())) {
                    buttonsHl.add(createAddCodeButton(userEntity));
                }
                if (userEntity.getInformedConsent() == null) {
                    createConsentButton(userEntity);
                    buttonsHl.add(consentBtn);
                }
                if (userEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) { //Si el usuario aún no se ha activado la cuenta
                    buttonsHl.add(createDetailsButton(userEntity), createEditButton(userEntity));
                } else {
                    buttonsHl.add(createDetailsButton(userEntity));
                }
                return buttonsHl;
            })).setFrozenToEnd(true).setFlexGrow(0).setWidth("220px");
        }

    }

    private Button createAddCodeButton(PatientEntity userEntity) {
        Button addCodeBtn = createButton(LumoIcon.RELOAD.create(), "reload-code-button", "Volver a generar el código de acceso");
        addCodeBtn.addClickListener(event -> {
            UserCodeConfirmDialog userCodeConfirmDialog = new UserCodeConfirmDialog(generateCode(), userEntity.getName(), userEntity.getLastName(), userEntity, constantValues);
            userCodeConfirmDialog.addListener(UserCodeConfirmDialog.SaveCode.class, this::closeConfirmCodeDialog);
            userCodeConfirmDialog.open();
        });
        return addCodeBtn;
    }

    private Button createEditButton(PatientEntity userEntity) {
        Button editBtn = createButton(new Icon(VaadinIcon.EDIT), "dark-green-background-button", "Editar");
        editBtn.addClickListener(event -> openEditUserDialog(userEntity));
        return editBtn;
    }

    private void createConsentButton(PatientEntity userEntity) {
        consentBtn = createButton(VaadinIcon.CLIPBOARD_CHECK.create(), "blue-button", "Consentimiento informado");
        consentBtn.addClickListener(event -> {
            UploadConsentDialog uploadConsentDialog = new UploadConsentDialog(userEntity);
            uploadConsentDialog.addListener(UploadConsentDialog.SaveEvent.class, this::closeConsentDialog);
            uploadConsentDialog.open();
        });
    }

    private Button createDetailsButton(PatientEntity userEntity) {
        Button detailsBtn = createButton(new Icon(VaadinIcon.USER_CHECK), "lumo-primary-color-background-button", "Ver detalles");
        detailsBtn.addClickListener(event -> UI.getCurrent().navigate(PatientDetailsView.class, userEntity.getDni()));
        return detailsBtn;
    }

    private void closeConsentDialog(UploadConsentDialog.SaveEvent uploadConsentPfdFormEvent) {
        if (uploadConsentPfdFormEvent.getPatientEntity().getInformedConsent() != null) {
            patientService.save(uploadConsentPfdFormEvent.getPatientEntity());
            consentBtn.setVisible(false);
            PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
            patientsLogEntity.setDate(LocalDate.now());
            patientsLogEntity.setTime(LocalTime.now());
            patientsLogEntity.setMessage("Consentimiento firmado");
            patientsLogEntity.setPatientEntity(uploadConsentPfdFormEvent.getPatientEntity());
            patientsLogEntity.setSanitaryEntity((SanitaryEntity) currentUser.getCurrentUser());
            // Obtener la IP del servidor
            patientsLogEntity.setIp(getServerIp());
            patientsLogService.save(patientsLogEntity);
        }
    }

    // Método para obtener la IP del servidor
    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getLocalAddr();
    }

    private static Renderer<PatientEntity> createPatientRenderer() {
        return LitRenderer.<PatientEntity>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", patientEntity -> {
                    byte[] profilePhoto = patientEntity.getProfilePhoto();
                    if (profilePhoto != null) {
                        return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                        //Si tiene foto de perfil la muestra
                    } else {
                        return "";
                    }
                })
                .withProperty("fullName", patientEntity -> patientEntity.getName() + " " + patientEntity.getLastName())
                .withProperty("email", PatientEntity::getDni);
    }

    private void openReactiveUser(PatientEntity userEntity) {
        userEntity.setState(ConstantUtilities.STATE_ACTIVE);
        deleteBtn.setVisible(true);
        reactivateBtn.setVisible(false);
        patientService.save(userEntity);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Paciente reactivado");
        patientsLogEntity.setPatientEntity(userEntity);
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) currentUser.getCurrentUser());
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);

        updateGrid();
    }

    private String generateCode() {
        byte[] bufferByte = new byte[6]; //Creación de un array de bytes de longitud 6
        SecureRandom secureRandom = new SecureRandom();

        secureRandom.nextBytes(bufferByte);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bufferByte);
    }

    private Button createButton(Icon icon, String className, String tooltip) {
        Button button = new Button(icon);
        button.addClassName(className);
        button.setTooltipText(tooltip);
        return button;
    }

    private void closeConfirmCodeDialog(UserCodeConfirmDialog.SaveCode saveCode) {
        PatientEntity userEntity = saveCode.getUser();
        userEntity.setAccessCodeDate(LocalDate.now().plusDays(10));
        patientService.save(userEntity);
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Nuevo código de acceso para el paciente.");
        patientsLogEntity.setPatientEntity(saveCode.getUser());
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) currentUser.getCurrentUser());
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        patientsLogService.save(patientsLogEntity);
    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     * si está seguro de eliminar el usuario.
     *
     * @param user Usuario que se va a eliminar.
     */
    private void openUnsubscribeUserConfirmDialog(PatientEntity user) {
        DeletePatientsConfirmDialog deletePatientsConfirmDialog = new DeletePatientsConfirmDialog(user);
        deletePatientsConfirmDialog.open();
        deletePatientsConfirmDialog.addListener(DeletePatientsConfirmDialog.DeleteEvent.class, this::unsubscribeUser);
    }

    /**
     * Se abre el cuadro de dialogo para editar al usuario.
     *
     * @param userEntity Usuario que se va a editar.
     */
    private void openEditUserDialog(PatientEntity userEntity) {
        PatientDialog patientDialog = new PatientDialog(userEntity, patientService, constantValues);
        patientDialog.setHeaderTitle("MODIFICAR PACIENTE");
        patientDialog.addListener(PatientDialog.SaveEvent.class, this::saveUser);
        patientDialog.open();

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
     * Si es así, muestra un mensaje de error.
     * Si no es así, guarda el usuario, actualiza el grid y cierra el cuadro de diálogo.
     * Si se está editando el usuario, se comprueba si alguno de los campos unicos han sido modificados y si ya existe en la base de datos
     * otro usuario con ese campo.
     * Si es así, muestra un mensaje de error.
     * Si no es así, guarda el usuario., actualiza el grid y cierra el cuadro de diálogo.
     *
     * @param saveEvent Evento que salta cuando el usuario pulsa en el botón de guardar en el cuadro de diálogo.
     */
    private void saveUser(PatientDialog.SaveEvent saveEvent) {

        patientService.save(saveEvent.getUserEntity());
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Paciente creado/modificado");
        patientsLogEntity.setPatientEntity(saveEvent.getUserEntity());
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) currentUser.getCurrentUser());
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        updateGrid();
    }

    /**
     * Abre el cuadro de diálogo de añadir usuario, como está añadiendo
     * pone el usuario a nulo.
     */
    @Override
    public void openDialog() {
        PatientDialog patientDialog = new PatientDialog(null, patientService, constantValues);
        patientDialog.setHeaderTitle("AÑADIR PACIENTE");
        patientDialog.addListener(PatientDialog.SaveEvent.class, this::saveUser);
        patientDialog.open();
    }

    /**
     * Da de baja el usuario poniendo su estado a inactivo.
     *
     * @param deleteEvent Usuario a dar de baja
     */
    private void unsubscribeUser(DeletePatientsConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getUserEntity().setState(ConstantUtilities.STATE_DISCHARGED);
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        patientService.save(deleteEvent.getUserEntity());
        PatientsLogEntity patientsLogEntity = new PatientsLogEntity();
        patientsLogEntity.setDate(LocalDate.now());
        patientsLogEntity.setTime(LocalTime.now());
        patientsLogEntity.setMessage("Paciente dado de baja");
        patientsLogEntity.setPatientEntity(deleteEvent.getUserEntity());
        patientsLogEntity.setSanitaryEntity((SanitaryEntity) currentUser.getCurrentUser());
        patientsLogEntity.setIp(getServerIp());
        patientsLogService.save(patientsLogEntity);
        updateGrid();
    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsqeudas en el grid
     */
    @Override
    public void setFilterContainer() {
        searchTextField.setTooltipText("Escriba el nombre, apellidos o nombre de usuario del paciente");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query ->
                    patientService.findByNameOrUsernameOrLastNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
        });
        searchTextField.addKeyPressListener(Key.ESCAPE, keyPressEvent -> searchTextField.setValue(""));
        masterGrid.setDataProvider(createDataProvider());

        addBtn.setTooltipText("Añadir paciente");

        Button helpButton = createButton(new Icon(VaadinIcon.QUESTION_CIRCLE), "help-button", "Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTER_HELP_USERS,
                    "Guía gestión de pacientes");
            windowHelp.open();
        });

        Button printButton = createButton(new Icon(VaadinIcon.PRINT), "help-button", "Imprimir listado");
        printButton.addClickListener(event -> printButton());
        filterContainerHl.add(searchTextField,
                createSearchDniTextField(),
                createSearchPhoneTextField(),
                createSearchEmailTextField(),
                 addBtn, printButton, helpButton);

        filterContainerHl.setFlexGrow(1,
                searchTextField);

        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Los pacientes se pasan mediante carga diferencia, solo cuando el usuario solicita
     * la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("pacientes.pdf", () -> {
            List<PatientEntity> patients = getPatientData();
            return new PatientsGridPdf((ArrayList<PatientEntity>) patients).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de pacientes");
        dialog.open();
    }

    /**
     * Obtiene los datos del paciente a través del DataProvider (carga diferida)
     *
     * @return La lista del paciente.
     */
    private List<PatientEntity> getPatientData() {
        DataProvider<PatientEntity, String> dataProvider = createDataProvider();
        Query<PatientEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }

    /**
     * Configura un DataProvider para cargar datos de pacientes de manera diferida y eficiente,
     * optimizando el rendiiento de la aplicaicón al minimizar la carga anticipada de datos.
     */
    private DataProvider<PatientEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset(); //Indice de inicio
                    int limit = query.getLimit(); //Cantidad de elementos a recuprar
                    String filter = query.getFilter().orElse("");
                    if (currentUser.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_ADMIN)) {
                        if (filter.isEmpty()) {
                            return patientService.findAll(
                                    offset / limit, limit).get().toList().stream(); //Devuelve la lista de pacientes segun el offset y el límite y la convierte a stream.
                        } else {
                            return patientService.findByNameOrUsernameOrLastNameContaining(filter, offset / limit, limit).get().toList().stream(); //Devuelve una página de pacientes que coinciden con el filtro y la convierte a stream.
                        }
                    } else {
                        List<String> activeStates = Arrays.asList(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE); // Lista de estados que deseas mostrar
                        Stream<PatientEntity> stream;
                        if (filter.isEmpty()) {
                            stream = patientService.findByState(activeStates, offset / limit, limit).get().toList().stream();
                        } else {
                            stream = patientService.findByStateInAndNameOrUsernameContaining(activeStates, filter, offset / limit, limit).get().toList().stream();
                        }
                        // Filtra pacientes con estado "dado de baja"
                        return stream;
                    }

                },
                query -> { //Obtiene el tamaño total de los datos después de aplicar el filtro.
                    String filter = query.getFilter().orElse("");
                    if (currentUser.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_ADMIN)) {
                        if (filter.isEmpty()) {
                            return (int) patientService.findAll(0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de pacientes en el sistema.
                        } else {
                            return (int) patientService.findByNameOrUsernameOrLastNameContaining(filter, 0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de pacientes que coinciden con el filtro.
                        }
                    } else {
                        List<String> activeStates = Arrays.asList(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE); // Lista de estados que deseas mostrar
                        if (filter.isEmpty()) {
                            return (int) patientService.findByState(activeStates, 0, Integer.MAX_VALUE).getTotalElements(); // Devuelve el número total de pacientes activos en el sistema.
                        } else {
                            return (int) patientService.findByStateInAndNameOrUsernameContaining(activeStates, filter, 0, Integer.MAX_VALUE).getTotalElements(); // Devuelve el número total de pacientes activos que coinciden con el filtro.
                        }
                    }

                }
        );
    }

    /**
     * Creación del textfield de buscar por el email del usuario.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchEmailTextField() {

        TextField searchEmailTextField = new TextField("Email");
        searchEmailTextField.setTooltipText("Escriba el email que desea buscar.");
        searchEmailTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchEmailTextField.setPrefixComponent(new Icon(VaadinIcon.MAILBOX));
        searchEmailTextField.addClassName("text-field-1100");
        searchEmailTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> patientService.findByEmailContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchEmailTextField;
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
        searchDniTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> patientService.findByDniContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchDniTextField;
    }

    /**
     * Creación del textfield de buscar por el teléfono del usuario.
     *
     * @return El texfield de buscar.
     */
    private TextField createSearchPhoneTextField() {
        TextField searchPhoneTextField = new TextField("Teléfono");
        searchPhoneTextField.addClassName("text-field-1300");
        searchPhoneTextField.setTooltipText("Escriba el teléfono que desea buscar.");
        searchPhoneTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        searchPhoneTextField.setPrefixComponent(new Icon(VaadinIcon.PHONE));
        searchPhoneTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> patientService.findByPhoneContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return searchPhoneTextField;
    }
}
