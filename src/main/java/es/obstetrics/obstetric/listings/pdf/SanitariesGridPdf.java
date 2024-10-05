package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todos los sanitarios registrados en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class SanitariesGridPdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<SanitaryEntity> sanitaryEntities;
    private static final BaseColor LIGHT_PURPLE =new BaseColor(172, 147, 197, 128);

    /**
     * Constructor.
     * @param sanitaryEntities Lista de sanitarios que irán en el PDF.
     */
    public SanitariesGridPdf(ArrayList<SanitaryEntity> sanitaryEntities) {
        super("listado_trabajadores.pdf");
        this.sanitaryEntities = sanitaryEntities;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Listado de trabajadores " + now;
        titles = new ArrayList<>(List.of("Nombre", "Apellidos", "DNI", "Sexo","Email",
                "Edad","Rol","Teléfono", "Dirección", "Código postal"));
    }

    /**
     * Añade el encabezado de la tabla al documento.
     * @param table Tabla donde se añadirá el encabezado.
     */
    private void addTableHeader(PdfPTable table) {
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,6);
        for (String title : titles) {
            PdfPCell header = new PdfPCell(new Phrase(title, headFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(LIGHT_PURPLE);
            table.addCell(header);
        }
    }

    /**
     * Añade las filas de los datos de los santiarios a a la tabla del docuemnto.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 6);
        for (SanitaryEntity patient : sanitaryEntities) {
            table.addCell(new PdfPCell(new Phrase(patient.getName() != null ? patient.getName() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getLastName() != null ? patient.getLastName() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getDni() != null ? patient.getDni() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getSex() != null ? patient.getSex() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getEmail() != null ? patient.getEmail() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getAge() != null ? patient.getAge() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getRole() != null ? patient.getRole() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getPhone() != null ? patient.getPhone() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getAddress() != null ? patient.getAddress() : "", font)));
            table.addCell(new PdfPCell(new Phrase(patient.getPostalCode() != null ? patient.getPostalCode() : "", font)));
        }
    }

    /**
     * Genera el documento PDF con el listado de sanitarios.
     *
     * @return El InputStream con el PDF generado.
     */
    @Override
    public InputStream generatePdf() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //Crea el flujo de bytes en memoria, que se usa para almacenar los datos binarios del PDF.
        Document document = new Document(PageSize.A4, 20, 20, 90, 20); //Margenes del documento.
        try {
            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream); //Writer responsable de escribir el contenido en el document.
            EventPerPagePdf event = new EventPerPagePdf(document, header, titles, new float[]{15f, 15f, 15f, 15f, 20f, 20f}, this.getFontSize()); //Maneja eventos por cada pagina (para establecer el encabezado y pie de pagina).
            writer.setPageEvent(event);

            document.open(); //Se abre el documento
            document.setMargins(20, 20, 90, 20);

            PdfPTable table = new PdfPTable(new float[]{1, 2, 2, 2, 3, 1,2,2,3,1}); //Se crea una tabla con 6 columnas, cada una de lismo ancho.
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
