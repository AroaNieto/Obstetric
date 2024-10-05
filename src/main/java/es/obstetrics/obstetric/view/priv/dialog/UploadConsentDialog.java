package es.obstetrics.obstetric.view.priv.dialog;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.FilesUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeleteSanitaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.views.users.PatientsGridView;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Evento encargado de subir la declaración responsable
 */
public class UploadConsentDialog  extends MasterDialog{
    private final H5 errorMessage;
    private byte[] contentBytePdf;
    private byte[] contentMiniature;
    private final Image imagePdf = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final PatientEntity patientEntity;

    public UploadConsentDialog(PatientEntity userEntity){
       this.patientEntity = userEntity;
        errorMessage = new H5("");
        this.contentBytePdf = null;
        imagePdf.setSrc("");
        contentBytePdf = null;
        createHeaderDialog();
        createDialogLayout();

    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Aceptar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        setMaxHeight("500px");
        setMinHeight("500px");

        setMaxWidth("400px");
        setMinWidth("400px");
    }

    /**
     * Dispara el evento para notificar a la clase {@link PatientsGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Limpia el valor de los campos
     */
    @Override
    public void clearTextField() {

    }

    @Override
    public void clickButton() {
        if(contentBytePdf == null){
            setErrorMessage("No ha adjuntado ningún archivo");
        }else{
            patientEntity.setInformedConsent(contentBytePdf);
            fireEvent(new SaveEvent(this, patientEntity));
            close();
        }
    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setErrorMessage(String message){
        errorMessage.setText(message);
    }

    /**
     * Método que se encarga de configurar el diseño del diálogo de contenido
     *  con sus campos correspondientes.
     */
    @Override
    public void createDialogLayout() {
        Upload uploadPdf = new Upload(buffer);

        uploadPdf.setDropAllowed(true); //Aceptar archivos que se arrastren y suelten
        uploadPdf.setAcceptedFileTypes(ConstantUtilities.FILE_TYPES_PDF,
                ConstantUtilities.EXTENSION_PDF);

        uploadPdf.addFileRejectedListener(e ->{
            String errMessage = e.getErrorMessage();

            Notification errNotification = Notification.show(errMessage, 5000, Notification.Position.MIDDLE);
            errNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        uploadPdf.setI18n(createUploadPdfI18N());
        uploadPdf.setMinWidth("340px");
        uploadPdf.setMaxWidth("340px");
        /*
            Evento que controla si un usuario elimina el archivo que acaba de cargar.
         */
        uploadPdf.getElement().addEventListener("file-remove", event -> {
            contentBytePdf = null;
            contentMiniature = null;
            imagePdf.setVisible(false);
        }).addEventData("event.detail.file.name");

        uploadPdf.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            try {
                contentBytePdf = inputStream.readAllBytes(); //Convertir el contenido
                String filenameJPG = FilesUtilities.setThumbnailOfPdfInByte("contenido" + UUID.randomUUID(),contentBytePdf);
                contentMiniature = FilesUtilities.fileToByte(filenameJPG);
                StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentMiniature));
                imagePdf.setSrc(resource);
                imagePdf.setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        errorMessage.addClassName("label-error");
        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el espacio completo
        dialogVl.add(uploadPdf, imagePdf,errorMessage);
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
    }

    private UploadI18N createUploadPdfI18N() {

        UploadI18N i18N = new UploadI18N();
        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne("Cargar consentimiento firmado...");
        i18N.setAddFiles(addFiles);

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne("Arrastre el archivo aquí");
        i18N.setDropFiles(dropFiles);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setIncorrectFileType("El archivo proporcionado no coincide con el tipo de formato que se requiere (.pfd)");
        i18N.setError(error);

        return i18N;
    }


    /**
     * Clase abstracta que extiene de {@link UploadConsentDialog}, evento ocurrido en dicha clase.
     *  Almacena el array de bytes del PDF
     */
    @Getter
    public static  abstract  class UploadConsentPfdFormEvent extends ComponentEvent<UploadConsentDialog> {
        private final PatientEntity patientEntity;

        protected  UploadConsentPfdFormEvent(UploadConsentDialog source, PatientEntity patientEntity){
            super(source, false);
            this.patientEntity = patientEntity;
        }
    }

    /**
     * Clase heredada de UploadConsentPfdFormEvent, representa un evento de guardado que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece el arrray asociado al evento.
     */
    public static  class SaveEvent extends UploadConsentPfdFormEvent {
        SaveEvent(UploadConsentDialog source, PatientEntity patientEntity){
            super(source, patientEntity);
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
