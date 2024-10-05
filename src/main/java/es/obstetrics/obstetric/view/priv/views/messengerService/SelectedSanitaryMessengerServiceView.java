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

@Route(value = "sanitary/messenger-service-sanitary-sanitary", layout = PrincipalView.class)
@PermitAll
public class SelectedSanitaryMessengerServiceView extends HorizontalLayout {
    private MessengerServiceView messengerServiceView = new MessengerServiceView();
    private final MessengerServiceService messengerServiceService;
    private final UserCurrent userCurrent;
    private final UserService userService;
    private final Div openmessengerServiceVl;
    private Set<Long> openmessengerServices;
    private List<UserEntity> usersOpenmessengerService;
    private boolean ismessengerServiceEnabled = true;
    private UserEntity selectedUser;
    private final ScheduledExecutorService scheduler;

    @Autowired
    public SelectedSanitaryMessengerServiceView(UserCurrent userCurrent,
                                                MessengerServiceService messengerServiceService,
                                                UserService userService) {
        this.userCurrent = userCurrent;
        this.messengerServiceService = messengerServiceService;
        this.userService = userService;
        openmessengerServices = new HashSet<>();
        usersOpenmessengerService = new ArrayList<>();
        openmessengerServiceVl = new Div();

        VerticalLayout mainContainerVl = new VerticalLayout();
        mainContainerVl.setSizeFull();

        showOpenmessengerServices(getOpenmessengerServices());
        add(createFl(new VerticalLayout(createToggleButton()), createContainer()), messengerServiceView);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkForNewMessages, 0, 1, TimeUnit.MINUTES); //La tarea se ejecutará cada minuto
    }

    private void checkForNewMessages() {

        getUI().ifPresent(ui -> {
            if (ui.isAttached()) {
                ui.access(() -> {
                    HorizontalLayout activemessengerServiceLayout = null;

                    // Guardar el estado del messengerService activo
                    for (Component component : openmessengerServiceVl.getChildren().toList()) {
                        if (component instanceof HorizontalLayout && component.hasClassName("messenger-service-active")) {
                            activemessengerServiceLayout = (HorizontalLayout) component;
                            break;
                        }
                    }
                    List<MessengerServiceEntity> allMessages = messengerServiceService.findBySenderIdOrReceiverId(userCurrent.getCurrentUser().getId(), userCurrent.getCurrentUser().getId());
                    if (allMessages != null) {
                        List<MessengerServiceEntity> hasNewMessages = new ArrayList<>();
                        for (MessengerServiceEntity message : allMessages) {
                            if (message.getTimestamp().isAfter(Instant.from(Instant.now().minusSeconds(60))) &&
                                    message.getReceiver().getId().equals(userCurrent.getCurrentUser().getId())) {
                                hasNewMessages.add(message);
                            }
                        }
                        if (!hasNewMessages.isEmpty()) {
                            updatemessengerService();
                            if (selectedUser != null) {
                                createmessengerService();
                            }
                        }
                    }
                    if (ismessengerServiceEnabled) { //Comprueob si tengo el messengerService habilitado para actualizar los mensajes, sino, no se actualizarían
                        showOpenmessengerServices(getOpenmessengerServices()); //Actualizar los messengerServices abiertos
                    }
                    // Re-aplicar la clase activa al componente correspondiente, como se ha creado de nuevo hay que encontrar el nuevo componente
                    if (activemessengerServiceLayout != null) {
                        String activeUserId = ((SideNavItem) activemessengerServiceLayout.getComponentAt(0)).getLabel();
                        for (Component component : openmessengerServiceVl.getChildren().toList()) {
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

    private void removeOpenmessengerServiceVl() {
        openmessengerServiceVl.removeAll();
    }

    private void updatemessengerService() {
        if (messengerServiceView != null) {
            remove(messengerServiceView);
        }
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
        Div container = new Div(buttonsAndLabel, openmessengerServiceVl);
        setHeightFull();
        setSpacing(true);
        return container;
    }

    /**
     * Crea los botones en los que se podrá añadir un nuevo messengerService o buscar un messengerService existente para escribir.
     *
     * @return El HL con los botones
     */
    private HorizontalLayout createButtons() {

        HorizontalLayout addButtonHl = new HorizontalLayout(createAddButton());
        addButtonHl.setJustifyContentMode(JustifyContentMode.START);
        addButtonHl.setJustifyContentMode(JustifyContentMode.END);

        HorizontalLayout buttonsHl = new HorizontalLayout(addButtonHl, new HorizontalLayout(createViewSanitaryButton()));
        buttonsHl.setSizeFull();
        return buttonsHl;
    }

    private Button createViewSanitaryButton() {
        Button viewMessagesButton = new Button(VaadinIcon.MAILBOX.create());
        viewMessagesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewMessagesButton.setTooltipText("Buscar un messengerService existente");
        viewMessagesButton.addClickListener(event -> createUpdateDialog());
        return viewMessagesButton;
    }

    private void createUpdateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Escoja con quien quiere conversar.");

        ComboBox<UserEntity> sanitaryEntityComboBox = new ComboBox<>("Conversar");
        sanitaryEntityComboBox.setItems(usersOpenmessengerService);

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
        for (Component component : openmessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }

        Map<UserEntity, HorizontalLayout> usermessengerServiceMap = new HashMap<>();
        for (Component component : openmessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout hl) {
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
        openmessengerService();
    }

    private FlexLayout createFl(VerticalLayout mymessengerServiceStateHl, Div container) {
        FlexLayout flexLayout = new FlexLayout();
        flexLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        flexLayout.setHeightFull();
        flexLayout.add(container);
        flexLayout.expand(container);
        flexLayout.add(mymessengerServiceStateHl);
        flexLayout.setAlignSelf(Alignment.END, mymessengerServiceStateHl);
        flexLayout.getStyle().set("background-color", "white")
                .set("width", "300px")
                .set("overflow-y", "auto");
        return flexLayout;
    }

    private Component createAddButton() {
        // Botón para añadir un nuevo messengerService
        Button addButton = new Button(VaadinIcon.PLUS_CIRCLE.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setTooltipText("Añadir un nuevo messengerService");
        addButton.addClickListener(event -> createStartmessengerService());
        return addButton;
    }

    private RadioButtonGroup<String> createToggleButton() {
        RadioButtonGroup<String> isActive = new RadioButtonGroup<>("Estado");
        isActive.setItems(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE);
        if (userCurrent.getCurrentUser().getStateMessagingSystemSanitary() == null) {
            isActive.setValue(ConstantUtilities.STATE_ACTIVE);
            updateUsermessengerServiceActive();
            ismessengerServiceEnabledTrue();
        } else {
            if (ConstantUtilities.STATE_ACTIVE.equals(userCurrent.getCurrentUser().getStateMessagingSystemSanitary())) {
                ismessengerServiceEnabledTrue();
                updatemessengerServiceStatus();
            } else {
                ismessengerServiceEnabledFalse();
            }
            isActive.setValue(userCurrent.getCurrentUser().getStateMessagingSystemSanitary());
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
                updatemessengerService();
            }
        });

        return isActive;
    }

    private void updateUsermessengerServiceInactive() {
        userCurrent.getCurrentUser().setStateMessagingSystemSanitary(ConstantUtilities.STATE_INACTIVE);
        userService.save(userCurrent.getCurrentUser());
        // Eliminar la clase activa de todos los elementos
        for (Component component : openmessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }
    }

    private void ismessengerServiceEnabledFalse() {
        ismessengerServiceEnabled = false;
    }

    private void ismessengerServiceEnabledTrue() {
        ismessengerServiceEnabled = true;
    }

    private void updateUsermessengerServiceActive() {
        userCurrent.getCurrentUser().setStateMessagingSystemSanitary(ConstantUtilities.STATE_ACTIVE);
        userService.save(userCurrent.getCurrentUser());
    }

    /**
     * Cuando el usuario decide habilitar/deshabilitar su chay se cambia su clase CSS para un vista interactiva.
     */
    private void updatemessengerServiceStatus() {
        for (Component component : openmessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout inboxLinHl) {

                // Obtener el usuario asociado al HorizontalLayout
                SideNavItem item = (SideNavItem) inboxLinHl.getComponentAt(0);
                String[] fullName = item.getLabel().split(" "); // Obtener el nombre completo del usuario
                String username = fullName[0];

                // Encontrar el usuario correspondiente en la lista de usuarios abiertos
                UserEntity user = usersOpenmessengerService.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);

                // Verificar si el sanitario tiene el messengerService activo para determinar la visibilidad
                if (user != null && user.getStateMessagingSystemSanitary() != null && user.getStateMessagingSystemSanitary().equals(ConstantUtilities.STATE_ACTIVE)) {
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

    private List<Long> getOpenmessengerServices() {
        List<MessengerServiceEntity> allMessages = messengerServiceService.findBySenderIdOrReceiverId(userCurrent.getCurrentUser().getId(), userCurrent.getCurrentUser().getId());
        openmessengerServices = new HashSet<>();
        if (allMessages != null) {
            for (MessengerServiceEntity message : allMessages) {
                Optional<UserEntity> userSender = userService.findById(message.getSenderId());
                Optional<UserEntity> userReceive = userService.findById(message.getReceiver().getId());
                if (userSender.isPresent() && userReceive.isPresent()) {
                    if (!userSender.get().getRole().equals(ConstantUtilities.ROLE_PATIENT) &&
                            !message.getSenderId().equals(userCurrent.getCurrentUser().getId())) {   // Si el usuario es el remitente
                        openmessengerServices.add(message.getSenderId());
                    } else if (!userReceive.get().getRole().equals(ConstantUtilities.ROLE_PATIENT) &&
                            !message.getReceiver().getId().equals(userCurrent.getCurrentUser().getId())) { // Si el usuario es el receptor
                        openmessengerServices.add(message.getReceiver().getId());
                    }
                }

            }
            // ELimina el usuario actual de la lista de messengerServices abiertos, porque no puede tener un messengerService consigo mismo
            openmessengerServices.remove(userCurrent.getCurrentUser().getId());
        }
        return new ArrayList<>(openmessengerServices);
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void showOpenmessengerServices(List<Long> openmessengerServices) {
        removeOpenmessengerServiceVl();
        usersOpenmessengerService = new ArrayList<>();
        for (Long openmessengerService : openmessengerServices) {
            if (userService.findById(openmessengerService).isPresent()) {
                usersOpenmessengerService.add(userService.findById(openmessengerService).get());
            }
        }
        for (UserEntity user : usersOpenmessengerService) {

            List<MessengerServiceEntity> messengerServices = messengerServiceService.findBySenderIdOrReceiverId(user.getId(), user.getId());
            messengerServices.sort(Comparator.comparing(MessengerServiceEntity::getTimestamp)); // Ordena los messengerServices para que se muestren de los más recientes a los menos
            int value = 0;
            for (MessengerServiceEntity onemessengerService : messengerServices) {
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
            if (user.getStateMessagingSystemSanitary() != null && user.getStateMessagingSystemSanitary().equals(ConstantUtilities.STATE_INACTIVE)) {
                inboxLinHl.setEnabled(false);
                inboxLinHl.addClassName("disabled");
                inboxLinHl.removeClassName("enable-link");
                if (selectedUser != null && selectedUser.getId().equals(user.getId())) { //Si el usuario estaba en el messengerService del médico, se elimina para que no pueda messengerServiceear más
                    remove(messengerServiceView);
                }
            }
            openmessengerServiceVl.add(inboxLinHl);
        }
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
            for (Component component : openmessengerServiceVl.getChildren().toList()) {
                if (component instanceof HorizontalLayout) {
                    component.removeClassName("messenger-service-active");
                }
            }

            // Agregar la clase activa al elemento clicado
            inboxLinHl.addClassName("messenger-service-active");
            if (ismessengerServiceEnabled) {
                inboxLink.setSuffixComponent(null);
                selectedUser = user;
                openmessengerService();
            }
            //updatemessengerServiceStatus();
        });
        return inboxLinHl;
    }

    private void openmessengerService() {
        updatemessengerService();
        createmessengerService();
    }

    private void createmessengerService() {
        messengerServiceView = new MessengerServiceView(userCurrent, selectedUser, messengerServiceService, userService);
        add(messengerServiceView);
    }

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
                        messengerService(sanitaryEntityComboBox.getValue());
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

    private void messengerService(UserEntity userEntity) {
        selectedUser = userEntity;
        openmessengerService();
        SideNavItem inboxLink = new SideNavItem(selectedUser.getUsername());
        HorizontalLayout inboxLinHl = createInboxLinkHl(inboxLink, selectedUser);

        for (Component component : openmessengerServiceVl.getChildren().toList()) {
            if (component instanceof HorizontalLayout) {
                component.removeClassName("messenger-service-active");
            }
        }
        inboxLinHl.addClassName("messenger-service-active");

        openmessengerServiceVl.add(inboxLinHl);
        usersOpenmessengerService.add(selectedUser);
    }

    private List<UserEntity> getAvailableUsers() {
        Set<UserEntity> nomessengerServices = new HashSet<>();

        if (ismessengerServiceEnabled) { //Si mi messengerService está activo
            List<UserEntity> sanitaries = userService.findByRole(ConstantUtilities.ROLE_GYNECOLOGIST);
            sanitaries.addAll(userService.findByRole(ConstantUtilities.ROLE_MATRONE));
            for (UserEntity sanitaryEntity : sanitaries) {
                if (sanitaryEntity.getState() != null && (sanitaryEntity.getStateMessagingSystemSanitary() == null || sanitaryEntity.getStateMessagingSystemSanitary().equals(ConstantUtilities.STATE_ACTIVE))) { //Si el sanitario tiene el messengerService activo
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
        }

        return new ArrayList<>(nomessengerServices);
    }

    private boolean isCont(UserEntity sanitaryEntity) {
        boolean cont = true;
        for (UserEntity oneUsermessengerService : usersOpenmessengerService) {
            if (oneUsermessengerService.getRole().equals(ConstantUtilities.ROLE_MATRONE) ||
                    oneUsermessengerService.getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                if (sanitaryEntity.getId().equals(oneUsermessengerService.getId())
                        || sanitaryEntity.getId().equals(userCurrent.getCurrentUser().getId())) {
                    cont = false;
                }
            }
        }
        return cont;
    }
}
