package es.obstetrics.obstetric.view.priv.confirmDialog.users;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.PatientEntity;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.InformedConsentPdf;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import lombok.Getter;

public class UserCodeConfirmDialog extends MasterDialog {

    private final String code;
    private final String name;
    private final String lastName;
    private final ConstantValues constantValues;
    private boolean printContent = false;
    private final H5 errorMessage;
    private final PatientEntity user;

    public UserCodeConfirmDialog(String code, String name, String lastName, PatientEntity user, ConstantValues constantValues){
        this.code = code;
        this.name = name;
        this.lastName = lastName;
        this.constantValues = constantValues;
        this.user= user;
        errorMessage = new H5("");
        createHeaderDialog();
        createDialogLayout();
    }

    private Button createPrintButton() {
        Button printButton = new Button("Imprimir consentimiento", new Icon(VaadinIcon.PRINT));
        printButton.addClassName("dark-green-button");
        printButton.addClickListener(event -> {
            StreamResource resource = new StreamResource("consentimiento_informado_"+name+"_"+ lastName+".pdf", () -> new InformedConsentPdf(name, lastName, constantValues).generatePdf());
           /*
                La descarga se hará automáticamente.
             */
            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getElement().setAttribute("hidden", true);
            add(downloadLink);
            downloadLink.getElement().callJsFunction("click");
            printContent = true;
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar 1 segundo antes de eliminar el Anchor
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                getUI().ifPresent(ui -> ui.access(() -> remove(downloadLink)));
            }).start();
        });
        return printButton;
    }


    @Override
    public void createHeaderDialog() {
        setHeaderTitle("Generar código de acceso");
        button.setText("Continuar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Dispara el evento para notificar a la clase {@link es.obstetrics.obstetric.view.priv.dialog.users.PatientDialog }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
    }

    /**
     * Método que se ejecuta cuando se hace click sobre el botón de save,
     *      llama al método WriteBean para guardar el usuario con su
     *      código.
     */
    @Override
    public void clickButton() {
        if(printContent){
            fireEvent(new SaveCode(this, code, user));
            closeDialog();
            setErrorMessage("");
        }else{
            setErrorMessage("Debe imprimir el consentimiento.");
        }
    }

    @Override
    public void createDialogLayout() {
        H3 codeH3 = new H3("CÓDIGO: " + code);
        H5 suggestionsH5 = new H5("No olvide proporcionarselo al paciente.");
        if(user == null){
            Button printButton = createPrintButton();
            dialogVl.add(codeH3, suggestionsH5, printButton, errorMessage);
            errorMessage.addClassName("label-error");
        }else{
            printContent = true;
            dialogVl.add(codeH3, suggestionsH5);
        }
        dialogVl.setAlignItems(FlexComponent.Alignment.STRETCH); //Los componentes ocuparán el espacio
        dialogVl.getStyle().set("width","30rem")
                .set("max-width","100%");
    }

    @Override
    public void clearTextField() {

    }


    /**
     * Establece el mensaje de error y lo muestra en el cuadro de diálogo.
     *
     * @param message Mensaje a mostrar
     */
    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    /**
     * Clase abstracta que extiene de {@link UserCodeConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el codigo que se va a asociar al usuario.
     */
    @Getter
    public static  abstract  class UserCodeDialogFormEvent extends ComponentEvent<UserCodeConfirmDialog> {
        private final String code;
        private final PatientEntity user;

        protected  UserCodeDialogFormEvent(UserCodeConfirmDialog source,
                                           String code, PatientEntity user){
            super(source, false);
            this.code = code;
            this.user = user;
        }
    }
    /**
     * Clase heredada de UserCodeDialogFormEvent, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece el codigo
     *     asociada al evento.
     */
    public static  class SaveCode extends UserCodeDialogFormEvent {
        SaveCode(UserCodeConfirmDialog source,String code, PatientEntity user){
            super(source, code, user);
        }
    }

    /**
     * Clase heredada de UserCodeDialogFormEvent, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static  class CloseEvent extends UserCodeDialogFormEvent {
        CloseEvent(UserCodeConfirmDialog source){
            super(source, null, null);
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
