package es.obstetrics.obstetric.view.priv.dialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.service.CenterService;
import es.obstetrics.obstetric.backend.service.SanitaryService;
import es.obstetrics.obstetric.backend.service.UserService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.views.users.SanitariesGridView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanitaryDialog extends MasterDialog {

    private final Binder<SanitaryEntity> userEntityBinder;
    private final TextField name;
    private final TextField lastName;
    private final TextField dni;
    private final TextField address;
    private final TextField phone;
    private final EmailField email;
    private final TextField postalCode;
    private final TextField age;
    private final PasswordField passwordHash;
    private final TextField username;
    private final ComboBox<String> sex;
    private final ComboBox<String> state;
    private final ComboBox<String> role;
    private final H5 errorMessage;
    private final MultiSelectComboBox<CenterEntity> centersComboBox;
    private final CenterService centerService;
    private List<CenterEntity> centers;
    private final SanitaryService sanitaryService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public SanitaryDialog(CenterService centerService,
                          SanitaryService sanitaryService,
                          SanitaryEntity sanitaryEntity,
                          PasswordEncoder passwordEncoder,
                          UserService userService){

        this.sanitaryService = sanitaryService;
        this.centerService = centerService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;

        name = new TextField("Nombre");
        passwordHash = new PasswordField("Contraseña");
        username = new TextField("Nombre de usuario");
        lastName= new TextField("Apellidos");
        dni= new TextField("DNI");
        address= new TextField("Dirección");
        phone= new TextField("Teléfono");
        age= new TextField("Edad");
        email= new EmailField("Email");
        postalCode= new TextField("Código postal");
        state = new ComboBox<>("Estado");
        sex = new ComboBox<>("Sexo");
        role = new ComboBox<>("Rol");
        errorMessage = new H5("");

        centersComboBox = new MultiSelectComboBox<>("Centro/s asociados");

        createHeaderDialog();
        createDialogLayout();
        userEntityBinder = new BeanValidationBinder<>(SanitaryEntity.class);

        userEntityBinder.bindInstanceFields(this);
        setUser(sanitaryEntity);
    }

    /**
     * Método utilizado para establecer los valores de los campos de diálogo dependiendo de si
     *  se está editando una usuario (user != null) o añadiendo (user == null).
     *
     * @param user usuario
     */
    private void setUser(SanitaryEntity user){
        clearTextField();
        setItems();
        if(user != null) {
            sex.setValue(user.getSex());
            if(!role.isEmpty()){
                role.setValue(user.getRole());
            }
            if(!user.getCenters().isEmpty()){
                List<CenterEntity> centerEntities = new ArrayList<>(user.getCenters());
                centersComboBox.setValue(centerEntities);
            }
        }
        userEntityBinder.setBean(user);  //Recojo el usuario
        userEntityBinder.readBean(user);
    }

    private void setItems(){
        state.setItems(ConstantUtilities.STATE_INACTIVE);
        state.setValue(ConstantUtilities.STATE_INACTIVE);
        centersComboBox.setItems(centerService.findAll(0,50).getContent());
        sex.setItems(ConstantUtilities.SEX_FEMALE, ConstantUtilities.SEX_MALE);
        state.setItems(ConstantUtilities.STATE_ACTIVE, ConstantUtilities.STATE_INACTIVE);
        role.setItems(ConstantUtilities.ROLE_GYNECOLOGIST, ConstantUtilities.ROLE_MATRONE, ConstantUtilities.ROLE_SECRETARY);
        role.addValueChangeListener(event -> {
            if(event.getValue() != null && event.getValue().equals(ConstantUtilities.ROLE_SECRETARY)){ //Si se trata de un secretario no va a tener centros asociados
                centersComboBox.setEnabled(false);
                centersComboBox.clear();
            }else{
                centersComboBox.setEnabled(true);
            }
        });
    }
    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link SanitariesGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
        setUser(null);
        clearTextField();
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de guardar, se comprueba si se está añadiendo una nuevo usuario o editando.
     *      - Si el valor del binder es nulo, se está añadiendo, se establecen los valores de fecha y se escribe en el binder.
     *      - Si el valor no es nulo, únicamente se escribe en el binder.
     */
    @Override
    public void clickButton() {

        if (userEntityBinder.getBean() == null) { // Si se está añadiendo un nuevo sanitario
            SanitaryEntity userEntity = new SanitaryEntity();
            try {
                userEntityBinder.writeBean(userEntity);
                Matcher matcher = createMatcher(userEntity.getPasswordHash());

                if (!matcher.matches()) {
                    setErrorMessage("La contraseña debe contenter al menos 8 caracteres, de los cuales debe contener una mayúsucla, una minúscula y un dígito.");
                    return;
                }
                if (sanitaryService.findOneByUsername(username.getValue()) != null) {
                    setErrorMessage("El nombre de usuario ya está en uso, debe poner otro.");
                    return;
                }
                if (sanitaryService.findOneByDni(dni.getValue()) != null) {
                    setErrorMessage("El DNI ya existe.");
                    return;
                }

                if (sanitaryService.findOneByEmail(email.getValue()) != null) {
                    setErrorMessage("El email ya existe.");
                    return;
                }
                if (sanitaryService.findOneByPhone(phone.getValue()) != null) {
                    setErrorMessage("El teléfono móvil ya existe.");
                    return;
                }
                if(userService.findOneByUsername(username.getValue()) != null){
                    setErrorMessage("El nombre de usuario ya está en uso.");
                    return;
                }
                userEntity.setPasswordHash(passwordEncoder.encode(userEntity.getPasswordHash()));
                userEntity.setState("ACTIVO");
                fireEvent(new SaveEvent(this, userEntity, centers));
            } catch (ValidationException e) {
                setErrorMessage(e.getMessage());
            }
        } else {

            try {
                userEntityBinder.writeBean(userEntityBinder.getBean());
                Matcher matcher = createMatcher(userEntityBinder.getBean().getPasswordHash());

                if (!matcher.matches()) {
                    setErrorMessage("La contraseña debe contenter al menos 8 caracteres, de los cuales debe contener una mayúsucla, una minúscula y un dígito.");
                    return;
                }
                if (sanitaryService.findByDni(userEntityBinder.getBean().getDni()) != null
                        && !userEntityBinder.getBean().getId().equals(sanitaryService.findByDni(userEntityBinder.getBean().getDni()).getId())) {
                    setErrorMessage("El DNI ya está en uso.");
                    return;
                }
                // Verificar si se ha modificado el correo electrónico
                if (sanitaryService.findByEmail(userEntityBinder.getBean().getEmail()) != null &&
                        !userEntityBinder.getBean().getId().equals(sanitaryService.findByEmail(userEntityBinder.getBean().getEmail()).getId())) {
                    setErrorMessage("El correo electrónico ya está en uso.");
                    return;
                }
                // Verificar si se ha modificado el telefono
                if (sanitaryService.findByPhone(userEntityBinder.getBean().getPhone()) != null  &&
                        !userEntityBinder.getBean().getId().equals(sanitaryService.findByDni(userEntityBinder.getBean().getDni()).getId())) {
                    setErrorMessage("El teléfono móvil ya esta en uso.");
                    return;
                }
                if(userService.findOneByUsername(userEntityBinder.getBean().getUsername()) != null &&
                        !userEntityBinder.getBean().getId().equals(sanitaryService.findOneByUsername(userEntityBinder.getBean().getUsername()).getId())){
                    setErrorMessage("El nombre de usuario ya está en uso.");
                    return;
                }
                sanitaryService.save(userEntityBinder.getBean()); // Actualizar la entidad
                fireEvent(new SaveEvent(this, userEntityBinder.getBean(), centers));
            } catch (ValidationException e) {
                setErrorMessage(e.getMessage());
            }
        }
        setErrorMessage("");
        close();
    }

    private Matcher createMatcher(String passwordHash) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(passwordHash);
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo del usuario
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {

        HorizontalLayout nameAndLastnameHl = new HorizontalLayout(name, lastName);
        nameAndLastnameHl.setSizeFull();
        nameAndLastnameHl.expand(name,lastName);

        HorizontalLayout dniAndAgeHl = new HorizontalLayout(age, dni);
        dniAndAgeHl.setSizeFull();
        dniAndAgeHl.expand(age,dni);

        HorizontalLayout phoneEmailHl = new HorizontalLayout(phone, email);
        phoneEmailHl.setSizeFull();
        phoneEmailHl.expand(phone,email);

        HorizontalLayout postalCodeAndSexHl = new HorizontalLayout(address,postalCode);
        postalCodeAndSexHl.setSizeFull();
        postalCodeAndSexHl.expand(address,postalCode);

        HorizontalLayout sexAndRoleHl = new HorizontalLayout(role, sex);
        sexAndRoleHl.setSizeFull();
        sexAndRoleHl.expand(role,sex);

       centersComboBox.setItemLabelGenerator(CenterEntity::getCenterName);
       centersComboBox.setWidth("300px");
       centersComboBox.addValueChangeListener(e -> centers = new ArrayList<>(e.getValue()));
        HorizontalLayout passwordAndUsernameHl = new HorizontalLayout(username, passwordHash);
        passwordAndUsernameHl.setSizeFull();
        passwordAndUsernameHl.expand(username, passwordHash);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el ancho completo
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(nameAndLastnameHl,dniAndAgeHl,phoneEmailHl,
                postalCodeAndSexHl,sexAndRoleHl, passwordAndUsernameHl,
                centersComboBox,
                errorMessage);
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
       centersComboBox.clear();
        passwordHash.clear();
        username.clear();
        sex.clear();
        lastName.clear();
        state.clear();
        dni.clear();
        address.clear();
        phone.clear();
        email.clear();
        postalCode.clear();
        role.clear();
        userEntityBinder.readBean(null);
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
     *  Clase abstracta que extiene de {@link SanitaryDialog}, evento ocurrido en dicha clase.
     *      Almacena el sanitario asociado al evento.
     */
    @Getter
    public static  abstract  class SanitaryFormEvent extends ComponentEvent<SanitaryDialog> {
        private final SanitaryEntity userEntity; //Usuario con el que se trabaja
        private final List<CenterEntity> centers;
        protected SanitaryFormEvent(SanitaryDialog source, SanitaryEntity userEntity,List<CenterEntity> centers){
            super(source, false);
            this.userEntity = userEntity;
            this.centers = centers;
        }
    }

    /**
     * Clase heredada de UserFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el usuario asociado al evento.
     */
    public static  class SaveEvent extends SanitaryFormEvent {
        SaveEvent(SanitaryDialog source, SanitaryEntity userEntity,List<CenterEntity> centers){
            super(source, userEntity,centers);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que maneajrá el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
