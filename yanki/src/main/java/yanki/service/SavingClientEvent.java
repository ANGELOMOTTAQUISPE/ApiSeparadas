package yanki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import yanki.config.events.Event;
import yanki.config.events.EventType;
import yanki.config.events.SavingClientCreatedEvent;
import yanki.model.Client;

import java.util.Date;
import java.util.UUID;
@Component
public class SavingClientEvent {
    @Autowired
    private KafkaTemplate<String, Event<?>> producer;

    @Value("${topic.customer.name:bank}")
    private String topicSaving;

    public void publish(Client savingAccount) {

        SavingClientCreatedEvent created = new SavingClientCreatedEvent();
        created.setData(savingAccount);
        created.setId(UUID.randomUUID().toString());
        created.setType(EventType.CREATED);
        created.setDate(new Date());

        this.producer.send(topicSaving, created);

    }
}
