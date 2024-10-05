package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;

/**
 * Clase que genera un documento PDF con el consentimiento informado del paciente que se acaba de registrar en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class InformedConsentPdf extends MasterReport {
    private final String header;
    private final String title;
    private final ConstantValues constantValues;
    private String name;
    private final String lastname;
    /**
     * Cosntructor
     * @param name Nombre del paciente
     * @param lastname Apellidos
     * @param constantValues Valores para asiganr al consentimiento
     */
    public InformedConsentPdf(String name,String lastname, ConstantValues constantValues) {
        super("consentimiento"+name+" "+lastname+".pdf");
        this.constantValues = constantValues;
        this.name = name;
        this.lastname = lastname;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Consentimiento informado " + now;
        this.title = "Texto informátivo sobre el tratamiento de datos personales";
    }

    /**
     * Genera el documento PDF con consentimiento informado.
     *
     * @return El InputStream con el PDF generado.
     */
    @Override
    public InputStream generatePdf() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Crea el flujo de bytes en memoria, que se usa para almacenar los datos binarios del PDF.
        Document document = new Document(PageSize.A4, 20, 20, 90, 20); // Márgenes del documento.
        try {
            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream); // Writer responsable de escribir el contenido en el documento.
            EventPerPagePdf event = new EventPerPagePdf(document, header, title, new float[]{15f, 15f, 15f, 15f, 20f, 20f}, this.getFontSize()); // Maneja eventos por cada página (para establecer el encabezado y pie de página).
            writer.setPageEvent(event);

            document.open(); // Se abre el documento
            document.setMargins(20, 20, 90, 20);

            Font fontContent = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            document.add(new Paragraph(" "));
            document.add(new PdfPCell(new Phrase(
                    "A los efectos previstos en el Reglamento Europeo de Protección de Datos y la reciente Ley Orgánica 3/2018, de Protección de Datos Personales, se le requiere su consentimiento para el tratamiento de sus datos personales en relación con el PROGRAMA DE EXCELENCIA REGULATORIA DE ESPAÑA (PERE), gestionado por la Comisión Nacional de los Mercados y la Competencia (CNMC), en colaboración con Red.es y la Secretaría de Estado para el Avance Digital (SEAD).", fontContent)));
            document.add(new Paragraph("Dicho consentimiento podrá ser retirado en cualquier momento, siendo lícito el tratamiento realizado antes de la retirada.", fontContent));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("¿Quién es el responsable del tratamiento? ", fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph(constantValues.getDateConsent(), fontContent));
            document.add(new Paragraph("Dirección de correo electrónico de contacto del responsable: " + constantValues.getEmailAdmin(), fontContent));
            document.add(new Paragraph("Contacto del Delegado de Protección de Datos: " + constantValues.getEmailAdmin(), fontContent));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("¿Para qué utilizamos sus datos personales? ", fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Utilizamos sus datos para gestionar su participación en la aplicación de Procesos Obstétricos y mejorar nuestros servicios.", fontContent));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("¿Durante cuánto tiempo conservamos sus datos?", fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Sus datos serán conservados durante el tiempo necesario para cumplir con los fines para los cuales fueron recogidos y según lo estipulado en la normativa vigente.", fontContent));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("¿Cuáles son sus derechos cuando nos facilita sus datos?", fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("• Derecho a solicitar el acceso a los datos personales", fontContent));
            document.add(new Paragraph("• Derecho a solicitar la rectificación de sus datos.", fontContent));
            document.add(new Paragraph("• Derecho a solicitar la supresión de sus datos.", fontContent));
            document.add(new Paragraph("• Derecho a solicitar la limitación del tratamiento en determinadas circunstancias.", fontContent));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("¿Dónde puedo ejercitar mis derechos?", fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Podrá ejercitar sus derechos enviando una comunicación escrita, acompañada de fotocopia de su DNI o documento acreditativo de su identidad, a la dirección indicada anteriormente.", fontContent));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Nombre y apellidos del paciente: " + name + " " + lastname, fontContent));
            document.add(new Paragraph(" "));
            Paragraph signature = new Paragraph("Firma del paciente: ______________________________________________________", FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK));
            signature.setAlignment(Element.ALIGN_LEFT);
            document.add(signature);

        } catch (DocumentException e) {
            log.error("Error al crear el documento PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
