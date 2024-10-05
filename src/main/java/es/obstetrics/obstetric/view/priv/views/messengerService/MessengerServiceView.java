package es.obstetrics.obstetric.view.priv.views.messengerService;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import es.obstetrics.obstetric.backend.entity.MessengerServiceEntity;
import es.obstetrics.obstetric.backend.entity.UserCurrent;
import es.obstetrics.obstetric.backend.entity.UserEntity;
import es.obstetrics.obstetric.backend.service.MessengerServiceService;
import es.obstetrics.obstetric.backend.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del messengerService.
 */
public class MessengerServiceView extends VerticalLayout {
    private  List<MessageListItem> items;
    private  MessageList list;

    public MessengerServiceView(UserCurrent userCurrent, UserEntity userReceiver, MessengerServiceService messengerServiceService, UserService userService) {

        list = new MessageList();
        List<MessengerServiceEntity> messengerServiceEntities = messengerServiceService.findByReceiverAndSenderId(userReceiver, userCurrent.getCurrentUser().getId());
        messengerServiceEntities.addAll(messengerServiceService.findByReceiverAndSenderId(userCurrent.getCurrentUser(),userReceiver.getId()));
        messengerServiceEntities.sort(Comparator.comparing(MessengerServiceEntity::getTimestamp)); // Ordena los messengerServices para que se muestren de los más recientes a los menos
        items = new ArrayList<>();

        for(MessengerServiceEntity messengerServiceEntity : messengerServiceEntities){
            if(messengerServiceEntity.getReceiver().getId().equals(userCurrent.getCurrentUser().getId())
                && messengerServiceEntity.getReadingDate() == null){ //Compruebo si el mensaje no estaba leido y si es así lo actualizo a leido
                messengerServiceEntity.setReadingDate(Instant.now());
                messengerServiceService.save(messengerServiceEntity);
            }
            Optional<UserEntity> userEntity = (userService.findById(messengerServiceEntity.getSenderId()));
            MessageListItem newMessage = new MessageListItem(
                    messengerServiceEntity.getContent(), messengerServiceEntity.getTimestamp(),userEntity.get().getUsername());
            items.add(newMessage);
        }
        list.setItems(items);

        MessageInput input = getMessageInput(userCurrent, userReceiver, messengerServiceService);

        list.setSizeFull();
        input.setWidthFull();
        setHorizontalComponentAlignment(Alignment.CENTER,list);
        setHorizontalComponentAlignment(Alignment.CENTER,input);
        add(createHeader(userReceiver),list, input);

    }

    private MessageInput getMessageInput(UserCurrent userCurrent, UserEntity userReceiver, MessengerServiceService messengerServiceService) {
        MessageInput input = new MessageInput();

        input.addSubmitListener(submitEvent -> {
            MessengerServiceEntity messengerServiceEntity = new MessengerServiceEntity();
            messengerServiceEntity.setContent(submitEvent.getValue());
            messengerServiceEntity.setReceiver(userReceiver);
            messengerServiceEntity.setSenderId(userCurrent.getCurrentUser().getId());
            messengerServiceEntity.setTimestamp(Instant.now());
            messengerServiceService.save(messengerServiceEntity);

            MessageListItem newMessage = new MessageListItem(
                    submitEvent.getValue(), Instant.now(), userCurrent.getCurrentUser().getUsername());
            newMessage.setUserColorIndex(3);
            items.add(newMessage);
            list.setItems(items);
        });
        return input;
    }

    private Component createHeader(UserEntity userReceiver) {
        H3 titleHeader= new H3(userReceiver.getName()+" "+userReceiver.getLastName());
        VerticalLayout headerHl = new VerticalLayout(titleHeader, new H4(userReceiver.getRole()));
        headerHl.addClassName("header-messages");
        return headerHl;
    }

    public MessengerServiceView(){
        setSizeFull();
    }

}