package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todos las agendas registrado en la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class DiaryGridPdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<DiaryEntity> diaryEntities;
    private static final BaseColor LIGHT_PURPLE =new BaseColor(172, 147, 197, 128);
    /**
     * Constructor.
     * @param diaryEntities Lista de agendas que irán en el PDF.
     */
    public DiaryGridPdf(ArrayList<DiaryEntity> diaryEntities) {
        super("listado_agendas.pdf");
        this.diaryEntities = diaryEntities;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Listado de agendas " + now;
        titles = new ArrayList<>(List.of("Nombre", "Sanitario","Centro", "Lun","Mar","Mie","Jue","Vie","Sab","Dom","Horario"));
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
     * Añade las filas de los datos de los horarios a a la tabla del documento.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (DiaryEntity diaryEntity : diaryEntities) {
            table.addCell(new PdfPCell(new Phrase(diaryEntity.getName() != null ? diaryEntity.getName() : "", font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(diaryEntity.getSanitaryEntity() != null ? diaryEntity.getSanitaryEntity() : ""), font)));
            table.addCell(new PdfPCell(new Phrase(diaryEntity.getCenterEntity().getCenterName() != null ? diaryEntity.getCenterEntity().getCenterName(): "", font)));

            if(diaryEntity.isMonday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isTuesday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isWednesday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isThursday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isFriday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isSunday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }

            if(diaryEntity.isSaturday()){
                table.addCell(new PdfPCell(new Phrase("SI", font)));
            }else{
                table.addCell(new PdfPCell(new Phrase("NO", font)));
            }
            Phrase phrase = new Phrase();
            phrase.setFont(font);
            for(ScheduleEntity oneSchedule: diaryEntity.getSchedules()){
                if(oneSchedule.getEndingDate() == null){
                    if(oneSchedule.getEndTime() == null){
                        phrase.add(new Chunk(oneSchedule.getStartDate() + " " + oneSchedule.getStartTime(), font));
                    }else{
                        phrase.add(new Chunk(oneSchedule.getStartDate() + " " + oneSchedule.getStartTime() + "-" + oneSchedule.getEndTime(), font));
                    }
                }else if(oneSchedule.getEndTime() == null){
                    phrase.add(new Chunk(oneSchedule.getStartDate() + "/" + oneSchedule.getEndingDate() + " " + oneSchedule.getStartTime(), font));
                }else{
                    phrase.add(new Chunk(oneSchedule.getStartDate() + "/" + oneSchedule.getEndingDate() + " " + oneSchedule.getStartTime() + "-" + oneSchedule.getEndTime(), font));
                }

                phrase.add(Chunk.NEWLINE);//Salto de linea
            }
            table.addCell(new PdfPCell(phrase));
        }
    }

    /**
     * Genera el documento PDF con el listado de horarios.
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

            PdfPTable table = new PdfPTable(new float[]{3,4,4,1,1,1,1,1,1,1,6}); //Se crea una tabla con 6 columnas, cada una de lismo ancho.
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

