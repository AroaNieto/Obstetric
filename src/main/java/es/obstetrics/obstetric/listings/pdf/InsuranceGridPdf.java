package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.CenterEntity;
import es.obstetrics.obstetric.backend.entity.InsuranceEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todos las aseguradoras registradas en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class InsuranceGridPdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<InsuranceEntity> insuranceEntities;
    private static final BaseColor LIGHT_PURPLE = new BaseColor(172, 147, 197, 128);
    /**
     * Constructor.
     * @param insuranceEntities Lista de aseguradoras que irán en el PDF.
     */
    public InsuranceGridPdf(ArrayList<InsuranceEntity> insuranceEntities) {
        super("listado_aseguradoras.pdf");
        this.insuranceEntities = insuranceEntities;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Listado de aseguradoras " + now;
        titles = new ArrayList<>(List.of("Nombre", "Teléfono", "Email","Dirección","Código postal","Centros asociados"));
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
     * Añade las filas de los datos de las aseguradoras a a la tabla del documento.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (InsuranceEntity insurance : insuranceEntities) {
            table.addCell(new PdfPCell(new Phrase(insurance.getName() != null ? insurance.getName() : "", font)));
            table.addCell(new PdfPCell(new Phrase(insurance.getPhone() != null ? insurance.getPhone() : "", font)));
            table.addCell(new PdfPCell(new Phrase(insurance.getEmail() != null ? insurance.getEmail() : "", font)));
            table.addCell(new PdfPCell(new Phrase(insurance.getAddress() != null ? insurance.getAddress() : "", font)));
            table.addCell(new PdfPCell(new Phrase(insurance.getPostalCode() != null ? insurance.getPostalCode() : "", font)));
            Phrase phrase = new Phrase();
            phrase.setFont(font);
            for(CenterEntity oneCenter: insurance.getCenters()){
                phrase.add(new Chunk(oneCenter.getCenterName(), font));
                phrase.add(Chunk.NEWLINE);//Salto de linea
            }
            table.addCell(new PdfPCell(phrase));
        }
    }

    /**
     * Genera el documento PDF con el listado de categorías.
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
            document.setMargins(25, 25, 90, 25);

            PdfPTable table = new PdfPTable(new float[]{3,2,3,3,1,4});
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

