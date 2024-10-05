package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.PatientsLogEntity;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que genera un documento PDF con el listado de todas las modificaciones de los pacientes de la aplicación.
 * Extiende de la clase MarterReport para gestionar la generación de documentos PDF.
 */
public class UserAccessGridViewPdf extends MasterReport {
        private final String header;
        private final ArrayList<String> titles;
        private final ArrayList<PatientsLogEntity> patientsLogEntities;

        /**
         * Constructor.
         * @param patientsLogEntities Lista de cambios que irán en el PDF.
         */
        public UserAccessGridViewPdf(ArrayList<PatientsLogEntity> patientsLogEntities) {
            super("listado_modificaciones_paciente.pdf");
            this.patientsLogEntities = patientsLogEntities;
            this.header = "Listado de cambios "+ LocalDate.now().getDayOfMonth()+"/"+LocalDate.now().getMonthValue()+"/"+LocalDate.now().getYear();
            titles = new ArrayList<>(List.of("Paciente","Dni", "Sanitario", "Dni","Día","Minutos", "IP"));
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
         * Añade las filas de los datos de las tipos de citas a a la tabla del documento.
         * @param table Tabla donde se añadirán las filas.
         */
        private void addRows(PdfPTable table) {
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (PatientsLogEntity patientsLogEntity : patientsLogEntities) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(patientsLogEntity.getPatientEntity() != null ? patientsLogEntity.getPatientEntity(): ""), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(patientsLogEntity.getPatientEntity() != null ? patientsLogEntity.getPatientEntity().getDni(): ""), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(patientsLogEntity.getSanitaryEntity() != null ? patientsLogEntity.getSanitaryEntity(): ""), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(patientsLogEntity.getSanitaryEntity() != null ? patientsLogEntity.getSanitaryEntity().getDni(): ""), font)));
                table.addCell(new PdfPCell(new Phrase(patientsLogEntity.getDate().getDayOfMonth()+"/" + patientsLogEntity.getDate().getMonthValue() +"/"+ patientsLogEntity.getDate().getYear(), font)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(patientsLogEntity.getTime()), font)));
                table.addCell(new PdfPCell(new Phrase(patientsLogEntity.getIp() != null ? patientsLogEntity.getIp() : "", font)));

            }
        }

        /**
         * Genera el documento PDF con el listado de accesos.
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

                PdfPTable table = new PdfPTable(new float[]{2,1,2,1,1,1,1});
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
