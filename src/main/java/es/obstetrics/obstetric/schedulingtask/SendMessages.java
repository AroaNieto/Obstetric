package es.obstetrics.obstetric.schedulingtask;

import es.obstetrics.obstetric.backend.entity.NotificationEntity;
import es.obstetrics.obstetric.backend.service.NotificationService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.backend.utilities.EmailUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
public class SendMessages {
    private final NotificationService notificationService;
    private final EmailUtility emailUtility;
    private static final Logger log = LoggerFactory.getLogger(SendMessages.class);
    private  static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final ConstantValues constantValues;
    private static final int BATCH_SIZE = 50;
    private final TaskExecutor taskExecutor;

    @Autowired
    public SendMessages(EmailUtility emailUtility,
                        NotificationService notificationService,
                        TaskExecutor taskExecutor,
                        ConstantValues constantValues){
        this.constantValues = constantValues;
        this.notificationService = notificationService;
        this.taskExecutor = taskExecutor;
        this.emailUtility = emailUtility;
    }

    /**
     * Tarea que se ejecutará todos los día sa las 12 de la noche, será la encargada de mandar las newsletter a los usuarios
     *      que hagan solicitado mandarla por email. Una vez mandada el mensaje se pondrá en entregado.
     */
      @Scheduled(cron = "0 */10 * * * ?") // Se ejecutará cada 10 minutos
    // @Scheduled(fixedRate = 10000)
    public void sendNotices(){
        List<NotificationEntity> notifications = notificationService.findByChanelAndMessageState(ConstantUtilities.MESSAGE_CHANEL_MAIL, ConstantUtilities.MESSAGE_NOT_DELIVERED); //Lista de mensajes

        for(int i = 0; i < notifications.size(); i += BATCH_SIZE) {
            //Crea una sublista de mensajes a partir del indice i hasta 100
            List<NotificationEntity> notifications100 = notifications.subList(i, Math.min(i + BATCH_SIZE, notifications.size()));
            processBatch(notifications100); //Procesa el lote de mensajes
        }

        log.info("The time to send notices is now  {}", dateFormat.format(new Date()));
    }

    /**
     * Envía correos de manera asincrona y actualiza el estado de los mensajes en un lote.
     * Cada correo electrónico se enviará por un hilo separado, mediante TaskExecutor
     *  esto permite que el hilo continue ejecutando otras tareas mientras
     *  se envían los correos.
     *
     * @param messages Lista de 100 mensajes
     */
    private void processBatch(List<NotificationEntity> messages) {
        for (NotificationEntity notificationEntity : messages) {
            taskExecutor.execute(() -> {
                try {
                    if(notificationEntity.getNewsletterEntity() != null){
                        emailUtility .sendEmail(notificationEntity.getUserEntity().getEmail(), "Nueva newsletter", getBody(notificationEntity));
                    }else if(notificationEntity.getAppointmentEntity() != null){
                        String calendarEvent = createICalendarEvent("Recordatorio de cita", "Citación con "+ notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity(), notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getCenterEntity().getCenterName(), notificationEntity.getAppointmentEntity().getDate(), notificationEntity.getAppointmentEntity().getStartTime(), notificationEntity.getAppointmentEntity().getDate(), notificationEntity.getAppointmentEntity().getEndTime());
                        emailUtility.sendEmail(notificationEntity.getUserEntity().getEmail(), "Cita", "El día "+notificationEntity.getAppointmentEntity().getDate()+
                                " tiene una cita a las "+notificationEntity.getAppointmentEntity().getStartTime().getHour()+":"+notificationEntity.getAppointmentEntity().getStartTime().getMinute()+
                                " con "+notificationEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity()+ ", puede agregarla al calendario. ", calendarEvent);
                    }
                    notificationEntity.setState(ConstantUtilities.MESSAGE_DELIVERED);
                    notificationEntity.setShippingDate(LocalDate.now());
                    notificationService.save(notificationEntity); // Se actualiza el mensaje como entregado

                } catch (Exception e) {
                    log.error("Error al enviar correo electrónico: {}", e.getMessage());
                }
            });
        }
    }

    public String createICalendarEvent(String summary, String description, String location,
                                       LocalDate startDate, LocalTime startTime,
                                       LocalDate endDate, LocalTime endTime) {

        ZonedDateTime startDateTime = ZonedDateTime.of(startDate, startTime, ZoneId.of("Europe/Madrid"));
        ZonedDateTime endDateTime = ZonedDateTime.of(endDate, endTime, ZoneId.of("Europe/Madrid"));

        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String startDateTimeFormatted = startDateTime.format(icsFormatter);
        String endDateTimeFormatted = endDateTime.format(icsFormatter);

        return "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID:-//hacksw/handcal//NONSGML v1.0//EN\n" +
                "BEGIN:VEVENT\n" +
                "UID:uid1@example.com\n" +
                "DTSTAMP:" + ZonedDateTime.now(ZoneId.of("UTC")).format(icsFormatter) + "\n" +
                "ORGANIZER;CN=Organizer Name:MAILTO:organizer@example.com\n" +
                "DTSTART:" + startDateTimeFormatted + "\n" +
                "DTEND:" + endDateTimeFormatted + "\n" +
                "SUMMARY:" + summary + "\n" +
                "DESCRIPTION:" + description + "\n" +
                "LOCATION:" + location + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";
    }

    /**
     * Crea el cuerpo que se enviará por correo a la paciente.
     *
     * @param notificationEntity Aviso a enviar.
     * @return El cuerpo del correo.
     */
    private String getBody(NotificationEntity notificationEntity) {
        String url = "";
        if(notificationEntity.getNewsletterEntity().getUrl() != null &&
                !notificationEntity.getNewsletterEntity().getUrl().isEmpty() &&
                !notificationEntity.getNewsletterEntity().getUrl().isBlank()){
            url = notificationEntity.getNewsletterEntity().getUrl();
        }else if(notificationEntity.getNewsletterEntity().getContentBytePdf() != null) {
            url = "http://"+constantValues.getUrl()+ConstantUtilities.ROUTE_USERS_NEWSLETTER+ notificationEntity.getNewsletterEntity().getId();
        }
        return notificationEntity.getUserEntity().getName() + ", " + constantValues.getContentMessageUrl()+
                " " + url;
    }
}
