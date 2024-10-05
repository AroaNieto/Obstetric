package es.obstetrics.obstetric.view.priv.dialog.users;

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
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.confirmDialog.users.DeletePatientsConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadProfilePhotoDialog extends MasterDialog {
    private byte[] contentByte;
    private final Image image = new Image();
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final H5 errorMessage;

    @Autowired
    public UploadProfilePhotoDialog(){
        this.contentByte = null;
        errorMessage = new H5("");
        errorMessage.addClassName("label-error");
        createHeaderDialog();
        createDialogLayout();
    }

    /**
     * Crea la cabecera y le da el estilo correspondiente
     *  al botón de guardar.
     */
    @Override
    public void createHeaderDialog() {
        button.setText("Modificar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setWidth("100px");

        setMaxHeight("500px");
        setMinHeight("500px");

        setMaxWidth("400px");
        setMinWidth("400px");
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.views.users.ProfileUserView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        setErrorMessage("");
    }

    @Override
    public void clickButton() {
        if(contentByte == null){
            setErrorMessage("No ha añadido ninguna fotografía para poder guardarla, revíselo.");
        }else{
            fireEvent(new SaveEvent(this, contentByte));
            closeDialog();
        }

    }

    private UploadI18N createUploadPdfI18N() {

        UploadI18N i18N = new UploadI18N();
        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne("Cargar foto de perfil...");
        i18N.setAddFiles(addFiles);

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne("Arrastre la fotografía aquí");
        i18N.setDropFiles(dropFiles);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setIncorrectFileType("El archivo proporcionado no coincide con el tipo de formato que se requiere (.jpg)");
        i18N.setError(error);

        return i18N;
    }

    @Override
    public void createDialogLayout() {
        Upload uploadUrl = new Upload(buffer);
        uploadUrl.setDropAllowed(true); //Aceptar archivos que se arrastren y suelten
        uploadUrl.setAcceptedFileTypes(ConstantUtilities.FILE_TYPES_JPG,
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
                dialogVl.add(image);
                contentByte = inputStream.readAllBytes(); //Convertir el contenido
                StreamResource resource = new StreamResource("foto.jpg", () -> new ByteArrayInputStream(contentByte));
                image.setSrc(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        /*
            Evento que controla si un usuario elimina el archivo que acaba de cargar.
         */
        uploadUrl.getElement().addEventListener("file-remove", event -> {
            contentByte = null;
            dialogVl.remove(image);
        }).addEventData("event.detail.file.name");

        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán todo el ancho
        dialogVl.add(uploadUrl,errorMessage);
        dialogVl.getStyle().set("width","45rem")
                .set("max-width","100%");
    }


    @Override
    public void clearTextField() {

    }

    @Override
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Clase abstracta que extiene de {@link UploadProfilePhotoDialog}, evento ocurrido en dicha clase.
     *  Almacena el array de bytes de la foto de perfil asociado al evento.
     */
    @Getter
    public static  abstract  class UploadProfilePhotoFormEvent extends ComponentEvent<UploadProfilePhotoDialog> {
        private final byte[] contentByte;

        protected  UploadProfilePhotoFormEvent(UploadProfilePhotoDialog source, byte[] contentBytePdf){
            super(source, false);
            this.contentByte = contentBytePdf;
        }
    }

    /**
     * Clase heredada de UploadProfilePhotoFormEvent, representa un evento de guardado que ocurre en el diálogo,
     *      Tiene un constructor que llama al constructor de la super clase y establece el array de bytes asociado al evento.
     */
    public static  class SaveEvent extends UploadProfilePhotoFormEvent {
        SaveEvent(UploadProfilePhotoDialog source, byte[] contentByteUrl){
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
