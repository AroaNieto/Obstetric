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
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadSrcDialog extends MasterDialog {
    private byte[] contentByteUrl;
    private final Image imageUrl = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();

    public UploadSrcDialog(byte[] contentByteUrl){
        if(contentByteUrl != null){ //Compruebo que el contenido se ha añadido antes
            this.contentByteUrl = contentByteUrl;
            StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentByteUrl));
            imageUrl.setSrc(resource);
        }else{
            this.contentByteUrl = null;
            imageUrl.setSrc("");
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
        fireEvent(new SaveEvent(this, contentByteUrl));
        close();
    }


    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
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

        Upload uploadUrl = new Upload(buffer);

        uploadUrl.setDropAllowed(true); //Aceptar archivos que se arrastren y suelten
        uploadUrl.setAcceptedFileTypes(ConstantUtilities.FILE_TYPES_JPG,
                ConstantUtilities.FILE_TYPES_PNG,
                ConstantUtilities.EXTENSION_PNG,
                ConstantUtilities.EXTENSION_JPG,
                ConstantUtilities.EXTENSION_JPGE);

        uploadUrl.addFileRejectedListener(e ->{
            String errMessage = e.getErrorMessage();

            Notification errNotification = Notification.show(errMessage, 5000, Notification.Position.MIDDLE);
            errNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        uploadUrl.setI18n(createUploadPdfI18N());
        uploadUrl.setMinWidth("340px");
        uploadUrl.setMaxWidth("340px");

        uploadUrl.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            try {
                StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentByteUrl));
                imageUrl.setSrc(resource);
                dialogVl.add(imageUrl);
                contentByteUrl = inputStream.readAllBytes(); //Convertir el contenido
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        /*
            Evento que controla si un usuario elimina el archivo que acaba de cargar.
         */
        uploadUrl.getElement().addEventListener("file-remove", event -> {
            contentByteUrl = null;
            dialogVl.remove(imageUrl);
        }).addEventData("event.detail.file.name");

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el ancho disponible
        dialogVl.add(uploadUrl);
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
    }

    private UploadI18N createUploadPdfI18N() {

        UploadI18N i18N = new UploadI18N();
        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne("Cargar imágen...");
        i18N.setAddFiles(addFiles);

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne("Arrastre el archivo aquí");
        i18N.setDropFiles(dropFiles);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setIncorrectFileType("El archivo proporcionado no coincide con el tipo de formato que se requiere (.jpg)");
        i18N.setError(error);

        return i18N;
    }

    /**
     * Clase abstracta que extiene de {@link UploadSrcDialog}, evento ocurrido en dicha clase.
     *  Almacena el array de bytes asociado al evento.
     */
    @Getter
    public static  abstract  class UploadSrcFormEvent extends ComponentEvent<UploadSrcDialog> {
        private final byte[] contentByteUrl;

        protected  UploadSrcFormEvent(UploadSrcDialog source, byte[] contentByteUrl){
            super(source, false);
            this.contentByteUrl = contentByteUrl;
        }

    }

    /**
     * Clase heredada de UploadSrcFormEvent, representa un evento de guardado que ocurre en el diálogo de contenido,
     *      Tiene un constructor que llama al constructor de la super clase y establece el  el array de bytes asociado al evento.
     */
    public static  class SaveEvent extends UploadSrcFormEvent {
        SaveEvent(UploadSrcDialog source, byte[] contentByteUrl){
            super(source, contentByteUrl);
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