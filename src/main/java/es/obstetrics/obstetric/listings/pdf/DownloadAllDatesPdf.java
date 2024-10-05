package es.obstetrics.obstetric.listings.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.EventPerPagePdf;
import es.obstetrics.obstetric.listings.MasterReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Genera el PDF que contiene todos los datos del usuario que ha solicitado
 *  ver todos sus datos almacenados en la aplicación.
 */
public class DownloadAllDatesPdf extends MasterReport {
    private final String header;
    private final String title;
    private final UserEntity user;
    private final NotificationService notificationService;
    private final LoginLogOutLogService loginLogOutLogService;
    private final AppointmentService appointmentService;
    private final PregnanceService pregnanceService;
    private final PatientsLogService patientsLogService;
    private final DiaryService diaryService;
    private final CenterService centerService;
    private final ReportService reportService;
    private static final BaseColor LIGHT_PURPLE = new BaseColor(172, 147, 197, 128);
    /**
     * Constructor
     * @param user usuario sobre el que se va a proceder a recopilar todos sus datos
     */
    public DownloadAllDatesPdf(UserEntity user, NotificationService notificationService,
                               LoginLogOutLogService loginLogOutLogService,
                               AppointmentService appointmentService,
                               PregnanceService pregnanceService,
                               PatientsLogService patientsLogService,
                               DiaryService diaryService,
                               CenterService centerService, ReportService reportService) {
        super("mis_datos_" + user.getName() + " " + user.getLastName() + ".pdf");
        this.diaryService = diaryService;
        this.reportService = reportService;
        this.centerService = centerService;
        this.appointmentService = appointmentService;
        this.pregnanceService = pregnanceService;
        this.patientsLogService = patientsLogService;
        String now = LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getYear();
        this.header = "Mis datos almacenados en Mother Bloom " + now;
        this.user = user;
        this.loginLogOutLogService = loginLogOutLogService;
        this.title = "Texto informátivo sobre el tratamiento de datos personales";
        this.notificationService = notificationService;
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

            Font fontContent = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
            // Agregar la información del usuario
            addParagraph(document, "Nombre y apellidos: ", user.getName() + " " + user.getLastName(), fontBold, fontContent);
            addParagraph(document, "DNI: ", user.getDni(), fontBold, fontContent);
            addParagraph(document, "Edad: ", user.getAge(), fontBold, fontContent);
            addParagraph(document, "Sexo: ", user.getSex(), fontBold, fontContent);
            addParagraph(document, "Dirección: ", user.getAddress(), fontBold, fontContent);
            addParagraph(document, "Código postal: ", user.getPostalCode(), fontBold, fontContent);
            addParagraph(document, "Nombre de usuario: ", user.getUsername(), fontBold, fontContent);
            addParagraph(document, "Email: ", user.getEmail(), fontBold, fontContent);
            addParagraph(document, "Teléfono: ", user.getPhone(), fontBold, fontContent);
            addParagraph(document, "Rol: ", user.getRole(), fontBold, fontContent);

            if (user.getRole().equalsIgnoreCase(ConstantUtilities.ROLE_PATIENT)) {
                PatientEntity patient = (PatientEntity) user;
                addParagraph(document, "Embarazos: ", String.valueOf(patient.getPregnancies().size()), fontBold, fontContent);
                addParagraph(document, "Número de abortos: ", String.valueOf(patient.getNumberOfAbortions()), fontBold, fontContent);
                addParagraph(document, "Número de embarazos: ", String.valueOf(patient.getNumberOfPregnancies()), fontBold, fontContent);
                addParagraph(document, "Tipo de sangre: ", patient.getBloodType(), fontBold, fontContent);
                addParagraph(document, "Rh: ", patient.getRh(), fontBold, fontContent);
                addParagraph(document, "Fecha de última regla: ", patient.getFur().toString(), fontBold, fontContent);
                addParagraph(document, "Menarquia: ", patient.getMenarche().toString(), fontBold, fontContent);
                addParagraph(document, "Fm: ", patient.getFm(), fontBold, fontContent);
                document.add(new Paragraph("Alergias: ", fontBold));
                if(patient.getAllergies() == null){
                    document.add(new Paragraph("", fontContent));
                }else{
                    document.add(new Paragraph(patient.getAllergies(), fontContent));
                }
                document.add(new Paragraph("Antecedentes familiares: ", fontBold));

                document.add(new Paragraph("Historial personal: ", fontBold));
                if(patient.getPersonalHistory() == null){
                    document.add(new Paragraph("", fontContent));
                }else{
                    document.add(new Paragraph(patient.getPersonalHistory(), fontContent));
                }
                document.add(new Paragraph("Antecedentes familiares: ", fontBold));

                if(patient.getFamilyBackground() == null){
                    document.add(new Paragraph("", fontContent));
                }else{
                    document.add(new Paragraph(patient.getFamilyBackground(), fontContent));
                }

                List<ReportEntity> reportEntities = reportService.findByPatientEntityAndState(patient, ConstantUtilities.STATE_ACTIVE);
                addParagraph(document, "Número de informes realizados: ", String.valueOf(reportEntities.size()),fontBold, fontContent);
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco

                document.add(new Paragraph("Citas: ", fontBold));
                document.add(new Paragraph(" "));
                List<AppointmentEntity> appointmentEntities = appointmentService.findByPatientEntity(patient);
                PdfPTable tableAppointment = createTableWithHeader(new String[]{"Sanitario ", "Centro ", "Día", "Horas ", "Tipo de cita", "Aseguradora", "Póliza", "Atendido"}, fontBold);

                for (AppointmentEntity appointmentEntity : appointmentEntities) {
                    tableAppointment.addCell(createCell(String.valueOf(appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() != null ? appointmentEntity.getScheduleEntity().getDiaryEntity().getSanitaryEntity() : ""), fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName() != null ? appointmentEntity.getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName() : "", fontContent));
                    tableAppointment.addCell(createCell(String.valueOf(appointmentEntity.getDate() != null ? appointmentEntity.getDate() : ""), fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getStartTime() + "-" + appointmentEntity.getEndTime(), fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getAppointmentTypeEntity().getDescription() != null ? appointmentEntity.getAppointmentTypeEntity().getDescription() : "", fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getInsuranceEntity() != null ? appointmentEntity.getInsuranceEntity().getName() : "", fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getInsurancePolice() != null ? appointmentEntity.getInsurancePolice() : "", fontContent));
                    tableAppointment.addCell(createCell(appointmentEntity.getHasAttended() != null ? appointmentEntity.getHasAttended() : "", fontContent));
                }

                document.add(tableAppointment); // Añadir la tabla al documento
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco
                document.add(new Paragraph("Embarazos: ", fontBold));
                document.add(new Paragraph(" "));
                List<PregnanceEntity> pregnancyEntities = pregnanceService.findByPatientEntity(patient);
                PdfPTable tablePregnancies = createTableWithHeader(new String[]{"Fecha de ultima regla", "Finalizado"}, fontBold);
                for (PregnanceEntity pregnancy : pregnancyEntities) {
                    tablePregnancies.addCell(createCell(String.valueOf(pregnancy.getLastPeriodDate() != null ? pregnancy.getLastPeriodDate() : ""), fontContent));
                    tablePregnancies.addCell(createCell(String.valueOf(pregnancy.getEndingDate() != null ? pregnancy.getEndingDate() : ""), fontContent));
                }
                document.add(tablePregnancies); // Añadir la tabla al documento
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco

                document.add(new Paragraph("Registros: ", fontBold));
                document.add(new Paragraph(" "));
                List<PatientsLogEntity> patientsLogEntities = patientsLogService.findByPatientEntity(patient);
                PdfPTable tableLogs = createTableWithHeader(new String[]{"Mensaje", "Día", "Hora", "Medico que modificó"}, fontBold);
                for (PatientsLogEntity patientsLogEntity : patientsLogEntities) {
                    tableLogs.addCell(createCell(patientsLogEntity.getMessage() != null ? patientsLogEntity.getMessage() : "", fontContent));
                    tableLogs.addCell(createCell(String.valueOf(patientsLogEntity.getDate() != null ? patientsLogEntity.getDate() : ""), fontContent));
                    tableLogs.addCell(createCell(String.valueOf(patientsLogEntity.getTime() != null ? patientsLogEntity.getTime() : ""), fontContent));
                    tableLogs.addCell(createCell(String.valueOf(patientsLogEntity.getSanitaryEntity()), fontContent));
                }
                document.add(tableLogs); // Añadir la tabla al documento
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco
            } else if (user.getRole().equalsIgnoreCase(ConstantUtilities.ROLE_MATRONE) ||
                    user.getRole().equalsIgnoreCase(ConstantUtilities.ROLE_GYNECOLOGIST)) {
                SanitaryEntity sanitary = (SanitaryEntity) user;
                if(user.getStateMessagingSystemSanitary() == null){
                    document.add(new Paragraph("Estado del chat pacientes: " + "ACTIVO", fontContent));
                }else{
                    document.add(new Paragraph("Estado del chat pacientes: " + user.getStateMessagingSystemPatient(), fontContent));
                }

                if(user.getStateMessagingSystemSanitary() == null){
                    document.add(new Paragraph("Estado del chat sanitarios: " + "ACTIVO", fontContent));
                }else{
                    document.add(new Paragraph("Estado del chat sanitarios: " + user.getStateMessagingSystemSanitary(), fontContent));
                }
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco

                document.add(new Paragraph("Agendas: ", fontBold));
                document.add(new Paragraph(" "));
                List<DiaryEntity> diaryEntities = diaryService.findBySanitaryEntity(sanitary);
                PdfPTable tableDiaryEntities = createTableWithHeader(new String[]{"Nombre", "Centro", "Inicio", "Fin", "Estado"}, fontBold);
                for (DiaryEntity diary : diaryEntities) {
                    tableDiaryEntities.addCell(createCell(diary.getName() != null ? diary.getName() : "", fontContent));
                    tableDiaryEntities.addCell(createCell(String.valueOf(diary.getCenterEntity().getCenterName() != null ? diary.getCenterEntity() : ""), fontContent));
                    tableDiaryEntities.addCell(createCell(String.valueOf(diary.getStartTime()), fontContent));
                    tableDiaryEntities.addCell(createCell(String.valueOf(diary.getEndTime() != null ? diary.getEndTime() : ""), fontContent));
                    tableDiaryEntities.addCell(createCell(diary.getState() != null ? diary.getState() : "", fontContent));
                }
                document.add(tableDiaryEntities); // Añadir la tabla al documento
                document.add(Chunk.NEWLINE); // Añadir una línea en blanco

                document.add(new Paragraph("Centros: ", fontBold));
                document.add(new Paragraph(" "));
                List<CenterEntity> centerEntities = centerService.findBySanitary(sanitary);
                PdfPTable tableSanitaryCenter = createTableWithHeader(new String[]{"Centro"}, fontBold);
                for (CenterEntity oneCenter : centerEntities) {
                    tableSanitaryCenter.addCell(createCell(oneCenter.getCenterName() != null ? oneCenter.getCenterName() : "", fontContent));
                }
                document.add(tableSanitaryCenter); // Añadir la tabla al documento
            }

            document.add(new Paragraph("Accesos: ", fontBold));
            document.add(new Paragraph(" "));
            List<LoginLogOutLogEntity> loginLogOutLogEntities = loginLogOutLogService.findByUserEntity(user);
            PdfPTable tableLoginLogout = createTableWithHeader(new String[]{"Fecha ", "Hora", "Mensaje"}, fontBold);
            for (LoginLogOutLogEntity loginLogOutLogEntity : loginLogOutLogEntities) {
                tableLoginLogout.addCell(createCell(loginLogOutLogEntity.getDate().toString(), fontContent));
                tableLoginLogout.addCell(createCell(loginLogOutLogEntity.getTime().toString(), fontContent));
                tableLoginLogout.addCell(createCell(loginLogOutLogEntity.getMessage(), fontContent));
            }
            document.add(tableLoginLogout); // Añadir la tabla al documento
            document.add(Chunk.NEWLINE); // Añadir una línea en blanco
            document.add(new Paragraph("Mensajes:", fontBold));
            document.add(new Paragraph(" "));
            List<NotificationEntity> notificationEntityList = notificationService.findByUserEntity(user);

            PdfPTable table = createTableWithHeader(new String[]{"Canal ", "Fecha de envío", "Tipo de mensaje"}, fontBold);
            for (NotificationEntity oneMessage : notificationEntityList) {
                table.addCell(createCell(oneMessage.getChanel(), fontContent));
                if(oneMessage.getShippingDate() == null){
                    table.addCell(createCell("", fontContent));
                }else{
                    table.addCell(createCell(oneMessage.getShippingDate().getDayOfMonth() + "/"
                            + oneMessage.getShippingDate().getMonthValue() + "-"
                            + oneMessage.getShippingDate().getYear(), fontContent));
                }
                if (oneMessage.getNewsletterEntity() != null) {
                    table.addCell(createCell(oneMessage.getNewsletterEntity().getName(), fontContent));
                } else {
                    table.addCell(createCell(" Cita el día "+  oneMessage.getAppointmentEntity().getDate() + " a las "+oneMessage.getAppointmentEntity().getStartTime()
                            + " con " + oneMessage.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getName() + " "
                            + oneMessage.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getLastName(), fontContent));
                }
            }
            document.add(table); // Añadir la tabla al documento
            document.add(Chunk.NEWLINE); // Añadir una línea en blanco
        } catch (DocumentException e) {
            log.error("Error al crear el documento PDF", e);
        }

        if (document.isOpen()) {
            document.close();
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

    /**
     * Crea la celda con el texto y el tamaño de este para añadirlo a la tabla
     * @param text Texto
     * @param font Tamaño de la fuente
     * @return La celda completa
     */
    private PdfPCell createCell(String text, Font font) {
        return new PdfPCell(new Phrase(text, font));
    }

    /**
     * Crea el encabezado de la tabla
     * @param headers Texto de cabecera por cada fila de la tabla
     * @param headerFont Tamaño del texto
     * @return Encabezado de la tabla
     */
    private PdfPTable createTableWithHeader(String[] headers, Font headerFont) {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        for (String header : headers) {
            PdfPCell cell = createCell(header, headerFont);
            cell.setBackgroundColor(LIGHT_PURPLE);
            table.addCell(cell);
        }
        return table;
    }
}
