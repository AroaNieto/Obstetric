package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.AppointmentEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentForInsurancePdf extends MasterReport {

    private final String header;
    private final ArrayList<String> titles;
    private final ArrayList<AppointmentEntity> appointmentEntities;

    /**
     * Constructor.
     * @param appointmentEntities Lista de citas con las aseguradoras que irán en el PDF.
     */
    public AppointmentForInsurancePdf(ArrayList<AppointmentEntity> appointmentEntities, LocalDate value, LocalDate value1) {
        super("listado_citas.pdf");
        this.appointmentEntities = appointmentEntities;
        this.header = "Listado de citas y aseguradoras " + value.getDayOfMonth() + "/"+ value.getMonthValue()+"/"+value.getYear() +" - " + value1.getDayOfMonth() + "/"+ value1.getMonthValue()+"/"+value1.getYear();
        titles = new ArrayList<>(List.of("Día","Medico","Paciente","Dni","Aseguradora", "Póliza","Tipo de cita"));
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
     * Añade las filas de los datos de las citas a a la tabla del documento.
     * @param table Tabla donde se añadirán las filas.
     */
    private void addRows(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (AppointmentEntity appointmentEntity : appointmentEntities) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getDate() != null ? appointmentEntity.getDate() : ""), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() != null ? appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity(): ""), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getPatientEntity() != null ? appointmentEntity.getPatientEntity() : ""), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getPatientEntity() != null ? appointmentEntity.getPatientEntity().getDni()  : ""), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getInsuranceEntity() != null ? appointmentEntity.getInsuranceEntity() : ""), font)));
            table.addCell(new PdfPCell(new Phrase(appointmentEntity.getInsurancePolice() != null ? appointmentEntity.getInsurancePolice() : "", font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(appointmentEntity.getAppointmentTypeEntity() != null ? appointmentEntity.getAppointmentTypeEntity() : ""), font)));
        }
    }

    /**
     * Genera el documento PDF con el listado de citas.
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

            PdfPTable table = new PdfPTable(new float[]{1,2,2,1,2,2,1});
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
