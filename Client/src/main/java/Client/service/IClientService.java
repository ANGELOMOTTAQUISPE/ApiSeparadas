package Client.service;

import Client.model.Client;
import reactor.core.publisher.Mono;

public interface IClientService extends ICRUD<Client, String> {
    Mono<Client> clientbydocumentNumber(String documentNumber);
}
