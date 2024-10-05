package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.MatronReportEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Clase que genera un documento PDF con el informe para las matronas.
 */
public class MatronReportPdf extends MasterReport {
    private final String header;
    private final String title;
    private final MatronReportEntity reportEntity;
    /**
     * Cosntructor
     * @param reportEntity Informe
     */
    public MatronReportPdf(MatronReportEntity reportEntity) {
        super("informe_"+reportEntity.getAppointmentEntity().getPatientEntity().getName()+"_"+reportEntity.getAppointmentEntity().getPatientEntity().getLastName()+".pdf");
        this.reportEntity = reportEntity;
        String now = reportEntity.getDate().getDayOfMonth() + "/" + reportEntity.getDate().getDayOfMonth() + "/" + reportEntity.getDate().getYear();
        this.header = "INFORME MÉDICO "+reportEntity.getAppointmentEntity().getPatientEntity().getName()+" "+reportEntity.getAppointmentEntity().getPatientEntity().getLastName()+ " " + now;;
        this.title = "INFORME MÉDICO "+reportEntity.getAppointmentEntity().getAppointmentTypeEntity().getDescription();
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

            Font fontContent = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            addParagraph(document, "Motivo de consulta: ", reportEntity.getReasonForConsultation(), fontBold, fontContent);
            addParagraph(document, "Matrona: ", String.valueOf(reportEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity()), fontBold, fontContent);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("DATOS MÉDICOS "));
            document.add(Chunk.NEWLINE);
            addParagraph(document, "Alergias: ", reportEntity.getAppointmentEntity().getPatientEntity().getAllergies(), fontBold, fontContent);
            addParagraph(document,"Grupo sanguineo: ",reportEntity.getAppointmentEntity().getPatientEntity().getBloodType(),fontBold, fontContent);
            addParagraph(document,"Rh: ",reportEntity.getAppointmentEntity().getPatientEntity().getRh(), fontBold, fontContent);
            addParagraph(document,"Antecedentes personales: ",reportEntity.getAppointmentEntity().getPatientEntity().getPersonalHistory(), fontBold, fontContent);
            addParagraph(document,"Antecedentes familiares: ",reportEntity.getAppointmentEntity().getPatientEntity().getFamilyBackground(), fontBold, fontContent);
            addParagraph(document,"Menarquia: ", String.valueOf(reportEntity.getAppointmentEntity().getPatientEntity().getMenarche()), fontBold, fontContent);
            addParagraph(document,"Fecha de última regla ", String.valueOf(reportEntity.getAppointmentEntity().getPatientEntity().getFur()), fontBold, fontContent);
            addParagraph(document,"FM: ",reportEntity.getAppointmentEntity().getPatientEntity().getFm(), fontBold, fontContent);
            addParagraph(document,"Embarazos totales: ", String.valueOf(reportEntity.getAppointmentEntity().getPatientEntity().getNumberOfPregnancies()), fontBold, fontContent);
            addParagraph(document,"Número de abortos: ", String.valueOf(reportEntity.getAppointmentEntity().getPatientEntity().getNumberOfAbortions()), fontBold, fontContent);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("DATOS DE LA CITA", fontBold));
            document.add(Chunk.NEWLINE);
            addParagraph(document,"Situación actual:",reportEntity.getCurrentSituation(), fontBold, fontContent);
            addParagraph(document,"Evolución del feto: ",reportEntity.getFetalHeartbeat(), fontBold, fontContent);
            addParagraph(document,"Control del dolor: ",reportEntity.getPainControl(), fontBold, fontContent);
            addParagraph(document,"Recomendaciones: ",reportEntity.getRecommendations(), fontBold, fontContent);

        } catch (DocumentException e) {
            log.error("Error al crear el documento PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    // Método para agregar un párrafo con una etiqueta en negrita y contenido normal
    private void addParagraph(Document document, String label, String content, Font fontBold, Font fontContent) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Phrase(label, fontBold));
        paragraph.add(new Phrase(content, fontContent));
        document.add(paragraph);
    }
}
