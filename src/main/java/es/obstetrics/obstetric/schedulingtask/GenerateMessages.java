package es.obstetrics.obstetric.schedulingtask;

import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.NewsletterService;
import es.obstetrics.obstetric.backend.service.NotificationService;
import es.obstetrics.obstetric.backend.service.PregnanceService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class GenerateMessages {

    private final PregnanceService pregnanceService;
    private final NewsletterService newsletterService;
    private final NotificationService notificationService;
    private final AppointmentService appointmentService;

    private static final Logger log = LoggerFactory.getLogger(SendMessages.class);
    private  static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    public GenerateMessages(PregnanceService pregnanceService,
                            NewsletterService newsletterService,
                            AppointmentService appointmentService,
                            NotificationService notificationService){
        this.pregnanceService = pregnanceService;
        this.newsletterService = newsletterService;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;

    }

    /**
     * Generador de noticias, la tarea se ejecuta de manera asíncrona todos los días a la 1 de la madrugada para comprobar los embarazos activos.
     *      1. Se crea la lista con todos los embarazos.
     *      2. Se recorre dicha lista
     *          2.1 Se comprueba si el embarazo sigue activo y si es así se calcula la semana de cada embarazo de cada paciente.
     *          2.2 Dependiendo de la semana en la que esté, se calcula el trimestre al que pertenece.
     *          2.3 Se crea una lista de contenidos relacionados con el trimestre en el que se encuentre el embarazo.
     *          2.4 Se recorre la lista y se van añadiendo los mensajes con su canal correspondiente en el método addMessages.
     */
     @Scheduled(cron = "0 0 1 * * ?") //Se ejecutará todos los días a la 1 de la madrugada
    //  @Scheduled(fixedRate = 10000)
    public void generateNotices(){
       List<PregnanceEntity> pregnancyEntities = pregnanceService.findActivePregnancies();
       List<AppointmentEntity> appointmentEntitiesReminder = appointmentService.findByReminderAndState(ConstantUtilities.RESPONSE_YES, ConstantUtilities.STATE_ACTIVE);
       List<AppointmentEntity> appointmentEntitiesNotice = appointmentService.findByNoticeAndState(ConstantUtilities.RESPONSE_YES, ConstantUtilities.STATE_ACTIVE);

        for(PregnanceEntity pregnanceEntity : pregnancyEntities){ //Recorro la lista de embarazos, calculando la semana de cada uno de ellos
            CompletableFuture.runAsync(() -> {
                int quarter = Utilities.quarterCalculator(pregnanceEntity.getLastPeriodDate().toEpochDay()); //Calculo de la semana en la que se encuentra el embarazo
                List<NewsletterEntity> contentEntities;
                if(quarter >= 0 && quarter<=12){ //Primer trimestre
                   contentEntities =  newsletterService.findByQuarterAndState(ConstantUtilities.FIRST_QUARTER,ConstantUtilities.STATE_ACTIVE);
                }else if(quarter >= 13 && quarter<=24){ //Segundo trimestre
                    contentEntities =  newsletterService.findByQuarterAndState(ConstantUtilities.SECOND_QUARTER,ConstantUtilities.STATE_ACTIVE);
                }else{ //Tercer trimestre
                    contentEntities =  newsletterService.findByQuarterAndState(ConstantUtilities.THIRD_QUARTER,ConstantUtilities.STATE_ACTIVE);
                }
                for(NewsletterEntity oneNewsletter: newsletterService.findByQuarterAndState(ConstantUtilities.NONE_QUARTERER,ConstantUtilities.STATE_ACTIVE)){
                    for(PatientEntity patient: oneNewsletter.getPatients()){
                        if(pregnanceEntity.getPatientEntity().getId().equals(patient.getId())){
                            contentEntities.add(oneNewsletter);
                        }

                    }
                }
                addMessagesNewsletter(contentEntities, pregnanceEntity);
            });
        }

       for (AppointmentEntity oneAppointment : appointmentEntitiesReminder) { //Recorro la lista de citas y compruebo si faltan dos días para esta
           CompletableFuture.runAsync(() -> {
               if ((oneAppointment.getDate().equals(LocalDate.now().plusDays(2)) && oneAppointment.getReminder().equals(ConstantUtilities.RESPONSE_YES))) {
                   addMessagesAppointment(oneAppointment);
               }
           });
       }
       for (AppointmentEntity oneAppointment : appointmentEntitiesNotice) { //Recorro la lista de citas que han pedido notificación de esta
           CompletableFuture.runAsync(() -> addMessagesAppointment(oneAppointment));
       }
        log.info("The time to generate notices is now {}", dateFormat.format(new Date()));
    }

    /**
     * Añade los diferentes mensajes con sus cmampos correspondientes.
     * Recorre la lista de contenidos y comprueba el canal, si el usuario ha seleccionado algún canal:
     *      - Si se trata de ambos canales, se añaden dos mensajes.
     *      - Si se trata de un solo canal, se añade el canal correspondiente.
     * @param contentEntities Contenido sobre el que se va a iterar
     * @param pregnanceEntity Embarazo sobre le qeu se va a trabajar para guardar los mensajes.
     */
    private void addMessagesNewsletter(List<NewsletterEntity> contentEntities, PregnanceEntity pregnanceEntity) {
        for(NewsletterEntity content: contentEntities){ //Por cada embarazo se añaden el contenido
            if(pregnanceEntity.getPatientEntity().getChanel() != null){ //Si el usuario ha seleccionado algún canal
                CompletableFuture.runAsync(() -> {
                    NotificationEntity message = createMessage(ConstantUtilities.MESSAGE_NOT_DELIVERED, "", pregnanceEntity.getPatientEntity());
                    message.setNewsletterEntity(content);
                    if (pregnanceEntity.getPatientEntity().getChanel().equals("Ambas")) { //Si el usuario ha seleccionado ambas opciones, se guardan dos mensajes
                        NotificationEntity messageEmail = createMessage(ConstantUtilities.MESSAGE_NOT_DELIVERED, ConstantUtilities.MESSAGE_CHANEL_MAIL, pregnanceEntity.getPatientEntity());
                        messageEmail.setNewsletterEntity(content);
                        message.setChanel(ConstantUtilities.MESSAGE_CHANEL_APP);
                        if (notificationService.existsByNewsletterEntityAndUserEntityAndChanel(messageEmail)) {
                            notificationService.save(messageEmail);
                        }
                    } else { //Si el usuario ha seleccionado una única opción, se guarda
                        message.setChanel(pregnanceEntity.getPatientEntity().getChanel());
                    }

                    saveMessage(message);
                });
            }
        }
    }

    /**
     * Si se trata de un envío de una notificación, solo se añade el mensaje de correo. Por el contrario se añaden ambos mensajes.
     * @param appointmentEntity Cita sobre la que se van a crear los mensajes.
     */
    private void addMessagesAppointment(AppointmentEntity appointmentEntity) {
       CompletableFuture.runAsync(() -> {
            NotificationEntity messageMail = createAppointmentMessageMail(appointmentEntity);
            if(appointmentEntity.getNotice().equals(ConstantUtilities.RESPONSE_YES)){
                saveMessage(messageMail);
            }else{
                NotificationEntity messageApp = createAppointmentMessageApp(appointmentEntity);
                saveMessage(messageApp);
                saveMessage(messageMail);
            }
        });
    }

    private NotificationEntity createAppointmentMessageApp(AppointmentEntity appointmentEntity){
        NotificationEntity messageApp = createMessage(ConstantUtilities.MESSAGE_NOT_DELIVERED,ConstantUtilities.MESSAGE_CHANEL_APP, appointmentEntity.getPatientEntity());
        messageApp.setAppointmentEntity(appointmentEntity);
        return messageApp;
    }
    private NotificationEntity createAppointmentMessageMail(AppointmentEntity appointmentEntity) {
        NotificationEntity messageEmail = createMessage(ConstantUtilities.MESSAGE_NOT_DELIVERED,ConstantUtilities.MESSAGE_CHANEL_MAIL, appointmentEntity.getPatientEntity());
        messageEmail.setAppointmentEntity(appointmentEntity);
        return messageEmail;
    }

    private NotificationEntity createMessage(String state, String chanel, UserEntity userEntity){
        NotificationEntity message = new NotificationEntity();
        message.setState(state);
        message.setChanel(chanel);
        message.setUserEntity(userEntity);
        return message;
    }

    private void saveMessage(NotificationEntity message) {
        if(message.getAppointmentEntity() != null){
            if (!notificationService.existsByAppointmentEntity(message.getAppointmentEntity())) {
                notificationService.save(message);
            }
        }else if(message.getNewsletterEntity() != null){
            if(notificationService.existsByNewsletterEntityAndUserEntityAndChanel(message)){
                notificationService.save(message);
            }
        }
    }

}
