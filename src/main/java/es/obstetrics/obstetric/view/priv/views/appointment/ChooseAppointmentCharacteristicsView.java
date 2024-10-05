package es.obstetrics.obstetric.view.priv.views.appointment;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase cuya funcionalidad se basa en que el usuario escoja los campos para
 * continuar con un proceso de creación de uan cita.
 */
@Route(value = "protected/patientsAppointment/choose-appointment", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class ChooseAppointmentCharacteristicsView extends Div implements HasUrlParameter<String> {

    private final PatientService patientService;
    private ComboBox<SanitaryEntity> sanitaryEntityCombo;
    private ComboBox<CenterEntity> centerCombo;
    private ComboBox<DiaryEntity> diaryEntityComboBox;
    private final CenterService centerService;
    private final SanitaryService sanitaryService;
    private final H5 errorMessage;
    private DatePicker startDate;
    private PatientEntity patientEntity;
    private final DiaryService diaryService;
    private final UserCurrent currentUser;

    @Autowired
    public ChooseAppointmentCharacteristicsView(PatientService patientService,
                                                CenterService centerService,
                                                SanitaryService sanitaryService,
                                                UserCurrent currentUser,
                                                DiaryService diaryService) {
        this.currentUser = currentUser;
        this.patientService = patientService;
        this.diaryService = diaryService;
        this.centerService = centerService;
        this.sanitaryService = sanitaryService;

        errorMessage = new H5("");
        errorMessage.addClassName("label-error");

    }

    /**
     * Recoge el dni de usuario pasado como parámetro a la vista
     * y crea toda la vista con sus combos y botones correspondientes.
     */
    @Override
    public void setParameter(BeforeEvent beforeEvent, String dni) {
        patientEntity = patientService.findOneByDni(dni);
        Button continueBtn = createButton();
        createComboStartDate();
        createComboDiaryEntity();
        UserHeaderTemplate header;
        if(currentUser.getCurrentUser().getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)){
            header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(), new UserEntity(),new H3("Pedir cita"));
            header.getButton().addClickListener(buttonClickEvent ->
                    UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
            );
        }else{
            header = new UserHeaderTemplate(VaadinIcon.ARROW_BACKWARD.create(), patientEntity,null);
            header.getButton().addClickListener(buttonClickEvent ->
                    UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
            );
        }
        header.getStyle().set("margin-bottom", "60px");
        createComboCenters();
        createComboSanitaries();
        H3 title = new H3("Rellene los campos para continuar con el proceso de citado");
        title.addClassName("form-title");
        VerticalLayout verticalLayout = createFormlayoutVl(title, createFormLayoutHl(createFormLayout()), continueBtn);
        verticalLayout.addClassName("choose-appointment-view");
        verticalLayout.setMargin(true);
        verticalLayout.setPadding(true);
        verticalLayout.setSizeFull();
        add(header,verticalLayout );

    }

    private VerticalLayout createFormlayoutVl(H3 title, HorizontalLayout formlayoutHl, Button continueBtn) {
        VerticalLayout formLayoutAndBtnVl = new VerticalLayout(title, formlayoutHl, errorMessage, continueBtn);
        formLayoutAndBtnVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, formlayoutHl);
        formLayoutAndBtnVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, errorMessage);
        formLayoutAndBtnVl.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, continueBtn);
        formLayoutAndBtnVl.setMargin(true);
        formLayoutAndBtnVl.setPadding(true);
        formLayoutAndBtnVl.addClassName("form-layout");
        return formLayoutAndBtnVl;
    }

    private HorizontalLayout createFormLayoutHl(FormLayout formLayout) {
        HorizontalLayout formlayoutHl = new HorizontalLayout(formLayout);
        formlayoutHl.setMargin(true);
        formlayoutHl.setPadding(true);
        formlayoutHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        formlayoutHl.addClassName("form-layout-hl");
        return formlayoutHl;
    }

    /**
     * Creación del botón para continuar con el proceso de citado.
     * @return El botón
     */
    private Button createButton() {
        Button continueBtn = new Button("Continuar");
        continueBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        continueBtn.addClickListener(e -> validateDates());
        return continueBtn;
    }

    /**
     * Crea el formlauout con todos los datos y lo hace responsive.
     * @return El fl.
     */
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        sanitaryEntityCombo.setEnabled(false);
        formLayout.add(centerCombo, sanitaryEntityCombo, startDate, diaryEntityComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        formLayout.addClassName("form-layout");
        return formLayout;
    }

    /**
     * Crea el combobox de la agenda, al comienzo está deshabilitado, hasta que se escriban los demás datos y se establezcan los valores.
     */
    private void createComboDiaryEntity() {
        diaryEntityComboBox = new ComboBox<>("Agendas disponibles para esa fecha");
        diaryEntityComboBox.setEnabled(false);
    }

    /**
     * Comprueba que se han introducido todos los valores para continuar con el proceso de citado.
     */
    private void validateDates() {
        if (sanitaryEntityCombo.isEmpty() || centerCombo.isEmpty() || startDate.isEmpty() || diaryEntityComboBox.isEmpty()) {
            setErrorMessage("Para continuar con el proceso debe rellenar toods los campos, revíselo.");
        } else {
            UI.getCurrent().navigate(ChooseAppointmentView.class,
                    patientEntity.getId() + "," +
                            startDate.getValue() + ","
                            + diaryEntityComboBox.getValue().getId());
        }
    }

    /**
     * Crea el DatePicker para seleccionar la fecha a partir de la que se va a realizar la búsqueda.
     *  Se desactiva cuando se ha escrito el centro y el sanitario.
     */
    private void createComboStartDate() {
        startDate = new DatePickerTemplate("Fecha de comienzo de búsqueda");
        startDate.setReadOnly(true);
        startDate.setMin(LocalDate.now());
        startDate.addValueChangeListener(event -> {
            if (startDate.getValue() != null) {
                setDiaryEntityCombo();
                diaryEntityComboBox.setEnabled(true);
            } else {
                startDate.setReadOnly(true);
                diaryEntityComboBox.setEnabled(false);
            }
        });
    }

    /**
     * Establece las agendas al combo a partir del sanitario, centro y fecha seleccionada.
     */
    private void setDiaryEntityCombo() {
        if (startDate.getValue() != null) {
            //Busco las agendas disponibles a partir de esa fecha
            List<DiaryEntity> diaryEntities = diaryService.findBySanitaryEntityAndCenterEntityAndState(sanitaryEntityCombo.getValue(), centerCombo.getValue(), ConstantUtilities.STATE_ACTIVE);
            List<DiaryEntity> diaryEntitiesDates = new ArrayList<>();
            for (DiaryEntity oneDiary : diaryEntities) {
                if ((oneDiary.getStartTime().isBefore(startDate.getValue()) || oneDiary.getStartTime().equals(startDate.getValue())) &&
                        (oneDiary.getEndTime() == null || oneDiary.getEndTime().isAfter(startDate.getValue()))) {
                    diaryEntitiesDates.add(oneDiary);
                }
            }
            diaryEntityComboBox.setItems(diaryEntitiesDates);
        }

    }

    /**
     * Crea un combobox para mostrar los sanitarios existentes.
     */
    private void createComboSanitaries() {
        sanitaryEntityCombo = new ComboBox<>("Sanitario");
        sanitaryEntityCombo.setPlaceholder("Selecciona un sanitario");
        sanitaryEntityCombo.setPrefixComponent(VaadinIcon.DOCTOR.create());
        sanitaryEntityCombo.setItems(sanitaryService.findByState(ConstantUtilities.STATE_ACTIVE));
        sanitaryEntityCombo.addValueChangeListener(event ->
                updateWithSanitariesCombo()
        );
    }

    /**
     * Crea un combobox para mostrar los centros existentes. Si se llama desde el proceso de citado value=0.
     * Si lo llama un sanitario para ver sus dietarios value=0.
     */
    private void createComboCenters() {
        centerCombo = new ComboBox<>("Centro");
        centerCombo.setPrefixComponent(VaadinIcon.HOSPITAL.create());
        centerCombo.setItemLabelGenerator(CenterEntity::getCenterName);
        /*
         * Paginación valores:
         * 0: indexación desde la primera página (0)
         * 150: hasta 150 elementos por página
         */
        centerCombo.setItems(centerService.findAll(0, 150).getContent());
        centerCombo.addValueChangeListener(event -> updateWithCentersCombo(event.getValue()));
        centerCombo.setPlaceholder("Selecciona un centro");
    }


    /**
     * Cuando el usuario selecciona un centro:
     * - Se buscan los sanitarios asociados a este y añaden a su combobox.
     */
    private void updateWithCentersCombo(CenterEntity value) {
        if (centerCombo.isEmpty()) {
            resetCombos();
        } else {
            sanitaryEntityCombo.setEnabled(true);
            List<SanitaryEntity> sanitaries = sanitaryService.findByCenterEntity(value);

            List<SanitaryEntity> sanitaryEntities = new ArrayList<>();
            for (SanitaryEntity oneSanitaryCenter : sanitaries) {
                if(oneSanitaryCenter.getState() == null || oneSanitaryCenter.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                    sanitaryEntities.add(oneSanitaryCenter);
                }
            }
            sanitaryEntityCombo.setItems(sanitaryEntities);
        }

    }

    private void resetCombos() {
        centerCombo.setItems(centerService.findAll(0, 150).getContent());
        sanitaryEntityCombo.setEnabled(false);
        sanitaryEntityCombo.clear();
        startDate.setReadOnly(true);
    }

    /**
     * Cuando el usuario selecciona un sanitario:
     * - Se buscan los centros asociados a este y añaden a su combobox.
     * - Se buscan las aseguradoras asociados a alguno de dichos centros y se muestran en su combobox.
     */
    private void updateWithSanitariesCombo() {
        if (centerCombo.isEmpty()) {
            resetCombos();
        } else {
            if (!centerService.existsBySanitaryEntityAndCenterEntity(sanitaryEntityCombo.getValue(), centerCombo.getValue())) {
                resetCombos();
            }
            if (sanitaryEntityCombo.isEmpty()) {
                startDate.setReadOnly(true);
            } else {
                startDate.setReadOnly(false);
                setDiaryEntityCombo();
            }
        }

    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }
}
