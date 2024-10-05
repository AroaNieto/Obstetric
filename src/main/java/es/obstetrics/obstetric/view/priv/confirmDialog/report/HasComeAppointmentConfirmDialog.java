package es.obstetrics.obstetric.view.priv.confirmDialog.report;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que extiende de la clase {@link MasterConfirmDialog}, se usa para
 *  que el usuario confirme si el paciende ha acudido a la cita o no.
 */
public class HasComeAppointmentConfirmDialog extends MasterConfirmDialog {

    private  RadioButtonGroup<String > hasCome;
    private MultiSelectComboBox<NewsletterEntity> newsletterEntityComboBox;
    private final NewsletterService newsletterService;
    private final PatientEntity patientEntity;
    private final AppointmentService appointmentService;
    private final AppointmentEntity appointmentEntity;

    public HasComeAppointmentConfirmDialog(NewsletterService newsletterService, AppointmentService appointmentService,
                                           PatientEntity patient, AppointmentEntity appointmentEntity){
        this.newsletterService = newsletterService;
        this.appointmentEntity = appointmentEntity;
        this.appointmentService = appointmentService;
        this.patientEntity = patient;
        createHeaderAndTextDialog();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Grabar informe");
        hasCome = new RadioButtonGroup<>("Ha acudido");
        newsletterEntityComboBox = new MultiSelectComboBox<>("Añadir nuevas newsletters");
        newsletterEntityComboBox.setItems(addItems()); //Se buscan las newsletter sin asociar un trimestre

        hasCome.setItems(ConstantUtilities.RESPONSE_YES, ConstantUtilities.RESPONSE_NO);
        setText("Antes de que la cita sea grabada, debe confirmar si el paciente ha acudido.");
        newsletterEntityComboBox.getStyle().set("width","300px");
        add(new VerticalLayout(hasCome,newsletterEntityComboBox));
    }

    /**ç
     * Se buscan las newsletters sin trimestre, que se encuentren activas.
     * @return Las newsletters que cumplan la condición.
     */
    private List<NewsletterEntity> addItems() {
        List<NewsletterEntity> newsletterEntities = new ArrayList<>();

        for(NewsletterEntity oneNewsletter: newsletterService.findByQuarterAndState(ConstantUtilities.NONE_QUARTERER, ConstantUtilities.STATE_ACTIVE)){
            boolean out = true;
            if(!oneNewsletter.getPatients().isEmpty()){
                for(PatientEntity patient: oneNewsletter.getPatients()){
                    if (patient.getId().equals(patientEntity.getId())) {
                        out = false;
                        break;
                    }
                }
            }
            if(out){
                newsletterEntities.add(oneNewsletter);
            }
        }
        return newsletterEntities;
    }

    @Override
    public void closeDialog() {
        close();
    }

    @Override
    public void clickButton() {
        if(hasCome.getValue() != null){
          //  appointmentEntity.setHasAttended(hasCome.getValue());
           // appointmentService.save(appointmentEntity);
            List<NewsletterEntity> newsletterEntities = new ArrayList<>(newsletterEntityComboBox.getValue());
            fireEvent(new HasCome(this, hasCome.getValue(), newsletterEntities));
            close();
        }
    }

    /**
     * Clase abstracta que extiene de {@link HasComeAppointmentConfirmDialog}, evento ocurrido en dicha clase.
     */
    @Getter
    public static  abstract  class HasComeConfirmDialogFormEvent extends ComponentEvent<HasComeAppointmentConfirmDialog> {

        private final String hasCome;
        private final List<NewsletterEntity> newsletterEntities;
        protected  HasComeConfirmDialogFormEvent(HasComeAppointmentConfirmDialog source,String hasCome, List<NewsletterEntity> newsletterEntity){
            super(source, false);
            this.hasCome = hasCome;
            this.newsletterEntities = newsletterEntity;
        }

    }
    /**
     * Clase heredada de HasComeConfirmDialogFormEvent, representa un evento de actualizar que ocurre en el diálogo
     * */
    public static  class HasCome extends HasComeConfirmDialogFormEvent {
        HasCome(HasComeAppointmentConfirmDialog source,String hasCome,List<NewsletterEntity> newsletterEntity){
            super(source,hasCome, newsletterEntity);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T> Clase
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
