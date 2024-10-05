package es.obstetrics.obstetric.backend.utilities;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Clase usada para guardar las distintas categorías, los valores se recogen del application.properties
 *      de la variable "${data.quarterer}" que contiene los valores separados por "," y se guardan en
 *      un array de string.
 */
@Component
@NoArgsConstructor
public class ConstantValues {

    @Value("${data.quarters}")
    private String dataQueterer;
    @Getter
    private String[] quarterer;

    @Value("${data.states}")
    private String dataStates;
    @Getter
    private String[] state;

    @Value("${data.roles}")
    private String dataRoles;
    @Getter
    private String[] role;

    @Value("${data.sex}")
    private String dataSex;
    @Getter
    private String[] sex;

    @Value("${data.typeContent}")
    private String dataTypeContent;
    @Getter
    private String[] typeContent;

    @Value("${data.duration}")
    private String dataDuration;
    @Getter
    private String[] duration;

    @Value("${data.week}")
    private String dataWeek;
    @Getter
    private String[] week;

    @Getter
    @Value("${data.url}")
    private String url;

    @Getter
    @Value("${date.message.newsletter.url}")
    private String contentMessageUrl;

    @Getter
    @Value("${date.message.forgotPassword}")
    private String messageForgotPassword;

    @Getter
    @Value("${date.message.appointment.day}")
    private String messageAppoinmentDay;

    @Getter
    @Value("${date.message.appointment.hour}")
    private String messageAppoinmentHour;

    @Value("${data.chanel}")
    private String dataChanel;
    @Getter
    private String[] chanel;

    @Value("${data.messageState}")
    private String dataMessageState;
    @Getter
    private String[] messageState;

    @Getter
    @Value("${date.email.admin}")
    private String emailAdmin;

    @Getter
    @Value("${date.consent}")
    private String dateConsent;

    @Getter
    @Value("${date.name.responsible}")
    private String nameResponsible;

    @Getter
    @Value("${date.email.responsible}")
    private String emailResponsible;

    @PostConstruct
    public void init(){
        quarterer = dataQueterer.split("\\,");
        state = dataStates.split("\\,");
        role = dataRoles.split("\\,");
        sex = dataSex.split("\\,");
        typeContent = dataTypeContent.split("\\,");
        duration = dataDuration.split("\\,");
        messageState = dataMessageState.split("\\,");
        chanel = dataChanel.split("\\,");
        week = dataWeek.split("\\,");
    }

}
