package yanki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yanki.model.Client;
import yanki.service.KafkaStringProducer;

@RestController
@RequestMapping(value = "/kafka")
public class KafkaController {

    private final KafkaStringProducer kafkaStringProducer;

    @Autowired
    KafkaController(KafkaStringProducer kafkaStringProducer) {
        this.kafkaStringProducer = kafkaStringProducer;
    }
/*
    @PostMapping(value = "/publish")
    public Mono<Client> sendMessageToKafkaTopic(@RequestBody Client message) {
        this.kafkaStringProducer.sendMessage(message);
        return Mono.just(message);
    }*/
    @PostMapping(value = "/publish")
    public Client save(@RequestBody Client savingService) {
        return this.kafkaStringProducer.save(savingService);
    }
}
