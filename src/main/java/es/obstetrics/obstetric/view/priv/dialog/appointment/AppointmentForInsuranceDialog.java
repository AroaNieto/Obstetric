package es.obstetrics.obstetric.view.priv.dialog.appointment;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.AppointmentForInsurancePdf;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;

import java.util.ArrayList;

public class AppointmentForInsuranceDialog extends MasterDialog {
    private final H5 errorMessage;
    private DatePicker startDate;
    private DatePicker endingDate;
    private final AppointmentService appointmentService;

    public AppointmentForInsuranceDialog(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
        errorMessage = new H5("");
        createDialogLayout();
        createHeaderDialog();
    }

    @Override
    public void createHeaderDialog() {
        setHeaderTitle("Descargar listado de citas por aseguradora");
        button.setText("Descargar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    @Override
    public void closeDialog() {
       close();
    }

    @Override
    public void clickButton() {
        if(startDate.isEmpty() || endingDate.isEmpty()){
            setErrorMessage("Debe rellenar ambos datos para poder descargar el listado.");
            return;
        }
        if(startDate.getValue().isAfter(endingDate.getValue())){
            setErrorMessage("La fecha de fin no puede ser anterior a la de inicio.");
            return;
        }
        ArrayList<AppointmentEntity> appointmentEntities =new ArrayList<>();
        for(AppointmentEntity oneAppointment: appointmentService.findAll()){
            if((oneAppointment.getDate().isEqual(startDate.getValue()) || oneAppointment.getDate().isAfter(startDate.getValue())) && ((oneAppointment.getDate().isEqual(endingDate.getValue()) || oneAppointment.getDate().isBefore(endingDate.getValue())))){
                if(oneAppointment.getInsuranceEntity() != null && oneAppointment.getHasAttended() != null && oneAppointment.getHasAttended().equals(ConstantUtilities.RESPONSE_YES)){
                    appointmentEntities.add(oneAppointment);
                }
            }
        }
        StreamResource resource = new StreamResource("citas_aseguradora.pdf", () -> new AppointmentForInsurancePdf(appointmentEntities, startDate.getValue(),endingDate.getValue()).generatePdf());

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de citas por aseguradora");
        dialog.open();
    }

    @Override
    public void createDialogLayout() {
        startDate = new DatePickerTemplate("Fecha de inicio");
        endingDate = new DatePickerTemplate("Fecha de fin");
        HorizontalLayout noticeAndReminderHl = new HorizontalLayout(startDate, endingDate);
        noticeAndReminderHl.setSizeFull();
        noticeAndReminderHl.expand(startDate, endingDate);

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el ancho completo
        dialogVl.getStyle().set("width", "45rem")
                .set("max-width", "100%");

        errorMessage.addClassName("label-error");

        dialogVl.add(noticeAndReminderHl, errorMessage);
    }

    @Override
    public void clearTextField() {
            endingDate.clear();
            startDate.clear();
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

}
