package es.obstetrics.obstetric.view.priv.views.messengerService;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.MessengerServiceEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.MessengerServiceService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Route(value = "protected/system-message-sanitary-patient", layout = PrincipalView.class)
@PermitAll
public class SelectedMessengerServiceView extends HorizontalLayout {
    private MessengerServiceView messengerServiceView = new MessengerServiceView();
    private final MessengerServiceService messengerServiceService;
    private final UserCurrent userCurrent;
    private final UserService userService;
    private final Div openMessengerServiceVl;
    private Set<Long> openMessengerService;
    private List<UserEntity> usersOpenMessengerService;
    private boolean isMessengerServiceEnabled = true;
    private UserEntity selectedUser;
    private final ScheduledExecutorService scheduler;

    @Autowired
    public SelectedMessengerServiceView(UserCurrent userCurrent,
                                        MessengerServiceService messengerServiceService,
                                        UserService userService) {
        this.userCurrent = userCurrent;
        this.messengerServiceService = messengerServiceService;
        this.userService = userService;
        openMessengerService = new HashSet<>();
        usersOpenMessengerService = new ArrayList<>();
        openMessengerServiceVl = new Div();

        Div container = createContainer();
        FlexLayout flexLayout;
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_MATRONE) || userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
            RadioButtonGroup<String> myState = createToggleButton();
            VerticalLayout myStateHl = new VerticalLayout(myState);
            showOpenMessengerService(getOpenMessengerServices());
            flexLayout = createFlexLayout(myStateHl, container);
        } else {
            showOpenMessengerService(getOpenMessengerServices());
            flexLayout = new FlexLayout(container);
            flexLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            flexLayout.add(container);
            flexLayout.expand(container);
        }

        flexLayout.setHeightFull();
        flexLayout.setWidth("300px");

        flexLayout.getStyle().set("background-color", "white")
                .set("overflow-y", "auto");
        add(flexLayout, messengerServiceView);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkForNewMessages, 0, 1, TimeUnit.MINUTES); //La tarea se ejecutará cada minuto
    }

    private void checkForNewMessages() {
        getUI().ifPresent(ui -> {
            if (ui.isAttached()) {
                ui.access(() -> {
                    HorizontalLayout activemessengerServiceLayout = null;

                    // Guardar el estado del messengerService activo
                    for (Component component : openMessengerServiceVl.getChildren().toList()) {
                        if (component instanceof HorizontalLayout && component.hasClassName("messenger-service-active")) {
                            activemessengerServiceLayout = (HorizontalLayout) component;
                            break;
                        }
                    }

                    List<MessengerServiceEntity> allMessages = messengerServiceService.findBySenderIdOrReceiverId(
                            userCurrent.getCurrentUser().getId(),
                            userCurrent.getCurrentUser().getId()
                    );

                    if (allMessages != null) {
                        List<MessengerServiceEntity> hasNewMessages = new ArrayList<>();
                        for (MessengerServiceEntity message : allMessages) {
                            if (message.getTimestamp().isAfter(Instant.now().minusSeconds(60)) &&
                                    message.getReceiver().getId().equals(userCurrent.getCurrentUser().getId())) {
                                hasNewMessages.add(message);
                            }
                        }

                        if (!hasNewMessages.isEmpty()) {
                            for (MessengerServiceEntity message : hasNewMessages) {
                                if (selectedUser != null && selectedUser.getId().equals(message.getSenderId())) {
                                    openMessengerService();
                                    break;
                                }
                            }
                        }
                    }

                    // Mostrar los messengerServices abiertos y actualizar el estado del messengerService
                    showOpenMessengerService(getOpenMessengerServices());

                    // Re-aplicar la clase activa al componente correspondiente, como se ha creado de nuevo hay que encontrar el nuevo componente
                    if (activemessengerServiceLayout != null) {
                        String activeUserId = ((SideNavItem) activemessengerServiceLayout.getComponentAt(0)).getLabel();
                        for (Component component : openMessengerServiceVl.getChildren().toList()) {
                            if (component instanceof HorizontalLayout) {
                                HorizontalLayout layout = (HorizontalLayout) component;
                                SideNavItem item = (SideNavItem) layout.getComponentAt(0);
                                if (item.getLabel().equals(activeUserId)) {
                                    layout.addClassName("messenger-service-active");
                                    break;
                                }
                            }
                        }
                    }

                    ui.push();
                });
            }
        });
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }


    private FlexLayout createFlexLayout(VerticalLayout mymessengerServiceStateHl, Div container) {
        FlexLayout flexLayout = new FlexLayout();
        flexLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        flexLayout.add(container);
        flexLayout.expand(container);
        flexLayout.add(mymessengerServiceStateHl);
        flexLayout.setAlignSelf(Alignment.END, mymessengerServiceStateHl);
        return flexLayout;
    }

    /**
     * Crea el contenedor con los botones y el mensaje de messengerService abiertos.
     *
     * @return El contenedor
     */
    private Div createContainer() {
        HorizontalLayout createButtons = createButtons();
        VerticalLayout buttonsAndLabel = new VerticalLayout(createButtons, new H5("Mensajes abiertos"));
        buttonsAndLabel.setSpacing(true);
        buttonsAndLabel.setPadding(true);
        setHeightFull();
        return new Div(buttonsAndLabel, openMessengerServiceVl);
    }

    /**
     * Crea los botones en los que se podrá añadir un nuevo messengerService o buscar un messengerService existente para escribir.
     *
     * @return El HL con los botones
     */
    private HorizontalLayout createButtons() {

        HorizontalLayout addButtonHl = new HorizontalLayout(createAddButton());
        addButtonHl.setJustifyContentMode(JustifyContentMode.START);

        HorizontalLayout viewButtonHl = new HorizontalLayout(createViewPatientButton());
        addButtonHl.setJustifyContentMode(JustifyContentMode.END);

        HorizontalLayout buttonsHl = new HorizontalLayout(addButtonHl, viewButtonHl);
        buttonsHl.setSizeFull();
        return buttonsHl;
    }

    private Button createViewPatientButton() {
        // Botón para escoger el messengerService
        Button viewmessengerServiceButton = new Button(VaadinIcon.MAILBOX.create());
        viewmessengerServiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewmessengerServiceButton.setTooltipText("Buscar un messengerService existente");
        viewmessengerServiceButton.addClickListener(event -> {
            createUpdateDialog();
        });

        return viewmessengerServiceButton;
    }

    private void createUpdateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Escoja con quien quiere conversar.");

        ComboBox<UserEntity> sanitaryEntityComboBox = new ComboBox<>("Conversar");
        sanitaryEntityComboBox.setItems(usersOpenMessengerService);

        Button button = new Button("Buscar usuario ");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(
                event -> {
                    if (!sanitaryEntityComboBox.isEmpty()) {
                        openUpdatemessengerService(sanitaryEntityComboBox.getValue());
                        dialog.close();
                    }
                }
        );
        Button cancelBtn = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cancelBtn.addClassName("lumo-error-color-background-button");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getHeader().add(cancelBtn);

        VerticalLayout dialogVl = new VerticalLayout(sanitaryEntityComboBox, button);
        dialogVl.setAlignItems(Alignment.STRETCH); //Los componentes ocuparán el ancho completo
        dialogVl.getStyle().set("width", "30rem")
                .set("max-width", "100%");

        dialog.setHeaderTitle("Buscar conversación");
        dialog.add(dialogVl);
        dialog.open();
    }

    private void openUpdatemessengerService(UserEntity user) {
        selectedUser = user;
        // Eliminar la clase activa de todos los elementos
        for (Component component : openMessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }

        Map<UserEntity, HorizontalLayout> usermessengerServiceMap = new HashMap<>();
        for (Component component : openMessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                HorizontalLayout hl = (HorizontalLayout) component;
                SideNavItem item = (SideNavItem) hl.getComponentAt(0);
                String fullName = user.getUsername();
                if (item.getLabel().equals(fullName)) {
                    usermessengerServiceMap.put(user, hl);
                }
            }
        }

        // Agregar la clase activa al elemento correspondiente
        HorizontalLayout targetLayout = usermessengerServiceMap.get(selectedUser);
        if (targetLayout != null) {
            targetLayout.addClassName("messenger-service-active");
        }
        openMessengerService();
    }

    private Component createAddButton() {
        // Botón para añadir un nuevo messengerService
        Button addButton = new Button(VaadinIcon.PLUS_CIRCLE.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setTooltipText("Añadir un nuevo messengerService");
        addButton.addClickListener(event -> createStartmessengerService());
        return addButton;
    }

    /**
     * RadioButton que almacenará el estado del messengerService del sanitario, si está en activado podrá realizar las funcionalidades
     * si está desactivado se deshabilitarán todas las funcionalidades.
     *
     * @return El radiobutton para que el sanitario pueda escoger su estado.
     */
    private RadioButtonGroup<String> createToggleButton() {
        RadioButtonGroup<String> isActive = new RadioButtonGroup<>("Estado");
        isActive.setItems(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE);
        if (userCurrent.getCurrentUser().getStateMessagingSystemPatient() == null) {
            isActive.setValue(ConstantUtilities.STATE_ACTIVE);
            updateUsermessengerServiceActive();
            ismessengerServiceEnabledTrue();
        } else {
            if (ConstantUtilities.STATE_ACTIVE.equals(userCurrent.getCurrentUser().getStateMessagingSystemPatient())) {
                ismessengerServiceEnabledTrue();
                updatemessengerServiceStatus();
            } else {
                ismessengerServiceEnabledFalse();
            }
            isActive.setValue(userCurrent.getCurrentUser().getStateMessagingSystemPatient());
        }
        /*
         * Se habilita o se deshabilitan los messengerService dependiendo del
         *  estado del sanitario.
         */
        isActive.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (ConstantUtilities.STATE_ACTIVE.equals(selected)) {
                updateUsermessengerServiceActive();
                ismessengerServiceEnabledTrue();
                updatemessengerServiceStatus();
            } else {
                updateUsermessengerServiceInactive();
                ismessengerServiceEnabledFalse();
                updatemessengerServiceStatus();
                remove(messengerServiceView);
            }
        });

        return isActive;
    }

    private void updateUsermessengerServiceInactive() {
        userCurrent.getCurrentUser().setStateMessagingSystemPatient(ConstantUtilities.STATE_INACTIVE);
        userService.save(userCurrent.getCurrentUser());
        // Eliminar la clase activa de todos los elementos
        for (Component component : openMessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }
    }

    private void ismessengerServiceEnabledFalse() {
        isMessengerServiceEnabled = false;
    }

    private void ismessengerServiceEnabledTrue() {
        isMessengerServiceEnabled = true;
    }

    /**
     * Guarda el estado del messengerService del sanitario.
     */
    private void updateUsermessengerServiceActive() {
        userCurrent.getCurrentUser().setStateMessagingSystemPatient(ConstantUtilities.STATE_ACTIVE);
        userService.save(userCurrent.getCurrentUser());
    }

    /**
     * Cuando el usuario decide habilitar/deshabilitar su chay se cambia su clase CSS para un vista interactiva.
     * Si se trata de un paciente, se comprueba si los sanitarios tienen los messengerServices desactivados para no
     * activarlos. Sino se desactiva o activa según su estado.
     */
    private void updatemessengerServiceStatus() {
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            for (Component component : openMessengerServiceVl.getChildren().toList()) {
                if (component instanceof HorizontalLayout) {
                    HorizontalLayout inboxLinHl = (HorizontalLayout) component;

                    // Obtener el usuario asociado al HorizontalLayout
                    SideNavItem item = (SideNavItem) inboxLinHl.getComponentAt(0);
                    String[] fullName = item.getLabel().split(" ");
                    String username = fullName[0].trim();
                    UserEntity user = usersOpenMessengerService.stream()
                            .filter(u -> {
                                return u.getUsername().equalsIgnoreCase(username);
                            })
                            .findFirst()
                            .orElse(null);

                    // Verificar si el usuario tiene el messengerService activo para determinar la visibilidad
                    if (user != null && user.getStateMessagingSystemPatient() != null && user.getStateMessagingSystemPatient().equals(ConstantUtilities.STATE_ACTIVE)) {
                        inboxLinHl.removeClassName("disabled");
                        inboxLinHl.addClassName("enable-link");
                        inboxLinHl.setEnabled(true);
                    } else {
                        inboxLinHl.setEnabled(false);
                        inboxLinHl.addClassName("disabled");
                        inboxLinHl.removeClassName("enable-link");
                    }
                }
            }
        } else {
            for (Component component : openMessengerServiceVl.getChildren().toList()) {
                if (component instanceof HorizontalLayout inboxLinHl) {
                    if (isMessengerServiceEnabled) {
                        inboxLinHl.removeClassName("disabled");
                        inboxLinHl.addClassName("enable-link");
                        inboxLinHl.setEnabled(true);
                    } else {
                        inboxLinHl.setEnabled(false);
                        inboxLinHl.addClassName("disabled");
                        inboxLinHl.removeClassName("enable-link");
                    }
                }
            }
        }

    }

    private List<Long> getOpenMessengerServices() {
        List<MessengerServiceEntity> allMessages = messengerServiceService.findBySenderIdOrReceiverId(userCurrent.getCurrentUser().getId(), userCurrent.getCurrentUser().getId());
        openMessengerService = new HashSet<>();
        if (allMessages != null) {
            if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
                for (MessengerServiceEntity message : allMessages) {
                    if (!message.getSenderId().equals(userCurrent.getCurrentUser().getId())) {   // Si el usuario es el remitente
                        openMessengerService.add(message.getSenderId());
                    } else if (!message.getReceiver().getId().equals(userCurrent.getCurrentUser().getId())) { // Si el usuario es el receptor
                        openMessengerService.add(message.getReceiver().getId());
                    }
                }
            } else {
                List<UserEntity> sanitaries = userService.findByRole(ConstantUtilities.ROLE_GYNECOLOGIST);
                sanitaries.addAll(userService.findByRole(ConstantUtilities.ROLE_MATRONE));
                for (MessengerServiceEntity message : allMessages) {
                    if (!message.getSenderId().equals(userCurrent.getCurrentUser().getId()) &&
                            userService.findById(message.getSenderId()).get().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {   // Si el usuario es el paciente
                        openMessengerService.add(message.getSenderId());
                    } else if (!message.getReceiver().getId().equals(userCurrent.getCurrentUser().getId()) &&
                            message.getReceiver().getRole().equals(ConstantUtilities.ROLE_PATIENT)) { // Si el usuario es el paciente
                        openMessengerService.add(message.getReceiver().getId());
                    }
                }
            }
            // ELimina el usuario actual de la lista de messengerServices abiertos, porque no puede tener un messengerService consigo mismo
            openMessengerService.remove(userCurrent.getCurrentUser().getId());
        }

        return new ArrayList<>(openMessengerService);
    }

    /**
     * Crea el VL con los usuarios que tienen messengerService abiertos y comprueba si tiene mensajes sin haber leído.
     *
     * @param openmessengerServices Lista de id de usuarios con los messengerServices abiertos.
     */
    private void showOpenMessengerService(List<Long> openmessengerServices) {
        removeOpenmessengerServiceVl();
        usersOpenMessengerService = new ArrayList<>();
        for (Long openMessengerService : openmessengerServices) {
            if (userService.findById(openMessengerService).isPresent()) {
                usersOpenMessengerService.add(userService.findById(openMessengerService).get());
            }
        }
        for (UserEntity user : usersOpenMessengerService) {

            List<MessengerServiceEntity> messages = messengerServiceService.findBySenderIdOrReceiverId(user.getId(), user.getId());
            messages.sort(Comparator.comparing(MessengerServiceEntity::getTimestamp)); // Ordena los messengerServices para que se muestren de los más recientes a los menos
            int value = 0;
            for (MessengerServiceEntity onemessengerService : messages) {
                if (onemessengerService.getReceiver().getId().equals(userCurrent.getCurrentUser().getId()) && onemessengerService.getReadingDate() == null) {
                    value++;
                }
            }
            SideNavItem inboxLink = new SideNavItem(user.getUsername());
            if (value != 0) {
                Span inboxCounter = new Span(String.valueOf(value));
                inboxLink.setSuffixComponent(inboxCounter);
                inboxLink.getSuffixComponent().addClassName("suffix-inbox");
            }

            HorizontalLayout inboxLinHl = createInboxLinkHl(inboxLink, user);
            if (user.getStateMessagingSystemPatient() != null && user.getStateMessagingSystemPatient().equals(ConstantUtilities.STATE_INACTIVE)) {
                inboxLinHl.setEnabled(false);
                inboxLinHl.addClassName("disabled");
                inboxLinHl.removeClassName("enable-link");
                if (selectedUser != null && selectedUser.getId().equals(user.getId())) { //Si el usuario estaba en el mensaje del médico, se elimina para que no pueda mandar más mensajes
                    remove(messengerServiceView);
                }
            }
            openMessengerServiceVl.add(inboxLinHl);
        }
        if (!userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            updatemessengerServiceStatus();
        }
    }

    private void removeOpenmessengerServiceVl() {
        openMessengerServiceVl.removeAll();
    }

    /**
     * Crea uno de los usuarios que estará en la lista de messengerServices abiertos.
     *
     * @param inboxLink El enlace
     * @param user      El usuario que se añadirá al enlace.
     * @return El HL con el enlace.
     */
    private HorizontalLayout createInboxLinkHl(SideNavItem inboxLink, UserEntity user) {
        HorizontalLayout inboxLinHl = new HorizontalLayout(inboxLink);
        inboxLinHl.addClassName("messenger-service-item");
        inboxLinHl.setSpacing(false);

        inboxLinHl.addClickListener(event -> {
            // Eliminar la clase activa de todos los elementos
            for (Component component : openMessengerServiceVl.getChildren().toList()) {
                if (component instanceof HorizontalLayout) {
                    component.removeClassName("messenger-service-active");
                }
            }

            // Agregar la clase activa al elemento clicado
            inboxLinHl.addClassName("messenger-service-active");
            if (isMessengerServiceEnabled) {
                inboxLink.setSuffixComponent(null);
                selectedUser = user;
                openMessengerService();
            }
            // updatemessengerServiceStatus();
        });
        return inboxLinHl;
    }

    /**
     * Elimina el messengerService actual y lo crea de nuevo.
     */
    private void openMessengerService() {
        if (messengerServiceView != null) {
            remove(messengerServiceView);
        }
        messengerServiceView = new MessengerServiceView(userCurrent, selectedUser, messengerServiceService, userService);
        add(messengerServiceView);
    }

    /**
     * Crea el botón para que el usuario pueda añadir una nueva conversación con un sanitario/paciente que no tenga en su messengerService de abiertos.
     */
    private void createStartmessengerService() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Escoja con quien quiere iniciar una conversación.");

        ComboBox<UserEntity> sanitaryEntityComboBox = new ComboBox<>("Conversar");
        sanitaryEntityComboBox.setItems(getAvailableUsers());

        Button button = new Button("Buscar usuario ");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(
                event -> {
                    if (!sanitaryEntityComboBox.isEmpty()) {
                        newmessengerService(sanitaryEntityComboBox.getValue());
                        dialog.close();
                    }
                }
        );
        Button cancelBtn = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        cancelBtn.addClassName("lumo-error-color-background-button");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getHeader().add(cancelBtn);

        VerticalLayout dialogVl = new VerticalLayout(sanitaryEntityComboBox, button);
        dialogVl.setAlignItems(Alignment.STRETCH); //Los componentes ocuparán el ancho completo
        dialogVl.getStyle().set("width", "30rem")
                .set("max-width", "100%");

        dialog.setHeaderTitle("Añadir nueva comunicación");
        dialog.add(dialogVl);
        dialog.open();
    }

    private void newmessengerService(UserEntity userEntity) {
        selectedUser = userEntity;
        openMessengerService();
        SideNavItem inboxLink = new SideNavItem(selectedUser.getUsername());
        HorizontalLayout inboxLinHl = createInboxLinkHl(inboxLink, selectedUser);

        for (Component component : openMessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }
        inboxLinHl.addClassName("messenger-service-active");

        openMessengerServiceVl.add(inboxLinHl);
        usersOpenMessengerService.add(selectedUser);
    }

    private List<UserEntity> getAvailableUsers() {
        if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
            List<UserEntity> sanitaries = userService.findByRole(ConstantUtilities.ROLE_GYNECOLOGIST);
            sanitaries.addAll(userService.findByRole(ConstantUtilities.ROLE_MATRONE));

            Set<UserEntity> nomessengerServices = new HashSet<>();

            for (UserEntity sanitaryEntity : sanitaries) {
                if (sanitaryEntity.getStateMessagingSystemPatient() == null || sanitaryEntity.getStateMessagingSystemPatient().equals(ConstantUtilities.STATE_ACTIVE)) { //Si el sanitario tiene el messengerService activo
                    boolean cont = isCont(sanitaryEntity);
                    if (sanitaryEntity.getId().equals(userCurrent.getCurrentUser().getId())) {
                        cont = false;
                    }
                    if (cont) {
                        nomessengerServices.add(sanitaryEntity);
                    }
                }

            }
            // Remover el usuario actual de la lista de messengerServices abiertos, porque no puede tener un messengerService consigo mismo
            nomessengerServices.remove(userCurrent.getCurrentUser());

            return new ArrayList<>(nomessengerServices);
        } else if (userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_MATRONE) ||
                userCurrent.getCurrentUser().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
            Set<UserEntity> nomessengerServices = new HashSet<>();
            if (isMessengerServiceEnabled) { //Si mi messengerService está activo
                List<UserEntity> patients = userService.findByRole(ConstantUtilities.ROLE_PATIENT);
                for (UserEntity patientEntity : patients) {
                    if (patientEntity.getState().equals(ConstantUtilities.STATE_ACTIVE)) {
                        boolean cont = true;

                        for (UserEntity oneUsermessengerService : usersOpenMessengerService) {
                            if (oneUsermessengerService.getRole().equals(ConstantUtilities.ROLE_PATIENT)) {
                                if (patientEntity.getId().equals(oneUsermessengerService.getId())) {
                                    cont = false;
                                }
                            }
                        }
                        if (cont) {
                            nomessengerServices.add(patientEntity);
                        }
                    }
                }
                // Remover el usuario actual de la lista de messengerServices abiertos, porque no puede tener un messengerService consigo mismo
                nomessengerServices.remove(userCurrent.getCurrentUser());
            }

            return new ArrayList<>(nomessengerServices);
        }
        return new ArrayList<>(); // Si no hay roles válidos, retornar una lista vacía
    }

    private boolean isCont(UserEntity sanitaryEntity) {
        boolean cont = true;
        for (UserEntity oneUsermessengerService : usersOpenMessengerService) {
            if (oneUsermessengerService.getRole().equals(ConstantUtilities.ROLE_MATRONE) ||
                    oneUsermessengerService.getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                if (sanitaryEntity.getId().equals(oneUsermessengerService.getId())) {
                    cont = false;
                }
            }

        }
        return cont;
    }
}
