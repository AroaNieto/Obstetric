package es.obstetrics.obstetric.view.priv.views.maintenance.content;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.PatientService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import es.obstetrics.obstetric.resources.templates.ImgTemplate;
import es.obstetrics.obstetric.view.allUsers.dialog.PdfDialog;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Route(value = "patients/newsletter", layout = PrincipalView.class)
@PageTitle("MotherBloom-Newsletter")
@PermitAll
public class NewsletterPatientView extends Div {
    private VerticalLayout containerVl;
    private final ArrayList<String> pastelColors;

    @Autowired
    public NewsletterPatientView(UserCurrent userCurrent,
                                 NewsletterService newsletterService,
                                 PatientService patientService) {

        pastelColors = new ArrayList<>();
        pastelColors.add("#FFB6C1");
        pastelColors.add("#D8BFD8");
        pastelColors.add("#FFFFE0");
        pastelColors.add("#A0EBBB");
        pastelColors.add("#ADD8E6");
        pastelColors.add("#FFD1DC");
        pastelColors.add("#FFDAC1");
        pastelColors.add("#E0BBE4");
        pastelColors.add("#C1E1C1");
        pastelColors.add("#FEE5AC");
        pastelColors.add("#B5EAD7");
        pastelColors.add("#C8E6C9");

        Div backgroundVL = new Div(createTitle("“Estar embarazada es estar vitalmente viva, completamente mujer y angustiosamente habitada." +
                " El alma y el espíritu se estiran –junto con el cuerpo– haciendo del embarazo un momento de transición, crecimiento y comienzos profundos”."),
                createTitle("Anne Christian Buchanan"));
        backgroundVL.addClassName("newsletter-background");
        setSizeFull();
        HorizontalLayout backgroundHl = new HorizontalLayout(backgroundVL);

        UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.HOME.create(), new UserEntity(), new H3("Mis newsletters"));
        header.getButton().addClickListener(buttonClickEvent ->
                UI.getCurrent().getPage().executeJs("window.history.back()") //Se dirige a la ventana anterior
        );
        //Comprobación de si tiene el embarazo activo
        PatientEntity patient = patientService.findOneByUsername(userCurrent.getCurrentUser().getUsername());
        if (patient != null && patient.getPregnancies() != null) {

            PregnanceEntity pregnancy = Utilities.isPregnantActive(patient.getPregnancies());
            if (pregnancy != null) {
                int quarter = Utilities.quarterCalculator(pregnancy.getLastPeriodDate().toEpochDay());
                FormLayout contentFl = new FormLayout();
                contentFl.setResponsiveSteps( //Diseño responsivo
                        new FormLayout.ResponsiveStep("0", 1),
                        new FormLayout.ResponsiveStep("30em", 2),
                        new FormLayout.ResponsiveStep("50em", 3),
                        new FormLayout.ResponsiveStep("60em", 5)
                );
                List<NewsletterEntity> newsletters = new ArrayList<>();
                if (quarter >= 0 && quarter <= 12) { //Primer trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.FIRST_QUARTER, ConstantUtilities.STATE_ACTIVE); //Lista de contenidos

                } else if (quarter >= 13 && quarter <= 24) { //Segundo trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.SECOND_QUARTER, ConstantUtilities.STATE_ACTIVE);//Lista de contenidos

                } else { //Tercer trimestre
                    newsletters = newsletterService.findByQuarterAndState(ConstantUtilities.THIRD_QUARTER, ConstantUtilities.STATE_ACTIVE);//Lista de contenidos
                }
                PatientEntity patientEntity = (PatientEntity) userCurrent.getCurrentUser();
                newsletters.addAll(patientEntity.getNewsletters());
                for (NewsletterEntity c : newsletters) {
                    Random random = new Random();
                    StreamResource resource = createResource(c);
                    createStyleContainer(c, new ImgTemplate(resource, "150px"), random.nextInt(pastelColors.size()));

                    contentFl.add(containerVl);
                }

                contentFl.getStyle().set("margin-left", "50px")
                        .set("margin-right", "50px");
                add(header,backgroundHl, contentFl);
            }

        } else {
            add(header,backgroundHl);
        }
    }

    private H3 createTitle(String title) {
        H3 titleH3 = new H3(title);
        titleH3.addClassName("title-newsletter");
        return titleH3;
    }

    /**
     * Crea el StreamResource que se usará mpara mostrar por pantalla la miniatura,, dependiendo
     * del tipo, se recoge un byte[] u otro.
     *
     * @param c Contenido sobre el que se creara el stremResource para
     *          mostrar por pantalla el contenido.
     */
    private StreamResource createResource(NewsletterEntity c) {
        if (c.getContentByteUrl() != null) { //Se establece la fotografía dependiendo de su tipo
            return new StreamResource("foto.jpg", () -> new ByteArrayInputStream(c.getContentByteUrl()));
        } else {
            return new StreamResource("foto.jpg", () -> new ByteArrayInputStream(c.getContentMiniature()));
        }
    }

    /**
     * Creación de los estilos del contenedor y se añaden sus eventos de click correspondientes
     * dependiendo de si se trata de una URL o un PDF.
     */
    private void createStyleContainer(NewsletterEntity c, HorizontalLayout image, int i) {
        containerVl = new VerticalLayout();
        containerVl.add(new H5(c.getName()), image, new Span(c.getSummary()));
        containerVl.getStyle().set("background-color", pastelColors.get(i))
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("border-radius", "20px")
                .set("font-family", "'Times New Roman', serif"); //Cuando se posiciona sobre él el cursor se activa
        containerVl.setMargin(true);
        containerVl.setPadding(true);
        containerVl.setMaxHeight("300px");
        containerVl.setMinHeight("300px");
        containerVl.addClickListener(e -> {
            if (c.getContentByteUrl() != null) {
                getUI().ifPresent(ui -> ui.getPage().executeJs("window.open($0, '_blank')", c.getUrl())); //Si el usuario pulsa en el contenido con URL, se redirige a el
            } else if (c.getContentMiniature() != null) {
                PdfDialog pdfDialog = new PdfDialog(c.getContentBytePdf(), c.getName());
                pdfDialog.open(); //Si el usuario desea ver el pdf, se abre el cuadro de diálogo
            }
        });
    }

}
