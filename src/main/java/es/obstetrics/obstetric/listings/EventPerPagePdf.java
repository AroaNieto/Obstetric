package es.obstetrics.obstetric.listings;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Clase que define eventos personalizados para encabezado y pie de página en un documento PDF.
 * Extiende de PdfPageEventHelper con sus métodos para controlar el contenido que se agrega en CADA PÁGINA del PDF.
 */
public class EventPerPagePdf extends PdfPageEventHelper {

    private final String headerString;
    private static final BaseColor LIGHT_GRAY = new BaseColor(239, 238, 238);
    private Document doc;
    private final float[] columnsWidths;
    private final int fontsize;


    public EventPerPagePdf(Document doc, String headerString, ArrayList<String> title, float[] columnsWidths, int fontsize) {
        this.headerString = headerString;
        this.fontsize = fontsize;
        this.columnsWidths = columnsWidths;
    }

    public EventPerPagePdf(Document doc, String headerString, String title, float[] columnsWidths, int fontsize) {
        this.headerString = headerString;
        this.fontsize = fontsize;
        this.columnsWidths = columnsWidths;
    }

    /**
     * Método que crea y retorna una tabla para el encabezado del documento PDF.
     * @param msg Mensaje que se mostrara en el encabezado
     * @return Tabla de encabezado configurada.
     */
    private PdfPTable createHeaderTable(String msg) {
        PdfPTable headerTable = new PdfPTable(new float[]{10f, 80f, 10f}); // Proporciones de las oclumnas
        headerTable.setWidthPercentage(100);
        try {
            Image imageHeader = Image.getInstance("frontend/themes/my-theme/photos/untitled-logo.png");
            imageHeader.scaleToFit(50, 50);
            imageHeader.setBorder(Rectangle.NO_BORDER);

            PdfPCell imageCell = new PdfPCell(imageHeader);
            imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            imageCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(imageCell);

            PdfPCell titleCell = new PdfPCell(new Phrase(msg, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK)));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(titleCell);

            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(emptyCell);
            // Encapsular la tabla en una celda con fondo gris
            PdfPTable wrapperTable = new PdfPTable(1);
            wrapperTable.setWidthPercentage(100);
            PdfPCell wrapperCell = new PdfPCell(headerTable);
            wrapperCell.setBackgroundColor(LIGHT_GRAY);
            wrapperCell.setPadding(5); // Ajustar el espacio dentro del recuadro
            wrapperCell.setBorder(Rectangle.NO_BORDER);
            wrapperTable.addCell(wrapperCell);
            return wrapperTable;
        } catch (BadElementException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que crea y retorna una tabla para el pie de página del documento PDF.
     * @param writer PdfWriter utilizado para escribir el contenido en el documento PDF.
     * @return PdfPTable Tabla de pie de página configurada.
     */
    private PdfPTable createFooterTable(PdfWriter writer) {
        PdfPTable footerTable = new PdfPTable(new float[]{1F});
        footerTable.setWidthPercentage(100);

        int pageNum = writer.getPageNumber();
        PdfPCell cell = new PdfPCell(new Phrase(" -" + pageNum + "-", new Font(Font.FontFamily.HELVETICA, 12)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(cell);

        return footerTable;
    }

    /**
     * Se llama automáticamente al final de cada página cuando se está generando el documento PDF. Se añade el encabezado y el pie de página.
     * Se configura el ancho total de la tabla para que sea igual al ancho del documento (document.right() - document.left())
     *
     * @param writer PdfWriter utilizado para escribir el contenido en el documento PDF.
     * @param document Documento PDF sobre el cual se están añadiendo los encabezados y pies de página.
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable header = createHeaderTable(headerString);
        header.setTotalWidth(document.right() - document.left());
       // header.writeSelectedRows(0, -1, document.left(), document.top() + ((document.topMargin() + header.getTotalHeight()) / 2 - 7), writer.getDirectContent()); //Posición donde se colocará el encabezado.
        header.writeSelectedRows(0, -1, document.left(), document.top() + ((document.topMargin() + header.getTotalHeight()) / 2 - 5), writer.getDirectContent());

        PdfPTable footer = createFooterTable(writer);
        footer.setTotalWidth(document.right() - document.left());
        footer.writeSelectedRows(0, -1, document.left(), document.bottom() +10, writer.getDirectContent()); //Posición donde se colocará el pie de página
    }
}
