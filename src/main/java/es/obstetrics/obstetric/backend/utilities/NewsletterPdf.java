package es.obstetrics.obstetric.backend.utilities;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import es.obstetrics.obstetric.backend.entity.NewsletterEntity;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.view.allUsers.dialog.PdfDialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Clase encarga de mostrar las newsletter que le han llegado al paciente ppor el email,
 *  recoge el valor del parametro, que se correspponde con el id del contenido, busca el
 *  byte del contenido en la BDD y lo añade en un cuadro de diálogo para que muestre el PDF.
 */
@Route(value = "/user/newsletter")
@PageTitle("MotherBloom-Newsletter")
@AnonymousAllowed
public class NewsletterPdf extends Div implements HasUrlParameter<String> {

   private final NewsletterService newsletterService;


    @Autowired
    public NewsletterPdf(NewsletterService newsletterService){
        this.newsletterService = newsletterService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String id) {
        if (id == null) {
            add("error");
        } else {
            Optional<NewsletterEntity> content = newsletterService.findById(Long.parseLong(id));
            PdfDialog pdfDialog = new PdfDialog(content.get().getContentBytePdf(), content.get().getName());
            pdfDialog.open(); //Si el usuario desea ver el pdf, se abre el cuadro de diálogo
            pdfDialog.setMaxWidth("350px");
        }
    }

}
