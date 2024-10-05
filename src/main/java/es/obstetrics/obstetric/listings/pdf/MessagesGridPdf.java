package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todos los mensajes registrados en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class MessagesGridPdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<NotificationEntity> notificationEntities;
    private static final BaseColor LIGHT_PURPLE = new BaseColor(172, 147, 197, 128);
    /**
     * Constructor.
     * @param notificationEntities Lista de los avisos que irán en el PDF.
     */
    public MessagesGridPdf(ArrayList<NotificationEntity> notificationEntities) {
        super("listado_mensajes.pdf");
        this.notificationEntities = notificationEntities;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Listado de mensajes " + now;
        titles = new ArrayList<>(List.of("Destinatario", "Canal", "Fecha de envio", "Mensaje"));
    }

    /**
     * Añade el encabezado de la tabla al documento.
     * @param table Tabla donde se añadirá el encabezado.
     */
    private void addTableHeader(PdfPTable table) {
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,8);
        for (String title : titles) {
            PdfPCell header = new PdfPCell(new Phrase(title, headFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(LIGHT_PURPLE);
            table.addCell(header);
        }
    }

    /**
     * Añade las filas de los datos de los mensajes a a la tabla del documento.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (NotificationEntity oneNotification : notificationEntities) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(oneNotification.getUserEntity() != null ? oneNotification.getUserEntity() : ""), font)));
            table.addCell(new PdfPCell(new Phrase(oneNotification.getChanel() != null ? oneNotification.getChanel() : "", font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(oneNotification.getShippingDate() != null ? oneNotification.getShippingDate() : ""), font)));
            if(oneNotification.getNewsletterEntity() != null){
                table.addCell(new PdfPCell(new Phrase(oneNotification.getNewsletterEntity().getName(), font)));
            }else{
                table.addCell(new PdfPCell(new Phrase(" Cita el día "+  oneNotification.getAppointmentEntity().getDate() + " a las "+oneNotification.getAppointmentEntity().getStartTime()
                        + " con " +  oneNotification.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " "
                        + oneNotification.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName(), font)));
            }

        }
    }

    /**
     * Genera el documento PDF con el listado de los mensajes.
     *
     * @return El InputStream con el PDF generado.
     */
    @Override
    public InputStream generatePdf() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //Crea el flujo de bytes en memoria, que se usa para almacenar los datos binarios del PDF.
        Document document = new Document(PageSize.A4, 25, 25, 90, 25); //Margenes del documento.
        try {
            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream); //Writer responsable de escribir el contenido en el document.
            EventPerPagePdf event = new EventPerPagePdf(document, header, titles, new float[]{15f, 15f, 15f, 15f, 20f, 20f}, this.getFontSize()); //Maneja eventos por cada pagina (para establecer el encabezado y pie de pagina).
            writer.setPageEvent(event);

            document.open(); //Se abre el documento
            document.setMargins(50, 50, 90, 50);

            PdfPTable table = new PdfPTable(new float[]{2,1,1,2}); //Se crea una tabla con 6 columnas, cada una de lismo ancho.
            table.setWidthPercentage(100); //La tabla ocupará el 100% del ancho disponible
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableHeader(table); //Añade el encabezado de la tabla
            addRows(table); //Añade las filas de la tabla.

            document.add(table); //Se añade la tabla completa
        } catch (DocumentException e) {
            log.error("Error al crear el documento PDF", e);
        } finally {
            document.close();
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}