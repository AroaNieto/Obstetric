package es.obstetrics.obstetric.view.priv.dialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.FilesUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class UploadPdfDialog extends MasterDialog {

    private byte[] contentBytePdf;
    private byte[] contentMiniature;
    private final Image imagePdf = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();

    public UploadPdfDialog(byte[] contentBytePdf, byte[] contentMiniature){
        if(contentBytePdf != null){
            this.contentMiniature = contentMiniature;
            this.contentBytePdf = contentBytePdf;
            StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentMiniature));
            imagePdf.setSrc(resource);
        }else{
            this.contentBytePdf = null;
            this.contentMiniature = null;
            imagePdf.setSrc("");
        }
        createHeaderDialog();
        createDialogLayout();

    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Continuar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        setMaxHeight("500px");
        setMinHeight("500px");

        setMaxWidth("400px");
        setMinWidth("400px");
    }

    /**
     * Dispara el evento para notificar a la clase {@link NewsletterDialog }
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
        fireEvent(new SaveEvent(this, contentBytePdf, contentMiniature));
        close();
    }

    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    @Override
    public void setErrorMessage(String message){
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
            dialogVl.remove(imagePdf);
        }).addEventData("event.detail.file.name");

        uploadPdf.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            try {
                contentBytePdf = inputStream.readAllBytes(); //Convertir el contenido
                String filenameJPG = FilesUtilities.setThumbnailOfPdfInByte("contenido" + UUID.randomUUID(),contentBytePdf);
                contentMiniature = FilesUtilities.fileToByte(filenameJPG);
                StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentMiniature));
                imagePdf.setSrc(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.add(uploadPdf, imagePdf);
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
    }

    private UploadI18N createUploadPdfI18N() {

        UploadI18N i18N = new UploadI18N();
        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne("Cargar PDF...");
        i18N.setAddFiles(addFiles);

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne("Arrastre el archivo aquí");
        i18N.setDropFiles(dropFiles);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setIncorrectFileType("El archivo proporcionado no coincide con el tipo de formato que se requiere (.pdf)");
        i18N.setError(error);

        return i18N;
    }

    /**
     * Clase abstracta que extiene de {@link UploadPdfDialog}, evento ocurrido en dicha clase.
     *  Almacena el array de bytes del PDF y su miniatura correspondiente.
     */
    @Getter
    public static  abstract  class UploadPfdFormEvent extends ComponentEvent<UploadPdfDialog> {
        private final byte[] contentBytePdf;
        private final byte[] contentMiniature;

        protected  UploadPfdFormEvent(UploadPdfDialog source, byte[] contentBytePdf, byte[] contentMiniature){
            super(source, false);
            this.contentBytePdf = contentBytePdf;
            this.contentMiniature= contentMiniature;
        }
    }

    /**
     * Clase heredada de UploadPdfDialog, representa un evento de guardado que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece los arrays asociados al evento.
     */
    public static  class SaveEvent extends UploadPfdFormEvent {
        SaveEvent(UploadPdfDialog source, byte[] contentByte, byte[] contentMiniature){
            super(source, contentByte, contentMiniature);
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
