package es.obstetrics.obstetric.view.priv.dialog.maintenance.insurance;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import es.obstetrics.obstetric.backend.service.CenterService;
import es.obstetrics.obstetric.backend.service.InsuranceService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class InsuranceDialog extends MasterDialog {

    private final Binder<InsuranceEntity> insuranceEntityBinder;
    private final TextField name;
    private final TextField phone;
    private final EmailField email;
    private final TextField address;
    private final TextField postalCode;
    private final MultiSelectComboBox<CenterEntity> centersComboBox;
    private final H5 errorMessage;
    private final InsuranceService insuranceService;
    private final CenterService centerService;
    private List<CenterEntity> centers;

    public InsuranceDialog(InsuranceEntity insuranceEntity, InsuranceService insuranceService, CenterService centerService) {
        this.insuranceService = insuranceService;
        this.centerService = centerService;
        button.addClassName("set-width");
        name = new TextField("Nombre");
        centersComboBox = new MultiSelectComboBox<>("Centro/s asociados");
        address = new TextField("Dirección");
        phone = new TextField("Teléfono");
        email = new EmailField("Email");
        postalCode = new TextField("Código postal");
        errorMessage = new H5("");
        insuranceEntityBinder = new BeanValidationBinder<>(InsuranceEntity.class);
        insuranceEntityBinder.bindInstanceFields(this);

        createHeaderDialog();
        createDialogLayout();
        setInsurance(insuranceEntity);

    }

    private void setInsurance(InsuranceEntity insuranceEntity) {
        clearTextField();
        centersComboBox.setItems(centerService.findAll(0, 50).getContent());
        centersComboBox.addValueChangeListener(e -> {
            centers = new ArrayList<>(e.getValue());
        });
        if (insuranceEntity != null) {
            centersComboBox.setValue(insuranceEntity.getCenters());
        }
        insuranceEntityBinder.setBean(insuranceEntity);  //Recojo la categoria
        insuranceEntityBinder.readBean(insuranceEntity);
    }

    @Override
    public void createHeaderDialog() {
        button.setText("Guardar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
        clearTextField();
    }

    @Override
    public void clickButton() {
        try {
            if (insuranceEntityBinder.getBean() == null) { //Si se está añadiendo una nueva aseguradora
                InsuranceEntity insuranceEntity = new InsuranceEntity();
                insuranceEntityBinder.writeBean(insuranceEntity);

                 if (insuranceService.findOneByName(insuranceEntity.getName()) != null) {
                    setErrorMessage("Nombre de aseguradora existente");
                    return;
                }
                insuranceEntity.setState(ConstantUtilities.STATE_ACTIVE);
                fireNewEvent(insuranceEntity);

            } else {

                insuranceEntityBinder.writeBean(insuranceEntityBinder.getBean());
                InsuranceEntity oldInsurance = insuranceService.findOneByName(insuranceEntityBinder.getBean().getName());
                //Se comprueba si ya existe una categoría con ese nombre
                if (oldInsurance != null &&
                        !oldInsurance.getId().equals(insuranceEntityBinder.getBean().getId())
                        && oldInsurance.getName().equals(insuranceEntityBinder.getBean().getName())) {
                    setErrorMessage("EL nombre de la aseguradora ya existe");
                    return;
                }
                insuranceEntityBinder.getBean().setState(ConstantUtilities.STATE_ACTIVE);
                fireNewEvent(insuranceEntityBinder.getBean());
            }
        } catch (ValidationException e) {
            setErrorMessage("Campos incorrectos, revíselos.");
        }
    }

    private void fireNewEvent(InsuranceEntity bean) {
        fireEvent(new SaveEvent(this, bean, centers));
        close();
        setErrorMessage("");
        clearTextField();
    }

    @Override
    public void createDialogLayout() {
        errorMessage.addClassName("label-error");
        HorizontalLayout nameAndPhoneHl = new HorizontalLayout(name, phone);
        nameAndPhoneHl.setSizeFull();
        nameAndPhoneHl.expand(name, phone);

        HorizontalLayout emailAndCentersHl = new HorizontalLayout(email, centersComboBox);
        emailAndCentersHl.setSizeFull();
        emailAndCentersHl.expand(email, centersComboBox);

        centersComboBox.setItemLabelGenerator(CenterEntity::getCenterName);

        HorizontalLayout addressAndPostalCodeHl = new HorizontalLayout(address, postalCode);
        addressAndPostalCodeHl.setSizeFull();
        addressAndPostalCodeHl.expand(address, postalCode);

        errorMessage.setText("");
        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.add(nameAndPhoneHl,
                emailAndCentersHl, addressAndPostalCodeHl,
                errorMessage);
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {
        name.clear();
        phone.clear();
        email.clear();
        address.clear();
        postalCode.clear();
        centersComboBox.clear();
        errorMessage.setText("");
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
     * Clase abstracta que extiene de {@link InsuranceDialog}, evento ocurrido en dicha clase.
     * Almacena la aseguradora asociada al evento.
     */
    @Getter
    public static abstract class InsuranceFormEvent extends ComponentEvent<InsuranceDialog> {
        private final InsuranceEntity insuranceEntity; //Categoría con la que se trabaja
        private final List<CenterEntity> centers;

        protected InsuranceFormEvent(InsuranceDialog source,
                                     InsuranceEntity insuranceEntity,
                                     List<CenterEntity> centers) {
            super(source, false);
            this.centers =centers;
            this.insuranceEntity = insuranceEntity;
        }
    }

    /**
     * Clase heredada de InsuranceFormEvent, representa un evento de guardado que ocurre en el diálogo de aseguradora
     * */
    public static class SaveEvent extends InsuranceFormEvent {
        SaveEvent(InsuranceDialog source, InsuranceEntity insuranceEntity,List<CenterEntity> centers) {
            super(source, insuranceEntity,centers);
        }
    }


    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que maneajrá el evento.
     * @param <T>
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
