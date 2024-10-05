package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.CategoryEntity;
import es.obstetrics.obstetric.backend.entity.SubcategoryEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todas las categorías y  subcategorías asociadas registradas en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class CategoriesGridPdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<CategoryEntity> categoryEntities;

    /**
     * Constructor.
     * @param categoryEntities Lista de categorias que irán en el PDF.
     */
    public CategoriesGridPdf(ArrayList<CategoryEntity> categoryEntities) {
        super("listado_categorías.pdf");
        this.categoryEntities = categoryEntities;
        this.header = "Listado de categorías";
        titles = new ArrayList<>(List.of("Nombre categoría", "Descripción categoría", "Subcategorías asociadas"));
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
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(header);
        }
    }

    /**
     * Añade las filas de los datos de las categorias a a la tabla del documento.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (CategoryEntity categoryEntity : categoryEntities) {
            table.addCell(new PdfPCell(new Phrase(categoryEntity.getName() != null ? categoryEntity.getName() : "", font)));
            table.addCell(new PdfPCell(new Phrase(categoryEntity.getDescription() != null ? categoryEntity.getDescription() : "", font)));
            Phrase phrase = new Phrase();
            phrase.setFont(font);
            for(SubcategoryEntity oneSubcategory: categoryEntity.getSubcategories()){
                phrase.add(new Chunk(oneSubcategory.getName(), font));
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

            PdfPTable table = new PdfPTable(new float[]{2, 4,4});
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
