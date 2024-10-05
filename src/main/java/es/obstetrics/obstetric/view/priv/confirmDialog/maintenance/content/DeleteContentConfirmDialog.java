package es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.content;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.view.priv.confirmDialog.MasterConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.MasterDialog;
import es.obstetrics.obstetric.view.priv.views.maintenance.content.CategoriesGridView;

/**
 * Clase que extiende de la clase {@link MasterDialog}, se usa para
 *  que el usuario confirme si desea eliminar el contenido.
 */
public class DeleteContentConfirmDialog extends MasterConfirmDialog {

    private final Binder<NewsletterEntity> contentEntityBinder;

    public DeleteContentConfirmDialog(NewsletterEntity content){

        contentEntityBinder = new Binder<>(NewsletterEntity.class);
        contentEntityBinder.setBean(content);  //Recojo el contenido
        contentEntityBinder.readBean(content);
        createHeaderAndTextDialog(); //Establecer los valores
    }


    /**
     * Crea la cabecera y le da el estilo correspondiente.
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader("Dar de baja contenido");
        setText("Se va a proceder a dar de baja el contenido: "
                + contentEntityBinder.getBean().getName().toUpperCase() +
                ", ¿Está seguro?");
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe cerrar el cuadro de diálogo.
     */
    @Override
    public void closeDialog() {
        close();
        fireEvent(new CloseEvent(this));
    }

    /**
     * Dispara el evento para notificar a la clase {@link CategoriesGridView }
     que debe borrar el contenido.
     */
    @Override
    public void clickButton() {
        close();
        fireEvent(new DeleteEvent(this,contentEntityBinder.getBean()));
    }

    /**
     * Clase abstracta que extiene de {@link DeleteContentConfirmDialog}, evento ocurrido en dicha clase.
     *  Almacena el contenido asociado al evento.
     */
    public static  abstract  class DeteleContentConfirmDialog extends ComponentEvent<DeleteContentConfirmDialog> {
        private NewsletterEntity newsletterEntity; //Comunidad con la que trabajamos

        protected  DeteleContentConfirmDialog(DeleteContentConfirmDialog source, NewsletterEntity newsletterEntity){
            super(source, false);
            this.newsletterEntity = newsletterEntity;
        }

        public NewsletterEntity getContent(){
            return newsletterEntity;
        }
    }
    /**
     * Clase heredada de DeleteContentDialog, representa un evento de cerrar que ocurre en el diálogo,
     *   Tiene un constructor que llama al constructor de la super clase y establece la subcategori asociada al evento.
     */
    public static  class DeleteEvent extends DeteleContentConfirmDialog{
        DeleteEvent(DeleteContentConfirmDialog source, NewsletterEntity newsletterEntity){
            super(source, newsletterEntity);
        }
    }

    public static  class CloseEvent extends DeteleContentConfirmDialog {
        CloseEvent(DeleteContentConfirmDialog source){
            super(source, null);
        }
    }

    /**
     * Método que permite registrar un listener par aun tipo específico de evento.
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener El listener que manejará. el evento.
     * @return Un objeto Registation que permite anular el registro del listener cuando sea necesario.
     * @param <T>
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener){
        return getEventBus().addListener(eventType, listener);
    }
}
